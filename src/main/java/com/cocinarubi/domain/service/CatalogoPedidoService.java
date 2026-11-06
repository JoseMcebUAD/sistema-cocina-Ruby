package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TamanoPorcion;
import com.cocinarubi.DBConstants.TipoComida;
import com.cocinarubi.DBConstants.TipoDescuento;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.CodigoClienteRepository;
import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.domain.entity.CodigoCliente;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.BasicoPedidoExtra;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.ComplementoComidaPedido;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.DesayunoPedido;
import com.cocinarubi.domain.entity.Combo;
import com.cocinarubi.domain.entity.ComboPedido;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.PedidoCocina;
import com.cocinarubi.domain.entity.PedidoDomicilio;
import com.cocinarubi.domain.entity.PedidoDomicilioCocina;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.domain.entity.RegistroCliente;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.BasicoPedidoDTO;
import com.cocinarubi.presentation.dto.request.BasicoPedidoExtraDTO;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComplementoPedidoDTO;
import com.cocinarubi.presentation.dto.request.DesayunoPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComboPedidoDTO;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioCocinaDTO;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.request.ProductoCocinaPedidoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta la construcción del {@link Pedido} a partir de los DTOs de entrada.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Resolver entidades del catálogo (Comida, Desayuno, Básico, ProductoCocina, Complemento, Ruta).</li>
 *   <li>Construir y asociar las líneas hijas al {@link Pedido} usando los métodos {@code addXxx} de la entidad.</li>
 *   <li>Despachar la lógica de tipo de entrega según {@code pedidoCreadoDesde} y {@code tipoPedido}.</li>
 *   <li>Calcular el precio final sumando todas las líneas y la tarifa de entrega cuando aplica.</li>
 * </ul>
 *
 * <p>Se extrae de {@link PedidoService} para mantener a ese servicio enfocado en el ciclo de
 * vida transaccional del pedido, sin mezclar la resolución de referencias de catálogo.
 */
@Service
public class CatalogoPedidoService {

    private final ComidaService comidaService;
    private final DesayunoService desayunoService;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaService productoCocinaService;
    private final ComplementoService complementoService;
    private final RutaService rutaService;
    private final RegistroClienteRepository registroClienteRepository;
    private final ComboService comboService;
    private final CodigoClienteRepository codigoClienteRepository;

    public CatalogoPedidoService(ComidaService comidaService,
                                  DesayunoService desayunoService,
                                  BasicoRepository basicoRepository,
                                  ProductoCocinaService productoCocinaService,
                                  ComplementoService complementoService,
                                  RutaService rutaService,
                                  RegistroClienteRepository registroClienteRepository,
                                  ComboService comboService,
                                  CodigoClienteRepository codigoClienteRepository) {
        this.comidaService = comidaService;
        this.desayunoService = desayunoService;
        this.basicoRepository = basicoRepository;
        this.productoCocinaService = productoCocinaService;
        this.complementoService = complementoService;
        this.rutaService = rutaService;
        this.registroClienteRepository = registroClienteRepository;
        this.comboService = comboService;
        this.codigoClienteRepository = codigoClienteRepository;
    }

    /**
     * Asocia al pedido la entidad de entrega correcta según el canal de origen y el tipo de pedido.
     *
     * <p>PICK_UP y MOSTRADOR generan un {@link PedidoCocina} en lugar de una entidad de domicilio
     * porque ambas modalidades se gestionan presencialmente desde la cocina y comparten el mismo
     * flujo de registro de nombre de cliente (opcional).
     * Los pedidos WEB que no son DOMICILIO no requieren ninguna entidad de entrega adicional.
     */
    public void handleTipoPedido(Pedido pedido, PedidoRequestDTO dto) {
        if (dto.getPedidoCreadoDesde() == PedidoCreadoDesde.COCINA) {
            switch (dto.getTipoPedido()) {
                case DOMICILIO -> {
                    // Si venía de PICK_UP/MOSTRADOR, limpiar PedidoCocina antes de asignar domicilio
                    pedido.setPedidoCocina(null);
                    agregarDomicilioCocina(pedido, dto.getPedidoDomicilioCocina());
                }
                case PICK_UP, MOSTRADOR -> {
                    // Si venía de DOMICILIO, nullificar aquí (no en update()) para evitar
                    // "deleted object would be re-saved" — el null sin recreación es seguro.
                    pedido.setPedidoDomicilioCocina(null);
                    agregarPedidoCocina(pedido, dto.getNombreCliente());
                }
            }
        } else {
            // Canal WEB nunca usa PedidoCocina
            pedido.setPedidoCocina(null);
            switch (dto.getTipoPedido()) {
                case DOMICILIO -> agregarDomicilio(pedido, dto.getDomicilio());
                case PICK_UP, MOSTRADOR -> {
                    // Si venía de DOMICILIO, nullificar aquí para disparar el orphanRemoval
                    // sin el riesgo de re-save que existe cuando se crea un nuevo @MapsId en la misma sesión.
                    pedido.setPedidoDomicilio(null);
                }
            }
        }
    }

    /**
     * Agrega líneas de Comida al pedido.
     *
     * <p>Canal WEB: el servidor resuelve {@code precioUnitario} desde el catálogo (precio original
     * según porción) y activa {@code descuento/precioDescuento} si el catálogo tiene descuento vigente.
     * Canal COCINA: usa los valores del DTO directamente; valida que si {@code descuento=true},
     * {@code precioDescuento} sea válido y menor al precio original.
     */
    public void agregarComidas(Pedido pedido, List<ComidaPedidoDTO> lineas, PedidoCreadoDesde canal) {
        for (ComidaPedidoDTO linea : lineas) {
            Comida comida = comidaService.findById(linea.getIdComida());
            boolean descuento;
            BigDecimal precioUnitario;
            BigDecimal precioDescuento;

            if (canal == PedidoCreadoDesde.WEB) {
                // Precio original según porción — nunca el precio de descuento del catálogo
                precioUnitario = linea.getTamanoPorcion() == TamanoPorcion.ENTERA
                        ? comida.getPrecioEntera() : comida.getPrecioMedia();
                descuento = comida.isDescuentoActivo() && comida.getPrecioDescuento() != null;
                precioDescuento = descuento ? comida.getPrecioDescuento() : null;
            } else {
                precioUnitario = linea.getPrecioUnitario();
                descuento = linea.isDescuento();
                precioDescuento = linea.getPrecioDescuento();
                if (descuento) {
                    validarPrecioDescuento(precioDescuento, precioUnitario, "comida");
                }
            }

            ComidaPedido item = ComidaPedido.builder()
                    .comida(comida)
                    .precioUnitario(precioUnitario)
                    .descuento(descuento)
                    .precioDescuento(precioDescuento)
                    .tamanoPorcion(linea.getTamanoPorcion())
                    .build();
            agregarComplementosConLimite(item, linea.getComplementos(), comida.getLimiteComplemento());
            pedido.addComidaPedido(item);
        }
    }

    /**
     * Agrega los complementos de una línea de comida validando el límite de slots gratuitos.
     *
     * <p>Sin límite (null): usa el precio del catálogo para cada complemento.
     * Con límite: {@code cobrarSiempre} siempre se cobra y NO consume slots gratuitos;
     * los primeros {@code limite} no-cobrar en orden de la lista son gratuitos (precio=0);
     * los no-cobrar restantes deben traer {@code precioUnitario > 0} en el DTO.
     */
    private void agregarComplementosConLimite(ComidaPedido item,
                                               List<ComplementoPedidoDTO> dtos,
                                               Integer limite) {
        if (limite == null) {
            for (int i = 0; i < dtos.size(); i++) {
                Complemento c = complementoService.findById(dtos.get(i).getIdComplemento());
                item.addComplemento(ComplementoComidaPedido.builder()
                        .complemento(c)
                        .precioUnitario(c.getPrecioExtra())
                        .orden(i + 1)
                        .build());
            }
            return;
        }

        // Resolver entidades en el mismo orden que los DTOs
        List<Complemento> complementos = new ArrayList<>(dtos.size());
        for (ComplementoPedidoDTO dto : dtos) {
            complementos.add(complementoService.findById(dto.getIdComplemento()));
        }

        // cobrarSiempre NO consume slots; los no-cobrar en exceso del límite deben tener precio
        long countNoCobrar = complementos.stream().filter(c -> !c.isCobrarSiempre()).count();
        long exceso = Math.max(0L, countNoCobrar - limite);

        if (exceso > 0) {
            long noCobrarConPrecio = 0;
            for (int i = 0; i < dtos.size(); i++) {
                if (!complementos.get(i).isCobrarSiempre()) {
                    BigDecimal precio = dtos.get(i).getPrecioUnitario();
                    if (precio != null && precio.compareTo(BigDecimal.ZERO) > 0) {
                        noCobrarConPrecio++;
                    }
                }
            }
            if (noCobrarConPrecio < exceso) {
                throw new BusinessException(
                        "Al menos " + exceso + " complemento(s) exceden el límite permitido y deben tener un precio extra.",
                        HttpStatus.BAD_REQUEST);
            }
        }

        // Validar y persistir en orden; el servidor decide qué no-cobrar son gratuitos
        int slotsUsados = 0;
        for (int i = 0; i < dtos.size(); i++) {
            Complemento complemento = complementos.get(i);
            BigDecimal precio = dtos.get(i).getPrecioUnitario();

            if (complemento.isCobrarSiempre()) {
                if (precio == null || precio.compareTo(BigDecimal.ZERO) == 0) {
                    throw new BusinessException(
                            "El complemento '" + complemento.getNombreComplemento()
                                    + "' siempre se cobra y debe llevar un precio en la solicitud.",
                            HttpStatus.BAD_REQUEST);
                }
                validarPrecioVsCatalogo(complemento, precio);
            } else {
                if (slotsUsados < limite) {
                    precio = BigDecimal.ZERO;
                    slotsUsados++;
                } else {
                    if (precio == null || precio.compareTo(BigDecimal.ZERO) == 0) {
                        throw new BusinessException(
                                "El complemento '" + complemento.getNombreComplemento()
                                        + "' excede el límite gratuito y debe llevar un precio en la solicitud.",
                                HttpStatus.BAD_REQUEST);
                    }
                    validarPrecioVsCatalogo(complemento, precio);
                }
            }

            item.addComplemento(ComplementoComidaPedido.builder()
                    .complemento(complemento)
                    .precioUnitario(precio)
                    .orden(i + 1)
                    .build());
        }
    }

    private void validarPrecioVsCatalogo(Complemento complemento, BigDecimal precio) {
        BigDecimal precioEntidad = complemento.getPrecioExtra() != null
                ? complemento.getPrecioExtra() : BigDecimal.ZERO;
        if (precio.compareTo(precioEntidad) != 0) {
            throw new BusinessException(
                    "El precio del complemento '" + complemento.getNombreComplemento()
                            + "' no coincide con el catálogo.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void agregarDesayunos(Pedido pedido, List<DesayunoPedidoDTO> lineas, PedidoCreadoDesde canal) {
        for (DesayunoPedidoDTO linea : lineas) {
            Desayuno desayuno = desayunoService.findById(linea.getIdDesayuno());
            boolean descuento;
            BigDecimal precio;
            BigDecimal precioDescuento;

            if (canal == PedidoCreadoDesde.WEB) {
                precio = desayuno.getPrecioMedia();
                descuento = desayuno.isDescuentoActivo() && desayuno.getPrecioDescuento() != null;
                precioDescuento = descuento ? desayuno.getPrecioDescuento() : null;
            } else {
                precio = linea.getPrecio();
                descuento = linea.isDescuento();
                precioDescuento = linea.getPrecioDescuento();
                if (descuento) {
                    validarPrecioDescuento(precioDescuento, precio, "desayuno");
                }
            }

            pedido.addDesayunoPedido(DesayunoPedido.builder()
                    .desayuno(desayuno)
                    .precio(precio)
                    .descuento(descuento)
                    .precioDescuento(precioDescuento)
                    .build());
        }
    }

    public void agregarBasicos(Pedido pedido, List<BasicoPedidoDTO> lineas, PedidoCreadoDesde canal) {
        for (BasicoPedidoDTO linea : lineas) {
            Basico basico = basicoRepository.findByIdWithComplementos(linea.getIdBasico())
                    .orElseThrow(() -> new BusinessException(
                            "Básico no encontrado con id: " + linea.getIdBasico(), HttpStatus.BAD_REQUEST));
            boolean descuento;
            BigDecimal precioUnitario;
            BigDecimal precioDescuento;

            if (canal == PedidoCreadoDesde.WEB) {
                precioUnitario = basico.getPrecioBasico();
                descuento = basico.isDescuentoActivo() && basico.getPrecioDescuento() != null;
                precioDescuento = descuento ? basico.getPrecioDescuento() : null;
            } else {
                precioUnitario = linea.getPrecioUnitario();
                descuento = linea.isDescuento();
                precioDescuento = linea.getPrecioDescuento();
                if (descuento) {
                    validarPrecioDescuento(precioDescuento, precioUnitario, "básico");
                }
            }

            BasicoPedido item = BasicoPedido.builder()
                    .basico(basico)
                    .precioUnitario(precioUnitario)
                    .descuento(descuento)
                    .precioDescuento(precioDescuento)
                    .build();
            for (BasicoPedidoExtraDTO extraDto : linea.getExtras()) {
                Complemento c = complementoService.findById(extraDto.getIdComplemento());
                item.addExtra(BasicoPedidoExtra.builder()
                        .complemento(c)
                        .cantidad((byte) extraDto.getCantidad().intValue())
                        .precio(c.getPrecioExtra())
                        .build());
            }
            pedido.addBasicoPedido(item);
        }
    }

    public void agregarProductosCocina(Pedido pedido, List<ProductoCocinaPedidoDTO> lineas, PedidoCreadoDesde canal) {
        for (ProductoCocinaPedidoDTO linea : lineas) {
            ProductoCocina producto = productoCocinaService.findEntityById(linea.getIdProductoCocina());
            boolean descuento;
            BigDecimal precioUnitario;
            BigDecimal precioDescuento;

            if (canal == PedidoCreadoDesde.WEB) {
                // Precio original según tipo de pedido (sin aplicar el descuento de catálogo en precioUnitario)
                precioUnitario = pedido.getTipoPedido() == TipoPedido.DOMICILIO
                        ? producto.getPrecioDomicilio() : producto.getPrecioNormal();
                descuento = producto.isDescuentoActivo() && producto.getPrecioDescuento() != null;
                precioDescuento = descuento ? producto.getPrecioDescuento() : null;
            } else {
                precioUnitario = linea.getPrecioUnitario();
                descuento = linea.isDescuento();
                precioDescuento = linea.getPrecioDescuento();
                if (descuento) {
                    validarPrecioDescuento(precioDescuento, precioUnitario, "producto de cocina");
                }
            }

            pedido.addProductoCocinaPedido(ProductoCocinaPedido.builder()
                    .productoCocina(producto)
                    .precioUnitario(precioUnitario)
                    .descuento(descuento)
                    .precioDescuento(precioDescuento)
                    .cantidad(linea.getCantidad().byteValue())
                    .build());
        }
    }

    /**
     * Agrega líneas de Combo (promoción) al pedido. Valida que cada combo exista
     * y esté DISPONIBLE — un combo NO_DISPONIBLE/AGOTADO no puede venderse.
     */
    public void agregarCombos(Pedido pedido, List<ComboPedidoDTO> lineas, PedidoCreadoDesde canal) {
        for (ComboPedidoDTO linea : lineas) {
            Combo combo = comboService.findEntityById(linea.getIdCombo());
            if (combo.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El combo #" + combo.getIdCombo() + " no está disponible",
                        HttpStatus.BAD_REQUEST);
            }
            boolean descuento;
            BigDecimal precioUnitario;
            BigDecimal precioDescuento;

            if (canal == PedidoCreadoDesde.WEB) {
                precioUnitario = combo.getPrecio();
                descuento = combo.isDescuentoActivo() && combo.getPrecioDescuento() != null;
                precioDescuento = descuento ? combo.getPrecioDescuento() : null;
            } else {
                precioUnitario = linea.getPrecioUnitario();
                descuento = linea.isDescuento();
                precioDescuento = linea.getPrecioDescuento();
                if (descuento) {
                    validarPrecioDescuento(precioDescuento, precioUnitario, "combo");
                }
            }

            pedido.addComboPedido(ComboPedido.builder()
                    .combo(combo)
                    .precioUnitario(precioUnitario)
                    .descuento(descuento)
                    .precioDescuento(precioDescuento)
                    .cantidad(linea.getCantidad())
                    .build());
        }
    }

    /** Valida que precioDescuento sea válido cuando descuento=true en canal COCINA. */
    private void validarPrecioDescuento(BigDecimal precioDescuento, BigDecimal precioUnitario, String tipo) {
        if (precioDescuento == null || precioDescuento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Al aplicar descuento en " + tipo + ", precioDescuento debe ser mayor a cero.",
                    HttpStatus.BAD_REQUEST);
        }
        if (precioUnitario != null && precioDescuento.compareTo(precioUnitario) >= 0) {
            throw new BusinessException(
                    "Al aplicar descuento en " + tipo + ", precioDescuento debe ser menor al precioUnitario.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private static final BigDecimal DESCUENTO_COMIDAS_DIEZ = BigDecimal.TEN;
    private static final int MINIMO_COMIDAS_DESCUENTO = 12;

    /**
     * Aplica el descuento de volumen al pedido si corresponde.
     * Quedan excluidas del conteo y del descuento:
     * <ul>
     *   <li>Líneas en {@code excluidas} (consumidas por una oferta) — evita double-discount oferta/volumen.</li>
     *   <li>Líneas con {@code descuento=true} (ya tienen descuento de catálogo o manual) — evita acumulación.</li>
     * </ul>
     *
     * @param excluidas comidas ya consumidas por una oferta aplicada; vacío si no hay ofertas
     */
    public void aplicarDescuentoVolumen(Pedido pedido, java.util.Set<ComidaPedido> excluidas) {
        pedido.setTipoDescuento(null);

        List<ComidaPedido> calificadas = pedido.getComidasPedido().stream()
                .filter(cp -> !excluidas.contains(cp)
                        && !cp.isDescuento()
                        && cp.getTamanoPorcion() == TamanoPorcion.MEDIA
                        && cp.getComida() != null
                        && cp.getComida().getTipoComida() == TipoComida.FIJA)
                .toList();

        if (calificadas.size() >= MINIMO_COMIDAS_DESCUENTO) {
            calificadas.forEach(cp -> {
                cp.setDescuento(true);
                cp.setPrecioDescuento(cp.getPrecioUnitario().subtract(DESCUENTO_COMIDAS_DIEZ));
            });
            pedido.setTipoDescuento(TipoDescuento.COMIDAS_DIEZ);
        }
    }

    /** Delegado sin exclusiones — conserva la firma original para compatibilidad. */
    public void aplicarDescuentoVolumen(Pedido pedido) {
        aplicarDescuentoVolumen(pedido, java.util.Set.of());
    }

    public BigDecimal calcularTotal(Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;
        for (ComidaPedido cp : pedido.getComidasPedido()) {
            // Precio efectivo: precioDescuento si hay descuento, precioUnitario en caso contrario
            BigDecimal precioComida = cp.isDescuento() ? cp.getPrecioDescuento() : cp.getPrecioUnitario();
            total = total.add(precioComida);
            for (ComplementoComidaPedido ccp : cp.getComplementos()) {
                total = total.add(ccp.getPrecioUnitario());
            }
        }
        for (DesayunoPedido dp : pedido.getDesayunosPedido()) {
            BigDecimal precioDesayuno = dp.isDescuento() ? dp.getPrecioDescuento() : dp.getPrecio();
            total = total.add(precioDesayuno);
        }
        for (BasicoPedido bp : pedido.getBasicosPedido()) {
            BigDecimal precioBasico = bp.isDescuento() ? bp.getPrecioDescuento() : bp.getPrecioUnitario();
            total = total.add(precioBasico);
            for (BasicoPedidoExtra extra : bp.getExtras()) {
                total = total.add(extra.getPrecio().multiply(BigDecimal.valueOf(extra.getCantidad())));
            }
        }
        for (ProductoCocinaPedido pcp : pedido.getProductosCocina()) {
            BigDecimal precioProducto = pcp.isDescuento() ? pcp.getPrecioDescuento() : pcp.getPrecioUnitario();
            total = total.add(precioProducto.multiply(BigDecimal.valueOf(pcp.getCantidad())));
        }
        for (ComboPedido cp : pedido.getCombosPedido()) {
            BigDecimal precioCombo = cp.isDescuento() ? cp.getPrecioDescuento() : cp.getPrecioUnitario();
            total = total.add(precioCombo.multiply(BigDecimal.valueOf(cp.getCantidad())));
        }
        if (pedido.getPedidoDomicilio() != null) {
            // Usa el snapshot de tarifa guardado en PedidoDomicilio: refleja la tarifaEspecial
            // del CodigoCliente (si aplica) o la tarifaEnvio de la ruta (flujo normal).
            total = total.add(pedido.getPedidoDomicilio().getTarifa());
        }
        if (pedido.getPedidoDomicilioCocina() != null) {
            total = total.add(pedido.getPedidoDomicilioCocina().getPrecioTarifa());
        }
        return total;
    }

    private void agregarDomicilio(Pedido pedido, PedidoDomicilioDTO domicilioDto) {
        Ruta ruta = rutaService.findEntityById(domicilioDto.getIdRuta());
        BigDecimal tarifa = resolverTarifaDomicilio(domicilioDto.getCodigo(), ruta);
        if (pedido.getPedidoDomicilio() != null) {
            // Actualizar en lugar de recrear: PedidoDomicilio también usa @MapsId.
            PedidoDomicilio existente = pedido.getPedidoDomicilio();
            existente.setRuta(ruta);
            existente.setDireccion(domicilioDto.getDireccion());
            existente.setCodigo(domicilioDto.getCodigo());
            existente.setTarifa(tarifa);
            return;
        }
        pedido.setPedidoDomicilio(PedidoDomicilio.builder()
                .pedido(pedido)
                .ruta(ruta)
                .direccion(domicilioDto.getDireccion())
                .codigo(domicilioDto.getCodigo())
                .tarifa(tarifa)
                .build());
    }

    /**
     * Devuelve la tarifa de envío según el origen del precio:
     * si el pedido viene con un CodigoCliente, consulta su tarifaEspecial en la BD;
     * de lo contrario usa la tarifaEnvio de la ruta geográfica.
     */
    private BigDecimal resolverTarifaDomicilio(String codigo, Ruta ruta) {
        if (codigo != null && !codigo.isBlank()) {
            // CodigoCliente: la tarifa viene del registro del código, no de la ruta General.
            CodigoCliente codigoEnt = codigoClienteRepository.findByCodigoCliente(codigo)
                    .orElseThrow(() -> new BusinessException(
                            "Código de cliente no encontrado: " + codigo, HttpStatus.BAD_REQUEST));
            return codigoEnt.getTarifaEspecial();
        }
        return ruta.getTarifaEnvio();
    }

    private void agregarDomicilioCocina(Pedido pedido, PedidoDomicilioCocinaDTO dto) {
        RegistroCliente cliente = registroClienteRepository.findById(dto.getIdRegistroCliente())
                .orElseThrow(() -> new BusinessException(
                        "Registro de cliente no encontrado con id: " + dto.getIdRegistroCliente(),
                        HttpStatus.BAD_REQUEST));
        Ruta ruta = rutaService.findEntityById(dto.getIdRuta());
        if (pedido.getPedidoDomicilioCocina() != null) {
            // Actualizar en lugar de recrear: mismo patrón que PedidoCocina.
            // PedidoDomicilioCocina usa @MapsId — crear un nuevo objeto con la misma PK
            // dentro de la sesión provoca "deleted object would be re-saved by cascade".
            PedidoDomicilioCocina existente = pedido.getPedidoDomicilioCocina();
            existente.setRegistroCliente(cliente);
            existente.setRuta(ruta);
            existente.setDomicilio(dto.getDomicilio());
            existente.setPrecioTarifa(dto.getTarifa());
            return;
        }
        pedido.setPedidoDomicilioCocina(PedidoDomicilioCocina.builder()
                .pedido(pedido)
                .registroCliente(cliente)
                .ruta(ruta)
                .domicilio(dto.getDomicilio())
                .precioTarifa(dto.getTarifa())
                .build());
    }

    private void agregarPedidoCocina(Pedido pedido, String nombreCliente) {
        if (pedido.getPedidoCocina() != null) {
            // Actualizar en lugar de recrear: evita DuplicateKeyException de Hibernate porque
            // PedidoCocina usa @MapsId y comparte PK con Pedido — crear un nuevo objeto con
            // la misma PK dentro de la misma sesión provoca conflicto en el first-level cache.
            pedido.getPedidoCocina().setNombreCliente(nombreCliente);
            return;
        }
        pedido.setPedidoCocina(PedidoCocina.builder()
                .pedido(pedido)
                .nombreCliente(nombreCliente)
                .build());
    }
}

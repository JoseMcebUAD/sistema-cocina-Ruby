package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.ComplementoComidaPedido;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.DesayunoPedido;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.PedidoDomicilio;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.BasicoPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.DesayunoPedidoDTO;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.request.ProductoCocinaPedidoDTO;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaPedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregate root del sistema de órdenes. Gestiona el ciclo de vida completo del
 * {@link Pedido} junto con sus líneas hijas y la decisión condicional de
 * {@link PedidoDomicilio} basada en {@link TipoPedido}.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;
    private final RutaRepository rutaRepository;
    private final PedidoValidationImp pedidoValidation;
    private final PedidoConfirmationImp pedidoConfirmation;

    public PedidoService(PedidoRepository pedidoRepository,
                         ComidaRepository comidaRepository,
                         DesayunoRepository desayunoRepository,
                         BasicoRepository basicoRepository,
                         ProductoCocinaRepository productoCocinaRepository,
                         ComplementoRepository complementoRepository,
                         RutaRepository rutaRepository,
                         PedidoValidationImp pedidoValidation,
                         PedidoConfirmationImp pedidoConfirmation) {
        this.pedidoRepository = pedidoRepository;
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.basicoRepository = basicoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
        this.rutaRepository = rutaRepository;
        this.pedidoValidation = pedidoValidation;
        this.pedidoConfirmation = pedidoConfirmation;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findAll() {
        return pedidoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO dto) {
        pedidoValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            pedidoConfirmation.validarPost(dto);
        }
        Pedido pedido = Pedido.builder()
                .metodoPago(dto.getMetodoPago())
                .tipoPedido(dto.getTipoPedido())
                .pedidoCreadoDesde(dto.getPedidoCreadoDesde())
                .pagoCliente(dto.getPagoCliente())
                .uuidCliente(dto.getUuidCliente())
                .fechaExpedicionPedido(LocalDateTime.now())
                .impreso(false)
                .build();

        agregarComidas(pedido, dto.getComidas());
        agregarDesayunos(pedido, dto.getDesayunos());
        agregarBasicos(pedido, dto.getBasicos());
        agregarProductosCocina(pedido, dto.getProductosCocina());
        handleTipoPedido(pedido, dto);

        pedido.setPrecioFinalOrden(calcularTotal(pedido));
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO update(int id, PedidoRequestDTO dto) {
        pedidoValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            pedidoConfirmation.validarPost(dto);
        }
        Pedido existente = findEntityById(id);
        existente.setMetodoPago(dto.getMetodoPago());
        existente.setTipoPedido(dto.getTipoPedido());
        existente.setPedidoCreadoDesde(dto.getPedidoCreadoDesde());
        existente.setPagoCliente(dto.getPagoCliente());
        existente.setUuidCliente(dto.getUuidCliente());

        // Reemplazar todas las líneas (orphanRemoval limpiará las anteriores)
        existente.getComidasPedido().clear();
        existente.getDesayunosPedido().clear();
        existente.getBasicosPedido().clear();
        existente.getProductosCocina().clear();
        existente.setPedidoDomicilio(null);

        agregarComidas(existente, dto.getComidas());
        agregarDesayunos(existente, dto.getDesayunos());
        agregarBasicos(existente, dto.getBasicos());
        agregarProductosCocina(existente, dto.getProductosCocina());
        handleTipoPedido(existente, dto);

        existente.setPrecioFinalOrden(calcularTotal(existente));
        return toResponseDTO(pedidoRepository.save(existente));
    }

    public void delete(int id) {
        if (!pedidoRepository.existsById(id)) {
            throw new BusinessException(
                    "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        pedidoRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Handler de tipo de pedido: decide si crea PedidoDomicilio
    // -------------------------------------------------------------------------

    /**
     * Decide qué construir según el {@link TipoPedido}:
     *   DOMICILIO  → crea y vincula {@link PedidoDomicilio}.
     *   PICK_UP    → no crea domicilio.
     *   MOSTRADOR  → no crea domicilio.
     */
    private void handleTipoPedido(Pedido pedido, PedidoRequestDTO dto) {
        switch (dto.getTipoPedido()) {
            case DOMICILIO -> agregarDomicilio(pedido, dto.getDomicilio());
            case PICK_UP, MOSTRADOR -> {
                // nada que hacer: el pedido se recoge o entrega sin datos de envío
            }
        }
    }

    private void agregarDomicilio(Pedido pedido, PedidoDomicilioDTO domicilioDto) {
        Ruta ruta = rutaRepository.findById(domicilioDto.getIdRuta())
                .orElseThrow(() -> new BusinessException(
                        "Ruta no encontrada con id: " + domicilioDto.getIdRuta(),
                        HttpStatus.BAD_REQUEST));
        PedidoDomicilio domicilio = PedidoDomicilio.builder()
                .pedido(pedido)
                .ruta(ruta)
                .direccion(domicilioDto.getDireccion())
                .codigo(domicilioDto.getCodigo())
                .build();
        pedido.setPedidoDomicilio(domicilio);
    }

    // -------------------------------------------------------------------------
    // Builders de líneas
    // -------------------------------------------------------------------------

    private void agregarComidas(Pedido pedido, List<ComidaPedidoDTO> lineas) {
        for (ComidaPedidoDTO linea : lineas) {
            Comida comida = comidaRepository.findById(linea.getIdComida())
                    .orElseThrow(() -> new BusinessException(
                            "Comida no encontrada", HttpStatus.BAD_REQUEST));
            ComidaPedido item = ComidaPedido.builder()
                    .comida(comida)
                    .precioUnitario(linea.getPrecioUnitario())
                    .tamanoPorcion(linea.getTamanoPorcion())
                    .build();
            for (Integer idComp : linea.getIdComplementos()) {
                Complemento c = complementoRepository.findById(idComp)
                        .orElseThrow(() -> new BusinessException(
                                "Complemento no encontrado", HttpStatus.BAD_REQUEST));
                item.addComplemento(ComplementoComidaPedido.builder()
                        .complemento(c)
                        .precioUnitario(c.getPrecioExtra())
                        .build());
            }
            pedido.addComidaPedido(item);
        }
    }

    private void agregarDesayunos(Pedido pedido, List<DesayunoPedidoDTO> lineas) {
        for (DesayunoPedidoDTO linea : lineas) {
            Desayuno desayuno = desayunoRepository.findById(linea.getIdDesayuno())
                    .orElseThrow(() -> new BusinessException(
                            "Desayuno no encontrado", HttpStatus.BAD_REQUEST));
            pedido.addDesayunoPedido(DesayunoPedido.builder()
                    .desayuno(desayuno)
                    .precio(linea.getPrecio())
                    .build());
        }
    }

    private void agregarBasicos(Pedido pedido, List<BasicoPedidoDTO> lineas) {
        for (BasicoPedidoDTO linea : lineas) {
            Basico basico = basicoRepository.findById(linea.getIdBasico())
                    .orElseThrow(() -> new BusinessException(
                            "Básico no encontrado", HttpStatus.BAD_REQUEST));
            pedido.addBasicoPedido(BasicoPedido.builder()
                    .basico(basico)
                    .precioUnitario(linea.getPrecioUnitario())
                    .build());
        }
    }

    private void agregarProductosCocina(Pedido pedido, List<ProductoCocinaPedidoDTO> lineas) {
        for (ProductoCocinaPedidoDTO linea : lineas) {
            ProductoCocina producto = productoCocinaRepository.findById(linea.getIdProductoCocina())
                    .orElseThrow(() -> new BusinessException(
                            "Producto de cocina no encontrado", HttpStatus.BAD_REQUEST));
            pedido.addProductoCocinaPedido(ProductoCocinaPedido.builder()
                    .productoCocina(producto)
                    .precioUnitario(linea.getPrecioUnitario())
                    .cantidad(linea.getCantidad().byteValue())
                    .build());
        }
    }

    // -------------------------------------------------------------------------
    // Cálculo de total
    // -------------------------------------------------------------------------

    /** Suma todas las líneas + tarifa de envío si aplica. */
    private BigDecimal calcularTotal(Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;

        for (ComidaPedido cp : pedido.getComidasPedido()) {
            total = total.add(cp.getPrecioUnitario());
            for (ComplementoComidaPedido ccp : cp.getComplementos()) {
                total = total.add(ccp.getPrecioUnitario());
            }
        }
        for (DesayunoPedido dp : pedido.getDesayunosPedido()) {
            total = total.add(dp.getPrecio());
        }
        for (BasicoPedido bp : pedido.getBasicosPedido()) {
            total = total.add(bp.getPrecioUnitario());
        }
        for (ProductoCocinaPedido pcp : pedido.getProductosCocina()) {
            BigDecimal subtotal = pcp.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(pcp.getCantidad()));
            total = total.add(subtotal);
        }
        if (pedido.getPedidoDomicilio() != null && pedido.getPedidoDomicilio().getRuta() != null) {
            total = total.add(pedido.getPedidoDomicilio().getRuta().getTarifaEnvio());
        }
        return total;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Pedido findEntityById(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        List<ComidaPedidoResponseDTO> comidas = pedido.getComidasPedido().stream()
                .map(this::toComidaPedidoDTO).collect(Collectors.toList());
        List<DesayunoPedidoResponseDTO> desayunos = pedido.getDesayunosPedido().stream()
                .map(this::toDesayunoPedidoDTO).collect(Collectors.toList());
        List<BasicoPedidoResponseDTO> basicos = pedido.getBasicosPedido().stream()
                .map(this::toBasicoPedidoDTO).collect(Collectors.toList());
        List<ProductoCocinaPedidoResponseDTO> productos = pedido.getProductosCocina().stream()
                .map(this::toProductoCocinaPedidoDTO).collect(Collectors.toList());
        PedidoDomicilioResponseDTO domicilio = pedido.getPedidoDomicilio() != null
                ? toDomicilioDTO(pedido.getPedidoDomicilio())
                : null;

        BigDecimal cambio = null;
        if (pedido.getPagoCliente() != null && pedido.getPrecioFinalOrden() != null) {
            cambio = pedido.getPagoCliente().subtract(pedido.getPrecioFinalOrden());
        }

        return new PedidoResponseDTO(
                pedido.getIdPedido(),
                pedido.getMetodoPago(),
                pedido.getTipoPedido(),
                pedido.getFechaExpedicionPedido(),
                pedido.getPedidoCreadoDesde(),
                pedido.getPrecioFinalOrden(),
                pedido.getPagoCliente(),
                cambio,
                pedido.getUuidCliente(),
                comidas, desayunos, basicos, productos, domicilio
        );
    }

    private ComidaPedidoResponseDTO toComidaPedidoDTO(ComidaPedido cp) {
        List<ComplementoResponseDTO> complementos = cp.getComplementos().stream()
                .map(ccp -> new ComplementoResponseDTO(
                        ccp.getComplemento().getIdComplemento(),
                        ccp.getComplemento().getNombreComplemento(),
                        ccp.getPrecioUnitario()))
                .collect(Collectors.toList());
        return new ComidaPedidoResponseDTO(
                cp.getIdComidaPedido(),
                cp.getComida().getIdComida(),
                cp.getComida().getNombreComida(),
                cp.getPrecioUnitario(),
                cp.getTamanoPorcion(),
                complementos
        );
    }

    private DesayunoPedidoResponseDTO toDesayunoPedidoDTO(DesayunoPedido dp) {
        return new DesayunoPedidoResponseDTO(
                dp.getIdDesayunoPedido(),
                dp.getDesayuno().getIdDesayuno(),
                dp.getDesayuno().getNombreDesayuno(),
                dp.getPrecio()
        );
    }

    private BasicoPedidoResponseDTO toBasicoPedidoDTO(BasicoPedido bp) {
        Basico b = bp.getBasico();
        List<ComplementoResponseDTO> comps = b.getComplementos().stream()
                .map(bc -> new ComplementoResponseDTO(
                        bc.getComplemento().getIdComplemento(),
                        bc.getComplemento().getNombreComplemento(),
                        bc.getComplemento().getPrecioExtra()))
                .collect(Collectors.toList());
        BasicoResponseDTO basicoDTO = new BasicoResponseDTO(
                b.getIdBasico(),
                b.getComida().getIdComida(),
                b.getComida().getNombreComida(),
                b.getDescripcion(),
                b.isDestacado(),
                b.getPrecioBasico(),
                comps
        );
        return new BasicoPedidoResponseDTO(bp.getIdBasicoPedido(), basicoDTO, bp.getPrecioUnitario());
    }

    private ProductoCocinaPedidoResponseDTO toProductoCocinaPedidoDTO(ProductoCocinaPedido pcp) {
        return new ProductoCocinaPedidoResponseDTO(
                pcp.getIdProductoCocinaPedido(),
                pcp.getProductoCocina().getIdProductoCocina(),
                pcp.getProductoCocina().getNombreProducto(),
                pcp.getPrecioUnitario(),
                pcp.getCantidad()
        );
    }

    private PedidoDomicilioResponseDTO toDomicilioDTO(PedidoDomicilio pd) {
        return new PedidoDomicilioResponseDTO(
                pd.getRuta() != null ? pd.getRuta().getIdRuta() : 0,
                pd.getRuta() != null ? pd.getRuta().getNombre() : null,
                pd.getDireccion(),
                pd.getCodigo()
        );
    }
}

package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.ComplementoComidaPedido;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.DesayunoPedido;
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
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComplementoPedidoDTO;
import com.cocinarubi.presentation.dto.request.DesayunoPedidoDTO;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.request.ProductoCocinaPedidoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public CatalogoPedidoService(ComidaService comidaService,
                                  DesayunoService desayunoService,
                                  BasicoRepository basicoRepository,
                                  ProductoCocinaService productoCocinaService,
                                  ComplementoService complementoService,
                                  RutaService rutaService,
                                  RegistroClienteRepository registroClienteRepository) {
        this.comidaService = comidaService;
        this.desayunoService = desayunoService;
        this.basicoRepository = basicoRepository;
        this.productoCocinaService = productoCocinaService;
        this.complementoService = complementoService;
        this.rutaService = rutaService;
        this.registroClienteRepository = registroClienteRepository;
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
                case DOMICILIO -> agregarDomicilioCocina(pedido, dto.getIdRegistroCliente());
                case PICK_UP, MOSTRADOR -> agregarPedidoCocina(pedido, dto.getNombreCliente());
            }
        } else {
            switch (dto.getTipoPedido()) {
                case DOMICILIO -> agregarDomicilio(pedido, dto.getDomicilio());
                case PICK_UP, MOSTRADOR -> { }
            }
        }
    }

    public void agregarComidas(Pedido pedido, List<ComidaPedidoDTO> lineas) {
        for (ComidaPedidoDTO linea : lineas) {
            Comida comida = comidaService.findById(linea.getIdComida());
            ComidaPedido item = ComidaPedido.builder()
                    .comida(comida)
                    .precioUnitario(linea.getPrecioUnitario())
                    .tamanoPorcion(linea.getTamanoPorcion())
                    .build();
            for (ComplementoPedidoDTO compDto : linea.getComplementos()) {
                Complemento c = complementoService.findById(compDto.getIdComplemento());
                item.addComplemento(ComplementoComidaPedido.builder()
                        .complemento(c)
                        .precioUnitario(c.getPrecioExtra())
                        .build());
            }
            pedido.addComidaPedido(item);
        }
    }

    public void agregarDesayunos(Pedido pedido, List<DesayunoPedidoDTO> lineas) {
        for (DesayunoPedidoDTO linea : lineas) {
            Desayuno desayuno = desayunoService.findById(linea.getIdDesayuno());
            pedido.addDesayunoPedido(DesayunoPedido.builder()
                    .desayuno(desayuno)
                    .precio(linea.getPrecio())
                    .build());
        }
    }

    public void agregarBasicos(Pedido pedido, List<BasicoPedidoDTO> lineas) {
        for (BasicoPedidoDTO linea : lineas) {
            Basico basico = basicoRepository.findByIdWithComplementos(linea.getIdBasico())
                    .orElseThrow(() -> new BusinessException(
                            "Básico no encontrado con id: " + linea.getIdBasico(), HttpStatus.BAD_REQUEST));
            pedido.addBasicoPedido(BasicoPedido.builder()
                    .basico(basico)
                    .precioUnitario(linea.getPrecioUnitario())
                    .build());
        }
    }

    public void agregarProductosCocina(Pedido pedido, List<ProductoCocinaPedidoDTO> lineas) {
        for (ProductoCocinaPedidoDTO linea : lineas) {
            ProductoCocina producto = productoCocinaService.findEntityById(linea.getIdProductoCocina());
            pedido.addProductoCocinaPedido(ProductoCocinaPedido.builder()
                    .productoCocina(producto)
                    .precioUnitario(linea.getPrecioUnitario())
                    .cantidad(linea.getCantidad().byteValue())
                    .build());
        }
    }

    public BigDecimal calcularTotal(Pedido pedido) {
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
            total = total.add(pcp.getPrecioUnitario().multiply(BigDecimal.valueOf(pcp.getCantidad())));
        }
        if (pedido.getPedidoDomicilio() != null && pedido.getPedidoDomicilio().getRuta() != null) {
            total = total.add(pedido.getPedidoDomicilio().getRuta().getTarifaEnvio());
        }
        if (pedido.getPedidoDomicilioCocina() != null) {
            total = total.add(pedido.getPedidoDomicilioCocina().getPrecioTarifa());
        }
        return total;
    }

    private void agregarDomicilio(Pedido pedido, PedidoDomicilioDTO domicilioDto) {
        Ruta ruta = rutaService.findEntityById(domicilioDto.getIdRuta());
        PedidoDomicilio domicilio = PedidoDomicilio.builder()
                .pedido(pedido)
                .ruta(ruta)
                .direccion(domicilioDto.getDireccion())
                .codigo(domicilioDto.getCodigo())
                .build();
        pedido.setPedidoDomicilio(domicilio);
    }

    private void agregarDomicilioCocina(Pedido pedido, Integer idRegistroCliente) {
        RegistroCliente cliente = registroClienteRepository.findById(idRegistroCliente)
                .orElseThrow(() -> new BusinessException(
                        "Registro de cliente no encontrado con id: " + idRegistroCliente,
                        HttpStatus.BAD_REQUEST));
        Ruta ruta = cliente.getRuta();
        PedidoDomicilioCocina domicilio = PedidoDomicilioCocina.builder()
                .pedido(pedido)
                .registroCliente(cliente)
                .ruta(ruta)
                .domicilio(cliente.getDireccion())
                .precioTarifa(ruta.getTarifaEnvio())
                .build();
        pedido.setPedidoDomicilioCocina(domicilio);
    }

    private void agregarPedidoCocina(Pedido pedido, String nombreCliente) {
        pedido.setPedidoCocina(PedidoCocina.builder()
                .pedido(pedido)
                .nombreCliente(nombreCliente)
                .build());
    }
}

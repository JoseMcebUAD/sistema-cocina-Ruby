package com.cocinarubi.domain.mapper;

import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.DesayunoPedido;
import com.cocinarubi.domain.entity.Paquete;
import com.cocinarubi.domain.entity.PaquetePedido;
import com.cocinarubi.domain.entity.PaqueteProducto;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.PedidoCocina;
import com.cocinarubi.domain.entity.PedidoDomicilio;
import com.cocinarubi.domain.entity.PedidoDomicilioCocina;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.domain.service.PaqueteService;
import com.cocinarubi.presentation.dto.response.BasicoPedidoExtraResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoPredeterminadoComidaResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PaquetePedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoCocinaResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioCocinaResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaPedidoResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convierte entidades de dominio relacionadas con {@link Pedido} a sus DTOs de respuesta.
 *
 * <p>El mapeo es manual (sin MapStruct) para tener control explícito sobre qué campos se exponen
 * y cómo se calculan valores derivados (p.ej. el cambio al cliente). Cada método público puede
 * reutilizarse de forma aislada desde tests o desde otros mappers que compongan respuestas más grandes.
 *
 * <p>Las tres sub-entidades de entrega ({@link PedidoDomicilio}, {@link PedidoDomicilioCocina},
 * {@link PedidoCocina}) son mutuamente excluyentes en el dominio; en la respuesta se incluyen
 * como campos nullable y el frontend decide cuál renderizar según {@code tipoPedido} y
 * {@code pedidoCreadoDesde}.
 */
@Component
public class PedidoMapper {

    private final PaqueteService paqueteService;

    public PedidoMapper(PaqueteService paqueteService) {
        this.paqueteService = paqueteService;
    }

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        List<ComidaPedidoResponseDTO> comidas = pedido.getComidasPedido().stream()
                .map(this::toComidaPedidoDTO).collect(Collectors.toList());
        List<DesayunoPedidoResponseDTO> desayunos = pedido.getDesayunosPedido().stream()
                .map(this::toDesayunoPedidoDTO).collect(Collectors.toList());
        List<BasicoPedidoResponseDTO> basicos = pedido.getBasicosPedido().stream()
                .map(this::toBasicoPedidoDTO).collect(Collectors.toList());
        List<ProductoCocinaPedidoResponseDTO> productos = pedido.getProductosCocina().stream()
                .map(this::toProductoCocinaPedidoDTO).collect(Collectors.toList());
        // Pre-computa nombres de productos de todos los paquetes en batch para evitar N+1 al mapear;
        // filtra nulls porque id_paquete puede ser NULL (SET NULL) si el paquete fue eliminado.
        List<Paquete> paquetesEntidades = pedido.getPaquetesPedido().stream()
                .map(PaquetePedido::getPaquete)
                .filter(p -> p != null)
                .collect(Collectors.toList());
        Map<TipoLineaPaquete, Map<Integer, String>> nombresPaquete = paquetesEntidades.isEmpty()
                ? Map.of()
                : paqueteService.resolverNombresPara(paquetesEntidades);
        List<PaquetePedidoResponseDTO> paquetes = pedido.getPaquetesPedido().stream()
                .map(pp -> toPaquetePedidoDTO(pp, nombresPaquete))
                .collect(Collectors.toList());
        PedidoDomicilioResponseDTO domicilio = pedido.getPedidoDomicilio() != null
                ? toDomicilioDTO(pedido.getPedidoDomicilio())
                : null;
        PedidoDomicilioCocinaResponseDTO domicilioCocina = pedido.getPedidoDomicilioCocina() != null
                ? toDomicilioCocinaDTO(pedido.getPedidoDomicilioCocina())
                : null;
        PedidoCocinaResponseDTO pedidoCocina = pedido.getPedidoCocina() != null
                ? toPedidoCocinaDTO(pedido.getPedidoCocina())
                : null;

        // El cambio solo es calculable cuando el cliente pagó en efectivo; con pagos exactos
        // (tarjeta, transferencia) pagoCliente llega null y el frontend lo omite del ticket.
        BigDecimal cambio = null;
        if (pedido.getPagoCliente() != null && pedido.getPrecioFinalOrden() != null) {
            cambio = pedido.getPagoCliente().subtract(pedido.getPrecioFinalOrden());
        }

        return new PedidoResponseDTO(
                pedido.getIdPedido(),
                pedido.getMetodoPagoPrincipal(),
                pedido.getMetodoPagoSecundario(),
                pedido.getTipoPedido(),
                pedido.getFechaExpedicionPedido(),
                pedido.getPedidoCreadoDesde(),
                pedido.getPrecioFinalOrden(),
                pedido.getPagoCliente(),
                cambio,
                pedido.getUuidCliente(),
                pedido.isPagado(),
                pedido.isImpreso(),
                pedido.getComentario(),
                comidas, desayunos, basicos, productos, paquetes, domicilio, domicilioCocina, pedidoCocina
        );
    }

    public PaquetePedidoResponseDTO toPaquetePedidoDTO(PaquetePedido pp,
                                                       Map<TipoLineaPaquete, Map<Integer, String>> nombres) {
        Paquete paquete = pp.getPaquete();
        // id_paquete puede ser NULL si el paquete fue eliminado (SET NULL migration V28)
        List<String> nombresProductos = new ArrayList<>(paquete.getProductos().size());
        for (PaqueteProducto linea : paquete.getProductos()) {
            String nombre = nombres.getOrDefault(linea.getTipoProducto(), Map.of())
                    .getOrDefault(linea.getIdProducto(), "(eliminado)");
            nombresProductos.add(nombre);
        }
        return new PaquetePedidoResponseDTO(
                pp.getIdPaquetePedido(),
                paquete.getIdPaquete(),
                paquete.getDescripcion(),
                pp.getPrecioUnitario(),
                pp.getCantidad(),
                nombresProductos
        );
    }

    public ComidaPedidoResponseDTO toComidaPedidoDTO(ComidaPedido cp) {
        // id_complemento puede ser NULL (SET NULL) si el complemento fue eliminado
        List<ComplementoResponseDTO> complementos = cp.getComplementos().stream()
                .map(ccp -> {
                    var comp = ccp.getComplemento();
                    return new ComplementoResponseDTO(
                            comp != null ? comp.getIdComplemento() : null,
                            comp != null ? comp.getNombreComplemento() : "(eliminado)",
                            ccp.getPrecioUnitario());
                })
                .collect(Collectors.toList());
        // id_comida puede ser NULL (SET NULL) si la comida fue eliminada
        var comida = cp.getComida();
        // Complementos predeterminados de la comida del catálogo (no los elegidos en el pedido)
        List<ComplementoPredeterminadoComidaResponseDTO> predeterminados =
                comida == null || comida.getComplementosPredeterminados() == null
                        ? List.of()
                        : comida.getComplementosPredeterminados().stream()
                                .map(c -> new ComplementoPredeterminadoComidaResponseDTO(
                                        c.getIdComplementoPredeterminadoComida(),
                                        c.getComplemento().getIdComplemento(),
                                        c.getComplemento().getNombreComplemento(),
                                        c.getCantidad()))
                                .collect(Collectors.toList());
        return new ComidaPedidoResponseDTO(
                cp.getIdComidaPedido(),
                comida != null ? comida.getIdComida() : null,
                comida != null ? comida.getNombreComida() : "(eliminado)",
                cp.getPrecioUnitario(),
                cp.getTamanoPorcion(),
                complementos,
                predeterminados
        );
    }

    public DesayunoPedidoResponseDTO toDesayunoPedidoDTO(DesayunoPedido dp) {
        // id_desayuno puede ser NULL (SET NULL) si el desayuno fue eliminado
        var desayuno = dp.getDesayuno();
        return new DesayunoPedidoResponseDTO(
                dp.getIdDesayunoPedido(),
                desayuno != null ? desayuno.getIdDesayuno() : null,
                desayuno != null ? desayuno.getNombreDesayuno() : "(eliminado)",
                dp.getPrecio()
        );
    }

    public BasicoPedidoResponseDTO toBasicoPedidoDTO(BasicoPedido bp) {
        Basico b = bp.getBasico();
        // id_basico puede ser NULL (SET NULL) si el básico fue eliminado
        BasicoResponseDTO basicoDTO = null;
        if (b != null) {
            // Los complementos de un Básico son fijos en el catálogo (no se eligen por línea de pedido
            // como en ComidaPedido), por eso se leen desde la entidad Basico y no desde BasicoPedido.
            // basico_complemento.id_complemento tiene CASCADE → si complemento se eliminó, la fila desaparece.
            List<ComplementoResponseDTO> comps = b.getComplementos().stream()
                    .map(bc -> new ComplementoResponseDTO(
                            bc.getComplemento().getIdComplemento(),
                            bc.getComplemento().getNombreComplemento(),
                            bc.getComplemento().getPrecioExtra()))
                    .collect(Collectors.toList());
            basicoDTO = new BasicoResponseDTO(
                    b.getIdBasico(),
                    b.getComida().getIdComida(),
                    b.getComida().getNombreComida(),
                    b.getDescripcion(),
                    b.isDestacado(),
                    b.getPrecioBasico(),
                    b.getEstatus(),
                    comps
            );
        }
        // id_complemento en basico_pedido_extra puede ser NULL (SET NULL) si el complemento fue eliminado
        List<BasicoPedidoExtraResponseDTO> extras = bp.getExtras().stream()
                .map(e -> {
                    var comp = e.getComplemento();
                    return new BasicoPedidoExtraResponseDTO(
                            e.getIdBasicoPedidoExtra(),
                            comp != null ? comp.getIdComplemento() : null,
                            comp != null ? comp.getNombreComplemento() : "(eliminado)",
                            e.getCantidad(),
                            e.getPrecio());
                })
                .collect(Collectors.toList());
        return new BasicoPedidoResponseDTO(bp.getIdBasicoPedido(), basicoDTO, bp.getPrecioUnitario(), extras);
    }

    public ProductoCocinaPedidoResponseDTO toProductoCocinaPedidoDTO(ProductoCocinaPedido pcp) {
        // id_producto_cocina puede ser NULL (SET NULL) si el producto fue eliminado
        var prod = pcp.getProductoCocina();
        return new ProductoCocinaPedidoResponseDTO(
                pcp.getIdProductoCocinaPedido(),
                prod != null ? prod.getIdProductoCocina() : null,
                prod != null ? prod.getNombreProducto() : "(eliminado)",
                pcp.getPrecioUnitario(),
                pcp.getCantidad()
        );
    }

    public PedidoDomicilioResponseDTO toDomicilioDTO(PedidoDomicilio pd) {
        // La ruta puede ser null si se eliminó del catálogo después de crear el pedido.
        PedidoDomicilioResponseDTO dto = new PedidoDomicilioResponseDTO(
                pd.getRuta() != null ? pd.getRuta().getIdRuta() : 0,
                pd.getRuta() != null ? pd.getRuta().getNombre() : null,
                pd.getDireccion(),
                pd.getCodigo()
        );
        dto.setTarifa(pd.getTarifa());
        dto.setTarifasEspeciales(pd.getTarifasEspeciales());
        return dto;
    }

    public PedidoDomicilioCocinaResponseDTO toDomicilioCocinaDTO(PedidoDomicilioCocina pdc) {
        PedidoDomicilioCocinaResponseDTO dto = new PedidoDomicilioCocinaResponseDTO(
                pdc.getIdPedido(),
                pdc.getRegistroCliente().getIdRegistroCliente(),
                pdc.getRegistroCliente().getNombre(),
                pdc.getRegistroCliente().getTelefono(),
                pdc.getRuta().getIdRuta(),
                pdc.getRuta().getNombre(),
                pdc.getDomicilio(),
                pdc.getPrecioTarifa()
        );
        dto.setTarifasEspeciales(pdc.getTarifasEspeciales());
        return dto;
    }

    public PedidoCocinaResponseDTO toPedidoCocinaDTO(PedidoCocina pc) {
        return new PedidoCocinaResponseDTO(pc.getIdPedido(), pc.getNombreCliente());
    }
}
package com.cocinarubi.domain.mapper;

import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.DesayunoPedido;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.PedidoCocina;
import com.cocinarubi.domain.entity.PedidoDomicilio;
import com.cocinarubi.domain.entity.PedidoDomicilioCocina;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoCocinaResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioCocinaResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaPedidoResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
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

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
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
                comidas, desayunos, basicos, productos, domicilio, domicilioCocina, pedidoCocina
        );
    }

    public ComidaPedidoResponseDTO toComidaPedidoDTO(ComidaPedido cp) {
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

    public DesayunoPedidoResponseDTO toDesayunoPedidoDTO(DesayunoPedido dp) {
        return new DesayunoPedidoResponseDTO(
                dp.getIdDesayunoPedido(),
                dp.getDesayuno().getIdDesayuno(),
                dp.getDesayuno().getNombreDesayuno(),
                dp.getPrecio()
        );
    }

    public BasicoPedidoResponseDTO toBasicoPedidoDTO(BasicoPedido bp) {
        Basico b = bp.getBasico();
        // Los complementos de un Básico son fijos en el catálogo (no se eligen por línea de pedido
        // como en ComidaPedido), por eso se leen desde la entidad Basico y no desde BasicoPedido.
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
                b.getEstatus(),
                comps
        );
        return new BasicoPedidoResponseDTO(bp.getIdBasicoPedido(), basicoDTO, bp.getPrecioUnitario());
    }

    public ProductoCocinaPedidoResponseDTO toProductoCocinaPedidoDTO(ProductoCocinaPedido pcp) {
        return new ProductoCocinaPedidoResponseDTO(
                pcp.getIdProductoCocinaPedido(),
                pcp.getProductoCocina().getIdProductoCocina(),
                pcp.getProductoCocina().getNombreProducto(),
                pcp.getPrecioUnitario(),
                pcp.getCantidad()
        );
    }

    public PedidoDomicilioResponseDTO toDomicilioDTO(PedidoDomicilio pd) {
        // La ruta puede ser null si se eliminó del catálogo después de crear el pedido.
        return new PedidoDomicilioResponseDTO(
                pd.getRuta() != null ? pd.getRuta().getIdRuta() : 0,
                pd.getRuta() != null ? pd.getRuta().getNombre() : null,
                pd.getDireccion(),
                pd.getCodigo()
        );
    }

    public PedidoDomicilioCocinaResponseDTO toDomicilioCocinaDTO(PedidoDomicilioCocina pdc) {
        return new PedidoDomicilioCocinaResponseDTO(
                pdc.getIdPedido(),
                pdc.getRegistroCliente().getIdRegistroCliente(),
                pdc.getRegistroCliente().getNombre(),
                pdc.getRegistroCliente().getTelefono(),
                pdc.getRuta().getIdRuta(),
                pdc.getRuta().getNombre(),
                pdc.getDomicilio(),
                pdc.getPrecioTarifa()
        );
    }

    public PedidoCocinaResponseDTO toPedidoCocinaDTO(PedidoCocina pc) {
        return new PedidoCocinaResponseDTO(pc.getIdPedido(), pc.getNombreCliente());
    }
}
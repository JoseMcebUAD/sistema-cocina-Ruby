package com.cocinarubi.domain.service;

import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.ComboPedido;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.OfertaPedido;
import com.cocinarubi.domain.entity.OfertaPedidoLinea;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.domain.model.OfertaAplicacion;
import com.cocinarubi.domain.model.OfertaAplicacion.LineaConsumo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fase 2 del guardado two-phase: construye y asocia al {@link Pedido} ya persistido
 * (con IDs disponibles) las entidades {@link OfertaPedido}/{@link OfertaPedidoLinea}
 * calculadas en fase 1 por {@link OfertaMatcherService}.
 *
 * <p>No persiste directamente; añade las entidades a la colección del pedido para que
 * el cascade de JPA las guarde en el siguiente {@code save}.</p>
 *
 * <p>Capa: Service — persistencia de resultados del matcher.
 */
@Service
public class OfertaAplicadorService {

    /**
     * Construye un {@link OfertaPedido} (con sus líneas) por cada aplicación y lo añade
     * al pedido. Debe llamarse después del {@code saveAndFlush} que asigna los IDs de las
     * líneas hijas ({@link ComidaPedido}, {@link ProductoCocinaPedido}, etc.).
     */
    public void guardar(Pedido pedido, List<OfertaAplicacion> aplicaciones) {
        for (OfertaAplicacion aplicacion : aplicaciones) {
            OfertaPedido ofertaPedido = OfertaPedido.builder()
                    .pedido(pedido)
                    .oferta(aplicacion.getOferta())
                    .precioAplicado(aplicacion.getPrecioAplicado())
                    .ahorro(aplicacion.getAhorro())
                    .build();
            for (LineaConsumo linea : aplicacion.getLineas()) {
                ofertaPedido.getLineas().add(OfertaPedidoLinea.builder()
                        .ofertaPedido(ofertaPedido)
                        .tipoEntidad(linea.getTipoEntidad())
                        .idLineaPedido(resolverIdLinea(linea))
                        .idProductoCatalogo(linea.getIdProductoCatalogo())
                        .nombreProducto(linea.getNombreProducto())
                        .cantidadConsumida(linea.getCantidadConsumida())
                        // Precio normal total de la línea = precioUnitario × cantidadConsumida
                        .precioNormalConsumido(linea.getPrecioNormal()
                                .multiply(java.math.BigDecimal.valueOf(linea.getCantidadConsumida())))
                        .build());
            }
            pedido.addOfertaPedido(ofertaPedido);
        }
    }

    /** Extrae el ID de la entidad de pedido ya flush (IDs disponibles tras saveAndFlush). */
    private int resolverIdLinea(LineaConsumo linea) {
        return switch (linea.getTipoEntidad()) {
            case COMIDA         -> ((ComidaPedido) linea.getLineaEntity()).getIdComidaPedido();
            case PRODUCTO_COCINA -> ((ProductoCocinaPedido) linea.getLineaEntity()).getIdProductoCocinaPedido();
            case BASICO         -> ((BasicoPedido) linea.getLineaEntity()).getIdBasicoPedido();
            case COMBO          -> ((ComboPedido) linea.getLineaEntity()).getIdComboPedido();
        };
    }
}

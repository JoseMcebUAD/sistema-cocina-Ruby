package com.cocinarubi.domain.model;

import com.cocinarubi.DBConstants.TipoEntidadOferta;
import com.cocinarubi.domain.entity.Oferta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Value object (sin persistencia) que representa el resultado de una aplicación
 * exitosa de una {@link Oferta} durante el algoritmo de matching.
 *
 * <p>Vive únicamente en memoria entre la detección (Phase 1) y la persistencia
 * (Phase 2) dentro de {@code PedidoService.save()}. No se almacena en base de datos
 * directamente; su contenido se traslada a {@link com.cocinarubi.domain.entity.OfertaPedido}
 * y {@link com.cocinarubi.domain.entity.OfertaPedidoLinea}.</p>
 *
 * <p>Capa: domain/model — value object.</p>
 */
public class OfertaAplicacion {

    private final Oferta oferta;
    private final BigDecimal precioAplicado;
    private final BigDecimal ahorro;
    private final List<LineaConsumo> lineas;

    public OfertaAplicacion(Oferta oferta, BigDecimal precioAplicado, BigDecimal ahorro, List<LineaConsumo> lineas) {
        this.oferta = oferta;
        this.precioAplicado = precioAplicado;
        this.ahorro = ahorro;
        this.lineas = lineas;
    }

    public Oferta getOferta() { return oferta; }
    public BigDecimal getPrecioAplicado() { return precioAplicado; }
    public BigDecimal getAhorro() { return ahorro; }
    public List<LineaConsumo> getLineas() { return lineas; }

    /**
     * Referencia a una línea del pedido que fue consumida por esta aplicación de oferta.
     * Guarda la entidad en memoria para poder leer su ID después de {@code saveAndFlush}.
     */
    public static class LineaConsumo {

        private final Object lineaEntity;
        private final TipoEntidadOferta tipoEntidad;
        private final Integer idProductoCatalogo;
        private final String nombreProducto;
        private final BigDecimal precioNormal;
        private final int cantidadConsumida;

        public LineaConsumo(Object lineaEntity, TipoEntidadOferta tipoEntidad,
                            Integer idProductoCatalogo, String nombreProducto,
                            BigDecimal precioNormal, int cantidadConsumida) {
            this.lineaEntity = lineaEntity;
            this.tipoEntidad = tipoEntidad;
            this.idProductoCatalogo = idProductoCatalogo;
            this.nombreProducto = nombreProducto;
            this.precioNormal = precioNormal;
            this.cantidadConsumida = cantidadConsumida;
        }

        public Object getLineaEntity() { return lineaEntity; }
        public TipoEntidadOferta getTipoEntidad() { return tipoEntidad; }
        public Integer getIdProductoCatalogo() { return idProductoCatalogo; }
        public String getNombreProducto() { return nombreProducto; }
        public BigDecimal getPrecioNormal() { return precioNormal; }
        public int getCantidadConsumida() { return cantidadConsumida; }
    }
}

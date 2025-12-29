package com.Model;

/**
 * Representa la entidad 'detalle_orden' de la base de datos.
 * Esta clase almacena los items individuales de una orden,
 * incluyendo especificaciones y precio de cada item.
 */
public class ModeloDetalleOrden {
    private int idDetalleOrden;
    private int idRelOrden;
    private String especificacionesDetalleOrden;
    private double precioDetalleOrden;

    public int getIdDetalleOrden() {
        return idDetalleOrden;
    }

    public void setIdDetalleOrden(int idDetalleOrden) {
        this.idDetalleOrden = idDetalleOrden;
    }

    public int getIdRelOrden() {
        return idRelOrden;
    }

    public void setIdRelOrden(int idRelOrden) {
        this.idRelOrden = idRelOrden;
    }

    public String getEspecificacionesDetalleOrden() {
        return especificacionesDetalleOrden;
    }

    public void setEspecificacionesDetalleOrden(String especificacionesDetalleOrden) {
        this.especificacionesDetalleOrden = especificacionesDetalleOrden;
    }

    public double getPrecioDetalleOrden() {
        return precioDetalleOrden;
    }

    public void setPrecioDetalleOrden(double precioDetalleOrden) {
        this.precioDetalleOrden = precioDetalleOrden;
    }
}

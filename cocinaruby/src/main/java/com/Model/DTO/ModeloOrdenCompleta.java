package com.Model.DTO;

import com.Model.ModeloOrden;
import com.Model.ModeloDetalleOrden;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que combina una orden con todos sus detalles.
 * Útil para mostrar la información completa de una orden en la UI,
 * incluyendo los items individuales que la componen.
 */
public class ModeloOrdenCompleta {
    private ModeloOrden orden;
    private List<ModeloDetalleOrden> detalles;

    public ModeloOrdenCompleta() {
        this.detalles = new ArrayList<>();
    }

    public ModeloOrdenCompleta(ModeloOrden orden, List<ModeloDetalleOrden> detalles) {
        this.orden = orden;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }

    public ModeloOrden getOrden() {
        return orden;
    }

    public void setOrden(ModeloOrden orden) {
        this.orden = orden;
    }

    public List<ModeloDetalleOrden> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ModeloDetalleOrden> detalles) {
        this.detalles = detalles;
    }

    /**
     * Agrega un detalle a la lista de detalles.
     *
     * @param detalle Detalle a agregar
     */
    public void agregarDetalle(ModeloDetalleOrden detalle) {
        this.detalles.add(detalle);
    }

    /**
     * Obtiene la cantidad total de items en la orden.
     *
     * @return Cantidad de items
     */
    public int getCantidadItems() {
        return detalles.size();
    }

    /**
     * Calcula el total de la orden sumando todos los detalles.
     * Este método es útil para verificación, aunque el total
     * también está almacenado en orden.getPrecioOrden().
     *
     * @return Total calculado
     */
    public double calcularTotal() {
        return detalles.stream()
                .mapToDouble(ModeloDetalleOrden::getPrecioDetalleOrden)
                .sum();
    }

    /**
     * Verifica si la orden tiene detalles.
     *
     * @return true si tiene al menos un detalle
     */
    public boolean tieneDetalles() {
        return !detalles.isEmpty();
    }
}

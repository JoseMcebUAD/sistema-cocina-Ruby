package com.Service.Factory;

import java.time.LocalDateTime;
import java.util.Locale;

import com.Model.ModeloOrden;
import com.Model.DTO.VIEW.ModeloVentasView;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenMostrador;

/**
 * Factory para construir una instancia de ModeloOrden según el tipo de cliente.
 */
public final class ModeloOrdenFactory {

    private ModeloOrdenFactory() {
    }

    public static ModeloOrden createFromSale(ModeloVentasView venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser null");
        }

        String tipo = normalizeType(venta.getTipoCliente());
        ModeloOrden orden;

        switch (tipo) {
            case "MESA":
                ModeloOrdenMesa ordenMesa = new ModeloOrdenMesa();
                ordenMesa.setNumeroMesa(extractMesaNumber(venta.getNombreCliente()));
                orden = ordenMesa;
                break;
            case "DOMICILIO":
                ModeloOrdenDomicilio ordenDomicilio = new ModeloOrdenDomicilio();
                ordenDomicilio.setNombreCliente(venta.getNombreCliente());
                ordenDomicilio.setTarifaDomicilio(venta.getTarifaDomicilio());
                orden = ordenDomicilio;
                break;
            case "MOSTRADOR":
            default:
                ModeloOrdenMostrador ordenMostrador = new ModeloOrdenMostrador();
                ordenMostrador.setNombrePersona(venta.getNombreCliente());
                orden = ordenMostrador;
                break;
        }

        fillCommonFields(orden, venta, tipo);
        return orden;
    }

    private static void fillCommonFields(ModeloOrden orden, ModeloVentasView venta, String tipo) {
        orden.setIdOrden(venta.getIdOrden());
        orden.setIdRelTipoPago(venta.getIdRelTipoPago());
        orden.setNombreTipoPago(venta.getNombreTipoPago());
        orden.setTipoCliente(tipo);
        orden.setFechaExpedicionOrden(
                venta.getFechaExpedicionOrden() != null ? venta.getFechaExpedicionOrden() : LocalDateTime.now());
        orden.setPrecioOrden(venta.getPrecioOrden());
        orden.setPagoCliente(venta.getPagoCliente());
        orden.setFacturado(venta.isFacturado());
    }

    private static String normalizeType(String tipoCliente) {
        if (tipoCliente == null) {
            return "MOSTRADOR";
        }
        String normalized = tipoCliente.trim().toUpperCase(Locale.ROOT);
        if ("MESA".equals(normalized) || "DOMICILIO".equals(normalized) || "MOSTRADOR".equals(normalized)) {
            return normalized;
        }
        return "MOSTRADOR";
    }

    private static String extractMesaNumber(String nombreCliente) {
        if (nombreCliente == null) {
            return "";
        }
        String clean = nombreCliente.trim();
        String upper = clean.toUpperCase(Locale.ROOT);
        if (upper.startsWith("MESA:")) {
            return clean.substring(5).trim();
        }
        if (upper.startsWith("MESA ")) {
            return clean.substring(5).trim();
        }
        return clean;
    }
}

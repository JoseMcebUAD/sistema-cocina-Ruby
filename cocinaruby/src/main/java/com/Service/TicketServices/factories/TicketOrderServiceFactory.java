package com.Service.TicketServices.factories;

import java.util.Locale;

import com.Model.DTO.ModeloRecibo;
import com.Service.TicketServices.implementations.MesaTicketOrderService;
import com.Service.TicketServices.implementations.DomicilioTicketOrderService;
import com.Service.TicketServices.implementations.MostradorTicketOrderService;

/**
 * Factory para crear el servicio de impresión según el tipo de cliente.
 */
public final class TicketOrderServiceFactory {

    private static final TicketOrderServiceFactory INSTANCE = new TicketOrderServiceFactory();

    private TicketOrderServiceFactory() {
    }

    public static TicketOrderServiceFactory getInstance() {
        return INSTANCE;
    }

    public AbstractTicketTemplateService create(ModeloRecibo recibo) {
        String type = normalizeType(recibo);
        switch (type) {
            case "MESA":
                return new MesaTicketOrderService(recibo);
            case "DOMICILIO":
                return new DomicilioTicketOrderService(recibo);
            case "MOSTRADOR":
            default:
                return new MostradorTicketOrderService(recibo);
        }
    }

    private String normalizeType(ModeloRecibo recibo) {
        if (recibo == null || recibo.getOrdenCompleta() == null || recibo.getOrdenCompleta().getOrden() == null) {
            return "MOSTRADOR";
        }
        String raw = recibo.getOrdenCompleta().getOrden().getTipoCliente();
        if (raw == null) {
            return "MOSTRADOR";
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("MESA".equals(normalized) || "DOMICILIO".equals(normalized) || "MOSTRADOR".equals(normalized)) {
            return normalized;
        }
        return "MOSTRADOR";
    }
}

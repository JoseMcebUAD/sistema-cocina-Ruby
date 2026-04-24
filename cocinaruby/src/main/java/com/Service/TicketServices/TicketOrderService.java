package com.Service.TicketServices;

import com.Model.DTO.ModeloRecibo;
import com.Service.TicketServices.factories.AbstractTicketTemplateService;
import com.Service.TicketServices.factories.TicketOrderServiceFactory;

/**
 * Fachada para impresión de tickets.
 * Delega en una implementación especializada por tipo de orden.
 */
public class TicketOrderService {

    private final TicketOrderServiceFactory factory = TicketOrderServiceFactory.getInstance();

    /**
     * Imprime un ticket seleccionando el servicio según el tipo de orden.
     *
     * @param factura Modelo con la información del ticket
     * @return resultado de la impresión
     */
    public PrintResultEnum printOrderTicket(ModeloRecibo factura) {
        AbstractTicketTemplateService service = factory.create(factura);
        return service.printOrderTicket();
    }

    /**
     * Enum para los posibles resultados de impresión.
     */
    public enum PrintResultEnum {
        SUCCESS("Impresión exitosa"),
        PRINTER_NOT_AVAILABLE("Impresora no disponible"),
        NO_PAPER("Se acabó el papel en la impresora"),
        PRINT_ERROR("Error al imprimir");

        private final String mensaje;

        PrintResultEnum(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() {
            return mensaje;
        }
    }
}

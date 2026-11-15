package com.cocinarubi.util.template.impl;

import com.cocinarubi.util.FormatearReciboService;
import com.cocinarubi.util.template.data.CorteCajaTicketData;
import com.github.anastaciocintra.escpos.EscPos;

import java.io.IOException;

/**
 * Template de corte de caja para tipo DOMICILIO.
 * El footer imprime el desglose pedido por pedido: cliente, método y monto.
 * Soporta pago doble (dos métodos en un mismo pedido).
 */
public final class DomicilioCorteCajaTemplate extends CorteCajaImpresionTemplate {

    public DomicilioCorteCajaTemplate(CorteCajaTicketData data,
                                       String[] lineasNombreNegocio,
                                       String separador,
                                       int anchoTicket,
                                       FormatearReciboService formatearReciboService) {
        super(data, lineasNombreNegocio, separador, anchoTicket, formatearReciboService);
    }

    /**
     * Desglose por cliente: nombre en bold, luego línea por cada método de pago.
     * Al final avance de papel y corte completo.
     */
    @Override
    protected void renderFooter(EscPos escpos) throws IOException {
        escpos.writeLF(separador).feed(1);

        for (CorteCajaTicketData.PedidoDesglose pedido : getData().getPedidos()) {
            escpos.writeLF(boldStyle, pedido.getNombreCliente());

            String lineaPpal = formatearReciboService.formatearLineaTotal(
                    "    " + pedido.getMetodoPagoPrincipal(),
                    FORMATO_PRECIO.format(pedido.getMontoPrincipal()),
                    anchoTicket);
            escpos.writeLF(lineaPpal);

            if (pedido.tienePagoDoble()) {
                String lineaSec = formatearReciboService.formatearLineaTotal(
                        "    " + pedido.getMetodoPagoSecundario(),
                        FORMATO_PRECIO.format(pedido.getMontoSecundario()),
                        anchoTicket);
                escpos.writeLF(lineaSec);
            }
            escpos.feed(1);
        }

        escpos.feed(5).cut(EscPos.CutMode.FULL);
    }
}

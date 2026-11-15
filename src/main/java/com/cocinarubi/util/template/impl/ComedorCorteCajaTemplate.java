package com.cocinarubi.util.template.impl;

import com.cocinarubi.util.FormatearReciboService;
import com.cocinarubi.util.template.data.CorteCajaTicketData;
import com.github.anastaciocintra.escpos.EscPos;

import java.io.IOException;

/**
 * Template de corte de caja para tipo COMEDOR + PICK-UP.
 * Solo imprime los totales (renderBody de la clase padre) y luego corta el papel.
 * No tiene desglose por cliente.
 */
public final class ComedorCorteCajaTemplate extends CorteCajaImpresionTemplate {

    public ComedorCorteCajaTemplate(CorteCajaTicketData data,
                                     String[] lineasNombreNegocio,
                                     String separador,
                                     int anchoTicket,
                                     FormatearReciboService formatearReciboService) {
        super(data, lineasNombreNegocio, separador, anchoTicket, formatearReciboService);
    }

    @Override
    protected void renderFooter(EscPos escpos) throws IOException {
        escpos.feed(5).cut(EscPos.CutMode.FULL);
    }
}

package com.Service.TicketServices.implementations;

import java.io.IOException;

import com.Model.DTO.ModeloRecibo;
import com.Model.Orden.ModeloOrdenMesa;
import com.Service.TicketServices.factories.AbstractTicketTemplateService;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.Style;

public final class MesaTicketOrderService extends AbstractTicketTemplateService {

    public MesaTicketOrderService(ModeloRecibo recibo) {
        super(recibo);
    }

    @Override
    protected void printTypeInfo(EscPos escpos, Style subtitle) throws IOException {
        escpos.write("Tipo: ").writeLF(subtitle, "MESA");
        ModeloRecibo factura = getRecibo();
        if (factura != null && factura.getOrdenCompleta() != null && factura.getOrdenCompleta().getOrden() instanceof ModeloOrdenMesa) {
            ModeloOrdenMesa ordenMesa = (ModeloOrdenMesa) factura.getOrdenCompleta().getOrden();
            String numeroMesa = ordenMesa.getNumeroMesa() != null ? ordenMesa.getNumeroMesa() : "";
            escpos.write("Mesa: ").writeLF(subtitle, numeroMesa);
        }
        escpos.feed(1);
    }
}

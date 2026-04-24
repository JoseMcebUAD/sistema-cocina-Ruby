package com.Service.TicketServices.implementations;

import java.io.IOException;

import com.Model.DTO.ModeloRecibo;
import com.Service.TicketServices.factories.AbstractTicketTemplateService;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.Style;

public final class MostradorTicketOrderService extends AbstractTicketTemplateService {

    public MostradorTicketOrderService(ModeloRecibo recibo) {
        super(recibo);
    }

    @Override
    protected void printTypeInfo(EscPos escpos, Style subtitle) throws IOException {
        escpos.write("Tipo: ").writeLF(subtitle, "MOSTRADOR").feed(1);
    }
}

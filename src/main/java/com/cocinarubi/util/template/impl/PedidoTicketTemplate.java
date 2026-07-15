package com.cocinarubi.util.template.impl;

import java.io.IOException;
import java.util.List;

import com.cocinarubi.Constants;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioResponseDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaPedidoResponseDTO;
import com.cocinarubi.util.FormatearReciboPedidoService;
import com.cocinarubi.util.template.AbstractOrderTemplate;
import com.cocinarubi.util.template.data.PedidoTicketData;
import com.github.anastaciocintra.escpos.EscPos;

public class PedidoTicketTemplate extends AbstractOrderTemplate<PedidoTicketData> {

    private final FormatearReciboPedidoService formatter = new FormatearReciboPedidoService();
    int anchoEfectivo = Constants.ANCHO_TICKET / 2;

    public PedidoTicketTemplate(PedidoTicketData data) {
        super(data);
    }

    @Override
    protected void renderSpecificDetails(EscPos escpos) throws IOException {
        PedidoTicketData data = getData();

        if (data.getFechaExpedicionPedido() != null) {
            escpos.writeLF(FORMATO_FECHA.format(data.getFechaExpedicionPedido()));
        }
        escpos.writeLF("Tipo: " + data.getTipoPedido());
        String pago = data.getMetodoPagoPrincipal() + (data.getMetodoPagoSecundario() != null ? " / " + data.getMetodoPagoSecundario() : "");
        escpos.writeLF("Pago: " + pago);
        escpos.writeLF(formatter.formatearLineaTotal("TOTAL", FORMATO_PRECIO.format(data.getPrecioFinalOrden())));
        escpos.writeLF(formatter.formatearLineaTotal("PAGO CLIENTE", FORMATO_PRECIO.format(data.getPagoCliente())));
        renderDomicilio(escpos, data.getDomicilio());
        escpos.feed(1);

        renderComidas(escpos, data.getComidas());
        renderDesayunos(escpos, data.getDesayunos());
        renderBasicos(escpos, data.getBasicos());
        renderProductosCocina(escpos, data.getProductosCocina());
    }

    @Override
    protected void renderFooter(EscPos escpos) throws IOException {
    }

    private void renderComidas(EscPos escpos, List<ComidaPedidoResponseDTO> comidas) throws IOException {
        if (comidas == null || comidas.isEmpty()) return;

        for (ComidaPedidoResponseDTO c : comidas) {
            String precio = FORMATO_PRECIO.format(c.getPrecioUnitario());
            for (String linea : formatter.formatProductBlock(c, precio, anchoEfectivo)) {
                if (linea.isEmpty()) escpos.feed(1);
                else escpos.writeLF(linea);
            }
        }
        escpos.writeLF(Constants.SEPARADOR_TICKET).feed(1);
    }

    private void renderDesayunos(EscPos escpos, List<DesayunoPedidoResponseDTO> desayunos) throws IOException {
        if (desayunos == null || desayunos.isEmpty()) return;
        for (DesayunoPedidoResponseDTO d : desayunos) {
            String precio = FORMATO_PRECIO.format(d.getPrecio());
            for (String linea : formatter.formatearDetalleOrden(d.getNombreDesayuno(), precio)) {
                escpos.writeLF(linea);
            }
        }
    }

    private void renderBasicos(EscPos escpos, List<BasicoPedidoResponseDTO> basicos) throws IOException {
        if (basicos == null || basicos.isEmpty()) return;
        for (BasicoPedidoResponseDTO b : basicos) {
            String precio = FORMATO_PRECIO.format(b.getPrecioUnitario());
            for (String linea : formatter.formatBasicoBlock(b, precio, anchoEfectivo)) {
                if (linea.isEmpty()) escpos.feed(1);
                else escpos.writeLF(linea);
            }
        }
    }

    private void renderProductosCocina(EscPos escpos, List<ProductoCocinaPedidoResponseDTO> productos) throws IOException {
        if (productos == null || productos.isEmpty()) return;
        for (ProductoCocinaPedidoResponseDTO p : productos) {
            String descripcion = p.getCantidad() + "x " + p.getNombreProducto();
            String precio = FORMATO_PRECIO.format(
                    p.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(p.getCantidad())));
            for (String linea : formatter.formatearDetalleOrden(descripcion, precio)) {
                escpos.writeLF(linea);
            }
        }
    }

    private void renderDomicilio(EscPos escpos, PedidoDomicilioResponseDTO domicilio) throws IOException {
        if (domicilio == null) return;
        escpos.feed(1);
        escpos.writeLF("DOMICILIO");
        if (domicilio.getNombreRuta() != null) {
            escpos.writeLF("Ruta: " + domicilio.getNombreRuta());
        }
        if (domicilio.getDireccion() != null) {
            escpos.writeLF("Dir: " + domicilio.getDireccion());
        }
        if (domicilio.getCodigo() != null) {
            escpos.writeLF("Cod: " + domicilio.getCodigo());
        }
        escpos.writeLF(Constants.SEPARADOR_TICKET);
    }
}

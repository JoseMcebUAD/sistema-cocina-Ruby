package com.cocinarubi.util.template.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.cocinarubi.Constants;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PaquetePedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioCocinaResponseDTO;
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

        // Encabezado: fecha, tipo y nombre de cliente (COCINA PICK_UP / MOSTRADOR / DOMICILIO)
        if (data.getFechaExpedicionPedido() != null) {
            escpos.writeLF(FORMATO_FECHA.format(data.getFechaExpedicionPedido()));
        }
        escpos.writeLF("Tipo: " + data.getTipoPedido());
        if (data.getNombreCliente() != null && !data.getNombreCliente().isBlank()) {
            escpos.writeLF("Cliente: " + data.getNombreCliente());
        }
        
        if (data.getMetodoPagoSecundario() != null) {
            printDobleMetodoPago(escpos, data);
        } else {
            escpos.writeLF("Método de pago: " + data.getMetodoPagoPrincipal().name());
            
        }
        // Método de pago y totales
        escpos.writeLF(subtitleStyle, formatter.formatearLineaTotal("TOTAL", FORMATO_PRECIO.format(data.getPrecioFinalOrden())));

        // Sección de entrega a domicilio (WEB usa PedidoDomicilioResponseDTO,
        // COCINA usa PedidoDomicilioCocinaResponseDTO)
        renderDomicilio(escpos, data.getDomicilio());
        renderDomicilioCocina(escpos, data.getDomicilioCocina());
        escpos.feed(1);

        // Líneas de productos
        renderComidas(escpos, data.getComidas());
        renderDesayunos(escpos, data.getDesayunos());
        renderBasicos(escpos, data.getBasicos());
        renderProductosCocina(escpos, data.getProductosCocina());
        renderPaquetes(escpos, data.getPaquetes());

        // Nota libre del operador (comentario del pedido)
        renderComentario(escpos, data.getComentario());

        escpos.feed(5).cut(EscPos.CutMode.FULL);
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
                else escpos.writeLF(subtitleStyle,linea);
            }
        }
        escpos.writeLF(Constants.SEPARADOR_TICKET).feed(1);
    }

    private void renderDesayunos(EscPos escpos, List<DesayunoPedidoResponseDTO> desayunos) throws IOException {
        if (desayunos == null || desayunos.isEmpty()) return;
        for (DesayunoPedidoResponseDTO d : desayunos) {
            String precio = FORMATO_PRECIO.format(d.getPrecio());
            for (String linea : formatter.formatearDetalleOrden(d.getNombreDesayuno(), precio)) {
                escpos.writeLF(subtitleStyle,linea);
            }
        }
    }

    private void renderBasicos(EscPos escpos, List<BasicoPedidoResponseDTO> basicos) throws IOException {
        if (basicos == null || basicos.isEmpty()) return;
        for (BasicoPedidoResponseDTO b : basicos) {
            String precio = FORMATO_PRECIO.format(b.getPrecioUnitario());
            for (String linea : formatter.formatBasicoBlock(b, precio, anchoEfectivo)) {
                if (linea.isEmpty()) escpos.feed(1);
                else escpos.writeLF(subtitleStyle,linea);
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
                escpos.writeLF(subtitleStyle, linea);
            }
        }
    }

    // Renderiza cada paquete como un bloque tipo comida: encabezado "PAQUETE <nombre>",
    // sublíneas "  - producto" por cada producto incluido, y precio (unitario × cantidad) alineado a la derecha.
    private void renderPaquetes(EscPos escpos, List<PaquetePedidoResponseDTO> paquetes) throws IOException {
        if (paquetes == null || paquetes.isEmpty()) return;
        for (PaquetePedidoResponseDTO p : paquetes) {
            String precio = FORMATO_PRECIO.format(
                    p.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(p.getCantidad())));
            for (String linea : formatter.formatPaqueteBlock(p, precio, anchoEfectivo)) {
                if (linea.isEmpty()) escpos.feed(1);
                else escpos.writeLF(subtitleStyle, linea);
            }
        }
    }

    // Domicilio WEB: ruta, tarifa base, tarifas especiales y dirección
    private void renderDomicilio(EscPos escpos, PedidoDomicilioResponseDTO domicilio) throws IOException {
        if (domicilio == null) return;
        escpos.feed(1);
        escpos.writeLF("DOMICILIO");
        if (domicilio.getNombreRuta() != null) {
            escpos.writeLF("Ruta: " + domicilio.getNombreRuta());
        }
        if (domicilio.getTarifa() != null) {
            escpos.writeLF(formatter.formatearLineaTotal("Tarifa", FORMATO_PRECIO.format(domicilio.getTarifa())));
        }
        if (domicilio.getTarifasEspeciales() != null) {
            escpos.writeLF(formatter.formatearLineaTotal("T. especial", FORMATO_PRECIO.format(domicilio.getTarifasEspeciales())));
        }
        if (domicilio.getDireccion() != null) {
            escpos.writeLF("Dir: " + domicilio.getDireccion());
        }
        if (domicilio.getCodigo() != null) {
            escpos.writeLF("Cod: " + domicilio.getCodigo());
        }
        escpos.writeLF(Constants.SEPARADOR_TICKET);
    }

    // Domicilio COCINA: nombre cliente, ruta, tarifa, tarifas especiales y dirección
    private void renderDomicilioCocina(EscPos escpos, PedidoDomicilioCocinaResponseDTO domicilio) throws IOException {
        if (domicilio == null) return;
        escpos.feed(1);
        escpos.writeLF("DOMICILIO");
        if (domicilio.getNombreRuta() != null) {
            escpos.writeLF("Ruta: " + domicilio.getNombreRuta());
        }
        if (domicilio.getPrecioTarifa() != null) {
            escpos.writeLF(formatter.formatearLineaTotal("Tarifa", FORMATO_PRECIO.format(domicilio.getPrecioTarifa())));
        }
        if (domicilio.getTarifasEspeciales() != null) {
            escpos.writeLF(formatter.formatearLineaTotal("T. especial", FORMATO_PRECIO.format(domicilio.getTarifasEspeciales())));
        }
        if (domicilio.getDomicilio() != null) {
            escpos.writeLF("Dir: " + domicilio.getDomicilio());
        }
        if (domicilio.getTelefono() != null) {
            escpos.writeLF("Tel: " + domicilio.getTelefono());
        }
        escpos.writeLF(Constants.SEPARADOR_TICKET);
    }

    private void printDobleMetodoPago(EscPos escpos, PedidoTicketData data) throws IOException {
        BigDecimal pagoSecundario = data.getPrecioFinalOrden().subtract(data.getPagoCliente());
        escpos.write("Pago en " + data.getMetodoPagoPrincipal().name() + ": ")
              .writeLF(subtitleStyle, FORMATO_PRECIO.format(data.getPagoCliente()));
        escpos.write("Pago en " + data.getMetodoPagoSecundario().name() + ": ")
              .writeLF(subtitleStyle, FORMATO_PRECIO.format(pagoSecundario));
    }

    // Nota libre del operador; se imprime al final antes del corte
    private void renderComentario(EscPos escpos, String comentario) throws IOException {
        if (comentario == null || comentario.isBlank()) return;
        escpos.feed(1);
        escpos.writeLF(Constants.SEPARADOR_TICKET);
        escpos.writeLF("Nota: " + comentario);
    }
}

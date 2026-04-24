package com.Service.TicketServices.factories;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintService;

import com.Config.Constants;
import com.Model.ModeloDetalleOrden;
import com.Model.DTO.ModeloRecibo;
import com.Service.TicketServices.FormatearReciboService;
import com.Service.TicketServices.TicketOrderService;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;

import util.PrinterServiceHolder;

/**
 * Template Method para tickets.
 * La secuencia de impresión es fija y cada tipo de orden personaliza sus bloques.
 */
public abstract class AbstractTicketTemplateService {

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final FormatearReciboService formatear = new FormatearReciboService();
    private final ModeloRecibo recibo;

    protected AbstractTicketTemplateService(ModeloRecibo recibo) {
        this.recibo = recibo;
    }

    protected ModeloRecibo getRecibo() {
        return recibo;
    }

    public final TicketOrderService.PrintResultEnum printOrderTicket() {
        if (!checkPrinterAvailability()) {
            System.err.println("Error de impresion: Impresora no disponible");
            return TicketOrderService.PrintResultEnum.PRINTER_NOT_AVAILABLE;
        }
        return generateTicket();
    }

    private TicketOrderService.PrintResultEnum generateTicket() {
        EscPos escpos = null;
        PrinterOutputStream printerOutputStream = null;
        try {
            PrintService printService = PrinterServiceHolder.INSTANCE.get();
            if (printService == null) {
                System.err.println("Error de impresion: PrintService es null");
                return TicketOrderService.PrintResultEnum.PRINTER_NOT_AVAILABLE;
            }

            printerOutputStream = new PrinterOutputStream(printService);
            escpos = new EscPos(printerOutputStream);

            Style title = new Style()
                    .setFontSize(Style.FontSize._3, Style.FontSize._3)
                    .setJustification(EscPosConst.Justification.Center);
            Style subtitle = new Style(escpos.getStyle())
                    .setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);
            Style bold = new Style(escpos.getStyle()).setBold(true);

            printHeader(escpos, title, subtitle);
            printTypeInfo(escpos, subtitle);
            escpos.writeLF(Constants.SEPARADOR_TICKET).feed(1);
            printDetails(escpos);
            printFooter(escpos, bold);

            escpos.close();
            System.out.println("Ticket impreso correctamente.");
            return TicketOrderService.PrintResultEnum.SUCCESS;
        } catch (IllegalStateException ex) {
            System.err.println("Error de impresion: Impresora no disponible - " + ex.getMessage());
            PrinterServiceHolder.INSTANCE.invalidate();
            return TicketOrderService.PrintResultEnum.PRINTER_NOT_AVAILABLE;
        } catch (IOException ex) {
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Error desconocido";
            System.err.println("Error de impresion: " + errorMsg);
            if (errorMsg.toLowerCase().contains("paper") || errorMsg.toLowerCase().contains("papel")) {
                return TicketOrderService.PrintResultEnum.NO_PAPER;
            }
            PrinterServiceHolder.INSTANCE.invalidate();
            Logger.getLogger(AbstractTicketTemplateService.class.getName()).log(Level.SEVERE, "Error al generar ticket", ex);
            return TicketOrderService.PrintResultEnum.PRINT_ERROR;
        } finally {
            if (escpos != null) {
                try {
                    escpos.close();
                } catch (IOException e) {
                    Logger.getLogger(AbstractTicketTemplateService.class.getName()).log(Level.SEVERE, "Error al cerrar escpos", e);
                }
            }
            if (printerOutputStream != null) {
                try {
                    printerOutputStream.close();
                } catch (IOException e) {
                    Logger.getLogger(AbstractTicketTemplateService.class.getName()).log(Level.SEVERE, "Error al cerrar printerOutputStream", e);
                }
            }
        }
    }

    protected void printHeader(EscPos escpos, Style title, Style subtitle) throws IOException {
        String fechaFormateada = "";
        LocalDateTime fechaExp = recibo != null ? recibo.getFechaExpedicion() : null;
        if (fechaExp != null) {
            fechaFormateada = fechaExp.format(FORMATO_FECHA);
        }

        String nombreCliente = "";
        if (recibo != null && recibo.getOrdenCompleta() != null && recibo.getOrdenCompleta().getOrden() != null) {
            nombreCliente = recibo.getNombreCliente();
        }
        String tipoPago = recibo.getOrdenCompleta().getOrden().getNombreTipoPago().length() != 0 ?  recibo.getOrdenCompleta().getOrden().getNombreTipoPago() : "";
        System.out.println(recibo.getOrdenCompleta().getOrden().getNombreTipoPago());
        escpos.writeLF(title, "COCINA RUBI")
                .feed(2)
                .write("Pago del cliente: ")
                .writeLF(subtitle,tipoPago)
                .write("Cliente: ")
                .writeLF(subtitle, nombreCliente)
                .write("Fecha: ")
                .writeLF(subtitle, fechaFormateada)
                .feed(1);
    }

    protected abstract void printTypeInfo(EscPos escpos, Style subtitle) throws IOException;

    protected void printDetails(EscPos escpos) throws IOException {
        if (recibo == null || recibo.getOrdenCompleta() == null || recibo.getOrdenCompleta().getDetalles() == null) {
            return;
        }
        List<ModeloDetalleOrden> detalleOrden = recibo.getOrdenCompleta().getDetalles();
        for (ModeloDetalleOrden detalle : detalleOrden) {
            String precio = FORMATO_PRECIO.format(detalle.getPrecioDetalleOrden());
            String especificaciones = detalle.getEspecificacionesDetalleOrden();
            int cantidad = detalle.getCantidad();
            List<String> lineas = formatear.formatearDetalleOrden(especificaciones, cantidad, precio);
            for (String linea : lineas) {
                escpos.writeLF(linea);
            }
        }
    }

    protected void printFooter(EscPos escpos, Style bold) throws IOException {
        double total = calculateTotal();
        String totalFormateado = FORMATO_PRECIO.format(total);
        escpos.writeLF(Constants.SEPARADOR_TICKET)
                .feed(1)
                .writeLF(bold, formatear.formatearLineaTotal("TOTAL", totalFormateado))
                .writeLF(Constants.SEPARADOR_TICKET)
                .feed(5)
                .cut(EscPos.CutMode.FULL);
    }

    protected double calculateTotal() {
        if (recibo == null || recibo.getOrdenCompleta() == null || recibo.getOrdenCompleta().getOrden() == null) {
            return 0.0;
        }
        System.out.println("total del pedido: "+  recibo.getOrdenCompleta().getOrden().getPrecioOrden());
        return recibo.getOrdenCompleta().getOrden().getPrecioOrden();
    }

    private boolean checkPrinterAvailability() {
        try {
            return PrinterServiceHolder.INSTANCE.isPrinterActive();
        } catch (Exception e) {
            System.err.println("Error al verificar disponibilidad de impresora: " + e.getMessage());
            return false;
        }
    }
}

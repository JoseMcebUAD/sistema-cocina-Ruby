package com.Service.TicketServices;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;

import com.Config.Constants;
import com.Model.ModeloDetalleOrden;
import com.Model.DTO.ModeloRecibo;
import util.PrinterServiceHolder;
import javax.print.PrintService;

/**
 * Clase para generar facturas de órdenes en impresora térmica.
 * Ejemplo: https://github.com/anastaciocintra/escpos-coffee-samples/blob/master/usual/textstyle/src/main/java/TextStyleSample.java
 */
public class TicketOrderService {

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private FormatearReciboService formatear = new FormatearReciboService();

    /**
     * Genera e imprime la factura de una orden en impresora térmica.
     * Retorna un estado indicando si la impresión fue exitosa.
     *
     * @param factura Modelo de factura con la información de la orden
     * @return PrintResultEnum indicando el estado de la impresión
     */
    public PrintResultEnum printOrderTicket(ModeloRecibo factura) {
        // Validar que la impresora esté disponible antes de intentar
        if (!checkPrinterAvailability()) {
            System.err.println("Error de impresión: Impresora no disponible");
            return PrintResultEnum.PRINTER_NOT_AVAILABLE;
        }
        
        return generateTicket(factura);
    }
    
    /**
     * Genera e imprime la factura de una orden en impresora térmica.
     *
     * @param factura Modelo de factura con la información de la orden
     * @return PrintResultEnum indicando el estado de la impresión
     */
    private PrintResultEnum generateTicket(ModeloRecibo factura) {
        EscPos escpos = null;
        PrinterOutputStream printerOutputStream = null;
        try {
            // Crear PrinterOutputStream desde el PrintService
            PrintService printService = PrinterServiceHolder.INSTANCE.get();
            if (printService == null) {
                System.err.println("Error de impresión: PrintService es null");
                return PrintResultEnum.PRINTER_NOT_AVAILABLE;
            }
            
            printerOutputStream = new PrinterOutputStream(printService);
            escpos = new EscPos(printerOutputStream);

            List<ModeloDetalleOrden> detalleOrden = factura.getOrden().getDetalles();

            // Estilos
            Style title = new Style()
                    .setFontSize(Style.FontSize._3, Style.FontSize._3)
                    .setJustification(EscPosConst.Justification.Center);

            Style subtitle = new Style(escpos.getStyle())
                    .setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);

            Style bold = new Style(escpos.getStyle())
                    .setBold(true);

            // Convertir fecha a String (proteger nulos)
            String fechaFormateada = "";
            LocalDateTime fechaExp = factura != null ? factura.getFechaExpedicion() : null;
            if (fechaExp != null) {
                fechaFormateada = fechaExp.format(FORMATO_FECHA);
            }

            // Encabezado
            escpos.writeLF(title, "COCINA RUBY")
                    .feed(2)
                    .write("Cliente: ")
                    .writeLF(subtitle, factura.getNombreCliente())
                    .write("Fecha: ")
                    .writeLF(subtitle, fechaFormateada)
                    .feed(2)
                    .writeLF(Constants.SEPARADOR_TICKET)
                    .feed(1);

            // Detalles de la orden
            for (ModeloDetalleOrden detalle : detalleOrden) {
                String precio = FORMATO_PRECIO.format(detalle.getPrecioDetalleOrden());
                String especificaciones = detalle.getEspecificacionesDetalleOrden();

                // Imprimir las líneas del detalle (puede ser múltiples si es muy largo)
                List<String> lineas = formatear.formatearDetalleOrden(especificaciones, precio);
                for (String linea : lineas) {
                    escpos.writeLF(linea);
                }
            }

            // Pie de factura
            double total = factura.getOrden().getOrden().getPrecioOrden();
            String totalFormateado = FORMATO_PRECIO.format(total);

            escpos.writeLF(Constants.SEPARADOR_TICKET)
                    .feed(1)
                    .writeLF(bold, formatear.formatearLineaTotal("TOTAL", totalFormateado))
                    .writeLF(Constants.SEPARADOR_TICKET)
                    .feed(5)
                    .cut(EscPos.CutMode.FULL);

            escpos.close();
            System.out.println("Ticket impreso correctamente.");
            return PrintResultEnum.SUCCESS;

        } catch (IllegalStateException ex) {
            // La impresora no está disponible
            System.err.println("Error de impresión: Impresora no disponible - " + ex.getMessage());
            PrinterServiceHolder.INSTANCE.invalidate();
            return PrintResultEnum.PRINTER_NOT_AVAILABLE;
        } catch (IOException ex) {
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Error desconocido";
            System.err.println("Error de impresión: " + errorMsg);
            
            // Detectar si es por falta de papel
            if (errorMsg.toLowerCase().contains("paper") || errorMsg.toLowerCase().contains("papel")) {
                return PrintResultEnum.NO_PAPER;
            }
            
            // Intentar reinicializar la impresora en caso de otros errores
            PrinterServiceHolder.INSTANCE.invalidate();
            Logger.getLogger(TicketOrderService.class.getName()).log(Level.SEVERE, "Error al generar factura", ex);
            return PrintResultEnum.PRINT_ERROR;
        } finally {
            if (escpos != null) {
                try {
                    escpos.close();
                } catch (IOException e) {
                    Logger.getLogger(TicketOrderService.class.getName()).log(Level.SEVERE, "Error al cerrar escpos", e);
                }
            }
            if (printerOutputStream != null) {
                try {
                    printerOutputStream.close();
                } catch (IOException e) {
                    Logger.getLogger(TicketOrderService.class.getName()).log(Level.SEVERE, "Error al cerrar printerOutputStream", e);
                }
            }
        }
    }
    
    /**
     * Verifica si la impresora está disponible.
     * 
     * @return true si la impresora está activa, false si no
     */
    private boolean checkPrinterAvailability() {
        try {
            return PrinterServiceHolder.INSTANCE.isPrinterActive();
        } catch (Exception e) {
            System.err.println("Error al verificar disponibilidad de impresora: " + e.getMessage());
            return false;
        }
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

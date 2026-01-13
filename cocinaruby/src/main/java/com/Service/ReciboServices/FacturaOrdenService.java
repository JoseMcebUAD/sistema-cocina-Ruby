package com.Service.ReciboServices;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;

import com.Config.Constants;
import com.Model.ModeloDetalleOrden;
import com.Model.DTO.ModeloFactura;
import util.PrinterServiceHolder;

/**
 * Clase para generar facturas de órdenes en impresora térmica.
 * Ejemplo: https://github.com/anastaciocintra/escpos-coffee-samples/blob/master/usual/textstyle/src/main/java/TextStyleSample.java
 */
public class FacturaOrdenService {

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private FormatearFacturaService formatear = new FormatearFacturaService();

    /**
     * Genera e imprime la factura de una orden en impresora térmica.
     *
     * @param factura Modelo de factura con la información de la orden
     */
    public void generarFacturaOrden(ModeloFactura factura) {
        EscPos escpos = null;
        PrinterOutputStream printerOutputStream = null;
        try {
            // Crear PrinterOutputStream desde el PrintService
            printerOutputStream = new PrinterOutputStream(PrinterServiceHolder.INSTANCE.get());
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

            // Convertir fecha a String
            String fechaFormateada = FORMATO_FECHA.format(factura.getFechaExpedicion());

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

        } catch (IOException ex) {
            Logger.getLogger(FacturaOrdenService.class.getName()).log(Level.SEVERE, "Error al generar factura", ex);
        } finally {
            if (escpos != null) {
                try {
                    escpos.close();
                } catch (IOException e) {
                    Logger.getLogger(FacturaOrdenService.class.getName()).log(Level.SEVERE, "Error al cerrar escpos", e);
                }
            }
            if (printerOutputStream != null) {
                try {
                    printerOutputStream.close();
                } catch (IOException e) {
                    Logger.getLogger(FacturaOrdenService.class.getName()).log(Level.SEVERE, "Error al cerrar printerOutputStream", e);
                }
            }
        }
    }
}

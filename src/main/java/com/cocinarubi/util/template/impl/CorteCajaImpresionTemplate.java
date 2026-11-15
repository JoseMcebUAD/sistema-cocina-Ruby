package com.cocinarubi.util.template.impl;

import com.cocinarubi.util.FormatearReciboService;
import com.cocinarubi.util.template.BaseTicketTemplate;
import com.cocinarubi.util.template.data.CorteCajaTicketData;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Nivel 2 — Template abstracto para tickets de corte de caja.
 * Implementa el encabezado (nombre del negocio + tipo de corte) y el cuerpo
 * (fecha, totales por método de pago). Las subclases implementan el footer.
 *
 * <p>No depende de {@code Constants}: el nombre del negocio, el separador y el
 * ancho del ticket se inyectan por constructor desde el service.</p>
 */
public abstract class CorteCajaImpresionTemplate extends BaseTicketTemplate<CorteCajaTicketData> {

    protected static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String[] lineasNombreNegocio;
    protected final String separador;
    protected final int anchoTicket;
    protected final FormatearReciboService formatearReciboService;

    /** Estilos inicializados en renderHeader y disponibles para las subclases. */
    protected Style subtitleStyle;
    protected Style boldStyle;

    /**
     * @param data                 datos calculados del corte
     * @param lineasNombreNegocio  líneas del nombre del negocio (cada una en FontSize._3 centrado)
     * @param separador            string separador (ej. "---...---", 40 chars)
     * @param anchoTicket          ancho útil del ticket en caracteres normales (ej. 35)
     * @param formatearReciboService helper de alineado de texto
     */
    protected CorteCajaImpresionTemplate(CorteCajaTicketData data,
        String[] lineasNombreNegocio,
        String separador,
        int anchoTicket,
        FormatearReciboService formatearReciboService) {
            
        super(data);
        this.lineasNombreNegocio = lineasNombreNegocio;
        this.separador = separador;
        this.anchoTicket = anchoTicket;
        this.formatearReciboService = formatearReciboService;
    }

    /** Nombre del negocio en FontSize._3 centrado + tipo de corte en FontSize._2 bold centrado. */
    @Override
    protected void renderHeader(EscPos escpos) throws IOException {
        Style titleStyle = new Style()
                .setFontSize(Style.FontSize._3, Style.FontSize._3)
                .setJustification(EscPosConst.Justification.Center);
        Style tipoStyle = new Style()
                .setFontSize(Style.FontSize._2, Style.FontSize._2)
                .setBold(true)
                .setJustification(EscPosConst.Justification.Center);

        subtitleStyle = new Style()
                .setFontSize(Style.FontSize._2, Style.FontSize._2)
                .setBold(true);
        boldStyle = new Style().setBold(true);

        for (String linea : lineasNombreNegocio) {
            escpos.writeLF(titleStyle, linea);
        }
        escpos.feed(1)
              .writeLF(tipoStyle, getData().getTituloTipo())
              .writeLF(separador)
              .feed(1);
    }

    /**
     * Fecha del corte y totales desglosados por método de pago.
     * Usa labels en FontSize._2 (doble ancho) + valores en tamaño normal.
     */
    @Override
    protected void renderBody(EscPos escpos) throws IOException {
        CorteCajaTicketData d = getData();
        printLinea(escpos, "Fecha:",    d.getFecha().format(FORMATO_FECHA));
        escpos.feed(1);
        printLinea(escpos, "Total:",    FORMATO_PRECIO.format(d.getTotal()));
        printLinea(escpos, "Transf.:",  FORMATO_PRECIO.format(d.getTotalTransferencia()));
        printLinea(escpos, "Efectivo:", FORMATO_PRECIO.format(d.getTotalEfectivo()));
        printLinea(escpos, "Tarjeta:",  FORMATO_PRECIO.format(d.getTotalTarjeta()));
    }

    /**
     * Imprime una línea con el label en FontSize._2 (doble ancho) y el valor en tamaño normal,
     * alineados al ancho del ticket.
     *
     * El label ocupa el doble de caracteres normales, por lo que los espacios se calculan como:
     * espacios = separador.length() - (label.length() * 2) - valor.length()
     */
    private void printLinea(EscPos escpos, String label, String valor) throws IOException {
        int labelWidthEnNormal = label.length() * 2;
        int espacios = separador.length() - labelWidthEnNormal - valor.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.max(1, espacios); i++) sb.append(' ');
        sb.append(valor);
        escpos.write(subtitleStyle, label).writeLF(new Style(), sb.toString());
    }
}

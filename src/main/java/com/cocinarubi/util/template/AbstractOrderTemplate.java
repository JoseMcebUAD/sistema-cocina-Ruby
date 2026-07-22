package com.cocinarubi.util.template;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import com.cocinarubi.Constants;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;

/**
 * Nivel 2 — Base para tickets de órdenes (pedidos, facturas, etc.).
 *
 * <p>Renderiza un encabezado común (nombre del negocio, tagline, número de cocina)
 * y estructura el cuerpo entre dos separadores. Las subclases solo implementan
 * {@link #renderSpecificDetails(EscPos)} y {@link #renderFooter(EscPos)}.</p>
 *
 * @param <T> tipo del modelo de datos del ticket
 */
public abstract class AbstractOrderTemplate<T> extends BaseTicketTemplate<T> {

    protected static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");
    protected static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Inicializado en renderHeader, disponible para subclases en renderBody
    protected Style subtitleStyle;

    protected AbstractOrderTemplate(T data) {
        super(data);
    }

    /**
     * Encabezado común: nombre del negocio, tagline y número de cocina.
     * También inicializa {@link #subtitleStyle} para uso en cuerpo y subclases.
     */
    @Override
    protected void renderHeader(EscPos escpos) throws IOException {
        subtitleStyle = new Style(escpos.getStyle())
                .setFontSize(Style.FontSize._2, Style.FontSize._2)
                .setBold(true)
                .setUnderline(Style.Underline.OneDotThick);

        Style title = new Style()
                .setFontSize(Style.FontSize._3, Style.FontSize._3)
                .setJustification(EscPosConst.Justification.Center);
        Style centered = new Style(escpos.getStyle())
                .setJustification(EscPosConst.Justification.Center);

        escpos.writeLF(title, "COCINA RUBI")
                .feed(1)
                .writeLF(centered, "Envíos")
                .writeLF(centered, "Numero de cocina: " + Constants.NUMERO_COCINA)
                .feed(1);
    }

    /**
     * Cuerpo del ticket: envuelve los detalles específicos entre dos separadores.
     * No sobreescribir — cada subclase debe implementar {@link #renderSpecificDetails(EscPos)}.
     */
    @Override
    protected final void renderBody(EscPos escpos) throws IOException {
        escpos.writeLF(Constants.SEPARADOR_TICKET).feed(1);
        renderSpecificDetails(escpos);
        escpos.writeLF(Constants.SEPARADOR_TICKET).feed(1);
    }

    /** Contenido específico de cada ticket, renderizado entre los dos separadores. */
    protected abstract void renderSpecificDetails(EscPos escpos) throws IOException;
}

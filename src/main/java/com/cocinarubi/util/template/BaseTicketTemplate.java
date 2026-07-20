package com.cocinarubi.util.template;

import java.io.IOException;

import com.github.anastaciocintra.escpos.EscPos;

/**
 * Nivel 1 — Base genérica para cualquier tipo de ticket.
 *
 * Define el flujo fijo de impresión (Template Method) sin asumir nada sobre
 * el modelo de datos. El programador tiene acceso directo al objeto EscPos
 * en cada hook para total libertad de formato.
 *
 * Uso:
 *   class MiTicket extends BaseTicketTemplate<MiDTO> { ... }
 *
 * @param <T> tipo del modelo de datos que usa la plantilla
 */
public abstract class BaseTicketTemplate<T> {

    private final T data;

    protected BaseTicketTemplate(T data) {
        this.data = data;
    }

    protected T getData() {
        return data;
    }

    /**
     * Template Method — flujo fijo. No sobreescribir.
     * Llama en orden: renderHeader → renderBody → renderFooter.
     */
    public final void render(EscPos escpos) throws IOException {
        renderHeader(escpos);
        renderBody(escpos);
        renderFooter(escpos);
    }

    protected abstract void renderHeader(EscPos escpos) throws IOException;

    protected abstract void renderBody(EscPos escpos) throws IOException;

    protected abstract void renderFooter(EscPos escpos) throws IOException;
}

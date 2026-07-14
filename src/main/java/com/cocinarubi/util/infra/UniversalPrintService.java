package com.cocinarubi.util.infra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.cocinarubi.util.template.BaseTicketTemplate;
import com.github.anastaciocintra.escpos.EscPos;

/**
 * Genera bytes ESC/POS en memoria para que el frontend los envíe
 * directamente al socket TCP:9100 de la impresora.
 */
@Component
public class UniversalPrintService {

    private static final Logger LOGGER = Logger.getLogger(UniversalPrintService.class.getName());

    /**
     * Renderiza la plantilla y retorna los bytes ESC/POS listos para enviar a la impresora.
     *
     * @param template cualquier implementación de BaseTicketTemplate
     * @return bytes ESC/POS, o arreglo vacío si hubo error de renderizado
     */
    public byte[] generateEscPosBytes(BaseTicketTemplate<?> template) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (EscPos escpos = new EscPos(baos)) {
            template.render(escpos);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al generar bytes ESC/POS", ex);
            return new byte[0];
        }
        return baos.toByteArray();
    }
}


package com.cocinarubi.util.infra;

import java.io.IOException;

import javax.print.PrintService;

import com.github.anastaciocintra.output.PrinterOutputStream;

/**
 * Extiende {@link PrinterOutputStream} para hacer el {@code close()} sincrónico.
 *
 * <p>El problema original: {@link PrinterOutputStream} hereda {@code PipedOutputStream.close()},
 * que solo cierra el pipe (señal EOF) sin esperar a que {@code threadPrint} termine
 * su llamada a {@code DocPrintJob.print()}. Esto deja el trabajo de impresión
 * corriendo en segundo plano cuando el siguiente trabajo ya ha empezado,
 * y el driver de la impresora EPSON ignora el segundo trabajo.
 *
 * <p>Esta clase anula {@code close()} para unirse al {@code threadPrint}
 * tras cerrar el pipe, garantizando que el trabajo anterior esté completamente
 * sometido al spooler antes de que el siguiente inicie.
 */
public class SynchronousPrinterOutputStream extends PrinterOutputStream {

    private static final long TIMEOUT_JOIN_MS = 10_000;

    public SynchronousPrinterOutputStream(PrintService printService) throws IOException {
        super(printService);
    }

    /**
     * Cierra el pipe (EOF → threadPrint termina job.print()) y espera
     * a que el thread de impresión finalice antes de retornar.
     */
    @Override
    public void close() throws IOException {
        super.close(); // Cierra PipedOutputStream → EOF llega al DocPrintJob
        try {
            threadPrint.join(TIMEOUT_JOIN_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


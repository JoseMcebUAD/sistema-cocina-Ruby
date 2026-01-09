package util;
import javax.print.PrintService;

import com.github.anastaciocintra.output.PrinterOutputStream;

/**
 * Enum para reutilizar la llamda al servicio de la fokin printer
 * Como es muy lenta, hacemos un tipo singleton para llamarla multiples veces sin instanciarlo
 * 
 * como se llama
 * -- PrinterServiceHoler.INSTANCE.init("printerName")
 * 
 * Se utiliza una sincronized si en algun momento de llaman al mismo tiempo las funciones
 * y funcione como debe
 */
public enum PrinterServiceHolder {

    INSTANCE;

    private PrintService printService;
    private String printerName;
    
    public synchronized void init(String printerName) {
        this.printerName = printerName;
        reconnect();
    }
    
    public synchronized PrintService get() {
        if (printService == null) {
            reconnect();
        }
        return printService;
    }
    /**
     * verifica si la impresora esta en linea
     * @return
     */
    public synchronized boolean isPrinterActive() {
        return printService != null;
    }
    
    
    private synchronized void reconnect() {
        printService = PrinterOutputStream.getPrintServiceByName(printerName);
        if (printService == null) {
            throw new IllegalStateException("Impresora no disponible");
        }
    }
    
    /**
     * lo resetea cuando hay un error, ya que alch no encontré una manera que cuando se vaya la luz o haya un error
     * no pete cuando intentes llamarlo otra vez
     */
    public synchronized void invalidate() {
        printService = null;
    }

    // Getters

    public synchronized String getPrinterName() {
        return printerName;
    }

    public synchronized PrintService getPrintService() {
        return printService;
    }
}

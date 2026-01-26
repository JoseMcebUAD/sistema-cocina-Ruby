package util;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.Config.Constants;


/**
 * Esta clase funciona para monitorear si la fokin impresora sirve o no, y para restaurar la conexion en dado caso que 
 * se vaya la luz, para no reiniciar el sistema
 */
public final class PrinterMonitor {

    //hacemos un timeout
    private static final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    //funcionalidad para observar a la impresora
    public static final void start(){
        scheduler.scheduleAtFixedRate(() -> {
            try{
                boolean isPrinterOnline = PrinterServiceHolder.INSTANCE.isPrinterActive();

                if(!isPrinterOnline){
                    PrinterServiceHolder.INSTANCE.init(Constants.NOMBRE_IMPRESORA);
                    System.out.println("Impresora reconectada");
                }


            }catch(Exception e){
                System.err.println("Error en PrinterMonitor: " + e.getMessage());
            }
        },5,6,TimeUnit.SECONDS);
    }

    /**
     * detiene el monitoreo de la impresora terfimca
     */
    public static void stop(){
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * verifica si el monitor esta conectao
     * @return true activo, false  detenido
     */
    public static boolean isRunning(){
        return !scheduler.isShutdown();
    }
}

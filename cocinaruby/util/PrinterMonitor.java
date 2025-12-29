import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.Constants;

/**
 * Esta clase funciona para monitorear si la fokin impresora sirve o no, y para restaurar la conexion en dado caso que 
 * se vaya la luz, para no reiniciar el sistema
 */
public class PrinterMonitor {

    //hacemos un timeout
    private static final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();
    //funcionalidad para observar a la impresora
    public static void start(){
        scheduler.scheduleAtFixedRate(() -> {
            try{
                boolean isPrinterOnline = PrinterServiceHolder.INSTANCE.isPrinterActive();

                if(!isPrinterOnline){
                    PrinterServiceHolder.INSTANCE.init(Constants.nombreImpresora);
                    System.out.println("Impresora reconectada");
                }
                

            }catch(Exception e){}
        },5,6,TimeUnit.SECONDS);
    }
}

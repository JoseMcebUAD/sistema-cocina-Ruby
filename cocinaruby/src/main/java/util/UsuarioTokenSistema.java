package util;
/**
 * Esta clase se usa para verificar el tiempo activo del usuario, una vez cierra el sistema , se le da un tiempo
 * de espera hasta poder hacer un 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

public class UsuarioTokenSistema {

    private  String rutaArchivo;
    //tiempo de vida del token del usuario para no pedirle inicio de sesión una vez más 
    private final int tiempoDeVidaUsuario = 1000*60*60*3;// ms,s,m,h
    //fecha y hora de la cual el usuario cierra el sistema
    private final Properties usuarioTokenProperties;
    public UsuarioTokenSistema(){
        //deberia regresar C:://.../cocina-ruby
        String direccionActual = System.getProperty("user.dir");
        String direccionArchivoToken = direccionActual +"/src/main/java/resources/com/UserSession/tokenUsuario.txt";
        this.rutaArchivo = direccionArchivoToken;
        this.usuarioTokenProperties = this.cargarConfiguracion("config.tokenUsuario");
        
    }
    /*
    *  Verifica si el usuario necesita iniciar sesión si ya ha cerrado el sistema
    */
    public boolean obtenerTokenUsuario() throws IOException{
        String timestamp = this.usuarioTokenProperties.getProperty("cerrarsessiontimestamp");

        if (timestamp == null || timestamp.isEmpty()) {
            return false; // No hay cierre guardado
        }

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime inicio = LocalDateTime.parse(timestamp, f);
        LocalDateTime fin = LocalDateTime.now();

        long minutos = Duration.between(inicio, fin).toMillis();

        return minutos >= this.tiempoDeVidaUsuario; 
    }
    
    /*
    *Escribe el cierre de sesión en el archivo
    */
   public void setTokenUsuario() throws IOException{
        LocalDateTime fin = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = fin.format(f);

        this.usuarioTokenProperties.setProperty("cerrarsessiontimestamp", timestamp);

        // Guardar al archivo config.tokenUsuario
        try (FileOutputStream output = new FileOutputStream("config.tokenUsuario")) {
            this.usuarioTokenProperties.store(output, "Token de sesión del usuario");
            System.out.println("✓ Token de sesión guardado: " + timestamp);
        }
    } 

    public String obtenerFechaYHoraActual(){
        String fechaHora;
        return  fechaHora = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Carga la configuración desde un archivo .tokenUsuario
     *
     * @param rutaArchivoConfig Ruta al archivo
     * @return Properties con la configuración (vacío si hay error)
     */
    private Properties cargarConfiguracion(String rutaArchivoConfig){
        Properties prop = new Properties();

        try(InputStream input = new FileInputStream(rutaArchivoConfig)){
            prop.load(input);
            System.out.println("✓ Configuración de token cargada desde: " + rutaArchivoConfig);
        }catch(IOException exception){
            System.err.println("⚠ No se pudo cargar el archivo de configuración: " + exception.getMessage());
            System.err.println("⚠ Se usará configuración por defecto (requiere autenticación)");
            // Retorna Properties vacío, lo que causará que obtenerTokenUsuario() retorne false
        }

        return prop;
    }

    // Getters y Setters

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public int getTiempoDeVidaUsuario() {
        return tiempoDeVidaUsuario;
    }

    public Properties getUsuarioTokenProperties() {
        return usuarioTokenProperties;
    }

}

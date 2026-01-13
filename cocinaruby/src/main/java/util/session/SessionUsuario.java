package util.session;
/**
 * Esta clase se usa para verificar el tiempo activo del usuario, una vez cierra el sistema , se le da un tiempo
 * de espera hasta poder hacer un 
 */

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public final class SessionUsuario {

    private static SessionUsuario instance;

    private SessionRepository sessionRepository;
    private SessionExpiration sessionExpiration;
    private final Properties properties; 
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SessionUsuario(){
        //deberia regresar C:://.../cocina-ruby
        String direccionActual = System.getProperty("user.dir");
        Path ruta = Path.of(direccionActual, "config.tokenUsuario");
        this.sessionExpiration = new SessionExpiration();
        this.sessionRepository = new FileSessionRepository(ruta);
        this.properties = this.sessionRepository.load();
        
    }
    //creamos el singleton 
    public static synchronized SessionUsuario getInstance() {
        if (instance == null) {
            instance = new SessionUsuario();
        }
       return instance;
    }

    public void guardarNombreUsuario(String nombreUsuario){
        this.properties.setProperty("nombreUsuario", nombreUsuario);
        this.sessionRepository.save(properties);
    }

    public void guardarInicioSesion(){
        this.properties.setProperty("cerrarsessiontimestamp",this.getFechaYhoraActual() );
        this.sessionRepository.save(properties);
    }
    
    public void borrarNombreUsuario(){
        this.properties.setProperty("nombreUsuario","" );
        this.sessionRepository.save(properties);
        
    }
    
    public void borrarInicioSesion(){
        this.properties.setProperty("cerrarsessiontimestamp","" );
        this.sessionRepository.save(properties);
    }
    /**
     * verifica la sesion activa del usuario
     * true si ya expiró
     * false si no ha expirado
     * @return
     */
    public boolean hasSessionExpired(){
        String timestamp = this.properties.getProperty("cerrarsessiontimestamp");

        LocalDateTime inicio = LocalDateTime.parse(timestamp,this.formatter);
        //verificamos si ya expiró la sesión del usuario
        boolean expired = this.sessionExpiration.isExpired(inicio);
        System.out.println(expired);

        if (expired) {
            this.borrarNombreUsuario();
        }

        return expired;
        
    }

    //verificar que el usuario está loggeado
    
    public boolean isUserLogged() {
        String nombre = this.properties.getProperty("nombreUsuario");
        return nombre != null && !nombre.isEmpty();
    }

    public boolean isUsuarioAllowedInMenu(){
        return !hasSessionExpired() && isUserLogged();
    }

    //obtiene la fecha de hoy()
    private String getFechaYhoraActual(){
        return LocalDateTime.now()
            .format(this.formatter);
    }
   
}

package util.session;

import java.util.Properties;

/**
 * Interfaz para almacenar todo lo relacionado con la sesion del usuario
 */
public interface SessionRepository {
    public Properties load();
    public void save(Properties userProperties);
}

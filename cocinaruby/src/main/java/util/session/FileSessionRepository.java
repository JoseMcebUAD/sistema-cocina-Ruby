package util.session;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

public class FileSessionRepository implements SessionRepository {

    private Path ruta;

    public FileSessionRepository(Path ruta){
        this.ruta = ruta;
    }
    @Override
    public Properties load() {

       Properties props = new Properties();

       try(InputStream in = new FileInputStream(this.ruta.toString())){
            props.load(in);
       }catch(IOException e){
        System.err.println("no se ha podido acceder a las properties");
       }

       return props;
    }

    @Override
    public void save(Properties userProperties) {
        
       try(OutputStream ou = new FileOutputStream(this.ruta.toString())){
            userProperties.store(ou, "sesion guardada");
       }catch(IOException e){
        System.err.println("no se ha podido acceder a las properties");
       }
    }

}

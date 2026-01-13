package util.session;

import java.time.Duration;
import java.time.LocalDateTime;

public class SessionExpiration {

    public final int MAX_LIM = 1000*60*60*3;// ms,s,m,h

    public boolean isExpired(LocalDateTime inicio){
        System.out.println("duracion:" +(Duration.between(inicio, LocalDateTime.now()).toMillis() >= MAX_LIM));
        return Duration.between(inicio, LocalDateTime.now()).toMillis() >= MAX_LIM;
    }

}

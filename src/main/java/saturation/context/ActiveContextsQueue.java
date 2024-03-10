package saturation.context;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

@ThreadSafe
public interface ActiveContextsQueue<T extends Context> {

    Optional<T> poll();

    boolean activateContext(T context);

    boolean deactivateContext(T context);

    int size();

}

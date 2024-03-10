package saturation.collections;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

@ThreadSafe
public interface ConcurrentStack<E> {

    boolean push(E element);

    Optional<E> pop();

    Optional<E> peek();

}
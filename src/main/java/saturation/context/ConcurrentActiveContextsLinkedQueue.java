package saturation.context;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentActiveContextsLinkedQueue implements ActiveContextsQueue<Context> {

    private final ConcurrentLinkedQueue<Context> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    @Override
    public Optional<Context> poll() {
        Context poll = concurrentLinkedQueue.poll();
        return Optional.ofNullable(poll);
    }

    @Override
    public boolean activateContext(Context context) {
        AtomicBoolean isActive = context.getIsActive();
        if (isActive.compareAndSet(false, true)) {
            return concurrentLinkedQueue.add(context);
        }
        return false;
    }

    @Override
    public boolean deactivateContext(Context context) {
        AtomicBoolean isActive = context.getIsActive();
        isActive.set(false);
        if (!context.isQueueTodoAxiomsEmpty()) {
            activateContext(context);
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return concurrentLinkedQueue.size();
    }
}

package saturation.collections;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLinkedStack<E> implements ConcurrentStack<E> {

    private final AtomicReference<Node<E>> top = new AtomicReference<>();

    /**
     * a special dummy node used to mark the end of the stack after it has been
     * activated
     */
    private static final Node<?> dummyNode = new Node<>(null);

    @Override
    public boolean push(E element) {
        Objects.requireNonNull(element);

        Node<E> newHead = new Node<>(element);
        Node<E> oldHead;

        for (;;) {
            oldHead = top.get();

            if (oldHead == null) {
                newHead.next = (Node<E>) dummyNode;
            } else {
                newHead.next = oldHead;
            }

            if (top.compareAndSet(oldHead, newHead)) {
                return oldHead == null;
            }
        }
    }

    @Override
    public Optional<E> peek() {
        Node<E> head = top.get();

        if (head == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(head.item);
    }

    @Override
    public Optional<E> pop() {
        for (;;) {
            Node<E> oldHead = top.get();
            Node<E> newHead;

            if (oldHead == null) {
                return Optional.empty();
            }

            newHead = oldHead.next;

            if (top.compareAndSet(oldHead, newHead)) {
                return Optional.ofNullable(oldHead.item);
            }
        }
    }

    private static class Node<T> {
        public final T item;
        public Node<T> next;
        public Node(T item) {
            this.item = item;
        }
    }

}
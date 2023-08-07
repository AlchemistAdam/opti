package dk.martinu.opti.img.spi.png;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

public class IdatQueue implements Iterable<byte[]> {

    protected Node root = null;
    protected Node last = null;
    protected boolean closed = false;

    public void add(byte[] idat) {
        Objects.requireNonNull(idat, "idat is null");
        if (closed)
            throw new IllegalStateException("queue is closed");
        Node node = new Node(idat);
        if (root == null) {
            root = last = node;
        }
        else {
            last = last.next = node;
        }
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new IdatIterator();
    }

    @Override
    public void forEach(Consumer<? super byte[]> action) {
        Objects.requireNonNull(action, "action is null");
        Node node = root;
        while (root != null) {
            action.accept(node.idat);
            node = node.next;
        }
    }

    @Override
    public Spliterator<byte[]> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), ORDERED | NONNULL);
    }

    public static class Node {

        public final byte[] idat;
        protected Node next;

        public Node(byte[] idat) {
            this.idat = idat;
        }
    }

    public class IdatIterator implements Iterator<byte[]> {

        protected Node node = root;

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public byte[] next() {
            if (hasNext()) {
                byte[] idat = node.idat;
                node = node.next;
                return idat;
            }
            else {
                throw new NoSuchElementException();
            }
        }
    }
}

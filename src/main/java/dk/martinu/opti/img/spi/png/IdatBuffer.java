package dk.martinu.opti.img.spi.png;

import java.util.Objects;

public class IdatBuffer {

    protected Node root = null;
    protected int length = 0;

    public void add(byte[] idat) {
        Objects.requireNonNull(idat, "idat is null");
        root = new Node(idat, root);
        length += idat.length;
    }

    public int length() {
        return length;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public byte[] getData() {
        byte[] data = new byte[length];
        int offset = 0;
        Node node = root;
        while (node != null) {
            System.arraycopy(node.idat, 0, data, offset, node.idat.length);
            offset += node.idat.length;
            node = node.next;
        }
        return data;
    }

    public static class Node {

        public final byte[] idat;
        protected final Node next;

        public Node(byte[] idat, Node next) {
            this.idat = idat;
            this.next = next;
        }
    }
}

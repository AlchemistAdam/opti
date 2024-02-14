package dk.martinu.opti.img.png;

import java.util.Objects;

final class ByteArrayBuffer {

    private Node root = null;
    private Node cursor = null;
    private int length = 0;

    public void add(byte[] array) {
        Objects.requireNonNull(array, "array is null");
        add(array, array.length);
    }

    public void add(byte[] array, int length) {
        Objects.requireNonNull(array, "array is null");
        if (length > array.length) {
            throw new IllegalArgumentException("length is greater than array length");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length is negative");
        }
        if (root == null) {
            root = cursor = new Node(array, length);
        }
        else {
            cursor.next = cursor = new Node(array, length);
        }
        this.length += length;
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
            System.arraycopy(node.array, 0, data, offset, node.length);
            offset += node.length;
            node = node.next;
        }
        return data;
    }

    public static class Node {
        public final byte[] array;
        public final int length;
        protected Node next;

        public Node(byte[] array, int length) {
            this.array = array;
            this.length = length;
        }
    }
}

package dk.martinu.opti;

import java.util.Arrays;
import java.util.Objects;

public final class ByteView {

    private final byte[] bytes;

    public ByteView(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes array is null");
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public int length() {
        return bytes.length;
    }

    public byte get(int index) {
        return bytes[index];
    }
}

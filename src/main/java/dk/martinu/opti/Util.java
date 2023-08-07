package dk.martinu.opti;

public class Util {

    public static int getInt(byte[] bytes) {
        return getInt(bytes, 0);
    }

    public static int getInt(byte[] bytes, int offset) {
        return bytes[offset] << 24 | bytes[1 + offset] << 16 | bytes[2 + offset] << 8 | bytes[3 + offset];
    }
}

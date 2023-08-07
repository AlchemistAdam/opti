package dk.martinu.opti.img.spi.png;

public enum ColorType {

    GREYSCALE((byte) 0),
    TRUECOLOR((byte) 2),
    INDEXED((byte) 3),
    GREYSCALE_ALPHA((byte) 4),
    TRUECOLOR_ALPHA((byte) 6);

    public final byte value;

    ColorType(byte value) {
        this.value = value;
    }

    public static ColorType get(byte value) {
        for (ColorType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("invalid color type {" + value + "}");
    }
}

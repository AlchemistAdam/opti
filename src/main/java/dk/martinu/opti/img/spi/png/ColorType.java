package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

public enum ColorType {

    GREYSCALE((byte) 0),
    TRUECOLOR((byte) 2),
    INDEXED((byte) 3),
    GREYSCALE_ALPHA((byte) 4),
    TRUECOLOR_ALPHA((byte) 6);

    public static ColorType get(byte value) throws ImageDataException {
        for (ColorType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new ImageDataException("invalid color type {%d}", value);
    }

    public final byte value;

    ColorType(byte value) {
        this.value = value;
    }

}

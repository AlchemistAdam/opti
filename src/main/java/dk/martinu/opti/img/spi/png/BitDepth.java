package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

public enum BitDepth {

    b1(1),
    b2(2),
    b4(4),
    b8(8),
    b16(16);

    public static BitDepth get(int value) throws ImageDataException {
        return switch (value) {
            case 16 -> b16;
            case 8 -> b8;
            case 4 -> b4;
            case 2 -> b2;
            case 1 -> b1;
            default -> throw new ImageDataException("invalid bit depth value {%d}", value);
        };
    }

    public final int value;

    BitDepth(int value) {
        this.value = value;
    }
}

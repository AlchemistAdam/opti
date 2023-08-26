package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

public enum FilterType {

    NONE,
    SUB,
    UP,
    AVERAGE,
    PAETH;

    public static FilterType get(int value) throws ImageDataException {
        return switch (value) {
            case 0 -> NONE;
            case 1 -> SUB;
            case 2 -> UP;
            case 3 -> AVERAGE;
            case 4 -> PAETH;
            default -> throw new ImageDataException("invalid filter type {%d}", value);
        };
    }
}

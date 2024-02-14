package dk.martinu.opti.img.png;

import dk.martinu.opti.img.spi.ImageDataException;

// TODO move constants into PngInfo class if not used elsewhere
public interface ColorType {

    int GRAYSCALE = 0;
    int TRUECOLOR = 2;
    int INDEXED = 3;
    int GRAYSCALE_ALPHA = 4;
    int TRUECOLOR_ALPHA = 6;

    static ColorType get(byte value) throws ImageDataException {
        int i = value & 0xFF;
        return switch (value) {
            case GRAYSCALE -> new Grayscale();
            case TRUECOLOR -> new Truecolor();
            case INDEXED -> new Indexed();
            case GRAYSCALE_ALPHA -> new GrayscaleAlpha();
            case TRUECOLOR_ALPHA -> new TruecolorAlpha();
            default ->
                    throw new ImageDataException("invalid color type value {%d}", value);
        };
    }

    PixelSetter getPixelSetter(int bitDepth, ReducedImage image, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException;

    // https://www.w3.org/TR/png/#4Concepts.PNGImage
    int getComponentCount();

    String getName();

    int getValue();

    default boolean usesAlpha() {
        return (getValue() & 4) != 0;
    }

    default boolean usesPalette() {
        return (getValue() & 1) != 0;
    }

    default boolean usesTruecolor() {
        return (getValue() & 2) != 0;
    }

    /**
     * DOC validateBitDepth
     * https://www.w3.org/TR/png/#table111
     *
     * @param bitDepth
     * @throws ImageDataException
     */
    default void validateBitDepth(int bitDepth) throws ImageDataException { }
}

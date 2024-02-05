package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

import java.util.function.IntUnaryOperator;

// DOC InterlaceMethod
interface InterlaceMethod {

    byte[] getPngSamples(int width, int height, int bitDepth, ColorType colorType, FilterMethod filterMethod,
            byte[] filterData, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException;

    /**
     * Retrieves a sequence of reduced images from the specified samples array
     * of an interlaced image.
     *
     * @param width        the interlaced image width
     * @param height       the interlaced image height
     * @param filterMethod the filter method that was used to transform the
     *                     samples
     * @param filterData   the interlaced image samples
     * @param bytesFunc    a function that returns the number of bytes in a
     *                     scanline of an image given the image width
     * @return an array of reduced images
     * @throws ImageDataException if an error occurred
     */
//    ReducedImage[] getReducedImages(int width, int height, FilterMethod filterMethod, byte[] filterData,
//            IntUnaryOperator bytesFunc) throws ImageDataException;
}

package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

import java.util.function.IntUnaryOperator;

final class NullMethod implements InterlaceMethod {

    static final NullMethod INSTANCE = new NullMethod();

    private NullMethod() { }

    @Override
    public byte[] getPngSamples(int width, int height, int bitDepth, ColorType colorType, FilterMethod filterMethod,
            byte[] filterData, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException {
        // single reduced image containing the samples
        ReducedImage img = new ReducedImage(width, height, filterMethod.revert(
                filterData, height, (int) Math.ceil(width * colorType.getComponentCount() * bitDepth / 8.0)));
        // pixel setter for reduced image samples
        PixelSetter setter = colorType.getPixelSetter(bitDepth, img, palette, transparency, background);

        int components = colorType.usesAlpha() ? colorType.getComponentCount() - 1 : colorType.getComponentCount();
        // destination array for PNG pixel samples
        byte[] dest = new byte[width * height * components];
        // index in dest for next pixel sample
        int index = 0;
        // iterate each scanline
        for (int y = 0; y < height; y++) {
            // TODO this can be optimized to a single method call.
            //  Implement new method in PixelSetter that sets all pixels in the
            //  entire scanline (can also be used in pass 7 by Adam7)
            // set pixel samples in dest from reduced image
            for (int x = 0; x < width; x++) {
                setter.setNext(dest, index);
                index += components;
            }
        }
        return dest;
    }
}

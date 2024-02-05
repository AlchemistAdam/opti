package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

import java.util.function.IntUnaryOperator;

final class Adam7 implements InterlaceMethod {

    static final Adam7 INSTANCE = new Adam7();

    private Adam7() { }

    @Override
    public byte[] getPngSamples(int width, int height, int bitDepth, ColorType colorType, FilterMethod filterMethod,
            byte[] filterData, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException {
        // reduced images containing the samples
        ReducedImage[] images = getReducedImages(width, height, bitDepth, colorType, filterMethod, filterData);
        // pixel setters for reduced image samples
        PixelSetter[] setters = new PixelSetter[images.length];
        for (int i = 0; i < images.length; i++) {
            if (images[i].samples().length > 0) {
                setters[i] = colorType.getPixelSetter(bitDepth, images[i], palette, transparency, background);
            }
            else {
                setters[i] = null;
            }
        }

        // TODO for every 2nd scanline, the index can only be 6
        // table of the reduced image/pixel setter index for a given pixel
        int[] table = {
                0, 5, 3, 5, 1, 5, 3, 5,
                6, 6, 6, 6, 6, 6, 6, 6,
                4, 5, 4, 5, 4, 5, 4, 5,
                6, 6, 6, 6, 6, 6, 6, 6,
                2, 5, 3, 5, 2, 5, 3, 5,
                6, 6, 6, 6, 6, 6, 6, 6,
                4, 5, 4, 5, 4, 5, 4, 5,
                6, 6, 6, 6, 6, 6, 6, 6};

        int components = colorType.usesAlpha() ? colorType.getComponentCount() - 1 : colorType.getComponentCount();
        // destination array for PNG pixel samples
        byte[] dest = new byte[width * height * components];
        // index in dest for next pixel sample
        int index = 0;
        // iterate each scanline
        for (int y = 0; y < height; y++) {
            // set pixel samples in dest from reduced image
            for (int x = 0; x < width; x++) {
                int tIndex = table[(x % 8) + (y % 8 * 8)];
                PixelSetter setter = setters[tIndex];
                if (setter != null) {
                    setter.setNext(dest, index);
                    index += components;
                }
            }
        }
        return dest;
    }

    private ReducedImage[] getReducedImages(int width, int height, int bitDepth, ColorType colorType,
            FilterMethod filterMethod, byte[] filterData) throws ImageDataException {

        // https://www.w3.org/TR/png/#8Interlace
        /* 
        The pass in which each pixel is transmitted (numbered from 1 to 7) is 
        defined by replicating the following 8-by-8 pattern over the entire
        image:
        1 6 4 6 2 6 4 6
        7 7 7 7 7 7 7 7
        5 6 5 6 5 6 5 6
        7 7 7 7 7 7 7 7
        3 6 4 6 3 6 4 6
        7 7 7 7 7 7 7 7
        5 6 5 6 5 6 5 6
        7 7 7 7 7 7 7 7
        */

        // lambda expression that returns the number of bytes in a scanline of 
        // an image given the image width
        IntUnaryOperator bytes = (w) -> (int) Math.ceil(w * colorType.getComponentCount() * bitDepth / 8.0);
        // return value
        ReducedImage[] images = new ReducedImage[7];
        // offset into filterData
        int offset = 0;

        // TODO recheck dimensions of Image are correct
        // pass 1
        {
            int w = (int) Math.ceil(width / 8.0d);
            int lines = (int) Math.ceil(height / 8.0d);
            int nBytes = bytes.applyAsInt(w);
            images[0] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            offset += lines * (nBytes + 1);
        }

        // pass 2
        if (width > 4) {
            int w = (int) Math.ceil((width - 4) / 8.0d);
            int lines = (int) Math.ceil(height / 8.0d);
            int nBytes = bytes.applyAsInt(w);
            images[1] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            offset += lines * (nBytes + 1);
        }
        else {
            images[1] = null;
        }

        // pass 3
        if (height > 4) {
            int w = (int) Math.ceil(width / 4.0d);
            int lines = (int) Math.ceil((height - 4) / 8.0d);
            int nBytes = bytes.applyAsInt(w);
            images[2] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            offset += lines * (nBytes + 1);
        }
        else {
            images[2] = null;
        }

        // pass 4
        if (width > 2) {
            int w = (int) Math.ceil((width - 2) / 4.0d);
            int lines = (int) Math.ceil(height / 4.0d);
            int nBytes = bytes.applyAsInt(w);
            images[3] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            offset += lines * (nBytes + 1);
        }
        else {
            images[3] = null;
        }

        // pass 5
        if (height > 2) {
            int w = (int) Math.ceil(width / 2.0d);
            int lines = (int) Math.ceil((height - 2) / 4.0d);
            int nBytes = bytes.applyAsInt(w);
            images[4] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            offset += lines * (nBytes + 1);
        }
        else {
            images[4] = null;
        }

        // pass 6
        if (width > 1) {
            int w = (int) Math.ceil((width - 1) / 2.0d);
            int lines = (int) Math.ceil(height / 2.0d);
            int nBytes = bytes.applyAsInt(w);
            images[5] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            offset += lines * (nBytes + 1);
        }
        else {
            images[5] = null;
        }

        // pass 7
        if (height > 1) {
            int w = width;
            int lines = (int) Math.ceil((height - 1) / 2.0d);
            int nBytes = bytes.applyAsInt(w);
            images[6] = new ReducedImage(w, lines, filterMethod.revert(filterData, offset, lines, nBytes));
            // offset += lines * (nBytes + 1);
        }
        else {
            images[6] = null;
        }

        return images;
    }
}

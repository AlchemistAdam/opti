package dk.martinu.opti.img.png;

import dk.martinu.opti.img.spi.ImageDataException;

import static dk.martinu.opti.img.png.PngInfo.BIT_DEPTH_16;
import static dk.martinu.opti.img.png.PngInfo.BIT_DEPTH_8;

public final class Truecolor implements ColorType {

    public static final int COMPONENT_COUNT = 3;

    @Override
    public int getComponentCount() {
        return COMPONENT_COUNT;
    }

    @Override
    public String getName() {
        return "TRUECOLOR";
    }

    @Override
    public PixelSetter getPixelSetter(int bitDepth, ReducedImage image, byte[] palette,
            byte[] transparency, byte[] background) throws ImageDataException {
        validateBitDepth(bitDepth);
        if (bitDepth == BIT_DEPTH_8) {
            if (transparency != null && background != null) {
                return new PixelSetter_8_Alpha(image, transparency, background);
            }
            else {
                return new PixelSetter_8(image);
            }
        }
        else /* if (bitDepth == BIT_DEPTH_16) */ {
            if (transparency != null && background != null) {
                return new PixelSetter_16_Alpha(image, transparency, background);
            }
            else {
                return new PixelSetter_16(image);
            }
        }
    }

    @Override
    public int getValue() {
        return 2;
    }

    @Override
    public void validateBitDepth(int bitDepth) throws ImageDataException {
        if (bitDepth != BIT_DEPTH_8 && bitDepth != BIT_DEPTH_16) {
            throw new ImageDataException("invalid bit depth for color type %s {%d}", getName(), bitDepth);
        }
    }

    private static final class PixelSetter_16 extends AbstractPixelSetter {

        PixelSetter_16(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            dest[index]     = samples[i];
            dest[index + 1] = samples[i + 2];
            dest[index + 2] = samples[i + 4];
            i += COMPONENT_COUNT * 2;
        }
    }

    private static final class PixelSetter_16_Alpha extends AbstractPixelSetter {

        final byte[] trns;
        final byte[] bkgd;

        PixelSetter_16_Alpha(ReducedImage image, byte[] trns, byte[] bkgd) {
            super(image);
            this.trns = trns;
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // transfer samples if not transparent
            if (samples[i] != trns[0] || samples[i + 1] != trns[1] ||
                    samples[i + 2] != trns[2] || samples[i + 3] != trns[3] ||
                    samples[i + 4] != trns[4] || samples[i + 5] != trns[5]) {
                dest[index]     = samples[i];
                dest[index + 1] = samples[i + 2];
                dest[index + 2] = samples[i + 4];
            }
            // otherwise replace with background color
            else {
                dest[index]     = bkgd[0];
                dest[index + 1] = bkgd[2];
                dest[index + 2] = bkgd[4];
            }
            i += COMPONENT_COUNT * 2;
        }
    }

    private static final class PixelSetter_8 extends AbstractPixelSetter {

        PixelSetter_8(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            dest[index]     = samples[i];
            dest[index + 1] = samples[i + 1];
            dest[index + 2] = samples[i + 2];
            i += COMPONENT_COUNT;
        }
    }

    private static final class PixelSetter_8_Alpha extends AbstractPixelSetter {

        final byte[] trns;
        final byte[] bkgd;

        PixelSetter_8_Alpha(ReducedImage image, byte[] trns, byte[] bkgd) {
            super(image);
            this.trns = trns;
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // transfer samples if not transparent
            if (samples[i] != trns[1] || samples[i + 1] != trns[3] || samples[i + 2] != trns[5]) {
                dest[index]     = samples[i];
                dest[index + 1] = samples[i + 1];
                dest[index + 2] = samples[i + 2];
            }
            // otherwise replace with background color
            else {
                dest[index]     = bkgd[1];
                dest[index + 1] = bkgd[3];
                dest[index + 2] = bkgd[5];
            }
            i += COMPONENT_COUNT;
        }
    }
}

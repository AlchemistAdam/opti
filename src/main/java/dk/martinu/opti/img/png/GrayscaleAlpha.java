package dk.martinu.opti.img.png;

import dk.martinu.opti.img.spi.ImageDataException;

import java.util.Objects;

import static dk.martinu.opti.img.png.PngInfo.BIT_DEPTH_16;
import static dk.martinu.opti.img.png.PngInfo.BIT_DEPTH_8;

public final class GrayscaleAlpha implements ColorType {

    public static final int COMPONENT_COUNT = 2;

    @Override
    public int getComponentCount() {
        return COMPONENT_COUNT;
    }

    @Override
    public String getName() {
        return "GRAYSCALE_ALPHA";
    }

    @Override
    public PixelSetter getPixelSetter(int bitDepth, ReducedImage image, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException {
        validateBitDepth(bitDepth);
        Objects.requireNonNull(background, "background is null");
        if (bitDepth == BIT_DEPTH_8) {
            return new PixelSetter_8(image, background);
        }
        else /* if (bitDepth == BIT_DEPTH_16) */ {
            return new PixelSetter_16(image, background);
        }
    }

    @Override
    public int getValue() {
        return 4;
    }

    @Override
    public void validateBitDepth(int bitDepth) throws ImageDataException {
        if (bitDepth != BIT_DEPTH_8 && bitDepth != BIT_DEPTH_16) {
            throw new ImageDataException("invalid bit depth for color type %s {%d}", getName(), bitDepth);
        }
    }

    private static final class PixelSetter_16 extends AbstractPixelSetter {

        final byte[] bkgd;

        PixelSetter_16(ReducedImage image, byte[] bkgd) {
            super(image);
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            int alpha = samples[i + 2] & 0xFF;
            if (alpha == 0xFF) {
                dest[index] = samples[i];
            }
            else if (alpha == 0) {
                dest[index] = bkgd[0];
            }
            else {
                // output = alpha * foreground + (1-alpha) * background
                float alpha_fg = alpha / 255.0F;
                float alpha_bg = 1.0F - alpha_fg;
                dest[index] = (byte) ((int) (alpha_fg * (samples[i] & 0xFF)) + (int) (alpha_bg * (bkgd[0] & 0xFF)));
            }
            i += COMPONENT_COUNT * 2;
        }
    }

    private static final class PixelSetter_8 extends AbstractPixelSetter {

        final byte[] bkgd;

        PixelSetter_8(ReducedImage image, byte[] bkgd) {
            super(image);
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            int alpha = samples[i + 1] & 0xFF;
            if (alpha == 0xFF) {
                dest[index] = samples[i];
            }
            else if (alpha == 0) {
                dest[index] = bkgd[1];
            }
            else {
                // output = alpha * foreground + (1-alpha) * background
                float alpha_fg = alpha / 255.0F;
                float alpha_bg = 1.0F - alpha_fg;
                dest[index] = (byte) ((int) (alpha_fg * (samples[i] & 0xFF)) + (int) (alpha_bg * (bkgd[1] & 0xFF)));
            }
            i += COMPONENT_COUNT;
        }
    }
}

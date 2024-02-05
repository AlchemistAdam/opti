package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

import static dk.martinu.opti.img.spi.png.PngInfo.*;
import static dk.martinu.opti.img.spi.png.InconstantPixelSetter.wrapIfInconstant;

public final class Indexed implements ColorType {

    public static final int COMPONENT_COUNT = 1;

    @Override
    public int getComponentCount() {
        return COMPONENT_COUNT;
    }

    @Override
    public String getName() {
        return "INDEXED";
    }

    @Override
    public PixelSetter getPixelSetter(int bitDepth, ReducedImage image, byte[] palette,
            byte[] transparency, byte[] background) throws ImageDataException {
        validateBitDepth(bitDepth);
        // NOTE: palette is premultiplied with alpha by PngInfo
        return switch (bitDepth) {
            case BIT_DEPTH_8 -> new PixelSetter_8(image, palette);
            case BIT_DEPTH_4 -> wrapIfInconstant(bitDepth, image.width(), new PixelSetter_4(image, palette));
            case BIT_DEPTH_2 -> wrapIfInconstant(bitDepth, image.width(), new PixelSetter_2(image, palette));
            // BIT_DEPTH_1
            default -> wrapIfInconstant(bitDepth, image.width(), new PixelSetter_1(image, palette));
        };
    }

    @Override
    public int getValue() {
        return 3;
    }

    @Override
    public void validateBitDepth(int bitDepth) throws ImageDataException {
        if (bitDepth == BIT_DEPTH_16) {
            throw new ImageDataException("invalid bit depth for color type %s {%d}", getName(), bitDepth);
        }
    }

    private static final class PixelSetter_1 extends PackedPixelSetter {

        final byte[] plte;

        PixelSetter_1(ReducedImage image, byte[] plte) {
            super(image);
            this.plte = plte;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // palette index
            int k;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                k = (sampleByte >> 7 & 0x01) * 3;
                position++;
            }
            else if (position == 1) {
                k = (sampleByte >> 6 & 0x01) * 3;
                position++;
            }
            else if (position == 2) {
                k = (sampleByte >> 5 & 0x01) * 3;
                position++;
            }
            else if (position == 3) {
                k = (sampleByte >> 4 & 0x01) * 3;
                position++;
            }
            else if (position == 4) {
                k = (sampleByte >> 3 & 0x01) * 3;
                position++;
            }
            else if (position == 5) {
                k = (sampleByte >> 2 & 0x01) * 3;
                position++;
            }
            else if (position == 6) {
                k = (sampleByte >> 1 & 0x01) * 3;
                position++;
            }
            else /* if (position == 7) */ {
                k = (sampleByte & 0x01) * 3;
                position = 0;
                i += COMPONENT_COUNT;
            }
            dest[index]     = plte[k];
            dest[index + 1] = plte[k + 1];
            dest[index + 2] = plte[k + 2];
        }
    }

//    private static final class PixelSetter_1_Alpha extends PackedPixelSetter {
//
//        final byte[] plte;
//        final byte[] trns;
//        final byte[] bkgd;
//
//        PixelSetter_1_Alpha(ReducedImage image, byte[] plte, byte[] trns, byte[] bkgd) {
//            super(image);
//            this.plte = plte;
//            this.trns = trns;
//            this.bkgd = bkgd;
//        }
//
//        @Override
//        public void setNext(byte[] dest, int index) {
//            // palette index
//            int k;
//            if (position == 0) {
//                sampleByte = samples[i] & 0xFF;
//                k = (sampleByte >> 7 & 0x01) * 3;
//                position++;
//            }
//            else if (position == 1) {
//                k = (sampleByte >> 6 & 0x01) * 3;
//                position++;
//            }
//            else if (position == 2) {
//                k = (sampleByte >> 5 & 0x01) * 3;
//                position++;
//            }
//            else if (position == 3) {
//                k = (sampleByte >> 4 & 0x01) * 3;
//                position++;
//            }
//            else if (position == 4) {
//                k = (sampleByte >> 3 & 0x01) * 3;
//                position++;
//            }
//            else if (position == 5) {
//                k = (sampleByte >> 2 & 0x01) * 3;
//                position++;
//            }
//            else if (position == 6) {
//                k = (sampleByte >> 1 & 0x01) * 3;
//                position++;
//            }
//            else /* if (position == 7) */ {
//                k = (sampleByte & 0x01) * 3;
//                position = 0;
//                i += COMPONENT_COUNT;
//            }
//            int alpha = k < trns.length ? trns[k] & 0x01 : 0xFF;
//            if (alpha == 0xFF) {
//                k *= 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else if (alpha == 0) {
//                k = (bkgd[0] & 0x01) * 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else {
//                k *= 3;
//                // background palette index
//                int m = (bkgd[0] & 0x01) * 3;
//                // output = alpha * foreground + (1-alpha) * background
//                float alpha_fg = alpha / 255.0F;
//                float alpha_bg = 1.0F - alpha_fg;
//                dest[index]     = (byte) ((int) (alpha_fg * (plte[k] & 0xFF)) + (int) (alpha_bg * (plte[m] & 0xFF)));
//                dest[index + 1] = (byte) ((int) (alpha_fg * (plte[k + 1] & 0xFF)) + (int) (alpha_bg * (plte[m + 1] & 0xFF)));
//                dest[index + 2] = (byte) ((int) (alpha_fg * (plte[k + 2] & 0xFF)) + (int) (alpha_bg * (plte[m + 2] & 0xFF)));
//            }
//        }
//    }

    private static final class PixelSetter_2 extends PackedPixelSetter {

        final byte[] plte;

        PixelSetter_2(ReducedImage image, byte[] plte) {
            super(image);
            this.plte = plte;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // palette index
            int k;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                k = (sampleByte >> 6 & 0x03) * 3;
                position++;
            }
            else if (position == 1) {
                k = (sampleByte >> 4 & 0x03) * 3;
                position++;
            }
            else if (position == 2) {
                k = (sampleByte >> 2 & 0x03) * 3;
                position++;
            }
            else /* if (position == 3) */ {
                k = (sampleByte & 0x03) * 3;
                position = 0;
                i += COMPONENT_COUNT;
            }
            dest[index]     = plte[k];
            dest[index + 1] = plte[k + 1];
            dest[index + 2] = plte[k + 2];
        }
    }

//    private static final class PixelSetter_2_Alpha extends PackedPixelSetter {
//
//        final byte[] plte;
//        final byte[] trns;
//        final byte[] bkgd;
//
//        PixelSetter_2_Alpha(ReducedImage image, byte[] plte, byte[] trns, byte[] bkgd) {
//            super(image);
//            this.plte = plte;
//            this.trns = trns;
//            this.bkgd = bkgd;
//        }
//
//        @Override
//        public void setNext(byte[] dest, int index) {
//            // palette index
//            int k;
//            if (position == 0) {
//                sampleByte = samples[i] & 0xFF;
//                k = sampleByte >> 6 & 0x03;
//                position++;
//            }
//            else if (position == 1) {
//                k = sampleByte >> 4 & 0x03;
//                position++;
//            }
//            else if (position == 2) {
//                k = sampleByte >> 2 & 0x03;
//                position++;
//            }
//            else /* if (position == 3) */ {
//                k = sampleByte & 0x03;
//                position = 0;
//                i += COMPONENT_COUNT;
//            }
//            int alpha = k < trns.length ? trns[k] & 0x03 : 0xFF;
//            if (alpha == 0xFF) {
//                k *= 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else if (alpha == 0) {
//                k = (bkgd[0] & 0x03) * 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else {
//                k *= 3;
//                // background palette index
//                int m = (bkgd[0] & 0x03) * 3;
//                // output = alpha * foreground + (1-alpha) * background
//                float alpha_fg = alpha / 255.0F;
//                float alpha_bg = 1.0F - alpha_fg;
//                dest[index]     = (byte) ((int) (alpha_fg * (plte[k] & 0xFF)) + (int) (alpha_bg * (plte[m] & 0xFF)));
//                dest[index + 1] = (byte) ((int) (alpha_fg * (plte[k + 1] & 0xFF)) + (int) (alpha_bg * (plte[m + 1] & 0xFF)));
//                dest[index + 2] = (byte) ((int) (alpha_fg * (plte[k + 2] & 0xFF)) + (int) (alpha_bg * (plte[m + 2] & 0xFF)));
//            }
//        }
//    }

    private static final class PixelSetter_4 extends PackedPixelSetter {

        final byte[] plte;

        PixelSetter_4(ReducedImage image, byte[] plte) {
            super(image);
            this.plte = plte;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // palette index
            int k;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                k = (sampleByte >> 4 & 0x0F) * 3;
                position++;
            }
            else /* if (position == 1) */ {
                k = (sampleByte & 0x0F) * 3;
                position = 0;
                i += COMPONENT_COUNT;
            }
            dest[index]     = plte[k];
            dest[index + 1] = plte[k + 1];
            dest[index + 2] = plte[k + 2];
        }
    }

//    private static final class PixelSetter_4_Alpha extends PackedPixelSetter {
//
//        final byte[] plte;
//        final byte[] trns;
//        final byte[] bkgd;
//
//
//        PixelSetter_4_Alpha(ReducedImage image, byte[] plte, byte[] trns, byte[] bkgd) {
//            super(image);
//            this.plte = plte;
//            this.trns = trns;
//            this.bkgd = bkgd;
//        }
//
//        @Override
//        public void setNext(byte[] dest, int index) {
//            // palette index
//            int k;
//            if (position == 0) {
//                sampleByte = samples[i] & 0xFF;
//                k = sampleByte >> 4 & 0x0F;
//                position++;
//            }
//            else /* if (position == 1) */ {
//                k = sampleByte & 0x0F;
//                position = 0;
//                i += COMPONENT_COUNT;
//            }
//            int alpha = k < trns.length ? trns[k] & 0x0F : 0xFF;
//            if (alpha == 0xFF) {
//                k *= 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else if (alpha == 0) {
//                k = (bkgd[0] & 0x0F) * 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else {
//                k *= 3;
//                // background palette index
//                int m = (bkgd[0] & 0x0F) * 3;
//                // output = alpha * foreground + (1-alpha) * background
//                float alpha_fg = alpha / 255.0F;
//                float alpha_bg = 1.0F - alpha_fg;
//                dest[index]     = (byte) ((int) (alpha_fg * (plte[k] & 0xFF)) + (int) (alpha_bg * (plte[m] & 0xFF)));
//                dest[index + 1] = (byte) ((int) (alpha_fg * (plte[k + 1] & 0xFF)) + (int) (alpha_bg * (plte[m + 1] & 0xFF)));
//                dest[index + 2] = (byte) ((int) (alpha_fg * (plte[k + 2] & 0xFF)) + (int) (alpha_bg * (plte[m + 2] & 0xFF)));
//            }
//        }
//    }

    private static final class PixelSetter_8 extends AbstractPixelSetter {

        final byte[] plte;

        PixelSetter_8(ReducedImage image, byte[] plte) {
            super(image);
            this.plte = plte;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // palette index
            int k = (samples[i] & 0xFF) * 3;
            dest[index]     = plte[k];
            dest[index + 1] = plte[k + 1];
            dest[index + 2] = plte[k + 2];
            i += COMPONENT_COUNT;
        }
    }

//    private static final class PixelSetter_8_Alpha extends AbstractPixelSetter {
//
//        final byte[] plte;
//        final byte[] trns;
//        final byte[] bkgd;
//
//
//        PixelSetter_8_Alpha(ReducedImage image, byte[] plte, byte[] trns, byte[] bkgd) {
//            super(image);
//            this.plte = plte;
//            this.trns = trns;
//            this.bkgd = bkgd;
//        }
//
//        @Override
//        public void setNext(byte[] dest, int index) {
//            // palette index
//            int k = samples[i] & 0xFF;
//            int alpha = k < trns.length ? trns[k] & 0xFF : 0xFF;
//            if (alpha == 0xFF) {
//                k *= 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else if (alpha == 0) {
//                k = (bkgd[0] & 0xFF) * 3;
//                dest[index]     = plte[k];
//                dest[index + 1] = plte[k + 1];
//                dest[index + 2] = plte[k + 2];
//            }
//            else {
//                k *= 3;
//                // background palette index
//                int m = (bkgd[0] & 0xFF) * 3;
//                // output = alpha * foreground + (1-alpha) * background
//                float alpha_fg = alpha / 255.0F;
//                float alpha_bg = 1.0F - alpha_fg;
//                dest[index]     = (byte) ((int) (alpha_fg * (plte[k] & 0xFF)) + (int) (alpha_bg * (plte[m] & 0xFF)));
//                dest[index + 1] = (byte) ((int) (alpha_fg * (plte[k + 1] & 0xFF)) + (int) (alpha_bg * (plte[m + 1] & 0xFF)));
//                dest[index + 2] = (byte) ((int) (alpha_fg * (plte[k + 2] & 0xFF)) + (int) (alpha_bg * (plte[m + 2] & 0xFF)));
//            }
//            i += COMPONENT_COUNT;
//        }
//    }
}

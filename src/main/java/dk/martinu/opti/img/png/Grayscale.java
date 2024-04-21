/*
 * Copyright (c) 2024, Adam Martinu. All rights reserved. Altering or
 * removing copyright notices or this file header is not allowed.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package dk.martinu.opti.img.png;

import static dk.martinu.opti.img.png.PngInfo.*;

public final class Grayscale implements ColorType {

    public static final int COMPONENT_COUNT = 1;

    @Override
    public int getComponentCount() {
        return COMPONENT_COUNT;
    }

    @Override
    public String getName() {
        return "GRAYSCALE";
    }

    @Override
    public PixelSetter getPixelSetter(int bitDepth, ReducedImage image, byte[] palette, byte[] transparency, byte[] background) {
        if (transparency != null && background != null) {
            return switch (bitDepth) {
                case BIT_DEPTH_16 -> new PixelSetter_16_Alpha(image, transparency, background);
                case BIT_DEPTH_8 -> new PixelSetter_8_Alpha(image, transparency, background);
                case BIT_DEPTH_4 -> InconstantPixelSetter.wrapIfInconstant(bitDepth, image.width, new PixelSetter_4_Alpha(image, transparency, background));
                case BIT_DEPTH_2 -> InconstantPixelSetter.wrapIfInconstant(bitDepth, image.width, new PixelSetter_2_Alpha(image, transparency, background));
                // BIT_DEPTH_1
                default -> InconstantPixelSetter.wrapIfInconstant(bitDepth, image.width, new PixelSetter_1_Alpha(image, transparency, background));
            };
        }
        else {
            return switch (bitDepth) {
                case BIT_DEPTH_16 -> new PixelSetter_16(image);
                case BIT_DEPTH_8 -> new PixelSetter_8(image);
                case BIT_DEPTH_4 -> InconstantPixelSetter.wrapIfInconstant(bitDepth, image.width, new PixelSetter_4(image));
                case BIT_DEPTH_2 -> InconstantPixelSetter.wrapIfInconstant(bitDepth, image.width, new PixelSetter_2(image));
                // BIT_DEPTH_1
                default -> InconstantPixelSetter.wrapIfInconstant(bitDepth, image.width, new PixelSetter_1(image));
            };
        }
    }

    @Override
    public int getValue() {
        return 0;
    }

    private static final class PixelSetter_1 extends PackedPixelSetter {

        public PixelSetter_1(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // sample stored in the lowest bit
            int b;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                b = sampleByte >> 7 & 0x01;
                position++;
            }
            else if (position == 1) {
                b = sampleByte >> 6 & 0x01;
                position++;
            }
            else if (position == 2) {
                b = sampleByte >> 5 & 0x01;
                position++;
            }
            else if (position == 3) {
                b = sampleByte >> 4 & 0x01;
                position++;
            }
            else if (position == 4) {
                b = sampleByte >> 3 & 0x01;
                position++;
            }
            else if (position == 5) {
                b = sampleByte >> 2 & 0x01;
                position++;
            }
            else if (position == 6) {
                b = sampleByte >> 1 & 0x01;
                position++;
            }
            else /* if (position == 7) */ {
                b = sampleByte & 0x01;
                position = 0;
                i += COMPONENT_COUNT;
            }
            // since there are only 2 possible values, the sample value is
            // assigned directly instead of replicating bits
            byte s = b == 0 ? 0 : (byte) 0xFF;
            dest[index]     = s;
        }
    }

    private static final class PixelSetter_16 extends AbstractPixelSetter {

        PixelSetter_16(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            byte s = samples[i];
            dest[index]     = s;
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
            byte s;
            // transfer sample if not transparent
            if (samples[i] != trns[0] || samples[i + 1] != trns[1]) {
                s = samples[i];
            }
            // otherwise replace with background color
            else {
                s = bkgd[0];
            }
            dest[index]     = s;
            i += COMPONENT_COUNT * 2;
        }
    }

    private static final class PixelSetter_1_Alpha extends PackedPixelSetter {

        final byte[] trns;
        final byte[] bkgd;

        PixelSetter_1_Alpha(ReducedImage image, byte[] trns, byte[] bkgd) {
            super(image);
            this.trns = trns;
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // sample stored in the lowest bit
            int b;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                b = sampleByte >> 7 & 0x01;
                position++;
            }
            else if (position == 1) {
                b = sampleByte >> 6 & 0x01;
                position++;
            }
            else if (position == 2) {
                b = sampleByte >> 5 & 0x01;
                position++;
            }
            else if (position == 3) {
                b = sampleByte >> 4 & 0x01;
                position++;
            }
            else if (position == 4) {
                b = sampleByte >> 3 & 0x01;
                position++;
            }
            else if (position == 5) {
                b = sampleByte >> 2 & 0x01;
                position++;
            }
            else if (position == 6) {
                b = sampleByte >> 1 & 0x01;
                position++;
            }
            else /* if (position == 7) */ {
                b = sampleByte & 0x01;
                position = 0;
                i += COMPONENT_COUNT;
            }
            // replace with background color if transparent
            if (b == (trns[1] & 0x01)) {
                b = (bkgd[1] & 0x01);
            }
            // since there are only 2 possible values, the sample value is
            // assigned directly instead of replicating bits
            byte s = b == 0 ? 0 : (byte) 0xFF;
            dest[index]     = s;
        }
    }

    private static final class PixelSetter_2 extends PackedPixelSetter {

        public PixelSetter_2(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // sample stored in lowest 2 bits
            int b;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                b = sampleByte >> 6 & 0x03;
                position++;
            }
            else if (position == 1) {
                b = sampleByte >> 4 & 0x03;
                position++;
            }
            else if (position == 2) {
                b = sampleByte >> 2 & 0x03;
                position++;
            }
            else /* if (position == 3) */ {
                b = sampleByte & 0x03;
                position = 0;
                i += COMPONENT_COUNT;
            }
            // since there are only 4 possible values, the sample value is
            // assigned directly instead of replicating bits
            byte s = switch (b) {
                case 0 -> 0;
                case 1 -> 0x55;
                case 2 -> (byte) 0xAA;
                default -> (byte) 0xFF; // case 3
            };
            dest[index]     = s;
        }
    }

    private static final class PixelSetter_2_Alpha extends PackedPixelSetter {

        final byte[] trns;
        final byte[] bkgd;

        PixelSetter_2_Alpha(ReducedImage image, byte[] trns, byte[] bkgd) {
            super(image);
            this.trns = trns;
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // sample stored in lowest 2 bits
            int b;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                b = sampleByte >> 6 & 0x03;
                position++;
            }
            else if (position == 1) {
                b = sampleByte >> 4 & 0x03;
                position++;
            }
            else if (position == 2) {
                b = sampleByte >> 2 & 0x03;
                position++;
            }
            else /* if (position == 3) */ {
                b = sampleByte & 0x03;
                position = 0;
                i += COMPONENT_COUNT;
            }
            // replace with background color if transparent
            if (b == (trns[1] & 0x03)) {
                b = (bkgd[1] & 0x03);
            }
            // since there are only 4 possible values, the sample value is
            // assigned directly instead of replicating bits
            byte s = switch (b) {
                case 0 -> 0;
                case 1 -> 0x55;
                case 2 -> (byte) 0xAA;
                default -> (byte) 0xFF; // case 3
            };
            dest[index]     = s;
        }
    }

    private static final class PixelSetter_4 extends PackedPixelSetter {

        PixelSetter_4(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            byte s;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                int b = sampleByte & 0xF0;
                s = (byte) (b | b >> 4);
                position++;
            }
            else /* if (position == 1) */ {
                int b = sampleByte & 0x0F;
                s = (byte) (b | b << 4);
                position = 0;
                i += COMPONENT_COUNT;
            }
            dest[index]     = s;
        }
    }

    private static final class PixelSetter_4_Alpha extends PackedPixelSetter {

        final byte[] trns;
        final byte[] bkgd;

        PixelSetter_4_Alpha(ReducedImage image, byte[] trns, byte[] bkgd) {
            super(image);
            this.trns = trns;
            this.bkgd = bkgd;
        }

        @Override
        public void setNext(byte[] dest, int index) {
            // output sample value
            byte s;
            if (position == 0) {
                sampleByte = samples[i] & 0xFF;
                int b = sampleByte & 0xF0;
                s = (byte) (b | b >> 4);
                position++;
            }
            else /* if (position == 1) */ {
                int b = sampleByte & 0x0F;
                s = (byte) (b << 4 | b );
                position = 0;
                i += COMPONENT_COUNT;
            }
            // replace with background color if transparent
            if ((s & 0x0F) == (trns[1] & 0x0F)) {
                int b = bkgd[1] & 0x0F;
                s = (byte) (b << 4 | b);
            }
            dest[index]     = s;
        }
    }

    private static final class PixelSetter_8 extends AbstractPixelSetter {

        PixelSetter_8(ReducedImage image) {
            super(image);
        }

        @Override
        public void setNext(byte[] dest, int index) {
            byte s = samples[i];
            dest[index]     = s;
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
            byte s = samples[i];
            // replace with background color if transparent
            if (s == trns[1]) {
                s = bkgd[1];
            }
            dest[index]     = s;
            i += COMPONENT_COUNT;
        }
    }
}

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

import dk.martinu.opti.img.spi.ImageDataException;

import static dk.martinu.opti.img.png.PngInfo.*;
import static dk.martinu.opti.img.png.InconstantPixelSetter.wrapIfInconstant;

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
            case BIT_DEPTH_4 -> wrapIfInconstant(bitDepth, image.width, new PixelSetter_4(image, palette));
            case BIT_DEPTH_2 -> wrapIfInconstant(bitDepth, image.width, new PixelSetter_2(image, palette));
            // BIT_DEPTH_1
            default -> wrapIfInconstant(bitDepth, image.width, new PixelSetter_1(image, palette));
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
}

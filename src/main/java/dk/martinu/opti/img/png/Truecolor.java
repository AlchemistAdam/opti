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

import static dk.martinu.opti.img.png.PngInfo.BIT_DEPTH_16;
import static dk.martinu.opti.img.png.PngInfo.BIT_DEPTH_8;

final class Truecolor implements ColorType {

    static final int COMPONENT_COUNT = 3;

    @Override
    public int getComponentCount() {
        return COMPONENT_COUNT;
    }

    @Override
    public String getName() {
        return "TRUECOLOR";
    }

    @Override
    public PixelSetter getPixelSetter(int bitDepth, ReducedImage image, byte[] plte, byte[] trns, byte[] bkgd) throws ImageDataException {
        validateBitDepth(bitDepth);
        if (trns != null && bkgd != null) {
            if (bitDepth == BIT_DEPTH_8) {
                return new PixelSetter_8_Alpha(image, trns, bkgd);
            }
            else /* if (bitDepth == BIT_DEPTH_16) */ {
                return new PixelSetter_16_Alpha(image, trns, bkgd);
            }
        }
        else if (bitDepth == BIT_DEPTH_8) {
            return new PixelSetter_8(image);
        }
        else /* if (bitDepth == BIT_DEPTH_16) */ {
            return new PixelSetter_16(image);
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

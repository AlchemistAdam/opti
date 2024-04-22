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

import java.util.Arrays;

/**
 * Interface to represent an interlace method. This interface only declares
 * methods for reversing (combining) the reduced images produced by pass
 * extraction.
 * <p>
 * See <a href="https://www.w3.org/TR/png/#8Interlace">8. Interlacing and pass
 * extraction</a> for details on interlace methods.
 * <p>
 * <b>NOTE:</b> because filtering is applied to the bytes of reduced images, and
 * the number of reduced images in a PNG data stream depends on the interlace
 * method used when encoding the PNG image, the PNG data stream passed to an
 * <b>must</b> be filtered. Reversing the filter transform is handled by the
 * {@code InterlaceMethod} implementation.
 *
 * @author Adam Martinu
 * @see NullMethod
 * @see Adam7
 * @since 1.0
 */
interface InterlaceMethod {

    /**
     * Reverses the filter transform on each reduced image in {@code filt} and
     * combines them into a single image, using the specified filter method and
     * parameters, and returns it.
     *
     * @param width        the width of the PNG image
     * @param height       the height of the PNG image
     * @param bitDepth     the image bit depth
     * @param colorType    the image color type
     * @param filterMethod the {@code FilterMethod} used to reconstruct the
     *                     filtered sample bytes
     * @param filt         the filtered sample bytes
     * @param plte         the color palette, or {@code null}
     * @param trns         the transparent colors, or {@code null}
     * @param bkgd         the background color, or {@code null}
     * @return an array of sample values of a single image, ordered left to
     * right, top to bottom
     * @throws ImageDataException if an error occurred when combining the
     *                            reduced images
     */
    byte[] getCombinedSamples(int width, int height, int bitDepth, ColorType colorType, FilterMethod filterMethod,
            byte[] filt, byte[] plte, byte[] trns, byte[] bkgd) throws ImageDataException;

    // DOC
    default byte[] getCompositingBackground(ColorType colorType, byte[] plte, byte[] bkgd) {
        if (bkgd == null) {
            // create new default (white) background color for color type
            // INDEXED
            if (colorType.usesPalette()) {
                return new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            }
            // TRUECOLOR and TRUECOLOR_ALPHA
            if (colorType.usesTruecolor()) {
                return new byte[] {
                        (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0xFF};
            }
            // GREYSCALE and GREYSCALE_ALPHA
            else {
                return new byte[] {(byte) 0xFF, (byte) 0xFF};
            }
        }
        else if (colorType.usesPalette()) {
            // get background color from palette
            int index = (bkgd[0] & 0xFF) * 3;
            return new byte[] {
                    plte[index],
                    plte[index + 1],
                    plte[index + 2]};
        }
        else {
            // background color samples are stored in array
            return bkgd;
        }
    }

    // DOC
    default byte[] getPremultipliedPalette(ColorType colorType, byte[] plte, byte[] trns, byte[] bkgd) {
        if (colorType.usesPalette() && plte != null && trns != null) {
            // background color constants for multiplying
            final float r = (float) (bkgd[0] & 0xFF);
            final float g = (float) (bkgd[1] & 0xFF);
            final float b = (float) (bkgd[2] & 0xFF);
            // return value with premultiplied colors
            byte[] copy = Arrays.copyOf(plte, plte.length);
            // iterate over all entries in tRNS (may contain fewer entries than palette entries)
            for (int i = 0, pi = 0; i < trns.length; i++, pi += 3) {
                int alpha = trns[i] & 0xFF;
                // fully transparent
                if (alpha == 0) {
                    copy[pi]     = bkgd[0];
                    copy[pi + 1] = bkgd[1];
                    copy[pi + 2] = bkgd[2];
                }
                // partially transparent
                else if (alpha != 0xFF) {
                    // output = alpha * foreground + (1-alpha) * background
                    float alpha_fg = alpha / 255.0F;
                    float alpha_bg = 1.0F - alpha_fg;
                    copy[pi]     = (byte) ((int) (alpha_fg * (copy[pi] & 0xFF)) + (int) (alpha_bg * r));
                    copy[pi + 1] = (byte) ((int) (alpha_fg * (copy[pi + 1] & 0xFF)) + (int) (alpha_bg * g));
                    copy[pi + 2] = (byte) ((int) (alpha_fg * (copy[pi + 2] & 0xFF)) + (int) (alpha_bg * b));
                }
            }
            return copy;
        }
        else {
            return plte;
        }
    }
}

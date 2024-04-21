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

/**
 * Implementation of a filter method that can reconstruct sample bytes filtered
 * with filter method 0 as described in the PNG Specification
 * <a href="https://www.w3.org/TR/png/#9Filters">9. Filtering</a>.
 *
 * @author Adam Martinu
 * @since 1.0
 */
final class FilterMethod_0 implements FilterMethod {

    /**
     * Constant for Filter type 0 (None).
     */
    private static final int TYPE_NONE = 0;
    /**
     * Constant for Filter type 1 (Sub).
     */
    private static final int TYPE_SUB = 1;
    /**
     * Constant for Filter type 2 (Up).
     */
    private static final int TYPE_UP = 2;
    /**
     * Constant for Filter type 3 (Average).
     */
    private static final int TYPE_AVERAGE = 3;
    /**
     * Constant for Filter type 4 (Paeth).
     */
    private static final int TYPE_PAETH = 4;

    @Override
    public byte[] reconstruct(int bitDepth, ColorType colorType, byte[] filt, int offset, int lines, int nBytes) throws ImageDataException {
        // destination for reconstructed bytes
        byte[] recon = new byte[lines * nBytes];

        // the offset to subtract from an index in a scanline to get filter bytes 'a' and 'c'
        int filterOffset;
        if (bitDepth < 8) {
            filterOffset = 1;
        }
        else {
            int sampleSize = bitDepth / 8;
            filterOffset = sampleSize * (colorType.usesTruecolor() ? 3 : 1);
            if (colorType.usesAlpha()) {
                filterOffset += sampleSize;
            }
        }

        /*
        reconstruct filtered bytes
        https://www.w3.org/TR/png/#9Filter-types
        ----
        i: current scanline
        j: index in filt
        k: index in recon
        ----
        j and k are assigned in outer loop and incremented in inner loops
         */
        for (int i = 0, j = offset + 1, k = 0; i < lines; i++, j = i * (nBytes + 1) + offset + 1, k = i * nBytes) {
            int filterTypeIndex = j - 1;
            // filter type preceding filtered bytes in scanline
            int filterType = filt[filterTypeIndex] & 0xFF;

            if (filterType == TYPE_NONE) {
                System.arraycopy(filt, j, recon, k, nBytes);
            }

            else if (filterType == TYPE_SUB) {
                for (int max = j + nBytes; j < max; j++, k++) {
                    if (j - filterOffset <= filterTypeIndex) {
                        recon[k] = filt[j];
                    }
                    else {
                        recon[k] = (byte) ((filt[j] & 0xFF) + (recon[k - filterOffset] & 0xFF));
                    }
                }
            }

            else if (filterType == TYPE_UP) {
                if (i == 0) {
                    System.arraycopy(filt, j, recon, k, nBytes);
                }
                else {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        recon[k] = (byte) ((filt[j] & 0xFF) + (recon[k - nBytes] & 0xFF));
                    }
                }
            }

            else if (filterType == TYPE_AVERAGE) {
                if (i == 0) {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        if (j - filterOffset <= filterTypeIndex) {
                            recon[k] = filt[j];
                        }
                        else {
                            recon[k] = (byte) ((filt[j] & 0xFF) + ((recon[k - filterOffset] & 0xFF) >>> 1));
                        }
                    }
                }
                else {
                    for (int m, max = j + nBytes; j < max; j++, k++) {
                        m = j - filterOffset;
                        if (m <= filterTypeIndex) {
                            recon[k] = (byte) ((filt[j] & 0xFF) + ((recon[k - nBytes] & 0xFF) >>> 1));
                        }
                        else {
                            recon[k] = (byte) ((filt[j] & 0xFF) + (((recon[k - filterOffset] & 0xFF) + (recon[k - nBytes] & 0xFF)) >>> 1));
                        }
                    }
                }
            }

            // https://www.w3.org/TR/png/#9Filter-type-4-Paeth
            else if (filterType == TYPE_PAETH) {
                if (i == 0) {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        if (j - filterOffset <= filterTypeIndex) {
                            recon[k] = filt[j];
                        }
                        else {
                            recon[k] = (byte) ((filt[j] & 0xFF) + (recon[k - filterOffset] & 0xFF));
                        }
                    }
                }
                else {
                    // filter bytes
                    int a, b = recon[k - nBytes] & 0xFF, c;
                    // paeth function variables
                    int p, pa, pb, pc, pr;

                    final int max = j + nBytes;
                    // the predictor (pr) is always 'b' for the first byte
                    recon[k++] = (byte) ((filt[j++] & 0xFF) + b);

                    while (j < max) {
                        // assign a, b and c
                        if (j - filterOffset <= filterTypeIndex) {
                            a = 0;
                            c = 0;
                        }
                        else {
                            a = recon[k - filterOffset] & 0xFF;
                            c = recon[k - nBytes - filterOffset] & 0xFF;
                        }
                        b = recon[k - nBytes] & 0xFF;
                        // update paeth variables
                        p  = a + b - c;
                        pa = Math.abs(p - a);
                        pb = Math.abs(p - b);
                        pc = Math.abs(p - c);
                        // determine predictor
                        if (pa <= pb && pa <= pc) {
                            pr = a;
                        }
                        else if (pb <= pc) {
                            pr = b;
                        }
                        else {
                            pr = c;
                        }
                        recon[k++] = (byte) ((filt[j++] & 0xFF) + pr);
                    }
                }
            }
            else {
                throw new ImageDataException("invalid filter type {%d}", filterType);
            }
        }
        return recon;
    }
}

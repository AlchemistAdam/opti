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

final class FilterMethod_0 implements FilterMethod {

    private static final int TYPE_NONE = 0;
    private static final int TYPE_SUB = 1;
    private static final int TYPE_UP = 2;
    private static final int TYPE_AVERAGE = 3;
    private static final int TYPE_PAETH = 4;

    /**
     * DOC reconstruct
     *
     * @param bitDepth  the image bit depth
     * @param colorType the image color type
     * @param filt      the filtered bytes source
     * @param offset    index offset in the filt array
     * @param lines     the number of scanlines to reconstruct
     * @param nBytes    the number of bytes in a scanline (excluding the filter
     *                  type byte)
     * @return an array of reconstructed scanlines
     * @throws ImageDataException if {@code filt} contains an invalid filter
     *                            type byte
     */
    // TODO remove debug print calls
    @Override
    public byte[] reconstruct(int bitDepth, ColorType colorType, byte[] filt, int offset, int lines, int nBytes) throws ImageDataException {
        // destination for reconstructed bytes
        byte[] recon = new byte[filt.length - lines];

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
        System.out.println("sample offset: " + filterOffset);
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
        for (int i = 0, j = offset + 1, k = 0; i < lines; i++, j = i * (nBytes + 1) + 1, k = i * nBytes) {
            int filterTypeIndex = j - 1;
            // filter type preceding filtered bytes in scanline
            int filterType = filt[filterTypeIndex];

            if (filterType == TYPE_NONE) {
                System.out.println("filter type NONE");
                System.arraycopy(filt, j, recon, k, nBytes);
            }

            else if (filterType == TYPE_SUB) {
                System.out.println("filter type SUB");
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
                System.out.println("filter type UP");
                if (i == 0) {
                    System.arraycopy(filt, j, recon, k, nBytes);
                }
                else {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        recon[k] = (byte) ((filt[j] & 0xFF) + (recon[k - nBytes] & 0xFF));
                    }
                }
            }

            // TODO use recon array
            else if (filterType == TYPE_AVERAGE) {
                System.out.println("filter type AVERAGE");
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
                            recon[k] = (byte) (((filt[m] & 0xFF) + ((recon[k - filterOffset] & 0xFF) + (recon[k - nBytes] & 0xFF)) >>> 1));
                        }
                    }
                }
            }

            // https://www.w3.org/TR/png/#9Filter-type-4-Paeth
            else if (filterType == TYPE_PAETH) {
                System.out.println("filter type PAETH");
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
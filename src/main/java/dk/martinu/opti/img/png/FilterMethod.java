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
 * Interface to represent a filter method. This interface only declares methods
 * for reversing (reconstructing) the filter transformation.
 *
 * @author Adam Martinu
 * @see FilterMethod_0
 * @since 1.0
 */
interface FilterMethod {

    /**
     * Reconstructs the filtered image sample bytes in {@code filt} using the
     * specified parameters, starting at index {@code 0}.
     *
     * @param bitDepth  the image bit depth
     * @param colorType the image color type
     * @param filt      the filtered sample bytes
     * @param lines     the number of scanlines to reconstruct
     * @param nBytes    the number of sample bytes in a scanline
     * @return an array of reconstructed sample bytes (scanlines)
     * @throws ImageDataException if the sample bytes could not be reconstructed
     */
    default byte[] reconstruct(int bitDepth, ColorType colorType, byte[] filt, int lines, int nBytes) throws ImageDataException {
        return reconstruct(bitDepth, colorType, filt, 0, lines, nBytes);
    }

    /**
     * Reconstructs the filtered image sample bytes in {@code filt} using the
     * specified parameters, and starting at the specified {@code offset} index.
     *
     * @param bitDepth  the image bit depth
     * @param colorType the image color type
     * @param filt      the filtered sample bytes
     * @param offset    index offset in the {@code filt} array to start
     *                  reconstruction
     * @param lines     the number of scanlines to reconstruct
     * @param nBytes    the number of sample bytes in a scanline
     * @return an array of reconstructed sample bytes (scanlines)
     * @throws ImageDataException if the sample bytes could not be reconstructed
     */
    byte[] reconstruct(int bitDepth, ColorType colorType, byte[] filt, int offset, int lines, int nBytes) throws ImageDataException;
}

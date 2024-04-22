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
     * @param palette      the color palette, or {@code null}
     * @param transparency the transparent colors, or {@code null}
     * @param background   the background color, or {@code null}
     * @return an array of sample values of a single image, ordered left to
     * right, top to bottom
     * @throws ImageDataException if an error occurred when combining the
     *                            reduced images
     */
    byte[] getCombinedSamples(int width, int height, int bitDepth, ColorType colorType, FilterMethod filterMethod,
            byte[] filt, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException;
}

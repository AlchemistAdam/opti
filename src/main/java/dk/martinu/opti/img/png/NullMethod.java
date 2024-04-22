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
 * Implementation of an interlace method that can combine reduced images, based
 * on interlace method 0 in the PNG Specification, section
 * <a href="https://www.w3.org/TR/png/#8InterlaceMethods">8.1</a>.
 *
 * <b>NOTE:</b> this implementation always scales sample depths to bit depth 8
 * and pre-multiplies alpha if present.
 *
 * @author Adam Martinu
 * @since 1.0
 */
final class NullMethod implements InterlaceMethod {

    /**
     * Singleton instance.
     */
    static final NullMethod INSTANCE = new NullMethod();

    /**
     * Private constructor, use {@link #INSTANCE}.
     */
    private NullMethod() { }

    @Override
    public byte[] getCombinedSamples(int width, int height, int bitDepth, ColorType colorType, FilterMethod filterMethod,
            byte[] filt, byte[] palette, byte[] transparency, byte[] background) throws ImageDataException {

        // number of bytes in a scanline
        int nBytes = (int) Math.ceil(width * colorType.getComponentCount() * bitDepth / 8.0);
        // the filtered data contains a single reduced image; number of
        // scanlines is equal to height
        byte[] samples = filterMethod.reconstruct(bitDepth, colorType, filt, height, nBytes);
        ReducedImage img = new ReducedImage(width, height, samples);

        // pixel setter for reduced image samples
        PixelSetter setter = colorType.getPixelSetter(bitDepth, img, palette, transparency, background);

        // number of components for each pixel in destination array
        int components = colorType.usesTruecolor() ? 3 : 1;
        // destination array for PNG pixel samples
        byte[] dest = new byte[width * height * components];
        // index in dest for next pixel sample
        int index = 0;
        // iterate each scanline
        for (int y = 0; y < height; y++) {
            // TODO this can be optimized to a single method call.
            //  Implement new method in PixelSetter that sets all pixels in the
            //  entire scanline (can also be used in pass 7 by Adam7)
            // set pixel samples in dest from reduced image
            for (int x = 0; x < width; x++) {
                setter.setNext(dest, index);
                index += components;
            }
        }
        return dest;
    }
}

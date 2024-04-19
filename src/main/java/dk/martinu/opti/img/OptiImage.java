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
package dk.martinu.opti.img;

import dk.martinu.opti.img.spi.ImageDecoder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public abstract class OptiImage {

    public static final String BACKGROUND = "background";
    private static final ServiceLoader<ImageDecoder> readers = ServiceLoader.load(ImageDecoder.class);

    public static OptiImage from(Path path) throws IOException {
        Objects.requireNonNull(path, "path is null");
        // locate image reader provider
        final ServiceLoader.Provider<ImageDecoder> provider = readers
                .stream()
                .filter(p -> p.get().canDecode(path))
                .findFirst()
                .orElse(null);
        // read image file
        if (provider != null) {
            return provider.get().decode(path);
        }
        else {
            return null;
        }
    }

    /**
     * Horizontal size of the image in pixels. This is equal to the length of a
     * scanline.
     */
    public final int width;
    /**
     * Vertical size of the image in pixels. This is equal to the number of
     * scanlines.
     */
    public final int height;
    /**
     * Number of channels. This is equal to the number of samples per pixel.
     */
    public final int channels;
    /**
     * Sample depth in bits of each channel. This is equal to the size of each
     * sample.
     */
    public final int depth;
    // TODO store samples array in subclasses, e.g. ByteImage
    /**
     * The image samples. The length of the samples array is equal to:
     * <pre>
     *     width * height * channels * depth / 8
     * </pre>
     */
    public final byte[] data;
    public final Map<String, Object> metadata;

    public OptiImage(int width, int height, int channels, int depth) {
        if (width < 1) {
            throw new IllegalArgumentException("width is less than 1");
        }
        if (height < 1) {
            throw new IllegalArgumentException("height is less than 1");
        }
        if (channels < 1) {
            throw new IllegalArgumentException("channels is less than 1");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("depth is less than 1");
        }
        if (depth > 8) {
            throw new IllegalArgumentException("depth is greater than 8");
        }
        this.width    = width;
        this.height   = height;
        this.channels = channels;
        this.depth    = depth;

        // TODO this only works for bit depth := 8
        // required length of samples array
        int nBytes;
        if (depth == 8) {
            nBytes = width * height * channels;
        }
        else {
            // TODO change so each scanline starts at byte boundaries
            // number of bits used to store image samples
            int nBits = width * height * channels * depth;
            nBytes = nBits / 8;
            if (nBits % 8 != 0) {
                nBytes += 1;
            }
        }
        data     = new byte[nBytes];
        metadata = Map.of();
    }

    protected OptiImage(int width, int height, int channels, int depth, byte[] samples, Map<String, Object> metadata) {
        if (width < 1) {
            throw new IllegalArgumentException("width is less than 1");
        }
        if (height < 1) {
            throw new IllegalArgumentException("height is less than 1");
        }
        if (channels < 1) {
            throw new IllegalArgumentException("channels is less than 1");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("depth is less than 1");
        }
        if (depth > 8) {
            throw new IllegalArgumentException("depth is greater than 8");
        }
        this.width    = width;
        this.height   = height;
        this.channels = channels;
        this.depth    = depth;
        // TODO validate data length
        this.data     = samples;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public abstract OptiImage allocate();

    public abstract OptiImage allocate(int width, int height);

    public byte[] getPixel(int x, int y, byte[] pixel) {
        for (int i = 0; i < channels; i++) {
            pixel[i] = getSample(x, y, i);
        }
        return pixel;
    }

    public Object getProperty(String key) {
        return metadata.get(key);
    }

    public abstract byte getSample(int x, int y, int channel);

    public byte[] getSamples(int x, int y, int channel, byte[] dest) {
        // number of samples to copy into dest
        final int len = Math.min(dest.length, (width * height) - (x + y * width));
        // i: n-th sample
        for (int i = 0; i < len; i++) {
            if (x == width) {
                x = 0;
                y++;
            }
            dest[i] = getSample(x++, y, channel);
        }
        return dest;
    }

    public abstract void setSample(int x, int y, int channel, byte s);
}

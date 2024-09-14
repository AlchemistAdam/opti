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

// TODO remove support for arbitrary bit depth; Opti only uses bd8.
//  Essentially, OptiImage becomes ByteImage
public abstract class ByteImage {

    public static final String IMAGE_FORMAT = "imageFormat";
    public static final String BIT_DEPTH = "bitDepth";
    public static final String COMPOSITING_BACKGROUND = "compositingBackground";
    private static final ServiceLoader<ImageDecoder> readers = ServiceLoader.load(ImageDecoder.class);

    public static ByteImage from(Path path) throws IOException {
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
    // TODO store samples array in subclasses, e.g. ByteImage
    /**
     * The image samples. The length of the samples array is equal to:
     * <pre>
     *     width * height * channels
     * </pre>
     */
    public final byte[] samples;
    public final Map<String, Object> metadata;

    public ByteImage(int width, int height, int channels) {
        if (width < 1) {
            throw new IllegalArgumentException("width is less than 1");
        }
        if (height < 1) {
            throw new IllegalArgumentException("height is less than 1");
        }
        if (channels < 1) {
            throw new IllegalArgumentException("channels is less than 1");
        }
        this.width    = width;
        this.height   = height;
        this.channels = channels;

        samples  = new byte[width * height * channels];
        metadata = Map.of();
    }

    protected ByteImage(int width, int height, int channels, byte[] samples, Map<String, Object> metadata) {
        if (width < 1) {
            throw new IllegalArgumentException("width is less than 1");
        }
        if (height < 1) {
            throw new IllegalArgumentException("height is less than 1");
        }
        if (channels < 1) {
            throw new IllegalArgumentException("channels is less than 1");
        }
        this.width    = width;
        this.height   = height;
        this.channels = channels;

        // TODO validate data length
        this.samples  = samples;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public abstract ByteImage allocate();

    public abstract ByteImage allocate(int width, int height);

    public byte[] getPixel(int x, int y, byte[] pixel) {
        System.arraycopy(samples, (x + y * height) * channels, pixel, 0, channels);
        return pixel;
    }

    public Object getProperty(String key) {
        return metadata.get(key);
    }

    public byte getSample(int x, int y, int channel) {
        return samples[(x + y * width) * channels + channel];
    }

    public void getSamples(int y, int channel, int[] dest) {
        final int len = Math.min(dest.length, width);
        for (int i = 0; i < len; i++) {
            dest[i] = getSample(i, y, channel) & 0xFF;
        }
    }

    public void getSamples(int x, int y, int channel, byte[] dest) {
        // number of samples to copy into dest
        final int len = Math.min(dest.length, (width * height) - (x + y * width));
        // offset into samples buffer
        final int offset = (x + y * width) * channels + channel;
        // i: n-th sample
        for (int i = 0; i < len; i++) {
            dest[i] = samples[offset + i * channels];
        }
    }

    public void setSample(int x, int y, int channel, byte s) {
        samples[(x + y * width) * channels + channel] = s;
    }
}

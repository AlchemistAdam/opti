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

import java.util.Map;

public class ByteImage extends OptiImage {

    public ByteImage(int width, int height, int channels) {
        super(width, height, channels, 8);
    }

    protected ByteImage(int width, int height, int channels, byte[] samples, Map<String, Object> metadata) {
        super(width, height, channels, 8, samples, metadata);
    }

    @Override
    public byte[] getSamples(int x, int y, int channel, byte[] dest) {
        // number of samples to copy into dest
        final int len = Math.min(dest.length, (width * height) - (x + y * width));
        // offset into samples buffer
        final int offset = (x + y * width) * channels + channel;
        // i: n-th sample
        for (int i = 0; i < len; i++) {
            dest[i] = data[offset + i * channels];
        }
        return dest;
    }

    @Override
    public OptiImage allocate() {
        return new ByteImage(width, height, channels);
    }

    @Override
    public OptiImage allocate(int width, int height) {
        return new ByteImage(width, height, channels);
    }

    @Override
    public byte getSample(int x, int y, int channel) {
        return data[(x + y * width) * channels + channel];
    }

    @Override
    public byte[] getPixel(int x, int y, byte[] pixel) {
        System.arraycopy(data, (x + y * height) * channels, pixel, 0, channels);
        return pixel;
    }

    @Override
    public void setSample(int x, int y, int channel, byte s) {
        data[(x + y * width) * channels + channel] = s;
    }
}

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

public class GrayscaleImage extends ByteImage {

    public GrayscaleImage(int width, int height) {
        super(width, height, 1);
    }

    public GrayscaleImage(int width, int height, byte[] samples, Map<String, Object> metadata) {
        super(width, height, 1, samples, metadata);
    }

    @Override
    public OptiImage allocate() {
        return new GrayscaleImage(width, height);
    }

    @Override
    public OptiImage allocate(int width, int height) {
        return new GrayscaleImage(width, height);
    }

    @Override
    public byte[] getPixel(int x, int y, byte[] pixel) {
        pixel[0] = data[x + y * width];
        return pixel;
    }

    @Override
    public byte getSample(int x, int y, int channel) {
        return data[x + y * width];
    }

    @Override
    public byte[] getSamples(int x, int y, int channel, byte[] dest) {
        // number of samples to copy into dest
        final int len = Math.min(dest.length, (width * height) - (x + y * width));
        System.arraycopy(data, x + y * width, dest, 0, len);
        return dest;
    }

    @Override
    public void setSample(int x, int y, int channel, byte s) {
        data[x + y * width] = s;
    }
}

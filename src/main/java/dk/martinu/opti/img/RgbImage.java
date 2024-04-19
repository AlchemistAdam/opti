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

public class RgbImage extends ByteImage {

    public RgbImage(int width, int height) {
        super(width, height, 3);
    }

    public RgbImage(int width, int height, byte[] samples, Map<String, Object> metadata) {
        super(width, height, 3, samples, metadata);
    }

    @Override
    public OptiImage allocate(int width, int height) {
        return new RgbImage(width, height);
    }

    @Override
    public OptiImage allocate() {
        return new RgbImage(width, height);
    }

    @Override
    public byte[] getPixel(int x, int y, byte[] pixel) {
        final int offset = (x + y * height) * 3;
        pixel[0] = data[offset];
        pixel[1] = data[offset + 1];
        pixel[2] = data[offset + 2];
        return pixel;
    }
}

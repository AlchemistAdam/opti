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

/**
 * Representation of a reduced image that constitutes an interleaved image.
 *
 * @author Adam Martinu
 * @see InterlaceMethod
 * @since 1.0
 */
class ReducedImage {

    final int width;
    final int height;
    final byte[] samples;

    /**
     * Constructs a new reduced image.
     *
     * @param width   the width of the image
     * @param height  the height of the image
     * @param samples byte array of samples
     */
    public ReducedImage(int width, int height, byte[] samples) {
        this.width = width;
        this.height = height;
        this.samples = samples;
    }
}

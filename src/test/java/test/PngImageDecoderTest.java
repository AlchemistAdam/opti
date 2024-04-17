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
package test;

import dk.martinu.opti.img.OptiImage;
import dk.martinu.opti.img.png.PngImageDecoder;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for decoding PNG images with the Opti PNG decoder. Images are
 * decoded with Java Image IO (IIO) for comparison.
 * <p>
 * The PNG image files from
 * <a href="http://www.schaik.com/pngsuite/pngsuite.html">PngSuite</a> are used
 * for testing the decoder. The only modification is that related images are
 * nested in discrete subdirectories, such that grouping tests is easier and
 * that groups can be tested in isolation.
 */
@DisplayName("PNG Decoder")
public class PngImageDecoderTest {

    /**
     * Relative root path for PNG image test files.
     */
    static final String ROOT = "res/png";

    /**
     * Decodes the specified PNG image file with Java Image IO (IIO) and returns
     * the decoded image object.
     *
     * @param file the file to read from
     * @return the decoded image object
     */
    private static BufferedImage decodeWithIIO(File file) {
        try {
            return ImageIO.read(file);
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Decodes the specified PNG image file with Opti PNG decoder and returns
     * the decoded image object.
     *
     * @param filePath the path of the image file to read from
     * @return the decoded image object
     */
    private static OptiImage decodeWithOpti(Path filePath) {
        OptiImage[] decode = new OptiImage[1];
        assertDoesNotThrow(() -> {
            decode[0] = PngImageDecoder.provider().decode(filePath);
        });
        return decode[0];
    }

    /**
     * Fills the specified samples array with samples from the specified image
     * channel, converting the samples to bit-depth 8 if necessary. This method
     * accounts for indexed color samples and alpha.
     *
     * @param img     the image
     * @param channel the channel (band) to get samples from
     * @param samples the array to store sample values in
     */
    private static void getIIOSamples(BufferedImage img, int channel, byte[] samples) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] samplesIIO = new int[samples.length];
        Raster raster = img.getRaster();

        // indexed / palette
        if (img.getColorModel() instanceof IndexColorModel icm) {
            // number of bits to shift sample values
            int shift = Math.max(0, icm.getComponentSize(channel) - 8);
            raster.getPixels(0, 0, width, height, samplesIIO);
            // lambda to get sample value for a specified pixel
            IntUnaryOperator sampleOp = switch (channel) {
                case 0 -> icm::getRed;
                case 1 -> icm::getGreen;
                case 2 -> icm::getBlue;
                default -> throw new RuntimeException();
            };
            // no alpha - copy sample values
            if (!icm.hasAlpha()) {
                for (int i = 0; i < samplesIIO.length; i++) {
                    samples[i] = (byte) (sampleOp.applyAsInt(samplesIIO[i]) >>> shift);
                }
            }
            // alpha is present - sample values need to be composited to match
            // Opti representation
            else {
                // number of bits to shift alpha values
                int alphaShift = Math.max(0, icm.getComponentSize(3) - 8);
                for (int i = 0; i < samplesIIO.length; i++) {
                    int alpha = icm.getAlpha(samplesIIO[i]) >>> alphaShift;
                    if (alpha == 255) {
                        samples[i] = (byte) sampleOp.applyAsInt(samplesIIO[i] >>> shift);
                    }
                    else if (alpha == 0) {
                        samples[i] = (byte) 0xFF;
                    }
                    else {
                        float a = alpha / 255.0F;
                        samples[i] = (byte) (a * sampleOp.applyAsInt(samplesIIO[i] >>> shift) + (1 - a) * 0xFF);
                    }
                }
            }
        }

        // not indexed / truecolor or grayscale
        else {
            // number of bits to shift sample values
            int shift = Math.max(0, raster.getSampleModel().getSampleSize(channel) - 8);
            raster.getSamples(0, 0, width, height, channel, samplesIIO);
            // no alpha - copy sample values
            if (img.getTransparency() == Transparency.OPAQUE) {
                for (int i = 0; i < samplesIIO.length; i++) {
                    samples[i] = (byte) (samplesIIO[i] >>> shift);
                }
            }
            // alpha is present - sample values need to be composited to match
            // Opti representation
            else {
                int alphaChannel = raster.getSampleModel().getNumBands() > 3 ? 3 : 1;
                // number of bits to shift alpha values
                int alphaShift = Math.max(0, raster.getSampleModel().getSampleSize(alphaChannel) - 8);
                int[] alphaIIO = new int[samplesIIO.length];
                raster.getSamples(0, 0, width, height, alphaChannel, alphaIIO);
                for (int i = 0; i < samplesIIO.length; i++) {
                    int alpha = alphaIIO[i] >>> alphaShift;
                    if (alpha == 255) {
                        samples[i] = (byte) (samplesIIO[i] >>> shift);
                    }
                    else if (alpha == 0) {
                        samples[i] = (byte) 0xFF;
                    }
                    else {
                        float a = alpha / 255.0F;
                        samples[i] = (byte) (a * (samplesIIO[i] >>> shift) + (1 - a) * 0xFF);
                    }
                }
            }
        }
    }

    /**
     * Test factory that creates a stream of tests for all PNG files in the
     * {@code /basic} subdirectory.
     *
     * @return a stream of dynamic tests
     */
    @TestFactory
    Stream<DynamicTest> pngBasic() {
        Path dir = Paths.get(ROOT + "/basic");
        return createTestsFromDir(dir);
    }

    /**
     * Utility method for test factories. Constructs a dynamic
     * {@link #decodeAndCompare(Path)} test for each PNG file in the specified
     * directory and returns them in a stream.
     *
     * @param dir the directory containing the PNG test files
     * @return a stream of dynamic tests
     */
    private Stream<DynamicTest> createTestsFromDir(Path dir) {
        if (!Files.isDirectory(dir)) {
            throw new RuntimeException("directory {" + dir + "} does not exist");
        }
        Path[] files;
        try {
            //noinspection resource
            files = Files.list(dir)
                    .filter(p -> p.getFileName().toString().endsWith(".png"))
                    .toArray(Path[]::new);
        }
        catch (IOException e) {
            throw new RuntimeException("could not open directory {" + dir + "}", e);
        }
        if (files.length != 0) {
            return Stream.of(files).map(filePath -> DynamicTest.dynamicTest(
                    filePath.getFileName().toString(), () -> decodeAndCompare(filePath)));
        }
        else {
            throw new RuntimeException("directory {" + dir + "} does not contain any PNG files");
        }
    }

    /**
     * Given the file path to a PNG image, decodes the image file with Opti and
     * IIO, and compares the binary representation of the image data from both
     * decoders.
     *
     * @param filePath file path to a PNG image file
     */
    private void decodeAndCompare(Path filePath) {
        OptiImage img = decodeWithOpti(filePath);
        BufferedImage bImg = decodeWithIIO(filePath.toFile());

        int width = img.width;
        int height = img.height;
        // compare image size
        assertEquals(bImg.getWidth(), width);
        assertEquals(bImg.getHeight(), height);

        // maximum allowed difference between sample values
        final int MAX_DIFF = 1;

        byte[] samplesOpti = new byte[width * height];
        byte[] samplesIIO = new byte[width * height];

        for (int i = 0; i < img.channels; i++) {
            img.getSamples(0, 0, i, samplesOpti);
            getIIOSamples(bImg, i, samplesIIO);
            for (int k = 0; k < samplesOpti.length; k++) {
                final int channel = i;
                final int pixel = k;
                final int expected = samplesIIO[pixel] & 0xFF;
                final int actual = samplesOpti[pixel] & 0xFF;
                int diff = Math.abs(expected - actual);
                assertTrue(
                        diff <= MAX_DIFF,
                        () -> String.format("wrong sample value in channel %d for pixel %d, expected %d but was %d",
                                channel, pixel, expected, actual));
            }
        }
    }
}

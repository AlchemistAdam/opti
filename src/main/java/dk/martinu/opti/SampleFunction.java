package dk.martinu.opti;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

/**
 * A function that reads pixels, stores their blue, green and red sample
 * values, and returns how many data elements that were read.
 * <p>
 * This interface defines sample function implementations for almost all types
 * of images (see {@link BufferedImage#getType()}). The sample functions are
 * listed below:
 * <ul>
 *     <li>{@code sampleFunctionByteABGR}</li>
 *     <li>{@code sampleFunctionByteABGR_Pre}</li>
 *     <li>{@code sampleFunctionByteBGR}</li>
 *     <br>
 *     <li>{@code sampleFunctionIntARGB}</li>
 *     <li>{@code sampleFunctionIntARGB_Pre}</li>
 *     <li>{@code sampleFunctionIntBGR}</li>
 *     <li>{@code sampleFunctionIntRGB}</li>
 * </ul>
 *
 * @param <T> the runtime type of the data buffer array.
 * @author Adam Martinu
 * @see DataBuffer
 */
// TODO also implement sample functions for float, double and short data elements
// TODO sample functions could very well benefit from parallelism for large images
@FunctionalInterface
interface SampleFunction<T> {

    /**
     * Sample function for images of type
     * {@link BufferedImage#TYPE_4BYTE_ABGR}. Sample values are multiplied with
     * alpha before they are stored.
     */
    SampleFunctionByte sampleFunctionByteABGR = (buffer, offset, n, samples) -> {
        // number of data elements to read
        final int count = n * 4;
        // total length to read into buffer
        final int len = count + offset;
        // d: data buffer array index
        // s: sample array index
        // a: alpha
        for (int d = offset, s = 0, a; d < len; d += 4, s += 3) {
            a = (int) buffer[d] & 0xFF; // alpha
            if (a != 0) {
                // pixel is opaque - no need to multiply
                if (a == 255) {
                    samples[s] = (int) buffer[d + 1] & 0xFF;     // blue
                    samples[s + 1] = (int) buffer[d + 2] & 0xFF; // green
                    samples[s + 2] = (int) buffer[d + 3] & 0xFF; // red
                }
                // pixel is transparent - multiply with alpha
                else {
                    samples[s] = ((int) buffer[d + 1] & 0xFF) * a / 255;     // blue
                    samples[s + 1] = ((int) buffer[d + 2] & 0xFF) * a / 255; // green
                    samples[s + 2] = ((int) buffer[d + 3] & 0xFF) * a / 255; // red
                }
            }
            else
                samples[s] = samples[s + 1] = samples[s + 2] = 0;
        }
        return count;
    };

    /**
     * Sample function for images of type
     * {@link BufferedImage#TYPE_4BYTE_ABGR_PRE}. Sample values are stored
     * as-is.
     */
    // TEST
    SampleFunctionByte sampleFunctionByteABGR_Pre = (buffer, offset, n, samples) -> {
        // number of data elements to read
        final int count = n * 4;
        // total length to read into buffer
        final int len = count + offset;
        // d: data buffer array index
        // s: sample array index
        for (int d = offset, s = 0; d < len; d += 4, s += 3) {
            samples[s] = (int) buffer[d + 1] & 0xFF;     // blue
            samples[s + 1] = (int) buffer[d + 2] & 0xFF; // green
            samples[s + 2] = (int) buffer[d + 3] & 0xFF; // red
        }
        return count;
    };

    /**
     * Sample function for images of type {@link BufferedImage#TYPE_3BYTE_BGR}.
     * Sample values are stored as-is.
     */
    SampleFunctionByte sampleFunctionByteBGR = (buffer, offset, n, samples) -> {
        // number of data elements to read
        final int count = n * 3;
        // total length to read into buffer
        final int len = count + offset;
        // d: data buffer array index
        // s: sample array index
        for (int d = offset, s = 0; d < len; d += 3, s += 3) {
            samples[s] = (int) buffer[d] & 0xFF;         // blue
            samples[s + 1] = (int) buffer[d + 1] & 0xFF; // green
            samples[s + 2] = (int) buffer[d + 2] & 0xFF; // red
        }
        return count;
    };

    /**
     * Sample function for images of type {@link BufferedImage#TYPE_INT_ARGB}.
     * Sample values are multiplied with alpha before they are stored.
     */
    // TEST
    SampleFunctionInt sampleFunctionIntARGB = (buffer, offset, n, samples) -> {
        // total length to read into buffer
        final int len = n + offset;
        // d: data buffer array index
        // s: sample array index
        // p: pixel
        // a: alpha
        for (int d = offset, s = 0, p, a; d < len; d++, s += 3) {
            p = buffer[d];
            a = p >> 24 & 0xFF; // alpha
            if (a != 0) {
                // pixel is opaque - no need to multiply
                if (a == 255) {
                    samples[s] = p & 0xFF;           // blue
                    samples[s + 1] = p >> 8 & 0xFF;  // green
                    samples[s + 2] = p >> 16 & 0xFF; // red
                }
                // pixel is transparent - multiply with alpha
                else {
                    samples[s] = (p & 0xFF) * a / 255;           // blue
                    samples[s + 1] = (p >> 8 & 0xFF) * a / 255;  // green
                    samples[s + 2] = (p >> 16 & 0xFF) * a / 255; // red
                }
            }
            else
                samples[s] = samples[s + 1] = samples[s + 2] = 0;
        }
        return n;
    };

    /**
     * Sample function for images of type
     * {@link BufferedImage#TYPE_INT_ARGB_PRE}. Sample values are stored as-is.
     */
    // TEST
    SampleFunctionInt sampleFunctionIntARGB_Pre = (buffer, offset, n, samples) -> {
        // total length to read into buffer
        final int len = n + offset;
        // d: buffer array index
        // s: sample array index
        // p: pixel
        for (int d = offset, s = 0, p; d < len; d++, s += 3) {
            p = buffer[d];
            samples[s] = p & 0xFF;           // blue
            samples[s + 1] = p >> 8 & 0xFF;  // green
            samples[s + 2] = p >> 16 & 0xFF; // red
        }
        return n;
    };

    /**
     * Sample function for images of type {@link BufferedImage#TYPE_INT_BGR}.
     * Sample values are stored as-is.
     */
    // TEST
    SampleFunctionInt sampleFunctionIntBGR = (buffer, offset, n, samples) -> {
        // total length to read into buffer
        final int len = n + offset;
        // d: buffer array index
        // s: sample array index
        // p: pixel
        for (int d = offset, s = 0, p; d < len; d++, s += 3) {
            p = buffer[d];
            samples[s] = p >> 16 & 0xFF;    // blue
            samples[s + 1] = p >> 8 & 0xFF; // green
            samples[s + 2] = p & 0xFF;      // red
        }
        return n;
    };

    /**
     * Sample function for images of type {@link BufferedImage#TYPE_INT_RGB}.
     * Sample values are stored as-is.
     */
    // TEST
    SampleFunctionInt sampleFunctionIntRGB = (buffer, offset, n, samples) -> {
        // total length to read into buffer
        final int len = n + offset;
        // d: buffer array index
        // s: sample array index
        // p: pixel
        for (int d = offset, s = 0, p; d < len; d++, s += 3) {
            p = buffer[d];
            samples[s] = p & 0xFF;           // blue
            samples[s + 1] = p >> 8 & 0xFF;  // green
            samples[s + 2] = p >> 16 & 0xFF; // red
        }
        return n;
    };

    /**
     * Reads {@code n} amount of pixels from the data buffer array,
     * starting at the specified offset, and returns the number of data
     * elements read. The blue, green and red sample values of each pixel
     * are stored the specified sample array.
     *
     * @param buffer  the data buffer array to read from
     * @param offset  the starting offset
     * @param n       the number of pixels to read
     * @param samples pre-allocated array to store sample values in
     * @return the number of data elements that were read from the buffer
     */
    int getSamples(final T buffer, final int offset, final int n, final int[] samples);

}

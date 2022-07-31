package dk.martinu.opti;

import java.awt.image.*;
import java.util.Objects;

import static dk.martinu.opti.SampleFunction.*;

/**
 * A handle to a data buffer and a sample function, which can read pixels
 * and store their blue, green and red sample values.
 * <p>
 * To obtain an instance of this class, use one of the static factory
 * methods.
 *
 * @param <T> the runtime type of the data buffer array.
 * @author Adam Martinu
 * @see #newByteHandle(BufferedImage)
 * @see #newIntHandle(BufferedImage)
 * @see SampleFunction
 */
public class DataHandle<T> {

    /**
     * Constructs a new byte data handle to read the specified image.
     *
     * @param image the image whose data buffer to read from
     * @throws NullPointerException     if {@code image} is {@code null}
     * @throws IllegalArgumentException if {@code image} is not a byte type
     *                                  or the data buffer is not an
     *                                  instance of {@code DataBufferByte}
     */
    public static DataHandle<byte[]> newByteHandle(final BufferedImage image) {
        Objects.requireNonNull(image, "image is null");
        final SampleFunction<byte[]> sampleFunction = switch (image.getType()) {
            case BufferedImage.TYPE_4BYTE_ABGR -> sampleFunctionByteABGR;
            case BufferedImage.TYPE_4BYTE_ABGR_PRE -> sampleFunctionByteABGR_Pre;
            case BufferedImage.TYPE_3BYTE_BGR -> sampleFunctionByteBGR;
            default -> throw new IllegalArgumentException("incompatible image type");
        };
        if (image.getData().getDataBuffer() instanceof DataBufferByte buffer)
            return new DataHandle<>(buffer.getData(), sampleFunction, buffer.getOffset());
        else
            throw new IllegalArgumentException("incompatible data buffer");
    }

    /**
     * Constructs a new int data handle to read the specified image.
     *
     * @param image the image whose data buffer to read from
     * @throws NullPointerException     if {@code image} is {@code null}
     * @throws IllegalArgumentException if {@code image} is not an int type
     *                                  or the data buffer is not an
     *                                  instance of {@code DataBufferInt}
     */
    public static DataHandle<int[]> newIntHandle(final BufferedImage image) {
        Objects.requireNonNull(image, "image is null");
        final SampleFunction<int[]> sampleFunction = switch (image.getType()) {
            case BufferedImage.TYPE_INT_ARGB -> sampleFunctionIntARGB;
            case BufferedImage.TYPE_INT_ARGB_PRE -> sampleFunctionIntARGB_Pre;
            case BufferedImage.TYPE_INT_RGB -> sampleFunctionIntRGB;
            case BufferedImage.TYPE_INT_BGR -> sampleFunctionIntBGR;
            default -> throw new IllegalArgumentException("incompatible image type");
        };
        if (image.getData().getDataBuffer() instanceof DataBufferInt buffer)
            return new DataHandle<>(buffer.getData(), sampleFunction, buffer.getOffset());
        else
            throw new IllegalArgumentException("incompatible data buffer");
    }

    /**
     * The data buffer array.
     */
    public final T data;
    /**
     * The sample function used to get sample values from {@link #data}.
     */
    public final SampleFunction<T> sampleFunction;
    /**
     * The current offset into {@link #data}.
     */
    public int offset;

    /**
     * Constructs a new data handle with the specified data buffer array,
     * sample function and offset.
     */
    protected DataHandle(final T data, final SampleFunction<T> sampleFunction, final int offset) {
        this.data = data;
        this.sampleFunction = sampleFunction;
        this.offset = offset;
    }

    /**
     * Reads {@code n} amount of pixels from the data buffer and stores
     * their blue, green and red sample values in the specified sample
     * array.
     *
     * @param n       the number of pixels to read
     * @param samples pre-allocated array to store sample values in
     */
    public void getSamples(final int n, final int[] samples) {
        offset += sampleFunction.getSamples(data, offset, n, samples);
    }
}

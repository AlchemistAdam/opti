package dk.martinu.opti.img.png;

import java.util.Objects;

/**
 * Wrapper for packed pixel setters that are <i>inconstant</i>. After each call
 * to {@link #setNext(byte[], int) setNext}, this pixel setter checks if all
 * pixels from the current scanline have been set. If so, then the position of
 * the wrapped setter is reset and its sample byte index is incremented.
 * <p>
 * A packed pixel setter is inconstant if the number of pixels set from the last
 * sample byte differs from previous bytes, i.e. the last sample byte has unused
 * bits. This happens when setting pixels from a reduced image if the image
 * width is not divisible by the number of pixels per byte (8 divided by bit
 * depth).
 * <p>
 * For example, a packed pixel setter for an image with width 7 and bit depth 2
 * is inconstant. Each byte of a scanline contains 4 pixels, thus the last 2
 * bits of the 2nd byte in each scanline in the image are unused.
 * <p>
 * <b>NOTE:</b> this class has no way of knowing if the wrapped setter is
 * actually inconstant, it is up to the caller to ensure that only inconstant
 * setters are wrapped.
 *
 * @author Adam Martinu
 * @see #wrapIfInconstant(int, int, PackedPixelSetter)
 * @since 1.0
 */
final class InconstantPixelSetter implements PixelSetter {

    /**
     * Returns the specified setter wrapped in an {@link InconstantPixelSetter}
     * instance if it is inconstant, otherwise returns {@code setter}.
     *
     * @param bitDepth the image bit depth used by {@code setter}
     * @param width    the image width used by {@code setter}
     * @param setter   the packed pixel setter
     * @return a wrapped pixel setter if inconstant, otherwise {@code setter}
     */
    static PixelSetter wrapIfInconstant(int bitDepth, int width, PackedPixelSetter setter) {
        return width % (8 / bitDepth) == 0 ?
                setter :
                new InconstantPixelSetter(setter);
    }

    /**
     * The packed pixel setter that is wrapped.
     */
    private final PackedPixelSetter setter;
    /**
     * Pixel index in scanline.
     */
    private int k = 0;

    /**
     * Creates a new {@link InconstantPixelSetter} that wraps the specified
     * setter.
     *
     * @throws NullPointerException if {@code setter} is {@code null}.
     * @see #wrapIfInconstant(int, int, PackedPixelSetter)
     */
    InconstantPixelSetter(PackedPixelSetter setter) {
        this.setter = Objects.requireNonNull(setter, "setter is null");
    }

    /**
     * Calls {@link PixelSetter#setNext(byte[], int) setNext} on the wrapped
     * setter, and resets its position and increments its sample byte index if
     * all pixels from the current scanline have been set.
     */
    @Override
    public void setNext(byte[] dest, int index) {
        setter.setNext(dest, index);
        // skip remaining bits if all pixels from current scanline have been set
        if (++k >= setter.width) {
            setter.position = 0;
            setter.i++;
            k = 0;
        }
    }
}

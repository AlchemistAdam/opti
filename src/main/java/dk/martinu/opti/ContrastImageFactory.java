package dk.martinu.opti;

import java.awt.image.*;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import static java.lang.Math.abs;

/**
 * Utility class for creating a contrast image from a source image.
 *
 * @author Adam Martinu
 */
public class ContrastImageFactory {

    /**
     * The band index for horizontal contrast values. Corresponds to the red
     * color component in a BGR component color model.
     */
    public static final int HORIZONTAL_CONTRAST_BAND = 0;
    /**
     * The band index for vertical contrast values. Corresponds to the green
     * color component in a BGR component color model.
     */
    public static final int VERTICAL_CONTRAST_BAND = 1;

    public static BufferedImage getContrast(final BufferedImage source) {
        Objects.requireNonNull(source, "source is null");

        // TODO currently uses linear scaling for contrast values (y = x/3)
        //  maybe a logarithmic function can give better results? (y = -131 + 58.1 * ln(x))

        // data handle to get sample values from source
        final DataHandle<?> handle = switch (source.getSampleModel().getDataType()) {
            case DataBuffer.TYPE_BYTE -> DataHandle.newByteHandle(source);
            case DataBuffer.TYPE_INT -> DataHandle.newIntHandle(source);
            default -> throw new IllegalArgumentException("unsupported data type {"
                    + source.getSampleModel().getDataType() + "}");
        };

        // source image size
        final int width = source.getWidth();
        final int height = source.getHeight();
        // pre-allocated and reusable BGR sample arrays
        final int[] s0 = new int[3 * width];
        final int[] s1 = new int[3 * width];

        // operator for computing the horizontal contrast of two pixels in s0
        // @formatter:off
        final IntUnaryOperator horizontal = x -> abs(s0[x - 3] - s0[x])
                                               + abs(s0[x - 2] - s0[x + 1])
                                               + abs(s0[x - 1] - s0[x + 2]);
        // @formatter:on

        // operator for computing the vertical contrast of two pixels in s1 and s0
        // @formatter:off
        final IntUnaryOperator vertical = x -> abs(s0[x]     - s1[x])
                                             + abs(s0[x + 1] - s1[x + 1])
                                             + abs(s0[x + 2] - s1[x + 2]);
        // @formatter:on

        // TODO edge detection does not require the image itself, only data
        //  maybe create data array instead, and only construct image with data array when requested
        //  this will also remove calls to WritableRaster.setSample
        // contrast image to return
        final BufferedImage contrast = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        final WritableRaster raster = contrast.getRaster();

        // get sample data of first row
        handle.getSamples(width, s0);

        // iterate pixels in first row
        for (int i = 3, x = 0; i < s0.length; i += 3, x++) {
            // store horizontal contrast
            raster.setSample(x, 0, HORIZONTAL_CONTRAST_BAND, horizontal.applyAsInt(i) / 3);
        }

        // iterate remaining rows
        for (int y = 1; y < height; y++) {

            // copy s0 into s1, so s0 can hold new sample data
            System.arraycopy(s0, 0, s1, 0, s0.length);

            // get sample data of next row
            handle.getSamples(width, s0);

            // store vertical contrast of first pixel (x=0)
            raster.setSample(0, y, VERTICAL_CONTRAST_BAND, vertical.applyAsInt(0) / 3);

            // iterate pixels in row
            for (int i = 3, x = 1; i < s0.length; i += 3, x++) {

                // store vertical contrast value
                raster.setSample(x, y, VERTICAL_CONTRAST_BAND, vertical.applyAsInt(i) / 3);

                // store horizontal contrast value
                raster.setSample(x - 1, y, HORIZONTAL_CONTRAST_BAND, horizontal.applyAsInt(i) / 3);
            }
        }

        // return contrast image
        return contrast;
    }

}

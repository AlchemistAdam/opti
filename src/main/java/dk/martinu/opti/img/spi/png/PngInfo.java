package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.*;
import dk.martinu.opti.img.spi.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static dk.martinu.opti.Util.getInt;
import static dk.martinu.opti.img.spi.png.ChunkType.*;

public class PngInfo {

    /**
     * Constant for compression method 0 (deflate).
     */
    public static final int COMPRESSION_DEFLATE = 0;
    /**
     * Constant for filter method 0, as it is represented in a PNG samples
     * stream.
     */
    public static final int FILTER_METHOD_0 = 0;

    /**
     * Constant for interlace method 0 (null), as it is represented in a PNG
     * samples stream.
     */
    public static final int INTERLACE_METHOD_0 = 0;
    /**
     * Constant for interlace method 1 (Adam7), as it is represented in a PNG
     * samples stream.
     */
    public static final int INTERLACE_METHOD_1 = 1;
    public static final int BIT_DEPTH_1 = 1;
    public static final int BIT_DEPTH_2 = 2;
    public static final int BIT_DEPTH_4 = 4;
    public static final int BIT_DEPTH_8 = 8;
    public static final int BIT_DEPTH_16 = 16;

    private static final int IHDR_LENGTH = 13;

    /* IHDR chunk fields */
    final int width;
    final int height;
    final int bitDepth;
    final ColorType colorType;
//     remove unless used by decoders
    final byte compressionMethod;
    final FilterMethod filterMethod;
    final InterlaceMethod interlaceMethod;

    /* image samples */

    protected byte[] palette = null;
    /**
     * 8-bit alpha samples for INDEXED, otherwise color with 16-bit samples to
     * make transparent.
     */
    protected byte[] transparency = null;
    /**
     * Background color with 16-bit samples.
     * <p>
     * See <a href="https://www.w3.org/TR/png/#11bKGD">bKGD</a> for details.
     */
    protected byte[] background = null;
    /**
     * Buffer for storing IDAT byte arrays as they are read.
     */
    protected ByteArrayBuffer idatBuffer = new ByteArrayBuffer();
    /**
     * {@code true} if {@link #idatBuffer} is closed, otherwise {@code false}.
     * Set to {@code true} when another chunk has been read after one or more
     * IDAT chunks have already been read.
     */
    protected boolean isIdatBufferClosed = false;

    public PngInfo(Chunk chunk) throws ImageFormatException, ImageDataException {
        Objects.requireNonNull(chunk, "chunk is null");
        if (chunk.type() != IHDR) {
            throw new ImageFormatException("missing IHDR chunk");
        }
        if (chunk.data().length != IHDR_LENGTH) {
            throw new ImageDataException("invalid IHDR chunk");
        }

        width = getInt(chunk.data());
        if (width < 1) {
            throw new ImageDataException("invalid image width {%d}", width);
        }

        height = getInt(chunk.data(), 4);
        if (height < 1) {
            throw new ImageDataException("invalid image height {%d}", height);
        }

        bitDepth = getBitDepth(chunk.data()[8]);
        colorType = ColorType.get(chunk.data()[9]);
        colorType.validateBitDepth(bitDepth);

        compressionMethod = chunk.data()[10];
        if (compressionMethod != COMPRESSION_DEFLATE) {
            throw new ImageDataException("invalid compression method {%d}", compressionMethod);
        }

        filterMethod = getFilterMethod(chunk.data()[11]);
        interlaceMethod = getInterlaceMethod(chunk.data()[12]);
    }

    public OptiImage createImage() throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType.usesPalette() && palette == null) {
            throw new ImageFormatException("missing PLTE chunk");
        }
        if (idatBuffer.isEmpty()) {
            throw new ImageFormatException("missing IDAT chunks");
        }

        // FIXME IDAT lengths are not validated and can contain any amount of samples
        //  both too much and too little

        // prepare background and palette for images with alpha
        byte[] bkgd = getCompositingBackground();
        byte[] plte = getPremultipliedPalette(bkgd);
        // image samples used by return value
        byte[] samples = interlaceMethod.getPngSamples(width, height, bitDepth, colorType,
                filterMethod, getFilteredData(), plte, transparency, bkgd);

        // return value
        final OptiImage img;
        // TODO copy pngSamples into image instance
        if (colorType.usesTruecolor() || colorType.usesPalette()) {
            img = new RgbImage(width, height, samples);
        }
        else {
            img = new GrayscaleImage(width, height, samples);
        }
        return img;
    }

    public void update(Chunk chunk) throws ImageFormatException, ImageDataException {
        Objects.requireNonNull(chunk, "chunk is null");

        // critical chunks
        if (chunk.isCritical()) {
            switch (chunk.type()) {
                case PLTE -> update_PLTE(chunk);
                case IDAT -> update_IDAT(chunk);
                default -> throw new ImageDataException(
                        "unknown critical chunk type %s", ChunkType.toString(chunk.type()));
            }
        }

        // ancillary chunks
        else {
            // ignore private chunks and chunks that do not conform to PNG spec
            if (chunk.isPrivate()) {
                return; // TODO log
            }
            if (chunk.isReserved()) {
                return; // TODO log
            }

            switch (chunk.type()) {
                /* chunks that are interpreted */
                case tRNS -> update_tRNS(chunk);
                case bKGD -> update_bKGD(chunk);

                /* ignored chunks (ordering is still enforced) */
                /* https://www.w3.org/TR/png/#5ChunkOrdering   */
                case tEXt, zTXt, iTXt, pHYs, tIME -> {
                    if (!idatBuffer.isEmpty()) {
                        isIdatBufferClosed = true;
                    }
                    // TODO log
                }
                case hIST -> {
                    if (palette == null) {
                        throw new ImageFormatException("PLTE chunk must precede hIST chunk");
                    }
                    if (!idatBuffer.isEmpty()) {
                        throw new ImageFormatException("hIST chunk must precede IDAT chunks");
                    }
                }
                case sPLT, eXIf -> {
                    if (!idatBuffer.isEmpty()) {
                        throw new ImageFormatException(
                                "%s chunk must precede IDAT chunks", ChunkType.toString(chunk.type()));
                    }
                    // TODO log
                }
                // color space chunks
                case cHRM, gAMA, iCCP, sBIT, sRGB, cICP, mDCv, cLLi -> {
                    if (palette != null) {
                        throw new ImageFormatException(
                                "%s chunk must precede PLTE chunk", ChunkType.toString(chunk.type()));
                    }
                    if (!idatBuffer.isEmpty()) {
                        throw new ImageFormatException(
                                "%s chunk must precede IDAT chunks", ChunkType.toString(chunk.type()));
                    }
                    // TODO log
                }
                // unknown ancillary chunks
                default -> {
                    if (!idatBuffer.isEmpty()) {
                        isIdatBufferClosed = true;
                    }
                    // TODO log
                }
            }
        }
    }

    protected void update_IDAT(Chunk chunk) throws ImageFormatException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (isIdatBufferClosed) {
            throw new ImageFormatException("IDAT chunks must be consecutive");
        }

        idatBuffer.add(chunk.data());
    }

    protected void update_PLTE(Chunk chunk) throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (palette != null) {
            throw new ImageFormatException("image contains multiple PLTE chunks");
        }
        if (transparency != null) {
            throw new ImageFormatException("PLTE chunk must precede tRNS chunk");
        }
        if (background != null) {
            throw new ImageFormatException("PLTE chunk must precede bKGD chunk");
        }
        if (!idatBuffer.isEmpty()) {
            throw new ImageFormatException("PLTE chunk must precede IDAT chunks");
        }

        // https://www.w3.org/TR/png/#11PLTE
        // TODO check rules
        if (!colorType.usesTruecolor()) {
            throw new ImageDataException("image color type %s does not allow PLTE chunk", colorType.getName());
        }
        final int len = chunk.data().length;
        if (len % 3 != 0) {
            throw new ImageDataException("invalid PLTE chunk samples length {%d}", len);
        }
        // TODO nani?
        if ((len / 3) > (2 << bitDepth - 1)) {
            throw new ImageDataException(
                    "PLTE chunk samples length is too large for bit depth {%d, %d}", len, bitDepth);
        }

        palette = chunk.data();
    }

    protected void update_bKGD(Chunk chunk) throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType.usesPalette() && palette == null) {
            throw new ImageFormatException("PLTE chunk must precede bKGD chunk");
        }
        if (!idatBuffer.isEmpty()) {
            throw new ImageFormatException("bKGD chunk must precede IDAT chunks");
        }

        // TODO mask color values for bit depth
        // FIXME incorrect len check, truecolor is 6 bytes
        // TODO ensure index is valid if INDEXED
        // https://www.w3.org/TR/png/#11bKGD
        final int len = chunk.data().length;
        if (len != (colorType.usesPalette() ? 1 : 2)) {
            throw new ImageDataException("invalid bKGD chunk samples length {%d}", len);
        }

        background = chunk.data();
    }

    protected void update_tRNS(Chunk chunk) throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType.usesPalette() && palette == null) {
            throw new ImageFormatException("PLTE chunk must precede tRNS chunk");
        }
        if (!idatBuffer.isEmpty()) {
            throw new ImageFormatException("tRNS chunk must precede IDAT chunks");
        }

        // https://www.w3.org/TR/png/#11tRNS
        if (colorType.usesAlpha()) {
            throw new ImageDataException("image color type %s cannot have tRNS chunk", colorType.getName());
        }

        // TODO mask alpha values for bit depth
        final int len = chunk.data().length;
        if (colorType.usesPalette()) {
            if (len > palette.length / 3) {
                throw new ImageDataException("too many entries in tRNS chunk {%d}", len);
            }
        }
        else if (colorType.usesTruecolor()) {
            // TODO validate TRUECOLOR 2
        }
        else {
            // TODO validate GREYSCALE 0
        }

        transparency = chunk.data();
    }

    private int getBitDepth(byte value) throws ImageDataException {
        int i = value & 0xFF;
        return switch (i) {
            case BIT_DEPTH_16 -> BIT_DEPTH_16;
            case BIT_DEPTH_8 -> BIT_DEPTH_8;
            case BIT_DEPTH_4 -> BIT_DEPTH_4;
            case BIT_DEPTH_2 -> BIT_DEPTH_2;
            case BIT_DEPTH_1 -> BIT_DEPTH_1;
            default ->
                    throw new ImageDataException("invalid bit depth value {%d}", i);
        };
    }

    private byte[] getCompositingBackground() {
        // create new background color for color type
        if (background == null) {
            // INDEXED
            if (colorType.usesPalette()) {
                return new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            }
            // TRUECOLOR and TRUECOLOR_ALPHA
            if (colorType.usesTruecolor()) {
                return new byte[] {
                        (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0xFF};
            }
            // GREYSCALE and GREYSCALE_ALPHA
            else {
                return new byte[] {(byte) 0xFF, (byte) 0xFF};
            }
        }
        // get background color from palette
        else if (colorType.usesPalette()) {
            int index = (background[0] & 0xFF) * 3;
            return new byte[] {
                    palette[index],
                    palette[index + 1],
                    palette[index + 2]};
        }
        // background color samples are stored in array
        else {
            return background;
        }
    }

    private byte[] getPremultipliedPalette(byte[] bkgd) {
        if (colorType.usesPalette() && palette != null && transparency != null) {
            // background color constants for multiplying
            final float r = (float) (bkgd[0] & 0xFF);
            final float g = (float) (bkgd[1] & 0xFF);
            final float b = (float) (bkgd[2] & 0xFF);
            // return value with premultiplied colors
            byte[] plte = Arrays.copyOf(palette, palette.length);
            // iterate over all entries in tRNS (may contain fewer entries than palette entries)
            for (int i = 0, pi = 0; i < transparency.length; i++, pi += 3) {
                int alpha = transparency[i] & 0xFF;
                // fully transparent
                if (alpha == 0) {
                    plte[pi]     = bkgd[0];
                    plte[pi + 1] = bkgd[1];
                    plte[pi + 2] = bkgd[2];
                }
                // partially transparent
                else if (alpha != 0xFF) {
                    // output = alpha * foreground + (1-alpha) * background
                    float alpha_fg = alpha / 255.0F;
                    float alpha_bg = 1.0F - alpha_fg;
                    plte[pi]     = (byte) ((int) (alpha_fg * (plte[pi] & 0xFF)) + (int) (alpha_bg * r));
                    plte[pi + 1] = (byte) ((int) (alpha_fg * (plte[pi + 1] & 0xFF)) + (int) (alpha_bg * g));
                    plte[pi + 2] = (byte) ((int) (alpha_fg * (plte[pi + 2] & 0xFF)) + (int) (alpha_bg * b));
                }
            }
            return plte;
        }
        else {
            return palette;
        }
    }

    private FilterMethod getFilterMethod(byte value) throws ImageDataException {
        int i = value & 0xFF;
        if (i == FILTER_METHOD_0) {
            return new FilterMethod_0();
        }
        else {
            throw new ImageDataException("invalid filter method value {%d}", i);
        }
    }

    private byte[] getFilteredData() throws ImageDataException {
        Inflater inflater = new Inflater();
        inflater.setInput(idatBuffer.getData()); // <- NOTE inflater uses array pointer; does not copy
        if (inflater.needsDictionary()) {
            throw new ImageDataException("cannot decompress image samples");
        }
        // buffer of decompressed, filtered image data
        ByteArrayBuffer dataBuffer = new ByteArrayBuffer();
        try {
            // decompress bytes progressively and store in buffer
            while (!inflater.finished()) {
                byte[] bytes = new byte[8192];
                int len = inflater.inflate(bytes);
                dataBuffer.add(bytes, len);
            }
            inflater.end();
        }
        catch (DataFormatException e) {
            throw new ImageDataException("an error occurred while decompressing image samples", e);
        }
        return dataBuffer.getData();
    }

    private InterlaceMethod getInterlaceMethod(byte value) throws ImageDataException {
        int i = value & 0xFF;
        if (i == INTERLACE_METHOD_0) {
            return NullMethod.INSTANCE;
        }
        else if (i == INTERLACE_METHOD_1) {
            return Adam7.INSTANCE;
        }
        else {
            throw new ImageDataException("invalid interlace method value {%d}", i);
        }
    }
}

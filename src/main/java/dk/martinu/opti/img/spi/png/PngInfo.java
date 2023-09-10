package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.GrayscaleImage;
import dk.martinu.opti.img.OptiImage;
import dk.martinu.opti.img.RgbImage;
import dk.martinu.opti.img.spi.Chunk;
import dk.martinu.opti.img.spi.ImageDataException;
import dk.martinu.opti.img.spi.ImageFormatException;

import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static dk.martinu.opti.Util.getInt;
import static dk.martinu.opti.img.spi.png.BitDepth.*;
import static dk.martinu.opti.img.spi.png.ChunkType.*;
import static dk.martinu.opti.img.spi.png.ColorType.*;

public class PngInfo {

    /**
     * Constant for compression method 0 (deflate).
     */
    public static final int COMPRESSION_DEFLATE = 0;
    /**
     * Constant for filter method 0.
     */
    public static final int FILTER_METHOD_0 = 0;
    /**
     * Constant for interlace method 0 (null).
     */
    public static final int INTERLACE_NULL = 0;
    /**
     * Constant for interlace method 1 (Adam7).
     */
    public static final int INTERLACE_ADAM_7 = 1;

    private static final int IHDR_LENGTH = 13;

    /* IHDR chunk fields */

    public final int width;
    public final int height;
    public final BitDepth bitDepth; // TODO byte or int?
    public final ColorType colorType;
    // TODO remove unless used by decoders
    public final byte compressionMethod;
    public final byte filterMethod;
    public final byte interlaceMethod;

    /* image data */

    protected byte[] palette = null;
    /**
     * 8-bit alpha samples for INDEXED, otherwise color with 16-bit samples to
     * make transparent.
     */
    protected byte[] transparency = null;
    /**
     * Background color with 16-bit samples.
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

        bitDepth = BitDepth.get(chunk.data()[8]);

        colorType = ColorType.get(chunk.data()[9]);
        switch (colorType) {
            case TRUECOLOR, GREYSCALE_ALPHA, TRUECOLOR_ALPHA -> {
                if (bitDepth != b8 && bitDepth != b16) {
                    throw new ImageDataException("invalid bit depth for color type %s {%d}", colorType, bitDepth);
                }
            }
            case INDEXED -> {
                if (bitDepth == b16) {
                    throw new ImageDataException("invalid bit depth for color type %s {%d}", colorType, bitDepth);
                }
            }
        }

        compressionMethod = chunk.data()[10];
        if (compressionMethod != COMPRESSION_DEFLATE) {
            throw new ImageDataException("invalid compression method {%d}", compressionMethod);
        }

        filterMethod = chunk.data()[11];
        if (filterMethod != FILTER_METHOD_0) {
            throw new ImageDataException("invalid filter method {%d}", filterMethod);
        }

        interlaceMethod = chunk.data()[12];
        if (interlaceMethod != INTERLACE_NULL && interlaceMethod != INTERLACE_ADAM_7) {
            throw new ImageDataException("invalid interlace method {%d}", interlaceMethod);
        }
    }

    public OptiImage createImage() throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType == INDEXED && palette == null) {
            throw new ImageFormatException("missing PLTE chunk");
        }
        if (idatBuffer.isEmpty()) {
            throw new ImageFormatException("missing IDAT chunk(s)");
        }

        // buffer of decompressed, filtered image data
        ByteArrayBuffer dataBuffer = new ByteArrayBuffer();
        // decompress image data and store in buffer
        Inflater inflater = new Inflater();
        inflater.setInput(idatBuffer.getData()); // <- NOTE inflater uses array pointer; does not copy
        if (inflater.needsDictionary()) {
            throw new ImageDataException("cannot decompress image data");
        }
        try {
            while (!inflater.finished()) {
                byte[] bytes = new byte[1024];
                int len = inflater.inflate(bytes);
                dataBuffer.add(bytes, len);
            }
            inflater.end();
        }
        catch (DataFormatException e) {
            throw new ImageDataException("an error occurred while decompressing image data", e);
        }
        // filtered source bytes
        byte[] src = dataBuffer.getData();
        // reconstructed destination bytes
        byte[] dest;

        // reconstruct filtered image data
        if (interlaceMethod == INTERLACE_NULL) {
            // scanline length in bytes
            final int len;
            if (colorType != INDEXED) {
                len = switch (bitDepth) {
                    case b16 -> width << 1;
                    case b8 -> width;
                    case b4 -> (int) Math.ceil((double) width / 2);
                    case b2 -> (int) Math.ceil((double) width / 4);
                    case b1 -> (int) Math.ceil((double) width / 8);
                };
            }
            else {
                len = width;
            }
            FilterMethod fm = new FilterMethod_0();
            dest = fm.reconstruct(src, height, len);
        }
        else /* if (interlaceMethod == INTERLACE_ADAM_7) */ {
            // TODO interlacing
            dest = new byte[0];
        }

        // FIXME IDAT lengths are not validated and can contain any amount of data
        //  both too much and too little

        // return value
        final OptiImage img;
        // TODO rgb
        if (colorType == TRUECOLOR) {
            img = new RgbImage(width, height);
        }
        // TODO rgba
        else if (colorType == TRUECOLOR_ALPHA) {
            img = new RgbImage(width, height);
        }

        else if (colorType == GREYSCALE) {
            img = new GrayscaleImage(width, height);

            /* sample depth is unchanged */
            if (bitDepth == b8) {
                // filter transparent samples and set to background
                if (transparency != null) {
                    // low-order byte of background color
                    final byte bkgd = background != null ? background[1] : 0;
                    for (int i = 0; i < dest.length; i++) {
                        if (dest[i] == transparency[1]) {
                            dest[i] = bkgd;
                            break;
                        }
                    }
                }
                // transfer samples directly
                System.arraycopy(dest, 0, img.data, 0, dest.length);
            }

            /* sample depth is downscaled */
            else if (bitDepth == b16) {
                // filter transparent samples and set to background
                if (transparency != null) {
                    // high-order byte of background color
                    final byte bkgd = background != null ? background[0] : 0;
                    for (int i = 0; i < dest.length; i += 2) {
                        if (dest[i] == transparency[0] && dest[i + 1] == transparency[1]) {
                            dest[i] = bkgd;
                            break;
                        }
                    }
                }
                // transfer high-order bytes into image data array
                for (int i = 0, k = 0; i < dest.length; i += 2, k = i >> 1) {
                    img.data[k] = dest[i];
                }
            }

            /* sample depth is upscaled */
            else if (bitDepth == b4) {
                // filter transparent samples and set to background
                if (transparency != null) {
                    // low-order byte of transparent color, masked
                    int trns = transparency[1] & 0x0F;
                    trns = trns << 4 | trns;
                    // low-order byte of background color, masked
                    int bkgd = background != null ? background[1] & 0x0F : 0;
                    bkgd = bkgd << 4 | bkgd;
                    for (int i = 0; i < dest.length; i++) {
                        int b = dest[i];
                        int s0 = b & 0xF0;
                        int s1 = b & 0x0F;
                        if ((s0 & trns) == s0) {
                            s0 = bkgd & 0xF0;
                        }
                        if ((s1 & trns) == s1) {
                            s1 = bkgd & 0x0F;
                        }
                        int f = s0 | s1;
                        if (f != (b & 0xFF)) {
                            dest[i] = (byte) f;
                        }
                    }
                }
                // split bytes and transfer into image data array
                for (int i = 0, k = 0; i < dest.length; i++, k = i << 1) {
                    int b = dest[i];
                    int s0 = b & 0xF0;
                    int s1 = b & 0x0F;
                    // left bit replication TESTME left bit replication
                    //@fmt:off
                    img.data[k]     = (byte) (s0 | s0 >> 4);
                    img.data[k + 1] = (byte) (s1 << 4 | s1);
                    //@fmt:on
                }
            }
            else if (bitDepth == b2) {
                byte[] data_2 = idatBuffer.getData();
                // filter transparent samples and set to background
                if (transparency != null) {
                    // low-order byte of transparent color, masked
                    int trns = transparency[1] & 0x03;
                    trns = trns << 6 | trns << 4 | trns << 2 | trns;
                    // low-order byte of background color, masked
                    int bkgd = background != null ? background[1] & 0x03 : 0;
                    bkgd = bkgd << 6 | bkgd << 4 | bkgd << 2 | bkgd;
                    for (int i = 0; i < data_2.length; i++) {
                        int b = data_2[i];
                        int s0 = b & 0xC0;
                        int s1 = b & 0x30;
                        int s2 = b & 0x0C;
                        int s3 = b & 0x03;
                        if ((s0 & trns) == s0) {
                            s0 = bkgd & 0xC0;
                        }
                        if ((s1 & trns) == s1) {
                            s1 = bkgd & 0x30;
                        }
                        if ((s2 & trns) == s2) {
                            s2 = bkgd & 0x0C;
                        }
                        if ((s3 & trns) == s3) {
                            s3 = bkgd & 0x03;
                        }
                        int f = s0 | s1 | s2 | s3;
                        if (f != (b & 0xFF)) {
                            data_2[i] = (byte) f;
                        }
                    }
                }
                // split bytes and transfer into image data array
                for (int i = 0, k = 0; i < data_2.length; i++, k = i << 2) {
                    int b = data_2[i];
                    int s0 = b & 0xC0;
                    int s1 = b & 0x30;
                    int s2 = b & 0x0C;
                    int s3 = b & 0x03;
                    // left bit replication TESTME left bit replication
                    //@fmt:off
                    img.data[k]     = (byte) (s0 | s0 >> 2 | s0 >> 4 | s0 >> 6);
                    img.data[k + 1] = (byte) (s1 << 2 | s1 | s1 >> 2 | s1 >> 4);
                    img.data[k + 2] = (byte) (s2 << 4 | s2 << 2 | s2 | s2 >> 2);
                    img.data[k + 3] = (byte) (s3 << 6 | s3 << 4 | s3 << 2 | s3);
                    //@fmt:on
                }
            }
            else /* if (bitDepth == 1) */ {
                // filter transparent samples and set to background
                filterTransparent:
                if (transparency != null) {
                    // low-order byte of transparent color, masked
                    int trns = (transparency[1] & 0x01) == 1 ? 0xFF : 0;
                    // low-order byte of background color, masked
                    int bkgd = (background != null ? background[1] & 0x01 : 0) == 1 ? 0xFF : 0;
                    if (trns == bkgd) {
                        break filterTransparent;
                    }
                    for (int i = 0; i < dest.length; i++) {
                        int b = dest[i];
                        int s0 = b & 0x01;
                        int s1 = b & 0x02;
                        int s2 = b & 0x04;
                        int s3 = b & 0x08;
                        int s4 = b & 0x10;
                        int s5 = b & 0x20;
                        int s6 = b & 0x40;
                        int s7 = b & 0x80;
                        // TODO there's most likely a much cleaner way to do this
                        if ((s0 & trns) == s0) {
                            s0 = bkgd & 0x01;
                        }
                        if ((s1 & trns) == s1) {
                            s1 = bkgd & 0x02;
                        }
                        if ((s2 & trns) == s2) {
                            s2 = bkgd & 0x04;
                        }
                        if ((s3 & trns) == s3) {
                            s3 = bkgd & 0x08;
                        }
                        if ((s4 & trns) == s4) {
                            s4 = bkgd & 0x10;
                        }
                        if ((s5 & trns) == s5) {
                            s5 = bkgd & 0x20;
                        }
                        if ((s6 & trns) == s6) {
                            s6 = bkgd & 0x40;
                        }
                        if ((s7 & trns) == s7) {
                            s7 = bkgd & 0x80;
                        }
                        int f = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
                        if (f != (b & 0xFF)) {
                            dest[i] = (byte) f;
                        }
                    }
                }
                // split bytes and transfer into image data array
                for (int i = 0, k = 0; i < dest.length; i++, k = i << 3) {
                    int b = dest[i];
                    //@fmt:off
                    img.data[k]     = (byte) ((b & 0x80) == 0 ? 0 : 0xFF);
                    img.data[k + 1] = (byte) ((b & 0x40) == 0 ? 0 : 0xFF);
                    img.data[k + 2] = (byte) ((b & 0x20) == 0 ? 0 : 0xFF);
                    img.data[k + 3] = (byte) ((b & 0x10) == 0 ? 0 : 0xFF);
                    img.data[k + 4] = (byte) ((b & 0x08) == 0 ? 0 : 0xFF);
                    img.data[k + 5] = (byte) ((b & 0x04) == 0 ? 0 : 0xFF);
                    img.data[k + 6] = (byte) ((b & 0x02) == 0 ? 0 : 0xFF);
                    img.data[k + 7] = (byte) ((b & 0x01) == 0 ? 0 : 0xFF);
                    //@fmt:on
                }
            }
        }

        // TODO greyscale alpha
        else if (colorType == GREYSCALE_ALPHA) {
            img = new GrayscaleImage(width, height);
        }

        // TODO indexed
        else /* if (colorType == INDEXED) */ {
            img = new RgbImage(width, height);
        }

        return img;
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
            throw new ImageFormatException("PLTE chunk must precede IDAT chunk(s)");
        }

        // https://www.w3.org/TR/png/#11PLTE
        if (colorType == GREYSCALE || colorType == GREYSCALE_ALPHA) {
            throw new ImageDataException("image color type %s cannot have PLTE chunk", colorType);
        }
        final int len = chunk.data().length;
        if (len % 3 != 0) {
            throw new ImageDataException("invalid PLTE chunk data length {%d}", len);
        }
        if ((len / 3) > (2 << bitDepth.value - 1)) {
            throw new ImageDataException(
                    "PLTE chunk data length is too large for bit depth {%d, %d}", len, bitDepth);
        }

        palette = chunk.data();
    }

    protected void update_tRNS(Chunk chunk) throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType == INDEXED && palette == null) {
            throw new ImageFormatException("PLTE chunk must precede tRNS chunk");
        }
        if (!idatBuffer.isEmpty()) {
            throw new ImageFormatException("tRNS chunk must precede IDAT chunk(s)");
        }

        // https://www.w3.org/TR/png/#11tRNS
        final int len = chunk.data().length;
        switch (colorType) {
            case GREYSCALE -> {
                // TODO validate
            }
            case TRUECOLOR -> {
                // TODO validate
            }
            case INDEXED -> {
                if (len > palette.length / 3) {
                    throw new ImageDataException("too many entries in tRNS chunk {%d}", len);
                }
            }
            case GREYSCALE_ALPHA, TRUECOLOR_ALPHA ->
                    throw new ImageDataException("image color type %s cannot have tRNS chunk", colorType);
        }

        transparency = chunk.data();
    }

    protected void update_IDAT(Chunk chunk) throws ImageFormatException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (isIdatBufferClosed) {
            throw new ImageFormatException("IDAT chunks must be consecutive");
        }

        idatBuffer.add(chunk.data());
    }

    protected void update_bKGD(Chunk chunk) throws ImageFormatException, ImageDataException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType == INDEXED && palette == null) {
            throw new ImageFormatException("PLTE chunk must precede bKGD chunk");
        }
        if (!idatBuffer.isEmpty()) {
            throw new ImageFormatException("bKGD chunk must precede IDAT chunk(s)");
        }

        // https://www.w3.org/TR/png/#11bKGD
        final int len = chunk.data().length;
        if (len != switch (colorType) {
            case INDEXED -> 1;
            case GREYSCALE, TRUECOLOR, GREYSCALE_ALPHA, TRUECOLOR_ALPHA -> 2;
        }) {
            throw new ImageDataException("invalid bKGD chunk data length {%d}", len);
        }

        background = chunk.data();
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
                        throw new ImageFormatException("hIST chunk must precede IDAT chunk(s)");
                    }
                }
                case sPLT, eXIf -> {
                    if (!idatBuffer.isEmpty()) {
                        throw new ImageFormatException(
                                "%s chunk must precede IDAT chunk(s)", ChunkType.toString(chunk.type()));
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
                                "%s chunk must precede IDAT chunk(s)", ChunkType.toString(chunk.type()));
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
}

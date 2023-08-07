package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.GrayscaleImage;
import dk.martinu.opti.img.OptiImage;
import dk.martinu.opti.img.RgbImage;
import dk.martinu.opti.img.spi.Chunk;
import dk.martinu.opti.img.spi.InvalidImageException;

import java.util.Iterator;
import java.util.Objects;

import static dk.martinu.opti.Util.getInt;
import static dk.martinu.opti.img.spi.png.ChunkType.*;
import static dk.martinu.opti.img.spi.png.ColorType.*;

public class PngInfo {

    private static final int IHDR_LENGTH = 13;

    /* IHDR chunk fields */

    public final int width;
    public final int height;
    public final int bitDepth; // TODO byte or int?
    public final ColorType colorType;
    // TODO remove unless used by decoders
    public final byte compressionMethod;
    public final byte filterMethod;
    public final byte interlaceMethod;

    /* image data */

    protected byte[] palette = null;
    protected byte[] transparency = null;
    protected byte[] background = null;

    protected IdatQueue idatQueue = new IdatQueue();

    public PngInfo(Chunk chunk) throws InvalidImageException {
        Objects.requireNonNull(chunk, "chunk is null");
        if (chunk.type() != IHDR) {
            throw new InvalidImageException("missing IHDR chunk");
        }
        if (chunk.data().length != IHDR_LENGTH) {
            throw new InvalidImageException("invalid IHDR chunk");
        }

        width = getInt(chunk.data());
        if (width < 1) {
            throw new InvalidImageException("invalid image width {%d}", width);
        }

        height = getInt(chunk.data(), 4);
        if (height < 1) {
            throw new InvalidImageException("invalid image height {%d}", height);
        }

        bitDepth = chunk.data()[8];
        if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16) {
            throw new InvalidImageException("invalid bit depth {%d}", bitDepth);
        }

        colorType = ColorType.get(chunk.data()[9]);
        switch (colorType) {
            case TRUECOLOR, GREYSCALE_ALPHA, TRUECOLOR_ALPHA -> {
                if (bitDepth != 8 && bitDepth != 16) {
                    throw new InvalidImageException("invalid bit depth for color type %s {%d}", colorType, bitDepth);
                }
            }
            case INDEXED -> {
                if (bitDepth == 16) {
                    throw new InvalidImageException("invalid bit depth for color type %s {%d}", colorType, bitDepth);
                }
            }
        }

        compressionMethod = chunk.data()[10];
        filterMethod = chunk.data()[11];
        interlaceMethod = chunk.data()[12];
    }

    // TODO create image
    public OptiImage createImage() throws InvalidImageException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType == INDEXED && palette == null) {
            throw new InvalidImageException("missing PLTE chunk");
        }
        if (idatQueue.isEmpty()) {
            throw new InvalidImageException("missing IDAT chunk(s)");
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
        // FIXME grayscale ignores tRNS entries
        else if (colorType == GREYSCALE) {
            img = new GrayscaleImage(width, height);

            /* sample depth is unchanged */
            if (bitDepth == 8) {
                final Iterator<byte[]> iterator = idatQueue.iterator();
                // offset into image data array
                int offset = 0;
                // transfer bytes directly into image data array
                while (iterator.hasNext()) {
                    byte[] idat = iterator.next();
                    System.arraycopy(idat, 0, img.data, offset, idat.length);
                    offset += idat.length;
                }
            }

            /* sample depth is downscaled */
            else if (bitDepth == 16) {
                final Iterator<byte[]> iterator = idatQueue.iterator();
                // offset into image data array
                int offset = 0;
                // true if skipping first byte in IDAT array
                boolean skip = false;
                // transfer high-order bytes into image data array
                while (iterator.hasNext()) {
                    byte[] idat = iterator.next();
                    int lim = idat.length >> 1;
                    for (int i = skip ? 1 : 0; i < lim; i++) {
                        img.data[i + offset] = idat[i * 2];
                    }
                    offset += lim;
                    skip = skip ? (idat.length & 1) == 0 : (idat.length & 1) == 1;
                }
            }

            /* sample depth is upscaled */
            else if (bitDepth == 4) {
                final Iterator<byte[]> iterator = idatQueue.iterator();
                // offset into image data array
                int offset = 0;
                while (iterator.hasNext()) {
                    byte[] idat = iterator.next();
                    for (int i = 0, k = 0; i < idat.length; i++, k = i << 1) {
                        byte b = idat[i];
                        int s0 = b & 0xF0;
                        int s1 = b & 0x0F;
                        // left bit replication
                        //@fmt:off
                        img.data[k + offset]     = (byte) (s0 | s0 >> 4);
                        img.data[k + offset + 1] = (byte) (s1 << 4 | s1);
                        //@fmt:on
                    }
                    offset += idat.length << 1;
                }
            }
            else if (bitDepth == 2) {
                final Iterator<byte[]> iterator = idatQueue.iterator();
                // offset into image data array
                int offset = 0;
                while (iterator.hasNext()) {
                    byte[] idat = iterator.next();
                    for (int i = 0, k = 0; i < idat.length; i++, k = i << 2) {
                        byte b = idat[i];
                        int s0 = b & 0xC0;
                        int s1 = b & 0x30;
                        int s2 = b & 0x0C;
                        int s3 = b & 0x03;
                        // TESTME upscaling
                        // left bit replication
                        //@fmt:off
                        img.data[k + offset]     = (byte) (s0 | s0 >> 2 | s0 >> 4 | s0 >> 6);
                        img.data[k + offset + 1] = (byte) (s1 << 2 | s1 | s1 >> 2 | s1 >> 4);
                        img.data[k + offset + 2] = (byte) (s2 << 4 | s2 << 2 | s2 | s2 >> 2);
                        img.data[k + offset + 3] = (byte) (s3 << 6 | s3 << 4 | s3 << 2 | s3);
                        //@fmt:on
                    }
                    offset += idat.length << 2;
                }
            }
            else /* if (bitDepth == 1) */ {
                final Iterator<byte[]> iterator = idatQueue.iterator();
                // offset into image data array
                int offset = 0;
                while (iterator.hasNext()) {
                    byte[] idat = iterator.next();
                    for (int i = 0, k = 0; i < idat.length; i++, k = i << 3) {
                        byte b = idat[i];
                        //@fmt:off
                        img.data[k + offset]     = (byte) ((b & 0x80) == 0 ? 0 : -1);
                        img.data[k + offset + 1] = (byte) ((b & 0x40) == 0 ? 0 : -1);
                        img.data[k + offset + 2] = (byte) ((b & 0x20) == 0 ? 0 : -1);
                        img.data[k + offset + 3] = (byte) ((b & 0x10) == 0 ? 0 : -1);
                        img.data[k + offset + 4] = (byte) ((b & 0x08) == 0 ? 0 : -1);
                        img.data[k + offset + 5] = (byte) ((b & 0x04) == 0 ? 0 : -1);
                        img.data[k + offset + 6] = (byte) ((b & 0x02) == 0 ? 0 : -1);
                        img.data[k + offset + 7] = (byte) ((b & 0x01) == 0 ? 0 : -1);
                        //@fmt:on
                    }
                    offset += idat.length << 3;
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

    protected void update_PLTE(Chunk chunk) throws InvalidImageException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (palette != null) {
            throw new InvalidImageException("image contains multiple PLTE chunks");
        }
        if (transparency != null) {
            throw new InvalidImageException("PLTE chunk must precede tRNS chunk");
        }
        if (background != null) {
            throw new InvalidImageException("PLTE chunk must precede bKGD chunk");
        }
        if (!idatQueue.isEmpty()) {
            throw new InvalidImageException("PLTE chunk must precede IDAT chunk(s)");
        }

        // https://www.w3.org/TR/png/#11PLTE
        if (colorType == GREYSCALE || colorType == GREYSCALE_ALPHA) {
            throw new InvalidImageException("image color type %s cannot have PLTE chunk", colorType);
        }
        final int len = chunk.data().length;
        if (len % 3 != 0) {
            throw new InvalidImageException("invalid PLTE chunk data length {%d}", len);
        }
        if ((len / 3) > (2 << bitDepth - 1)) {
            throw new InvalidImageException(
                    "PLTE chunk data length is too large for bit depth {%d, %d}", len, bitDepth);
        }

        palette = chunk.data();
    }

    protected void update_tRNS(Chunk chunk) throws InvalidImageException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType == INDEXED && palette == null) {
            throw new InvalidImageException("PLTE chunk must precede tRNS chunk");
        }
        if (!idatQueue.isEmpty()) {
            throw new InvalidImageException("tRNS chunk must precede IDAT chunk(s)");
        }

        // https://www.w3.org/TR/png/#11tRNS
        final int len = chunk.data().length;
        switch (colorType) {
            case INDEXED -> {
                if (len > palette.length / 3) {
                    throw new InvalidImageException("too many entries in tRNS chunk {%d}", len);
                }
            }
            case GREYSCALE_ALPHA, TRUECOLOR_ALPHA ->
                    throw new InvalidImageException("image color type %s cannot have tRNS chunk", colorType);
        }

        transparency = chunk.data();
    }

    protected void update_IDAT(Chunk chunk) throws InvalidImageException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (idatQueue.isClosed()) {
            throw new InvalidImageException("IDAT chunks must be consecutive");
        }

        idatQueue.add(chunk.data());
    }

    protected void update_bKGD(Chunk chunk) throws InvalidImageException {
        // https://www.w3.org/TR/png/#5ChunkOrdering
        if (colorType == INDEXED && palette == null) {
            throw new InvalidImageException("PLTE chunk must precede bKGD chunk");
        }
        if (!idatQueue.isEmpty()) {
            throw new InvalidImageException("bKGD chunk must precede IDAT chunk(s)");
        }

        // https://www.w3.org/TR/png/#11bKGD
        final int len = chunk.data().length;
        switch (colorType) {
            case INDEXED -> {
                if (len != 1) {
                    throw new InvalidImageException("invalid bKGD chunk data length {%d}", len);
                }
            }
            case GREYSCALE, TRUECOLOR, GREYSCALE_ALPHA, TRUECOLOR_ALPHA -> {
                if (len != 2) {
                    throw new InvalidImageException("invalid bKGD chunk data length {%d}", len);
                }
            }
        }

        background = chunk.data();
    }

    public void update(Chunk chunk) throws InvalidImageException {
        Objects.requireNonNull(chunk, "chunk is null");

        // critical chunks
        if (chunk.isCritical()) {
            switch (chunk.type()) {
                case PLTE -> update_PLTE(chunk);
                case IDAT -> update_IDAT(chunk);
                default -> throw new InvalidImageException(
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
                    if (!idatQueue.isEmpty()) {
                        idatQueue.close();
                    }
                    // TODO log
                }
                case hIST -> {
                    if (palette == null) {
                        throw new InvalidImageException("PLTE chunk must precede hIST chunk");
                    }
                    if (!idatQueue.isEmpty()) {
                        throw new InvalidImageException("hIST chunk must precede IDAT chunk(s)");
                    }
                }
                case sPLT, eXIf -> {
                    if (!idatQueue.isEmpty()) {
                        throw new InvalidImageException(
                                "%s chunk must precede IDAT chunk(s)", ChunkType.toString(chunk.type()));
                    }
                    // TODO log
                }
                // color space chunks
                case cHRM, gAMA, iCCP, sBIT, sRGB, cICP, mDCv, cLLi -> {
                    if (palette != null) {
                        throw new InvalidImageException(
                                "%s chunk must precede PLTE chunk", ChunkType.toString(chunk.type()));
                    }
                    if (!idatQueue.isEmpty()) {
                        throw new InvalidImageException(
                                "%s chunk must precede IDAT chunk(s)", ChunkType.toString(chunk.type()));
                    }
                    // TODO log
                }
                // unknown ancillary chunks
                default -> {
                    if (!idatQueue.isEmpty()) {
                        idatQueue.close();
                    }
                    // TODO log
                }
            }
        }
    }
}

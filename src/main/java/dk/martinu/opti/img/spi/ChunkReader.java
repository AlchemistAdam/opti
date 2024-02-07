package dk.martinu.opti.img.spi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;
import java.util.zip.CRC32;

public class ChunkReader {

    private static boolean isTypeValid(int type) {
        for (int i = 0; i < 4; i++, type >>>= 8) {
            int b = type & 0xFF;
            if ((b < 'A' || b > 'Z') && (b < 'a' || b > 'z')) {
                return false;
            }
        }
        return true;
    }
    protected final ReadableByteChannel input;

    public ChunkReader(ReadableByteChannel input) {
        this.input = Objects.requireNonNull(input, "input is null");
    }

    public Chunk getChunk() throws IOException, ImageDataException {
        // reusable int buffer
        final ByteBuffer bInt = ByteBuffer.allocate(4);
        // used to compute CRC value of chunk
        final CRC32 crc32 = new CRC32();

        /* LENGTH */
        if (input.read(bInt.clear()) != 4) {
            throw new IOException("missing chunk length");
        }
        int len = bInt.flip().getInt();
        if (len < 0) {
            throw new IOException("invalid chunk length");
        }

        /* TYPE */
        if (input.read(bInt.clear()) != 4) {
            throw new IOException("missing chunk type");
        }
        int type = bInt.flip().getInt();
        if (!isTypeValid(type)) {
            throw new ImageDataException("chunk type is invalid {" + Chunk.typeToString(type) + "}");
        }
        crc32.update(bInt.flip());

        /* DATA */
        byte[] data = new byte[len];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if (input.read(buffer) != len) {
            throw new IOException("missing chunk data");
        }
        crc32.update(buffer.flip());

        /* CRC */
        if (input.read(bInt.clear()) != 4) {
            throw new IOException("missing chunk CRC");
        }
        int crc = bInt.flip().getInt();
        if (crc != (int) crc32.getValue()) {
            throw new IOException("invalid CRC value for chunk {" + Chunk.typeToString(type) + "}, data is corrupt");
        }

        return new Chunk(type, data, crc);
    }
}

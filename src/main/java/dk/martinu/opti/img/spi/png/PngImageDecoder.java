package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.OptiImage;
import dk.martinu.opti.img.spi.Chunk;
import dk.martinu.opti.img.spi.ChunkReader;
import dk.martinu.opti.img.spi.ImageDecoder;
import dk.martinu.opti.img.spi.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static dk.martinu.opti.Util.getInt;

public class PngImageDecoder implements ImageDecoder {

    public static PngImageDecoder provider() {
        return new PngImageDecoder();
    }

    protected void validateFileHeader(FileChannel in) throws IOException, InvalidImageException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        if (in.read(buffer) != buffer.capacity()) {
            throw new IOException("missing PNG file header");
        }
        if (!isFileHeaderValid(buffer.array())) {
            throw new InvalidImageException("invalid PNG file header");
        }
    }

    protected boolean isFileHeaderValid(byte[] header) {
        return 0x89_50_4E_47 == getInt(header) && 0x0D_0A_1A_0A == getInt(header, 4);
    }

    @Override
    public boolean canDecode(Path path) {
        if (Files.isRegularFile(path) && Files.isReadable(path)) {
            if (path.endsWith(".png")) {
                return true;
            }
            try (FileChannel in = FileChannel.open(path, StandardOpenOption.READ)) {
                final ByteBuffer buffer = ByteBuffer.allocate(8);
                return in.read(buffer) == buffer.capacity()
                        && isFileHeaderValid(buffer.array());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public OptiImage decode(Path path) throws IOException {
        try (FileChannel input = FileChannel.open(path, StandardOpenOption.READ)) {
            validateFileHeader(input);
            final ChunkReader reader = new ChunkReader(input);
            // create image info from IHDR chunk
            final PngInfo info = new PngInfo(reader.getChunk());
            // read remaining chunks and update info
            Chunk chunk;
            while ((chunk = reader.getChunk()).type() != ChunkType.IEND)
                info.update(chunk);
            if (chunk.data().length != 0) {
                throw new InvalidImageException("invalid IEND chunk");
            }
            // create image from updated info
            return info.createImage();
        }
        catch (Exception e) {
            throw new IOException("could not read PNG image from file " + path, e);
        }
    }
}

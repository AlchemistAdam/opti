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
package dk.martinu.opti.img.png;

import dk.martinu.opti.img.ByteImage;
import dk.martinu.opti.img.spi.*;

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

    protected void validateFileHeader(FileChannel in) throws IOException, ImageFormatException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        if (in.read(buffer) != buffer.capacity()) {
            throw new IOException("missing PNG file header");
        }
        if (!isFileHeaderValid(buffer.array())) {
            throw new ImageFormatException("invalid PNG file header");
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
    public ByteImage decode(Path path) throws IOException {
        try (FileChannel input = FileChannel.open(path, StandardOpenOption.READ)) {
            validateFileHeader(input);
            final ChunkReader reader = new ChunkReader(input);
            // create image info from IHDR chunk
            final PngInfo info = new PngInfo(reader.getChunk());
            // read remaining chunks and update info
            Chunk chunk;
            while ((chunk = reader.getChunk()).type() != ChunkType.IEND) {
                info.update(chunk);
            }
            if (chunk.data().length != 0) {
                throw new ImageException("invalid IEND chunk");
            }
            // create image from updated info
            return info.createImage();
        }
        catch (IOException | ImageException e) {
            throw new IOException("could not read PNG image from file " + path, e);
        }
    }
}

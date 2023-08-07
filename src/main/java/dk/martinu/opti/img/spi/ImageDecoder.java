package dk.martinu.opti.img.spi;

import dk.martinu.opti.img.OptiImage;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageDecoder {

    boolean canDecode(Path path);

    OptiImage decode(Path path) throws IOException;
}

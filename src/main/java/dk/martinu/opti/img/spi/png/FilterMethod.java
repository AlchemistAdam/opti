package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

public interface FilterMethod {

    byte[] reconstruct(byte[] src, int lines, int len) throws ImageDataException;
}

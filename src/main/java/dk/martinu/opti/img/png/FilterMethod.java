package dk.martinu.opti.img.png;

import dk.martinu.opti.img.spi.ImageDataException;

public interface FilterMethod {


    default byte[] revert(byte[] data, int lines, int nBytes) throws ImageDataException {
        return revert(data, 0, lines, nBytes);
    }

    byte[] revert(byte[] data, int offset, int lines, int nBytes) throws ImageDataException;
}

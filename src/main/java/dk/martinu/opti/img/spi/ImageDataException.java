package dk.martinu.opti.img.spi;

public class ImageDataException extends ImageException {
    public ImageDataException(String msg) {
        super(msg);
    }

    public ImageDataException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ImageDataException(String msg, Object... args) {
        super(msg, args);
    }
}

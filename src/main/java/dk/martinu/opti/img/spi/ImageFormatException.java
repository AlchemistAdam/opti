package dk.martinu.opti.img.spi;

public class ImageFormatException extends ImageException {
    public ImageFormatException(String msg) {
        super(msg);
    }

    public ImageFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ImageFormatException(String msg, Object... args) {
        super(msg, args);
    }
}

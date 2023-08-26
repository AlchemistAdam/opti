package dk.martinu.opti.img.spi;

public class ImageException extends Exception {

    public ImageException(String msg) {
        super(msg);
    }

    public ImageException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ImageException(String msg, Object... args) {
        this(String.format(msg, args));
    }
}

package dk.martinu.opti.img.spi;

public class InvalidImageException extends Exception {

    public InvalidImageException(String msg) {
        super(msg);
    }

    public InvalidImageException(String msg, Object... args) {
        this(String.format(msg, args));
    }
}

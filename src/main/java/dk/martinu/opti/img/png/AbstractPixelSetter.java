package dk.martinu.opti.img.png;

import java.util.Objects;

abstract class AbstractPixelSetter implements PixelSetter {

    final int width;
    final int height;
    final byte[] samples;
    int i = 0;

    AbstractPixelSetter(ReducedImage image) {
        Objects.requireNonNull(image, "image is null");
        this.width = image.width();
        this.height = image.height();
        this.samples = image.samples();
    }
}

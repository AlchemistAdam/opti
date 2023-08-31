package dk.martinu.opti.transform;

import dk.martinu.opti.img.OptiImage;

public class BoxBlur implements ImageTransform {

    public final int radius;
    private final int size;
    private final int n;

    public BoxBlur(int radius) {
        if (radius < 1) {
            throw new IllegalArgumentException("radius is less than 1");
        }
        this.radius = radius;
        size = radius * 2 + 1;
        n = size * size;
    }

    @Override
    public OptiImage applyTo(OptiImage source) {
        // return source if image is too small to blur
        if (source.width < size || source.height < size) {
            return source;
        }
        // TODO this implementation is very inefficient
        // transformed image dimensions
        final int width = source.width - radius * 2;
        final int height = source.height - radius * 2;
        // allocate return image
        final OptiImage img = source.allocate(width, height);
        // blur samples
        for (int channel = 0; channel < source.channels; channel++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int sum = 0;
                    for (int sy = -radius; sy <= radius; sy++) {
                        for (int sx = -radius; sx <= radius; sx++) {
                            sum += source.getSample(x + sx, y + sy, channel);
                        }
                    }
                    img.setSample(x, y, channel, (byte) (sum / n));
                }
            }
        }
        return img;
    }
}

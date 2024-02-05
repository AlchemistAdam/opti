package dk.martinu.opti.transform;

import dk.martinu.opti.img.GrayscaleImage;
import dk.martinu.opti.img.OptiImage;
import dk.martinu.opti.img.RgbImage;

public class BoxBlur2 implements ImageTransform{

    public final int radius;
//    private final int size;
//    private final int n;

    public BoxBlur2(int radius) {
        if (radius < 1) {
            throw new IllegalArgumentException("radius is less than 1");
        }
        this.radius = radius;
//        size = radius * 2 + 1;
//        n = size * size;
    }

    @Override
    public OptiImage applyTo(OptiImage source) {
        // TODO if sample size is 8 then switch on channels instead of class types
        // return transform applied to image depending on type
        if (source instanceof GrayscaleImage) {
            return applyToGrayscale(source);
        }
        else if (source instanceof RgbImage) {
            return applyToRgb(source);
        }
        else {
            return applyToOther(source);
        }
    }

    protected OptiImage applyToGrayscale(OptiImage source) {
        // https://en.wikipedia.org/wiki/Box_blur

        // transformed image dimensions
        final int width = source.width - 2;
        final int height = source.height - 2;

        // sample variables
        byte s0, s1, s2;

        // horizontal pass
        for (int y = 0; y < height; y++) {
            s0 = source.getSample(0, y + 1, 0);
            s1 = source.getSample(1, y + 1, 0);
            s2 = source.getSample(2, y + 1, 0);

            for (int x = 1; x < width; x++) {

            }
        }
        // vertical pass


        // allocate return image with border pixels removed
        //noinspection UnnecessaryLocalVariable
        final OptiImage dest = source.allocate(width, height);
        return dest;
    }

    // TODO
    protected OptiImage applyToRgb(OptiImage source) {
        return null;
    }

    // TODO
    protected OptiImage applyToOther(OptiImage source) {
        return null;
    }
}

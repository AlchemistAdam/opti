package dk.martinu.opti.transform;

import dk.martinu.opti.img.OptiImage;

@FunctionalInterface
public interface ImageTransform {

    OptiImage applyTo(OptiImage source);
}

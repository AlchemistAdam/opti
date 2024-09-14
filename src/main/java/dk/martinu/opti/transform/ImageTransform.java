package dk.martinu.opti.transform;

import dk.martinu.opti.img.ByteImage;

@FunctionalInterface
public interface ImageTransform {

    ByteImage applyTo(ByteImage source);
}

package dk.martinu.opti.img.png;

abstract class PackedPixelSetter extends AbstractPixelSetter {

    int sampleByte = 0;
    int position = 0;

    PackedPixelSetter(ReducedImage image) {
        super(image);
    }
}

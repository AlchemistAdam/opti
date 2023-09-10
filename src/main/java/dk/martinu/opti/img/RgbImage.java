package dk.martinu.opti.img;

public class RgbImage extends ByteImage {

    @Override
    public OptiImage allocate(int width, int height) {
        return new RgbImage(width, height);
    }

    public RgbImage(int width, int height) {
        super(width, height, 3);
    }

    @Override
    public OptiImage allocate() {
        return new RgbImage(width, height);
    }

    @Override
    public byte[] getPixel(int x, int y, byte[] pixel) {
        final int offset = x + y * height;
        pixel[0] = data[offset];
        pixel[1] = data[offset + 1];
        pixel[2] = data[offset + 2];
        return pixel;
    }
}

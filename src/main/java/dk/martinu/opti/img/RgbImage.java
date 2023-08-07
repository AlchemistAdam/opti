package dk.martinu.opti.img;

public class RgbImage extends ByteImage {

    public RgbImage(int width, int height) {
        super(width, height, 3);
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

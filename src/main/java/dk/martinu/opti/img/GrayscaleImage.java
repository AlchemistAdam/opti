package dk.martinu.opti.img;

public class GrayscaleImage extends ByteImage {

    public GrayscaleImage(int width, int height) {
        super(width, height, 1);
    }

    @Override
    public byte[] getPixel(int x, int y, byte[] pixel) {
        pixel[0] = data[x + y * width];
        return pixel;
    }
}

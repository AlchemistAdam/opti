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

    @Override
    public byte[] getSamples(int x, int y, int channel, byte[] dest) {
        // number of samples to copy into dest
        final int len = Math.min(dest.length, (width * height) - (x + y * width));
        System.arraycopy(data, x + y * width, dest, 0, len);
        return dest;
    }

    @Override
    public byte getSample(int x, int y, int channel) {
        return data[x + y * width];
    }

    @Override
    public OptiImage allocate() {
        return new GrayscaleImage(width, height);
    }

    @Override
    public OptiImage allocate(int width, int height) {
        return new GrayscaleImage(width, height);
    }

    @Override
    public void setSample(int x, int y, int channel, byte s) {
        data[x + y * width] = s;
    }
}

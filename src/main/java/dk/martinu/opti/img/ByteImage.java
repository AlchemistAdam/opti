package dk.martinu.opti.img;

public class ByteImage extends OptiImage {


    public ByteImage(int width, int height, int channels) {
        super(width, height, channels, 8);
    }

    @Override
    public OptiImage allocate() {
        return new ByteImage(width, height, channels);
    }

    @Override
    public byte getSample(int x, int y, int channel) {
        return data[x + y * width + channel];
    }

    @Override
    public byte[] getPixel(int x, int y, byte[] pixel) {
        System.arraycopy(data, x + y * height, pixel, 0, channels);
        return pixel;
    }
}

import dk.martinu.opti.img.spi.ImageDecoder;
import dk.martinu.opti.img.png.PngImageDecoder;

module dk.martinu.opti {

    exports dk.martinu.opti;
    exports dk.martinu.opti.img;
    exports dk.martinu.opti.img.spi;
    exports dk.martinu.opti.img.png;

    uses ImageDecoder;
    provides ImageDecoder with PngImageDecoder;
}
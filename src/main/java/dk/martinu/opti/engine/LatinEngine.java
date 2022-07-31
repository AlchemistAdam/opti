package dk.martinu.opti.engine;

import java.awt.image.BufferedImage;

import dk.martinu.opti.Text;

public class LatinEngine implements Engine {

    public static LatinEngine provider(){
        return new LatinEngine();
    }

    @Override
    public int suitability(final BufferedImage source) {
        return 0;
    }

    @Override
    public Text getText(final BufferedImage source) {
        return null;
    }
}

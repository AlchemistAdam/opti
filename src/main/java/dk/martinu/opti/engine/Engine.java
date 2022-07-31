package dk.martinu.opti.engine;

import java.awt.image.BufferedImage;

import dk.martinu.opti.Text;

public interface Engine {

    int suitability(final BufferedImage source);

    Text getText(final BufferedImage source);
}

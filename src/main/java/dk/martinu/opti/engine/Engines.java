package dk.martinu.opti.engine;

import java.awt.image.*;
import java.util.*;

public class Engines {

    private static volatile ServiceLoader<Engine> engines = null;

    public static ServiceLoader<Engine> getEngines() {
        if (engines == null)
            synchronized (Engines.class) {
                if (engines == null)
                    engines = ServiceLoader.load(Engine.class);
            }
        return engines;
    }

    public static Engine getEngine(final BufferedImage source) {
        Objects.requireNonNull(source, "source is null");

        final TreeMap<Integer, Engine> map = new TreeMap<>();
        for (Engine engine : getEngines()) {
            final int suitability = engine.suitability(source);
            // engine is not suitable, skip it
            if (suitability == 0) {
                continue;
            }
            // engine might be suitable
            if (suitability < 100) {
                map.put(suitability, engine);
            }
            // engine is suitable
            else {
                return engine;
            }
        }
        return null;
    }

    private Engines() { }
}

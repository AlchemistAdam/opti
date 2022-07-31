import dk.martinu.opti.engine.LatinEngine;
import dk.martinu.opti.engine.Engine;

module dk.martinu.opti {

    requires java.base;
    requires java.desktop;
    requires dk.martinu.kofi;

    exports dk.martinu.opti;
    exports dk.martinu.opti.engine;
    exports dk.martinu.opti.geom;
//    exports dk.martinu.opti.script; TODO uncomment when package is not empty
//    exports dk.martinu.opti.spi; TODO uncomment when package is not empty
    exports dk.martinu.opti.ui;

    uses Engine;

    provides Engine with LatinEngine;
}
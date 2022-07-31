package dk.martinu.opti.geom;

import java.awt.Polygon;
import java.lang.ref.SoftReference;
import java.util.Objects;

public class Tetragon {

    public final Point p0;
    public final Point p1;
    public final Point p2;
    public final Point p3;
    protected SoftReference<Polygon> polygon = null;

    public Tetragon(final Point p0, final Point p1, final Point p2, final Point p3) {
        this.p0 = Objects.requireNonNull(p0, "p0 is null");
        this.p1 = Objects.requireNonNull(p1, "p1 is null");
        this.p2 = Objects.requireNonNull(p2, "p2 is null");
        this.p3 = Objects.requireNonNull(p3, "p3 is null");
    }

    public Polygon getPolygon() {
        Polygon p = null;
        if (polygon == null || (p = polygon.get()) == null) {
            p = new Polygon(
                    new int[] {p0.x, p1.x, p2.x, p3.x},
                    new int[] {p0.y, p1.y, p2.y, p3.y},
                    4);
            polygon = new SoftReference<>(p);
        }
        return p;
    }
}

package dk.martinu.opti;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import dk.martinu.opti.geom.Tetragon;

public class Sequence {

    protected final List<Glyph> glyphs;
    protected final Tetragon area;
    protected final Color color;

    public Sequence(final Collection<Glyph> glyphs, final Tetragon area, final Color color) {
        this.glyphs = List.copyOf(glyphs);
        this.area = Objects.requireNonNull(area, "area is null");
        this.color = Objects.requireNonNull(color, "color is null");
    }

    public Tetragon getArea() {
        return area;
    }

    public List<Glyph> getGlyphs() {
        return glyphs;
    }

    public void paint(final Graphics g) {
        g.setColor(color);
        g.fillPolygon(area.getPolygon());
        for(Glyph glyph : glyphs)
            glyph.paint(g);
    }
}

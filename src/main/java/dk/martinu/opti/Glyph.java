package dk.martinu.opti;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Objects;

import dk.martinu.opti.geom.Rectangle;

public class Glyph {

    protected final Rectangle bounds;
    protected final Color color;

    public Glyph(final Rectangle bounds, final Color color) {
        this.bounds = Objects.requireNonNull(bounds, "bounds is null");
        this.color = Objects.requireNonNull(color, "color is null");
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void paint(final Graphics g) {
        g.setColor(color);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}

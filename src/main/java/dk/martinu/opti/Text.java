package dk.martinu.opti;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

import dk.martinu.opti.geom.Rectangle;

public class Text {

    protected final BufferedImage src;
    protected final List<Line> lines;
    protected final String script;
    protected final Rectangle bounds;
    protected final Color color;

    protected Text(final BufferedImage source, final Collection<Line> lines, final String script, final Rectangle bounds, final Color color) {
        src = Objects.requireNonNull(source, "source is null");
        this.lines = List.copyOf(lines);
        this.script = Objects.requireNonNull(script, "script is null");
        this.bounds = Objects.requireNonNull(bounds, "bounds is null");
        this.color = Objects.requireNonNull(color, "color is null");

    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getScript() {
        return script;
    }

    public List<Line> getLines() {
        return lines;
    }

    public BufferedImage getSource() {
        return src;
    }

    public void paint(final Graphics g) {
        g.setColor(color);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        for (Line line : getLines())
            line.paint(g);
    }
}

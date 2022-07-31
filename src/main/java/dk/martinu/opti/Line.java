package dk.martinu.opti;

import java.awt.*;
import java.util.*;
import java.util.List;

import dk.martinu.opti.geom.Tetragon;

public class Line {

    protected final List<Sequence> sequences;
    protected final Tetragon area;
    protected final Color color;

    public Line(final Collection<Sequence> sequences, final Tetragon area, final Color color) {
        this.sequences = List.copyOf(sequences);
        this.area = Objects.requireNonNull(area, "area is null");
        this.color = Objects.requireNonNull(color, "color is null");
    }

    public Tetragon getArea() {
        return area;
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    public void paint(final Graphics g) {
        g.setColor(color);
        g.fillPolygon(area.getPolygon());
        for(Sequence sequence : sequences)
            sequence.paint(g);
    }
}

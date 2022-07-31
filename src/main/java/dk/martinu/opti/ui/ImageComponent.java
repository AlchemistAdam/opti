package dk.martinu.opti.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class ImageComponent extends JComponent {

    protected BufferedImage source = null;
    protected float zoom = 1.0f;

    public ImageComponent() {
        setVisible(false);
    }

    public BufferedImage getSource() {
        return source;
    }

    public float getZoom() {
        return zoom;
    }

    public void setSource(final BufferedImage source) {
        if ((this.source = source) != null) {
            setPreferredSize(new Dimension((int) (source.getWidth() * zoom), (int) (source.getHeight() * zoom)));
            setVisible(true);
        }
        else {
            setZoom(1.0f);
            setPreferredSize(new Dimension(0, 0));
            setVisible(false);
        }
        repaint();
    }

    public void setZoom(final float zoom) {
        this.zoom = zoom;
        if (source != null)
            setPreferredSize(new Dimension((int) (source.getWidth() * zoom), (int) (source.getHeight() * zoom)));
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (source != null)
            g.drawImage(source,
                    0,
                    0,
                    (int) (source.getWidth() * zoom),
                    (int) (source.getHeight() * zoom),
                    null);
    }
}

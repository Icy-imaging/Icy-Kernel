/**
 * 
 */
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Simple image overlay class.
 */
public class ImageOverlay extends Overlay
{
    public Image image;

    public ImageOverlay(String name, Image image)
    {
        super(name, OverlayPriority.IMAGE_NORMAL);

        this.image = image;
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        if (g != null)
            g.drawImage(image, 0, 0, null);
    }
}

/**
 * 
 */
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * This class is used to provide backward compatibility with the {@link Painter} interface.
 * 
 * @author Stephane
 */
@SuppressWarnings({"deprecation", "javadoc"})
public class OverlayWrapper extends Overlay
{
    private final Painter painter;

    public OverlayWrapper(Painter painter, String name)
    {
        super(name);

        this.painter = painter;
    }

    public Painter getPainter()
    {
        return painter;
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        painter.paint(g, sequence, canvas);
    }

    @Override
    public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.mouseClick(e, imagePoint.toPoint2D(), canvas);
        else
            painter.mouseClick(e, null, canvas);
    }

    @Override
    public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.mousePressed(e, imagePoint.toPoint2D(), canvas);
        else
            painter.mousePressed(e, null, canvas);
    }

    @Override
    public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.mouseReleased(e, imagePoint.toPoint2D(), canvas);
        else
            painter.mouseReleased(e, null, canvas);
    }

    @Override
    public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.mouseMove(e, imagePoint.toPoint2D(), canvas);
        else
            painter.mouseMove(e, null, canvas);
    }

    @Override
    public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.mouseDrag(e, imagePoint.toPoint2D(), canvas);
        else
            painter.mouseDrag(e, null, canvas);
    }

    @Override
    public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.keyPressed(e, imagePoint.toPoint2D(), canvas);
        else
            painter.keyPressed(e, null, canvas);
    }

    @Override
    public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (imagePoint != null)
            painter.keyReleased(e, imagePoint.toPoint2D(), canvas);
        else
            painter.keyReleased(e, null, canvas);
    }
}

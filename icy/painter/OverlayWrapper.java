/**
 * 
 */
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

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
    public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.mouseClick(e, imagePoint, canvas);
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        painter.paint(g, sequence, canvas);
    }

    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.mousePressed(e, imagePoint, canvas);
    }

    @Override
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.mouseReleased(e, imagePoint, canvas);
    }

    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.mouseMove(e, imagePoint, canvas);
    }

    @Override
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.mouseDrag(e, imagePoint, canvas);
    }

    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.keyPressed(e, imagePoint, canvas);
    }

    @Override
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        painter.keyReleased(e, imagePoint, canvas);
    }
}

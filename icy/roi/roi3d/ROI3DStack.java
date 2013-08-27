package icy.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.painter.VtkPainter;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.rectangle.Rectangle3D;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract class defining a generic 3D ROI as a stack of individual 2D ROI slices.
 * 
 * @author Alexandre Dufour
 * @author Stephane Dallongeville
 * @param <R>
 *        the type of 2D ROI for each slice of this 3D ROI
 */
public abstract class ROI3DStack<R extends ROI2D> extends ROI3D implements Iterable<R>
{
    public static final String PROPERTY_USECHILDCOLOR = "useChildColor";

    protected final TreeMap<Integer, R> slices = new TreeMap<Integer, R>();

    protected final Class<R> roiClass;
    protected boolean useChildOverlayProperties;

    /**
     * Creates a new 3D ROI based on the given 2D ROI type.
     */
    public ROI3DStack(Class<R> roiClass)
    {
        super();

        this.roiClass = roiClass;
        useChildOverlayProperties = false;
    }

    /**
     * Create a new empty 2D ROI slice.
     */
    protected R createSlice()
    {
        try
        {
            return roiClass.newInstance();
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true, true);
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the ROI directly uses the 2D slice draw properties and
     * <code>false</code> if it uses the global 3D ROI draw properties (as the color for instance).
     */
    public boolean getUseChildOverlayProperties()
    {
        return useChildOverlayProperties;
    }

    /**
     * Set to <code>true</code> if you want to directly use the 2D draw properties and
     * <code>false</code> to keep the global 3D ROI draw properties (as the color for instance).
     * 
     * @see #setColor(int, Color)
     */
    public void setUseChildOverlayProperties(boolean value)
    {
        if (useChildOverlayProperties != value)
        {
            useChildOverlayProperties = value;
            propertyChanged(PROPERTY_USECHILDCOLOR);
            // need to redraw it
            getOverlay().painterChanged();
        }
    }

    @Override
    public void setC(int value)
    {
        super.setC(value);

        for (R slice : slices.values())
            slice.setC(value);
    }

    /**
     * Set the painter color for the specified ROI slice.
     * 
     * @see #setUseChildOverlayProperties(boolean)
     */
    public void setColor(int z, Color value)
    {
        final ROI2D slice = getSlice(z, false);

        if (slice != null)
            slice.setColor(value);
    }

    @Override
    public void setColor(Color value)
    {
        super.setColor(value);

        for (R slice : slices.values())
            slice.setColor(value);
    }

    @Override
    public void setT(int value)
    {
        super.setT(value);

        for (R slice : slices.values())
            slice.setT(value);
    }

    @Override
    public void setStroke(double value)
    {
        super.setStroke(value);

        for (R slice : slices.values())
            slice.setStroke(value);
    }

    /**
     * @return The size of this ROI stack along Z. Note that the returned value indicates the
     *         difference between upper and lower bounds of this ROI, but doesn't guarantee that all
     *         slices in-between exist ({@link #getSlice(int, boolean)} may still return
     *         <code>null</code>.
     */
    public int getSizeZ()
    {
        if (slices.isEmpty())
            return 0;

        return (slices.lastKey().intValue() - slices.firstKey().intValue()) + 1;
    }

    /**
     * Returns the ROI slice at given Z position.
     */
    public R getSlice(int z)
    {
        return getSlice(z, false);
    }

    /**
     * Returns the ROI slice at given Z position.
     */
    public R getSlice(int z, boolean createIfNull)
    {
        R roi = slices.get(Integer.valueOf(z));

        if ((roi == null) && createIfNull)
        {
            roi = createSlice();
            if (roi != null)
                setSlice(z, roi);
        }

        return roi;
    }

    /**
     * Sets the slice for the given z position
     */
    protected void setSlice(int z, R roi2d)
    {
        // add the new one
        roi2d.beginUpdate();

        roi2d.setColor(getColor());
        roi2d.setStroke(getStroke());
        roi2d.setZ(z);
        roi2d.setT(getT());
        roi2d.setC(getC());

        roi2d.endUpdate();

        slices.put(Integer.valueOf(z), roi2d);
    }

    /**
     * Removes slice at the given z position and returns it.
     */
    protected R removeSlice(int z)
    {
        // remove the current slice (if any)
        return slices.remove(Integer.valueOf(z));
    }

    @Override
    public Rectangle3D computeBounds3D()
    {
        Rectangle2D xyBounds = new Rectangle2D.Double();

        for (R slice : slices.values())
            xyBounds.add(slice.getBounds2D());

        final int z;
        final int sizeZ;

        if (!slices.isEmpty())
        {
            z = slices.firstKey().intValue();
            sizeZ = (slices.lastKey().intValue() - z) + 1;
        }
        else
        {
            z = 0;
            sizeZ = 0;
        }

        return new Rectangle3D.Double(xyBounds.getX(), xyBounds.getY(), z, xyBounds.getWidth(), xyBounds.getHeight(),
                sizeZ);
    }

    @Override
    public boolean contains(double x, double y, double z)
    {
        final R roi2d = getSlice((int) z, false);

        if (roi2d != null)
            return roi2d.contains(x, y);

        return false;
    }

    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        // TODO
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        // TODO
        return false;
    }

    @Override
    public double getPerimeter()
    {
        // perimeter = first slice volume + inter slices perimeter + last slice volume
        double perimeter = 0;

        if (slices.size() <= 2)
        {
            for (R slice : slices.values())
                perimeter += slice.getVolume();
        }
        else
        {
            perimeter = slices.firstEntry().getValue().getVolume();

            for (R slice : slices.subMap(Integer.valueOf(1), slices.lastKey()).values())
                perimeter += slice.getPerimeter();

            perimeter += slices.lastEntry().getValue().getVolume();
        }

        return perimeter;
    }

    @Override
    public double getVolume()
    {
        double volume = 0;

        for (R slice : slices.values())
            volume += slice.getVolume();

        return volume;
    }

    @Override
    public Iterator<R> iterator()
    {
        return slices.values().iterator();
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            // we don't need to save the 2D ROI class as the parent class already do it

            for (Element e : XMLUtil.getElements(node, "slice"))
            {
                // faster than using complete XML serialization
                final R slice = createSlice();

                // error while reloading the ROI from XML
                if ((slice == null) || !slice.loadFromXML(e))
                    return false;

                setSlice(slice.getZ(), slice);
            }
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        for (R slice : slices.values())
        {
            Element sliceNode = XMLUtil.addElement(node, "slice");

            if (!slice.saveToXML(sliceNode))
                return false;
        }

        return true;
    }

    public abstract class ROI3DStackPainter extends ROIPainter implements VtkPainter
    {
        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                if (canvas instanceof IcyCanvas3D)
                {
                    // TODO

                }
                else if (canvas instanceof IcyCanvas2D)
                {
                    // forward event to current slice
                    final int z = canvas.getPositionZ();

                    if (z >= 0)
                    {
                        final R slice = getSlice(z, false);

                        if (slice != null)
                            slice.getOverlay().paint(g, sequence, canvas);
                    }
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.keyPressed(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().keyPressed(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.keyReleased(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().keyReleased(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseEntered(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseEntered(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseExited(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseExited(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseMove(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseMove(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseDrag(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseDrag(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mousePressed(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mousePressed(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseReleased(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseReleased(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseClick(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseClick(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseWheelMoved(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final int z = canvas.getPositionZ();

                if (z >= 0)
                {
                    final R slice = getSlice(z, false);

                    if (slice != null)
                        slice.getOverlay().mouseWheelMoved(e, imagePoint, canvas);
                }
            }
        }
    }
}

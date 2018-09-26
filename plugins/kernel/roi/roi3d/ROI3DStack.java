/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.kernel.roi.roi3d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.canvas.IcyCanvas;
import icy.painter.OverlayEvent;
import icy.painter.OverlayListener;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI2D.ROI2DPainter;
import icy.roi.ROI3D;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.util.XMLUtil;

/**
 * Base class defining a generic 3D ROI as a stack of individual 2D ROI slices.
 * 
 * @author Alexandre Dufour
 * @author Stephane Dallongeville
 * @param <R>
 *        the type of 2D ROI for each slice of this 3D ROI
 */
public class ROI3DStack<R extends ROI2D> extends ROI3D implements ROIListener, OverlayListener, Iterable<R>
{
    /**
     * @deprecated this property does not exist anymore
     */
    @Deprecated
    public static final String PROPERTY_USECHILDCOLOR = "useChildColor";

    protected final TreeMap<Integer, R> slices = new TreeMap<Integer, R>();

    protected final Class<? extends R> roiClass;
    protected Semaphore modifyingSlice;
    protected double translateZ;

    /**
     * Creates a new 3D ROI based on the given 2D ROI type.
     */
    public ROI3DStack(Class<? extends R> roiClass)
    {
        super();

        this.roiClass = roiClass;
        modifyingSlice = new Semaphore(1);
        translateZ = 0d;
    }

    @Override
    public String getDefaultName()
    {
        return "ROI2D stack";
    }

    @Override
    protected ROIPainter createPainter()
    {
        return new ROI3DStackPainter();
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
     * Returns <code>true</code> if the ROI directly uses the 2D slice color draw property and <code>false</code> if it
     * uses the global 3D ROI color draw property.
     */
    @SuppressWarnings("unchecked")
    public boolean getUseChildColor()
    {
        return ((ROI3DStackPainter) getOverlay()).getUseChildColor();
    }

    /**
     * Set to <code>true</code> if you want to directly use the 2D slice color draw property and <code>false</code> to
     * keep the global 3D ROI color draw property.
     * 
     * @see #setColor(int, Color)
     */
    @SuppressWarnings("unchecked")
    public void setUseChildColor(boolean value)
    {
        ((ROI3DStackPainter) getOverlay()).setUseChildColor(value);
    }

    /**
     * Set the painter color for the specified ROI slice.
     * 
     * @see #setUseChildColor(boolean)
     */
    @SuppressWarnings("unchecked")
    public void setColor(int z, Color value)
    {
        ((ROI3DStackPainter) getOverlay()).setColor(z, value);
    }

    @Override
    public void setCreating(boolean value)
    {
        beginUpdate();
        try
        {
            super.setCreating(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setCreating(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setReadOnly(boolean value)
    {
        beginUpdate();
        try
        {
            super.setReadOnly(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setReadOnly(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setFocused(boolean value)
    {
        beginUpdate();
        try
        {
            super.setFocused(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setFocused(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setSelected(boolean value)
    {
        beginUpdate();
        try
        {
            super.setSelected(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setSelected(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setName(String value)
    {
        beginUpdate();
        try
        {
            super.setName(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setName(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setT(int value)
    {
        beginUpdate();
        try
        {
            super.setT(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setT(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setC(int value)
    {
        beginUpdate();
        try
        {
            super.setC(value);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.setC(value);
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Returns <code>true</code> if the ROI stack is empty.
     */
    @Override
    public boolean isEmpty()
    {
        return slices.isEmpty();
    }

    /**
     * @return The size of this ROI stack along Z.<br>
     *         Note that the returned value indicates the difference between upper and lower bounds
     *         of this ROI, but doesn't guarantee that all slices in-between exist ( {@link #getSlice(int)} may still
     *         return <code>null</code>.<br>
     */
    public int getSizeZ()
    {
        synchronized (slices)
        {
            if (slices.isEmpty())
                return 0;

            return (slices.lastKey().intValue() - slices.firstKey().intValue()) + 1;
        }
    }

    /**
     * Returns the ROI slice at given Z position.
     */
    public R getSlice(int z)
    {
        return slices.get(Integer.valueOf(z));
    }

    /**
     * Returns the ROI slice at given Z position.
     */
    public R getSlice(int z, boolean createIfNull)
    {
        R result = getSlice(z);

        if ((result == null) && createIfNull)
        {
            result = createSlice();
            if (result != null)
                setSlice(z, result);
        }

        return result;
    }

    /**
     * Sets the ROI slice for the given Z position.
     */
    public void setSlice(int z, R roi2d)
    {
        // nothing to do
        if (getSlice(z) == roi2d)
            return;

        // remove previous
        removeSlice(z);

        if (roi2d != null)
        {
            roi2d.beginUpdate();
            try
            {
                // set Z, T and C position
                roi2d.setZ(z);
                roi2d.setT(getT());
                roi2d.setC(getC());
            }
            finally
            {
                roi2d.endUpdate();
            }

            // listen events from this ROI and its overlay
            roi2d.addListener(this);
            roi2d.getOverlay().addOverlayListener(this);

            synchronized (slices)
            {
                // set new slice
                slices.put(Integer.valueOf(z), roi2d);
            }
        }

        // notify ROI changed
        roiChanged(true);
    }

    /**
     * Removes slice at the given Z position and returns it.
     */
    public R removeSlice(int z)
    {
        final R result;

        synchronized (slices)
        {
            // remove the current slice (if any)
            result = slices.remove(Integer.valueOf(z));
        }

        // remove listeners
        if (result != null)
        {
            result.removeListener(this);
            result.getOverlay().removeOverlayListener(this);

            // notify ROI changed
            roiChanged(true);
        }

        return result;
    }

    /**
     * Removes all slices.
     */
    public void clear()
    {
        // nothing to do
        if (isEmpty())
            return;

        synchronized (slices)
        {
            for (R slice : slices.values())
            {
                slice.removeListener(this);
                slice.getOverlay().removeOverlayListener(this);
            }

            slices.clear();
        }

        roiChanged(true);
    }

    /**
     * Add the specified {@link ROI3DStack} content to this ROI3DStack
     */
    public void add(ROI3DStack<R> roi) throws UnsupportedOperationException
    {
        beginUpdate();
        try
        {
            synchronized (slices)
            {
                for (Entry<Integer, R> entry : roi.slices.entrySet())
                    add(entry.getKey().intValue(), entry.getValue());
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Exclusively add the specified {@link ROI3DStack} content to this ROI3DStack
     */
    public void exclusiveAdd(ROI3DStack<R> roi) throws UnsupportedOperationException
    {
        beginUpdate();
        try
        {
            synchronized (slices)
            {
                for (Entry<Integer, R> entry : roi.slices.entrySet())
                    exclusiveAdd(entry.getKey().intValue(), entry.getValue());
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Process intersection of the specified {@link ROI3DStack} with this ROI3DStack.
     */
    public void intersect(ROI3DStack<R> roi) throws UnsupportedOperationException
    {
        beginUpdate();
        try
        {
            synchronized (slices)
            {
                final Set<Integer> keys = roi.slices.keySet();
                final Set<Integer> toRemove = new HashSet<Integer>();

                // remove slices which are not contained
                for (Integer key : slices.keySet())
                    if (!keys.contains(key))
                        toRemove.add(key);

                // do remove first
                for (Integer key : toRemove)
                    removeSlice(key.intValue());

                // then process intersection
                for (Entry<Integer, R> entry : roi.slices.entrySet())
                    intersect(entry.getKey().intValue(), entry.getValue());
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Remove the specified {@link ROI3DStack} from this ROI3DStack
     */
    public void subtract(ROI3DStack<R> roi) throws UnsupportedOperationException
    {
        beginUpdate();
        try
        {
            synchronized (slices)
            {
                for (Entry<Integer, R> entry : roi.slices.entrySet())
                    subtract(entry.getKey().intValue(), entry.getValue());
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public ROI add(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;

            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC()))
            {
                if (this.getClass().isInstance(roi3d))
                {
                    add((ROI3DStack) roi3d);
                    return this;
                }
            }
        }
        else if (roiClass.isInstance(roi))
        {
            final ROI2D roi2d = (ROI2D) roi;

            // only if on same position
            if ((roi2d.getZ() != -1) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
            {
                try
                {
                    add(roi2d.getZ(), (R) roi2d);
                    return this;
                }
                catch (UnsupportedOperationException e)
                {
                    // not supported, try generic method instead
                    return super.add(roi, allowCreate);
                }
            }
        }

        return super.add(roi, allowCreate);
    }

    @Override
    public ROI intersect(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;

            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC()))
            {
                if (this.getClass().isInstance(roi3d))
                {
                    intersect((ROI3DStack) roi3d);
                    return this;
                }
            }
            else if (roiClass.isInstance(roi))
            {
                final ROI2D roi2d = (ROI2D) roi;

                // only if on same position
                if ((roi2d.getZ() != -1) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
                {
                    try
                    {
                        intersect(roi2d.getZ(), (R) roi2d);
                        return this;
                    }
                    catch (UnsupportedOperationException e)
                    {
                        // not supported, try generic method instead
                        return super.intersect(roi, allowCreate);
                    }
                }
            }
        }

        return super.intersect(roi, allowCreate);
    }

    @Override
    public ROI exclusiveAdd(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;

            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC()))
            {
                if (this.getClass().isInstance(roi3d))
                {
                    exclusiveAdd((ROI3DStack) roi3d);
                    return this;
                }
            }
            else if (roiClass.isInstance(roi))
            {
                final ROI2D roi2d = (ROI2D) roi;

                // only if on same position
                if ((roi2d.getZ() != -1) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
                {
                    try
                    {
                        exclusiveAdd(roi2d.getZ(), (R) roi2d);
                        return this;
                    }
                    catch (UnsupportedOperationException e)
                    {
                        // not supported, try generic method instead
                        return super.add(roi, allowCreate);
                    }
                }
            }
        }

        return super.add(roi, allowCreate);
    }

    @Override
    public ROI subtract(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;

            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC()))
            {
                if (this.getClass().isInstance(roi3d))
                {
                    subtract((ROI3DStack<R>) roi3d);
                    return this;
                }
            }
            else if (roiClass.isInstance(roi))
            {
                final ROI2D roi2d = (ROI2D) roi;

                // only if on same position
                if ((roi2d.getZ() != -1) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
                {
                    try
                    {
                        subtract(roi2d.getZ(), (R) roi2d);
                        return this;
                    }
                    catch (UnsupportedOperationException e)
                    {
                        // not supported, try generic method instead
                        return super.subtract(roi, allowCreate);
                    }
                }
            }
        }

        return super.subtract(roi, allowCreate);
    }

    /**
     * Adds content of specified <code>ROI</code> slice into the <code>ROI</code> slice at given Z position.
     * The resulting content of this <code>ROI</code> will include the union of both ROI's contents.<br>
     * If no slice was present at the specified Z position then the method is equivalent to
     * {@link #setSlice(int, ROI2D)}
     * 
     * @param z
     *        the position where the slice must be merged
     * @param roiSlice
     *        the 2D ROI to merge
     * @throws UnsupportedOperationException
     *         if the given ROI slice cannot be added to this ROI
     */
    public void add(int z, R roiSlice)
    {
        if (roiSlice == null)
            return;

        final R currentSlice = getSlice(z);
        final ROI newSlice;

        // merge both slice
        if (currentSlice != null)
        {
            // we need to modify the Z, T and C position so we do the merge correctly
            roiSlice.setZ(z);
            roiSlice.setT(getT());
            roiSlice.setC(getC());
            // do ROI union
            newSlice = currentSlice.add(roiSlice, true);

            // check the resulting ROI is the same type
            if (!newSlice.getClass().isInstance(currentSlice))
                throw new UnsupportedOperationException("Can't add the result of the merge operation on 2D slice " + z
                        + ": " + newSlice.getClassName());
        }
        else
            // get a copy
            newSlice = roiSlice.getCopy();

        // set slice
        setSlice(z, (R) newSlice);
    }

    /**
     * Sets the content of the <code>ROI</code> slice at given Z position to be the union of its current content and the
     * content of the specified <code>ROI</code>, minus their intersection.
     * The resulting <code>ROI</code> will include only content that were contained in either this <code>ROI</code> or
     * in the specified <code>ROI</code>, but not in both.<br>
     * If no slice was present at the specified Z position then the method is equivalent to
     * {@link #setSlice(int, ROI2D)}
     * 
     * @param z
     *        the position where the slice must be merged
     * @param roiSlice
     *        the 2D ROI to merge
     * @throws UnsupportedOperationException
     *         if the given ROI slice cannot be exclusively added to this ROI
     */
    public void exclusiveAdd(int z, R roiSlice)
    {
        if (roiSlice == null)
            return;

        final R currentSlice = getSlice(z);
        final ROI newSlice;

        // merge both slice
        if (currentSlice != null)
        {
            // we need to modify the Z, T and C position so we do the merge correctly
            roiSlice.setZ(z);
            roiSlice.setT(getT());
            roiSlice.setC(getC());
            // do ROI exclusive union
            newSlice = currentSlice.exclusiveAdd(roiSlice, true);

            // check the resulting ROI is same type
            if (!newSlice.getClass().isInstance(currentSlice))
                throw new UnsupportedOperationException("Can't add the result of the merge operation on 2D slice " + z
                        + ": " + newSlice.getClassName());
        }
        else
            // get a copy
            newSlice = roiSlice.getCopy();

        if (newSlice.isEmpty())
            removeSlice(z);
        else
            setSlice(z, (R) newSlice);
    }

    /**
     * Sets the content of the <code>ROI</code> slice at given Z position to the intersection of
     * its current content and the content of the specified <code>ROI</code>.
     * The resulting ROI will include only contents that were contained in both ROI.<br>
     * If no slice was present at the specified Z position then the method does nothing.
     * 
     * @param z
     *        the position where the slice must be merged
     * @param roiSlice
     *        the 2D ROI to merge
     * @throws UnsupportedOperationException
     *         if the given ROI slice cannot be intersected with this ROI
     */
    public void intersect(int z, R roiSlice)
    {
        // better to throw an exception here than removing slice
        if (roiSlice == null)
            throw new IllegalArgumentException("Cannot intersect an empty slice in a 3D ROI");

        final R currentSlice = getSlice(z);

        // merge both slice
        if (currentSlice != null)
        {
            // we need to modify the Z, T and C position so we do the merge correctly
            roiSlice.setZ(z);
            roiSlice.setT(getT());
            roiSlice.setC(getC());
            // do ROI intersection
            final ROI newSlice = currentSlice.intersect(roiSlice, true);

            // check the resulting ROI is same type
            if (!newSlice.getClass().isInstance(currentSlice))
                throw new UnsupportedOperationException("Can't add the result of the merge operation on 2D slice " + z
                        + ": " + newSlice.getClassName());

            if (newSlice.isEmpty())
                removeSlice(z);
            else
                setSlice(z, (R) newSlice);
        }
    }

    /**
     * Subtract the specified <code>ROI</code> content from the <code>ROI</code> slice at given Z position.<br>
     * If no slice was present at the specified Z position then the method does nothing.
     * 
     * @param z
     *        the position where the subtraction should be done
     * @param roiSlice
     *        the 2D ROI to subtract from Z slice
     * @throws UnsupportedOperationException
     *         if the given ROI slice cannot be subtracted from this ROI
     */
    public void subtract(int z, R roiSlice) throws UnsupportedOperationException
    {
        if (roiSlice == null)
            return;

        final R currentSlice = getSlice(z);

        // merge both slice
        if (currentSlice != null)
        {
            // we need to modify the Z, T and C position so we do the merge correctly
            roiSlice.setZ(z);
            roiSlice.setT(getT());
            roiSlice.setC(getC());
            // do ROI subtraction
            final ROI newSlice = currentSlice.subtract(roiSlice, true);

            // check the resulting ROI is same type
            if (!newSlice.getClass().isInstance(currentSlice))
                throw new UnsupportedOperationException("Can't add the result of the merge operation on 2D slice " + z
                        + ": " + newSlice.getClassName());

            if (newSlice.isEmpty())
                removeSlice(z);
            else
                setSlice(z, (R) newSlice);
        }
    }

    /**
     * Called when a ROI slice has changed.
     */
    protected void sliceChanged(ROIEvent event)
    {
        if (modifyingSlice.availablePermits() <= 0)
            return;

        final ROI source = event.getSource();

        switch (event.getType())
        {
            case ROI_CHANGED:
                // position change of a slice can change global bounds --> transform to 'content changed' event type
                roiChanged(true);
                // roiChanged(StringUtil.equals(event.getPropertyName(), ROI_CHANGED_ALL));
                break;

            case FOCUS_CHANGED:
                setFocused(source.isFocused());
                break;

            case SELECTION_CHANGED:
                setSelected(source.isSelected());
                break;

            case PROPERTY_CHANGED:
                final String propertyName = event.getPropertyName();

                if ((propertyName == null) || propertyName.equals(PROPERTY_READONLY))
                    setReadOnly(source.isReadOnly());
                if ((propertyName == null) || propertyName.equals(PROPERTY_CREATING))
                    setCreating(source.isCreating());
                break;
        }
    }

    /**
     * Called when a ROI slice overlay has changed.
     */
    protected void sliceOverlayChanged(OverlayEvent event)
    {
        switch (event.getType())
        {
            case PAINTER_CHANGED:
                // forward the event to ROI stack overlay
                getOverlay().painterChanged();
                break;

            case PROPERTY_CHANGED:
                // forward the event to ROI stack overlay
                getOverlay().propertyChanged(event.getPropertyName());
                break;
        }
    }

    @Override
    public Rectangle3D computeBounds3D()
    {
        Rectangle2D xyBounds = null;

        synchronized (slices)
        {
            for (R slice : slices.values())
            {
                final Rectangle2D bnd2d = slice.getBounds2D();

                // only add non empty bounds
                if (!bnd2d.isEmpty())
                {
                    if (xyBounds == null)
                        xyBounds = (Rectangle2D) bnd2d.clone();
                    else
                        xyBounds.add(bnd2d);
                }
            }
        }

        // create empty 2D bounds
        if (xyBounds == null)
            xyBounds = new Rectangle2D.Double();

        final int z;
        final int sizeZ;

        if (!slices.isEmpty())
        {
            z = slices.firstKey().intValue();
            sizeZ = getSizeZ();
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
        final R roi2d = getSlice((int) Math.floor(z));

        if (roi2d != null)
            return roi2d.contains(x, y);

        return false;
    }

    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        final Rectangle3D bounds = getBounds3D();

        // easy discard
        if (!bounds.contains(x, y, z, sizeX, sizeY, sizeZ))
            return false;

        final int lim = (int) Math.floor(z + sizeZ);
        for (int zc = (int) Math.floor(z); zc < lim; zc++)
        {
            final R roi2d = getSlice(zc);
            if ((roi2d == null) || !roi2d.contains(x, y, sizeX, sizeY))
                return false;
        }

        return true;
    }

    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        final Rectangle3D bounds = getBounds3D();

        // easy discard
        if (!bounds.intersects(x, y, z, sizeX, sizeY, sizeZ))
            return false;

        final int lim = (int) Math.floor(z + sizeZ);
        for (int zc = (int) Math.floor(z); zc < lim; zc++)
        {
            final R roi2d = getSlice(zc);
            if ((roi2d != null) && roi2d.intersects(x, y, sizeX, sizeY))
                return true;
        }

        return false;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        // default
        return false;
    }

    @Override
    public void unselectAllPoints()
    {
        beginUpdate();
        try
        {
            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.unselectAllPoints();
                }
            }
            finally
            {
                modifyingSlice.release();
            }
        }
        finally
        {
            endUpdate();
        }
    }

    // default approximated implementation for ROI3DStack
    @Override
    public double computeSurfaceArea(Sequence sequence) throws UnsupportedOperationException
    {
        // 3D contour points = first slice points + all slices perimeter + last slice points
        double result = 0;

        synchronized (slices)
        {
            if (!slices.isEmpty())
            {
                final double psx = sequence.getPixelSizeX();
                final double psy = sequence.getPixelSizeY();
                final double psz = sequence.getPixelSizeZ();

                result = slices.firstEntry().getValue().getNumberOfPoints() * psx * psy;
                result += slices.lastEntry().getValue().getNumberOfPoints() * psx * psy;

                for (R slice : slices.values())
                    result += slice.getLength(sequence) * psz;
            }
        }

        return result;
    }

    // default approximated implementation for ROI3DStack
    @Override
    public double computeNumberOfContourPoints()
    {
        // 3D contour points = first slice points + inter slices contour points + last slice points
        double result = 0;

        synchronized (slices)
        {
            if (slices.size() <= 2)
            {
                for (R slice : slices.values())
                    result += slice.getNumberOfPoints();
            }
            else
            {
                final Entry<Integer, R> firstEntry = slices.firstEntry();
                final Entry<Integer, R> lastEntry = slices.lastEntry();
                final Integer firstKey = firstEntry.getKey();
                final Integer lastKey = lastEntry.getKey();

                result = firstEntry.getValue().getNumberOfPoints();

                for (R slice : slices.subMap(firstKey, false, lastKey, false).values())
                    result += slice.getNumberOfContourPoints();

                result += lastEntry.getValue().getNumberOfPoints();
            }
        }

        return result;
    }

    @Override
    public double computeNumberOfPoints()
    {
        double volume = 0;

        synchronized (slices)
        {
            for (R slice : slices.values())
                volume += slice.getNumberOfPoints();
        }

        return volume;
    }

    @Override
    public boolean canTranslate()
    {
        synchronized (slices)
        {
            // only need to test the first entry
            if (!slices.isEmpty())
                return slices.firstEntry().getValue().canTranslate();
        }

        return false;
    }

    /**
     * Translate the stack of specified Z position.
     */
    public void translate(int z)
    {
        // easy optimizations
        if ((z == 0) || isEmpty())
            return;

        synchronized (slices)
        {
            final Map<Integer, R> map = new HashMap<Integer, R>(slices);

            slices.clear();
            for (Entry<Integer, R> entry : map.entrySet())
            {
                final R roi = entry.getValue();
                final int newZ = roi.getZ() + z;

                // // only positive value accepted
                // if (newZ >= 0)
                // {
                roi.setZ(newZ);
                slices.put(Integer.valueOf(newZ), roi);
                // }
            }
        }

        // notify ROI changed
        roiChanged(false);
    }

    @Override
    public void translate(double dx, double dy, double dz)
    {
        beginUpdate();
        try
        {
            translateZ += dz;
            // convert to integer
            final int dzi = (int) translateZ;
            // keep trace of not used floating part
            translateZ -= dzi;

            translate(dzi);

            modifyingSlice.acquireUninterruptibly();
            try
            {
                synchronized (slices)
                {
                    for (R slice : slices.values())
                        slice.translate(dx, dy);
                }
            }
            finally
            {
                modifyingSlice.release();
            }

            // notify ROI changed because we modified slice 'internally'
            if ((dx != 0d) || (dy != 0d))
                roiChanged(false);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, boolean inclusive)
    {
        final R roi2d = getSlice(z);

        if (roi2d != null)
            return roi2d.getBooleanMask(x, y, width, height, inclusive);

        return new boolean[width * height];
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, boolean inclusive)
    {
        final R roi2d = getSlice(z);

        if (roi2d != null)
            return roi2d.getBooleanMask(inclusive);

        return new BooleanMask2D(new Rectangle(), new boolean[0]);
    }

    // called when one of the slice ROI changed
    @Override
    public void roiChanged(ROIEvent event)
    {
        // propagate children change event
        sliceChanged(event);
    }

    // called when one of the slice ROI overlay changed
    @Override
    public void overlayChanged(OverlayEvent event)
    {
        // propagate children overlay change event
        sliceOverlayChanged(event);
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
            clear();

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

        synchronized (slices)
        {
            for (R slice : slices.values())
            {
                Element sliceNode = XMLUtil.addElement(node, "slice");

                if (!slice.saveToXML(sliceNode))
                    return false;
            }
        }

        return true;
    }

    public class ROI3DStackPainter extends ROI3DPainter
    {
        protected ROIPainter getSliceOverlayForCanvas(IcyCanvas canvas)
        {
            final int z = canvas.getPositionZ();

            // canvas position of -1 mean 3D canvas (all Z visible)
            if (z >= 0)
                return getSliceOverlay(z);

            return null;
        }

        /**
         * Returns the ROI overlay at given Z position.
         */
        protected ROIPainter getSliceOverlay(int z)
        {
            R roi = getSlice(z);

            if (roi != null)
                return roi.getOverlay();

            return null;
        }

        /**
         * @deprecated this property does not exist anymore (always return <code>false</code>)
         */
        @Deprecated
        public boolean getUseChildColor()
        {
            return false;
        }

        /**
         * @deprecated this property does not exist anymore
         */
        @Deprecated
        public void setUseChildColor(boolean value)
        {
            //
        }

        /**
         * Set the painter color for the specified ROI slice.
         * 
         * @see #setUseChildColor(boolean)
         */
        public void setColor(int z, Color value)
        {
            final ROIPainter sliceOverlay = getSliceOverlay(z);

            if (sliceOverlay != null)
            {
                modifyingSlice.acquireUninterruptibly();
                try
                {
                    sliceOverlay.setColor(value);
                }
                finally
                {
                    modifyingSlice.release();
                }
            }
        }

        @Override
        public void setColor(Color value)
        {
            beginUpdate();
            try
            {
                super.setColor(value);

                if (!getUseChildColor())
                {
                    modifyingSlice.acquireUninterruptibly();
                    try
                    {
                        synchronized (slices)
                        {
                            for (R slice : slices.values())
                                slice.getOverlay().setColor(value);
                        }
                    }
                    finally
                    {
                        modifyingSlice.release();
                    }
                }
            }
            finally
            {
                endUpdate();
            }
        }

        @Override
        public void setOpacity(float value)
        {
            beginUpdate();
            try
            {
                super.setOpacity(value);

                modifyingSlice.acquireUninterruptibly();
                try
                {
                    synchronized (slices)
                    {
                        for (R slice : slices.values())
                            slice.getOverlay().setOpacity(value);
                    }
                }
                finally
                {
                    modifyingSlice.release();
                }
            }
            finally
            {
                endUpdate();
            }
        }

        @Override
        public void setStroke(double value)
        {
            beginUpdate();
            try
            {
                super.setStroke(value);

                modifyingSlice.acquireUninterruptibly();
                try
                {
                    synchronized (slices)
                    {
                        for (R slice : slices.values())
                            slice.getOverlay().setStroke(value);
                    }
                }
                finally
                {
                    modifyingSlice.release();
                }
            }
            finally
            {
                endUpdate();
            }
        }

        @Override
        public void setShowName(boolean value)
        {
            beginUpdate();
            try
            {
                super.setShowName(value);

                modifyingSlice.acquireUninterruptibly();
                try
                {
                    synchronized (slices)
                    {
                        for (R slice : slices.values())
                            slice.getOverlay().setShowName(value);
                    }
                }
                finally
                {
                    modifyingSlice.release();
                }
            }
            finally
            {
                endUpdate();
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.paint(g, sequence, canvas);
            }
            // use default parent implementation
            else
                super.paint(g, sequence, canvas);
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.keyPressed(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.keyPressed(e, imagePoint, canvas);
        }

        @Override
        public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.keyReleased(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.keyReleased(e, imagePoint, canvas);
        }

        @Override
        public void mouseEntered(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseEntered(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseEntered(e, imagePoint, canvas);
        }

        @Override
        public void mouseExited(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseExited(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseExited(e, imagePoint, canvas);
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseMove(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseMove(e, imagePoint, canvas);
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseDrag(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseDrag(e, imagePoint, canvas);
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mousePressed(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mousePressed(e, imagePoint, canvas);
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseReleased(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseReleased(e, imagePoint, canvas);
        }

        @Override
        public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseClick(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseClick(e, imagePoint, canvas);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay != null)
                    sliceOverlay.mouseWheelMoved(e, imagePoint, canvas);
            }
            // use default parent implementation
            else
                super.mouseWheelMoved(e, imagePoint, canvas);
        }

        @Override
        public void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            // 2D canvas --> use slice implementation if possible
            if ((canvas.getPositionZ() >= 0) && isActiveFor(canvas))
            {
                // forward event to current slice
                final ROIPainter sliceOverlay = getSliceOverlayForCanvas(canvas);

                if (sliceOverlay instanceof ROI2DPainter)
                    ((ROI2DPainter) sliceOverlay).drawROI(g, sequence, canvas);
            }

            // nothing to do...
        }
    }
}

/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.util.EventUtil;
import icy.util.ShapeUtil.ShapeOperation;
import icy.util.XMLUtil;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public abstract class ROI2D extends ROI
{
    /**
     * Return ROI2D of ROI list
     */
    public static ArrayList<ROI2D> getROI2DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI2D> result = new ArrayList<ROI2D>();

        for (ROI roi : rois)
            if (roi instanceof ROI2D)
                result.add((ROI2D) roi);

        return result;
    }

    /**
     * Return ROI2D of ROI list
     */
    public static ROI2D[] getROI2DList(ROI[] rois)
    {
        final ArrayList<ROI2D> result = new ArrayList<ROI2D>();

        for (ROI roi : rois)
            if (roi instanceof ROI2D)
                result.add((ROI2D) roi);

        return result.toArray(new ROI2D[result.size()]);
    }

    /**
     * Merge the specified array of {@link ROI2DShape} with the given {@link ShapeOperation}.<br>
     * 
     * @param rois
     *        ROIs we want to merge.
     * @param operation
     *        {@link ShapeOperation} to apply.
     * @return {@link Area} shape representing the result of the merge operation.
     */
    public static ROI2D merge(ROI2D[] rois, ShapeOperation operation)
    {
        // test if we only have ROI2DShape
        if (ROI.getROIList(rois, ROI2DShape.class).size() == rois.length)
        {
            // convert array
            final ROI2DShape roisShape[] = new ROI2DShape[rois.length];
            System.arraycopy(rois, 0, roisShape, 0, rois.length);
            // we can use the Shape merge
            return ROI2DShape.merge(roisShape, operation);
        }

        // we use a boolean mask
        final ROI2DArea result = new ROI2DArea();

        switch (operation)
        {
            case OR:
                result.setAsBooleanMask(BooleanMask2D.getUnionBooleanMask(rois));
                result.setName("Union");
                break;
            case AND:
                result.setAsBooleanMask(BooleanMask2D.getIntersectBooleanMask(rois));
                result.setName("Intersection");
                break;
            case XOR:
                result.setAsBooleanMask(BooleanMask2D.getExclusiveUnionBooleanMask(rois));
                result.setName("Exclusive union");
                break;
            default:
                result.setName("Merge");
        }

        return result;
    }

    /**
     * Subtract the content of the roi2 from the roi1 and return the result as a new {@link ROI2D}.
     * 
     * @return {@link ROI2D} representing the result of subtraction.
     */
    public static ROI2D subtract(ROI2D roi1, ROI2D roi2)
    {
        if ((roi1 instanceof ROI2DShape) && (roi2 instanceof ROI2DShape))
            return ROI2DShape.subtract((ROI2DShape) roi1, (ROI2DShape) roi2);

        // use ROI2DArea
        final ROI2DArea result = new ROI2DArea(BooleanMask2D.getSubtractionMask(roi1.getBooleanMask(),
                roi2.getBooleanMask()));

        result.setName("Substraction");

        return result;
    }

    /**
     * @deprecated Use {@link ROI2D#subtract(ROI2D, ROI2D)} instead
     */
    @Deprecated
    public static ROI2D substract(ROI2D roi1, ROI2D roi2)
    {
        return subtract(roi1, roi2);
    }

    protected abstract class ROI2DPainter extends ROIPainter
    {
        protected Point2D startDragMousePosition;
        protected Point2D startDragROIPosition;

        public ROI2DPainter()
        {
            super();

            startDragMousePosition = null;
            startDragROIPosition = null;
        }

        protected boolean updateFocus(InputEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            final boolean focused = isOver(canvas, imagePoint);

            setFocused(focused);

            return focused;
        }

        protected boolean updateSelect(InputEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            final boolean selectedPoint = hasSelectedPoint();

            // union selection
            if (EventUtil.isShiftDown(e))
            {
                if (focused)
                {
                    // only if not already selected
                    if (!selected)
                    {
                        setSelected(true, false);
                        return true;
                    }
                }
            }
            else if (EventUtil.isControlDown(e))
            // switch selection
            {
                // inverse state
                if (focused)
                {
                    setSelected(!selected, false);
                    return true;
                }
            }
            else
            // exclusive selection
            {
                // we stay selected when we click on control points
                final boolean newSelected = focused || selectedPoint;

                if (newSelected)
                {
                    // only if not already selected
                    if (!selected)
                    {
                        setSelected(newSelected, true);
                        return true;
                    }
                }
            }

            return false;
        }

        protected boolean updateDrag(InputEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // not dragging --> exit
            if (startDragMousePosition == null)
                return false;

            double dx = imagePoint.getX() - startDragMousePosition.getX();
            double dy = imagePoint.getY() - startDragMousePosition.getY();

            // shift action --> limit to one direction
            if (EventUtil.isShiftDown(e))
            {
                // X drag
                if (Math.abs(dx) > Math.abs(dy))
                    dy = 0;
                // Y drag
                else
                    dx = 0;
            }

            // set new position
            setPosition(new Point2D.Double(startDragROIPosition.getX() + dx, startDragROIPosition.getY() + dy));

            return true;
        }

        @Override
        public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2D.this.beginUpdate();
            try
            {
                // not yet consumed...
                if (!e.isConsumed())
                {
                    // left button action
                    if (EventUtil.isLeftMouseButton(e))
                    {
                        // roi focused (mouse over ROI bounds) ?
                        if (focused)
                        {
                            // update selection
                            updateSelect(e, imagePoint, canvas);
                            // always consume (to enable dragging)
                            e.consume();
                        }
                        // roi selected and no point selected ?
                        else if (selected && !hasSelectedPoint())
                        {
                            if (editable)
                            {
                                // try to add point first
                                if (addPointAt(imagePoint, EventUtil.isControlDown(e)))
                                    e.consume();
                                // else we update selection
                                else if (updateSelect(e, imagePoint, canvas))
                                    e.consume();
                            }
                            else
                            {
                                // just update selection
                                if (updateSelect(e, imagePoint, canvas))
                                    e.consume();
                            }
                        }
                        else
                        {
                            // update selection
                            if (updateSelect(e, imagePoint, canvas))
                                e.consume();
                        }
                    }
                    // right button action
                    else if (EventUtil.isRightMouseButton(e))
                    {
                        // no editable --> no action here
                        if (!editable)
                            return;

                        // only use delete now to remove point

                        // // roi selected ?
                        // if (selected)
                        // {
                        // // try to remove point
                        // if (removePointAt(canvas, imagePoint))
                        // // consume
                        // e.consume();
                        // }
                    }
                }
            }
            finally
            {
                ROI2D.this.endUpdate();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            startDragMousePosition = null;
        }

        @Override
        public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // no editable --> no action here
            if (!editable)
                return;

            ROI2D.this.beginUpdate();
            try
            {
                if (!e.isConsumed())
                {
                    // left button action
                    if (EventUtil.isLeftMouseButton(e))
                    {
                        // roi focused ?
                        if (focused)
                        {
                            // start drag position
                            if (startDragMousePosition == null)
                            {
                                startDragMousePosition = imagePoint;
                                startDragROIPosition = getPosition2D();
                            }

                            updateDrag(e, imagePoint, canvas);

                            // consume event
                            e.consume();
                        }
                    }
                    // right button action
                    else if (EventUtil.isRightMouseButton(e))
                    {
                        // // roi selected ?
                        // if (selected)
                        // {
                        // // try to remove point
                        // if (removePointAt(canvas, imagePoint))
                        // // consume
                        // e.consume();
                        // }
                    }
                }
            }
            finally
            {
                ROI2D.this.endUpdate();
            }

            // update mouse position
            setMousePos(imagePoint);
        }

        @Override
        public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // update focus
            if (!e.isConsumed())
            {
                if (updateFocus(e, imagePoint, canvas))
                    e.consume();
            }

            // update mouse position
            setMousePos(imagePoint);
        }

        @Override
        public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            if (!e.isConsumed())
            {
                // unselect ROI on double click
                if (e.getClickCount() == 2)
                {
                    if (selected)
                    {
                        setSelected(false, true);
                        e.consume();
                    }
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2D.this.beginUpdate();
            try
            {
                // just for the shift key state change
                if (editable)
                    updateDrag(e, imagePoint, canvas);

                if (!e.isConsumed())
                {
                    switch (e.getKeyCode())
                    {
                        case KeyEvent.VK_ESCAPE:
                            // shape selected ? --> unselect the ROI
                            if (selected)
                            {
                                setSelected(false, true);
                                e.consume();
                            }
                            break;

                        case KeyEvent.VK_DELETE:
                        case KeyEvent.VK_BACK_SPACE:
                            if (editable)
                            {
                                // roi selected ?
                                if (selected)
                                {
                                    // remove selected control point if there is one
                                    if (removeSelectedPoint(canvas, imagePoint))
                                        e.consume();
                                    else
                                    {
                                        // else simply delete ROI
                                        ROI2D.this.remove();
                                        e.consume();
                                    }
                                }
                                // roi focused ? --> delete ROI
                                else if (focused)
                                {
                                    ROI2D.this.remove();
                                    e.consume();
                                }
                            }
                            break;
                    }
                }
            }
            finally
            {
                ROI2D.this.endUpdate();
            }
        }

        @Override
        public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // just for the shift key state change
            if (editable)
                updateDrag(e, imagePoint, canvas);
        }
    }

    public static final String ID_Z = "z";
    public static final String ID_T = "t";
    public static final String ID_C = "c";

    /**
     * z coordinate attachment
     */
    protected int z;
    /**
     * t coordinate attachment
     */
    protected int t;
    /**
     * c coordinate attachment
     */
    protected int c;

    public ROI2D()
    {
        super();

        // by default we consider no specific Z, T and C attachment
        z = -1;
        t = -1;
        c = -1;
    }

    @Override
    final public int getDimension()
    {
        return 2;
    }

    /**
     * @return the z
     */
    public int getZ()
    {
        return z;
    }

    /**
     * @param value
     *        the z to set
     */
    public void setZ(int value)
    {
        if (z != value)
        {
            z = value;
            roiChanged();
        }
    }

    /**
     * @return the t
     */
    public int getT()
    {
        return t;
    }

    /**
     * @param value
     *        the t to set
     */
    public void setT(int value)
    {
        if (t != value)
        {
            t = value;
            roiChanged();
        }
    }

    /**
     * @return the c
     */
    public int getC()
    {
        return c;
    }

    /**
     * @param value
     *        the c to set
     */
    public void setC(int value)
    {
        if (c != value)
        {
            c = value;
            roiChanged();
        }
    }

    /**
     * Return true if the ROI is active for the specified canvas.<br>
     * It internally uses the current canvas Z, T, C coordinates and the visible state of the
     * attached layer.
     */
    public boolean isActiveFor(IcyCanvas canvas)
    {
        if (!canvas.isLayersVisible())
            return false;

        final Layer layer = canvas.getLayer(painter);

        if ((layer != null) && layer.isVisible())
            return isActiveFor(canvas.getPositionZ(), canvas.getPositionT(), canvas.getPositionC());

        return false;
    }

    /**
     * Return true if the ROI is active for the specified Z, T, C coordinates
     */
    public boolean isActiveFor(int z, int t, int c)
    {
        return ((this.z == -1) || (this.z == z)) && ((this.t == -1) || (this.t == t))
                && ((this.c == -1) || (this.c == c));
    }

    /**
     * Return true if this ROI support adding new point
     */
    public abstract boolean canAddPoint();

    /**
     * Return true if this ROI support removing point
     */
    public abstract boolean canRemovePoint();

    /**
     * Add a new point at specified position (used to build ROI)
     */
    public abstract boolean addPointAt(Point2D pos, boolean ctrl);

    /**
     * Remove point at specified position (used to build ROI)
     */
    public abstract boolean removePointAt(IcyCanvas canvas, Point2D imagePoint);

    /**
     * Remove selected point at specified position (used to build ROI)
     */
    protected abstract boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint);

    /**
     * Return true if the ROI has a current selected point
     */
    public abstract boolean hasSelectedPoint();

    /**
     * return true if specified point coordinates overlap the ROI<br>
     * Edge overlap only, used for roi manipulation
     */
    public boolean isOver(IcyCanvas canvas, Point2D p)
    {
        return isOver(canvas, p.getX(), p.getY());
    }

    /**
     * return true if specified point coordinates overlap the ROI<br>
     * Edge overlap only, used for roi manipulation
     */
    public abstract boolean isOver(IcyCanvas canvas, double x, double y);

    /**
     * return true if specified point coordinates overlap a ROI (control) point<br>
     * used for roi manipulation
     */
    public boolean isOverPoint(IcyCanvas canvas, Point2D p)
    {
        return isOverPoint(canvas, p.getX(), p.getY());
    }

    /**
     * Return true if specified point coordinates overlap a ROI control point (used for roi
     * manipulation).
     */
    public abstract boolean isOverPoint(IcyCanvas canvas, double x, double y);

    /**
     * Tests if a specified {@link Point2D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point2D</code> to be tested
     * @return <code>true</code> if the specified <code>Point2D</code> is inside the boundary of the
     *         <code>ROI</code>; <code>false</code> otherwise.
     */
    public boolean contains(Point2D p)
    {
        return contains(p.getX(), p.getY());
    }

    /**
     * Tests if the interior of the <code>ROI</code> entirely contains the specified
     * <code>Rectangle2D</code>. The {@code ROI.contains()} method allows a implementation to
     * conservatively return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle2D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the
     * {@code ROI} contains the {@code Rectangle2D}.
     * 
     * @param r
     *        The specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         <code>Rectangle2D</code>; <code>false</code> otherwise or, if the <code>ROI</code>
     *         contains the <code>Rectangle2D</code> and the <code>intersects</code> method returns
     *         <code>true</code> and the containment calculations would be too expensive to perform.
     * @see #contains(double, double, double, double)
     */
    public boolean contains(Rectangle2D r)
    {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the specified coordinates are inside the boundary of the <code>ROI</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @return <code>true</code> if the specified coordinates are inside the <code>ROI</code>
     *         boundary; <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y);

    /**
     * Tests if the interior of the <code>ROI</code> entirely contains the specified rectangular
     * area. All coordinates that lie inside the rectangular area must lie within the
     * <code>ROI</code> for the entire rectangular area to be considered contained within the
     * <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively
     * return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * rectangular area are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the
     * {@code ROI} contains the rectangular area.
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param w
     *        the width of the specified rectangular area
     * @param h
     *        the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         specified rectangular area; <code>false</code> otherwise or, if the <code>ROI</code>
     *         contains the rectangular area and the <code>intersects</code> method returns
     *         <code>true</code> and the containment calculations would be too expensive to perform.
     */
    public abstract boolean contains(double x, double y, double w, double h);

    /**
     * Returns an integer {@link Rectangle} that completely encloses the <code>ROI</code>. Note that
     * there is no guarantee that the returned <code>Rectangle</code> is the smallest bounding box
     * that encloses the <code>ROI</code>, only that the <code>ROI</code> lies entirely within the
     * indicated <code>Rectangle</code>. The returned <code>Rectangle</code> might also fail to
     * completely enclose the <code>ROI</code> if the <code>ROI</code> overflows the limited range
     * of the integer data type. The <code>getBounds2D</code> method generally returns a tighter
     * bounding box due to its greater flexibility in representation.
     * 
     * @return an integer <code>Rectangle</code> that completely encloses the <code>ROI</code>.
     */
    public Rectangle getBounds()
    {
        return getBounds2D().getBounds();
    }

    /**
     * Returns a high precision and more accurate bounding box of the <code>ROI</code> than the
     * <code>getBounds</code> method. Note that there is no guarantee that the returned
     * {@link Rectangle2D} is the smallest bounding box that encloses the <code>ROI</code>, only
     * that the <code>ROI</code> lies entirely within the indicated <code>Rectangle2D</code>. The
     * bounding box returned by this method is usually tighter than that returned by the
     * <code>getBounds</code> method and never fails due to overflow problems since the return value
     * can be an instance of the <code>Rectangle2D</code> that uses double precision values to store
     * the dimensions.
     * 
     * @return an instance of <code>Rectangle2D</code> that is a high-precision bounding box of the
     *         <code>ROI</code>.
     */
    public abstract Rectangle2D getBounds2D();

    /**
     * Returns the top left point of the ROI bounds.<br>
     * Equivalent to :<br>
     * <code>getBounds().getLocation()</code>
     * 
     * @see #getBounds()
     */
    public Point getPosition()
    {
        return getBounds().getLocation();
    }

    /**
     * Returns the top left point of the ROI bounds.<br>
     * Equivalent to :<br>
     * <code>new Point2D.Double(getBounds2D().getX(), getBounds2D().getY())</code>
     * 
     * @see #getBounds2D()
     */
    public Point2D getPosition2D()
    {
        final Rectangle2D r = getBounds2D();
        return new Point2D.Double(r.getX(), r.getY());
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * <code>Rectangle2D</code>. The {@code ROI.intersects()} method allows a {@code ROI}
     * implementation to conservatively return {@code true} when:
     * <ul>
     * <li>there is a high probability that the <code>Rectangle2D</code> and the <code>ROI</code>
     * intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * {@code Rectangle2D} does not intersect the {@code ROI}.
     * 
     * @param r
     *        the specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         specified <code>Rectangle2D</code> intersect, or are both highly likely to intersect
     *         and intersection calculations would be too expensive to perform; <code>false</code>
     *         otherwise.
     * @see #intersects(double, double, double, double)
     */
    public boolean intersects(Rectangle2D r)
    {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * rectangular area. The rectangular area is considered to intersect the <code>ROI</code> if any
     * point is contained in both the interior of the <code>ROI</code> and the specified rectangular
     * area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively
     * return {@code true} when:
     * <ul>
     * <li>there is a high probability that the rectangular area and the <code>ROI</code> intersect,
     * but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * rectangular area does not intersect the {@code ROI}.
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param w
     *        the width of the specified rectangular area
     * @param h
     *        the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         rectangular area intersect, or are both highly likely to intersect and intersection
     *         calculations would be too expensive to perform; <code>false</code> otherwise.
     */
    public abstract boolean intersects(double x, double y, double w, double h);

    /**
     * Get the {@link BooleanMask2D} object representing the roi.<br>
     * It contains the rectangle mask bounds and the associated boolean array mask.<br>
     * if the pixel (x,y) is contained in the roi then result.mask[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result.mask[(y * w) + x] = false
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask2D getBooleanMask(boolean inclusive)
    {
        final Rectangle bounds = getBounds();
        return new BooleanMask2D(bounds, getBooleanMask(bounds, inclusive));
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi.<br>
     * if the pixel (x,y) is contained in the roi then result[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result[(y * w) + x] = false
     * 
     * @param rect
     *        area we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public boolean[] getBooleanMask(Rectangle rect, boolean inclusive)
    {
        return getBooleanMask(rect.x, rect.y, rect.width, rect.height, inclusive);
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi.<br>
     * if the pixel (x,y) is contained in the roi then result[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result[(y * w) + x] = false
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param w
     *        the width of the specified rectangular area
     * @param h
     *        the height of the specified rectangular area
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     * @return the boolean bitmap mask
     */
    public boolean[] getBooleanMask(int x, int y, int w, int h, boolean inclusive)
    {
        final boolean[] result = new boolean[w * h];

        // simple and basic implementation, override it to have better performance
        int offset = 0;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                result[offset] = contains(x + i, y + j, 1, 1);
                if (inclusive)
                    result[offset] |= intersects(x + i, y + j, 1, 1);
                offset++;
            }
        }

        return result;
    }

    /**
     * Get the {@link BooleanMask2D} object representing the roi.<br>
     * It contains the rectangle mask bounds and the associated boolean array mask.<br>
     * if the pixel (x,y) is contained in the roi then result.mask[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result.mask[(y * w) + x] = false
     */
    public BooleanMask2D getBooleanMask()
    {
        return getBooleanMask(false);
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi.<br>
     * if the pixel (x,y) is contained in the roi then result[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result[(y * w) + x] = false
     * 
     * @param rect
     *        area we want to retrieve the boolean mask
     */
    public boolean[] getBooleanMask(Rectangle rect)
    {
        return getBooleanMask(rect, false);
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi.<br>
     * if the pixel (x,y) is contained in the roi then result[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result[(y * w) + x] = false
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param w
     *        the width of the specified rectangular area
     * @param h
     *        the height of the specified rectangular area
     * @return the bitmap mask
     */
    public boolean[] getBooleanMask(int x, int y, int w, int h)
    {
        return getBooleanMask(x, y, w, h, false);
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} instead.
     */
    @Deprecated
    public BooleanMask2D getAsBooleanMask(boolean inclusive)
    {
        return getBooleanMask(inclusive);
    }

    /**
     * @deprecated Use {@link #getBooleanMask(Rectangle, boolean)} instead.
     */
    @Deprecated
    public boolean[] getAsBooleanMask(Rectangle rect, boolean inclusive)
    {
        return getBooleanMask(rect, inclusive);
    }

    /**
     * @deprecated Use {@link #getBooleanMask(int, int, int, int, boolean)} instead.
     */
    @Deprecated
    public boolean[] getAsBooleanMask(int x, int y, int w, int h, boolean inclusive)
    {
        return getBooleanMask(x, y, w, h, inclusive);
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} instead.
     */
    @Deprecated
    public BooleanMask2D getAsBooleanMask()
    {
        return getBooleanMask();
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} instead.
     */
    @Deprecated
    public boolean[] getAsBooleanMask(Rectangle rect)
    {
        return getBooleanMask(rect);
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} instead.
     */
    @Deprecated
    public boolean[] getAsBooleanMask(int x, int y, int w, int h)
    {
        return getBooleanMask(x, y, w, h);
    }

    /**
     * Translate the ROI position by the specified dx and dy
     * 
     * @param dx
     * @param dy
     */
    public abstract void translate(double dx, double dy);

    /**
     * Set the ROI position.<br>
     * This is equivalent to :<br>
     * <code>translate(newPosition - getPosition2D())</code>
     */
    public void setPosition(Point2D newPosition)
    {
        final Point2D oldPos = getPosition2D();
        translate(newPosition.getX() - oldPos.getX(), newPosition.getY() - oldPos.getY());
    }

    @Override
    public boolean contains(double x, double y, double z, double t, double c)
    {
        return contains(x, y) && ((this.z == -1) || (this.z == z)) && ((this.t == -1) || (this.t == t))
                && ((this.c == -1) || (this.c == c));
    }

    @Override
    public boolean intersects(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC)
    {
        if ((sizeX == 0d) || (sizeY == 0d) || (sizeZ == 0d) || (sizeT == 0d) || (sizeC == 0d))
            return false;

        final int zmin = (int) Math.round(z);
        final double zmax = z + sizeZ;
        final int tmin = (int) Math.round(t);
        final double tmax = t + sizeT;
        final int cmin = (int) Math.round(c);
        final double cmax = c + sizeC;

        return intersects(x, y, sizeX, sizeY) && ((this.z == -1) || ((this.z >= zmin) && (this.z < zmax)))
                && ((this.t == -1) || ((this.t >= tmin) && (this.t < tmax)))
                && ((this.c == -1) || ((this.c >= cmin) && (this.c < cmax)));
    }

    /*
     * Generic implementation for ROI2D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double getPerimeter()
    {
        // approximation by using number of point of the edge of boolean mask
        return getBooleanMask(true).getEdgePoints().length;
    }

    /*
     * Generic implementation for ROI2D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double getVolume()
    {
        // approximation by using number of point of boolean mask
        return getBooleanMask(true).getPoints().length;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            setZ(XMLUtil.getElementIntValue(node, ID_Z, -1));
            setT(XMLUtil.getElementIntValue(node, ID_T, -1));
            setC(XMLUtil.getElementIntValue(node, ID_C, -1));
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

        XMLUtil.setElementIntValue(node, ID_Z, getZ());
        XMLUtil.setElementIntValue(node, ID_T, getT());
        XMLUtil.setElementIntValue(node, ID_C, getC());

        return true;
    }

}

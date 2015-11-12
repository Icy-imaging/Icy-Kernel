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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.gui.util.FontUtil;
import icy.preferences.GeneralPreferences;
import icy.roi.edit.PositionROIEdit;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.ShapeUtil.ShapeOperation;
import icy.util.XMLUtil;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.roi.roi2d.ROI2DArea;

public abstract class ROI2D extends ROI
{
    /**
     * Return ROI2D of ROI list.
     */
    public static List<ROI2D> getROI2DList(List<ROI> rois)
    {
        final List<ROI2D> result = new ArrayList<ROI2D>();

        for (ROI roi : rois)
            if (roi instanceof ROI2D)
                result.add((ROI2D) roi);

        return result;
    }

    /**
     * @deprecated Use {@link ROI2D#getROI2DList(List)} instead.
     */
    @Deprecated
    public static ArrayList<ROI2D> getROI2DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI2D> result = new ArrayList<ROI2D>();

        for (ROI roi : rois)
            if (roi instanceof ROI2D)
                result.add((ROI2D) roi);

        return result;
    }

    /**
     * @deprecated Use {@link ROI2D#getROI2DList(List)} instead.
     */
    @Deprecated
    public static ROI2D[] getROI2DList(ROI[] rois)
    {
        final ArrayList<ROI2D> result = new ArrayList<ROI2D>();

        for (ROI roi : rois)
            if (roi instanceof ROI2D)
                result.add((ROI2D) roi);

        return result.toArray(new ROI2D[result.size()]);
    }

    /**
     * @deprecated Use {@link ROIUtil#merge(List, icy.util.ShapeUtil.BooleanOperator)} instead.
     */
    @Deprecated
    public static ROI2D merge(ROI2D[] rois, ShapeOperation operation)
    {
        final List<ROI> list = new ArrayList<ROI>();

        for (ROI2D roi2d : rois)
            list.add(roi2d);

        return (ROI2D) ROIUtil.merge(list, operation.getBooleanOperator());
    }

    /**
     * @deprecated Use {@link ROI#getSubtraction(ROI)} instead.
     */
    @Deprecated
    public static ROI2D substract(ROI2D roi1, ROI2D roi2)
    {
        return subtract(roi1, roi2);
    }

    /**
     * @deprecated Use {@link ROI#getSubtraction(ROI)} instead.
     */
    @Deprecated
    public static ROI2D subtract(ROI2D roi1, ROI2D roi2)
    {
        ROI result = roi1.getSubtraction(roi2);

        if (result instanceof ROI2D)
            return (ROI2D) result;

        // use ROI2DArea then...
        result = new ROI2DArea(BooleanMask2D.getSubtraction(roi1.getBooleanMask(true), roi2.getBooleanMask(true)));

        result.setName("Subtraction");

        return (ROI2D) result;
    }

    public abstract class ROI2DPainter extends ROIPainter
    {
        protected Point2D startDragMousePosition;
        protected Point2D startDragROIPosition;

        public ROI2DPainter()
        {
            super();

            startDragMousePosition = null;
            startDragROIPosition = null;
        }

        protected boolean updateFocus(InputEvent e, Point5D imagePoint, IcyCanvas canvas)
        {
            // test on canvas has already be done, don't do it again
            final boolean focused = isOverEdge(canvas, imagePoint.getX(), imagePoint.getY());

            setFocused(focused);

            return focused;
        }

        protected boolean updateSelect(InputEvent e, Point5D imagePoint, IcyCanvas canvas)
        {
            // nothing to do if the ROI does not have focus
            if (!isFocused())
                return false;

            // union selection
            if (EventUtil.isShiftDown(e))
            {
                // not already selected --> add ROI to selection
                if (!isSelected())
                {
                    setSelected(true);
                    return true;
                }
            }
            else if (EventUtil.isControlDown(e))
            // switch selection
            {
                // inverse state
                setSelected(!isSelected());
                return true;
            }
            else
            // exclusive selection
            {
                // not selected --> exclusive ROI selection
                if (!isSelected())
                {
                    // exclusive selection can fail if we use embedded ROI (as ROIStack)
                    if (!canvas.getSequence().setSelectedROI(ROI2D.this))
                        ROI2D.this.setSelected(true);

                    return true;
                }
            }

            return false;
        }

        protected boolean updateDrag(InputEvent e, Point5D imagePoint, IcyCanvas canvas)
        {
            // not dragging --> exit
            if (startDragMousePosition == null)
                return false;
            if (imagePoint == null)
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

            // needed for undo operation
            final Sequence sequence;
            final Point5D savePosition;

            // get canvas which modify the ROI --> get the sequence
            if (canvas != null)
                sequence = canvas.getSequence();
            else
                sequence = null;

            if (sequence != null)
                savePosition = getPosition5D();
            else
                savePosition = null;

            // set new position
            setPosition2D(new Point2D.Double(startDragROIPosition.getX() + dx, startDragROIPosition.getY() + dy));

            // allow undo as the ROI position has been modified from canvas
            if (savePosition != null)
                // add position change to undo manager
                sequence.addUndoableEdit(new PositionROIEdit(ROI2D.this, savePosition));

            return true;
        }

        @Override
        public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // do parent stuff
            super.keyReleased(e, imagePoint, canvas);

            if (isActiveFor(canvas))
            {
                // check we can do the action
                if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                {
                    // just for the shift key state change
                    if (!isReadOnly())
                        updateDrag(e, imagePoint, canvas);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // do parent stuff
            super.mousePressed(e, imagePoint, canvas);

            // not yet consumed...
            if (!e.isConsumed())
            {
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2D.this.beginUpdate();
                        try
                        {
                            // left button action
                            if (EventUtil.isLeftMouseButton(e))
                            {
                                // update selection
                                if (updateSelect(e, imagePoint, canvas))
                                    e.consume();
                                // always consume when focused to enable dragging
                                else if (isFocused())
                                    e.consume();
                            }
                        }
                        finally
                        {
                            ROI2D.this.endUpdate();
                        }
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // do parent stuff
            super.mouseReleased(e, imagePoint, canvas);

            startDragMousePosition = null;
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // do parent stuff
            super.mouseDrag(e, imagePoint, canvas);

            // not yet consumed and ROI editable...
            if (!e.isConsumed() && !isReadOnly())
            {
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2D.this.beginUpdate();
                        try
                        {
                            // left button action
                            if (EventUtil.isLeftMouseButton(e))
                            {
                                // roi focused ?
                                if (isFocused())
                                {
                                    // start drag position
                                    if (startDragMousePosition == null)
                                    {
                                        startDragMousePosition = imagePoint.toPoint2D();
                                        startDragROIPosition = getPosition2D();
                                    }

                                    updateDrag(e, imagePoint, canvas);

                                    // consume event
                                    e.consume();
                                }
                            }
                        }
                        finally
                        {
                            ROI2D.this.endUpdate();
                        }
                    }
                }
            }
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // do parent stuff
            super.mouseMove(e, imagePoint, canvas);

            // update focus
            if (!e.isConsumed())
            {
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        if (updateFocus(e, imagePoint, canvas))
                            e.consume();
                    }
                }
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                drawROI(g, sequence, canvas);
                // display name ?
                if (getShowName())
                    drawName(g, sequence, canvas);
            }
        }

        /**
         * Draw the ROI
         */
        protected abstract void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas);

        /**
         * Draw the ROI name
         */
        protected void drawName(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                // not supported
                if (g == null)
                    return;

                final Graphics2D g2 = (Graphics2D) g.create();
                final IcyCanvas2D cnv2d = (IcyCanvas2D) canvas;
                final Rectangle2D bounds = getBounds2D();
                final Point pos = cnv2d.imageToCanvas(bounds.getCenterX(), bounds.getMinY());
                final double coef = Math.log(canvas.getScaleX() + 1);
                final double fontSize = (GeneralPreferences.getGuiFontSize() - 4) + (int) (coef * 10d);

                // go to absolute coordinates
                g2.transform(cnv2d.getInverseTransform());
                // set text anti alias
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                // set font
                g2.setFont(FontUtil.setSize(g2.getFont(), (int) fontSize));
                // set color
                g2.setColor(getColor());

                // draw ROI name
                GraphicsUtil.drawHCenteredString(g2, getName(), pos.x, pos.y - (int) (2 * fontSize), true);

                g2.dispose();
            }

            if (canvas instanceof IcyCanvas3D)
            {
                // not yet supported

            }
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
     * Returns the Z position.<br>
     * <code>-1</code> is a special value meaning the ROI is set on all Z slices (infinite Z
     * dimension).
     */
    public int getZ()
    {
        return z;
    }

    /**
     * Sets Z position of this 2D ROI.<br>
     * You cannot set the ROI on a negative Z position as <code>-1</code> is a special value meaning
     * the ROI is set on all Z slices (infinite Z dimension).
     */
    public void setZ(int value)
    {
        final int v;

        // special value for infinite dimension --> change to -1
        if (value == Integer.MIN_VALUE)
            v = -1;
        else
            v = value;

        if (z != v)
        {
            z = v;
            roiChanged();
        }
    }

    /**
     * Returns the T position.<br>
     * <code>-1</code> is a special value meaning the ROI is set on all T frames (infinite T
     * dimension).
     */
    public int getT()
    {
        return t;
    }

    /**
     * Sets T position of this 3D ROI.<br>
     * You cannot set the ROI on a negative T position as <code>-1</code> is a special value meaning
     * the ROI is set on all T frames (infinite T dimension).
     */
    public void setT(int value)
    {
        final int v;

        // special value for infinite dimension --> change to -1
        if (value == Integer.MIN_VALUE)
            v = -1;
        else
            v = value;

        if (t != v)
        {
            t = v;
            roiChanged();
        }
    }

    /**
     * Returns the C position.<br>
     * <code>-1</code> is a special value meaning the ROI is set on all C channels (infinite C
     * dimension).
     */
    public int getC()
    {
        return c;
    }

    /**
     * Sets C position of this 2D ROI.<br>
     * You cannot set the ROI on a negative C position as <code>-1</code> is a special value meaning
     * the ROI is set on all C channels (infinite C dimension).
     */
    public void setC(int value)
    {
        final int v;

        // special value for infinite dimension --> change to -1
        if (value == Integer.MIN_VALUE)
            v = -1;
        else
            v = value;

        if (c != v)
        {
            c = v;
            roiChanged();
        }
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return isActiveFor(canvas.getPositionZ(), canvas.getPositionT(), canvas.getPositionC());
    }

    /**
     * Return true if the ROI is active for the specified Z, T, C coordinates
     */
    public boolean isActiveFor(int z, int t, int c)
    {
        return ((getZ() == -1) || (z == -1) || (getZ() == z)) && ((getT() == -1) || (t == -1) || (getT() == t))
                && ((getC() == -1) || (c == -1) || (getC() == c));
    }

    // public abstract boolean canAddPoint();
    //
    // /**
    // * Return true if this ROI support removing point
    // */
    // public abstract boolean canRemovePoint();
    //
    // /**
    // * Add a new point at specified position (used to build ROI)
    // */
    // public abstract boolean addPointAt(Point2D pos, boolean ctrl);
    //
    // /**
    // * Remove point at specified position (used to build ROI)
    // */
    // public abstract boolean removePointAt(IcyCanvas canvas, Point2D imagePoint);
    //
    // /**
    // * Remove selected point at specified position (used to build ROI)
    // */
    // protected abstract boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint);

    /**
     * @deprecated Use {@link #isOverEdge(IcyCanvas, Point2D)} instead.
     */
    @Deprecated
    public boolean isOver(IcyCanvas canvas, Point2D p)
    {
        return isOverEdge(canvas, p.getX(), p.getY());
    }

    /**
     * @deprecated Use {@link #isOverEdge(IcyCanvas, double, double)} instead.
     */
    @Deprecated
    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        return isOverEdge(canvas, x, y);
    }

    /**
     * Returns true if specified point coordinates overlap the ROI edge.<br>
     * Use {@link #contains(Point2D)} to test for content overlap instead.
     */
    public boolean isOverEdge(IcyCanvas canvas, Point2D p)
    {
        return isOverEdge(canvas, p.getX(), p.getY());
    }

    /**
     * Returns true if specified point coordinates overlap the ROI edge.<br>
     * Use {@link #contains(double, double)} to test for content overlap instead.
     */
    public abstract boolean isOverEdge(IcyCanvas canvas, double x, double y);

    /**
     * Returns true if specified point coordinates overlap the ROI edge.<br>
     * Use {@link #contains(Point5D)} to test for content overlap instead.
     */
    public boolean isOverEdge(IcyCanvas canvas, Point5D p)
    {
        return isOverEdge(canvas, p.getX(), p.getY(), p.getZ(), p.getT(), p.getC());
    }

    /**
     * Returns true if specified point coordinates overlap the ROI edge.<br>
     * Use {@link #contains(double, double, double, double, double)} to test for content overlap
     * instead.
     */
    public boolean isOverEdge(IcyCanvas canvas, double x, double y, double z, double t, double c)
    {
        if (isActiveFor((int) z, (int) t, (int) c))
            return isOverEdge(canvas, x, y);

        return false;
    }

    /**
     * Tests if a specified {@link Point2D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point2D</code> to be tested
     * @return <code>true</code> if the specified <code>Point2D</code> is inside the boundary of the <code>ROI</code>;
     *         <code>false</code> otherwise.
     */
    public boolean contains(Point2D p)
    {
        return contains(p.getX(), p.getY());
    }

    /**
     * Tests if the interior of the <code>ROI</code> entirely contains the specified <code>Rectangle2D</code>. The
     * {@code ROI.contains()} method allows a implementation to
     * conservatively return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle2D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the {@code ROI} contains the
     * {@code Rectangle2D}.
     * 
     * @param r
     *        The specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the <code>Rectangle2D</code>;
     *         <code>false</code> otherwise or, if the <code>ROI</code> contains the <code>Rectangle2D</code> and the
     *         <code>intersects</code> method returns <code>true</code> and the containment calculations would be too
     *         expensive to perform.
     * @see #contains(double, double, double, double)
     */
    public boolean contains(Rectangle2D r)
    {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the specified coordinates are inside the <code>ROI</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @return <code>true</code> if the specified coordinates are inside the <code>ROI</code> boundary;
     *         <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y);

    /**
     * Tests if the <code>ROI</code> entirely contains the specified rectangular area. All
     * coordinates that lie inside the rectangular area must lie within the <code>ROI</code> for the
     * entire rectangular area to be considered contained within the <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively return {@code false}
     * when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the rectangular area are
     * prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the {@code ROI} contains
     * the rectangular area.
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
     *         specified rectangular area; <code>false</code> otherwise or, if the <code>ROI</code> contains the
     *         rectangular area and the <code>intersects</code> method returns <code>true</code> and the containment
     *         calculations would be too expensive to perform.
     */
    public abstract boolean contains(double x, double y, double w, double h);

    @Override
    public boolean contains(double x, double y, double z, double t, double c)
    {
        final boolean cok;
        final boolean zok;
        final boolean tok;

        if (getZ() == -1)
            zok = true;
        else
            zok = (z >= getZ()) && (z < (getZ() + 1d));
        if (getT() == -1)
            tok = true;
        else
            tok = (t >= getT()) && (t < (getT() + 1d));
        if (getC() == -1)
            cok = true;
        else
            cok = (c >= getC()) && (c < (getC() + 1d));

        return cok && zok && tok && contains(x, y);
    }

    @Override
    public boolean contains(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
            double sizeT, double sizeC)
    {
        final boolean zok;
        final boolean tok;
        final boolean cok;

        if (getZ() == -1)
            zok = true;
        else
            zok = (z >= getZ()) && ((z + sizeZ) <= (getZ() + 1d));
        if (getT() == -1)
            tok = true;
        else
            tok = (t >= getT()) && ((t + sizeT) <= (getT() + 1d));
        if (getC() == -1)
            cok = true;
        else
            cok = (c >= getC()) && ((c + sizeC) <= (getC() + 1d));

        return zok && tok && cok && contains(x, y, sizeX, sizeY);
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified <code>Rectangle2D</code>.
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively return {@code true}
     * when:
     * <ul>
     * <li>there is a high probability that the <code>Rectangle2D</code> and the <code>ROI</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the {@code Rectangle2D}
     * does not intersect the {@code ROI}.
     * 
     * @param r
     *        the specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         specified <code>Rectangle2D</code> intersect, or are both highly likely to intersect
     *         and intersection calculations would be too expensive to perform; <code>false</code> otherwise.
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
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively return {@code true}
     * when:
     * <ul>
     * <li>there is a high probability that the rectangular area and the <code>ROI</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the rectangular area does
     * not intersect the {@code ROI}.
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

    @Override
    public boolean intersects(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC)
    {
        // easy discard
        if ((sizeX == 0d) || (sizeY == 0d) || (sizeZ == 0d) || (sizeT == 0d) || (sizeC == 0d))
            return false;

        final boolean zok;
        final boolean tok;
        final boolean cok;

        if (getZ() == -1)
            zok = true;
        else
            zok = ((z + sizeZ) > getZ()) && (z < (getZ() + 1d));
        if (getT() == -1)
            tok = true;
        else
            tok = ((t + sizeT) > getT()) && (t < (getT() + 1d));
        if (getC() == -1)
            cok = true;
        else
            cok = ((c + sizeC) > getC()) && (c < (getC() + 1d));

        return intersects(x, y, sizeX, sizeY) && zok && tok && cok;
    }

    /**
     * Calculate and returns the 2D bounding box of the <code>ROI</code>.<br>
     * This method is used by {@link #getBounds2D()} which should try to cache the result as the
     * bounding box calculation can take some computation time for complex ROI.
     */
    public abstract Rectangle2D computeBounds2D();

    @Override
    public Rectangle5D computeBounds5D()
    {
        final Rectangle2D bounds2D = computeBounds2D();
        if (bounds2D == null)
            return new Rectangle5D.Double();

        final Rectangle5D.Double result = new Rectangle5D.Double(bounds2D.getX(), bounds2D.getY(), 0d, 0d, 0d,
                bounds2D.getWidth(), bounds2D.getHeight(), 0d, 0d, 0d);

        if (getZ() == -1)
        {
            result.z = Double.NEGATIVE_INFINITY;
            result.sizeZ = Double.POSITIVE_INFINITY;
        }
        else
        {
            result.z = getZ();
            result.sizeZ = 1d;
        }
        if (getT() == -1)
        {
            result.t = Double.NEGATIVE_INFINITY;
            result.sizeT = Double.POSITIVE_INFINITY;
        }
        else
        {
            result.t = getT();
            result.sizeT = 1d;
        }
        if (getC() == -1)
        {
            result.c = Double.NEGATIVE_INFINITY;
            result.sizeC = Double.POSITIVE_INFINITY;
        }
        else
        {
            result.c = getC();
            result.sizeC = 1d;
        }

        return result;
    }

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
     * Returns a high precision and more accurate bounding box of the <code>ROI</code> than the <code>getBounds</code>
     * method. Note that there is no guarantee that the returned {@link Rectangle2D} is the smallest bounding box that
     * encloses the <code>ROI</code>, only
     * that the <code>ROI</code> lies entirely within the indicated <code>Rectangle2D</code>. The
     * bounding box returned by this method is usually tighter than that returned by the <code>getBounds</code> method
     * and never fails due to overflow problems since the return value
     * can be an instance of the <code>Rectangle2D</code> that uses double precision values to store
     * the dimensions.
     * 
     * @return an instance of <code>Rectangle2D</code> that is a high-precision bounding box of the <code>ROI</code>.
     */
    public Rectangle2D getBounds2D()
    {
        return getBounds5D().toRectangle2D();
    }

    /**
     * Returns the upper left point of the ROI bounds.<br>
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
     * Returns the ROI position which normally correspond to the <i>minimum</i> point of the ROI
     * bounds:<br>
     * <code>new Point2D.Double(getBounds2D().getX(), getBounds2D().getY())</code>
     * 
     * @see #getBounds2D()
     */
    public Point2D getPosition2D()
    {
        final Rectangle2D r = getBounds2D();
        return new Point2D.Double(r.getX(), r.getY());
    }

    @Override
    public boolean canSetBounds()
    {
        // default
        return false;
    }

    /**
     * Set the <code>ROI</code> 2D bounds.<br>
     * Note that not all ROI supports bounds modification and you should call {@link #canSetBounds()} first to test if
     * the operation is supported.<br>
     * 
     * @param bounds
     *        new ROI 2D bounds
     */
    public void setBounds2D(Rectangle2D bounds)
    {
        // do nothing by default (not supported)
    }

    @Override
    public void setBounds5D(Rectangle5D bounds)
    {
        beginUpdate();
        try
        {
            // infinite Z dim ?
            if (bounds.getSizeZ() == Double.POSITIVE_INFINITY)
                setZ(-1);
            else
                setZ((int) bounds.getZ());
            // infinite T dim ?
            if (bounds.getSizeT() == Double.POSITIVE_INFINITY)
                setT(-1);
            else
                setT((int) bounds.getT());
            // infinite C dim ?
            if (bounds.getSizeC() == Double.POSITIVE_INFINITY)
                setC(-1);
            else
                setC((int) bounds.getC());

            setBounds2D(bounds.toRectangle2D());
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean canSetPosition()
    {
        // default implementation use translation if available
        return canTranslate();
    }

    /**
     * @deprecated Use {@link #setPosition2D(Point2D)} instead.
     */
    @Deprecated
    public void setPosition(Point2D position)
    {
        setPosition2D(position);
    }

    /**
     * Set the <code>ROI</code> 2D position.<br>
     * Note that not all ROI supports position modification and you should call {@link #canSetPosition()} first to test
     * if the operation is supported.<br>
     * 
     * @param position
     *        new ROI 2D position
     */
    public void setPosition2D(Point2D position)
    {
        // use translation operation by default if supported
        if (canTranslate())
        {
            final Point2D oldPos = getPosition2D();
            translate(position.getX() - oldPos.getX(), position.getY() - oldPos.getY());
        }
    }

    @Override
    public void setPosition5D(Point5D position)
    {
        beginUpdate();
        try
        {
            setZ((int) position.getZ());
            setT((int) position.getT());
            setC((int) position.getC());
            setPosition2D(position.toPoint2D());
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Returns <code>true</code> if the ROI support translate operation.
     * 
     * @see #translate(double, double)
     */
    public boolean canTranslate()
    {
        // by default
        return false;
    }

    /**
     * Translate the ROI position by the specified delta X/Y.<br>
     * Note that not all ROI support this operation so you should test it by calling {@link #canTranslate()} first.
     * 
     * @param dx
     *        translation value to apply on X dimension
     * @param dy
     *        translation value to apply on Y dimension
     * @see #canTranslate()
     * @see #setPosition2D(Point2D)
     */
    public void translate(double dx, double dy)
    {
        // not supported by default
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, int t, int c, boolean inclusive)
    {
        // not on the correct Z, T, C position --> return empty mask
        if (!isActiveFor(z, t, c))
            return new boolean[width * height];

        return getBooleanMask(x, y, width, height, inclusive);
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi.<br>
     * if the pixel (x,y) is contained in the roi then result[(y * width) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi then result[(y * width) + x] = false
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param width
     *        the width of the specified rectangular area
     * @param height
     *        the height of the specified rectangular area
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     * @return the boolean bitmap mask
     */
    public boolean[] getBooleanMask(int x, int y, int width, int height, boolean inclusive)
    {
        final boolean[] result = new boolean[width * height];

        // simple and basic implementation, override it to have better performance
        int offset = 0;
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                if (inclusive)
                    result[offset] = intersects(x + i, y + j, 1d, 1d);
                else
                    result[offset] = contains(x + i, y + j, 1d, 1d);
                offset++;
            }
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getBooleanMask(int, int, int, int, boolean)} instead
     */
    @Deprecated
    public boolean[] getBooleanMask(int x, int y, int width, int height)
    {
        return getBooleanMask(x, y, width, height, false);
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
     * @deprecated Use {@link #getBooleanMask(Rectangle, boolean)} instead
     */
    @Deprecated
    public boolean[] getBooleanMask(Rectangle rect)
    {
        return getBooleanMask(rect, false);
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, int t, int c, boolean inclusive)
    {
        // not on the correct Z, T, C position --> return empty mask
        if (!isActiveFor(z, t, c))
            return new BooleanMask2D();

        return getBooleanMask(inclusive);
    }

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

        // empty ROI --> return empty mask
        if (bounds.isEmpty())
            return new BooleanMask2D(new Rectangle(), new boolean[0]);

        return new BooleanMask2D(bounds, getBooleanMask(bounds, inclusive));
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} instead.
     */
    @Deprecated
    public BooleanMask2D getBooleanMask()
    {
        return getBooleanMask(false);
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
     * Generic implementation for ROI2D using the BooleanMask object so the result is just an
     * approximation. This method should be overridden whenever possible to provide more optimal
     * approximations.
     */
    @Override
    public double computeNumberOfContourPoints()
    {
        return getBooleanMask(true).getContourLength();
    }

    /**
     * Generic implementation for ROI2D using the BooleanMask object so the result is just an
     * approximation. This method should be overridden whenever possible to provide more optimal
     * approximations.
     */
    @Override
    public double computeNumberOfPoints()
    {
        double numPoints = 0;

        // approximation by using number of point of boolean mask with and without border
        numPoints += getBooleanMask(true).getNumberOfPoints();
        numPoints += getBooleanMask(false).getNumberOfPoints();
        numPoints /= 2d;

        return numPoints;
    }

    /**
     * Return perimeter of the 2D ROI in pixels.<br>
     * This is basically the number of pixel representing ROI contour.<br>
     * 
     * @deprecated Use {@link #getNumberOfContourPoints()} instead.
     * @see #getNumberOfContourPoints()
     * @see #computeNumberOfContourPoints()
     */
    @Override
    @Deprecated
    public double getPerimeter()
    {
        return getNumberOfContourPoints();
    }

    /**
     * Return area of the 2D ROI in pixels.<br>
     * This is basically the number of pixel contained in the ROI.<br>
     * 
     * @deprecated Use {@link #getNumberOfPoints()} instead.
     * @see #getNumberOfPoints()
     * @see #computeNumberOfPoints()
     */
    @Deprecated
    public double getArea()
    {
        return getNumberOfPoints();
    }

    /**
     * Returns a sub part of the ROI.<br/>
     * <code>null</code> can be returned if result is empty.
     * 
     * @param z
     *        the specific Z position (slice) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI Z dimension)
     * @param t
     *        the specific T position (frame) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI T dimension)
     * @param c
     *        the specific C position (channel) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI C dimension)
     */
    @Override
    public ROI getSubROI(int z, int t, int c)
    {
        if (!isActiveFor(z, t, c))
            return null;

        final ROI2D result = (ROI2D) getCopy();

        // copy can fail...
        if (result != null)
        {
            // set Z, T, C position
            if (z != -1)
                result.setZ(z);
            if (t != -1)
                result.setT(t);
            if (c != -1)
                result.setC(c);

            // set name
            result.setName(getName() + getNameSuffix(z, t, c));
        }

        return result;
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

/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.common.EventHierarchicalChecker;
import icy.sequence.Sequence;
import icy.type.collection.array.ArrayUtil;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.w3c.dom.Node;

/**
 * ROI Area type.<br>
 * Use a bitmap mask internally for fast boolean mask operation.<br>
 * 
 * @author Stephane
 */
public class ROI2DArea extends ROI2D
{
    protected static final float DEFAULT_CURSOR_SIZE = 15f;

    // we want the cursor size static
    static float cursorSize = DEFAULT_CURSOR_SIZE;

    protected class ROI2DAreaPainter extends ROI2DPainter
    {
        private static final float DEFAULT_ALPHA = 0.3f;
        private static final float MIN_CURSOR_SIZE = 1f;
        private static final float MAX_CURSOR_SIZE = 500f;

        private final Ellipse2D cursor;
        private Point2D.Double cursorPosition;
        private Color cursorColor;
        private float alphaLevel;

        /**
         * 
         */
        public ROI2DAreaPainter()
        {
            super();

            cursor = new Ellipse2D.Double();
            cursorPosition = new Point2D.Double();
            cursorColor = Color.red;
            alphaLevel = DEFAULT_ALPHA;
        }

        void updateCursor()
        {
            final double x = cursorPosition.getX();
            final double y = cursorPosition.getY();

            cursor.setFrameFromDiagonal(x - cursorSize, y - cursorSize, x + cursorSize, y + cursorSize);

            // if roi selected (cursor displayed) --> painter changed
            if (selected)
                changed();
        }

        /**
         * @return the cursor position
         */
        public Point2D getCursorPosition()
        {
            return cursorPosition;
        }

        /**
         * @param position
         *        the cursor position to set
         */
        public void setCursorPosition(Point2D position)
        {
            if (!cursorPosition.equals(position))
            {
                cursorPosition.setLocation(position);
                updateCursor();
            }
        }

        /**
         * @return the cursorSize
         */
        public float getCursorSize()
        {
            return cursorSize;
        }

        /**
         * @param value
         *        the cursorSize to set
         */
        public void setCursorSize(float value)
        {
            final float adjValue = Math.max(Math.min(value, MAX_CURSOR_SIZE), MIN_CURSOR_SIZE);

            if (cursorSize != adjValue)
            {
                cursorSize = adjValue;
                updateCursor();
            }
        }

        /**
         * @return the alphaLevel
         */
        public float getAlphaLevel()
        {
            return alphaLevel;
        }

        /**
         * @param value
         *        the alphaLevel to set
         */
        public void setAlphaLevel(float value)
        {
            final float newValue = Math.max(0f, Math.min(1f, value));

            if (alphaLevel != newValue)
            {
                alphaLevel = newValue;
                changed();
            }
        }

        /**
         * @return the cursorColor
         */
        public Color getCursorColor()
        {
            return cursorColor;
        }

        /**
         * @param value
         *        the cursorColor to set
         */
        public void setCursorColor(Color value)
        {
            if (!cursorColor.equals(value))
            {
                cursorColor = value;
                changed();
            }
        }

        public void addToMask(Point2D pos)
        {
            setCursorPosition(pos);
            updateMask(cursor, false);
        }

        public void removeFromMask(Point2D pos)
        {
            setCursorPosition(pos);
            updateMask(cursor, true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see icy.painter.AbstractPainter#onChanged(icy.common.EventHierarchicalChecker)
         */
        @Override
        public void onChanged(EventHierarchicalChecker object)
        {
            final Color roiColor = getDisplayColor();

            // roi color changed ?
            if (!previousColor.equals(roiColor))
            {
                // update mask color
                updateMaskColor();
                // set to new color
                previousColor = getDisplayColor();
            }

            super.onChanged(object);
        }

        @Override
        public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2DArea.this.beginUpdate();
            try
            {
                super.keyPressed(e, imagePoint, canvas);

                if (!e.isConsumed())
                {
                    switch (e.getKeyCode())
                    {
                        case KeyEvent.VK_ADD:
                            if (ROI2DArea.this.selected)
                                setCursorSize(cursorSize * 1.1f);
                            break;

                        case KeyEvent.VK_SUBTRACT:
                            if (ROI2DArea.this.selected)
                                setCursorSize(cursorSize * 0.9f);
                            break;
                    }
                }
            }
            finally
            {
                ROI2DArea.this.endUpdate();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see icy.roi.ROI2D.ROI2DPainter#mousePressed(java.awt.event.MouseEvent,
         * java.awt.geom.Point2D, icy.canvas.IcyCanvas)
         */
        @Override
        public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2DArea.this.beginUpdate();
            try
            {
                super.mousePressed(e, imagePoint, canvas);

                if (!e.isConsumed())
                {
                    // right button action
                    if (EventUtil.isRightMouseButton(e))
                    {
                        // roi selected ?
                        if (ROI2DArea.this.selected)
                        {
                            // inside bounds ?
                            if (getBounds2D().intersects(cursor.getBounds2D()))
                            {
                                // roi not focused ? --> remove point from mask
                                if (!focused)
                                    removePointAt(canvas, imagePoint);
                            }
                            else
                                // else just unselect the roi
                                setSelected(false, false);
                        }
                    }
                }
            }
            finally
            {
                ROI2DArea.this.endUpdate();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see icy.painter.AbstractPainter#mouseReleased(java.awt.event.MouseEvent,
         * java.awt.geom.Point2D, icy.canvas.IcyCanvas)
         */
        @Override
        public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            // update only on release as it can be long
            if (boundsNeedUpdate)
                optimizeBounds(true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see icy.roi.ROI2D.ROI2DPainter#mouseDrag(java.awt.event.MouseEvent,
         * java.awt.geom.Point2D, icy.canvas.IcyCanvas)
         */
        @Override
        public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2DArea.this.beginUpdate();
            try
            {
                super.mouseDrag(e, imagePoint, canvas);

                if (!e.isConsumed())
                {
                    // right button action
                    if (EventUtil.isRightMouseButton(e))
                    {
                        // roi selected ?
                        if (ROI2DArea.this.selected)
                        {
                            // inside bounds ? --> remove point from mask
                            if (getBounds2D().intersects(cursor.getBounds2D()))
                            {
                                // roi not focused ? --> remove point from mask
                                if (!focused)
                                    removePointAt(canvas, imagePoint);
                            }
                        }
                    }
                }
            }
            finally
            {
                ROI2DArea.this.endUpdate();
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // prepare color and stroke
            g.setColor(getDisplayColor());
            if (ROI2DArea.this.selected)
                g.setStroke(new BasicStroke((float) getAdjustedStroke(canvas, ROI2DArea.this.stroke + 1d)));
            else
                g.setStroke(new BasicStroke((float) getAdjustedStroke(canvas, ROI2DArea.this.stroke)));

            // draw bounds
            g.draw(bounds);
            // draw mask
            g.drawImage(mask, null, bounds.x, bounds.y);

            // ROI selected ? draw cursor
            if (selected && !focused)
            {
                // set alpha
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaLevel));
                // cursor color
                g.setColor(cursorColor);
                // draw cursor
                g.fill(cursor);
                // set alpha back to normal
                g.setComposite(AlphaComposite.SrcOver);
            }
        }

    }

    public static final String ID_BOUNDS_X = "boundsX";
    public static final String ID_BOUNDS_Y = "boundsY";
    public static final String ID_BOUNDS_W = "boundsW";
    public static final String ID_BOUNDS_H = "boundsH";
    // protected static final String ID_BOOLMASK_LEN = "boolMaskLen";
    public static final String ID_BOOLMASK_DATA = "boolMaskData";

    /**
     * image containing the mask
     */
    BufferedImage mask;
    /**
     * rectangle bounds
     */
    final Rectangle bounds;

    /**
     * internals
     */
    int[] maskData;
    boolean boundsNeedUpdate;
    double translateX, translateY;
    Color previousColor;

    public ROI2DArea(Point2D position)
    {
        super();

        bounds = new Rectangle();
        boundsNeedUpdate = false;
        translateX = 0d;
        translateY = 0d;
        // keep trace of previous color
        previousColor = getDisplayColor();

        // default image
        mask = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        maskData = ((DataBufferInt) mask.getRaster().getDataBuffer()).getData();

        if (position != null)
        {
            // init mouse position
            setMousePos(position);
            // add current point to mask
            addPointAt(position, false);
        }

        setName("Area2D");
    }

    public ROI2DArea()
    {
        this(null);
    }

    void addToBounds(Rectangle bnd)
    {
        // save previous bounds
        final Rectangle oldBounds = new Rectangle(bounds);

        if (bounds.isEmpty())
            bounds.setBounds(bnd);
        else
            bounds.add(bnd);

        try
        {
            // update image to the new bounds
            updateImage(oldBounds, bounds);
        }
        catch (Error E)
        {
            // maybe a "out of memory" error, restore back old bounds
            bounds.setBounds(oldBounds);
            System.err.println("can't enlarge ROI, no enough memory !");
        }
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask<br>
     */
    void optimizeBounds(boolean removeIfEmpty)
    {
        // bounds are being updated
        boundsNeedUpdate = false;

        // save previous bounds
        final Rectangle oldBounds = new Rectangle(bounds);

        // recompute bound from the mask data
        final int sizeX = mask.getWidth();
        final int sizeY = mask.getHeight();

        int minX, minY, maxX, maxY;
        minX = maxX = minY = maxY = 0;
        boolean empty = true;
        int offset = 0;
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                if (maskData[offset++] != 0)
                {
                    if (empty)
                    {
                        minX = maxX = x;
                        minY = maxY = y;
                        empty = false;
                    }
                    else
                    {
                        if (x < minX)
                            minX = x;
                        else if (x > maxX)
                            maxX = x;
                        if (y < minY)
                            minY = y;
                        else if (y > maxY)
                            maxY = y;
                    }
                }
            }
        }

        if (!empty)
        {
            bounds.setBounds(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1);
            // update image to the new bounds
            updateImage(oldBounds, bounds);
            // notify changed
            roiChanged();
        }
        // empty ? delete ROI if flag allow it
        else if (removeIfEmpty)
            delete();
    }

    public int getMaskColor()
    {
        final int alpha = (int) (getPainter().getAlphaLevel() * 255);
        return (alpha << 24) | (ROI2DArea.this.getDisplayColor().getRGB() & 0x00FFFFFF);
    }

    void updateMaskColor()
    {
        final int maskColor = getMaskColor();
        final int len = mask.getWidth() * mask.getHeight();

        for (int offset = 0; offset < len; offset++)
        {
            // change the color where we already have color data
            if (maskData[offset] != 0)
                maskData[offset] = maskColor;
        }
    }

    void updateImage(Rectangle oldBnd, Rectangle newBnd)
    {
        // copy rectangle
        final Rectangle oldBounds = new Rectangle(oldBnd);
        final Rectangle newBounds = new Rectangle(newBnd);

        // replace to oldBounds origin
        oldBounds.translate(-oldBnd.x, -oldBnd.y);
        newBounds.translate(-oldBnd.x, -oldBnd.y);

        // dimension changed ?
        if ((oldBounds.width != newBounds.width) || (oldBounds.height != newBounds.height))
        {
            final BufferedImage newMask = new BufferedImage(newBounds.width, newBounds.height,
                    BufferedImage.TYPE_INT_ARGB);
            final int[] newMaskData = ((DataBufferInt) newMask.getRaster().getDataBuffer()).getData();

            final Rectangle intersect = newBounds.intersection(oldBounds);

            if (!intersect.isEmpty())
            {
                int offSrc = 0;
                int offDst = 0;

                // adjust offset in source mask
                if (intersect.x > 0)
                    offSrc += intersect.x;
                if (intersect.y > 0)
                    offSrc += intersect.y * oldBounds.width;
                // adjust offset in destination mask
                if (newBounds.x < 0)
                    offDst += -newBounds.x;
                if (newBounds.y < 0)
                    offDst += -newBounds.y * newBounds.width;

                // preserve data
                for (int j = 0; j < intersect.height; j++)
                {
                    System.arraycopy(maskData, offSrc, newMaskData, offDst, intersect.width);

                    offSrc += oldBounds.width;
                    offDst += newBounds.width;
                }
            }

            // set new image and maskData
            mask = newMask;
            maskData = newMaskData;
        }
    }

    /**
     * add or remove a point in the mask
     */
    public void updateMask(int x, int y, boolean remove)
    {
        // first update bounds
        if (remove)
            // bounds need to be entirely computed
            boundsNeedUpdate = true;
        else
            // update bounds (this update the image dimension if needed)
            addToBounds(new Rectangle(x, y, 1, 1));

        // get image graphics object
        final Graphics2D g = mask.createGraphics();

        // set no alpha
        g.setComposite(AlphaComposite.Src);

        final int adjX = x - bounds.x;
        final int adjY = y - bounds.y;
        final int value;

        // set color depending remove or adding to mask
        if (remove)
            value = 0;
        else
            value = getMaskColor();

        maskData[adjX + (adjY * bounds.width)] = value;

        // notify roi changed
        ROI2DArea.this.roiChanged();
    }

    /**
     * Update mask from specified shape
     */
    public void updateMask(Shape shape, boolean remove)
    {
        // first update bounds
        if (remove)
            // bounds need to be entirely computed
            boundsNeedUpdate = true;
        else
            // update bounds (this update the image dimension if needed)
            addToBounds(shape.getBounds());

        // get image graphics object
        final Graphics2D g = mask.createGraphics();

        // set no alpha
        g.setComposite(AlphaComposite.Src);
        // set color depending remove or adding to mask
        if (remove)
            g.setColor(new Color(0x00000000, true));
        else
            g.setColor(new Color(getMaskColor(), true));
        // translate to origin of image
        g.translate(-bounds.x, -bounds.y);
        // draw cursor in the mask
        g.fill(shape);

        g.dispose();

        // notify roi changed
        ROI2DArea.this.roiChanged();
    }

    @Override
    public void setMousePos(Point2D pos)
    {
        if ((pos != null) && !mousePos.equals(pos))
        {
            mousePos.setLocation(pos);
            getPainter().setCursorPosition(pos);
        }
    }

    @Override
    public ROI2DAreaPainter getPainter()
    {
        return (ROI2DAreaPainter) super.painter;
    }

    @Override
    protected ROI2DAreaPainter createPainter()
    {
        return new ROI2DAreaPainter();
    }

    @Override
    public boolean hasSelectedPoint()
    {
        return false;
    }

    @Override
    public boolean canAddPoint()
    {
        return true;
    }

    @Override
    public boolean canRemovePoint()
    {
        return true;
    }

    @Override
    public boolean addPointAt(Point2D pos, boolean ctrl)
    {
        getPainter().addToMask(pos);
        return true;
    }

    @Override
    public boolean removePointAt(IcyCanvas canvas, Point2D pos)
    {
        getPainter().removeFromMask(pos);
        return true;
    }

    @Override
    protected boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint)
    {
        // no selected point for this ROI
        return false;
    }

    /**
     * Add a point to the mask
     */
    public void addPoint(Point pos)
    {
        addPoint(pos.x, pos.y);
    }

    /**
     * Add a point to the mask
     */
    public void addPoint(int x, int y)
    {
        updateMask(x, y, false);
    }

    /**
     * Remove a point to the mask
     */
    public void removePoint(Point pos)
    {
        removePoint(pos.x, pos.y);
    }

    /**
     * Remove a point to the mask
     */
    public void removePoint(int x, int y)
    {
        updateMask(x, y, true);
    }

    /**
     * Add a rectangle to the mask
     */
    public void addRect(Rectangle r)
    {
        updateMask(r, false);
    }

    /**
     * Add a rectangle to the mask
     */
    public void addRect(int x, int y, int w, int h)
    {
        addRect(new Rectangle(x, y, w, h));
    }

    /**
     * Remove a rectangle from the mask
     */
    public void removeRect(Rectangle r)
    {
        updateMask(r, true);
    }

    /**
     * Remove a rectangle from the mask
     */
    public void removeRect(int x, int y, int w, int h)
    {
        removeRect(new Rectangle(x, y, w, h));
    }

    /**
     * Clear the mask
     */
    public void clear()
    {
        // save previous bounds
        final Rectangle oldBounds = new Rectangle(bounds);
        final Rectangle emptyBounds = new Rectangle();

        // reset image with new rectangle
        bounds.setBounds(emptyBounds);
        updateImage(oldBounds, emptyBounds);
    }

    @Override
    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        final double strk = getAdjustedStroke(canvas) * 1.8;
        final Rectangle2D rect = new Rectangle2D.Double(x - (strk * 0.5), y - (strk * 0.5), strk, strk);
        // use flatten path, intersects on curved shape return incorrect result
        return ShapeUtil.pathIntersects(bounds.getPathIterator(null, 0.1), rect);
    }

    @Override
    public boolean isOverPoint(IcyCanvas canvas, double x, double y)
    {
        return false;
    }

    @Override
    public boolean contains(double x, double y)
    {
        // fast discard
        if (!bounds.contains(x, y))
            return false;

        // replace to origin
        final int xi = (int) x - bounds.x;
        final int yi = (int) y - bounds.y;

        return (maskData[(yi * mask.getWidth()) + xi] != 0);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        // fast discard
        if (!bounds.contains(x, y, w, h))
            return false;

        // replace to origin
        final int xi = (int) x - bounds.x;
        final int yi = (int) y - bounds.y;
        final int wi = (int) w;
        final int hi = (int) h;

        // scan all pixels, can take sometime if mask is large
        int offset = (yi * bounds.width) + xi;
        for (int j = 0; j < hi; j++)
        {
            for (int i = 0; i < wi; i++)
                if (maskData[offset++] == 0)
                    return false;

            offset += bounds.width - wi;
        }

        return true;
    }

    @Override
    public Rectangle getBounds()
    {
        return bounds;
    }

    @Override
    public Rectangle2D getBounds2D()
    {
        return bounds;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        // fast discard
        if (!bounds.intersects(x, y, w, h))
            return false;

        // replace to origin
        int xi = (int) x - bounds.x;
        int yi = (int) y - bounds.y;
        int wi = (int) w;
        int hi = (int) h;

        // adjust box to mask size
        if (xi < 0)
        {
            wi += xi;
            xi = 0;
        }
        if (yi < 0)
        {
            hi += yi;
            yi = 0;
        }
        if ((xi + wi) > bounds.width)
            wi -= (xi + wi) - bounds.width;
        if ((yi + hi) > bounds.height)
            hi -= (yi + hi) - bounds.height;

        // scan all pixels, can take sometime if mask is large
        int offset = (yi * bounds.width) + xi;
        for (int j = 0; j < hi; j++)
        {
            for (int i = 0; i < wi; i++)
                if (maskData[offset++] != 0)
                    return true;

            offset += bounds.width - wi;
        }

        return false;
    }

    @Override
    public boolean[] getAsBooleanMask(Rectangle r)
    {
        final boolean[] result = new boolean[r.width * r.height];

        // calculate intersection
        final Rectangle intersect = bounds.intersection(r);

        // no intersection between mask and specified rectangle
        if (intersect.isEmpty())
            return result;

        int offSrc = 0;
        int offDst = 0;

        // adjust offset in source mask
        if (intersect.x > bounds.x)
            offSrc += (intersect.x - bounds.x);
        if (intersect.y > bounds.y)
            offSrc += (intersect.y - bounds.y) * bounds.width;
        // adjust offset in destination mask
        if (bounds.x > r.x)
            offDst += (bounds.x - r.x);
        if (bounds.y > r.y)
            offDst += (bounds.y - r.y) * r.width;

        for (int j = 0; j < intersect.height; j++)
        {
            for (int i = 0; i < intersect.width; i++)
                result[offDst++] = (maskData[offSrc++] != 0);

            offSrc += bounds.width - intersect.width;
            offDst += r.width - intersect.width;
        }

        return result;
    }

    @Override
    public boolean[] getAsBooleanMask(int x, int y, int w, int h)
    {
        return getAsBooleanMask(new Rectangle(x, y, w, h));
    }

    @Override
    public void translate(double dx, double dy)
    {
        translateX += dx;
        translateY += dy;
        // convert to integer
        final int dxi = (int) translateX;
        final int dyi = (int) translateY;
        // keep trace of not used floating part
        translateX -= dxi;
        translateY -= dyi;

        bounds.translate(dxi, dyi);

        roiChanged();
    }

    /**
     * Set the mask from a BooleanMask2D object
     */
    public void setAsBooleanMask(BooleanMask2D mask)
    {
        if ((mask != null) && !mask.isEmpty())
            setAsBooleanMask(mask.bounds, mask.mask);
    }

    /**
     * Set the mask from a boolean array.<br>
     * r represents the region defined by the boolean array.
     * 
     * @param r
     * @param booleanMask
     */
    public void setAsBooleanMask(Rectangle r, boolean[] booleanMask)
    {
        // save previous bounds
        final Rectangle oldBounds = new Rectangle(bounds);

        // reset image with new rectangle
        bounds.setBounds(r);
        updateImage(oldBounds, r);

        final int maskColor = getMaskColor();
        final int len = r.width * r.height;

        for (int i = 0; i < len; i++)
        {
            if (booleanMask[i])
                maskData[i] = maskColor;
            else
                maskData[i] = 0;
        }

        optimizeBounds(false);
    }

    public void setAsBooleanMask(int x, int y, int w, int h, boolean[] booleanMask)
    {
        setAsBooleanMask(new Rectangle(x, y, w, h), booleanMask);
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#loadFromXML(org.w3c.dom.Node)
     */
    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            final Rectangle rect = new Rectangle();

            // retrieve mask bounds
            rect.x = XMLUtil.getElementIntValue(node, ID_BOUNDS_X, 0);
            rect.y = XMLUtil.getElementIntValue(node, ID_BOUNDS_Y, 0);
            rect.width = XMLUtil.getElementIntValue(node, ID_BOUNDS_W, 0);
            rect.height = XMLUtil.getElementIntValue(node, ID_BOUNDS_H, 0);

            // retrieve mask data
            final byte[] data = XMLUtil.getElementBytesValue(node, ID_BOOLMASK_DATA, new byte[0]);
            // set the ROI from the unpacked boolean mask
            setAsBooleanMask(rect, ArrayUtil.toBooleanArray1D(data));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#saveToXML(org.w3c.dom.Node)
     */
    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        // retrieve mask bounds
        XMLUtil.setElementIntValue(node, ID_BOUNDS_X, bounds.x);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_Y, bounds.y);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_W, bounds.width);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_H, bounds.height);

        // retrieve the boolean mask
        final boolean[] mask = getAsBooleanMask(bounds);
        // set mask data as byte array
        XMLUtil.setElementBytesValue(node, ID_BOOLMASK_DATA, ArrayUtil.toByteArray1D(mask));

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#endUpdate()
     */
    @Override
    public void endUpdate()
    {
        super.endUpdate();

        // update done ?
        if (!isUpdating())
        {
            // update only on release as it can be long
            if (boundsNeedUpdate)
                optimizeBounds(true);
        }
    }
}

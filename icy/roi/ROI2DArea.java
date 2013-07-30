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

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.image.ImageUtil;
import icy.sequence.Sequence;
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
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

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

    // we want to keep a static cursor size
    static float globalCursorSize = DEFAULT_CURSOR_SIZE;

    public class ROI2DAreaPainter extends ROI2DPainter
    {
        public static final float CONTENT_ALPHA = 0.3f;
        private static final float MIN_CURSOR_SIZE = 0.6f;
        private static final float MAX_CURSOR_SIZE = 500f;

        private final Ellipse2D cursor;
        private Point2D.Double cursorPosition;
        private Color cursorColor;
        private float cursorSize;

        /**
         * 
         */
        public ROI2DAreaPainter()
        {
            super();

            cursor = new Ellipse2D.Double();
            cursorPosition = new Point2D.Double();
            cursorColor = Color.red;
            cursorSize = globalCursorSize;
        }

        void updateCursor()
        {
            final double x = cursorPosition.getX();
            final double y = cursorPosition.getY();

            cursor.setFrameFromDiagonal(x - cursorSize, y - cursorSize, x + cursorSize, y + cursorSize);

            // if roi selected (cursor displayed) --> painter changed
            if (isSelected())
                painterChanged();
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
                globalCursorSize = adjValue;
                cursorSize = adjValue;
                updateCursor();
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
                painterChanged();
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

        @Override
        public void painterChanged()
        {
            updateMaskColor(true);

            super.painterChanged();
        }

        @Override
        public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // canvas3D not handled here
            if (canvas instanceof Canvas3D)
                return;
            // no image position --> exit
            if (imagePoint == null)
                return;

            ROI2DArea.this.beginUpdate();
            try
            {
                super.keyPressed(e, imagePoint, canvas);

                // no editable --> no action here
                if (isReadOnly())
                    return;

                if (!e.isConsumed())
                {
                    switch (e.getKeyChar())
                    {
                        case '+':
                            if (isSelected())
                                setCursorSize(cursorSize * 1.1f);
                            break;

                        case '-':
                            if (isSelected())
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

        @Override
        public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // canvas3D not handled here
            if (canvas instanceof Canvas3D)
                return;
            // no image position --> exit
            if (imagePoint == null)
                return;

            ROI2DArea.this.beginUpdate();
            try
            {
                super.mousePressed(e, imagePoint, canvas);

                // no editable --> no action here
                if (isReadOnly())
                    return;

                if (!e.isConsumed())
                {
                    // right button action
                    if (EventUtil.isRightMouseButton(e))
                    {
                        // roi selected ?
                        if (isSelected())
                        {
                            // roi not focused ? --> remove point from mask
                            if (!isFocused())
                                removePointAt(canvas, imagePoint);

                            e.consume();
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
        public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            super.mouseReleased(e, imagePoint, canvas);

            // canvas3D not handled here
            if (canvas instanceof Canvas3D)
                return;
            // no image position --> exit
            if (imagePoint == null)
                return;

            // update only on release as it can be long
            if (!isReadOnly() && boundsNeedUpdate)
                optimizeBounds(true);
        }

        @Override
        public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            // canvas3D not handled here
            if (canvas instanceof Canvas3D)
                return;
            // no image position --> exit
            if (imagePoint == null)
                return;

            ROI2DArea.this.beginUpdate();
            try
            {
                super.mouseDrag(e, imagePoint, canvas);

                // no editable --> no action here
                if (isReadOnly())
                    return;

                if (!e.isConsumed())
                {
                    // roi selected ?
                    if (isSelected())
                    {
                        // left button action
                        if (EventUtil.isLeftMouseButton(e))
                        {
                            // try to add a new point
                            if (addPointAt(imagePoint, EventUtil.isControlDown(e)))
                                // consume
                                e.consume();
                        }
                        // right button action
                        else if (EventUtil.isRightMouseButton(e))
                        {
                            // roi not focused ? --> remove point from mask
                            if (!isFocused())
                                removePointAt(canvas, imagePoint);

                            e.consume();
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

            drawROI(g, sequence, canvas);
            drawInfos(g, sequence, canvas);
        }

        /**
         * Draw the ROI itself
         */
        protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                final AlphaComposite prevAlpha = (AlphaComposite) g2.getComposite();
                // show content with an alpha factor
                g2.setComposite(prevAlpha.derive(prevAlpha.getAlpha() * getOpacity()));

                // trivial paint optimization
                final boolean shapeVisible = ShapeUtil.isVisible(g, getBounds());

                if (shapeVisible)
                {
                    // draw mask
                    g2.drawImage(imageMask, null, bounds.x, bounds.y);
                }

                // ROI selected ? draw cursor
                if (isSelected() && !isFocused() && !isReadOnly())
                {
                    // trivial paint optimization
                    if (ShapeUtil.isVisible(g, cursor))
                    {
                        // draw cursor border
                        g2.setColor(Color.black);
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                        g2.draw(cursor);

                        // cursor color
                        g2.setColor(cursorColor);
                        // draw cursor
                        g2.fill(cursor);
                    }
                }

                // restore alpha
                g2.setComposite(prevAlpha);

                if (shapeVisible)
                {
                    // draw border
                    if (isSelected())
                    {
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                        g2.setColor(getDisplayColor());
                        g2.draw(bounds);
                    }
                    else
                    {
                        // outside border
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                        g2.setColor(Color.black);
                        g2.draw(bounds);
                        // internal border
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                        g2.setColor(getDisplayColor());
                        g2.draw(bounds);
                    }
                }

                // for (Point2D pt : getBooleanMask().getEdgePoints())
                // g2.drawRect((int) pt.getX(), (int) pt.getY(), 1, 1);

                g2.dispose();
            }

            if (canvas instanceof IcyCanvas3D)
            {
                // not yet supported

            }
        }

        /**
         * Draw extras informations as name, size and position
         */
        protected void drawInfos(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            // if (canvas instanceof IcyCanvas2D)
            // {
            // final IcyCanvas2D cnv2d = (IcyCanvas2D) canvas;
            // final Rectangle2D bounds = getBounds2D();
            //
            // if (selected)
            // {
            // // draw position and size inside ROI
            // final String roiPositionString = "X=" + StringUtil.toString(bounds.getX(), 1) +
            // "  Y="
            // + StringUtil.toString(bounds.getY(), 1);
            // final String roiBoundingSizeString = "W=" + StringUtil.toString(bounds.getWidth(), 1)
            // + "  H="
            // + StringUtil.toString(bounds.getHeight(), 1);
            // final String text = roiPositionString + "\n" + roiBoundingSizeString;
            //
            // // position = just above ROI bounds
            // final Point pos = cnv2d.imageToCanvas(bounds.getX() + (bounds.getWidth() / 2),
            // bounds.getY());
            // final Font font = new Font("Arial", Font.BOLD, 12);
            //
            // final Graphics2D g2 = (Graphics2D) g.create();
            //
            // g2.transform(cnv2d.getInverseTransform());
            // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            // RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // g2.setFont(font);
            // g2.setColor(getDisplayColor());
            //
            // GraphicsUtil.drawCenteredString(g2, text, pos.x,
            // pos.y - (int) (GraphicsUtil.getStringBounds(g2, text).getHeight()), true);
            //
            // g2.dispose();
            // }
            // }
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
    BufferedImage imageMask;
    /**
     * rectangle bounds
     */
    final Rectangle bounds;

    /**
     * internals
     */
    final byte[] red;
    final byte[] green;
    final byte[] blue;
    IndexColorModel colorModel;
    byte[] maskData; // 0 = false, 1 = true
    boolean boundsNeedUpdate;
    double translateX, translateY;
    Color previousColor;

    /**
     * Create a ROI2D Area type from the specified {@link BooleanMask2D}.
     */
    public ROI2DArea()
    {
        super();

        bounds = new Rectangle();
        boundsNeedUpdate = false;
        translateX = 0d;
        translateY = 0d;

        // prepare indexed image
        red = new byte[256];
        green = new byte[256];
        blue = new byte[256];

        // keep trace of previous color
        previousColor = getDisplayColor();

        // set colormap
        red[1] = (byte) previousColor.getRed();
        green[1] = (byte) previousColor.getGreen();
        blue[1] = (byte) previousColor.getBlue();

        // classic 8 bits indexed with one transparent color (index = 0)
        colorModel = new IndexColorModel(8, 256, red, green, blue, 0);
        // create default image
        imageMask = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
        // get data pointer
        maskData = ((DataBufferByte) imageMask.getRaster().getDataBuffer()).getData();

        setName("Area2D");
    }

    /**
     * Create a ROI2D Area type from the specified {@link BooleanMask2D}.
     */
    public ROI2DArea(BooleanMask2D mask)
    {
        this();

        setAsBooleanMask(mask);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DArea(Point2D position, boolean cm)
    {
        this(position);
    }

    /**
     * Create a ROI2D Area type with a single point.
     */
    public ROI2DArea(Point2D position)
    {
        this();

        if (position != null)
        {
            // init mouse position
            setMousePos(position);
            // add current point to mask
            addPointAt(position, false);
        }
    }

    void addToBounds(Rectangle bnd)
    {
        final Rectangle newBounds;

        if (bounds.isEmpty())
            newBounds = new Rectangle(bnd);
        else
        {
            newBounds = new Rectangle(bounds);
            newBounds.add(bnd);
        }

        try
        {
            // update image to the new bounds
            updateImage(newBounds);
        }
        catch (Error E)
        {
            // maybe a "out of memory" error, restore back old bounds
            System.err.println("can't enlarge ROI, no enough memory !");
        }
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask<br>
     * You should call it after consecutive remove operations.
     */
    public void optimizeBounds(boolean removeIfEmpty)
    {
        // bounds are being updated
        boundsNeedUpdate = false;

        // recompute bound from the mask data
        final int sizeX = imageMask.getWidth();
        final int sizeY = imageMask.getHeight();

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
            // update image to the new bounds
            updateImage(new Rectangle(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1));
            // notify changed
            roiChanged();
        }
        // empty ? delete ROI if flag allow it
        else if (removeIfEmpty)
            remove();
    }

    public int getMaskColor()
    {
        final int alpha = (int) (ROI2DAreaPainter.CONTENT_ALPHA * 255);
        return (alpha << 24) | (getPainter().getDisplayColor().getRGB() & 0x00FFFFFF);
    }

    void updateMaskColor(boolean rebuildImage)
    {
        final Color color = getPainter().getDisplayColor();

        // roi color changed ?
        if (!previousColor.equals(color))
        {
            // update colormap
            red[1] = (byte) color.getRed();
            green[1] = (byte) color.getGreen();
            blue[1] = (byte) color.getBlue();

            colorModel = new IndexColorModel(8, 256, red, green, blue, 0);

            // recreate image (needed so new colormodel takes effect)
            if (rebuildImage)
                imageMask = ImageUtil.createIndexedImage(imageMask.getWidth(), imageMask.getHeight(), colorModel,
                        maskData);

            // set to new color
            previousColor = color;
        }
    }

    /**
     * Returns the internal image mask.
     */
    public BufferedImage getImageMask()
    {
        return imageMask;
    }

    void updateImage(Rectangle newBnd)
    {
        // copy rectangle
        final Rectangle oldBounds = new Rectangle(bounds);
        final Rectangle newBounds = new Rectangle(newBnd);

        // replace to oldBounds origin
        oldBounds.translate(-bounds.x, -bounds.y);
        newBounds.translate(-bounds.x, -bounds.y);

        // dimension changed ?
        if ((oldBounds.width != newBounds.width) || (oldBounds.height != newBounds.height))
        {
            final BufferedImage newImageMask = new BufferedImage(newBounds.width, newBounds.height,
                    BufferedImage.TYPE_BYTE_INDEXED, colorModel);
            final byte[] newMaskData = ((DataBufferByte) newImageMask.getRaster().getDataBuffer()).getData();

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
            imageMask = newImageMask;
            maskData = newMaskData;
            bounds.setBounds(newBnd);
        }
    }

    /**
     * add or remove a point in the mask
     */
    public void updateMask(int x, int y, boolean remove)
    {
        if (remove)
            // mark that bounds need to be updated
            boundsNeedUpdate = true;
        else
            // update bounds (this update the image dimension if needed)
            addToBounds(new Rectangle(x, y, 1, 1));

        final int adjX = x - bounds.x;
        final int adjY = y - bounds.y;

        // set color depending remove or adding to mask
        maskData[adjX + (adjY * bounds.width)] = (byte) (remove ? 0 : 1);

        // notify roi changed
        roiChanged();
    }

    /**
     * Update mask from specified shape
     */
    public void updateMask(Shape shape, boolean remove)
    {
        if (remove)
        {
            // outside bounds ? --> nothing to remove...
            // nothing to do
            if (!bounds.intersects(shape.getBounds2D()))
                return;

            // mark that bounds need to be updated
            boundsNeedUpdate = true;
        }
        else
            // update bounds (this update the image dimension if needed)
            addToBounds(shape.getBounds());

        // get image graphics object
        final Graphics2D g = imageMask.createGraphics();

        g.setComposite(AlphaComposite.Src);
        // set color depending remove or adding to mask
        if (remove)
            g.setColor(new Color(colorModel.getRGB(0), true));
        else
            g.setColor(new Color(colorModel.getRGB(1), true));
        // translate to origin of image and pixel center
        g.translate(-(bounds.x + 0.5d), -(bounds.y + 0.5d));
        // draw cursor in the mask
        g.fill(shape);

        g.dispose();

        // notify roi changed
        roiChanged();
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
     * Remove a point to the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation<br>
     * to refresh the mask bounds.
     */
    public void removePoint(Point pos)
    {
        removePoint(pos.x, pos.y);
    }

    /**
     * Remove a point to the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation<br>
     * to refresh the mask bounds.
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
     * Remove a rectangle from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation<br>
     * to refresh the mask bounds.
     */
    public void removeRect(Rectangle r)
    {
        updateMask(r, true);
    }

    /**
     * Remove a rectangle from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation<br>
     * to refresh the mask bounds.
     */
    public void removeRect(int x, int y, int w, int h)
    {
        removeRect(new Rectangle(x, y, w, h));
    }

    /**
     * Add a shape to the mask
     */
    public void addShape(Shape s)
    {
        updateMask(s, false);
    }

    /**
     * Remove a shape to the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation<br>
     * to refresh the mask bounds.
     */
    public void removeShape(Shape s)
    {
        updateMask(s, true);
    }

    /**
     * Return true if bounds need to be updated by calling optimizeBounds() method.
     */
    public boolean getBoundsNeedUpdate()
    {
        return boundsNeedUpdate;
    }

    /**
     * Clear the mask
     */
    public void clear()
    {
        // reset image with new rectangle
        updateImage(new Rectangle());
    }

    @Override
    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        // use bigger stroke for isOver test for easier intersection
        final double strk = getAdjustedStroke(canvas) * 3;
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

        return (maskData[(yi * imageMask.getWidth()) + xi] != 0);
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
        final int wi = (int) (x + w) - (int) x;
        final int hi = (int) (y + h) - (int) y;

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

    /*
     * already calculated
     */
    @Override
    public Rectangle2D computeBounds()
    {
        return bounds;
    }

    /*
     * We can override directly this method as we use our own bounds calculation method here
     */
    @Override
    public Rectangle getBounds()
    {
        return bounds;
    }

    /*
     * We can override directly this method as we use our own bounds calculation method here
     */
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
        int wi = (int) (x + w) - (int) x;
        int hi = (int) (y + h) - (int) y;

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
    public boolean[] getBooleanMask(int x, int y, int w, int h, boolean inclusive)
    {
        final boolean[] result = new boolean[w * h];

        // calculate intersection
        final Rectangle intersect = bounds.intersection(new Rectangle(x, y, w, h));

        // no intersection between mask and specified rectangle
        if (intersect.isEmpty())
            return result;

        // this ROI doesn't take care of inclusive parameter as intersect = contains
        int offSrc = 0;
        int offDst = 0;

        // adjust offset in source mask
        if (intersect.x > bounds.x)
            offSrc += (intersect.x - bounds.x);
        if (intersect.y > bounds.y)
            offSrc += (intersect.y - bounds.y) * bounds.width;
        // adjust offset in destination mask
        if (bounds.x > x)
            offDst += (bounds.x - x);
        if (bounds.y > y)
            offDst += (bounds.y - y) * w;

        for (int j = 0; j < intersect.height; j++)
        {
            for (int i = 0; i < intersect.width; i++)
                result[offDst++] = (maskData[offSrc++] != 0);

            offSrc += bounds.width - intersect.width;
            offDst += w - intersect.width;
        }

        return result;
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

    @Override
    public void setPosition(Point2D newPosition)
    {
        bounds.setLocation((int) newPosition.getX(), (int) newPosition.getY());

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
    protected void setAsByteMask(Rectangle r, byte[] mask)
    {
        // reset image with new rectangle
        updateImage(r);

        final int len = r.width * r.height;

        for (int i = 0; i < len; i++)
            maskData[i] = mask[i];

        optimizeBounds(false);
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
        // reset image with new rectangle
        updateImage(r);

        final int len = r.width * r.height;

        for (int i = 0; i < len; i++)
            maskData[i] = (byte) (booleanMask[i] ? 1 : 0);

        optimizeBounds(false);
    }

    public void setAsBooleanMask(int x, int y, int w, int h, boolean[] booleanMask)
    {
        setAsBooleanMask(new Rectangle(x, y, w, h), booleanMask);
    }

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
            setAsByteMask(rect, data);
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

        // retrieve mask bounds
        XMLUtil.setElementIntValue(node, ID_BOUNDS_X, bounds.x);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_Y, bounds.y);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_W, bounds.width);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_H, bounds.height);

        // set mask data as byte array
        XMLUtil.setElementBytesValue(node, ID_BOOLMASK_DATA, maskData);

        return true;
    }
}

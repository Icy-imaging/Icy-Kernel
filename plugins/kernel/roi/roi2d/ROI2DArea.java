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
package plugins.kernel.roi.roi2d;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.common.EventHierarchicalChecker;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIEvent;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.edit.Area2DChangeROIEdit;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.point.Point5D.Double;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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

import plugins.kernel.canvas.VtkCanvas;

/**
 * ROI Area type.<br>
 * Use a bitmap mask internally for fast boolean mask operation.<br>
 * 
 * @author Stephane
 */
public class ROI2DArea extends ROI2D
{
    protected static final float DEFAULT_CURSOR_SIZE = 15f;

    // we want to keep a static brush
    protected static final Ellipse2D brush = new Ellipse2D.Double();
    // protected static final Point2D.Double cursorPosition = new Point2D.Double();
    protected static Color brushColor = Color.red;
    protected static float brushSize = DEFAULT_CURSOR_SIZE;

    public class ROI2DAreaPainter extends ROI2DPainter
    {
        /**
         * @deprecated Use {@link #getOpacity()} instead.
         */
        @Deprecated
        public static final float CONTENT_ALPHA = 0.3f;

        private static final float MIN_CURSOR_SIZE = 0.3f;
        private static final float MAX_CURSOR_SIZE = 500f;

        protected final Point2D brushPosition;

        public ROI2DAreaPainter()
        {
            super();

            brushPosition = new Point2D.Double();
        }

        void updateCursor()
        {
            final double x = brushPosition.getX();
            final double y = brushPosition.getY();

            brush.setFrameFromDiagonal(x - brushSize, y - brushSize, x + brushSize, y + brushSize);

            // if roi selected (cursor displayed) --> painter changed
            if (isSelected())
                painterChanged();
        }

        /**
         * Returns the brush position.
         */
        public Point2D getBrushPosition()
        {
            return (Point) brushPosition.clone();
        }

        /**
         * Set the brush position.
         */
        public void setBrushPosition(Point2D position)
        {
            if (!brushPosition.equals(position))
            {
                brushPosition.setLocation(position);
                updateCursor();
            }
        }

        /**
         * @deprecated Use {@link #getBrushPosition()} instead.
         */
        @Deprecated
        public Point2D getCursorPosition()
        {
            return getBrushPosition();
        }

        /**
         * @deprecated Use {@link #setBrushPosition(Point2D)} instead.
         */
        @Deprecated
        public void setCursorPosition(Point2D position)
        {
            setBrushPosition(position);
        }

        /**
         * Returns the brush size.
         */
        public float getBrushSize()
        {
            return brushSize;
        }

        /**
         * Sets the brush size.
         */
        public void setBrushSize(float value)
        {
            final float adjValue = Math.max(Math.min(value, MAX_CURSOR_SIZE), MIN_CURSOR_SIZE);

            if (brushSize != adjValue)
            {
                brushSize = adjValue;
                updateCursor();
            }
        }

        /**
         * @deprecated Use {@link #getBrushSize()} instead
         */
        @Deprecated
        public float getCursorSize()
        {
            return getBrushSize();
        }

        /**
         * @deprecated Use {@link #setBrushSize(float)} instead
         */
        @Deprecated
        public void setCursorSize(float value)
        {
            setBrushSize(value);
        }

        /**
         * Returns the brush color
         */
        public Color getBrushColor()
        {
            return brushColor;
        }

        /**
         * Sets the brush color
         */
        public void setBrushColor(Color value)
        {
            if (!brushColor.equals(value))
            {
                brushColor = value;
                painterChanged();
            }
        }

        /**
         * @deprecated Use {@link #getBrushColor()} instead
         */
        @Deprecated
        public Color getCursorColor()
        {
            return getBrushColor();
        }

        /**
         * @deprecated Use {@link #setBrushColor(Color)} instead
         */
        @Deprecated
        public void setCursorColor(Color value)
        {
            setBrushColor(value);
        }

        public void addToMask(Point2D pos)
        {
            setBrushPosition(pos);
            updateMask(brush, false);
        }

        public void removeFromMask(Point2D pos)
        {
            setBrushPosition(pos);
            updateMask(brush, true);
        }

        @Override
        public void painterChanged()
        {
            updateMaskColor(true);

            super.painterChanged();
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.keyPressed(e, imagePoint, canvas);

            // not yet consumed and ROI editable...
            if (!e.isConsumed() && !isReadOnly())
            {
                // then process it here
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DArea.this.beginUpdate();
                        try
                        {
                            switch (e.getKeyChar())
                            {
                                case '+':
                                    if (isSelected())
                                    {
                                        setBrushSize(getBrushSize() * 1.1f);
                                        e.consume();
                                    }
                                    break;

                                case '-':
                                    if (isSelected())
                                    {
                                        setBrushSize(getBrushSize() * 0.9f);
                                        e.consume();
                                    }
                                    break;
                            }
                        }
                        finally
                        {
                            ROI2DArea.this.endUpdate();
                        }
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mousePressed(e, imagePoint, canvas);

            // not yet consumed, ROI editable, selected and not focused...
            if (!e.isConsumed() && !isReadOnly() && isSelected() && !isFocused())
            {
                // then process it here
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        // keep trace of roi changes from user mouse action
                        roiModifiedByMouse = false;
                        // save current ROI
                        undoSave = getBooleanMask(true);

                        ROI2DArea.this.beginUpdate();
                        try
                        {
                            // left button action
                            if (EventUtil.isLeftMouseButton(e))
                            {
                                // add point first
                                addToMask(imagePoint.toPoint2D());
                                roiModifiedByMouse = true;
                                e.consume();
                            }
                            // right button action
                            else if (EventUtil.isRightMouseButton(e))
                            {
                                // remove point
                                removeFromMask(imagePoint.toPoint2D());
                                roiModifiedByMouse = true;
                                e.consume();
                            }
                        }
                        finally
                        {
                            ROI2DArea.this.endUpdate();
                        }
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseReleased(e, imagePoint, canvas);

            // update only on release as it can be long
            if (!isReadOnly())
            {
                if (roiModifiedByMouse)
                {
                    if (boundsNeedUpdate)
                    {
                        if (optimizeBounds())
                        {
                            roiChanged();

                            // empty ? delete ROI
                            if (bounds.isEmpty())
                            {
                                ROI2DArea.this.remove();
                                // nothing more to do
                                return;
                            }
                        }
                    }

                    final Sequence sequence = canvas.getSequence();

                    // add undo operation
                    try
                    {
                        if ((sequence != null) && (undoSave != null))
                            sequence.addUndoableEdit(new Area2DChangeROIEdit(ROI2DArea.this, undoSave));
                    }
                    catch (OutOfMemoryError err)
                    {
                        // can't create undo operation, show message and clear undo manager
                        System.out.println("Warning: not enough memory to create undo point for ROI area change");
                        sequence.clearUndoManager();
                    }

                    // release save
                    undoSave = null;
                    roiModifiedByMouse = false;
                }
            }
        }

        @Override
        public void mouseMove(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseMove(e, imagePoint, canvas);

            // not yet consumed, ROI editable and selected...
            if (!e.isConsumed() && !isReadOnly() && isSelected())
            {
                // then process it here
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        setBrushPosition(imagePoint.toPoint2D());
                    }
                }
            }
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseDrag(e, imagePoint, canvas);

            // not yet consumed, ROI editable and selected...
            if (!e.isConsumed() && !isReadOnly() && isSelected())
            {
                // then process it here
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DArea.this.beginUpdate();
                        try
                        {
                            // left button action
                            if (EventUtil.isLeftMouseButton(e))
                            {
                                // add point first
                                addToMask(imagePoint.toPoint2D());
                                roiModifiedByMouse = true;
                                e.consume();
                            }
                            // right button action
                            else if (EventUtil.isRightMouseButton(e))
                            {
                                // remove point
                                removeFromMask(imagePoint.toPoint2D());
                                roiModifiedByMouse = true;
                                e.consume();
                            }
                        }
                        finally
                        {
                            ROI2DArea.this.endUpdate();
                        }
                    }
                }
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            super.paint(g, sequence, canvas);

            if (isActiveFor(canvas))
            {
                // ROI selected ? draw cursor
                if (isSelected() && !isFocused() && !isReadOnly())
                    drawCursor(g, sequence, canvas);
            }
        }

        /**
         * Draw the ROI itself
         */
        @Override
        protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                // not supported
                if (g == null)
                    return;

                final Rectangle bounds = getBounds();
                // trivial paint optimization
                final boolean shapeVisible = GraphicsUtil.isVisible(g, bounds);

                if (shapeVisible)
                {
                    final Graphics2D g2 = (Graphics2D) g.create();
                    final boolean small;

                    // disable LOD when creating the ROI
                    if (isCreating())
                        small = false;
                    else
                    {
                        final double scale = Math.max(Math.abs(canvas.getScaleX()), Math.abs(canvas.getScaleY()));
                        small = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight()) < LOD_SMALL;
                    }

                    // simplified draw
                    if (small)
                    {
                        g2.setColor(getDisplayColor());
                        g2.drawImage(imageMask, null, bounds.x, bounds.y);
                    }
                    // normal draw
                    else
                    {
                        final AlphaComposite prevAlpha = (AlphaComposite) g2.getComposite();
                        float newAlpha = prevAlpha.getAlpha() * getOpacity();
                        newAlpha = Math.min(1f, newAlpha);
                        newAlpha = Math.max(0f, newAlpha);

                        // show content with an alpha factor
                        g2.setComposite(prevAlpha.derive(newAlpha));

                        // draw mask
                        g2.drawImage(imageMask, null, bounds.x, bounds.y);

                        // restore alpha
                        g2.setComposite(prevAlpha);

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

                    g2.dispose();
                }

                // for (Point2D pt : getBooleanMask().getEdgePoints())
                // g2.drawRect((int) pt.getX(), (int) pt.getY(), 1, 1);
            }

            if (canvas instanceof IcyCanvas3D)
            {
                // not yet supported

            }
        }

        /**
         * draw the ROI cursor
         */
        protected void drawCursor(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                // not supported
                if (g == null)
                    return;

                final Rectangle bounds = brush.getBounds();
                // trivial paint optimization
                final boolean shapeVisible = GraphicsUtil.isVisible(g, bounds);

                if (shapeVisible)
                {
                    final Graphics2D g2 = (Graphics2D) g.create();
                    final boolean tiny;

                    // disable LOD when creating the ROI
                    if (isCreating())
                        tiny = false;
                    else
                    {
                        final double scale = Math.max(canvas.getScaleX(), canvas.getScaleY());
                        tiny = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight()) < LOD_TINY;
                    }

                    // simplified draw
                    if (tiny)
                    {
                        // cursor color
                        g2.setColor(brushColor);
                        // draw cursor
                        g2.fill(brush);
                    }
                    // normal draw
                    else
                    {
                        final AlphaComposite prevAlpha = (AlphaComposite) g2.getComposite();
                        float newAlpha = prevAlpha.getAlpha() * getOpacity() * 2f;
                        newAlpha = Math.min(1f, newAlpha);
                        newAlpha = Math.max(0f, newAlpha);

                        // show cursor with an alpha factor
                        g2.setComposite(prevAlpha.derive(newAlpha));

                        // draw cursor border
                        g2.setColor(Color.black);
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                        g2.draw(brush);
                        // draw cursor
                        g2.setColor(brushColor);
                        g2.fill(brush);
                    }

                    g2.dispose();
                }
            }

            if (canvas instanceof IcyCanvas3D)
            {
                // not yet supported

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
    BufferedImage imageMask;
    /**
     * rectangle bounds
     */
    final Rectangle bounds;

    /**
     * internals
     */
    protected final byte[] red;
    protected final byte[] green;
    protected final byte[] blue;
    protected IndexColorModel colorModel;
    protected byte[] maskData; // 0 = false, 1 = true
    protected double translateX, translateY;
    protected Color previousColor;
    protected boolean boundsNeedUpdate;
    protected boolean roiModifiedByMouse;
    protected BooleanMask2D undoSave;

    /**
     * Create a ROI2D Area type from the specified {@link BooleanMask2D}.
     */
    public ROI2DArea()
    {
        super();

        bounds = new Rectangle();
        boundsNeedUpdate = false;
        roiModifiedByMouse = false;
        undoSave = null;
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

        // set name and icon
        setName("Area2D");
        setIcon(ResourceUtil.ICON_ROI_AREA);
    }

    /**
     * @deprecated Use {@link #ROI2DArea(Point5D)} instead
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

        // add current point to mask
        addBrush(position);
    }

    /**
     * Generic constructor for interactive mode.
     */
    public ROI2DArea(Point5D position)
    {
        this(position.toPoint2D());
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
     * Create a copy of the specified 2D Area ROI
     */
    public ROI2DArea(ROI2DArea area)
    {
        super();

        bounds = new Rectangle();
        boundsNeedUpdate = false;
        roiModifiedByMouse = false;
        undoSave = null;
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
        imageMask = new BufferedImage(area.bounds.width, area.bounds.height, BufferedImage.TYPE_BYTE_INDEXED,
                colorModel);
        maskData = ((DataBufferByte) imageMask.getRaster().getDataBuffer()).getData();

        System.arraycopy(area.maskData, 0, maskData, 0, maskData.length);

        bounds.setBounds(area.bounds);

        // set name and icon
        setName("Area2D");
        setIcon(ResourceUtil.ICON_ROI_AREA);
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
            // perhaps a "out of memory" error, restore back old bounds
            System.err.println("can't enlarge ROI, no enough memory !");
        }
    }

    /**
     * @deprecated Use {@link #optimizeBounds()} instead.
     */
    @Deprecated
    public void optimizeBounds(boolean removeIfEmpty)
    {
        optimizeBounds();
        if (removeIfEmpty && bounds.isEmpty())
            remove();
    }

    /**
     * Returns true if the ROI is empty (the mask does not contains any point).
     */
    public boolean isEmpty()
    {
        if (bounds.isEmpty())
            return true;

        for (byte b : maskData)
            if (b != 0)
                return false;

        return true;
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask<br>
     * You should call it after consecutive remove operations.
     */
    public boolean optimizeBounds()
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
        final byte[] data = maskData;

        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                if (data[offset++] != 0)
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
            // update image to the new bounds
            return updateImage(new Rectangle(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1));

        // update to empty bounds
        return updateImage(new Rectangle(bounds.x, bounds.y, 0, 0));
    }

    /**
     * @deprecated Use {@link #getDisplayColor()} instead.
     */
    @Deprecated
    public Color getMaskColor()
    {
        return getPainter().getDisplayColor();
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

            // recreate image (so the new colormodel takes effect)
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

    boolean updateImage(Rectangle newBnd)
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
            if (!newBounds.isEmpty())
            {
                // new bounds not empty
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
            }
            else
            {
                // new bounds empty --> use single pixel image to avoid NPE
                imageMask = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
                maskData = ((DataBufferByte) imageMask.getRaster().getDataBuffer()).getData();
            }

            bounds.setBounds(newBnd);

            return true;
        }

        return false;
    }

    /**
     * Set the value of the specified point.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation to refresh the mask
     * bounds.
     */
    public void setPoint(int x, int y, boolean value)
    {
        if (value)
        {
            // set point in mask
            addToBounds(new Rectangle(x, y, 1, 1));

            // set color depending remove or adding to mask
            maskData[(x - bounds.x) + ((y - bounds.y) * bounds.width)] = 1;
        }
        else
        {
            // remove point from mask
            maskData[(x - bounds.x) + ((y - bounds.y) * bounds.width)] = 0;

            // mark that bounds need to be updated
            boundsNeedUpdate = true;
        }

        // notify roi changed
        roiChanged();
    }

    /**
     * @deprecated Use {@link #setPoint(int, int, boolean)} instead.
     */
    @Deprecated
    public void updateMask(int x, int y, boolean remove)
    {
        setPoint(x, y, !remove);
    }

    /**
     * Add the specified {@link ROI2DArea} content to this ROI2DArea
     */
    public void add(ROI2DArea roi)
    {
        final Rectangle boundsToAdd = roi.getBounds();
        final byte[] maskToAdd = roi.maskData;

        // update bounds (this update the image dimension if needed)
        addToBounds(boundsToAdd);

        int offDst, offSrc;
        final byte[] data = maskData;

        // calculate offset
        offDst = ((boundsToAdd.y - bounds.y) * bounds.width) + (boundsToAdd.x - bounds.x);
        offSrc = 0;

        for (int y = 0; y < boundsToAdd.height; y++)
        {
            for (int x = 0; x < boundsToAdd.width; x++)
                if (maskToAdd[offSrc++] != 0)
                    data[offDst + x] = 1;

            offDst += bounds.width;
        }

        // notify roi changed
        roiChanged();
    }

    /**
     * Add the specified {@link BooleanMask2D} content to this ROI2DArea
     */
    public void add(BooleanMask2D mask)
    {
        final Rectangle boundsToAdd = mask.bounds;
        final boolean[] maskToAdd = mask.mask;

        // update bounds (this update the image dimension if needed)
        addToBounds(boundsToAdd);

        int offDst, offSrc;
        final byte[] data = maskData;

        // calculate offset
        offDst = ((boundsToAdd.y - bounds.y) * bounds.width) + (boundsToAdd.x - bounds.x);
        offSrc = 0;

        for (int y = 0; y < boundsToAdd.height; y++)
        {
            for (int x = 0; x < boundsToAdd.width; x++)
                if (maskToAdd[offSrc++])
                    data[offDst + x] = 1;

            offDst += bounds.width;
        }

        // notify roi changed
        roiChanged();
    }

    /**
     * Exclusively add the specified {@link ROI2DArea} content to this ROI2DArea:
     * 
     * <pre>
     *          mask1       xor      mask2        =       result
     * 
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public void exclusiveAdd(ROI2DArea roi)
    {
        final Rectangle boundsToXAdd = roi.getBounds();
        final byte[] maskToXAdd = roi.maskData;

        // update bounds (this update the image dimension if needed)
        addToBounds(boundsToXAdd);

        int offDst, offSrc;
        final byte[] data = maskData;

        // calculate offset
        offDst = ((boundsToXAdd.y - bounds.y) * bounds.width) + (boundsToXAdd.x - bounds.x);
        offSrc = 0;

        for (int y = 0; y < boundsToXAdd.height; y++)
        {
            for (int x = 0; x < boundsToXAdd.width; x++)
                if (maskToXAdd[offSrc++] != 0)
                    data[offDst + x] ^= 1;

            offDst += bounds.width;
        }

        // optimize bounds
        if (isUpdating())
            boundsNeedUpdate = true;
        else
            optimizeBounds();

        // notify roi changed
        roiChanged();
    }

    /**
     * Exclusively add the specified {@link BooleanMask2D} content to this ROI2DArea:
     * 
     * <pre>
     *          mask1       xor      mask2        =       result
     * 
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public void exclusiveAdd(BooleanMask2D mask)
    {
        final Rectangle boundsToXAdd = mask.bounds;
        final boolean[] maskToXAdd = mask.mask;

        // update bounds (this update the image dimension if needed)
        addToBounds(boundsToXAdd);

        int offDst, offSrc;
        final byte[] data = maskData;

        // calculate offset
        offDst = ((boundsToXAdd.y - bounds.y) * bounds.width) + (boundsToXAdd.x - bounds.x);
        offSrc = 0;

        for (int y = 0; y < boundsToXAdd.height; y++)
        {
            for (int x = 0; x < boundsToXAdd.width; x++)
                if (maskToXAdd[offSrc++])
                    data[offDst + x] ^= 1;

            offDst += bounds.width;
        }

        // optimize bounds
        if (isUpdating())
            boundsNeedUpdate = true;
        else
            optimizeBounds();

        // notify roi changed
        roiChanged();
    }

    /**
     * Remove the specified {@link ROI2DArea} from this ROI2DArea
     */
    public void remove(ROI2DArea mask)
    {
        final Rectangle boundsToRemove = mask.getBounds();
        final byte[] maskToRemove = mask.maskData;

        // compute intersection
        final Rectangle intersection = bounds.intersection(boundsToRemove);

        // nothing to remove so nothing to do...
        if (intersection.isEmpty())
            return;

        // calculate offset
        int offDst = ((intersection.y - bounds.y) * bounds.width) + (intersection.x - bounds.x);
        int offSrc = ((intersection.y - boundsToRemove.y) * boundsToRemove.width) + (intersection.x - boundsToRemove.x);
        final byte[] data = maskData;

        for (int y = 0; y < intersection.height; y++)
        {
            for (int x = 0; x < intersection.width; x++)
                if (maskToRemove[offSrc + x] != 0)
                    data[offDst + x] = 0;

            offDst += bounds.width;
            offSrc += boundsToRemove.width;
        }

        // optimize bounds
        if (isUpdating())
            boundsNeedUpdate = true;
        else
            optimizeBounds();

        // notify roi changed
        roiChanged();
    }

    /**
     * Remove the specified {@link BooleanMask2D} from this ROI2DArea
     */
    public void remove(BooleanMask2D mask)
    {
        final Rectangle boundsToRemove = mask.bounds;
        final boolean[] maskToRemove = mask.mask;

        // compute intersection
        final Rectangle intersection = bounds.intersection(boundsToRemove);

        // nothing to remove so nothing to do...
        if (intersection.isEmpty())
            return;

        // calculate offset
        int offDst = ((intersection.y - bounds.y) * bounds.width) + (intersection.x - bounds.x);
        int offSrc = ((intersection.y - boundsToRemove.y) * boundsToRemove.width) + (intersection.x - boundsToRemove.x);
        final byte[] data = maskData;

        for (int y = 0; y < intersection.height; y++)
        {
            for (int x = 0; x < intersection.width; x++)
                if (maskToRemove[offSrc + x])
                    data[offDst + x] = 0;

            offDst += bounds.width;
            offSrc += boundsToRemove.width;
        }

        // optimize bounds
        if (isUpdating())
            boundsNeedUpdate = true;
        else
            optimizeBounds();

        // notify roi changed
        roiChanged();
    }

    /**
     * Update mask by adding/removing the specified shape to/from it.
     * 
     * @param shape
     *        the shape to add in or remove from the mask
     * @param remove
     *        if set to <code>true</code> the shape will be removed from the mask
     * @param inclusive
     *        if we should also consider the edge of the shape to update the mask
     * @param accurate
     *        if set to <code>true</code> the operation will be done to be as pixel accurate as possible
     * @param immediateUpdate
     *        if set to <code>true</code> the bounds of the mask will be immediately recomputed (only meaningful for a
     *        remove operation)
     */
    public void updateMask(Shape shape, boolean remove, boolean inclusive, boolean accurate, boolean immediateUpdate)
    {
        if (remove)
        {
            // outside bounds ? --> nothing to remove so nothing to do...
            if (!bounds.intersects(shape.getBounds2D()))
                return;

            // mark that bounds need to be updated
            if (isUpdating() || !immediateUpdate)
                boundsNeedUpdate = true;
        }
        else
            // update bounds (this update the image dimension if needed)
            addToBounds(shape.getBounds());

        // get image graphics object
        final Graphics2D g = imageMask.createGraphics();

        // we don't need anti aliasing here
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        // force accurate stroke rendering
        if (accurate)
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setComposite(AlphaComposite.Src);
        // set color depending remove or adding to mask
        if (remove)
            g.setColor(new Color(colorModel.getRGB(0), true));
        else
            g.setColor(new Color(colorModel.getRGB(1), true));
        // translate to origin of image and pixel center
        g.translate(-(bounds.x + 0.5d), -(bounds.y + 0.5d));
        // draw shape into the mask
        g.fill(ShapeUtil.getClosedPath(shape));
        // we want edge as well
        if (inclusive)
            g.draw(shape);

        g.dispose();

        // need to optimize bounds
        if (remove && !isUpdating() && immediateUpdate)
            optimizeBounds();

        // notify roi changed
        roiChanged();
    }

    /**
     * Update mask from specified shape
     */
    public void updateMask(Shape shape, boolean remove)
    {
        updateMask(shape, remove, true, false, false);
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

    /**
     * @deprecated useless method.
     */
    @Deprecated
    public boolean canAddPoint()
    {
        return true;
    }

    /**
     * @deprecated useless method.
     */
    @Deprecated
    public boolean canRemovePoint()
    {
        return true;
    }

    /**
     * @deprecated Use {@link #addBrush(Point2D)} instead.
     */
    @Deprecated
    public boolean addPointAt(Point2D pos, boolean ctrl)
    {
        addBrush(pos);
        return true;
    }

    /**
     * @deprecated Use {@link #removeBrush(Point2D)} instead.
     */
    @Deprecated
    public boolean removePointAt(IcyCanvas canvas, Point2D pos)
    {
        removeBrush(pos);
        return true;
    }

    /**
     * @deprecated Useless method.
     */
    @Deprecated
    protected boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint)
    {
        // no selected point for this ROI
        return false;
    }

    /**
     * Add brush point at specified position.
     */
    public void addBrush(Point2D pos)
    {
        getPainter().addToMask(pos);
    }

    /**
     * Remove brush point from the mask at specified position.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removeBrush(Point2D pos)
    {
        getPainter().removeFromMask(pos);
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
        setPoint(x, y, true);
    }

    /**
     * Remove a point from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removePoint(Point pos)
    {
        removePoint(pos.x, pos.y);
    }

    /**
     * Remove a point to the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removePoint(int x, int y)
    {
        setPoint(x, y, false);
    }

    /**
     * Add a rectangle to the mask
     */
    public void addRect(Rectangle r)
    {
        updateMask(r, false, false, true, true);
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
        updateMask(r, true, false, true, true);
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
        updateMask(s, false, false, true, true);
    }

    /**
     * Remove a shape to the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation<br>
     * to refresh the mask bounds.
     */
    public void removeShape(Shape s)
    {
        updateMask(s, true, false, true, true);
    }

    @Override
    public ROI add(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;

            // only if on same position
            if ((getZ() == roi2d.getZ()) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
            {
                if (roi2d instanceof ROI2DArea)
                    add((ROI2DArea) roi2d);
                else if (roi2d instanceof ROI2DShape)
                    updateMask(((ROI2DShape) roi2d).getShape(), false, true, true, true);
                else
                    add(roi2d.getBooleanMask(true));

                return this;
            }
        }

        return super.add(roi, allowCreate);
    }

    @Override
    public ROI intersect(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;

            // only if on same position
            if ((getZ() == roi2d.getZ()) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
            {
                setAsBooleanMask(BooleanMask2D.getIntersection(getBooleanMask(true), roi2d.getBooleanMask(true)));

                return this;
            }
        }

        return super.intersect(roi, allowCreate);
    }

    @Override
    public ROI exclusiveAdd(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;

            // only if on same position
            if ((getZ() == roi2d.getZ()) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
            {
                if (roi2d instanceof ROI2DArea)
                    exclusiveAdd((ROI2DArea) roi2d);
                else
                    exclusiveAdd(roi2d.getBooleanMask(true));

                return this;
            }
        }

        return super.exclusiveAdd(roi, allowCreate);
    }

    @Override
    public ROI subtract(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;

            // only if on same position
            if ((getZ() == roi2d.getZ()) && (getT() == roi2d.getT()) && (getC() == roi2d.getC()))
            {
                if (roi2d instanceof ROI2DArea)
                    remove((ROI2DArea) roi2d);
                else if (roi2d instanceof ROI2DShape)
                    updateMask(((ROI2DShape) roi2d).getShape(), true, true, true, true);
                else
                    remove(roi2d.getBooleanMask(true));

                return this;
            }
        }

        return super.subtract(roi, allowCreate);
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
    public boolean isOverEdge(IcyCanvas canvas, double x, double y)
    {
        // use bigger stroke for isOverEdge test for easier intersection
        final double strk = getAdjustedStroke(canvas) * 3;
        final Rectangle2D rect = new Rectangle2D.Double(x - (strk * 0.5), y - (strk * 0.5), strk, strk);

        // fast intersect test to start with
        if (getBounds2D().intersects(rect))
            // use flatten path, intersects on curved shape return incorrect result
            return ShapeUtil.pathIntersects(bounds.getPathIterator(null, 0.1), rect);

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
        final byte[] data = maskData;

        // scan all pixels, can take sometime if mask is large
        int offset = (yi * bounds.width) + xi;
        for (int j = 0; j < hi; j++)
        {
            for (int i = 0; i < wi; i++)
                if (data[offset++] == 0)
                    return false;

            offset += bounds.width - wi;
        }

        return true;
    }

    /*
     * already calculated
     */
    @Override
    public Rectangle2D computeBounds2D()
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
        final byte[] data = maskData;

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
                if (data[offset++] != 0)
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
        final byte[] data = maskData;

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
                result[offDst++] = (data[offSrc++] != 0);

            offSrc += bounds.width - intersect.width;
            offDst += w - intersect.width;
        }

        return result;
    }

    @Override
    public double computeNumberOfPoints()
    {
        // just count the number of point contained in the mask
        double result = 0d;
        final byte[] data = maskData;

        for (int i = 0; i < data.length; i++)
            if (data[i] != 0)
                result += 1d;

        return result;
    }

    @Override
    public boolean canTranslate()
    {
        return true;
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
    public boolean canSetPosition()
    {
        return true;
    }

    @Override
    public void setPosition2D(Point2D newPosition)
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

        System.arraycopy(mask, 0, maskData, 0, r.width * r.height);

        if (optimizeBounds())
            roiChanged();
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

        final byte[] data = maskData;

        for (int i = 0; i < data.length; i++)
            data[i] = (byte) (booleanMask[i] ? 1 : 0);

        if (optimizeBounds())
            roiChanged();
    }

    public void setAsBooleanMask(int x, int y, int w, int h, boolean[] booleanMask)
    {
        setAsBooleanMask(new Rectangle(x, y, w, h), booleanMask);
    }

    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        // do here global process on ROI change
        if (((ROIEvent) object).getType() == ROIEventType.ROI_CHANGED)
        {
            // update bounds if needed
            if (boundsNeedUpdate && !roiModifiedByMouse)
            {
                if (optimizeBounds())
                    roiChanged();
            }
        }

        super.onChanged(object);
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

            // an error occurred while retrieved XML data
            if (data == null)
                return false;

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

        final Rectangle bnd = (Rectangle) bounds.clone();
        final byte[] data = maskData;

        // invalid --> return false
        if ((bnd.width * bnd.height) != data.length)
            return false;

        // retrieve mask bounds
        XMLUtil.setElementIntValue(node, ID_BOUNDS_X, bnd.x);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_Y, bnd.y);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_W, bnd.width);
        XMLUtil.setElementIntValue(node, ID_BOUNDS_H, bnd.height);
        // set mask data as byte array (we need to clone to avoid any data modification during serialization)        
        XMLUtil.setElementBytesValue(node, ID_BOOLMASK_DATA, data.clone());

        return true;
    }
}

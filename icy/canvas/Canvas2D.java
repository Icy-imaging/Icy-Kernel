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
package icy.canvas;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import icy.canvas.Canvas2D.CanvasView.ImageCache.ImageCacheTile;
import icy.canvas.CanvasLayerEvent.LayersEventType;
import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.menu.ROITask;
import icy.gui.menu.ROITask.ROITaskListener;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.ImageUtil;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.math.Interpolator;
import icy.math.MathUtil;
import icy.math.MultiSmoothMover;
import icy.math.MultiSmoothMover.MultiSmoothMoverAdapter;
import icy.math.SmoothMover;
import icy.math.SmoothMover.SmoothMoveType;
import icy.math.SmoothMover.SmoothMoverAdapter;
import icy.painter.ImageOverlay;
import icy.painter.Overlay;
import icy.preferences.CanvasPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.type.rectangle.Rectangle2DUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.ShapeUtil;
import icy.util.StringUtil;
import plugins.kernel.roi.tool.plugin.ROILineCutterPlugin;

/**
 * New Canvas 2D : default ICY 2D viewer.<br>
 * Support translation / scale and rotation transformation.<br>
 * 
 * @author Stephane
 */
public class Canvas2D extends IcyCanvas2D implements ROITaskListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 8850168605044063031L;

    static final int ICON_SIZE = 20;
    static final int ICON_TARGET_SIZE = 20;

    static final Image ICON_CENTER_IMAGE = ResourceUtil.ICON_CENTER_IMAGE;
    static final Image ICON_FIT_IMAGE = ResourceUtil.ICON_FIT_IMAGE;
    static final Image ICON_FIT_CANVAS = ResourceUtil.ICON_FIT_CANVAS;
    // static final Image ICON_CENTER_IMAGE = ImageUtil.scale(ResourceUtil.ICON_CENTER_IMAGE,
    // ICON_SIZE, ICON_SIZE);
    // static final Image ICON_FIT_IMAGE = ImageUtil.scale(ResourceUtil.ICON_FIT_IMAGE, ICON_SIZE,
    // ICON_SIZE);
    // static final Image ICON_FIT_CANVAS = ImageUtil.scale(ResourceUtil.ICON_FIT_CANVAS, ICON_SIZE,
    // ICON_SIZE);

    static final Image ICON_TARGET = ImageUtil.scale(ResourceUtil.ICON_TARGET, ICON_SIZE, ICON_SIZE);
    static final Image ICON_TARGET_BLACK = ImageUtil.getColorImageFromAlphaImage(ICON_TARGET, Color.black);
    static final Image ICON_TARGET_LIGHT = ImageUtil.getColorImageFromAlphaImage(ICON_TARGET, Color.lightGray);

    /**
     * Possible rounded zoom factor : 0.01 --> 100
     */
    final static double[] zoomRoundedFactors = new double[] {0.01d, 0.02d, 0.0333d, 0.05d, 0.075d, 0.1d, 0.15d, 0.2d,
            0.25d, 0.333d, 0.5d, 0.66d, 0.75d, 1d, 1.25d, 1.5d, 1.75d, 2d, 2.5d, 3d, 4d, 5d, 6.6d, 7.5d, 10d, 15d, 20d,
            30d, 50d, 66d, 75d, 100d};

    /**
     * Image overlay to encapsulate image display in a canvas layer
     */
    protected class Canvas2DImageOverlay extends IcyCanvasImageOverlay
    {
        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (g == null)
                return;

            final List<ImageCacheTile> tiles = canvasView.imageCache.getImageAsTiles();

            // draw image
            for (ImageCacheTile tile : tiles)
                g.drawImage(tile.image, tile.rect.x, tile.rect.y, null);

            if (tiles.isEmpty())
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                // set back canvas coordinate
                g2.transform(getInverseTransform());

                g2.setFont(canvasView.font);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (canvasView.imageCache.isProcessing())
                    // cache not yet built
                    canvasView.drawTextCenter(g2, "Loading...", 0.8f);
                else if (canvasView.imageCache.getNotEnoughMemory())
                    // not enough memory to render image
                    canvasView.drawTextCenter(g2, "Not enough memory to display image", 0.8f);
                else
                    // no image
                    canvasView.drawTextCenter(g2, " No image ", 0.8f);

                g2.dispose();
            }
        }
    }

    public class CanvasMap extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7305605644605013768L;

        private Point mouseMapPos;
        private Point mapStartDragPos;
        private double mapStartRotationZ;
        private boolean mapMoving;
        private boolean mapRotating;

        public CanvasMap()
        {
            super();

            mouseMapPos = new Point(0, 0);
            mapStartDragPos = null;
            mapStartRotationZ = 0;
            mapMoving = false;
            mapRotating = false;

            setBorder(BorderFactory.createRaisedBevelBorder());
            // height will then be fixed to 160
            setPreferredSize(new Dimension(160, 160));

            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        /**
         * Return AffineTransform object which transform an image coordinate to map coordinate.
         */
        public AffineTransform getImageTransform()
        {
            final int w = getWidth();
            final int h = getHeight();
            final int imgW = getImageSizeX();
            final int imgH = getImageSizeY();

            if ((imgW == 0) || (imgH == 0))
                return null;

            final double sx = (double) w / (double) imgW;
            final double sy = (double) h / (double) imgH;
            final double tx, ty;
            final double s;

            // scale to viewport
            if (sx < sy)
            {
                s = sx;
                tx = 0;
                ty = (h - (imgH * s)) / 2;
            }
            else if (sx > sy)
            {
                s = sy;
                ty = 0;
                tx = (w - (imgW * s)) / 2;
            }
            else
            {
                s = sx;
                tx = 0;
                ty = 0;
            }

            final AffineTransform result = new AffineTransform();

            // get transformation to fit image in minimap
            result.translate(tx, ty);
            result.scale(s, s);

            return result;
        }

        /**
         * Transform a CanvasMap point in CanvasView point
         */
        public Point getCanvasPosition(Point p)
        {
            // transform map coordinate to canvas coordinate
            return imageToCanvas(getImagePosition(p));
        }

        /**
         * Transforms a Image point in CanvasView point.
         */
        public Point getCanvasPosition(Point2D.Double p)
        {
            // transform image coordinate to canvas coordinate
            return imageToCanvas(p);
        }

        /**
         * Transforms a CanvasMap point in Image point.
         */
        public Point2D.Double getImagePosition(Point p)
        {
            final AffineTransform trans = getImageTransform();

            try
            {
                // get image coordinates
                return (Point2D.Double) trans.inverseTransform(p, new Point2D.Double());
            }
            catch (Exception ecx)
            {
                return new Point2D.Double(0, 0);
            }
        }

        public boolean isDragging()
        {
            return mapStartDragPos != null;
        }

        protected void updateDrag(InputEvent e)
        {
            // not moving --> exit
            if (!mapMoving)
                return;

            final Point2D.Double startDragImagePoint = getImagePosition(mapStartDragPos);
            final Point2D.Double imagePoint = getImagePosition(mouseMapPos);

            // shift action --> limit to one direction
            if (EventUtil.isShiftDown(e))
            {
                // X drag
                if (Math.abs(mouseMapPos.x - mapStartDragPos.x) > Math.abs(mouseMapPos.y - mapStartDragPos.y))
                    imagePoint.y = startDragImagePoint.y;
                // Y drag
                else
                    imagePoint.x = startDragImagePoint.x;
            }

            // center view on this point (this update mouse canvas position)
            centerOnImage(imagePoint);
            // no need to update mouse canvas position here as it stays at center
        }

        protected void updateRot(InputEvent e)
        {
            // not rotating --> exit
            if (!mapRotating)
                return;

            final Point2D.Double imagePoint = getImagePosition(mouseMapPos);

            // update mouse canvas position from image position
            setMousePos(imageToCanvas(imagePoint));

            // get map center
            final int mapCenterX = getWidth() / 2;
            final int mapCenterY = getHeight() / 2;

            // get last and current mouse position delta with center
            final int lastMouseDeltaPosX = mapStartDragPos.x - mapCenterX;
            final int lastMouseDeltaPosY = mapStartDragPos.y - mapCenterY;
            final int newMouseDeltaPosX = mouseMapPos.x - mapCenterX;
            final int newMouseDeltaPosY = mouseMapPos.y - mapCenterY;

            // get angle in radian between last and current mouse position
            // relative to image center
            double newAngle = Math.atan2(newMouseDeltaPosY, newMouseDeltaPosX);
            double lastAngle = Math.atan2(lastMouseDeltaPosY, lastMouseDeltaPosX);

            // inverse rotation
            double angle = lastAngle - newAngle;

            // control button down --> rotation is enforced
            if (EventUtil.isControlDown(e))
                angle *= 3;

            final double destAngle;

            // shift action --> limit to 45� rotation
            if (EventUtil.isShiftDown(e))
                destAngle = Math.rint((mapStartRotationZ + angle) * (8d / (2 * Math.PI))) * ((2 * Math.PI) / 8d);
            else
                destAngle = mapStartRotationZ + angle;

            // modify rotation with smooth mover
            setRotation(destAngle, true);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            canvasView.handlingMouseMoveEvent = true;
            try
            {
                mouseMapPos = new Point(e.getPoint());

                // get the drag event ?
                if (isDragging())
                {
                    // left button action --> center view on mouse point
                    if (EventUtil.isLeftMouseButton(e))
                    {

                        mapMoving = true;
                        if (mapRotating)
                        {
                            mapRotating = false;
                            // force repaint so the cross is no more visible
                            canvasView.repaint();
                        }

                        updateDrag(e);
                    }
                    else if (EventUtil.isRightMouseButton(e))
                    {
                        mapMoving = false;
                        if (!mapRotating)
                        {
                            mapRotating = true;
                            // force repaint so the cross is visible
                            canvasView.repaint();
                        }

                        updateRot(e);
                    }

                    // consume event
                    e.consume();
                }
            }
            finally
            {
                canvasView.handlingMouseMoveEvent = false;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            mouseMapPos = new Point(e.getPoint());

            // send to canvas view with converted canvas position
            canvasView.onMousePositionChanged(getCanvasPosition(e.getPoint()));
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            // nothing here
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // start drag mouse position
            mapStartDragPos = (Point) e.getPoint().clone();
            // store canvas parameters
            mapStartRotationZ = getRotationZ();

            // left click action --> center view on mouse point
            if (EventUtil.isLeftMouseButton(e))
            {
                final AffineTransform trans = getImageTransform();

                if (trans != null)
                {
                    try
                    {
                        // get image coordinates
                        final Point2D imagePoint = trans.inverseTransform(e.getPoint(), null);
                        // center view on this point
                        centerOnImage(imagePoint.getX(), imagePoint.getY());
                        // update new canvas position
                        setMousePos(imageToCanvas(imagePoint.getX(), imagePoint.getY()));
                        // consume event
                        e.consume();
                    }
                    catch (Exception ecx)
                    {
                        // ignore
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // assume end dragging
            mapStartDragPos = null;
            mapRotating = false;
            mapMoving = false;
            // repaint
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            // nothing here
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            // nothing here
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            // we first center image to mouse position
            final AffineTransform trans = getImageTransform();

            if (trans != null)
            {
                try
                {
                    // get image coordinates
                    final Point2D imagePoint = trans.inverseTransform(e.getPoint(), null);
                    // center view on this point
                    centerOnImage(imagePoint.getX(), imagePoint.getY());
                    // update new canvas position
                    setMousePos(imageToCanvas(imagePoint.getX(), imagePoint.getY()));
                }
                catch (Exception ecx)
                {
                    // ignore
                }
            }

            // send to canvas view
            if (canvasView.onMouseWheelMoved(e.isConsumed(), e.getWheelRotation(), EventUtil.isLeftMouseButton(e),
                    EventUtil.isRightMouseButton(e), EventUtil.isControlDown(e), EventUtil.isShiftDown(e)))
                e.consume();
        }

        public void keyPressed(KeyEvent e)
        {
            // just for the shift key state change
            updateDrag(e);
            updateRot(e);
        }

        public void keyReleased(KeyEvent e)
        {
            // just for the shift key state change
            updateDrag(e);
            updateRot(e);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            final AffineTransform trans = getImageTransform();

            if (trans != null)
            {
                final Graphics2D g2 = (Graphics2D) g.create();
                final List<ImageCacheTile> tiles = canvasView.imageCache.getImageAsTiles();
                // final BufferedImage img = canvasView.imageCache.getImage();

                // draw image
                for (ImageCacheTile tile : tiles)
                {
                    trans.translate(tile.rect.getX(), tile.rect.getY());
                    g2.drawImage(tile.image, trans, null);
                    trans.translate(-tile.rect.getX(), -tile.rect.getY());
                }
                // if (img != null)
                // g2.drawImage(img, trans, null);

                // then apply canvas inverse transformation
                trans.scale(1 / getScaleX(), 1 / getScaleY());
                trans.translate(-getOffsetX(), -getOffsetY());

                final int canvasSizeX = getCanvasSizeX();
                final int canvasSizeY = getCanvasSizeY();
                final int canvasCenterX = canvasSizeX / 2;
                final int canvasCenterY = canvasSizeY / 2;

                trans.translate(canvasCenterX, canvasCenterY);
                trans.rotate(-getRotationZ());
                trans.translate(-canvasCenterX, -canvasCenterY);

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // get transformed rectangle
                final Shape shape = trans.createTransformedShape(new Rectangle(canvasSizeX, canvasSizeY));

                // and draw canvas view rect of the image
                // TODO : the g2.draw(shape) cost sometime !
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.black);
                g2.draw(shape);
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.white);
                g2.draw(shape);

                // rotation helper
                if (mapRotating)
                {
                    final Point2D center = trans.transform(new Point(canvasCenterX, canvasCenterY), null);
                    final int centerX = (int) Math.round(center.getX());
                    final int centerY = (int) Math.round(center.getY());

                    final BasicStroke blackStr = new BasicStroke(4);
                    final BasicStroke greenStr = new BasicStroke(2);

                    g2.setStroke(blackStr);
                    g2.setColor(Color.black);
                    g2.drawLine(centerX - 4, centerY - 4, centerX + 4, centerY + 4);
                    g2.drawLine(centerX - 4, centerY + 4, centerX + 4, centerY - 4);

                    g2.setStroke(greenStr);
                    g2.setColor(Color.green);
                    g2.drawLine(centerX - 4, centerY - 4, centerX + 4, centerY + 4);
                    g2.drawLine(centerX - 4, centerY + 4, centerX + 4, centerY - 4);
                }

                g2.dispose();
            }
        }
    }

    public class CanvasView extends JPanel
            implements ActionListener, MouseWheelListener, MouseListener, MouseMotionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4041355608444378172L;

        public class ImageCache implements Runnable
        {
            public class ImageCacheTile
            {
                final static int TILE_SIZE = 2048;

                public Rectangle rect;
                public BufferedImage image;

                public ImageCacheTile(Rectangle r, BufferedImage img)
                {
                    super();

                    rect = new Rectangle(r);
                    image = img;
                }

                public ImageCacheTile(Rectangle r)
                {
                    this(r, new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB));
                }
            }

            /**
             * image cache
             */
            private List<ImageCacheTile> tiles;

            /**
             * processor
             */
            private final SingleProcessor processor;
            /**
             * internals
             */
            private boolean needRebuild;
            private boolean notEnoughMemory;

            public ImageCache()
            {
                super();

                processor = new SingleProcessor(true, "Canvas2D renderer");
                // we want the processor to stay alive for sometime
                processor.setKeepAliveTime(3, TimeUnit.SECONDS);

                tiles = new ArrayList<ImageCacheTile>();
                needRebuild = true;
                notEnoughMemory = false;

                // build cache
                processor.submit(this);
            }

            public void invalidCache()
            {
                needRebuild = true;
            }

            public boolean isValid()
            {
                return !needRebuild;
            }

            public boolean isProcessing()
            {
                return processor.isProcessing();
            }

            public void refresh()
            {
                // rebuild cache
                if (needRebuild)
                    processor.submit(this);

                // just repaint in the meantime
                getViewComponent().repaint();
            }

            /**
             * @deprecated Caching is done as tiles now so it's better to use {@link #getImageAsTiles()}
             */
            @Deprecated
            public BufferedImage getImage()
            {
                // get original image
                final IcyBufferedImage icyImage = Canvas2D.this.getImage(getPositionT(), getPositionZ(),
                        getPositionC());

                return IcyBufferedImageUtil.toBufferedImage(icyImage, null);
            }

            public List<ImageCacheTile> getImageAsTiles()
            {
                synchronized (tiles)
                {
                    // duplicate list
                    return new ArrayList<ImageCacheTile>(tiles);
                }
            }

            public boolean getNotEnoughMemory()
            {
                return notEnoughMemory;
            }

            @Override
            public void run()
            {
                // important to set it to false at beginning
                needRebuild = false;

                // get original image
                final IcyBufferedImage icyImage = Canvas2D.this.getImage(getPositionT(), getPositionZ(),
                        getPositionC());

                // clear cache so we know we don't have any image at this position
                if (icyImage == null)
                    tiles.clear();
                else
                {
                    try
                    {
                        {
                            // get tiles list
                            final List<Rectangle> newTiles = ImageUtil.getTileList(icyImage.getSizeX(),
                                    icyImage.getSizeY(), ImageCacheTile.TILE_SIZE, ImageCacheTile.TILE_SIZE);
                            final int len = newTiles.size();

                            int indNewTiles = 0;
                            // compare with previous tile list
                            for (ImageCacheTile tile : tiles)
                            {
                                if (indNewTiles < len)
                                {
                                    final Rectangle oldRect = tile.rect;
                                    final Rectangle newRect = newTiles.get(indNewTiles);

                                    // size changed ? --> re alloc image
                                    if ((oldRect.width != newRect.width) || (oldRect.height != newRect.height))
                                        // re alloc image
                                        tile.image = new BufferedImage(newRect.width, newRect.height,
                                                BufferedImage.TYPE_INT_ARGB);
                                    // adjust rect (position) if needed
                                    tile.rect = newRect;
                                }

                                indNewTiles++;
                            }

                            // remove extras tiles
                            while (tiles.size() > len)
                                tiles.remove(tiles.size() - 1);
                            // add extras tiles
                            while (indNewTiles < len)
                                tiles.add(new ImageCacheTile(newTiles.get(indNewTiles++)));

                            // rebuild images
                            final LUT l = getLut();
                            for (ImageCacheTile tile : tiles)
                                tile.image = IcyBufferedImageUtil.toBufferedImage(
                                        IcyBufferedImageUtil.getSubImage(icyImage, tile.rect), tile.image, l);
                        }

                        notEnoughMemory = false;
                    }
                    catch (OutOfMemoryError e)
                    {
                        notEnoughMemory = true;
                    }
                }

                // repaint now
                getViewComponent().repaint();
            }
        }

        /**
         * Image cache
         */
        final ImageCache imageCache;

        /**
         * internals
         */
        final Font font;
        private final Timer refreshTimer;
        private final Timer zoomInfoTimer;
        private final Timer rotationInfoTimer;
        private final SmoothMover zoomInfoAlphaMover;
        private final SmoothMover rotationInfoAlphaMover;
        private String zoomMessage;
        private String rotationMessage;
        Dimension lastSize;
        boolean actived;
        boolean handlingMouseMoveEvent;
        private Point startDragPosition;
        private Point startOffset;
        double curScaleX;
        double curScaleY;
        private double startRotationZ;
        // private Cursor previousCursor;
        boolean moving;
        boolean rotating;
        boolean hasMouseFocus;
        boolean areaSelection;

        public CanvasView()
        {
            super();

            imageCache = new ImageCache();
            actived = false;
            handlingMouseMoveEvent = false;
            startDragPosition = null;
            startOffset = null;
            curScaleX = -1;
            curScaleY = -1;
            // previousCursor = getCursor();
            moving = false;
            rotating = false;
            hasMouseFocus = false;
            areaSelection = false;
            lastSize = getSize();

            font = new Font("Arial", Font.BOLD, 16);

            zoomInfoAlphaMover = new SmoothMover(0);
            zoomInfoAlphaMover.setMoveTime(500);
            zoomInfoAlphaMover.setUpdateDelay(20);
            zoomInfoAlphaMover.addListener(new SmoothMoverAdapter()
            {
                @Override
                public void valueChanged(SmoothMover source, double newValue, int pourcent)
                {
                    // just repaint
                    repaint();
                }
            });
            rotationInfoAlphaMover = new SmoothMover(0);
            rotationInfoAlphaMover.setMoveTime(500);
            rotationInfoAlphaMover.setUpdateDelay(20);
            rotationInfoAlphaMover.addListener(new SmoothMoverAdapter()
            {
                @Override
                public void valueChanged(SmoothMover source, double newValue, int pourcent)
                {
                    // just repaint
                    repaint();
                }
            });

            refreshTimer = new Timer(100, this);
            refreshTimer.setRepeats(false);
            zoomInfoTimer = new Timer(1000, this);
            zoomInfoTimer.setRepeats(false);
            rotationInfoTimer = new Timer(1000, this);
            rotationInfoTimer.setRepeats(false);

            addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    final Dimension newSize = getSize();
                    int extX = 0;
                    int extY = 0;

                    // first time component is displayed ?
                    if (!actived)
                    {
                        // by default we adapt image to canvas size
                        fitImageToCanvas(false);
                        // center image (if cannot fit to canvas size)
                        centerImage();
                        actived = true;
                    }
                    else
                    {
                        // auto FIT enabled
                        if (zoomFitCanvasButton.isSelected())
                            fitImageToCanvas(true);
                        else
                        {
                            // re-center
                            final int dx = newSize.width - lastSize.width;
                            final int dy = newSize.height - lastSize.height;
                            final int dx2 = dx / 2;
                            final int dy2 = dy / 2;
                            // keep trace of lost bit
                            extX = (2 * dx2) - dx;
                            extY = (2 * dy2) - dy;

                            setOffset((int) smoothTransform.getDestValue(TRANS_X) + dx2,
                                    (int) smoothTransform.getDestValue(TRANS_Y) + dy2, true);
                        }
                    }

                    // keep trace of size plus lost part
                    lastSize.width = newSize.width + extX;
                    lastSize.height = newSize.height + extY;
                }
            });

            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        /**
         * Release some stuff
         */
        void shutDown()
        {
            // stop timer and movers
            refreshTimer.stop();
            zoomInfoTimer.stop();
            rotationInfoTimer.stop();
            refreshTimer.removeActionListener(this);
            zoomInfoTimer.removeActionListener(this);
            rotationInfoTimer.removeActionListener(this);
            zoomInfoAlphaMover.shutDown();
            rotationInfoAlphaMover.shutDown();
        }

        /**
         * Returns the internal {@link ImageCache} object.
         */
        public ImageCache getImageCache()
        {
            return imageCache;
        }

        protected void updateDrag(boolean control, boolean shift)
        {
            if (!moving)
                return;

            final Point mousePos = getMousePos();
            final Point delta = new Point(mousePos.x - startDragPosition.x, mousePos.y - startDragPosition.y);

            // shift action --> limit to one direction
            if (shift)
            {
                // X drag
                if (Math.abs(delta.x) > Math.abs(delta.y))
                    delta.y = 0;
                // Y drag
                else
                    delta.x = 0;
            }

            translate(startOffset, delta, control);
        }

        protected void translate(Point startPos, Point delta, boolean control)
        {
            final Point2D.Double deltaD;

            // control button down
            if (control)
                // drag is scaled by current scales factor
                // deltaD = canvasToImageDelta(delta.x, delta.y, 1d / getScaleX(), 1d / getScaleY(),
                // getRotationZ());
                deltaD = canvasToImageDelta(delta.x * 3, delta.y * 3, 1d, 1d, getRotationZ());
            else
                // just get rid of rotation factor
                deltaD = canvasToImageDelta(delta.x, delta.y, 1d, 1d, getRotationZ());

            // modify offset with smooth mover
            setOffset((int) Math.round(startPos.x + deltaD.x), (int) Math.round(startPos.y + deltaD.y), true);
        }

        protected void updateRot(boolean control, boolean shift)
        {
            if (!rotating)
                return;

            final Point mousePos = getMousePos();

            // get canvas center
            final int canvasCenterX = getCanvasSizeX() / 2;
            final int canvasCenterY = getCanvasSizeY() / 2;

            // get last and current mouse position delta with center
            final int lastMouseDeltaPosX = startDragPosition.x - canvasCenterX;
            final int lastMouseDeltaPosY = startDragPosition.y - canvasCenterY;
            final int newMouseDeltaPosX = mousePos.x - canvasCenterX;
            final int newMouseDeltaPosY = mousePos.y - canvasCenterY;

            // get angle in radian between last and current mouse position
            // relative to image center
            double newAngle = Math.atan2(newMouseDeltaPosY, newMouseDeltaPosX);
            double lastAngle = Math.atan2(lastMouseDeltaPosY, lastMouseDeltaPosX);

            double angle = newAngle - lastAngle;

            // control button down --> rotation is enforced
            if (control)
                angle *= 3;

            final double destAngle;

            // shift action --> limit to 45� rotation
            if (shift)
                destAngle = Math.rint((startRotationZ + angle) * (8d / (2 * Math.PI))) * ((2 * Math.PI) / 8d);
            else
                destAngle = startRotationZ + angle;

            // modify rotation with smooth mover
            setRotation(destAngle, true);
        }

        /**
         * Internal canvas process on mouseClicked event.<br>
         * Return true if event should be consumed.
         */
        boolean onMouseClicked(boolean consumed, int clickCount, boolean left, boolean right, boolean control)
        {
            if (!consumed)
            {
                // nothing yet
            }

            return false;
        }

        /**
         * Internal canvas process on mousePressed event.<br>
         * Return true if event should be consumed.
         */
        boolean onMousePressed(boolean consumed, boolean left, boolean right, boolean control)
        {
            // not yet consumed
            if (!consumed)
            {
                final ROITask toolTask = Icy.getMainInterface().getROIRibbonTask();
                final Sequence seq = getSequence();

                // left button press ?
                if (left)
                {
                    // ROI tool selected --> ROI creation
                    if ((toolTask != null) && toolTask.isROITool())
                    {
                        // get the ROI plugin class name
                        final String roiClassName = toolTask.getSelected();

                        // unselect tool before ROI creation unless
                        // control modifier is used for multiple ROI creation
                        if (!control)
                            Icy.getMainInterface().setSelectedTool(null);

                        // only if sequence still live
                        if (seq != null)
                        {
                            // try to create ROI from current selected ROI tool
                            final ROI roi = ROI.create(roiClassName, getMouseImagePos5D());
                            // roi created ? --> it becomes the selected ROI
                            if (roi != null)
                            {
                                roi.setCreating(true);

                                // attach to sequence (hacky method to avoid undoing ROI cutting)
                                seq.addROI(roi, !roiClassName.equals(ROILineCutterPlugin.class.getName()));
                                // then do exclusive selection
                                seq.setSelectedROI(roi);
                            }

                            // consume event
                            return true;
                        }
                    }

                    // start area selection
                    if (control)
                        areaSelection = true;
                }

                // start drag mouse position
                startDragPosition = getMousePos();
                // store canvas parameters
                startOffset = new Point(getOffsetX(), getOffsetY());
                startRotationZ = getRotationZ();

                // repaint
                refresh();
                updateCursor();

                // consume event to activate drag
                return true;
            }

            return false;
        }

        /**
         * Internal canvas process on mouseReleased event.<br>
         * Return true if event should be consumed.
         */
        boolean onMouseReleased(boolean consumed, boolean left, boolean right, boolean control)
        {
            // area selection ?
            if (areaSelection)
            {
                final Sequence seq = getSequence();

                if (seq != null)
                {
                    final List<ROI> rois = seq.getROIs();

                    // we have some rois ?
                    if (rois.size() > 0)
                    {
                        final Rectangle2D area = canvasToImage(getAreaSelection());
                        // 5D area
                        final Rectangle5D area5d = new Rectangle5D.Double(area.getX(), area.getY(), getPositionZ(),
                                getPositionT(), Double.NEGATIVE_INFINITY, area.getWidth(), area.getHeight(), 1d, 1d,
                                Double.POSITIVE_INFINITY);

                        seq.beginUpdate();
                        try
                        {
                            for (ROI roi : rois)
                                roi.setSelected(roi.intersects(area5d));
                        }
                        finally
                        {
                            seq.endUpdate();
                        }
                    }
                }
            }

            // assume end dragging
            startDragPosition = null;
            moving = false;
            rotating = false;
            areaSelection = false;

            // repaint
            refresh();
            updateCursor();

            // consume event
            return true;
        }

        /**
         * Internal canvas process on mouseMove event.<br>
         * Always processed, no consume here.
         */
        void onMousePositionChanged(Point pos)
        {
            handlingMouseMoveEvent = true;
            try
            {
                // update mouse position
                setMousePos(pos);
            }
            finally
            {
                handlingMouseMoveEvent = false;
            }
        }

        /**
         * Internal canvas process on mouseDragged event.<br>
         * Return true if event should be consumed.
         */
        boolean onMouseDragged(boolean consumed, Point pos, boolean left, boolean right, boolean control, boolean shift)
        {
            if (!consumed)
            {
                // canvas get the drag event ?
                if (isDragging())
                {
                    // left mouse button action : translation
                    if (left)
                    {
                        moving = true;
                        if (rotating)
                        {
                            rotating = false;
                            // force repaint so the cross is no more visible
                            canvasView.repaint();
                        }

                        updateDrag(control, shift);
                    }
                    // right mouse button action : rotation
                    else if (right)
                    {
                        moving = false;
                        if (!rotating)
                        {
                            rotating = true;
                            // force repaint so the cross is visible
                            canvasView.repaint();
                        }

                        updateRot(control, shift);
                    }

                    // dragging --> consume event
                    return true;
                }
                // repaint area selection
                else if (areaSelection)
                    repaint();

                // no dragging --> no consume
                return false;
            }

            return false;
        }

        /**
         * Internal canvas process on mouseWheelMoved event.<br>
         * Return true if event should be consumed.
         */
        boolean onMouseWheelMoved(boolean consumed, int wheelRotation, boolean left, boolean right, boolean control,
                boolean shift)
        {
            if (!consumed)
            {
                if (!isDragging())
                {
                    // as soon we manipulate the image with mouse, we want to be focused
                    if (!viewer.hasFocus())
                        viewer.requestFocus();

                    double sx, sy;

                    // adjust mouse wheel depending preference
                    double wr = wheelRotation * CanvasPreferences.getMouseWheelSensitivity();
                    if (CanvasPreferences.getInvertMouseWheelAxis())
                        wr = -wr;

                    sx = 1d + (wr / 100d);
                    sy = 1d + (wr / 100d);

                    // if (wr > 0d)
                    // {
                    // sx = 20d / 19d;
                    // sy = 20d / 19d;
                    // }
                    // else
                    // {
                    // sx = 19d / 20d;
                    // sy = 19d / 20d;
                    // }

                    // control button down --> fast zoom
                    if (control)
                    {
                        sx *= sx;
                        sy *= sy;
                    }

                    // reload current value
                    if (curScaleX == -1)
                        curScaleX = smoothTransform.getDestValue(SCALE_X);
                    if (curScaleY == -1)
                        curScaleY = smoothTransform.getDestValue(SCALE_Y);

                    curScaleX = Math.max(0.01d, Math.min(100d, curScaleX * sx));
                    curScaleY = Math.max(0.01d, Math.min(100d, curScaleY * sy));

                    double newScaleX = curScaleX;
                    double newScaleY = curScaleY;

                    // shift key down --> adjust to closest "round" number
                    if (shift)
                    {
                        newScaleX = MathUtil.closest(newScaleX, zoomRoundedFactors);
                        newScaleY = MathUtil.closest(newScaleY, zoomRoundedFactors);
                    }

                    setScale(newScaleX, newScaleY, false, true);

                    // consume event
                    return true;
                }
            }

            // don't consume this event
            return false;
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            // send mouse event to overlays first
            Canvas2D.this.mouseClick(e);

            // process
            if (onMouseClicked(e.isConsumed(), e.getClickCount(), EventUtil.isLeftMouseButton(e),
                    EventUtil.isRightMouseButton(e), EventUtil.isControlDown(e)))
                e.consume();
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // send mouse event to overlays first
            Canvas2D.this.mousePressed(e);

            // process
            if (onMousePressed(e.isConsumed(), EventUtil.isLeftMouseButton(e), EventUtil.isRightMouseButton(e),
                    EventUtil.isControlDown(e)))
                e.consume();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // send mouse event to overlays first
            Canvas2D.this.mouseReleased(e);

            // process
            if (onMouseReleased(e.isConsumed(), EventUtil.isLeftMouseButton(e), EventUtil.isRightMouseButton(e),
                    EventUtil.isControlDown(e)))
                e.consume();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            hasMouseFocus = true;

            // send mouse event to overlays
            Canvas2D.this.mouseEntered(e);
            // and refresh
            refresh();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            hasMouseFocus = false;

            // send mouse event to overlays
            Canvas2D.this.mouseExited(e);
            // and refresh
            refresh();
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            // process first without consume (update mouse canvas position)
            onMousePositionChanged(e.getPoint());

            // send mouse event to overlays after so mouse canvas position is ok
            Canvas2D.this.mouseMove(e);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            // process first without consume (update mouse canvas position)
            onMousePositionChanged(e.getPoint());

            // send mouse event to overlays after so mouse canvas position is ok
            Canvas2D.this.mouseDrag(e);

            // process
            if (onMouseDragged(e.isConsumed(), e.getPoint(), EventUtil.isLeftMouseButton(e),
                    EventUtil.isRightMouseButton(e), EventUtil.isControlDown(e), EventUtil.isShiftDown(e)))
                e.consume();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            // send mouse event to overlays
            Canvas2D.this.mouseWheelMoved(e);

            // process
            if (onMouseWheelMoved(e.isConsumed(), e.getWheelRotation(), EventUtil.isLeftMouseButton(e),
                    EventUtil.isRightMouseButton(e), EventUtil.isControlDown(e), EventUtil.isShiftDown(e)))
                e.consume();
        }

        public void keyPressed(KeyEvent e)
        {
            final boolean control = EventUtil.isControlDown(e);
            final boolean shift = EventUtil.isShiftDown(e);

            // just for modifiers key state change
            updateDrag(control, shift);
            updateRot(control, shift);
        }

        public void keyReleased(KeyEvent e)
        {
            final boolean control = EventUtil.isControlDown(e);
            final boolean shift = EventUtil.isShiftDown(e);

            // just for modifiers key state change
            updateDrag(control, shift);
            updateRot(control, shift);
        }

        /**
         * Draw specified image layer and others layers on specified {@link Graphics2D} object.
         */
        void drawLayer(Graphics2D g, Sequence seq, Layer layer)
        {
            if (layer.isVisible())
            {
                final float opacity = layer.getOpacity();

                if (opacity != 1f)
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                else
                    g.setComposite(AlphaComposite.SrcOver);

                layer.getOverlay().paint(g, seq, Canvas2D.this);
            }
        }

        /**
         * Draw specified image layer and others layers on specified {@link Graphics2D} object.
         */
        void drawImageAndLayers(Graphics2D g, Layer imageLayer)
        {
            final Sequence seq = getSequence();
            final Layer defaultImageLayer = getImageLayer();

            // global layer visible switch for canvas
            if (isLayersVisible())
            {
                final List<Layer> layers = getLayers(true);

                // draw them in inverse order to have first painter event at top
                for (int i = layers.size() - 1; i >= 0; i--)
                {
                    final Layer layer = layers.get(i);

                    // replace the default image layer by the specified one
                    if (layer == defaultImageLayer)
                        drawLayer(g, seq, imageLayer);
                    else
                        drawLayer(g, seq, layer);
                }
            }
            else
                // display image layer only
                drawLayer(g, seq, imageLayer);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            final int w = getCanvasSizeX();
            final int h = getCanvasSizeY();
            final int canvasCenterX = w / 2;
            final int canvasCenterY = h / 2;

            // background and layers
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                // background
                if (isBackgroundColorEnabled())
                {
                    g2.setBackground(getBackgroundColor());
                    g2.clearRect(0, 0, w, h);
                }

                // apply filtering
                if (CanvasPreferences.getFiltering() && ((getScaleX() < 4d) && (getScaleY() < 4d)))
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                else
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // apply transformation
                g2.transform(getTransform());

                // draw image and layers
                drawImageAndLayers(g2, getImageLayer());

                g2.dispose();
            }

            // area selection
            if (areaSelection)
            {
                final Rectangle area = getAreaSelection();
                final Graphics2D g2 = (Graphics2D) g.create();

                g2.setStroke(new BasicStroke(1));
                g2.setColor(Color.darkGray);
                g2.drawRect(area.x + 1, area.y + 1, area.width, area.height);
                g2.setColor(Color.lightGray);
                g2.drawRect(area.x, area.y, area.width, area.height);

                g2.dispose();
            }

            // synchronized canvas ? display external cursor
            if (!hasMouseFocus)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                final Point mousePos = getMousePos();
                final int x = mousePos.x - (ICON_TARGET_SIZE / 2);
                final int y = mousePos.y - (ICON_TARGET_SIZE / 2);

                // display cursor at mouse pos
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.drawImage(ICON_TARGET_LIGHT, x + 1, y + 1, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.drawImage(ICON_TARGET_BLACK, x, y, null);

                g2.dispose();
            }

            // display zoom info
            if (zoomInfoAlphaMover.getValue() > 0)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                g2.setFont(font);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawTextBottomRight(g2, zoomMessage, (float) zoomInfoAlphaMover.getValue());

                g2.dispose();
            }

            // display rotation info
            if (rotationInfoAlphaMover.getValue() > 0)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                g2.setFont(font);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawTextTopRight(g2, rotationMessage, (float) rotationInfoAlphaMover.getValue());

                g2.dispose();
            }

            // rotation helper
            if (rotating)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                final BasicStroke blackStr = new BasicStroke(5);
                final BasicStroke greenStr = new BasicStroke(3);

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setStroke(blackStr);
                g2.setColor(Color.black);
                g2.drawLine(canvasCenterX - 5, canvasCenterY - 5, canvasCenterX + 5, canvasCenterY + 5);
                g2.drawLine(canvasCenterX - 5, canvasCenterY + 5, canvasCenterX + 5, canvasCenterY - 5);

                g2.setStroke(greenStr);
                g2.setColor(Color.green);
                g2.drawLine(canvasCenterX - 5, canvasCenterY - 5, canvasCenterX + 5, canvasCenterY + 5);
                g2.drawLine(canvasCenterX - 5, canvasCenterY + 5, canvasCenterX + 5, canvasCenterY - 5);

                g2.dispose();
            }

            // image or layers changed during repaint --> refresh again
            if (!isCacheValid())
                refresh();
            // cache is being rebuild --> refresh to show progression
            else if (imageCache.isProcessing())
                refreshLater(100);

            // repaint minimap to reflect change (simplest way to refresh minimap)
            canvasMap.repaint();
        }

        public void drawTextBottomRight(Graphics2D g, String text, float alpha)
        {
            final Rectangle2D rect = GraphicsUtil.getStringBounds(g, text);
            final int w = (int) rect.getWidth();
            final int h = (int) rect.getHeight();
            final int x = getWidth() - (w + 8 + 2);
            final int y = getHeight() - (h + 8 + 2);

            g.setColor(Color.gray);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.fillRoundRect(x, y, w + 8, h + 8, 8, 8);

            g.setColor(Color.white);
            g.drawString(text, x + 4, y + 2 + h);
        }

        public void drawTextTopRight(Graphics2D g, String text, float alpha)
        {
            final Rectangle2D rect = GraphicsUtil.getStringBounds(g, text);
            final int w = (int) rect.getWidth();
            final int h = (int) rect.getHeight();
            final int x = getWidth() - (w + 8 + 2);
            final int y = 2;

            g.setColor(Color.gray);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.fillRoundRect(x, y, w + 8, h + 8, 8, 8);

            g.setColor(Color.white);
            g.drawString(text, x + 4, y + 2 + h);
        }

        public void drawTextCenter(Graphics2D g, String text, float alpha)
        {
            final Rectangle2D rect = GraphicsUtil.getStringBounds(g, text);
            final int w = (int) rect.getWidth();
            final int h = (int) rect.getHeight();
            final int x = (getWidth() - (w + 8 + 2)) / 2;
            final int y = (getHeight() - (h + 8 + 2)) / 2;

            g.setColor(Color.gray);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.fillRoundRect(x, y, w + 8, h + 8, 8, 8);

            g.setColor(Color.white);
            g.drawString(text, x + 4, y + 2 + h);
        }

        /**
         * Update mouse cursor
         */
        protected void updateCursor()
        {
            // final Cursor cursor = getCursor();
            //
            // // save previous cursor if different from HAND
            // if (cursor.getType() != Cursor.HAND_CURSOR)
            // previousCursor = cursor;
            //
            if (isDragging())
            {
                GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                return;
            }

            if (areaSelection)
            {
                GuiUtil.setCursor(this, Cursor.CROSSHAIR_CURSOR);
                return;
            }

            final Sequence seq = getSequence();

            if (seq != null)
            {
                final ROI overlappedRoi = seq.getFocusedROI();

                // overlapping an ROI ?
                if (overlappedRoi != null)
                {
                    final Layer layer = getLayer(overlappedRoi);

                    if ((layer != null) && layer.isVisible())
                    {
                        GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                        return;
                    }
                }

                final List<ROI> selectedRois = seq.getSelectedROIs();

                // search if we are overriding ROI control points
                for (ROI selectedRoi : selectedRois)
                {
                    final Layer layer = getLayer(selectedRoi);

                    if ((layer != null) && layer.isVisible() && selectedRoi.hasSelectedPoint())
                    {
                        GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                        return;
                    }
                }
            }

            // setCursor(previousCursor);
            GuiUtil.setCursor(this, Cursor.DEFAULT_CURSOR);
        }

        public void refresh()
        {
            imageCache.refresh();
        }

        /**
         * Refresh in sometime
         */
        public void refreshLater(int milli)
        {
            refreshTimer.setInitialDelay(milli);
            refreshTimer.start();
        }

        /**
         * Display zoom message for the specified amount of time (in ms)
         */
        public void setZoomMessage(String value, int delay)
        {
            zoomMessage = value;

            if (StringUtil.isEmpty(value))
            {
                zoomInfoTimer.stop();
                zoomInfoAlphaMover.setValue(0d);
            }
            else
            {
                zoomInfoAlphaMover.setValue(0.8d);
                zoomInfoTimer.setInitialDelay(delay);
                zoomInfoTimer.restart();
            }
        }

        /**
         * Display rotation message for the specified amount of time (in ms)
         */
        public void setRotationMessage(String value, int delay)
        {
            rotationMessage = value;

            if (StringUtil.isEmpty(value))
            {
                rotationInfoTimer.stop();
                rotationInfoAlphaMover.setValue(0d);
            }
            else
            {
                rotationInfoAlphaMover.setValue(0.8d);
                rotationInfoTimer.setInitialDelay(delay);
                rotationInfoTimer.restart();
            }
        }

        public void imageChanged()
        {
            imageCache.invalidCache();
        }

        public void layersChanged()
        {
            // nothing here
        }

        public boolean isDragging()
        {
            return !areaSelection && (startDragPosition != null);
        }

        public boolean isCacheValid()
        {
            return imageCache.isValid();
        }

        /**
         * Returns the current Rectangle region of the area selection.<br>
         * It returns <code>null</code> if we are not in area selection mode
         */
        public Rectangle getAreaSelection()
        {
            if (!areaSelection)
                return null;

            final int x, y;
            final int w, h;
            final Point mp = getMousePos();

            if (mp.x > startDragPosition.x)
            {
                x = startDragPosition.x;
                w = mp.x - x;
            }
            else
            {
                x = mp.x;
                w = startDragPosition.x - x;
            }
            if (mp.y > startDragPosition.y)
            {
                y = startDragPosition.y;
                h = mp.y - y;
            }
            else
            {
                y = mp.y;
                h = startDragPosition.y - y;
            }

            return new Rectangle(x, y, w, h);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final Object source = e.getSource();

            if (source == refreshTimer)
                refresh();
            else if (source == zoomInfoTimer)
                zoomInfoAlphaMover.moveTo(0);
            else if (source == rotationInfoTimer)
                rotationInfoAlphaMover.moveTo(0);
        }
    }

    /**
     * * index 0 : translation X (int) index 1 : translation Y (int) index 2 :
     * scale X (double) index 3 : scale Y (double) index 4 : rotation angle
     * (double)
     * 
     * @author Stephane
     */
    static class Canvas2DSmoothMover extends MultiSmoothMover
    {
        public Canvas2DSmoothMover(int size, SmoothMoveType type)
        {
            super(size, type);
        }

        public Canvas2DSmoothMover(int size)
        {
            super(size);
        }

        @Override
        public void moveTo(int index, double value)
        {
            final double v;

            // format value for radian 0..2PI range
            if (index == ROT)
                v = MathUtil.formatRadianAngle(value);
            else
                v = value;

            if (destValues[index] != v)
            {
                destValues[index] = v;
                // start movement
                start(index, System.currentTimeMillis());
            }
        }

        @Override
        public void moveTo(double[] values)
        {
            final int maxInd = Math.min(values.length, destValues.length);

            // first we check we have at least one value which had changed
            boolean changed = false;
            for (int index = 0; index < maxInd; index++)
            {
                final double value;

                // format value for radian 0..2PI range
                if (index == ROT)
                    value = MathUtil.formatRadianAngle(values[index]);
                else
                    value = values[index];

                if (destValues[index] != value)
                {
                    changed = true;
                    break;
                }
            }

            // value changed ?
            if (changed)
            {
                // better synchronization for multiple changes
                final long time = System.currentTimeMillis();

                for (int index = 0; index < maxInd; index++)
                {
                    final double value;

                    // format value for radian 0..2PI range
                    if (index == ROT)
                        value = MathUtil.formatRadianAngle(values[index]);
                    else
                        value = values[index];

                    destValues[index] = value;
                    // start movement
                    start(index, time);
                }
            }
        }

        @Override
        public void setValue(int index, double value)
        {
            final double v;

            // format value for radian 0..2PI range
            if (index == ROT)
                v = MathUtil.formatRadianAngle(value);
            else
                v = value;

            // stop current movement
            stop(index);
            // directly set value
            destValues[index] = v;
            setCurrentValue(index, v, 100);
        }

        @Override
        public void setValues(double[] values)
        {
            final int maxInd = Math.min(values.length, destValues.length);

            for (int index = 0; index < maxInd; index++)
            {
                final double value;

                // format value for radian 0..2PI range
                if (index == ROT)
                    value = MathUtil.formatRadianAngle(values[index]);
                else
                    value = values[index];

                // stop current movement
                stop(index);
                // directly set value
                destValues[index] = value;
                setCurrentValue(index, value, 100);
            }
        }

        @Override
        protected void setCurrentValue(int index, double value, int pourcent)
        {
            final double v;

            // format value for radian 0..2PI range
            if (index == ROT)
                v = MathUtil.formatRadianAngle(value);
            else
                v = value;

            if (currentValues[index] != v)
            {
                currentValues[index] = v;
                // notify value changed
                changed(index, v, pourcent);
            }
        }

        @Override
        protected void start(int index, long time)
        {
            final double current = currentValues[index];
            final double dest;

            if (index == ROT)
            {
                double d = destValues[index];

                // choose shorter path
                if (Math.abs(d - current) > Math.PI)
                {
                    if (d > Math.PI)
                        dest = d - (Math.PI * 2);
                    else
                        dest = d + (Math.PI * 2);
                }
                else
                    dest = d;
            }
            else
                dest = destValues[index];

            // number of step to reach final value
            final int size = Math.max(moveTime / getUpdateDelay(), 1);

            // calculate interpolation
            switch (type)
            {
                case NONE:
                    stepValues[index] = new double[2];
                    stepValues[index][0] = current;
                    stepValues[index][1] = dest;
                    break;

                case LINEAR:
                    stepValues[index] = Interpolator.doLinearInterpolation(current, dest, size);
                    break;

                case LOG:
                    stepValues[index] = Interpolator.doLogInterpolation(current, dest, size);
                    break;

                case EXP:
                    stepValues[index] = Interpolator.doExpInterpolation(current, dest, size);
                    break;
            }

            // notify and start
            if (!isMoving(index))
            {
                moveStarted(index, time);
                moving[index] = true;
            }
            else
                moveModified(index, time);
        }
    }

    /**
     * pref ID
     */
    static final String PREF_CANVAS2D_ID = "Canvas2D";

    static final String ID_FIT_CANVAS = "fitCanvas";
    static final String ID_BG_COLOR_ENABLED = "bgColorEnabled";
    static final String ID_BG_COLOR = "bgColor";

    final static int TRANS_X = 0;
    final static int TRANS_Y = 1;
    final static int SCALE_X = 2;
    final static int SCALE_Y = 3;
    final static int ROT = 4;

    /**
     * view where we draw
     */
    final CanvasView canvasView;

    /**
     * minimap in canvas panel
     */
    final CanvasMap canvasMap;

    /**
     * GUI & setting
     */
    IcyToggleButton zoomFitCanvasButton;
    Color bgColor;

    /**
     * preferences
     */
    final XMLPreferences preferences;

    /**
     * The smoothTransform object contains all transform informations<br>
     */
    final Canvas2DSmoothMover smoothTransform;

    // internal
    String textInfos;
    Dimension previousImageSize;
    boolean modifyingZoom;
    boolean modifyingRotation;

    public Canvas2D(Viewer viewer)
    {
        super(viewer);

        // all channel visible at once
        posC = -1;

        // view panel
        canvasView = new CanvasView();
        // mini map
        canvasMap = new CanvasMap();

        // variables initialization
        preferences = CanvasPreferences.getPreferences().node(PREF_CANVAS2D_ID);

        // init transform (5 values, log transition type)
        smoothTransform = new Canvas2DSmoothMover(5, SmoothMoveType.LOG);
        // initials transform values
        smoothTransform.setValues(new double[] {0d, 0d, 1d, 1d, 0d});
        textInfos = null;
        modifyingZoom = false;
        modifyingRotation = false;
        previousImageSize = new Dimension(getImageSizeX(), getImageSizeY());

        smoothTransform.addListener(new MultiSmoothMoverAdapter()
        {
            @Override
            public void valueChanged(MultiSmoothMover source, int index, double newValue, int pourcent)
            {
                // notify canvas transformation has changed
                switch (index)
                {
                    case TRANS_X:
                        offsetChanged(DimensionId.X);
                        break;

                    case TRANS_Y:
                        offsetChanged(DimensionId.Y);
                        break;

                    case SCALE_X:
                        scaleChanged(DimensionId.X);
                        break;

                    case SCALE_Y:
                        scaleChanged(DimensionId.Y);
                        break;

                    case ROT:
                        rotationChanged(DimensionId.Z);
                        break;
                }
            }

            @Override
            public void moveEnded(MultiSmoothMover source, int index, double value)
            {
                // scale move ended, we can fix notify canvas transformation has changed
                switch (index)
                {
                    case SCALE_X:
                        canvasView.curScaleX = -1;
                        break;

                    case SCALE_Y:
                        canvasView.curScaleY = -1;
                }
            }
        });

        // want fast transition
        smoothTransform.setMoveTime(400);
        // and very smooth refresh if possible
        smoothTransform.setUpdateDelay(20);

        // build inspector canvas panel & GUI stuff
        buildSettingGUI();

        // set view in center
        add(canvasView, BorderLayout.CENTER);

        // mouse infos panel setting: we want to see values for X/Y only (2D view)
        mouseInfPanel.setInfoXVisible(true);
        mouseInfPanel.setInfoYVisible(true);
        // Z and T values are already visible in Z/T navigator bar
        mouseInfPanel.setInfoZVisible(false);
        mouseInfPanel.setInfoTVisible(false);
        // no C navigation with this canvas (all channels visible)
        mouseInfPanel.setInfoCVisible(false);
        // data and color information visible
        mouseInfPanel.setInfoDataVisible(true);
        mouseInfPanel.setInfoColorVisible(true);

        updateZNav();
        updateTNav();

        final ROITask trt = Icy.getMainInterface().getROIRibbonTask();
        if (trt != null)
            trt.addListener(this);
    }

    @Override
    public void shutDown()
    {
        super.shutDown();

        canvasView.shutDown();

        // shutdown mover object (else internal timer keep a reference to Canvas2D)
        smoothTransform.shutDown();

        final ROITask trt = Icy.getMainInterface().getROIRibbonTask();
        if (trt != null)
            trt.removeListener(this);
    }

    @Override
    protected Overlay createImageOverlay()
    {
        return new Canvas2DImageOverlay();
    }

    public Canvas2DSettingPanel getCanvasSettingPanel()
    {
        return (Canvas2DSettingPanel) panel;
    }

    /**
     * Build canvas panel for inspector
     */
    private void buildSettingGUI()
    {
        // canvas setting panel (for inspector)
        panel = new Canvas2DSettingPanel(this);
        // add the map to it
        panel.add(canvasMap, BorderLayout.CENTER);

        // fit canvas toggle
        zoomFitCanvasButton = new IcyToggleButton(new IcyIcon(ICON_FIT_CANVAS));
        zoomFitCanvasButton.setSelected(preferences.getBoolean(ID_FIT_CANVAS, false));
        zoomFitCanvasButton.setFocusable(false);
        zoomFitCanvasButton.setToolTipText("Keep image fitting to window size");
        zoomFitCanvasButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean selected = zoomFitCanvasButton.isSelected();

                preferences.putBoolean(ID_FIT_CANVAS, selected);

                // fit if enabled
                if (selected)
                    fitImageToCanvas(true);
            }
        });
    }

    @Override
    public Component getViewComponent()
    {
        return canvasView;
    }

    /**
     * Return the {@link CanvasView} component of Canvas2D.
     */
    public CanvasView getCanvasView()
    {
        return canvasView;
    }

    /**
     * Return the {@link CanvasMap} component of Canvas2D.
     */
    public CanvasMap getCanvasMap()
    {
        return canvasMap;
    }

    @Override
    public void customizeToolbar(JToolBar toolBar)
    {
        toolBar.addSeparator();
        toolBar.add(zoomFitCanvasButton);
        // toolBar.addSeparator();
        // toolBar.add(zoomFitImageButton);
        // toolBar.add(centerImageButton);
    }

    @Override
    public void fitImageToCanvas()
    {
        fitImageToCanvas(false);
    }

    /**
     * Change zoom so image fit in canvas view dimension
     */
    public void fitImageToCanvas(boolean smooth)
    {
        // search best ratio
        final Point2D.Double s = getFitImageToCanvasScale();

        if (s != null)
        {
            final double scale = Math.min(s.x, s.y);

            // set mouse position on image center
            centerMouseOnImage();
            // apply scale
            setScale(scale, scale, true, smooth);
        }
    }

    @Override
    public void fitCanvasToImage()
    {
        // center image first
        centerImage();

        super.fitCanvasToImage();
    }

    @Override
    public void centerOnImage(double x, double y)
    {
        // get point on canvas
        final Point pt = imageToCanvas(x, y);
        final int canvasCenterX = getCanvasSizeX() / 2;
        final int canvasCenterY = getCanvasSizeY() / 2;

        final Point2D.Double newTrans = canvasToImageDelta(canvasCenterX - pt.x, canvasCenterY - pt.y, 1d, 1d,
                getRotationZ());

        setOffset((int) (smoothTransform.getDestValue(TRANS_X) + Math.round(newTrans.x)),
                (int) (smoothTransform.getDestValue(TRANS_Y) + Math.round(newTrans.y)), false);
    }

    /**
     * Set mouse position on image center
     */
    protected void centerMouseOnImage()
    {
        setMouseImagePos(getImageSizeX() / 2, getImageSizeY() / 2);
    }

    /**
     * Set mouse position on current view center
     */
    protected void centerMouseOnView()
    {
        setMousePos(getCanvasSizeX() >> 1, getCanvasSizeY() >> 1);
    }

    @Override
    public void centerOn(Rectangle region)
    {
        final Rectangle2D imageRectMax = Rectangle2DUtil
                .getScaledRectangle(new Rectangle(getImageSizeX(), getImageSizeY()), 1.5d, true);

        Rectangle2D adjusted = Rectangle2DUtil.getScaledRectangle(region, 2d, true);

        // get undersize
        double wu = Math.max(0, 100d - adjusted.getWidth());
        double hu = Math.max(0, 100d - adjusted.getHeight());

        // enlarge a bit to have at least a 100x100 rectangle
        if ((wu > 0) || (hu > 0))
            ShapeUtil.enlarge(adjusted, wu, hu, true);

        // get overflow on original image size
        double wo = Math.max(0, adjusted.getWidth() - imageRectMax.getWidth());
        double ho = Math.max(0, adjusted.getHeight() - imageRectMax.getHeight());

        // reduce a bit to clip on max image size
        if ((wo > 0) || (ho > 0))
            ShapeUtil.enlarge(adjusted, -wo, -ho, true);

        final Rectangle viewRect = new Rectangle(getViewComponent().getSize());

        // calculate new scale factors
        final double scaleX = viewRect.width / adjusted.getWidth();
        final double scaleY = viewRect.height / adjusted.getHeight();

        // get point on canvas
        final int offX;
        final int offY;
        final double newScale;

        if (scaleX < scaleY)
        {
            newScale = scaleX;
            // use scale X, adapt offset Y
            offX = (int) (adjusted.getX() * newScale);
            offY = (int) ((adjusted.getY() * newScale) - ((viewRect.height - (adjusted.getHeight() * newScale)) / 2d));
        }
        else
        {
            newScale = scaleY;
            // use scale Y, adapt offset X
            offX = (int) ((adjusted.getX() * newScale) - ((viewRect.width - (adjusted.getWidth() * newScale)) / 2d));
            offY = (int) (adjusted.getY() * newScale);
        }

        // apply new position and scaling
        setTransform(-offX, -offY, newScale, newScale, smoothTransform.getDestValue(ROT), true);
    }

    /**
     * Set transform
     */
    protected void setTransform(int tx, int ty, double sx, double sy, double rot, boolean smooth)
    {
        final double[] values = new double[] {tx, ty, sx, sy, rot};

        // modify all at once for synchronized change events
        if (smooth)
            smoothTransform.moveTo(values);
        else
            smoothTransform.setValues(values);
    }

    /**
     * Set offset X and Y.<br>
     * 
     * @param smooth
     *        use smooth transition
     */
    public void setOffset(int x, int y, boolean smooth)
    {
        final int adjX = Math.min(getMaxOffsetX(), Math.max(getMinOffsetX(), x));
        final int adjY = Math.min(getMaxOffsetY(), Math.max(getMinOffsetY(), y));

        setTransform(adjX, adjY, smoothTransform.getDestValue(SCALE_X), smoothTransform.getDestValue(SCALE_Y),
                smoothTransform.getDestValue(ROT), smooth);
    }

    /**
     * Set zoom factor (this use the smart zoom position and smooth transition).
     * 
     * @param center
     *        if true then zoom is centered to current view else zoom is
     *        centered using current mouse position
     * @param smooth
     *        use smooth transition
     */
    public void setScale(double factor, boolean center, boolean smooth)
    {
        // first we center mouse position if requested
        if (center)
            centerMouseOnImage();

        setScale(factor, factor, true, smooth);
    }

    /**
     * Set zoom X and Y factor.<br>
     * This use the smart zoom position and smooth transition.
     * 
     * @param mouseCentered
     *        if true the current mouse image position will becomes the
     *        center of viewport else the current mouse image position will
     *        keep its place.
     * @param smooth
     *        use smooth transition
     */
    public void setScale(double x, double y, boolean mouseCentered, boolean smooth)
    {
        final Sequence seq = getSequence();
        // there is no way of changing scale if no sequence
        if (seq == null)
            return;

        // get destination rot
        final double rot = smoothTransform.getDestValue(ROT);
        // limit min and max zoom ratio
        final double newScaleX = Math.max(0.01d, Math.min(100d, x));
        final double newScaleY = Math.max(0.01d, Math.min(100d, y));

        // get new mouse position on canvas pixel
        final Point newMouseCanvasPos = imageToCanvas(mouseImagePos.x, mouseImagePos.y, 0, 0, newScaleX, newScaleY,
                rot);
        // new image size
        final int newImgSizeX = (int) Math.ceil(getImageSizeX() * newScaleX);
        final int newImgSizeY = (int) Math.ceil(getImageSizeY() * newScaleY);
        // canvas center
        final int canvasCenterX = getCanvasSizeX() / 2;
        final int canvasCenterY = getCanvasSizeY() / 2;

        final Point2D.Double newTrans;

        if (mouseCentered)
        {
            // we want the mouse image point to becomes the canvas center (take rotation in account)
            newTrans = canvasToImageDelta(canvasCenterX - newMouseCanvasPos.x, canvasCenterY - newMouseCanvasPos.y, 1d,
                    1d, rot);
        }
        else
        {
            final Point mousePos = getMousePos();
            // we want the mouse image point to keep its place (take rotation in account)
            newTrans = canvasToImageDelta(mousePos.x - newMouseCanvasPos.x, mousePos.y - newMouseCanvasPos.y, 1d, 1d,
                    rot);
        }

        // limit translation to min / max offset
        final int newTransX = Math.min(canvasCenterX,
                Math.max(canvasCenterX - newImgSizeX, (int) Math.round(newTrans.x)));
        final int newTransY = Math.min(canvasCenterY,
                Math.max(canvasCenterY - newImgSizeY, (int) Math.round(newTrans.y)));

        setTransform(newTransX, newTransY, newScaleX, newScaleY, rot, smooth);
    }

    /**
     * Set zoom X and Y factor.<br>
     * This is direct affectation method without position modification.
     * 
     * @param smooth
     *        use smooth transition
     */
    public void setScale(double x, double y, boolean smooth)
    {
        setTransform((int) smoothTransform.getDestValue(TRANS_X), (int) smoothTransform.getDestValue(TRANS_Y), x, y,
                smoothTransform.getDestValue(ROT), smooth);
    }

    /**
     * Set zoom factor.<br>
     * Only here for backward compatibility with ICY4IJ.<br>
     * Zoom is center on image.
     * 
     * @deprecated use setScale(...) instead
     */
    @Deprecated
    public void setZoom(float zoom)
    {
        // set mouse position on image center
        centerMouseOnImage();
        // then apply zoom
        setScale(zoom, zoom, true, false);
    }

    /**
     * Get destination image size X in canvas pixel coordinate
     */
    public int getDestImageCanvasSizeX()
    {
        return (int) Math.ceil(getImageSizeX() * smoothTransform.getDestValue(SCALE_X));
    }

    /**
     * Get destination image size Y in canvas pixel coordinate
     */
    public int getDestImageCanvasSizeY()
    {
        return (int) Math.ceil(getImageSizeY() * smoothTransform.getDestValue(SCALE_Y));
    }

    void backgroundColorEnabledChanged()
    {
        // save to preference
        preferences.putBoolean(ID_BG_COLOR_ENABLED, isBackgroundColorEnabled());
        // and refresh view
        canvasView.refresh();
    }

    void backgroundColorChanged()
    {
        // save to preference
        preferences.putInt(ID_BG_COLOR, getBackgroundColor().getRGB());
        // and refresh view
        canvasView.refresh();
    }

    /**
     * Returns the background color enabled state
     */
    public boolean isBackgroundColorEnabled()
    {
        return getCanvasSettingPanel().isBackgroundColorEnabled();
    }

    /**
     * Sets the background color enabled state
     */
    public void setBackgroundColorEnabled(boolean value)
    {
        getCanvasSettingPanel().setBackgroundColorEnabled(value);
    }

    /**
     * Returns the background color
     */
    public Color getBackgroundColor()
    {
        return getCanvasSettingPanel().getBackgroundColor();
    }

    /**
     * Sets the background color
     */
    public void setBackgroundColor(Color color)
    {
        getCanvasSettingPanel().setBackgroundColor(color);
    }

    /**
     * @return the automatic 'fit to canvas' state
     */
    public boolean getFitToCanvas()
    {
        return zoomFitCanvasButton.isSelected();
    }

    /**
     * Sets the automatic 'fit to canvas' state
     */
    public void setFitToCanvas(boolean value)
    {
        zoomFitCanvasButton.setSelected(value);
    }

    @Override
    public boolean isSynchronizationSupported()
    {
        return true;
    }

    protected int getMinOffsetX()
    {
        return (getCanvasSizeX() / 2) - getDestImageCanvasSizeX();
    }

    protected int getMaxOffsetX()
    {
        return (getCanvasSizeX() / 2);
    }

    protected int getMinOffsetY()
    {
        return (getCanvasSizeY() / 2) - getDestImageCanvasSizeY();
    }

    protected int getMaxOffsetY()
    {
        return (getCanvasSizeY() / 2);
    }

    @Override
    public int getOffsetX()
    {
        // can be called before constructor ended
        if (smoothTransform == null)
            return 0;

        return (int) smoothTransform.getValue(TRANS_X);
    }

    @Override
    public int getOffsetY()
    {
        // can be called before constructor ended
        if (smoothTransform == null)
            return 0;

        return (int) smoothTransform.getValue(TRANS_Y);
    }

    @Override
    public double getScaleX()
    {
        // can be called before constructor ended
        if (smoothTransform == null)
            return 0d;

        return smoothTransform.getValue(SCALE_X);
    }

    @Override
    public double getScaleY()
    {
        // can be called before constructor ended
        if (smoothTransform == null)
            return 0d;

        return smoothTransform.getValue(SCALE_Y);
    }

    @Override
    public double getRotationZ()
    {
        // can be called before constructor ended
        if (smoothTransform == null)
            return 0d;

        return smoothTransform.getValue(ROT);
    }

    /**
     * Only here for backward compatibility with ICY4IJ plugin.
     * 
     * @deprecated use getScaleX() or getScaleY() instead
     */
    @Deprecated
    public double getZoomFactor()
    {
        return getScaleX();
    }

    /**
     * We want angle to be in [0..2*PI]
     */
    public double getRotation()
    {
        return MathUtil.formatRadianAngle(getRotationZ());
    }

    @Override
    protected void setPositionCInternal(int c)
    {
        // not supported in this canvas, C should stay at -1
    }

    @Override
    protected void setOffsetXInternal(int value)
    {
        // this will automatically call the offsetChanged() event
        smoothTransform.setValue(TRANS_X, Math.min(getMaxOffsetX(), Math.max(getMinOffsetX(), value)));
    }

    @Override
    protected void setOffsetYInternal(int value)
    {
        // this will automatically call the offsetChanged() event
        smoothTransform.setValue(TRANS_Y, Math.min(getMaxOffsetY(), Math.max(getMinOffsetY(), value)));
    }

    @Override
    protected void setScaleXInternal(double value)
    {
        // this will automatically call the scaledChanged() event
        smoothTransform.setValue(SCALE_X, value);
        canvasView.curScaleX = value;
    }

    @Override
    protected void setScaleYInternal(double value)
    {
        // this will automatically call the scaledChanged() event
        smoothTransform.setValue(SCALE_Y, value);
        canvasView.curScaleY = value;
    }

    @Override
    protected void setRotationZInternal(double value)
    {
        // this will automatically call the rotationChanged() event
        smoothTransform.setValue(ROT, value);
    }

    /**
     * Set rotation angle (radian).<br>
     * 
     * @param smooth
     *        use smooth transition
     */
    public void setRotation(double value, boolean smooth)
    {
        setTransform((int) smoothTransform.getDestValue(TRANS_X), (int) smoothTransform.getDestValue(TRANS_Y),
                smoothTransform.getDestValue(SCALE_X), smoothTransform.getDestValue(SCALE_Y), value, smooth);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // send to overlays
        super.keyPressed(e);

        if (!e.isConsumed())
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_R:
                    // reset zoom and rotation
                    setRotation(0, false);
                    fitImageToCanvas(true);

                    // also reset LUT
                    if (EventUtil.isShiftDown(e, true))
                    {
                        final Sequence sequence = getSequence();
                        final Viewer viewer = getViewer();
                        if ((viewer != null) && (sequence != null))
                            viewer.setLut(sequence.createCompatibleLUT());
                    }

                    e.consume();
                    break;

                case KeyEvent.VK_LEFT:
                    if (EventUtil.isMenuControlDown(e, true))
                        setPositionT(Math.max(getPositionT() - 5, 0));
                    else
                        setPositionT(Math.max(getPositionT() - 1, 0));
                    e.consume();
                    break;

                case KeyEvent.VK_RIGHT:
                    if (EventUtil.isMenuControlDown(e, true))
                        setPositionT(getPositionT() + 5);
                    else
                        setPositionT(getPositionT() + 1);
                    e.consume();
                    break;

                case KeyEvent.VK_UP:
                    if (EventUtil.isMenuControlDown(e, true))
                        setPositionZ(getPositionZ() + 5);
                    else
                        setPositionZ(getPositionZ() + 1);
                    e.consume();
                    break;

                case KeyEvent.VK_DOWN:
                    if (EventUtil.isMenuControlDown(e, true))
                        setPositionZ(Math.max(getPositionZ() - 5, 0));
                    else
                        setPositionZ(Math.max(getPositionZ() - 1, 0));
                    e.consume();
                    break;

                case KeyEvent.VK_NUMPAD2:
                    if (!canvasView.moving)
                    {
                        final Point startPos = new Point(getOffsetX(), getOffsetY());
                        final Point delta = new Point(0, -getCanvasSizeY() / 4);
                        canvasView.translate(startPos, delta, EventUtil.isControlDown(e));
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_NUMPAD4:
                    if (!canvasView.moving)
                    {
                        final Point startPos = new Point(getOffsetX(), getOffsetY());
                        final Point delta = new Point(getCanvasSizeX() / 4, 0);
                        canvasView.translate(startPos, delta, EventUtil.isControlDown(e));
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_NUMPAD6:
                    if (!canvasView.moving)
                    {
                        final Point startPos = new Point(getOffsetX(), getOffsetY());
                        final Point delta = new Point(-getCanvasSizeX() / 4, 0);
                        canvasView.translate(startPos, delta, EventUtil.isControlDown(e));
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_NUMPAD8:
                    if (!canvasView.moving)
                    {
                        final Point startPos = new Point(getOffsetX(), getOffsetY());
                        final Point delta = new Point(0, getCanvasSizeY() / 4);
                        canvasView.translate(startPos, delta, EventUtil.isControlDown(e));
                        e.consume();
                    }
                    break;
            }
        }

        // forward to view
        canvasView.keyPressed(e);
        // forward to map
        canvasMap.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // send to overlays
        super.keyReleased(e);

        // forward to view
        canvasView.keyReleased(e);
        // forward to map
        canvasMap.keyReleased(e);
    }

    @Override
    public void refresh()
    {
        canvasView.imageChanged();
        canvasView.layersChanged();
        canvasView.refresh();
    }

    /**
     * Return an ARGB BufferedImage form of the image located at position [T, Z, C].<br>
     * If the 'out' image is not compatible with wanted image, a new image is returned.
     */
    public BufferedImage getARGBImage(int t, int z, int c, BufferedImage out)
    {
        final IcyBufferedImage img = Canvas2D.this.getImage(t, z, c);

        if (img != null)
        {
            final BufferedImage result;

            if ((out != null) && ImageUtil.sameSize(img, out))
                result = out;
            else
                result = new BufferedImage(img.getSizeX(), img.getSizeY(), BufferedImage.TYPE_INT_ARGB);

            return IcyBufferedImageUtil.toBufferedImage(img, result, getLut());
        }

        return null;
    }

    @Override
    public BufferedImage getRenderedImage(int t, int z, int c, boolean cv)
    {
        final Sequence seq = getSequence();
        if (seq == null)
            return null;

        // save position
        final int prevT = getPositionT();
        final int prevZ = getPositionZ();
        final boolean dl = isLayersVisible();

        if (dl)
        {
            // set wanted position (needed for correct overlay drawing)
            // we have to fire events else some stuff can miss the change
            setPositionT(t);
            setPositionZ(z);
        }
        try
        {
            final Dimension size;

            if (cv)
                size = getCanvasSize();
            else
                size = seq.getDimension2D();

            // get result image and graphics object
            final BufferedImage result = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = result.createGraphics();

            // set default clip region
            g.setClip(0, 0, size.width, size.height);

            if (cv)
            {
                // apply filtering
                if (CanvasPreferences.getFiltering() && ((getScaleX() < 4d) && (getScaleY() < 4d)))
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                else
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // apply transformation
                g.transform(getTransform());
            }
            else
            {
                // apply filtering
                if (CanvasPreferences.getFiltering())
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                else
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            // create temporary image, overlay and layer so we can choose the correct image
            // (not optimal for memory and performance)
            final BufferedImage img = getARGBImage(t, z, c, null);
            final Overlay imgOverlay = new ImageOverlay("Image", img);
            final Layer imgLayer = new Layer(imgOverlay);

            // keep visibility and priority information
            imgLayer.setVisible(getImageLayer().isVisible());
            imgLayer.setPriority(getImageLayer().getPriority());

            // draw image and layers
            canvasView.drawImageAndLayers(g, imgLayer);

            g.dispose();

            return result;
        }
        finally
        {
            if (dl)
            {
                // restore position
                setPositionT(prevT);
                setPositionZ(prevZ);
            }
        }
    }

    /**
     * @deprecated Use <code>getRenderedImage(t, z, -1, true)</code> instead.
     */
    @Deprecated
    public BufferedImage getRenderedImage(int t, int z)
    {
        return getRenderedImage(t, z, -1, true);
    }

    /**
     * @deprecated Use <code>getRenderedImage(t, z, -1, canvasView)</code> instead.
     */
    @Deprecated
    public BufferedImage getRenderedImage(int t, int z, boolean canvasView)
    {
        return getRenderedImage(t, z, -1, canvasView);
    }

    /**
     * Synchronize views of specified list of canvas
     */
    @Override
    protected void synchronizeCanvas(List<IcyCanvas> canvasList, IcyCanvasEvent event, boolean processAll)
    {
        final IcyCanvasEventType type = event.getType();
        final DimensionId dim = event.getDim();

        // position synchronization
        if (isSynchOnSlice())
        {
            if (processAll || (type == IcyCanvasEventType.POSITION_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    // only support T and Z positioning
                    final int z = getPositionZ();
                    final int t = getPositionT();

                    for (IcyCanvas cnv : canvasList)
                    {
                        if (z != -1)
                            cnv.setPositionZ(z);
                        if (t != -1)
                            cnv.setPositionT(t);
                    }
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                    {
                        final int pos = getPosition(dim);
                        if (pos != -1)
                            cnv.setPosition(dim, pos);
                    }
                }
            }
        }

        // view synchronization
        if (isSynchOnView())
        {
            if (processAll || (type == IcyCanvasEventType.SCALE_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final double sX = getScaleX();
                    final double sY = getScaleY();

                    for (IcyCanvas cnv : canvasList)
                        ((Canvas2D) cnv).setScale(sX, sY, false);
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setScale(dim, getScale(dim));
                }
            }

            if (processAll || (type == IcyCanvasEventType.ROTATION_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final double rot = getRotationZ();

                    for (IcyCanvas cnv : canvasList)
                        ((Canvas2D) cnv).setRotation(rot, false);
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setRotation(dim, getRotation(dim));
                }
            }

            // process offset in last as it can be limited depending destination scale value
            if (processAll || (type == IcyCanvasEventType.OFFSET_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final int offX = getOffsetX();
                    final int offY = getOffsetY();

                    for (IcyCanvas cnv : canvasList)
                        ((Canvas2D) cnv).setOffset(offX, offY, false);
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setOffset(dim, getOffset(dim));
                }
            }

        }

        // cursor synchronization
        if (isSynchOnCursor())
        { // mouse synchronization
            if (processAll || (type == IcyCanvasEventType.MOUSE_IMAGE_POSITION_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final double mouseImagePosX = getMouseImagePosX();
                    final double mouseImagePosY = getMouseImagePosY();

                    for (IcyCanvas cnv : canvasList)
                        ((Canvas2D) cnv).setMouseImagePos(mouseImagePosX, mouseImagePosY);
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setMouseImagePos(dim, getMouseImagePos(dim));
                }
            }
        }
    }

    @Override
    public void changed(IcyCanvasEvent event)
    {
        super.changed(event);

        // not yet initialized
        if (canvasView == null)
            return;

        final IcyCanvasEventType type = event.getType();

        switch (type)
        {
            case POSITION_CHANGED:
                // image has changed
                canvasView.imageChanged();

            case OFFSET_CHANGED:
            case SCALE_CHANGED:
            case ROTATION_CHANGED:
                // update mouse image position from mouse canvas position
                setMouseImagePos(canvasToImage(getMousePos()));

                // display info message
                if (type == IcyCanvasEventType.SCALE_CHANGED)
                {
                    final String zoomInfo = Integer.toString((int) (getScaleX() * 100));

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // in panel
                            modifyingZoom = true;
                            try
                            {
                                getCanvasSettingPanel().updateZoomState(zoomInfo);
                            }
                            finally
                            {
                                modifyingZoom = false;
                            }
                        }
                    });

                    // and in canvas
                    canvasView.setZoomMessage("Zoom : " + zoomInfo + " %", 500);
                }
                else if (type == IcyCanvasEventType.ROTATION_CHANGED)
                {
                    final String rotInfo = Integer.toString((int) Math.round(getRotation() * 180d / Math.PI));

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // in panel
                            modifyingRotation = true;
                            try
                            {
                                getCanvasSettingPanel().updateRotationState(rotInfo);
                            }
                            finally
                            {
                                modifyingRotation = false;
                            }
                        }
                    });

                    // and in canvas
                    canvasView.setRotationMessage("Rotation : " + rotInfo + " �", 500);
                }

                // refresh canvas
                canvasView.refresh();
                break;

            case MOUSE_IMAGE_POSITION_CHANGED:
                // mouse position changed outside mouse move event ?
                if (!canvasView.handlingMouseMoveEvent && !canvasView.isDragging() && !isSynchSlave())
                {
                    // mouse position in canvas
                    final Point mousePos = getMousePos();
                    final Point mouseAbsolutePos = getMousePos();
                    // absolute mouse position
                    SwingUtilities.convertPointToScreen(mouseAbsolutePos, canvasView);

                    // simulate a mouse move event so overlays can handle position change
                    final MouseEvent mouseEvent = new MouseEvent(this, MouseEvent.MOUSE_MOVED,
                            System.currentTimeMillis(), 0, mousePos.x, mousePos.y, mouseAbsolutePos.x,
                            mouseAbsolutePos.y, 0, false, 0);

                    // send mouse move event to overlays
                    mouseMove(mouseEvent, getMouseImagePos5D());
                }

                // update mouse cursor
                canvasView.updateCursor();

                // needed to refresh custom cursor
                if (!canvasView.hasMouseFocus)
                    canvasView.refresh();
                break;
        }
    }

    @Override
    protected void lutChanged(int component)
    {
        super.lutChanged(component);

        // refresh image
        if (canvasView != null)
        {
            canvasView.imageChanged();
            canvasView.refresh();
        }
    }

    @Override
    protected void layerChanged(CanvasLayerEvent event)
    {
        super.layerChanged(event);

        // layer visibility property modified ?
        if ((event.getType() == LayersEventType.CHANGED) && Layer.isPaintProperty(event.getProperty()))
        {
            // layer refresh
            if (canvasView != null)
            {
                canvasView.layersChanged();
                canvasView.refresh();
            }
        }
    }

    @Override
    protected void sequenceOverlayChanged(Overlay overlay, SequenceEventType type)
    {
        super.sequenceOverlayChanged(overlay, type);

        // layer refresh
        if (canvasView != null)
        {
            canvasView.layersChanged();
            canvasView.refresh();
        }
    }

    @Override
    protected void sequenceDataChanged(IcyBufferedImage image, SequenceEventType type)
    {
        super.sequenceDataChanged(image, type);

        // refresh image
        if (canvasView != null)
        {
            canvasView.imageChanged();
            canvasView.refresh();
        }
    }

    @Override
    protected void sequenceTypeChanged()
    {
        super.sequenceTypeChanged();

        // sequence XY dimension changed ?
        if ((previousImageSize.width != getImageSizeX()) || (previousImageSize.height != getImageSizeY()))
        {
            // fit to canvas enabled ? --> adapt zoom to new sequence XY dimension
            if (getFitToCanvas())
                fitImageToCanvas(true);
        }
    }

    @Override
    public void toolChanged(String command)
    {
        final Sequence seq = getSequence();

        final ROITask toolTask = Icy.getMainInterface().getROIRibbonTask();

        if (toolTask != null)
        {
            // if we selected a ROI tool we force layers to be visible
            if (toolTask.isROITool())
                setLayersVisible(true);
        }

        // unselected all ROI
        if (seq != null)
            seq.setSelectedROI(null);
    }

}

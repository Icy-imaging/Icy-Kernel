/**
 * 
 */
package icy.canvas;

import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.menu.ToolRibbonTask.ToolRibbonTaskListener;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.main.Icy;
import icy.math.Interpolator;
import icy.math.MathUtil;
import icy.math.MultiSmoothMover;
import icy.math.MultiSmoothMover.MultiSmoothMoverAdapter;
import icy.math.SmoothMover;
import icy.math.SmoothMover.SmoothMoveType;
import icy.math.SmoothMover.SmoothMoverAdapter;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * New Canvas 2D : default ICY 2D viewer.<br>
 * Support translation / scale and rotation transformation.<br>
 * 
 * @author Stephane
 */
public class Canvas2D extends IcyCanvas2D implements ToolRibbonTaskListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 8850168605044063031L;

    static final int ICON_SIZE = 20;
    static final int ICON_TARGET_SIZE = 20;

    static final Image ICON_CENTER_IMAGE = ResourceUtil.getAlphaIconAsImage("center.png", ICON_SIZE);
    static final Image ICON_FIT_IMAGE = ResourceUtil.getAlphaIconAsImage("fit_in2.png", ICON_SIZE);
    static final Image ICON_FIT_CANVAS = ResourceUtil.getAlphaIconAsImage("fit_out.png", ICON_SIZE);

    static final Image ICON_TARGET = ResourceUtil.getAlphaIconAsImage("simple_target.png", ICON_TARGET_SIZE);
    static final Image ICON_TARGET_BLACK = ImageUtil.getColorImageFromAlphaImage(ICON_TARGET, Color.black);
    static final Image ICON_TARGET_LIGHT = ImageUtil.getColorImageFromAlphaImage(ICON_TARGET, Color.lightGray);

    /**
     * Possible rounded zoom factor : 0.01 --> 100
     */
    final static double[] zoomRoundedFactors = new double[] {0.01d, 0.02d, 0.0333d, 0.05d, 0.075d, 0.1d, 0.15d, 0.2d,
            0.25d, 0.333d, 0.5d, 0.66d, 0.75d, 1d, 1.25d, 1.5d, 1.75d, 2d, 2.5d, 3d, 4d, 5d, 6.6d, 7.5d, 10d, 15d, 20d,
            30d, 50d, 66d, 75d, 100d};

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
            setMouseCanvasPos(imageToCanvas(imagePoint));

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

            // shift action --> limit to 45° rotation
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

                // final AffineTransform trans = getImageTransform();
                //
                // if (trans != null)
                // {
                // try
                // {
                //
                // // save last mouse position
                // final int lastMouseCanvasPosX = mouseCanvasPos.x;
                // final int lastMouseCanvasPosY = mouseCanvasPos.y;
                //
                // // image position
                // final Point2D imagePoint = trans.inverseTransform(e.getPoint(), null);
                //
                // // left button action --> center view on mouse point
                // if (EventUtil.isLeftMouseButton(e))
                // {
                // // center view on this point (this update mouse canvas position)
                // centerOnImage(imagePoint.getX(), imagePoint.getY());
                // // no need to update mouse canvas position here as it stays at center
                //
                // // consume event
                // e.consume();
                // }
                // else if (EventUtil.isRightMouseButton(e))
                // {
                // mapRotating = true;
                //
                // // update mouse canvas position from image position
                // setMouseCanvasPos(imageToCanvas(imagePoint.getX(), imagePoint.getY()));
                // // get canvas center image position
                // final Point2D.Double canvasCenter = canvasToImage(getCanvasSizeX() / 2,
                // getCanvasSizeY() / 2);
                //
                // // get last and current mouse position delta with center
                // final Point2D.Double lastMouseDeltaPos = canvasToImage(lastMouseCanvasPosX,
                // lastMouseCanvasPosY);
                // lastMouseDeltaPos.x -= canvasCenter.x;
                // lastMouseDeltaPos.y -= canvasCenter.y;
                // final Point2D.Double newMouseDeltaPos = getMouseImagePos();
                // newMouseDeltaPos.x -= canvasCenter.x;
                // newMouseDeltaPos.y -= canvasCenter.y;
                //
                // // get reverse angle in radian between last and
                // // current mouse position relative to canvas center
                // double angle = Math.atan2(lastMouseDeltaPos.y, lastMouseDeltaPos.x)
                // - Math.atan2(newMouseDeltaPos.y, newMouseDeltaPos.x);
                //
                // // control button down --> rotation is enforced
                // if (EventUtil.isControlDown(e))
                // angle *= 3;
                //
                // angle = MathUtil.formatRadianAngle2(angle);
                //
                // // modify rotation with smooth mover
                // setRotation(transform.getDestValue(ROT) + angle, true);
                //
                // e.consume();
                // }
                // }
                // catch (Exception ecx)
                // {
                // // ignore
                // }
                // }
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
            if (canvasView.onMouseMove(e.isConsumed(), getCanvasPosition(e.getPoint())))
                e.consume();
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {

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
                        setMouseCanvasPos(imageToCanvas(imagePoint.getX(), imagePoint.getY()));
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

        }

        @Override
        public void mouseExited(MouseEvent e)
        {

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
                    setMouseCanvasPos(imageToCanvas(imagePoint.getX(), imagePoint.getY()));
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
                final BufferedImage img = canvasView.imageCache.getImage();

                // draw image
                g2.drawImage(img, trans, null);

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

    public class CanvasView extends JPanel implements ActionListener, MouseWheelListener, MouseListener,
            MouseMotionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4041355608444378172L;

        public class ImageCache implements Runnable
        {
            /**
             * image cache
             */
            private BufferedImage imageCache;

            /**
             * processor
             */
            private final SingleProcessor processor;
            /**
             * internals
             */
            private boolean needRebuild;

            public ImageCache()
            {
                super();

                processor = new SingleProcessor(true);
                processor.setDefaultThreadName("Canvas2D renderer");
                imageCache = null;
                needRebuild = true;
                // build cache
                processor.addTask(this, false);
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
                if (needRebuild)
                    // rebuild cache
                    processor.addTask(this, false);

                // just repaint
                CanvasView.this.repaint();
            }

            public BufferedImage getImage()
            {
                return imageCache;
            }

            @Override
            public void run()
            {
                // important to set it to false at beginning
                needRebuild = false;

                final IcyBufferedImage img = Canvas2D.this.getCurrentImage();

                if (img != null)
                    imageCache = img.getARGBImage(getLut(), imageCache);
                else
                    imageCache = null;

                // repaint now
                CanvasView.this.repaint();
            }
        }

        /**
         * Image cache
         */
        final ImageCache imageCache;

        /**
         * internals
         */
        private final Font font;
        private final Timer refreshTimer;
        private final Timer zoomInfoTimer;
        private final Timer rotationInfoTimer;
        private final Timer zoomLatchTimer;
        private final SmoothMover zoomInfoAlphaMover;
        private final SmoothMover rotationInfoAlphaMover;
        private String zoomMessage;
        private String rotationMessage;
        Dimension lastSize;
        boolean actived;
        boolean handlingMouseMoveEvent;
        private Point startDragPosition;
        private int startOffsetX;
        private int startOffsetY;
        private double curScaleX;
        private double curScaleY;
        private double startRotationZ;
        private boolean moving;
        private boolean rotating;
        private boolean hasMouseFocus;

        public CanvasView()
        {
            super();

            imageCache = new ImageCache();
            actived = false;
            handlingMouseMoveEvent = false;
            startDragPosition = null;
            curScaleX = -1;
            curScaleY = -1;
            moving = false;
            rotating = false;
            hasMouseFocus = false;
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
            zoomLatchTimer = new Timer(1000, this);
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

                            setOffset((int) transform.getDestValue(TRANS_X) + dx2,
                                    (int) transform.getDestValue(TRANS_Y) + dy2, true);
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
            zoomLatchTimer.stop();
            refreshTimer.removeActionListener(this);
            zoomInfoTimer.removeActionListener(this);
            rotationInfoTimer.removeActionListener(this);
            zoomLatchTimer.removeActionListener(this);
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

            Point delta = new Point(mouseCanvasPos.x - startDragPosition.x, mouseCanvasPos.y - startDragPosition.y);

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

            final Point2D.Double deltaD;

            // control button down
            if (control)
                // drag is scaled by current scales factor
                deltaD = canvasToImageDelta(delta.x, delta.y, 1d / getScaleX(), 1d / getScaleY(), getRotationZ());
            else
                // just get rid of rotation factor
                deltaD = canvasToImageDelta(delta.x, delta.y, 1d, 1d, getRotationZ());

            // modify offset with smooth mover
            setOffset((int) Math.round(startOffsetX + deltaD.x), (int) Math.round(startOffsetY + deltaD.y), true);
        }

        protected void updateRot(boolean control, boolean shift)
        {
            if (!rotating)
                return;

            // get canvas center
            final int canvasCenterX = getCanvasSizeX() / 2;
            final int canvasCenterY = getCanvasSizeY() / 2;

            // get last and current mouse position delta with center
            final int lastMouseDeltaPosX = startDragPosition.x - canvasCenterX;
            final int lastMouseDeltaPosY = startDragPosition.y - canvasCenterY;
            final int newMouseDeltaPosX = mouseCanvasPos.x - canvasCenterX;
            final int newMouseDeltaPosY = mouseCanvasPos.y - canvasCenterY;

            // get angle in radian between last and current mouse position
            // relative to image center
            double newAngle = Math.atan2(newMouseDeltaPosY, newMouseDeltaPosX);
            double lastAngle = Math.atan2(lastMouseDeltaPosY, lastMouseDeltaPosX);

            double angle = newAngle - lastAngle;

            // control button down --> rotation is enforced
            if (control)
                angle *= 3;

            final double destAngle;

            // shift action --> limit to 45° rotation
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
                // // double click = zoom action
                // if (clickCount == 2)
                // {
                // // previous scale destination values
                // final double scaleX = transform.getDestValue(SCALE_X);
                // final double scaleY = transform.getDestValue(SCALE_Y);
                //
                // double sx, sy;
                //
                // if (left)
                // {
                // sx = 2d;
                // sy = 2d;
                // }
                // else
                // {
                // sx = 0.5d;
                // sy = 0.5d;
                // }
                //
                // // control button down --> fast zoom
                // if (control)
                // {
                // sx *= sx;
                // sy *= sy;
                // }
                //
                // setScale(scaleX * sx, scaleY * sy, true, true);
                //
                // return true;
                // }
            }

            return false;
        }

        /**
         * Internal canvas process on mousePressed event.<br>
         * Return true if event should be consumed.
         */
        boolean onMousePressed(boolean consumed, boolean left, boolean right, boolean control)
        {
            if (!consumed)
            {
                // start drag mouse position
                startDragPosition = (Point) mouseCanvasPos.clone();
                // store canvas parameters
                startOffsetX = getOffsetX();
                startOffsetY = getOffsetY();
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
            // assume end dragging
            startDragPosition = null;
            moving = false;
            rotating = false;

            // repaint
            refresh();
            updateCursor();

            // consume event
            return true;
        }

        /**
         * Internal canvas process on mouseMove event.<br>
         * Return true if event should be consumed.
         */
        boolean onMouseMove(boolean consumed, Point pos)
        {
            if (!consumed)
            {
                handlingMouseMoveEvent = true;
                try
                {
                    // update mouse position
                    setMouseCanvasPos(pos);
                }
                finally
                {
                    handlingMouseMoveEvent = false;
                }
            }

            // don't consume this event
            return false;
        }

        /**
         * Internal canvas process on mouseDragged event.<br>
         * Return true if event should be consumed.
         */
        boolean onMouseDragged(boolean consumed, Point pos, boolean left, boolean right, boolean control, boolean shift)
        {
            if (!consumed)
            {
                handlingMouseMoveEvent = true;
                try
                {
                    // update mouse position
                    setMouseCanvasPos(pos);

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

                    // no dragging --> no consume
                    return false;
                }
                finally
                {
                    handlingMouseMoveEvent = false;
                }
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

                    if ((wheelRotation > 0) ^ GeneralPreferences.getInvertMouseWheelAxis())
                    {
                        sx = 20d / 19d;
                        sy = 20d / 19d;
                    }
                    else
                    {
                        sx = 19d / 20d;
                        sy = 19d / 20d;
                    }

                    // control button down --> fast zoom
                    if (control)
                    {
                        sx *= sx;
                        sy *= sy;
                    }

                    // reload current value
                    if (curScaleX == -1)
                        curScaleX = transform.getDestValue(SCALE_X);
                    if (curScaleY == -1)
                        curScaleY = transform.getDestValue(SCALE_Y);

                    curScaleX = curScaleX * sx;
                    curScaleY = curScaleY * sy;

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
            // send mouse event to painters first
            for (Layer layer : getVisibleOrderedLayersForEvent())
                layer.getPainter().mouseClick(e, getMouseImagePos(), Canvas2D.this);

            // process
            if (onMouseClicked(e.isConsumed(), e.getClickCount(), EventUtil.isLeftMouseButton(e),
                    EventUtil.isRightMouseButton(e), EventUtil.isControlDown(e)))
                e.consume();
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            final String tool = Icy.getMainInterface().getSelectedTool();

            // priority ROI creation (control key down)
            if (EventUtil.isControlDown(e) && EventUtil.isLeftMouseButton(e) && ToolRibbonTask.isROITool(tool))
            {
                // try to create ROI from current selected tool
                final ROI roi = ROI.create(tool, getSequence(), getMouseImagePos(), true);
                // roi created ? --> it becomes the selected ROI
                if (roi != null)
                {
                    roi.setSelected(true, true);
                    roi.setFocused(true);
                }
                // consume event
                e.consume();
            }

            // send mouse event to painters now
            for (Layer layer : getVisibleOrderedLayersForEvent())
                layer.getPainter().mousePressed(e, getMouseImagePos(), Canvas2D.this);

            // not yet consumed
            if (!e.isConsumed())
            {
                // ROI creation
                if (EventUtil.isLeftMouseButton(e) && ToolRibbonTask.isROITool(tool))
                {
                    // return to default selection tool before ROI creation
                    Icy.getMainInterface().setSelectedTool(ToolRibbonTask.SELECT);

                    // try to create ROI from current selected tool
                    final ROI roi = ROI.create(tool, getSequence(), getMouseImagePos(), true);
                    // roi created ? --> it becomes the selected ROI
                    if (roi != null)
                        roi.setSelected(true, true);

                    // consume event
                    e.consume();
                }
            }

            // process
            if (onMousePressed(e.isConsumed(), EventUtil.isLeftMouseButton(e), EventUtil.isRightMouseButton(e),
                    EventUtil.isControlDown(e)))
                e.consume();

            updateCursor();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // send mouse event to painters first
            for (Layer layer : getVisibleOrderedLayersForEvent())
                layer.getPainter().mouseReleased(e, getMouseImagePos(), Canvas2D.this);

            // process
            if (onMouseReleased(e.isConsumed(), EventUtil.isLeftMouseButton(e), EventUtil.isRightMouseButton(e),
                    EventUtil.isControlDown(e)))
                e.consume();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            hasMouseFocus = true;
            refresh();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            hasMouseFocus = false;
            refresh();
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            // process
            if (onMouseMove(e.isConsumed(), e.getPoint()))
                e.consume();

            // send mouse event to painters after
            for (Layer layer : getVisibleOrderedLayersForEvent())
                layer.getPainter().mouseMove(e, getMouseImagePos(), Canvas2D.this);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            // process
            if (onMouseDragged(e.isConsumed(), e.getPoint(), EventUtil.isLeftMouseButton(e),
                    EventUtil.isRightMouseButton(e), EventUtil.isControlDown(e), EventUtil.isShiftDown(e)))
                e.consume();

            // send mouse event to painters after
            for (Layer layer : getVisibleOrderedLayersForEvent())
                layer.getPainter().mouseDrag(e, getMouseImagePos(), Canvas2D.this);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
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

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            final int canvasCenterX = getCanvasSizeX() / 2;
            final int canvasCenterY = getCanvasSizeY() / 2;

            final BufferedImage img = imageCache.getImage();

            if (img != null)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                if (getTransform().getScaleX() < 4. && getTransform().getScaleX() < 4.) {
	                if (!transform.isMoving()) {
	                	// Draw the image with bicubic resampling,
	                	// except when zoom is larger than 400 %, where nearest-neighbour is desirable
	                	// (so that the user can see that he is operating on pixels),
	                	// and except during the zooming animation, when speed must be high.
	                	// TODO: when the zoom factor is smaller than 1, a low-pass filter should be applied
	                	// before the sampling, otherwise severe aliasing is introduced.
	                	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);	
	                } else {
	                	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	                }
                }
                
                g2.transform(getTransform());
                g2.drawImage(img, null, 0, 0);

                if (getDrawLayers())
                {
                    final Sequence seq = getSequence();

                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    final ArrayList<Layer> layers = getVisibleOrderedLayersForEvent();

                    // draw them in inverse order to have first painter event at top
                    for (int i = layers.size() - 1; i >= 0; i--)
                    {
                        final Layer layer = layers.get(i);
                        final float alpha = layer.getAlpha();

                        if (alpha != 1f)
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        else
                            g2.setComposite(AlphaComposite.SrcOver);

                        layer.getPainter().paint(g2, seq, Canvas2D.this);
                    }
                }

                g2.dispose();
            }
            else
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                g2.setFont(font);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (Canvas2D.this.getCurrentImage() != null)
                    // cache not yet built
                    drawTextCenter(g2, "Loading...", 0.8f);
                else
                    // no image
                    drawTextCenter(g2, " No image ", 0.8f);

                g2.dispose();
            }

            // synchronized canvas ? display external cursor
            if (!hasMouseFocus)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                final int x = mouseCanvasPos.x - (ICON_TARGET_SIZE / 2);
                final int y = mouseCanvasPos.y - (ICON_TARGET_SIZE / 2);

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

        void updateCursor()
        {
            if (isDragging())
            {
                GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                return;
            }

            final Sequence seq = getSequence();

            if (seq != null)
            {
                final ArrayList<ROI2D> selectedRois2D = ROI2D.getROI2DList(seq.getSelectedROIs());

                // search if we are overriding ROI control points
                for (ROI2D selectedRoi : selectedRois2D)
                {
                    final Layer layer = getLayer(selectedRoi);

                    if ((layer != null) && layer.isVisible() && selectedRoi.hasSelectedPoint())
                    {
                        GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                        return;
                    }
                }

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
            }

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

        }

        public boolean isDragging()
        {
            return startDragPosition != null;
        }

        public boolean isCacheValid()
        {
            return imageCache.isValid();
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
            else if (source == zoomLatchTimer)
            {
                curScaleX = -1;
                curScaleY = -1;
            }
        }
    }

    /**
     * * index 0 : translation X (int) index 1 : translation Y (int) index 2 :
     * scale X (double) index 3 : scale Y (double) index 4 : rotation angle
     * (double)
     * 
     * @author Stephane
     */
    private static class Canvas2DSmoothMover extends MultiSmoothMover
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
            final int size = Math.max(moveTime / timer.getDelay(), 1);

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
    private static final String PREF_CANVAS2D_ID = "Canvas2D";

    private static final String ID_FIT_CANVAS = "fitCanvas";

    private final static int TRANS_X = 0;
    private final static int TRANS_Y = 1;
    private final static int SCALE_X = 2;
    private final static int SCALE_Y = 3;
    private final static int ROT = 4;

    /**
     * view where we draw
     */
    final CanvasView canvasView;

    /**
     * minimap in canvas panel
     */
    final CanvasMap canvasMap;

    /**
     * gui
     */
    JComboBox zoomComboBox;
    JComboBox rotationComboBox;

    IcyToggleButton zoomFitCanvasButton;
    IcyButton zoomFitImageButton;
    IcyButton centerImageButton;

    /**
     * preferences
     */
    final XMLPreferences preferences;

    /**
     * The transform object contains all transform informations<br>
     */
    final Canvas2DSmoothMover transform;

    /**
     * internals
     */
    Point mouseCanvasPos;
    Point2D.Double mouseImagePos;
    String textInfos;
    boolean modifyingZoom;
    boolean modifyingRotation;

    public Canvas2D(Viewer viewer)
    {
        super(viewer);

        // arrange to our dimension format
        if (posZ == -1)
            posZ = 0;
        if (posT == -1)
            posT = 0;
        posC = -1;

        // view panel
        canvasView = new CanvasView();
        // mini map
        canvasMap = new CanvasMap();

        // variables initialization
        preferences = ApplicationPreferences.getPreferences().node(PREF_CANVAS2D_ID);

        // init transform (5 values, log transition type)
        transform = new Canvas2DSmoothMover(5, SmoothMoveType.LOG);
        // initials transform values
        transform.setValues(new double[] {0d, 0d, 1d, 1d, 0d});
        // initial mouse position
        mouseCanvasPos = new Point();
        mouseImagePos = new Point2D.Double();
        textInfos = null;
        modifyingZoom = false;
        modifyingRotation = false;

        transform.addListener(new MultiSmoothMoverAdapter()
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
        });

        // want fast transition
        transform.setMoveTime(400);
        // and very smooth refresh if possible
        transform.setUpdateDelay(20);

        // build inspector canvas panel
        buildCanvasPanel();

        // set view in center
        add(canvasView, BorderLayout.CENTER);

        // mouse infos panel setting
        mouseInfPanel.setInfoXVisible(true);
        mouseInfPanel.setInfoYVisible(true);
        // already visible in Z/T navigator
        mouseInfPanel.setInfoZVisible(false);
        mouseInfPanel.setInfoTVisible(false);
        // no C navigation with this canvas
        mouseInfPanel.setInfoCVisible(false);
        // data and color visible
        mouseInfPanel.setInfoDataVisible(true);
        mouseInfPanel.setInfoColorVisible(true);

        updateZNav();
        updateTNav();

        // as scale isn't necessary changed (if already 100%)
        zoomComboBox.setSelectedItem(Integer.toString((int) (getScaleX() * 100)) + " %");

        final ToolRibbonTask trt = Icy.getMainInterface().getToolRibbon();
        if (trt != null)
            trt.addListener(this);
    }

    @Override
    public void shutDown()
    {
        super.shutDown();

        canvasView.shutDown();

        // shutdown mover object (else internal timer keep a reference to Canvas2D)
        transform.shutDown();

        final ToolRibbonTask trt = Icy.getMainInterface().getToolRibbon();
        if (trt != null)
            trt.removeListener(this);
    }

    /**
     * Build canvas panel for inspector
     */
    private void buildCanvasPanel()
    {
        // canvas panel (for inspector)
        panel = new JPanel();

        zoomComboBox = new JComboBox(new String[] {"10", "50", "100", "200", "400", "1000"});
        zoomComboBox.setEditable(true);
        zoomComboBox.setToolTipText("Select zoom factor");
        zoomComboBox.setSelectedIndex(2);
        ComponentUtil.setFixedWidth(zoomComboBox, 80);
        zoomComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!modifyingZoom)
                {
                    try
                    {
                        final double scale = Double.parseDouble((String) zoomComboBox.getSelectedItem()) / 100;

                        // set mouse position on view center
                        centerMouseOnView();
                        // set new scale
                        setScale(scale, scale, true, true);
                    }
                    catch (NumberFormatException E)
                    {
                        // ignore change
                    }
                }
            }
        });

        rotationComboBox = new JComboBox(new String[] {"0", "45", "90", "135", "180", "225", "270", "315"});
        rotationComboBox.setEditable(true);
        rotationComboBox.setToolTipText("Select rotation angle");
        rotationComboBox.setSelectedIndex(0);
        ComponentUtil.setFixedWidth(rotationComboBox, 80);
        rotationComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!modifyingRotation)
                {
                    try
                    {
                        final double angle = Double.parseDouble((String) rotationComboBox.getSelectedItem());
                        // we first apply modulo
                        setRotation(getRotation(), false);
                        // then set new angle
                        setRotation((angle * Math.PI) / 180d, true);
                    }
                    catch (NumberFormatException E)
                    {
                        // ignore change
                    }
                }
            }
        });

        final IcyButton zoomPlus = new IcyButton(ResourceUtil.ICON_PLUS, ICON_SIZE);
        zoomPlus.setFlat(true);
        zoomPlus.setToolTipText("Increase zoom factor");
        zoomPlus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final double scale = transform.getDestValue(SCALE_X) * 1.25;

                // set mouse position on view center
                centerMouseOnView();
                // apply scale
                setScale(scale, scale, true, true);
            }
        });

        final IcyButton zoomMinus = new IcyButton(ResourceUtil.ICON_MINUS, ICON_SIZE);
        zoomMinus.setFlat(true);
        zoomMinus.setToolTipText("Reduce zoom factor");
        zoomMinus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final double scale = transform.getDestValue(SCALE_X) * 0.8;

                // set mouse position on view center
                centerMouseOnView();
                // apply scale
                setScale(scale, scale, true, true);
            }
        });

        final IcyButton rotateUnclock = new IcyButton(ResourceUtil.ICON_ROTATE_UNCLOCK, ICON_SIZE);
        rotateUnclock.setFlat(true);
        rotateUnclock.setToolTipText("Rotate counter clockwise");
        rotateUnclock.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setRotation(transform.getDestValue(ROT) + (Math.PI / 8), true);
            }
        });

        final IcyButton rotateClock = new IcyButton(ResourceUtil.ICON_ROTATE_CLOCK, ICON_SIZE);
        rotateClock.setFlat(true);
        rotateClock.setToolTipText("Rotate clockwise");
        rotateClock.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setRotation(transform.getDestValue(ROT) - (Math.PI / 8), true);
            }
        });

        zoomFitImageButton = new IcyButton(ICON_FIT_IMAGE);
        zoomFitImageButton.setFocusable(false);
        zoomFitImageButton.setToolTipText("Fit window to image size");
        zoomFitImageButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fitCanvasToImage();
            }
        });

        centerImageButton = new IcyButton(ICON_CENTER_IMAGE);
        centerImageButton.setFocusable(false);
        centerImageButton.setToolTipText("Center image in window");
        centerImageButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                centerImage();
            }
        });

        zoomFitCanvasButton = new IcyToggleButton(ICON_FIT_CANVAS);
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

                zoomComboBox.setEnabled(!selected);
                zoomPlus.setEnabled(!selected);
                zoomMinus.setEnabled(!selected);
                zoomFitImageButton.setEnabled(!selected);
                centerImageButton.setEnabled(!selected);

                // fit if enabled
                if (selected)
                    fitImageToCanvas(true);
            }
        });

        final boolean selected = zoomFitCanvasButton.isSelected();

        // enabled or not depending checkbox state
        zoomComboBox.setEnabled(!selected);
        zoomPlus.setEnabled(!selected);
        zoomMinus.setEnabled(!selected);
        zoomFitImageButton.setEnabled(!selected);
        centerImageButton.setEnabled(!selected);

        // bottom sub panel
        final JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));

        subPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthBoldLabel("Zoom", 70), zoomComboBox,
                GuiUtil.createFixedWidthBoldLabel("%", 20), Box.createHorizontalGlue(), zoomMinus,
                Box.createHorizontalStrut(4), zoomPlus, Box.createHorizontalStrut(4)));
        subPanel.add(Box.createVerticalStrut(4));
        subPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthBoldLabel("Rotation", 70), rotationComboBox,
                GuiUtil.createFixedWidthBoldLabel("°", 20), Box.createHorizontalGlue(), rotateUnclock,
                Box.createHorizontalStrut(4), rotateClock, Box.createHorizontalStrut(4)));

        panel.setLayout(new BorderLayout());

        panel.add(canvasMap, BorderLayout.CENTER);
        panel.add(subPanel, BorderLayout.SOUTH);
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
    public void addViewerToolbarComponents(JToolBar toolBar)
    {
        toolBar.addSeparator();
        toolBar.add(zoomFitCanvasButton);
        toolBar.addSeparator();
        toolBar.add(zoomFitImageButton);
        toolBar.add(centerImageButton);
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

        setOffset((int) (transform.getDestValue(TRANS_X) + Math.round(newTrans.x)),
                (int) (transform.getDestValue(TRANS_Y) + Math.round(newTrans.y)), false);
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
        setMouseCanvasPos(getCanvasSizeX() >> 1, getCanvasSizeY() >> 1);
    }

    /**
     * Set transform
     */
    protected void setTransform(int tx, int ty, double sx, double sy, double rot, boolean smooth)
    {
        final double[] values = new double[] {tx, ty, sx, sy, rot};

        // modify all at once for synchronized change events
        if (smooth)
            transform.moveTo(values);
        else
            transform.setValues(values);
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

        setTransform(adjX, adjY, transform.getDestValue(SCALE_X), transform.getDestValue(SCALE_Y),
                transform.getDestValue(ROT), smooth);
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
        final double rot = transform.getDestValue(ROT);
        // limit min and max zoom ratio
        final double newScaleX = Math.max(0.01d, Math.min(100d, x));
        final double newScaleY = Math.max(0.01d, Math.min(100d, y));

        // get new mouse position on canvas pixel
        final Point newMouseCanvasPos = imageToCanvas(mouseImagePos.x, mouseImagePos.y, 0, 0, newScaleX, newScaleY, rot);
        // new image size
        final int newImgSizeX = (int) Math.ceil(getImageSizeX() * newScaleX);
        final int newImgSizeY = (int) Math.ceil(getImageSizeY() * newScaleY);
        // canvas center
        final int canvasCenterX = getCanvasSizeX() / 2;
        final int canvasCenterY = getCanvasSizeY() / 2;

        final Point2D.Double newTrans;

        if (mouseCentered)
        {
            // we want the mouse image point to becomes the canvas center (take
            // rotation in account)
            newTrans = canvasToImageDelta(canvasCenterX - newMouseCanvasPos.x, canvasCenterY - newMouseCanvasPos.y, 1d,
                    1d, rot);
        }
        else
        {
            // we want the mouse image point to keep its place (take rotation in
            // account)
            newTrans = canvasToImageDelta(mouseCanvasPos.x - newMouseCanvasPos.x, mouseCanvasPos.y
                    - newMouseCanvasPos.y, 1d, 1d, rot);
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
        setTransform((int) transform.getDestValue(TRANS_X), (int) transform.getDestValue(TRANS_Y), x, y,
                transform.getDestValue(ROT), smooth);
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
        return (int) Math.ceil(getImageSizeX() * transform.getDestValue(SCALE_X));
    }

    /**
     * Get destination image size Y in canvas pixel coordinate
     */
    public int getDestImageCanvasSizeY()
    {
        return (int) Math.ceil(getImageSizeY() * transform.getDestValue(SCALE_Y));
    }

    @Override
    public boolean isSynchronizationSupported()
    {
        return true;
    }

    @Override
    public int getCanvasSizeX()
    {
        // can be called before constructor ended
        if (canvasView == null)
            return 0;

        // by default we use panel width
        int res = canvasView.getWidth();
        // preferred width if size not yet set
        if (res == 0)
            res = canvasView.getPreferredSize().width;

        return res;
    }

    @Override
    public int getCanvasSizeY()
    {
        // can be called before constructor ended
        if (canvasView == null)
            return 0;

        // by default we use panel height
        int res = canvasView.getHeight();
        // preferred height if size not yet set
        if (res == 0)
            res = canvasView.getPreferredSize().height;

        return res;
    }

    @Override
    public double getMouseImagePosX()
    {
        // can be called before constructor ended
        if (mouseImagePos == null)
            return 0d;

        return mouseImagePos.x;

    }

    @Override
    public double getMouseImagePosY()
    {
        // can be called before constructor ended
        if (mouseImagePos == null)
            return 0d;

        return mouseImagePos.y;
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
        if (transform == null)
            return 0;

        return (int) transform.getValue(TRANS_X);
    }

    @Override
    public int getOffsetY()
    {
        // can be called before constructor ended
        if (transform == null)
            return 0;

        return (int) transform.getValue(TRANS_Y);
    }

    @Override
    public double getScaleX()
    {
        // can be called before constructor ended
        if (transform == null)
            return 0d;

        return transform.getValue(SCALE_X);
    }

    @Override
    public double getScaleY()
    {
        // can be called before constructor ended
        if (transform == null)
            return 0d;

        return transform.getValue(SCALE_Y);
    }

    @Override
    public double getRotationZ()
    {
        // can be called before constructor ended
        if (transform == null)
            return 0d;

        return transform.getValue(ROT);
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

    /**
     * Set mouse canvas position
     */
    public void setMouseCanvasPos(int x, int y)
    {
        if ((mouseCanvasPos.x != x) || (mouseCanvasPos.y != y))
        {
            mouseCanvasPos.x = x;
            mouseCanvasPos.y = y;

            // direct update of mouse image position
            mouseImagePos = canvasToImage(mouseCanvasPos);
            // notify change
            mouseImagePositionChanged(DimensionId.NULL);
        }
    }

    /**
     * Set mouse canvas position
     */
    public void setMouseCanvasPos(Point point)
    {
        setMouseCanvasPos(point.x, point.y);
    }

    @Override
    public void setMouseImagePos(double x, double y)
    {
        if ((mouseImagePos.x != x) || (mouseImagePos.y != y))
        {
            mouseImagePos.x = x;
            mouseImagePos.y = y;

            // direct update of mouse canvas position
            mouseCanvasPos = imageToCanvas(mouseImagePos);
            // notify change
            mouseImagePositionChanged(DimensionId.NULL);
        }
    }

    @Override
    protected void setPositionCInternal(int c)
    {
        // not supported in this canvas, C should stay at -1
    }

    @Override
    protected void setMouseImagePosXInternal(double value)
    {
        mouseImagePos.x = value;

        // direct update of mouse canvas position
        mouseCanvasPos = imageToCanvas(mouseImagePos);

        super.setMouseImagePosXInternal(value);
    }

    @Override
    protected void setMouseImagePosYInternal(double value)
    {
        mouseImagePos.y = value;

        // direct update of mouse canvas position
        mouseCanvasPos = imageToCanvas(mouseImagePos);

        super.setMouseImagePosYInternal(value);
    }

    @Override
    protected void setOffsetXInternal(int value)
    {
        // this will automatically call the offsetChanged() event
        transform.setValue(TRANS_X, Math.min(getMaxOffsetX(), Math.max(getMinOffsetX(), value)));
    }

    @Override
    protected void setOffsetYInternal(int value)
    {
        // this will automatically call the offsetChanged() event
        transform.setValue(TRANS_Y, Math.min(getMaxOffsetY(), Math.max(getMinOffsetY(), value)));
    }

    @Override
    protected void setScaleXInternal(double value)
    {
        // this will automatically call the scaledChanged() event
        transform.setValue(SCALE_X, value);
    }

    @Override
    protected void setScaleYInternal(double value)
    {
        // this will automatically call the scaledChanged() event
        transform.setValue(SCALE_Y, value);
    }

    @Override
    protected void setRotationZInternal(double value)
    {
        // this will automatically call the rotationChanged() event
        transform.setValue(ROT, value);
    }

    /**
     * Set rotation angle (radian).<br>
     * 
     * @param smooth
     *        use smooth transition
     */
    public void setRotation(double value, boolean smooth)
    {
        setTransform((int) transform.getDestValue(TRANS_X), (int) transform.getDestValue(TRANS_Y),
                transform.getDestValue(SCALE_X), transform.getDestValue(SCALE_Y), value, smooth);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_LEFT:
                if (EventUtil.isControlDown(e))
                    setPositionT(Math.max(getPositionT() - 5, 0));
                else
                    setPositionT(Math.max(getPositionT() - 1, 0));
                e.consume();
                break;

            case KeyEvent.VK_RIGHT:
                if (EventUtil.isControlDown(e))
                    setPositionT(getPositionT() + 5);
                else
                    setPositionT(getPositionT() + 1);
                e.consume();
                break;

            case KeyEvent.VK_UP:
                if (EventUtil.isControlDown(e))
                    setPositionZ(getPositionZ() + 5);
                else
                    setPositionZ(getPositionZ() + 1);
                e.consume();
                break;

            case KeyEvent.VK_DOWN:
                if (EventUtil.isControlDown(e))
                    setPositionZ(Math.max(getPositionZ() - 5, 0));
                else
                    setPositionZ(Math.max(getPositionZ() - 1, 0));
                e.consume();
                break;

            case KeyEvent.VK_0:
                setSyncId(0);
                e.consume();
                break;

            case KeyEvent.VK_1:
                // already set, switch it off
                if (getSyncId() == 1)
                    setSyncId(0);
                else
                    setSyncId(1);
                e.consume();
                break;

            case KeyEvent.VK_2:
                // already set, switch it off
                if (getSyncId() == 2)
                    setSyncId(0);
                else
                    setSyncId(2);
                e.consume();
                break;

            case KeyEvent.VK_3:
                // already set, switch it off
                if (getSyncId() == 3)
                    setSyncId(0);
                else
                    setSyncId(3);
                e.consume();
                break;

            case KeyEvent.VK_4:
                // already set, switch it off
                if (getSyncId() == 4)
                    setSyncId(0);
                else
                    setSyncId(4);
                e.consume();
                break;
        }

        // forward to view
        canvasView.keyPressed(e);
        // forward to map
        canvasMap.keyPressed(e);
        // then send to painters
        super.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // forward to view
        canvasView.keyReleased(e);
        // forward to map
        canvasMap.keyReleased(e);
        // then send to painters
        super.keyReleased(e);
    }

    @Override
    public void refresh()
    {
        canvasView.imageChanged();
        canvasView.layersChanged();
        canvasView.refresh();
    }

    @Override
    public BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView)
    {
        if (c != -1)
            throw new UnsupportedOperationException("getRenderedImage(..) with c != -1 not supported.");

        return getRenderedImage(t, z, canvasView);
    }

    /**
     * Return a rendered image for image at position (t, z)<br>
     * 
     * @param t
     *        T position of wanted image (-1 for complete sequence)
     * @param z
     *        Z position of wanted image (-1 for complete stack)
     */
    public BufferedImage getRenderedImage(int t, int z)
    {
        return getRenderedImage(t, z, true);
    }

    /**
     * Return a rendered image for image at position (t, z)<br>
     * 
     * @param t
     *        T position of wanted image (-1 for complete sequence)
     * @param z
     *        Z position of wanted image (-1 for complete stack)
     * @param canvasView
     *        render with canvas view if true else use default sequence
     *        dimension
     */
    public BufferedImage getRenderedImage(int t, int z, boolean canvasView)
    {
        final IcyBufferedImage srcImg = getImage(t, z);

        if (srcImg == null)
            return null;

        // save position
        final int prevT = getPositionT();
        final int prevZ = getPositionZ();
        final boolean dl = drawLayers;

        if (dl)
        {
            // set wanted position (needed for correct overlay drawing)
            // we have to fire events else some stuff can miss the change
            setPositionT(t);
            setPositionZ(z);
        }
        try
        {
            // FIXME : not really optimal in memory and processing
            final BufferedImage img = srcImg.getARGBImage(getLut());
            final BufferedImage result;
            final Graphics2D g;

            if (canvasView)
            {
                final Dimension size = getCanvasSize();
                final int canvasCenterX = getCanvasSizeX() / 2;
                final int canvasCenterY = getCanvasSizeY() / 2;

                // get result image and graphics object
                result = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
                g = result.createGraphics();

                final AffineTransform trans = new AffineTransform();

                trans.translate(canvasCenterX, canvasCenterY);
                trans.rotate(getRotationZ());
                trans.translate(-canvasCenterX, -canvasCenterY);

                trans.translate(getOffsetX(), getOffsetY());
                trans.scale(getScaleX(), getScaleY());

                // apply transformation (translation + scale)
                g.transform(trans);

                // draw transformed image
                g.drawImage(img, 0, 0, null);
            }
            else
            {
                // no need to go further...
                if (!dl)
                    return img;

                // use the image Graphics object directly
                result = img;
                g = result.createGraphics();
            }

            if (dl)
            {
                final Sequence seq = getSequence();

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                final ArrayList<Layer> layers = getVisibleOrderedLayersForEvent();

                // draw them in inverse order to have first painter event at top
                for (int i = layers.size() - 1; i >= 0; i--)
                {
                    final Layer layer = layers.get(i);

                    final float alpha = layer.getAlpha();

                    if (alpha != 1f)
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    else
                        g.setComposite(AlphaComposite.SrcOver);

                    layer.getPainter().paint(g, seq, this);
                }
            }

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
                    final int posZ = getPositionZ();
                    final int posT = getPositionT();

                    for (IcyCanvas cnv : canvasList)
                    {
                        if (posZ != -1)
                            cnv.setPositionZ(posZ);
                        if (posT != -1)
                            cnv.setPositionT(posT);
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
                setMouseImagePos(canvasToImage(mouseCanvasPos));

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
                                zoomComboBox.setSelectedItem(zoomInfo);
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
                                rotationComboBox.setSelectedItem(rotInfo);
                            }
                            finally
                            {
                                modifyingRotation = false;
                            }
                        }
                    });

                    // and in canvas
                    canvasView.setRotationMessage("Rotation : " + rotInfo + " °", 500);
                }

                // refresh canvas
                canvasView.refresh();
                break;

            case MOUSE_IMAGE_POSITION_CHANGED:
                // mouse position changed outside mouse move event ?
                if (!canvasView.handlingMouseMoveEvent && !canvasView.isDragging() && (!isSynchSlave()))
                {
                    // mouse position in canvas
                    final Point mouseAbsolutePos = new Point(mouseCanvasPos);
                    // absolute mouse position
                    SwingUtilities.convertPointToScreen(mouseAbsolutePos, canvasView);

                    // simulate a mouse move event so painters can handle position
                    // change
                    final MouseEvent mouseEvent = new MouseEvent(this, MouseEvent.MOUSE_MOVED,
                            System.currentTimeMillis(), 0, mouseCanvasPos.x, mouseCanvasPos.y, mouseAbsolutePos.x,
                            mouseAbsolutePos.y, 0, false, 0);

                    // send mouse event to painters
                    for (Layer layer : getVisibleOrderedLayersForEvent())
                        layer.getPainter().mouseMove(mouseEvent, new Point2D.Double(mouseImagePos.x, mouseImagePos.y),
                                this);
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
    public void layersChanged(LayersEvent event)
    {
        super.layersChanged(event);

        // repaint
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
    public void toolChanged(String command)
    {
        final Sequence seq = getSequence();

        // unselected all ROI
        if (seq != null)
            seq.setSelectedROIs(null);
    }
}

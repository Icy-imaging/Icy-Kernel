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
package icy.canvas;

import icy.gui.main.MainFrame;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.painter.Overlay;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * @author Stephane
 */
public abstract class IcyCanvas2D extends IcyCanvas
{
    /**
     * 
     */
    private static final long serialVersionUID = 743937493919099495L;

    /** mouse position (image coordinate space) */
    protected Point2D.Double mouseImagePos;

    // image coordinate to canvas coordinate transform
    protected final AffineTransform transform;
    // canvas coordinate to image coordinate transform
    protected AffineTransform inverseTransform;
    protected boolean transformChanged;

    public IcyCanvas2D(Viewer viewer)
    {
        super(viewer);

        // default for 2D canvas
        posX = -1;
        posY = -1;
        posZ = 0;
        posT = 0;

        // initial mouse position
        mouseImagePos = new Point2D.Double();
        transform = new AffineTransform();
        inverseTransform = new AffineTransform();
        transformChanged = false;
    }

    @Override
    public void setPositionZ(int z)
    {
        // position -1 not supported for Z dimension on this canvas
        if (z != -1)
            super.setPositionZ(z);
    }

    @Override
    public void setPositionT(int t)
    {
        // position -1 not supported for T dimension on this canvas
        if (t != -1)
            super.setPositionT(t);
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

    /**
     * Return mouse image position
     */
    public Point2D.Double getMouseImagePos()
    {
        return (Double) mouseImagePos.clone();
    }

    public void setMouseImagePos(double x, double y)
    {
        if ((mouseImagePos.x != x) || (mouseImagePos.y != y))
        {
            mouseImagePos.x = x;
            mouseImagePos.y = y;

            // direct update of mouse canvas position
            mousePos = imageToCanvas(mouseImagePos);
            // notify change
            mouseImagePositionChanged(DimensionId.NULL);
        }
    }

    /**
     * Set mouse image position
     */
    public void setMouseImagePos(Point2D.Double point)
    {
        setMouseImagePos(point.x, point.y);
    }

    @Override
    public boolean setMousePos(int x, int y)
    {
        final boolean result = super.setMousePos(x, y);

        if (result)
        {
            // direct update of mouse image position
            mouseImagePos = canvasToImage(mousePos);
            // notify change
            mouseImagePositionChanged(DimensionId.NULL);
        }

        return result;
    }

    /**
     * @deprecated Use {@link #setMousePos(int, int)} instead
     */
    @Deprecated
    public void setMouseCanvasPos(int x, int y)
    {
        setMousePos(x, y);
    }

    /**
     * @deprecated Use {@link #setMousePos(Point)} instead.
     */
    @Deprecated
    public void setMouseCanvasPos(Point point)
    {
        setMousePos(point);
    }

    @Override
    protected void setMouseImagePosXInternal(double value)
    {
        mouseImagePos.x = value;

        // direct update of mouse canvas position
        mousePos = imageToCanvas(mouseImagePos);

        super.setMouseImagePosXInternal(value);
    }

    @Override
    protected void setMouseImagePosYInternal(double value)
    {
        mouseImagePos.y = value;

        // direct update of mouse canvas position
        mousePos = imageToCanvas(mouseImagePos);

        super.setMouseImagePosYInternal(value);
    }

    /**
     * Convert specified canvas delta to image delta.<br>
     */
    protected Point2D.Double canvasToImageDelta(int x, int y, double scaleX, double scaleY, double rot)
    {
        // get cos and sin
        final double cos = Math.cos(-rot);
        final double sin = Math.sin(-rot);

        // apply rotation
        final double resX = (x * cos) - (y * sin);
        final double resY = (x * sin) + (y * cos);

        // and scale
        return new Point2D.Double(resX / scaleX, resY / scaleY);
    }

    /**
     * Convert specified canvas delta point to image delta point
     */
    public Point2D.Double canvasToImageDelta(int x, int y)
    {
        return canvasToImageDelta(x, y, getScaleX(), getScaleY(), getRotationZ());
    }

    /**
     * Convert specified canvas delta point to image delta point
     */
    public Point2D.Double canvasToImageDelta(Point point)
    {
        return canvasToImageDelta(point.x, point.y);
    }

    /**
     * Convert specified canvas delta point to image delta point.
     * The conversion is affected by zoom ratio but with the specified logarithm factor.
     */
    public Point2D.Double canvasToImageLogDelta(int x, int y, double logFactor)
    {
        final double sx = getScaleX() / Math.pow(10, Math.log10(getScaleX()) / logFactor);
        final double sy = getScaleY() / Math.pow(10, Math.log10(getScaleY()) / logFactor);

        return canvasToImageDelta(x, y, sx, sy, getRotationZ());
    }

    /**
     * Convert specified canvas delta point to image delta point.
     * The conversion is affected by zoom ratio but with the specified logarithm factor.
     */
    public Point2D.Double canvasToImageLogDelta(int x, int y)
    {
        return canvasToImageLogDelta(x, y, 5d);
    }

    /**
     * Convert specified canvas point to image point.<br>
     * By default we consider the rotation applied relatively to canvas center.<br>
     * Override this method if you want different transformation type.
     */
    protected Point2D.Double canvasToImage(int x, int y, int offsetX, int offsetY, double scaleX, double scaleY,
            double rot)
    {
        // get canvas center
        final double canvasCenterX = getCanvasSizeX() / 2;
        final double canvasCenterY = getCanvasSizeY() / 2;

        // center to canvas for rotation
        final double dx = x - canvasCenterX;
        final double dy = y - canvasCenterY;

        // get cos and sin
        final double cos = Math.cos(-rot);
        final double sin = Math.sin(-rot);

        // apply rotation
        double resX = (dx * cos) - (dy * sin);
        double resY = (dx * sin) + (dy * cos);

        // translate back to position
        resX += canvasCenterX;
        resY += canvasCenterY;

        // basic transform to image coordinates
        resX = ((resX - offsetX) / scaleX);
        resY = ((resY - offsetY) / scaleY);

        return new Point2D.Double(resX, resY);
    }

    /**
     * Convert specified canvas point to image point
     */
    public Point2D.Double canvasToImage(int x, int y)
    {
        final Point2D.Double result = new Point2D.Double(0d, 0d);

        // we can directly use the transform object here
        getInverseTransform().transform(new Point2D.Double(x, y), result);

        return result;

        // return canvasToImage(x, y, getOffsetX(), getOffsetY(), getScaleX(), getScaleY(),
        // getRotationZ());
    }

    /**
     * Convert specified canvas point to image point
     */
    public Point2D.Double canvasToImage(Point point)
    {
        return canvasToImage(point.x, point.y);
    }

    /**
     * Convert specified canvas rectangle to image rectangle
     */
    public Rectangle2D.Double canvasToImage(int x, int y, int w, int h)
    {
        // convert each rectangle point
        final Point2D.Double pt1 = canvasToImage(x, y);
        final Point2D.Double pt2 = canvasToImage(x + w, y);
        final Point2D.Double pt3 = canvasToImage(x + w, y + h);
        final Point2D.Double pt4 = canvasToImage(x, y + h);

        // get minimum and maximum X / Y
        final double minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
        final double maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
        final double minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
        final double maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

        // return transformed rectangle
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Convert specified canvas rectangle to image rectangle
     */
    public Rectangle2D.Double canvasToImage(Rectangle rect)
    {
        return canvasToImage(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Convert specified image delta to canvas delta.<br>
     */
    protected Point imageToCanvasDelta(double x, double y, double scaleX, double scaleY, double rot)
    {
        // apply scale
        final double dx = x * scaleX;
        final double dy = y * scaleY;

        // get cos and sin
        final double cos = Math.cos(rot);
        final double sin = Math.sin(rot);

        // apply rotation
        final double resX = (dx * cos) - (dy * sin);
        final double resY = (dx * sin) + (dy * cos);

        return new Point((int) Math.round(resX), (int) Math.round(resY));
    }

    /**
     * Convert specified image delta point to canvas delta point
     */
    public Point imageToCanvasDelta(double x, double y)
    {
        return imageToCanvasDelta(x, y, getScaleX(), getScaleY(), getRotationZ());
    }

    /**
     * Convert specified image delta point to canvas delta point
     */
    public Point imageToCanvasDelta(Point2D.Double point)
    {
        return imageToCanvasDelta(point.x, point.y);
    }

    /**
     * Convert specified image point to canvas point.<br>
     * By default we consider the rotation applied relatively to image center.<br>
     * Override this method if you want different transformation type.
     */
    protected Point imageToCanvas(double x, double y, int offsetX, int offsetY, double scaleX, double scaleY, double rot)
    {
        // get canvas center
        final double canvasCenterX = getCanvasSizeX() / 2;
        final double canvasCenterY = getCanvasSizeY() / 2;

        // basic transform to canvas coordinates and canvas centering
        final double dx = ((x * scaleX) + offsetX) - canvasCenterX;
        final double dy = ((y * scaleY) + offsetY) - canvasCenterY;

        // get cos and sin
        final double cos = Math.cos(rot);
        final double sin = Math.sin(rot);

        // apply rotation
        double resX = (dx * cos) - (dy * sin);
        double resY = (dx * sin) + (dy * cos);

        // translate back to position
        resX += canvasCenterX;
        resY += canvasCenterY;

        return new Point((int) Math.round(resX), (int) Math.round(resY));
    }

    /**
     * Convert specified image point to canvas point
     */
    public Point imageToCanvas(double x, double y)
    {
        final Point result = new Point();

        // we can directly use the transform object here
        getTransform().transform(new Point2D.Double(x, y), result);

        return result;

        // return imageToCanvas(x, y, getOffsetX(), getOffsetY(), getScaleX(), getScaleY(),
        // getRotationZ());
    }

    /**
     * Convert specified image point to canvas point
     */
    public Point imageToCanvas(Point2D.Double point)
    {
        return imageToCanvas(point.x, point.y);
    }

    /**
     * Convert specified image rectangle to canvas rectangle
     */
    public Rectangle imageToCanvas(double x, double y, double w, double h)
    {
        // convert each rectangle point
        final Point pt1 = imageToCanvas(x, y);
        final Point pt2 = imageToCanvas(x + w, y);
        final Point pt3 = imageToCanvas(x + w, y + h);
        final Point pt4 = imageToCanvas(x, y + h);

        // get minimum and maximum X / Y
        final int minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
        final int maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
        final int minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
        final int maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

        // return transformed rectangle
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Convert specified image rectangle to canvas rectangle
     */
    public Rectangle imageToCanvas(Rectangle2D.Double rect)
    {
        return imageToCanvas(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Get 2D view size in canvas pixel coordinate
     * 
     * @return a Dimension which represents the visible size.
     */
    public Dimension getCanvasSize()
    {
        return new Dimension(getCanvasSizeX(), getCanvasSizeY());
    }

    /**
     * Get 2D image size
     */
    public Dimension getImageSize()
    {
        return new Dimension(getImageSizeX(), getImageSizeY());
    }

    /**
     * Get 2D image size in canvas pixel coordinate
     */
    public Dimension getImageCanvasSize()
    {
        final double imageSizeX = getImageSizeX();
        final double imageSizeY = getImageSizeY();
        final double scaleX = getScaleX();
        final double scaleY = getScaleY();
        final double rot = getRotationZ();

        // convert image rectangle
        final Point pt1 = imageToCanvas(0d, 0d, 0, 0, scaleX, scaleY, rot);
        final Point pt2 = imageToCanvas(imageSizeX, 0d, 0, 0, scaleX, scaleY, rot);
        final Point pt3 = imageToCanvas(0d, imageSizeY, 0, 0, scaleX, scaleY, rot);
        final Point pt4 = imageToCanvas(imageSizeX, imageSizeY, 0, 0, scaleX, scaleY, rot);

        final int minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
        final int maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
        final int minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
        final int maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

        return new Dimension(maxX - minX, maxY - minY);
    }

    /**
     * Get 2D canvas visible rectangle (canvas coordinate).
     */
    public Rectangle getCanvasVisibleRect()
    {
        // try to return view component visible rectangle by default
        final Component comp = getViewComponent();
        if (comp instanceof JComponent)
            return ((JComponent) comp).getVisibleRect();

        // just return the canvas component visible rectangle
        return getVisibleRect();
    }

    /**
     * Get 2D image visible rectangle (image coordinate).<br>
     * Prefer the {@link Graphics#getClipBounds()} method for paint operation as the image visible
     * rectangle may return wrong information sometime (when using the
     * {@link #getRenderedImage(int, int, int, boolean)} method for instance).
     */
    public Rectangle2D getImageVisibleRect()
    {
        return canvasToImage(getCanvasVisibleRect());
    }

    /**
     * Center image on specified image position in canvas
     */
    public void centerOnImage(double x, double y)
    {
        // get point on canvas
        final Point pt = imageToCanvas(x, y);
        final int canvasCenterX = getCanvasSizeX() / 2;
        final int canvasCenterY = getCanvasSizeY() / 2;

        final Point2D.Double newTrans = canvasToImageDelta(canvasCenterX - pt.x, canvasCenterY - pt.y, 1d, 1d,
                getRotationZ());

        setOffsetX(getOffsetX() + (int) Math.round(newTrans.x));
        setOffsetY(getOffsetY() + (int) Math.round(newTrans.y));
    }

    /**
     * Center image on specified image position in canvas
     */
    public void centerOnImage(Point2D.Double pt)
    {
        centerOnImage(pt.x, pt.y);
    }

    /**
     * Center image in canvas
     */
    public void centerImage()
    {
        centerOnImage(getImageSizeX() / 2, getImageSizeY() / 2);
    }

    /**
     * get scale X and scale Y so image fit in canvas view dimension
     */
    protected Point2D.Double getFitImageToCanvasScale()
    {
        final double imageSizeX = getImageSizeX();
        final double imageSizeY = getImageSizeY();

        if ((imageSizeX > 0d) && (imageSizeY > 0d))
        {
            final double rot = getRotationZ();

            // convert image rectangle
            final Point pt1 = imageToCanvas(0d, 0d, 0, 0, 1d, 1d, rot);
            final Point pt2 = imageToCanvas(imageSizeX, 0d, 0, 0, 1d, 1d, rot);
            final Point pt3 = imageToCanvas(0d, imageSizeY, 0, 0, 1d, 1d, rot);
            final Point pt4 = imageToCanvas(imageSizeX, imageSizeY, 0, 0, 1d, 1d, rot);

            final int minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
            final int maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
            final int minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
            final int maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

            // get image dimension transformed by rotation
            final double sx = (double) getCanvasSizeX() / (double) (maxX - minX);
            final double sy = (double) getCanvasSizeY() / (double) (maxY - minY);

            return new Point2D.Double(sx, sy);
        }

        return null;
    }

    /**
     * Change scale so image fit in canvas view dimension
     */
    public void fitImageToCanvas()
    {
        final Point2D.Double s = getFitImageToCanvasScale();

        if (s != null)
        {
            final double scale = Math.min(s.x, s.y);

            setScaleX(scale);
            setScaleY(scale);
        }
    }

    /**
     * Change canvas size (so viewer size) to get it fit with image dimension if possible
     */
    public void fitCanvasToImage()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        final Dimension imageCanvasSize = getImageCanvasSize();

        if ((imageCanvasSize.width > 0) && (imageCanvasSize.height > 0) && (mainFrame != null))
        {
            final Dimension maxDim = mainFrame.getDesktopSize();
            final Dimension adjImgCnvSize = canvasToViewer(imageCanvasSize);

            // fit in available space --> resize viewer
            viewer.setSize(Math.min(adjImgCnvSize.width, maxDim.width), Math.min(adjImgCnvSize.height, maxDim.height));
        }
    }

    /**
     * Convert canvas dimension to viewer dimension
     */
    public Dimension canvasToViewer(Dimension dim)
    {
        final Dimension canvasViewSize = getCanvasSize();
        final Dimension viewerSize = viewer.getSize();
        final Dimension result = new Dimension(dim);

        result.width -= canvasViewSize.width;
        result.width += viewerSize.width;
        result.height -= canvasViewSize.height;
        result.height += viewerSize.height;

        return result;
    }

    /**
     * Convert viewer dimension to canvas dimension
     */
    public Dimension viewerToCanvas(Dimension dim)
    {
        final Dimension canvasViewSize = getCanvasSize();
        final Dimension viewerSize = viewer.getSize();
        final Dimension result = new Dimension(dim);

        result.width -= viewerSize.width;
        result.width += canvasViewSize.width;
        result.height -= viewerSize.height;
        result.height += canvasViewSize.height;

        return result;
    }

    /**
     * Update internal {@link AffineTransform} object.
     */
    protected void updateTransform()
    {
        final int canvasCenterX = getCanvasSizeX() / 2;
        final int canvasCenterY = getCanvasSizeY() / 2;

        // rotation is centered to canvas
        transform.setToTranslation(canvasCenterX, canvasCenterY);
        transform.rotate(getRotationZ());
        transform.translate(-canvasCenterX, -canvasCenterY);

        transform.translate(getOffsetX(), getOffsetY());
        transform.scale(getScaleX(), getScaleY());

        transformChanged = true;
    }

    /**
     * Return the 2D {@link AffineTransform} object which convert from image coordinate to canvas
     * coordinate.<br>
     * {@link Overlay} should directly use the transform information from the {@link Graphics2D}
     * object provided in their {@link Overlay#paint(Graphics2D, Sequence, IcyCanvas)} method.
     */
    public AffineTransform getTransform()
    {
        return transform;
    }

    /**
     * Return the 2D {@link AffineTransform} object which convert from canvas coordinate to image
     * coordinate.<br>
     * {@link Overlay} should directly use the transform information from the {@link Graphics2D}
     * object provided in their {@link Overlay#paint(Graphics2D, Sequence, IcyCanvas)} method.
     */
    public AffineTransform getInverseTransform()
    {
        if (transformChanged)
        {
            try
            {
                inverseTransform = transform.createInverse();
            }
            catch (NoninvertibleTransformException e)
            {
                inverseTransform = new AffineTransform();
            }

            transformChanged = false;
        }

        return inverseTransform;
    }

    @Override
    public void changed(IcyCanvasEvent event)
    {
        super.changed(event);

        switch (event.getType())
        {
            case OFFSET_CHANGED:
            case ROTATION_CHANGED:
            case SCALE_CHANGED:
                updateTransform();
                break;
        }
    }
}

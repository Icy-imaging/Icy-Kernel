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
package plugins.kernel.roi.roi2d;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.common.EventHierarchicalChecker;
import icy.painter.Anchor2D;
import icy.painter.Anchor2D.Anchor2DPositionListener;
import icy.painter.OverlayEvent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.painter.OverlayListener;
import icy.painter.PainterEvent;
import icy.painter.PathAnchor2D;
import icy.painter.VtkPainter;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIEvent;
import icy.sequence.Sequence;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.ShapeUtil;
import icy.util.ShapeUtil.BooleanOperator;
import icy.vtk.VtkUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkPointSet;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

/**
 * @author Stephane
 */
public abstract class ROI2DShape extends ROI2D implements Shape
{
    public class ROI2DShapePainter extends ROI2DPainter implements VtkPainter
    {
        // VTK 3D objects, we use Object to prevent UnsatisfiedLinkError
        Object polyData;
        Object polyMapper;
        Object actor;
        // 3D internal
        boolean needRebuild;
        double scaling[];

        public ROI2DShapePainter()
        {
            super();

            // don't create VTK object on constructor
            polyData = null;
            polyMapper = null;
            actor = null;

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            needRebuild = true;
        }

        /**
         * update 3D painter for 3D canvas (called only when vtk is loaded).
         */
        protected void rebuild3DPainter(VtkCanvas canvas)
        {
            final Sequence seq = canvas.getSequence();

            // nothing to update
            if (seq == null)
                return;

            // initialize VTK objects if not yet done
            if (actor == null)
            {
                // init 3D painters stuff
                polyData = new vtkPolyData();

                polyMapper = new vtkPolyDataMapper();
                ((vtkPolyDataMapper) polyMapper).SetInputData((vtkPolyData) polyData);

                actor = new vtkActor();
                ((vtkActor) actor).SetMapper((vtkPolyDataMapper) polyMapper);
            }

            final List<Point3D.Double> point3DList = new ArrayList<Point3D.Double>();
            final List<Poly3D> polyList = new ArrayList<Poly3D>();
            final double[] coords = new double[6];

            final double nbSlice = seq.getSizeZ(canvas.getPositionT());
            // use flat path
            final PathIterator path = getPathIterator(null, 0.5d);

            // starting position
            double xm = 0d;
            double ym = 0d;
            double x0 = 0d;
            double y0 = 0d;
            double x1 = 0d;
            double y1 = 0d;
            int ind;

            // build point data
            while (!path.isDone())
            {
                int segType = path.currentSegment(coords);

                switch (segType)
                {
                    case PathIterator.SEG_MOVETO:
                        x0 = xm = coords[0];
                        y0 = ym = coords[1];
                        break;

                    case PathIterator.SEG_LINETO:
                        x1 = coords[0];
                        y1 = coords[1];

                        ind = point3DList.size();

                        point3DList.add(new Point3D.Double(x0 * scaling[0], y0 * scaling[1], 0));
                        point3DList.add(new Point3D.Double(x1 * scaling[0], y1 * scaling[1], 0));
                        point3DList.add(new Point3D.Double(x0 * scaling[0], y0 * scaling[1], nbSlice * scaling[2]));
                        point3DList.add(new Point3D.Double(x1 * scaling[0], y1 * scaling[1], nbSlice * scaling[2]));
                        polyList.add(new Poly3D(1 + ind, 2 + ind, 0 + ind));
                        polyList.add(new Poly3D(3 + ind, 2 + ind, 1 + ind));

                        x0 = x1;
                        y0 = y1;
                        break;

                    case PathIterator.SEG_CLOSE:
                        x1 = xm;
                        y1 = ym;

                        ind = point3DList.size();

                        point3DList.add(new Point3D.Double(x0 * scaling[0], y0 * scaling[1], 0));
                        point3DList.add(new Point3D.Double(x1 * scaling[0], y1 * scaling[1], 0));
                        point3DList.add(new Point3D.Double(x0 * scaling[0], y0 * scaling[1], nbSlice * scaling[2]));
                        point3DList.add(new Point3D.Double(x1 * scaling[0], y1 * scaling[1], nbSlice * scaling[2]));
                        polyList.add(new Poly3D(1 + ind, 2 + ind, 0 + ind));
                        polyList.add(new Poly3D(3 + ind, 2 + ind, 1 + ind));

                        x0 = x1;
                        y0 = y1;
                        break;
                }

                path.next();
            }

            // convert to array
            final double[][] vertices = new double[point3DList.size()][3];
            final int[][] indexes = new int[polyList.size()][3];

            int pointIndex = 0;
            for (Point3D.Double p3D : point3DList)
            {
                vertices[pointIndex][0] = p3D.x;
                vertices[pointIndex][1] = p3D.y;
                vertices[pointIndex][2] = p3D.z;
                pointIndex++;
            }

            int polyIndex = 0;
            for (Poly3D poly : polyList)
            {
                indexes[polyIndex][0] = poly.p1;
                indexes[polyIndex][1] = poly.p2;
                indexes[polyIndex][2] = poly.p3;
                polyIndex++;
            }

            ((vtkPolyData) polyData).SetPolys(VtkUtil.getCells(polyList.size(), VtkUtil.prepareCells(indexes)));
            ((vtkPointSet) polyData).SetPoints(VtkUtil.getPoints(vertices));

            ((vtkPolyDataMapper) polyMapper).Update();
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // send event to controls points first
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.keyPressed(e, imagePoint, canvas);
                            }

                            // specific action for ROI2DShape
                            if (!e.isConsumed())
                            {
                                switch (e.getKeyCode())
                                {
                                    case KeyEvent.VK_DELETE:
                                    case KeyEvent.VK_BACK_SPACE:
                                        // try to remove selected point
                                        if (removeSelectedPoint(canvas))
                                            e.consume();
                                        break;
                                }
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }

            }

            // then send event to parent
            super.keyPressed(e, imagePoint, canvas);
        }

        @Override
        public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // send event to controls points first
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.keyReleased(e, imagePoint, canvas);
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.keyReleased(e, imagePoint, canvas);
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // default anchor action on mouse pressed
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.mousePressed(e, imagePoint, canvas);
                            }

                            // specific action for this ROI
                            if (!e.isConsumed())
                            {
                                // left button action
                                if (EventUtil.isLeftMouseButton(e))
                                {
                                    // ROI should not be focused to add point (for multi selection)
                                    if (!isFocused())
                                    {
                                        // try to add point first
                                        if (addPoint(imagePoint.toPoint2D(), EventUtil.isControlDown(e)))
                                            e.consume();
                                    }
                                }
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.mousePressed(e, imagePoint, canvas);
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // default anchor action on mouse release
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.mouseReleased(e, imagePoint, canvas);
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.mouseReleased(e, imagePoint, canvas);
        }

        @Override
        public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // default anchor action on mouse click
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.mouseClick(e, imagePoint, canvas);
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.mouseClick(e, imagePoint, canvas);

            // not yet consumed...
            if (!e.isConsumed())
            {
                // and process ROI stuff now
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        // single click
                        if (e.getClickCount() == 1)
                        {
                            // right click action
                            if (EventUtil.isRightMouseButton(e))
                            {
                                // unselect (don't consume event)
                                if (isSelected())
                                    ROI2DShape.this.setSelected(false);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // default anchor action on mouse drag
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.mouseDrag(e, imagePoint, canvas);
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.mouseDrag(e, imagePoint, canvas);
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (!(canvas instanceof VtkCanvas) && (imagePoint != null))
                    {
                        ROI2DShape.this.beginUpdate();
                        try
                        {
                            // refresh control point state
                            synchronized (controlPoints)
                            {
                                for (Anchor2D pt : controlPoints)
                                    pt.mouseMove(e, imagePoint, canvas);
                            }
                        }
                        finally
                        {
                            ROI2DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.mouseMove(e, imagePoint, canvas);
        }

        /**
         * Draw the ROI
         */
        protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                final Rectangle2D bounds = shape.getBounds2D();

                // enlarge bounds with stroke
                final double over = getAdjustedStroke(canvas) * 2;
                ShapeUtil.enlarge(bounds, over, over, true);

                // define LOD level
                final boolean shapeVisible = isVisible(bounds, g, canvas);
                final boolean small = isSmall(bounds, g, canvas);
                final boolean tiny = isTiny(bounds, g, canvas);

                // simplified draw
                if (small)
                {
                    if (shapeVisible)
                    {
                        final Graphics2D g2 = (Graphics2D) g.create();

                        // draw shape
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                        g2.setColor(getDisplayColor());
                        g2.draw(shape);

                        if (isSelected())
                        {
                            g2.fill(shape);

                            if (!tiny)
                            {
                                // draw simplified control points
                                if (!isReadOnly())
                                {
                                    final int ray = (int) canvas.canvasToImageDeltaX(2);

                                    for (Anchor2D pt : controlPoints)
                                    {
                                        if (pt.isVisible())
                                        {
                                            // control point content
                                            if (pt.isSelected())
                                                g2.setColor(pt.getSelectedColor());
                                            else
                                                g2.setColor(pt.getColor());
                                            g2.fillRect((int) pt.getPositionX() - ray, (int) pt.getPositionY() - ray,
                                                    ray * 2, ray * 2);
                                        }
                                    }
                                }
                            }
                        }

                        g2.dispose();
                    }
                }
                // normal draw
                else
                {
                    // ROI selected ?
                    if (shapeVisible && isSelected())
                    {
                        final Graphics2D g2 = (Graphics2D) g.create();
                        final AlphaComposite prevAlpha = (AlphaComposite) g2.getComposite();

                        float newAlpha = prevAlpha.getAlpha() * getOpacity();
                        newAlpha = Math.min(1f, newAlpha);
                        newAlpha = Math.max(0f, newAlpha);

                        // show content with an alpha factor
                        g2.setComposite(prevAlpha.derive(newAlpha));
                        g2.setColor(getDisplayColor());
                        g2.fill(shape);

                        g2.dispose();
                    }

                    final Graphics2D g2 = (Graphics2D) g.create();

                    if (shapeVisible)
                    {
                        if (isSelected())
                        {
                            // just draw plain object shape without border
                            g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                            g2.setColor(getDisplayColor());
                            g2.draw(shape);
                        }
                        else
                        {
                            // draw border
                            g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                            g2.setColor(Color.black);
                            g2.draw(shape);
                            // draw shape
                            g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                            g2.setColor(getDisplayColor());
                            g2.draw(shape);
                        }
                    }

                    // draw from flatten shape as we use it for collision detection
                    // ShapeUtil.drawFromPath(getPathIterator(null, 0.1), g);

                    if (isSelected() && !isReadOnly())
                    {
                        // draw control point if selected
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                                pt.paint(g2, sequence, canvas);
                        }
                    }

                    g2.dispose();
                }
            }

            if (canvas instanceof VtkCanvas)
            {
                // 3D canvas
                final VtkCanvas canvas3d = (VtkCanvas) canvas;

                // FIXME : need a better implementation
                final double[] s = canvas3d.getVolumeScale();

                // scaling changed ?
                if (!Arrays.equals(scaling, s))
                {
                    // update scaling
                    scaling = s;
                    // need rebuild
                    needRebuild = true;
                }

                // need to rebuild 3D data structures ?
                if (needRebuild)
                {
                    rebuild3DPainter(canvas3d);
                    needRebuild = false;
                }
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
                drawROI(g, sequence, canvas);
        }

        /**
         * Returns <code>true</code> if the specified bounds should be considered as "tiny" in the
         * specified canvas / graphics context.
         */
        protected boolean isVisible(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            return GraphicsUtil.isVisible(g, bounds);
        }

        /**
         * Returns <code>true</code> if the specified bounds should be considered as "tiny" in the
         * specified canvas / graphics context.
         */
        protected boolean isSmall(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isCreating())
                return false;

            final AffineTransform trans = g.getTransform();
            final double scale = Math.max(trans.getScaleX(), trans.getScaleY());
            final double size = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight());

            return size < LOD_SMALL;
        }

        /**
         * Returns <code>true</code> if the specified bounds should be considered as "tiny" in the
         * specified canvas / graphics context.
         */
        protected boolean isTiny(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isCreating())
                return false;

            final AffineTransform trans = g.getTransform();
            final double scale = Math.max(trans.getScaleX(), trans.getScaleY());
            final double size = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight());

            return size < LOD_TINY;
        }

        @Override
        public void setColor(Color value)
        {
            super.setColor(value);

            final Color color = getColor();
            final Color focusedColor = getFocusedColor();

            beginUpdate();
            try
            {
                synchronized (controlPoints)
                {
                    for (Anchor2D anchor : controlPoints)
                    {
                        anchor.setColor(color);
                        anchor.setSelectedColor(focusedColor);
                    }
                }
            }
            finally
            {
                endUpdate();
            }
        }

        @Override
        public vtkProp[] getProps()
        {
            return new vtkProp[] {(vtkProp) actor};
        }
    }

    class Poly3D
    {
        public Poly3D(int p1, int p2, int p3)
        {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        int p1;
        int p2;
        int p3;
    }

    /**
     * ROI shape (in image coordinates)
     */
    protected final Shape shape;
    /**
     * control points
     */
    protected final List<Anchor2D> controlPoints;

    /**
     * internals
     */
    protected final Anchor2DPositionListener anchor2DPositionListener;
    protected final OverlayListener anchor2DOverlayListener;

    public ROI2DShape(Shape shape)
    {
        super();

        this.shape = shape;
        controlPoints = new ArrayList<Anchor2D>();

        anchor2DPositionListener = new Anchor2DPositionListener()
        {
            @Override
            public void positionChanged(Anchor2D source)
            {
                controlPointPositionChanged(source);
            }
        };

        anchor2DOverlayListener = new OverlayListener()
        {
            @Override
            public void overlayChanged(OverlayEvent event)
            {
                controlPointOverlayChanged(event);
            }
        };
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROI2DShapePainter();
    }

    /**
     * build a new anchor with specified position
     */
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new Anchor2D(pos.getX(), pos.getY(), getColor(), getFocusedColor());
    }

    /**
     * build a new anchor with specified position
     */
    protected Anchor2D createAnchor(double x, double y)
    {
        return createAnchor(new Point2D.Double(x, y));
    }

    /**
     * @return the shape
     */
    public Shape getShape()
    {
        return shape;
    }

    @Override
    public void setSelected(boolean value)
    {
        // unselected ? --> unselected all control points
        if (!value)
        {
            synchronized (controlPoints)
            {
                for (Anchor2D pt : controlPoints)
                    pt.setSelected(false);
            }
        }

        super.setSelected(value);
    }

    /**
     * Rebuild shape.<br>
     * This method should be overridden by derived classes which<br>
     * have to call the super.update() method at end.
     */
    protected void updateShape()
    {
        // the shape should have been rebuilt here
        ((ROI2DShapePainter) painter).needRebuild = true;
    }

    protected Anchor2D getSelectedPoint()
    {
        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                if (pt.isSelected())
                    return pt;
        }

        return null;
    }

    /**
     * @deprecated Use {@link #getSelectedPoint()} instead.
     */
    @Deprecated
    protected Anchor2D getSelectedControlPoint()
    {
        return getSelectedPoint();
    }

    @Override
    public boolean hasSelectedPoint()
    {
        return (getSelectedPoint() != null);
    }

    /**
     * Return true if this ROI support adding new point
     */
    public boolean canAddPoint()
    {
        return true;
    }

    /**
     * Return true if this ROI support removing point
     */
    public boolean canRemovePoint()
    {
        return true;
    }

    /**
     * internal use only
     */
    protected void addPoint(Anchor2D pt)
    {
        addPoint(pt, -1);
    }

    /**
     * internal use only
     */
    protected void addPoint(Anchor2D pt, int index)
    {
        pt.addPositionListener(anchor2DPositionListener);
        pt.addOverlayListener(anchor2DOverlayListener);

        if (index == -1)
            controlPoints.add(pt);
        else
            controlPoints.add(index, pt);

        roiChanged();
    }

    public boolean addPoint(Point2D pos, boolean insert)
    {
        if (!canAddPoint())
            return false;

        // insertion mode
        if (insert)
        {
            final Anchor2D pt = createAnchor(pos);

            // place the new point with closest points
            addPoint(pt, getInsertPointPosition(pos));
            // always select
            pt.setSelected(true);

            return true;
        }

        // creation mode
        if (isCreating())
        {
            final Anchor2D pt = createAnchor(pos);

            // just add the new point at last position
            addPoint(pt);
            // always select
            pt.setSelected(true);

            return true;
        }

        return false;
    }

    /**
     * @deprecated Use {@link #addPoint(Point2D, boolean)} instead.
     */
    @Deprecated
    public boolean addPointAt(Point2D pos, boolean insert)
    {
        return addPoint(pos, insert);
    }

    /**
     * internal use only
     */
    @SuppressWarnings("unused")
    protected boolean removePoint(IcyCanvas canvas, Anchor2D pt)
    {
        boolean empty;

        pt.removeOverlayListener(anchor2DOverlayListener);
        pt.removePositionListener(anchor2DPositionListener);

        synchronized (controlPoints)
        {
            controlPoints.remove(pt);
            empty = controlPoints.isEmpty();
        }

        // empty ROI ? --> remove from all sequence
        if (empty)
            remove();
        else
            roiChanged();

        return true;
    }

    /**
     * internal use only (used for fast clear)
     */
    protected void removeAllPoint()
    {
        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
            {
                pt.removeOverlayListener(anchor2DOverlayListener);
                pt.removePositionListener(anchor2DPositionListener);
            }

            controlPoints.clear();
        }
    }

    /**
     * @deprecated Use {@link #removeSelectedPoint(IcyCanvas)} instead.
     */
    @Deprecated
    public boolean removePointAt(IcyCanvas canvas, Point2D imagePoint)
    {
        if (!canRemovePoint())
            return false;

        // first we try to remove selected point
        if (!removeSelectedPoint(canvas))
        {
            // if no point selected, try to select and remove a point at specified position
            if (selectPointAt(canvas, imagePoint))
                return removeSelectedPoint(canvas);

            return false;
        }

        return true;
    }

    /**
     * @deprecated Use {@link #removeSelectedPoint(IcyCanvas)} instead.
     */
    @Deprecated
    protected boolean removeSelectedPoint(IcyCanvas canvas, @SuppressWarnings("unused") Point2D imagePoint)
    {
        return removeSelectedPoint(canvas);
    }

    public boolean removeSelectedPoint(IcyCanvas canvas)
    {
        if (!canRemovePoint())
            return false;

        final Anchor2D selectedPoint = getSelectedPoint();

        if (selectedPoint == null)
            return false;

        synchronized (controlPoints)
        {
            final int index = controlPoints.indexOf(selectedPoint);

            // try to remove point
            if (!removePoint(canvas, selectedPoint))
                return false;

            // last control point removed --> delete ROI
            if (controlPoints.size() == 0)
                remove();
            else
            {
                // save the point position
                final Point2D imagePoint = selectedPoint.getPosition();

                // we are using PathAnchor2D ?
                if (selectedPoint instanceof PathAnchor2D)
                {
                    final PathAnchor2D selectedPathPoint = (PathAnchor2D) selectedPoint;

                    switch (selectedPathPoint.getType())
                    {
                    // we removed a MOVETO point ?
                        case PathIterator.SEG_MOVETO:
                            // try to set next point to MOVETO state
                            if (index < controlPoints.size())
                            {
                                final PathAnchor2D nextPoint = (PathAnchor2D) controlPoints.get(index);

                                // next point is a CLOSE one ?
                                if (nextPoint.getType() == PathIterator.SEG_CLOSE)
                                {
                                    // delete it
                                    if (removePoint(canvas, nextPoint))
                                    {
                                        // it was the last control point --> delete ROI
                                        if (controlPoints.size() == 0)
                                            remove();
                                    }
                                }
                                else
                                {
                                    // whatever is next point, set it to MOVETO
                                    nextPoint.setType(PathIterator.SEG_MOVETO);
                                    nextPoint.setVisible(true);
                                }
                            }
                            break;

                        // we removed a CLOSE point ?
                        case PathIterator.SEG_CLOSE:
                            // try to set previous point to CLOSE state
                            if (index > 0)
                            {
                                final PathAnchor2D prevPoint = (PathAnchor2D) controlPoints.get(index - 1);

                                // next point is a MOVETO one ?
                                if (prevPoint.getType() == PathIterator.SEG_MOVETO)
                                {
                                    // delete it
                                    if (removePoint(canvas, prevPoint))
                                    {
                                        // it was the last control point --> delete ROI
                                        if (controlPoints.size() == 0)
                                            remove();
                                    }
                                }
                                else
                                {
                                    // whatever is previous point, set it to CLOSE
                                    prevPoint.setType(PathIterator.SEG_CLOSE);
                                    prevPoint.setVisible(false);
                                }
                            }
                            break;
                    }
                }

                // select a new point if possible
                if (controlPoints.size() > 0)
                    selectPointAt(canvas, imagePoint);
            }
        }

        return true;
    }

    protected boolean selectPointAt(IcyCanvas canvas, Point2D imagePoint)
    {
        synchronized (controlPoints)
        {
            // find the new selected control point
            for (Anchor2D pt : controlPoints)
            {
                // control point is overlapped ?
                if (pt.isOver(canvas, imagePoint))
                {
                    // select it
                    pt.setSelected(true);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Return total distance of the specified list of points.
     */
    protected double getTotalDistance(List<Point2D> points, boolean connectLastPoint)
    {
        final int size = points.size();
        double result = 0d;

        if (size > 1)
        {
            for (int i = 0; i < size - 1; i++)
                result += points.get(i).distance(points.get(i + 1));

            // add last to first point distance
            if (connectLastPoint)
                result += points.get(size - 1).distance(points.get(0));
        }

        return result;
    }

    /**
     * Return total distance of the specified list of points.
     */
    protected double getTotalDistance(List<Point2D> points)
    {
        // by default the total length need last point connection
        return getTotalDistance(points, true);
    }

    /**
     * Find best insert position for specified point
     */
    protected int getInsertPointPosition(Point2D pos)
    {
        final List<Point2D> points = getPoints();

        final int size = points.size();
        // by default we use last position
        int result = size;
        double minDistance = Double.MAX_VALUE;

        // we try all cases
        for (int i = size; i >= 0; i--)
        {
            // add point at current position
            points.add(i, pos);

            // calculate total distance
            final double d = getTotalDistance(points);
            // minimum distance ?
            if (d < minDistance)
            {
                // save index
                minDistance = d;
                result = i;
            }

            // remove point from current position
            points.remove(i);
        }

        return result;
    }

    /**
     * Returns true if specified point coordinates overlap the ROI edge.
     */
    @Override
    public boolean isOverEdge(IcyCanvas canvas, double x, double y)
    {
        // use bigger stroke for isOver test for easier intersection
        final double strk = painter.getAdjustedStroke(canvas) * 3;
        final Rectangle2D rect = new Rectangle2D.Double(x - (strk * 0.5), y - (strk * 0.5), strk, strk);
        final Rectangle2D roiBounds = getBounds2D();

        // special test for empty object (point or orthogonal line)
        if (roiBounds.isEmpty())
            return rect.intersectsLine(roiBounds.getMinX(), roiBounds.getMinY(), roiBounds.getMaxX(),
                    roiBounds.getMaxY());

        // fast intersect test to start with
        if (roiBounds.intersects(rect))
            // use flatten path, intersects on curved shape return incorrect result
            return ShapeUtil.pathIntersects(getPathIterator(null, 0.1), rect);

        return false;
    }

    // @Override
    // public boolean isOverPoint(IcyCanvas canvas, double x, double y)
    // {
    // if (isSelected())
    // {
    // for (Anchor2D pt : controlPoints)
    // if (pt.isOver(canvas, x, y))
    // return true;
    // }
    //
    // return false;
    // }

    /**
     * Return the list of control points for this ROI.
     */
    public List<Anchor2D> getControlPoints()
    {
        synchronized (controlPoints)
        {
            return new ArrayList<Anchor2D>(controlPoints);
        }
    }

    /**
     * Return the list of positions of control points for this ROI.
     */
    public ArrayList<Point2D> getPoints()
    {
        final ArrayList<Point2D> result = new ArrayList<Point2D>();

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                result.add(pt.getPosition());
        }

        return result;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return shape.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return shape.getPathIterator(at, flatness);
    }

    @Override
    public boolean contains(Point2D p)
    {
        return shape.contains(p);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return shape.contains(r);
    }

    @Override
    public boolean contains(double x, double y)
    {
        return shape.contains(x, y);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return shape.contains(x, y, w, h);
    }

    @Override
    public boolean contains(ROI roi)
    {
        if (roi instanceof ROI2DShape)
            // if the union of both ROI equals base ROI then base ROI contains the other one
            return ShapeUtil.union(shape, ((ROI2DShape) roi).shape).equals(new Area(shape));

        return super.contains(roi);
    }

    @Override
    public boolean intersects(Rectangle2D r)
    {
        return shape.intersects(r);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return shape.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(ROI roi)
    {
        if (roi instanceof ROI2DShape)
            // if the intersection of both ROI is not empty
            return !ShapeUtil.intersect(shape, ((ROI2DShape) roi).shape).isEmpty();

        return super.intersects(roi);
    }

    @Override
    public Rectangle2D computeBounds2D()
    {
        return shape.getBounds2D();
    }

    @Override
    protected ROI computeOperation(ROI roi, BooleanOperator op) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;
            ROI2DPath result = null;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                // special case for subtraction
                if (op == null)
                    result = new ROI2DPath(ShapeUtil.subtract(this, roiShape));
                else if (op == BooleanOperator.AND)
                    result = new ROI2DPath(ShapeUtil.intersect(this, roiShape));
                else if (op == BooleanOperator.OR)
                    result = new ROI2DPath(ShapeUtil.union(this, roiShape));
                else if (op == BooleanOperator.XOR)
                    result = new ROI2DPath(ShapeUtil.exclusiveUnion(this, roiShape));
            }

            if (result != null)
            {
                // don't forget to restore 5D position
                result.setZ(getZ());
                result.setT(getT());
                result.setC(getC());

                return result;
            }
        }

        return super.computeOperation(roi, op);
    }

    @Override
    public boolean canTranslate()
    {
        return true;
    }

    @Override
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            synchronized (controlPoints)
            {
                for (Anchor2D pt : controlPoints)
                    pt.translate(dx, dy);
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Called when anchor position changed
     */
    public void controlPointPositionChanged(Anchor2D source)
    {
        // anchor(s) position changed --> ROI changed
        roiChanged();
    }

    /**
     * Called when anchor overlay changed
     */
    public void controlPointOverlayChanged(OverlayEvent event)
    {
        // we only mind about painter change from anchor...
        if (event.getType() == OverlayEventType.PAINTER_CHANGED)
        {
            // we have a control point selected --> remove focus on ROI
            if (hasSelectedPoint())
                setFocused(false);

            // anchor changed --> ROI painter changed
            getOverlay().painterChanged();
        }
    }

    /**
     * Called when anchor painter changed, provided only for backward compatibility.<br>
     * Don't use it.
     */
    @SuppressWarnings({"deprecation", "unused"})
    public void painterChanged(PainterEvent event)
    {
        // ignore it now
    }

    /**
     * roi changed
     */
    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        final ROIEvent event = (ROIEvent) object;

        // do here global process on ROI change
        switch (event.getType())
        {
            case ROI_CHANGED:
                // refresh shape
                updateShape();
                break;
        }

        super.onChanged(object);
    }
}

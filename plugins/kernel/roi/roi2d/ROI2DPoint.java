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
import icy.painter.Anchor2D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point5D;
import icy.type.point.Point5D.Double;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

/**
 * ROI 2D Point class.<br>
 * Define a single point ROI<br>
 * 
 * @author Stephane
 */
public class ROI2DPoint extends ROI2DShape
{
    public class ROI2DPointPainter extends ROI2DShapePainter
    {
        @Override
        protected boolean isSmall(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isSelected())
                return false;

            return true;
        }

        @Override
        protected boolean isTiny(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isSelected())
                return false;

            return true;
        }

        @Override
        public void mouseMove(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            super.mouseMove(e, imagePoint, canvas);

            // special case: we want to set focus when we have control point selected
            if (hasSelectedPoint())
                setFocused(true);
        }

        @Override
        protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                if (isSelected() && !isReadOnly())
                {
                    // draw control point if selected
                    synchronized (controlPoints)
                    {
                        for (Anchor2D pt : controlPoints)
                            pt.paint(g2, sequence, canvas);
                    }
                }
                else
                {
                    final Point2D pos = getPoint();
                    final double ray = getAdjustedStroke(canvas);
                    final Ellipse2D ellipse = new Ellipse2D.Double(pos.getX() - ray, pos.getY() - ray, ray * 2, ray * 2);

                    // draw shape
                    g2.setColor(getDisplayColor());
                    g2.fill(ellipse);
                }

                g2.dispose();
            }

            if (canvas instanceof VtkCanvas)
            {
                // 3D canvas
                final VtkCanvas cnv = (VtkCanvas) canvas;

                // FIXME : need a better implementation
                final double[] s = cnv.getVolumeScale();

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
                    // initialize VTK objects if not yet done
                    if (actor == null)
                        initVtkObjects();

                    // request rebuild 3D objects
                    canvas3d = cnv;
                    ThreadUtil.runSingle(this);
                    needRebuild = false;
                }

                // actor can be accessed in canvas3d for rendering so we need to synchronize access
                cnv.lock();
                try
                {
                    // update visibility
                    if (actor != null)
                        ((vtkActor) actor).SetVisibility(canvas.isVisible(this) ? 1 : 0);
                }
                finally
                {
                    cnv.unlock();
                }
            }
        }

        @Override
        protected void initVtkObjects()
        {
            // init 3D painters stuff
            vtkSource = new vtkSphereSource();
            ((vtkSphereSource) vtkSource).SetRadius(2);
            ((vtkSphereSource) vtkSource).SetThetaResolution(12);
            ((vtkSphereSource) vtkSource).SetPhiResolution(12);

            polyMapper = new vtkPolyDataMapper();
            ((vtkPolyDataMapper) polyMapper).SetInputConnection(((vtkSphereSource) vtkSource).GetOutputPort());

            actor = new vtkActor();
            ((vtkActor) actor).SetMapper((vtkPolyDataMapper) polyMapper);
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected void rebuildVtkObjects()
        {
            final VtkCanvas canvas = canvas3d;
            // nothing to update
            if (canvas == null)
                return;

            final Sequence seq = canvas.getSequence();
            // nothing to update
            if (seq == null)
                return;

            final Point2D pos = getPoint();
            double curZ = getZ();

            // all slices ?
            if (curZ == -1)
                // set object at middle of the volume
                curZ = seq.getSizeZ() / 2d;

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            canvas3d.lock();
            try
            {
                ((vtkSphereSource) vtkSource).SetRadius(getStroke());
                ((vtkSphereSource) vtkSource).SetCenter(pos.getX(), pos.getY(), curZ);
                ((vtkPolyDataMapper) polyMapper).Update();

                ((vtkActor) actor).SetScale(scaling);
                final Color color = getColor();
                ((vtkActor) actor).GetProperty().SetColor(color.getRed() / 255d, color.getGreen() / 255d,
                        color.getBlue() / 255d);
                // opacity is for interior only, contour can be done with layer opacity information
                // ((vtkActor) actor).GetProperty().SetOpacity(getOpacity());
            }
            finally
            {
                canvas3d.unlock();
            }

            // no more pending request
            if (!ThreadUtil.hasWaitingSingleTask(this))
                canvas3d = null;
        }
    }

    public static final String ID_POSITION = "position";

    private final Anchor2D position;

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPoint(Point2D pt, boolean cm)
    {
        this(pt);
    }

    public ROI2DPoint(Point2D position)
    {
        super(new Line2D.Double());

        this.position = createAnchor(position);

        // add to the control point list
        controlPoints.add(this.position);

        this.position.addOverlayListener(anchor2DOverlayListener);
        this.position.addPositionListener(anchor2DPositionListener);

        // select the point for "interactive" mode
        this.position.setSelected(true);
        // getOverlay().setMousePos(new Point5D.Double(position.getX(), position.getY(), -1d, -1d,
        // -1d));

        updateShape();

        // set name and icon
        setName("Point2D");
        setIcon(ResourceUtil.ICON_ROI_POINT);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPoint(Point5D pt)
    {
        this(pt.toPoint2D());
        // getOverlay().setMousePos(pt);
    }

    public ROI2DPoint(double x, double y)
    {
        this(new Point2D.Double(x, y));
    }

    public ROI2DPoint()
    {
        this(new Point2D.Double());
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROI2DPointPainter();
    }

    /**
     * @deprecated Use {@link #getLine()} instead.
     */
    @Deprecated
    public Rectangle2D getRectangle()
    {
        final Point2D pt = getPoint();
        return new Rectangle2D.Double(pt.getX(), pt.getY(), 0d, 0d);
    }

    public Line2D getLine()
    {
        return (Line2D) shape;
    }

    public Point2D getPoint()
    {
        return position.getPosition();
    }

    @Override
    public boolean contains(double x, double y)
    {
        return false;
    }

    @Override
    public boolean contains(Point2D p)
    {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return false;
    }

    @Override
    public boolean contains(ROI roi)
    {
        return false;
    }

    @Override
    protected void updateShape()
    {
        final Point2D pt = getPoint();
        final double x = pt.getX();
        final double y = pt.getY();

        getLine().setLine(x, y, x, y);

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean canAddPoint()
    {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    protected boolean removePoint(IcyCanvas canvas, Anchor2D pt)
    {
        if (canvas != null)
        {
            // remove point on this ROI remove the ROI from current sequence
            canvas.getSequence().removeROI(this);
            return true;
        }

        return false;
    }

    @Override
    public double computeNumberOfContourPoints()
    {
        return 0d;
    }

    @Override
    public double computeNumberOfPoints()
    {
        return 0d;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            position.loadPositionFromXML(XMLUtil.getElement(node, ID_POSITION));
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

        position.savePositionToXML(XMLUtil.setElement(node, ID_POSITION));

        return true;
    }
}

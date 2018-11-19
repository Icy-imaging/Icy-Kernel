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
import icy.common.CollapsibleEvent;
import icy.painter.Anchor2D;
import icy.painter.OverlayEvent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.vtk.IcyVtkPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

/**
 * ROI 2D Point class.<br>
 * Define a single point ROI<br>
 * 
 * @author Stephane Dallongeville
 */
public class ROI2DPoint extends ROI2DShape
{
    public class ROI2DPointPainter extends ROI2DShapePainter
    {
        vtkSphereSource vtkSource;

        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();

            // release extra VTK objects
            if (vtkSource != null)
                vtkSource.Delete();
        }

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
        public void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
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
                    final Ellipse2D ellipse = new Ellipse2D.Double(pos.getX() - ray, pos.getY() - ray, ray * 2,
                            ray * 2);

                    // draw shape
                    g2.setColor(getDisplayColor());
                    g2.fill(ellipse);
                }

                g2.dispose();
            }
            else
                // just use parent method
                super.drawROI(g, sequence, canvas);
        }

        @Override
        protected void initVtkObjects()
        {
            super.initVtkObjects();
            
            // init 3D painters stuff
            vtkSource = new vtkSphereSource();
            vtkSource.SetRadius(getStroke());
            vtkSource.SetThetaResolution(12);
            vtkSource.SetPhiResolution(12);

            // delete previously created objects that we will recreate
            if (actor != null)
                actor.Delete();
            if (polyMapper != null)
                polyMapper.Delete();
            
            polyMapper = new vtkPolyDataMapper();
            polyMapper.SetInputConnection((vtkSource).GetOutputPort());

            actor = new vtkActor();
            actor.SetMapper(polyMapper);

            // initialize color
            final Color col = getColor();
            actor.GetProperty().SetColor(col.getRed() / 255d, col.getGreen() / 255d, col.getBlue() / 255d);
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected void rebuildVtkObjects()
        {
            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
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
            vtkPanel.lock();
            try
            {
                // need to handle scaling on radius and position to keep a "round" sphere (else we obtain ellipsoid)
                vtkSource.SetRadius(getStroke() * scaling[0]);
                vtkSource.SetCenter(pos.getX() * scaling[0], pos.getY() * scaling[1], (curZ + 0.5d) * scaling[2]);
                polyMapper.Update();

                // vtkSource.SetRadius(getStroke());
                // vtkSource.SetCenter(pos.getX(), pos.getY(), curZ);
                // polyMapper.Update();
                // actor.SetScale(scaling);
            }
            finally
            {
                vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }

        @Override
        protected void updateVtkDisplayProperties()
        {
            if (actor == null)
                return;

            final VtkCanvas cnv = canvas3d.get();
            final Color col = getDisplayColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;
            // final float opacity = getOpacity();

            final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

            // we need to lock canvas as actor can be accessed during rendering
            if (vtkPanel != null)
                vtkPanel.lock();
            try
            {
                actor.GetProperty().SetColor(r, g, b);
            }
            finally
            {
                if (vtkPanel != null)
                    vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
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
        this.position.setSelected(true);
        addPoint(this.position);

        // set icon (default name is defined by getDefaultName())
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
    public String getDefaultName()
    {
        return "Point2D";
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

    /**
     * Called when anchor overlay changed
     */
    @Override
    public void controlPointOverlayChanged(OverlayEvent event)
    {
        // we only mind about painter change from anchor...
        if (event.getType() == OverlayEventType.PAINTER_CHANGED)
        {
            // here we want to have ROI focused when point is selected (special case for ROIPoint)
            if (hasSelectedPoint())
                setFocused(true);

            // anchor changed --> ROI painter changed
            getOverlay().painterChanged();
        }
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
    public boolean intersects(ROI r)
    {
        // special case of ROI2DPoint
        if (r instanceof ROI2DPoint)
            return onSamePos(((ROI2DPoint) r), false) && ((ROI2DPoint) r).getPoint().equals(getPoint());

        return super.intersects(r);
    }

    /**
     * roi changed
     */
    @Override
    public void onChanged(CollapsibleEvent object)
    {
        final ROIEvent event = (ROIEvent) object;

        // do here global process on ROI change
        switch (event.getType())
        {
            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                // stroke changed --> rebuild vtk object
                if (StringUtil.equals(property, PROPERTY_STROKE))
                    ((ROI2DShapePainter) getOverlay()).needRebuild = true;
                break;

            case SELECTION_CHANGED:
                // always select the control point when ROI was just selected
                if (isSelected())
                    position.setSelected(true);
                break;

            default:
                break;
        }

        super.onChanged(object);
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

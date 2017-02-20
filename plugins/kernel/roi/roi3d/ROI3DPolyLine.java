package plugins.kernel.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.common.CollapsibleEvent;
import icy.math.Line3DIterator;
import icy.painter.Anchor3D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.sequence.Sequence;
import icy.type.geom.Line3D;
import icy.type.geom.Polyline3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.vtk.IcyVtkPanel;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkTubeFilter;

/**
 * 3D Polyline ROI
 * 
 * @author Stephane Dallongeville
 */
public class ROI3DPolyLine extends ROI3DShape
{
    public class ROI3DPolyLinePainter extends ROI3DShapePainter
    {
        // extra VTK 3D objects
        protected vtkTubeFilter tubeFilter;

        public ROI3DPolyLinePainter()
        {
            super();

            // don't create VTK object on constructor
            tubeFilter = null;
        }

        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();

            // release allocated VTK resources
            if (tubeFilter != null)
                tubeFilter.Delete();
        };

        @Override
        protected void initVtkObjects()
        {
            super.initVtkObjects();

            // init specific tube filter
            tubeFilter = new vtkTubeFilter();
            tubeFilter.SetInputData(polyData);
            tubeFilter.SetRadius(1d);
            tubeFilter.CappingOn();
            tubeFilter.SetNumberOfSides(8);
            // tubeFilter.SidesShareVerticesOff();
            polyMapper.SetInputConnection(tubeFilter.GetOutputPort());
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected boolean rebuildVtkObjects()
        {
            if (!super.rebuildVtkObjects())
                return false;

            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return false;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return false;

            // sub VTK object not yet initialized (it can happen, have to check why ??)
            if (tubeFilter == null)
                return false;

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                // just be sure the tube filter is also up to date
                tubeFilter.Update();
            }
            finally
            {
                vtkPanel.unlock();
            }

            return true;
        }

        protected boolean updateVtkTubeRadius()
        {
            // VTK object not yet initialized
            if (actor == null)
                return false;

            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return false;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return false;

            // sub VTK object not yet initialized (it can happen, have to check why ??)
            if (tubeFilter == null)
                return false;

            // update tube radius base on canvas scale X and image scale X
            final double radius = canvas.canvasToImageLogDeltaX((int) getStroke()) * scaling[0];

            if (tubeFilter.GetRadius() != radius)
            {
                // actor can be accessed in canvas3d for rendering so we need to synchronize access
                vtkPanel.lock();
                try
                {
                    tubeFilter.SetRadius(radius);
                    tubeFilter.Update();
                }
                finally
                {
                    vtkPanel.unlock();
                }

                // need to repaint
                painterChanged();
            }

            return true;
        }

        @Override
        public void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            super.drawROI(g, sequence, canvas);

            // update VTK tube radius if needed
            if (canvas instanceof VtkCanvas)
                updateVtkTubeRadius();
        }

        @Override
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified)
        {
            drawShape(g, sequence, canvas, simplified, false);
        }
    }

    /**
     * 
     */
    public ROI3DPolyLine(Point3D pt)
    {
        super(new Polyline3D());

        // add points to list
        final Anchor3D anchor = createAnchor(pt);
        // just add the new point at last position
        addPoint(anchor);
        // always select
        anchor.setSelected(true);

        updatePolyline();

        // set icon
        setIcon(ResourceUtil.ICON_ROI_POLYLINE);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI3DPolyLine(Point5D pt)
    {
        this(pt.toPoint3D());
    }

    public ROI3DPolyLine(Polyline3D polyline)
    {
        this(new Point3D.Double());

        setPolyline3D(polyline);
    }

    public ROI3DPolyLine(List<Point3D> points)
    {
        this(new Point3D.Double());

        setPoints(points);
    }

    public ROI3DPolyLine()
    {
        this(new Point3D.Double());
    }

    @Override
    public String getDefaultName()
    {
        return "PolyLine3D";
    }

    @Override
    protected ROI3DPolyLinePainter createPainter()
    {
        return new ROI3DPolyLinePainter();
    }

    public Polyline3D getPolyline3D()
    {
        return (Polyline3D) shape;
    }

    public void setPoints(List<Point3D> pts)
    {
        beginUpdate();
        try
        {
            removeAllPoint();
            for (Point3D pt : pts)
                addNewPoint(pt, false);
        }
        finally
        {
            endUpdate();
        }
    }

    public void setPolyline3D(Polyline3D value)
    {
        beginUpdate();
        try
        {
            removeAllPoint();
            for (int i = 0; i < value.npoints; i++)
                addNewPoint(new Point3D.Double(value.xpoints[i], value.ypoints[i], value.zpoints[i]), false);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    protected double getTotalDistance(List<Point3D> points, double factorX, double factorY, double factorZ)
    {
        // for polyline the total length don't need last point connection
        return Point3D.getTotalDistance(points, factorX, factorY, factorZ, false);
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, boolean inclusive)
    {
        if ((width <= 0) || (height <= 0))
            return new boolean[0];

        final List<Point3D> points = getPointsInternal();
        final boolean[] result = new boolean[width * height];

        // 2D bounds
        final Rectangle bounds2d = new Rectangle(x, y, width, height);

        for (int i = 1; i < points.size(); i++)
            drawLine3DInBooleanMask2D(bounds2d, result, z, points.get(i - 1), points.get(i));

        return result;
    }

    public static void drawLine3DInBooleanMask2D(Rectangle bounds2d, boolean[] result, int z, Point3D p1, Point3D p2)
    {
        final Line2D l = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());

        // 2D intersection ?
        if (l.intersects(bounds2d))
        {
            // 3D intersection ?
            if (((p1.getZ() <= z) && (p2.getZ() >= z)) || ((p2.getZ() <= z) && (p1.getZ() >= z)))
            {
                final int bx = bounds2d.x;
                final int by = bounds2d.y;
                final int pitch = bounds2d.width;
                final Line3DIterator it = new Line3DIterator(new Line3D(p1, p2), 1d);

                while (it.hasNext())
                {
                    final Point3D pt = it.next();

                    // same Z ?
                    if (Math.floor(pt.getZ()) == z)
                    {
                        final int x = (int) Math.floor(pt.getX());
                        final int y = (int) Math.floor(pt.getY());

                        // draw inside the mask
                        if (bounds2d.contains(x, y))
                            result[(x - bx) + ((y - by) * pitch)] = true;
                    }
                }
            }
        }
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
            case ROI_CHANGED:
                // refresh shape
                updatePolyline();
                break;

            case FOCUS_CHANGED:
                ((ROI3DPolyLinePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case SELECTION_CHANGED:
                final boolean s = isSelected();

                // update controls point state given the selection state of the ROI
                synchronized (controlPoints)
                {
                    for (Anchor3D pt : controlPoints)
                    {
                        pt.setVisible(s);
                        if (!s)
                            pt.setSelected(false);
                    }
                }

                ((ROI3DPolyLinePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                if (StringUtil.equals(property, PROPERTY_STROKE) || StringUtil.equals(property, PROPERTY_COLOR)
                        || StringUtil.equals(property, PROPERTY_OPACITY))
                    ((ROI3DPolyLinePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            default:
                break;
        }

        super.onChanged(object);
    }

    @Override
    public double computeNumberOfPoints()
    {
        return 0d;
    }

    @Override
    public boolean contains(ROI roi)
    {
        return false;
    }

    protected void updatePolyline()
    {
        final int len = controlPoints.size();
        final double ptsX[] = new double[len];
        final double ptsY[] = new double[len];
        final double ptsZ[] = new double[len];

        for (int i = 0; i < len; i++)
        {
            final Anchor3D pt = controlPoints.get(i);

            ptsX[i] = pt.getX();
            ptsY[i] = pt.getY();
            ptsZ[i] = pt.getZ();
        }

        final Polyline3D polyline3d = getPolyline3D();

        // we can have a problem here if we try to redraw while we are modifying the polygon points
        synchronized (polyline3d)
        {
            polyline3d.npoints = len;
            polyline3d.xpoints = ptsX;
            polyline3d.ypoints = ptsY;
            polyline3d.zpoints = ptsZ;
            polyline3d.calculateLines();
        }

        // the shape should have been rebuilt here
        ((ROI3DPolyLinePainter) painter).needRebuild = true;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            removeAllPoint();

            final ArrayList<Node> nodesPoint = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null)
            {
                for (Node n : nodesPoint)
                {
                    final Anchor3D pt = createAnchor(new Point3D.Double());
                    pt.loadPositionFromXML(n);
                    addPoint(pt);
                }
            }
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

        final Element dependances = XMLUtil.setElement(node, ID_POINTS);
        for (Anchor3D pt : controlPoints)
            pt.savePositionToXML(XMLUtil.addElement(dependances, ID_POINT));

        return true;
    }
}

/**
 * 
 */
package plugins.kernel.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.math.Line3DIterator;
import icy.painter.Anchor3D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.type.geom.Line3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.util.XMLUtil;
import icy.vtk.IcyVtkPanel;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkTubeFilter;

/**
 * ROI 3D Line.
 * 
 * @author Stephane Dallongeville
 */
public class ROI3DLine extends ROI3DShape
{
    public class ROI3DLinePainter extends ROI3DShapePainter
    {
        // extra VTK 3D objects
        protected vtkTubeFilter tubeFilter;

        public ROI3DLinePainter()
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

        @Override
        protected boolean isTiny(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isSelected())
                return false;

            return super.isTiny(bounds, g, canvas);
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected void rebuildVtkObjects()
        {
            super.rebuildVtkObjects();

            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            // sub VTK object not yet initialized (it can happen, have to check why ??)
            if (tubeFilter == null)
                return;

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
        }

        protected void updateVtkTubeRadius()
        {
            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            // sub VTK object not yet initialized (it can happen, have to check why ??)
            if (tubeFilter == null)
                return;

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

    public static final String ID_PT1 = "pt1";
    public static final String ID_PT2 = "pt2";

    protected final Anchor3D pt1;
    protected final Anchor3D pt2;

    public ROI3DLine(Point3D pt1, Point3D pt2)
    {
        super(new Line3D());

        this.pt1 = createAnchor(pt1);
        this.pt2 = createAnchor(pt2);
        // keep pt2 selected to size the line for "interactive mode"
        this.pt2.setSelected(true);

        addPoint(this.pt1);
        addPoint(this.pt2);

        // set icon
        setIcon(ResourceUtil.ICON_ROI_LINE);
    }

    public ROI3DLine(Line3D line)
    {
        this(line.getP1(), line.getP2());
    }

    public ROI3DLine(Point3D pt)
    {
        this(new Point3D.Double(pt.getX(), pt.getY(), pt.getZ()), pt);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI3DLine(Point5D pt)
    {
        this(pt.toPoint3D());
        // getOverlay().setMousePos(pt);
    }

    public ROI3DLine(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        this(new Point3D.Double(x1, y1, z1), new Point3D.Double(x2, y2, z2));
    }

    public ROI3DLine()
    {
        this(new Point3D.Double(), new Point3D.Double());
    }

    @Override
    public String getDefaultName()
    {
        return "Line3D";
    }

    @Override
    protected ROI3DShapePainter createPainter()
    {
        return new ROI3DLinePainter();
    }

    public Line3D getLine()
    {
        return (Line3D) shape;
    }

    @Override
    protected void updateShape()
    {
        getLine().setLine(pt1.getPosition(), pt2.getPosition());

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
    public boolean canRemovePoint()
    {
        // this ROI doesn't support point remove
        return false;
    }

    @Override
    protected boolean removePoint(IcyCanvas canvas, Anchor3D pt)
    {
        // this ROI doesn't support point remove
        return false;
    }

    @Override
    public boolean canSetBounds()
    {
        return true;
    }

    @Override
    public void setBounds3D(Rectangle3D bounds)
    {
        beginUpdate();
        try
        {
            pt1.setPosition(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
            pt2.setPosition(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        }
        finally
        {
            endUpdate();
        }
    }

    public void setLine(Line3D line)
    {
        setBounds3D(line.getBounds());
    }

    @Override
    protected double getTotalDistance(List<Point3D> points, double factorX, double factorY, double factorZ)
    {
        // for line the total length don't need last point connection
        return Point3D.getTotalDistance(points, factorX, factorY, factorZ, false);
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, boolean inclusive)
    {
        if ((width <= 0) || (height <= 0))
            return new boolean[0];

        final boolean[] result = new boolean[width * height];
        // 2D bounds
        final Rectangle bounds2d = new Rectangle(x, y, width, height);

        drawLine3DInBooleanMask2D(bounds2d, result, z, pt1.getPositionInternal(), pt2.getPositionInternal());

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

    @Override
    public boolean intersects(ROI r)
    {
        // special case of ROI3DLine
        if (r instanceof ROI3DLine)
            return onSamePos(((ROI3DLine) r), false) && ((ROI3DLine) r).getLine().intersectsLine(getLine());

        return super.intersects(r);
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            pt1.loadPositionFromXML(XMLUtil.getElement(node, ID_PT1));
            pt2.loadPositionFromXML(XMLUtil.getElement(node, ID_PT2));
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

        pt1.savePositionToXML(XMLUtil.setElement(node, ID_PT1));
        pt2.savePositionToXML(XMLUtil.setElement(node, ID_PT2));

        return true;
    }
}

/**
 * 
 */
package plugins.kernel.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.common.CollapsibleEvent;
import icy.painter.Anchor3D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.sequence.Sequence;
import icy.type.geom.Line3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.vtk.IcyVtkPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

/**
 * ROI 3D Point class.<br>
 * 
 * @author Stephane Dallongeville
 */
public class ROI3DPoint extends ROI3DShape
{
    public class ROI3DPointPainter extends ROI3DShapePainter
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

        // @Override
        // public void mouseMove(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        // {
        // super.mouseMove(e, imagePoint, canvas);
        //
        // // special case: we want to set focus when we have control point selected
        // if (hasSelectedPoint())
        // setFocused(true);
        // }

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
                        for (Anchor3D pt : controlPoints)
                            pt.paint(g2, sequence, canvas);
                    }
                }
                else
                {
                    final Point3D pos = getPoint();
                    final double ray = getAdjustedStroke(canvas);
                    final Ellipse2D ellipse = new Ellipse2D.Double(pos.getX() - ray, pos.getY() - ray, ray * 2, ray * 2);

                    // get canvas Z position
                    final int cnvZ = canvas.getPositionZ();
                    // calculate z fade range
                    final double zRange = Math.min(10d, Math.max(3d, sequence.getSizeZ() / 8d));

                    // get Z pos
                    final double z = pos.getZ();
                    // get delta Z (difference between canvas Z position and point Z pos)
                    final double dz = Math.abs(z - cnvZ);

                    // not visible on this Z position
                    if (dz > zRange)
                        return;

                    // ratio for size / opacity
                    final float ratio = 1f - (float) (dz / zRange);

                    if (ratio != 1f)
                        GraphicsUtil.mixAlpha(g2, ratio);

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
            // init 3D painters stuff
            vtkSource = new vtkSphereSource();
            vtkSource.SetRadius(getStroke());
            vtkSource.SetThetaResolution(12);
            vtkSource.SetPhiResolution(12);

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

            final Point3D pos = getPoint();

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                // need to handle scaling on radius and position to keep a "round" sphere (else we obtain ellipsoid)
                vtkSource.SetRadius(getStroke() * scaling[0]);
                vtkSource.SetCenter(pos.getX() * scaling[0], pos.getY() * scaling[1], pos.getZ() * scaling[2]);
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
            if (actor != null)
            {
                final VtkCanvas cnv = canvas3d.get();
                final Color col = getDisplayColor();
                final double r = col.getRed() / 255d;
                final double g = col.getGreen() / 255d;
                final double b = col.getBlue() / 255d;
                // final float opacity = getOpacity();

                final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

                // we need to lock canvas as actor can be accessed during rendering
                if (vtkPanel != null)
                {
                    vtkPanel.lock();
                    try
                    {
                        actor.GetProperty().SetColor(r, g, b);
                    }
                    finally
                    {
                        vtkPanel.unlock();
                    }
                }
                else
                {
                    actor.GetProperty().SetColor(r, g, b);
                }

                // need to repaint
                painterChanged();
            }
        }
    }

    public static final String ID_POSITION = "position";

    private final Anchor3D position;

    /**
     * @deprecated
     */
    @Deprecated
    public ROI3DPoint(Point3D pt, boolean cm)
    {
        this(pt);
    }

    public ROI3DPoint(Point3D position)
    {
        super(new Line3D());

        this.position = createAnchor(position);
        this.position.setSelected(true);
        addPoint(this.position);

        // set icon
        setIcon(ResourceUtil.ICON_ROI_POINT);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI3DPoint(Point5D pt)
    {
        this(pt.toPoint3D());
    }

    public ROI3DPoint(double x, double y, double z)
    {
        this(new Point3D.Double(x, y, z));
    }

    public ROI3DPoint()
    {
        this(new Point3D.Double());
    }

    @Override
    public String getDefaultName()
    {
        return "Point3D";
    }

    @Override
    protected ROI3DShapePainter createPainter()
    {
        return new ROI3DPointPainter();
    }

    public Line3D getLine()
    {
        return (Line3D) shape;
    }

    public Point3D getPoint()
    {
        return position.getPosition();
    }

    @Override
    public boolean contains(ROI roi)
    {
        return false;
    }

    @Override
    public boolean intersects(ROI r)
    {
        // special case of ROI3DPoint
        if (r instanceof ROI3DPoint)
            return onSamePos(((ROI3DPoint) r), false) && ((ROI3DPoint) r).getPoint().equals(getPoint());

        return super.intersects(r);
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, boolean inclusive)
    {
        if ((width <= 0) || (height <= 0))
            return new boolean[0];

        final boolean[] result = new boolean[width * height];
        // 2D bounds
        final Rectangle bounds2d = new Rectangle(x, y, width, height);

        final Point3D pos = getPoint();

        // same Z ?
        if (Math.floor(pos.getZ()) == z)
        {
            // inside the mask ?
            if (bounds2d.contains(pos.toPoint2D()))
            {
                final int px = (int) Math.floor(pos.getX());
                final int py = (int) Math.floor(pos.getY());

                // set the pixel
                result[(px - x) + ((py - y) * width)] = true;
            }
        }

        return result;
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
                    ((ROI3DShapePainter) getOverlay()).needRebuild = true;
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
        final Point3D pt = getPoint();
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        getLine().setLine(x, y, z, x, y, z);

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
    protected boolean removePoint(IcyCanvas canvas, Anchor3D pt)
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
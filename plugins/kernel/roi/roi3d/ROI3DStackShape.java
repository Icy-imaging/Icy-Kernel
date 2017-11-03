/**
 * 
 */
package plugins.kernel.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.painter.VtkPainter;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.geom.PathIterator;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.roi.roi2d.ROI2DShape;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkInformation;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;

/**
 * Base class defining a generic 3D Shape ROI as a stack of individual 2D Shape ROI.
 * 
 * @author Stephane
 */
public abstract class ROI3DStackShape extends ROI3DStack<ROI2DShape>
{
    public ROI3DStackShape(Class<? extends ROI2DShape> roiClass)
    {
        super(roiClass);
    }

    @Override
    protected ROIPainter createPainter()
    {
        return new ROI3DStackShapePainter();
    }

    @Override
    public boolean isOverEdge(IcyCanvas canvas, double x, double y, double z)
    {
        final ROI2DShape slice = getSlice((int) z);

        if (slice != null)
            return slice.isOverEdge(canvas, x, y);

        return false;
    }

    public class ROI3DStackShapePainter extends ROI3DStackPainter implements VtkPainter, Runnable
    {
        // VTK 3D objects
        protected vtkPolyData outline;
        protected vtkPolyDataMapper outlineMapper;
        protected vtkActor outlineActor;
        protected vtkInformation vtkInfo;
        protected vtkCellArray vCells;
        protected vtkPoints vPoints;
        protected vtkPolyData polyData;
        protected vtkPolyDataMapper polyMapper;
        protected vtkActor actor;
        // 3D internal
        protected boolean needRebuild;
        protected double scaling[];
        protected WeakReference<VtkCanvas> canvas3d;

        public ROI3DStackShapePainter()
        {
            super();

            // don't create VTK object on constructor
            outline = null;
            outlineMapper = null;
            outlineActor = null;
            vtkInfo = null;
            vCells = null;
            vPoints = null;
            polyData = null;
            polyMapper = null;
            actor = null;

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            needRebuild = true;
            canvas3d = new WeakReference<VtkCanvas>(null);
        }

        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();

            // release allocated VTK resources
            if (actor != null)
                actor.Delete();
            if (polyMapper != null)
                polyMapper.Delete();
            if (polyData != null)
                polyData.Delete();
            if (vPoints != null)
                vPoints.Delete();
            if (vCells != null)
                vCells.Delete();
            if (outlineActor != null)
            {
                outlineActor.SetPropertyKeys(null);
                outlineActor.Delete();
            }
            if (vtkInfo != null)
            {
                vtkInfo.Remove(VtkCanvas.visibilityKey);
                vtkInfo.Delete();
            }
            if (outlineMapper != null)
                outlineMapper.Delete();
            if (outline != null)
            {
                outline.GetPointData().GetScalars().Delete();
                outline.GetPointData().Delete();
                outline.Delete();
            }
        };

        protected void initVtkObjects()
        {
            outline = VtkUtil.getOutline(0d, 1d, 0d, 1d, 0d, 1d);
            outlineMapper = new vtkPolyDataMapper();
            outlineActor = new vtkActor();
            outlineActor.SetMapper(outlineMapper);
            // disable picking on the outline
            outlineActor.SetPickable(0);
            // and set it to wireframe representation
            outlineActor.GetProperty().SetRepresentationToWireframe();
            // use vtkInformations to store outline visibility state (hacky)
            vtkInfo = new vtkInformation();
            vtkInfo.Set(VtkCanvas.visibilityKey, 0);
            // VtkCanvas use this to restore correctly outline visibility flag
            outlineActor.SetPropertyKeys(vtkInfo);

            // init poly data object
            polyData = new vtkPolyData();
            polyMapper = new vtkPolyDataMapper();
            polyMapper.SetInputData(polyData);
            actor = new vtkActor();
            actor.SetMapper(polyMapper);

            // initialize color and stroke
            final Color col = getColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;

            outlineActor.GetProperty().SetColor(r, g, b);
            final vtkProperty property = actor.GetProperty();
            property.SetPointSize(getStroke());
            property.SetColor(r, g, b);
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
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

            // get bounds
            final Rectangle3D bounds = getBounds3D();

            // update outline
            VtkUtil.setOutlineBounds(outline, bounds.getMinX() * scaling[0], bounds.getMaxX() * scaling[0],
                    bounds.getMinY() * scaling[1], bounds.getMaxY() * scaling[1], bounds.getMinZ() * scaling[2],
                    bounds.getMaxZ() * scaling[2], canvas);

            // update polydata object
            final List<double[]> point3DList = new ArrayList<double[]>();
            final List<int[]> polyList = new ArrayList<int[]>();
            final double[] coords = new double[6];

            // starting position
            double xm = 0d;
            double ym = 0d;
            double x0 = 0d;
            double y0 = 0d;
            double x1 = 0d;
            double y1 = 0d;
            double xs = scaling[0];
            double ys = scaling[1];
            int ind;

            for (double z = bounds.getMinZ(); z <= bounds.getMaxZ(); z += 1d)
            {
                // get ROI shape for this slice
                final ROI2DShape roi2dShape = getSlice((int) z);

                // no ROI here --> continue
                if (roi2dShape == null)
                    continue;

                final double z0 = (z + 0d) * scaling[2];
                final double z1 = (z + 1d) * scaling[2];

                // use flat path
                final PathIterator path = roi2dShape.getPathIterator(null, 0.5d);

                // build point data
                while (!path.isDone())
                {
                    switch (path.currentSegment(coords))
                    {
                        case PathIterator.SEG_MOVETO:
                            x0 = xm = coords[0] * xs;
                            y0 = ym = coords[1] * ys;
                            break;

                        case PathIterator.SEG_LINETO:
                            x1 = coords[0] * xs;
                            y1 = coords[1] * ys;

                            ind = point3DList.size();

                            point3DList.add(new double[] {x0, y0, z0});
                            point3DList.add(new double[] {x1, y1, z0});
                            point3DList.add(new double[] {x0, y0, z1});
                            point3DList.add(new double[] {x1, y1, z1});
                            polyList.add(new int[] {1 + ind, 2 + ind, 0 + ind});
                            polyList.add(new int[] {3 + ind, 2 + ind, 1 + ind});

                            x0 = x1;
                            y0 = y1;
                            break;

                        case PathIterator.SEG_CLOSE:
                            x1 = xm;
                            y1 = ym;

                            ind = point3DList.size();

                            point3DList.add(new double[] {x0, y0, z0});
                            point3DList.add(new double[] {x1, y1, z0});
                            point3DList.add(new double[] {x0, y0, z1});
                            point3DList.add(new double[] {x1, y1, z1});
                            polyList.add(new int[] {1 + ind, 2 + ind, 0 + ind});
                            polyList.add(new int[] {3 + ind, 2 + ind, 1 + ind});

                            x0 = x1;
                            y0 = y1;
                            break;
                    }

                    path.next();
                }
            }

            // convert to array
            final double[][] vertices = new double[point3DList.size()][3];
            final int[][] indexes = new int[polyList.size()][3];

            ind = 0;
            for (double[] pt3D : point3DList)
                vertices[ind++] = pt3D;

            ind = 0;
            for (int[] poly : polyList)
                indexes[ind++] = poly;

            final vtkCellArray previousCells = vCells;
            final vtkPoints previousPoints = vPoints;
            vCells = VtkUtil.getCells(polyList.size(), VtkUtil.prepareCells(indexes));
            vPoints = VtkUtil.getPoints(vertices);

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                // update outline polygon data
                outlineMapper.SetInputData(outline);
                outlineMapper.Update();
                // update polygon data from cell and points
                polyData.SetPolys(vCells);
                polyData.SetPoints(vPoints);
                polyMapper.Update();

                // release previous allocated VTK objects
                if (previousCells != null)
                    previousCells.Delete();
                if (previousPoints != null)
                    previousPoints.Delete();
            }
            finally
            {
                vtkPanel.unlock();
            }

            // update color and others properties
            updateVtkDisplayProperties();
        }

        protected void updateVtkDisplayProperties()
        {
            if (actor == null)
                return;

            final VtkCanvas cnv = canvas3d.get();
            final vtkProperty vtkProperty = actor.GetProperty();
            final Color col = getDisplayColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;
            final double strk = getStroke();
            // final float opacity = getOpacity();

            final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

            // we need to lock canvas as actor can be accessed during rendering
            if (vtkPanel != null)
                vtkPanel.lock();
            try
            {
                // set actors color
                outlineActor.GetProperty().SetColor(r, g, b);
                if (isSelected())
                {
                    outlineActor.GetProperty().SetRepresentationToWireframe();
                    outlineActor.SetVisibility(1);
                    vtkInfo.Set(VtkCanvas.visibilityKey, 1);
                }
                else
                {
                    outlineActor.GetProperty().SetRepresentationToPoints();
                    outlineActor.SetVisibility(0);
                    vtkInfo.Set(VtkCanvas.visibilityKey, 0);
                }
                vtkProperty.SetColor(r, g, b);
                vtkProperty.SetPointSize(strk);
                // opacity here is about ROI content, global opacity is handled by Layer
                // vtkProperty.SetOpacity(opacity);
                setVtkObjectsColor(col);
            }
            finally
            {
                if (vtkPanel != null)
                    vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }

        protected void setVtkObjectsColor(Color color)
        {
            if (outline != null)
                VtkUtil.setPolyDataColor(outline, color, canvas3d.get());
            if (polyData != null)
                VtkUtil.setPolyDataColor(polyData, color, canvas3d.get());
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                if (canvas instanceof VtkCanvas)
                {
                    // 3D canvas
                    final VtkCanvas cnv = (VtkCanvas) canvas;
                    // update reference if needed
                    if (canvas3d.get() != cnv)
                        canvas3d = new WeakReference<VtkCanvas>(cnv);

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
                        ThreadUtil.runSingle(this);
                        needRebuild = false;
                    }
                }
                else
                    super.paint(g, sequence, canvas);
            }
        }

        @Override
        protected boolean updateFocus(InputEvent e, Point5D imagePoint, IcyCanvas canvas)
        {
            // specific VTK canvas processing
            if (canvas instanceof VtkCanvas)
            {
                // mouse is over the ROI actor ? --> focus the ROI
                final boolean focused = (actor != null) && (actor == ((VtkCanvas) canvas).getPickedObject());

                setFocused(focused);

                return focused;
            }

            return super.updateFocus(e, imagePoint, canvas);
        }

        @Override
        public vtkProp[] getProps()
        {
            // initialize VTK objects if not yet done
            if (actor == null)
                initVtkObjects();

            return new vtkActor[] {actor, outlineActor};
        }

        @Override
        public void run()
        {
            rebuildVtkObjects();
        }
    }
}

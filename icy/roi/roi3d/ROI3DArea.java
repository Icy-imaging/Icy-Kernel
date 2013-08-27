package icy.roi.roi3d;

import icy.canvas.Canvas3D;
import icy.main.Icy;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.roi2d.ROI2DArea;
import icy.sequence.Sequence;
import icy.type.point.Point3D;
import icy.vtk.VtkUtil;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkStructuredGrid;
import vtk.vtkStructuredGridOutlineFilter;

public class ROI3DArea extends ROI3DStack<ROI3DAreaSlice>
{
    protected ROI3DArea()
    {
        super(ROI3DAreaSlice.class);

        setName("3D area");
    }

    public ROI3DArea(int x, int y, int z)
    {
        this();
        addPoint(x, y, z);
    }

    public ROI3DArea(Point2D pt)
    {
        this((int) pt.getX(), (int) pt.getY(), 0);
    }

    /**
     * Adds the specified point to this ROI
     * 
     * @param x
     * @param y
     * @param z
     */
    public void addPoint(int x, int y, int z)
    {
        getSlice(z, true).addPoint(x, y);
    }

    /**
     * Sets the ROI slice at given Z position to this 3D ROI
     * 
     * @param z
     *        the position where the slice must be set
     * @param roiSlice
     *        the 2D ROI to set
     * @param merge
     *        <code>true</code> if the given slice should be merged with the existing slice, or
     *        <code>false</code> to replace the existing slice.
     */
    public void setSlice(int z, ROI2D roiSlice, boolean merge)
    {
        if (roiSlice == null)
            throw new IllegalArgumentException("Cannot add an empty slice in a 3D ROI");

        final ROI2DArea currentSlice = getSlice(z, false);
        final ROI newSlice;

        // merge both slice
        if ((currentSlice != null) && merge)
        {
            // we need to modify the Z position so we do the merge for same Z
            roiSlice.setZ(z);
            // do ROI union
            newSlice = currentSlice.getUnion(roiSlice);
        }
        else
            newSlice = roiSlice;

        if (newSlice instanceof ROI2DArea)
            setSlice(z, new ROI3DAreaSlice(this, (ROI2DArea) newSlice));
        else if (newSlice instanceof ROI2D)
            setSlice(z, new ROI3DAreaSlice(this, ((ROI2D) newSlice).getBooleanMask(true)));
        else
            throw new IllegalArgumentException("Can't add the result of the merge operation on slice " + z + ": "
                    + newSlice.getClassName());
    }

    @Override
    protected ROIPainter createPainter()
    {
        return new ROI3DAreaPainter();
    }

    public Point3D[] getEdgePoints()
    {
        ArrayList<Point3D> points = new ArrayList<Point3D>();

        for (ROI2DArea slice : this)
        {
            for (Point pt : slice.getBooleanMask(true).getEdgePoints())
                points.add(new Point3D.Integer(pt.x, pt.y, slice.getZ()));
        }

        return points.toArray(new Point3D[points.size()]);
    }

    public Point3D[] getPoints()
    {
        ArrayList<Point3D> points = new ArrayList<Point3D>();

        for (ROI2DArea slice : this)
        {
            for (Point pt : slice.getBooleanMask().getPoints())
                points.add(new Point3D.Integer(pt.x, pt.y, slice.getZ()));
        }

        return points.toArray(new Point3D[points.size()]);
    }

    public class ROI3DAreaPainter extends ROI3DStackPainter
    {
        // VTK 3D objects, we use Object to prevent UnsatisfiedLinkError
        final Object grid;
        final Object gridOutline;
        // final Object polyData;
        final Object polyMapper;
        final Object actor;
        // 3D internal
        boolean needRebuild;
        double scaling[];

        public ROI3DAreaPainter()
        {
            // avoid to use vtk objects when vtk library is missing.
            if (Icy.isVtkLibraryLoaded())
            {
                // init 3D painters stuff
                // polyData = new vtkPolyData();
                grid = new vtkStructuredGrid();
                gridOutline = new vtkStructuredGridOutlineFilter();

                ((vtkStructuredGridOutlineFilter) gridOutline).SetInput((vtkStructuredGrid) grid);

                polyMapper = new vtkPolyDataMapper();
                // ((vtkPolyDataMapper) polyMapper).SetInput((vtkPolyData) polyData);
                ((vtkPolyDataMapper) polyMapper).SetInput(((vtkStructuredGridOutlineFilter) gridOutline).GetOutput());

                actor = new vtkActor();
                ((vtkActor) actor).SetMapper((vtkPolyDataMapper) polyMapper);
            }
            else
            {
                // polyData = null;
                grid = null;
                gridOutline = null;
                polyMapper = null;
                actor = null;
            }

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            needRebuild = true;
        }

        /**
         * update 3D painter for 3D canvas (called only when vtk is loaded).
         */
        protected void rebuild3DPainter(Canvas3D canvas)
        {
            final Sequence seq = canvas.getSequence();

            // nothing to update
            if (seq == null)
                return;

            int width = seq.getSizeX();
            int height = seq.getSizeY();

            ArrayList<double[]> verticesArray = new ArrayList<double[]>(0);

            for (ROI2DArea area : ROI3DArea.this)
            {
                int k = area.getZ();

                verticesArray.ensureCapacity(verticesArray.size() + width * height);

                boolean[] mask = area.getBooleanMask(0, 0, width, height, true);
                int offset = 0;
                for (int j = 0; j < height; j++)
                    for (int i = 0; i < width; i++, offset++)
                    {
                        if (mask[offset])
                            verticesArray.add(new double[] {i, j, k});
                    }
            }

            double[][] points = new double[verticesArray.size()][3];
            verticesArray.toArray(points);

            ((vtkStructuredGrid) grid).SetDimensions(width, height, getSizeZ());
            ((vtkStructuredGrid) grid).SetPoints(VtkUtil.getPoints(points));

            ((vtkStructuredGridOutlineFilter) gridOutline).Update();
            ((vtkPolyDataMapper) polyMapper).Update();
        }

        @Override
        public vtkProp[] getProps()
        {
            return new vtkActor[] {(vtkActor) actor};
        }
    }

    /**
     * Translate all 2D slices by the specified delta X <code>dx</code> and Delta Y <code>dy</code>
     */
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            for (ROI3DAreaSlice slice : slices.values())
                slice.translate(dx, dy);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Set all 2D slices ROI to same position.<br>
     */
    public void setPosition(Point2D newPosition)
    {
        beginUpdate();
        try
        {
            for (ROI3DAreaSlice slice : slices.values())
                slice.setPosition(newPosition);
        }
        finally
        {
            endUpdate();
        }
    }
}

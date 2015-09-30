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
package plugins.kernel.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.common.EventHierarchicalChecker;
import icy.painter.VtkPainter;
import icy.roi.BooleanMask2D;
import icy.roi.BooleanMask3D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIEvent;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.vtk.VtkUtil;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.roi.roi2d.ROI2DArea;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkStructuredGrid;
import vtk.vtkStructuredGridOutlineFilter;

/**
 * 3D Area ROI.
 * 
 * @author Stephane
 */
public class ROI3DArea extends ROI3DStack<ROI2DArea>
{
    public class ROI3DAreaPainter extends ROI3DStackPainter implements VtkPainter, Runnable
    {
        // VTK 3D objects, we use Object to prevent UnsatisfiedLinkError
        Object grid;
        Object gridOutline;
        // final Object polyData;
        Object polyMapper;
        Object actor;
        // 3D internal
        boolean needRebuild;
        double scaling[];
        VtkCanvas canvas3d;

        public ROI3DAreaPainter()
        {
            // polyData = null;
            grid = null;
            gridOutline = null;
            polyMapper = null;
            actor = null;

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            needRebuild = true;
            canvas3d = null;
        }

        protected void initVtkObjects()
        {
            // init 3D painters stuff
            // polyData = new vtkPolyData();
            grid = new vtkStructuredGrid();
            gridOutline = new vtkStructuredGridOutlineFilter();

            ((vtkStructuredGridOutlineFilter) gridOutline).SetInputData((vtkStructuredGrid) grid);

            polyMapper = new vtkPolyDataMapper();
            // ((vtkPolyDataMapper) polyMapper).SetInput((vtkPolyData) polyData);
            ((vtkPolyDataMapper) polyMapper).SetInputConnection(((vtkStructuredGridOutlineFilter) gridOutline)
                    .GetOutputPort());

            actor = new vtkActor();
            ((vtkActor) actor).SetMapper((vtkPolyDataMapper) polyMapper);
        }

        /**
         * rebuild VTK objects (called only when VTK canvas is selected).
         */
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

            int width = seq.getSizeX();
            int height = seq.getSizeY();

            final ArrayList<double[]> verticesArray = new ArrayList<double[]>();

            for (ROI2DArea area : ROI3DArea.this)
            {
                int k = area.getZ();

                verticesArray.ensureCapacity(verticesArray.size() + width * height);

                boolean[] mask = area.getBooleanMask(0, 0, width, height, true);

                int offset = 0;
                for (int j = 0; j < height; j++)
                {
                    for (int i = 0; i < width; i++, offset++)
                    {
                        if (mask[offset])
                            verticesArray.add(new double[] {i, j, k});
                    }
                }
            }

            double[][] points = new double[verticesArray.size()][3];
            verticesArray.toArray(points);

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            canvas3d.lock();
            try
            {
                ((vtkStructuredGrid) grid).SetDimensions(width, height, getSizeZ());
                ((vtkStructuredGrid) grid).SetPoints(VtkUtil.getPoints(points));

                ((vtkStructuredGridOutlineFilter) gridOutline).Update();
                ((vtkPolyDataMapper) polyMapper).Update();
            }
            finally
            {
                canvas3d.unlock();
            }

            // no more pending request
            if (!ThreadUtil.hasWaitingSingleTask(this))
                canvas3d = null;
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
                }
                else
                    super.paint(g, sequence, canvas);
            }
        }

        @Override
        public vtkProp[] getProps()
        {
            return new vtkActor[] {(vtkActor) actor};
        }

        @Override
        public void run()
        {
            rebuildVtkObjects();
        }
    }

    public ROI3DArea()
    {
        super(ROI2DArea.class);

        setName("3D area");
    }

    public ROI3DArea(Point3D pt)
    {
        this();

        addBrush(pt.toPoint2D(), (int) pt.getZ());
    }

    public ROI3DArea(Point5D pt)
    {
        this(pt.toPoint3D());
    }

    /**
     * Create a 3D Area ROI type from the specified {@link BooleanMask3D}.
     */
    public ROI3DArea(BooleanMask3D mask)
    {
        this();

        setAsBooleanMask(mask);
    }

    /**
     * Create a copy of the specified 3D Area ROI.
     */
    public ROI3DArea(ROI3DArea area)
    {
        this();

        // copy the source 3D area ROI
        for (Entry<Integer, ROI2DArea> entry : area.slices.entrySet())
            slices.put(entry.getKey(), new ROI2DArea(entry.getValue()));
    }

    @Override
    protected ROIPainter createPainter()
    {
        return new ROI3DAreaPainter();
    }

    /**
     * Adds the specified point to this ROI
     */
    public void addPoint(int x, int y, int z)
    {
        setPoint(x, y, z, true);
    }

    /**
     * Remove a point from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removePoint(int x, int y, int z)
    {
        setPoint(x, y, z, false);
    }

    /**
     * Set the value for the specified point in the mask.
     * Don't forget to call optimizeBounds() after consecutive remove point operation
     * to refresh the mask bounds.
     */
    public void setPoint(int x, int y, int z, boolean value)
    {
        getSlice(z, true).setPoint(x, y, value);
    }

    /**
     * Add brush point at specified position and for specified Z slice.
     */
    public void addBrush(Point2D pos, int z)
    {
        getSlice(z, true).addBrush(pos);
    }

    /**
     * Remove brush point from the mask at specified position and for specified Z slice.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removeBrush(Point2D pos, int z)
    {
        getSlice(z, true).removeBrush(pos);
    }

    /**
     * Sets the ROI slice at given Z position to this 3D ROI
     * 
     * @param z
     *        the position where the slice must be set
     * @param roiSlice
     *        the 2D ROI to set
     * @param merge
     *        <code>true</code> if the given slice should be merged with the existing slice, or <code>false</code> to
     *        replace the existing slice.
     */
    public void setSlice(int z, ROI2D roiSlice, boolean merge)
    {
        if (roiSlice == null)
            throw new IllegalArgumentException("Cannot add an empty slice in a 3D ROI");

        final ROI2DArea currentSlice = getSlice(z);
        final ROI newSlice;

        // merge both slice
        if ((currentSlice != null) && merge)
        {
            // we need to modify the Z, T and C position so we do the merge correctly
            roiSlice.setZ(z);
            roiSlice.setT(getT());
            roiSlice.setC(getC());
            // do ROI union
            newSlice = currentSlice.getUnion(roiSlice);
        }
        else
            newSlice = roiSlice;

        if (newSlice instanceof ROI2DArea)
            setSlice(z, (ROI2DArea) newSlice);
        else if (newSlice instanceof ROI2D)
            setSlice(z, new ROI2DArea(((ROI2D) newSlice).getBooleanMask(true)));
        else
            throw new IllegalArgumentException("Can't add the result of the merge operation on 2D slice " + z + ": "
                    + newSlice.getClassName());
    }

    /**
     * Returns true if the ROI is empty (the mask does not contains any point).
     */
    @Override
    public boolean isEmpty()
    {
        for (ROI2DArea area : slices.values())
            if (!area.isEmpty())
                return false;

        return true;
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} and {@link BooleanMask3D#getContourPoints()} instead.
     */
    @Deprecated
    public Point3D[] getEdgePoints()
    {
        return getBooleanMask(true).getContourPoints();
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} and {@link BooleanMask3D#getPoints()} instead.
     */
    @Deprecated
    public Point3D[] getPoints()
    {
        return getBooleanMask(true).getPoints();
    }

    /**
     * @deprecated Use {@link #translate(double, double, double)} instead.
     */
    @Deprecated
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            for (ROI2DArea slice : slices.values())
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
    public void setPosition2D(Point2D newPosition)
    {
        beginUpdate();
        try
        {
            for (ROI2DArea slice : slices.values())
                slice.setPosition2D(newPosition);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Set the mask from a BooleanMask3D object
     */
    public void setAsBooleanMask(BooleanMask3D mask)
    {
        if (mask != null)
        {
            final Collection<BooleanMask2D> values = mask.mask.values();
            setAsBooleanMask(mask.bounds, values.toArray(new BooleanMask2D[values.size()]));
        }
    }

    /**
     * Set the 3D mask from a 2D boolean mask array
     * 
     * @param rect
     *        the 3D region defined by 2D boolean mask array
     * @param mask
     *        the 3D mask data (array length should be equals to rect.sizeZ)
     */
    public void setAsBooleanMask(Rectangle3D.Integer rect, BooleanMask2D[] mask)
    {
        if (rect.isInfiniteZ())
            throw new IllegalArgumentException("Cannot set infinite Z dimension on the 3D Area ROI.");

        beginUpdate();
        try
        {
            clear();

            for (int z = 0; z < rect.sizeZ; z++)
                setSlice(z + rect.z, new ROI2DArea(mask[z]));

            optimizeBounds();
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask.<br>
     * You should call it after consecutive remove operations.
     */
    public void optimizeBounds()
    {
        final Rectangle3D.Integer bounds = getBounds();

        beginUpdate();
        try
        {
            for (int z = bounds.z; z < bounds.z + bounds.sizeZ; z++)
            {
                final ROI2DArea roi = getSlice(z);

                if (roi.isEmpty())
                    removeSlice(z);
                else
                {
                    if (roi.optimizeBounds())
                        roi.roiChanged();
                }
            }
        }
        finally
        {
            endUpdate();
        }
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
                // the painter need to be rebuild
                ((ROI3DAreaPainter) painter).needRebuild = true;
                break;
        }

        super.onChanged(object);
    }
}

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
package icy.vtk;

import icy.canvas.Layer;
import icy.common.exception.TooLargeArrayException;
import icy.image.colormap.IcyColorMap;
import icy.image.lut.LUT.LUTChannel;
import icy.math.Scaler;
import icy.painter.Overlay;
import icy.painter.VtkPainter;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;
import icy.type.rectangle.Rectangle5D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkActor2DCollection;
import vtk.vtkActorCollection;
import vtk.vtkCellArray;
import vtk.vtkCollection;
import vtk.vtkColorTransferFunction;
import vtk.vtkContourFilter;
import vtk.vtkDataArray;
import vtk.vtkDoubleArray;
import vtk.vtkFloatArray;
import vtk.vtkIdTypeArray;
import vtk.vtkImageConstantPad;
import vtk.vtkImageData;
import vtk.vtkIntArray;
import vtk.vtkLongArray;
import vtk.vtkObject;
import vtk.vtkPiecewiseFunction;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderer;
import vtk.vtkShortArray;
import vtk.vtkUnsignedCharArray;
import vtk.vtkUnsignedIntArray;
import vtk.vtkUnsignedLongArray;
import vtk.vtkUnsignedShortArray;

/**
 * @author Stephane
 */
public class VtkUtil
{
    // VTK type
    public final static int VTK_VOID = 0;
    public final static int VTK_BIT = 1;
    public final static int VTK_CHAR = 2;
    public final static int VTK_SIGNED_CHAR = 15;
    public final static int VTK_UNSIGNED_CHAR = 3;
    public final static int VTK_SHORT = 4;
    public final static int VTK_UNSIGNED_SHORT = 5;
    public final static int VTK_INT = 6;
    public final static int VTK_UNSIGNED_INT = 7;
    public final static int VTK_LONG = 8;
    public final static int VTK_UNSIGNED_LONG = 9;
    public final static int VTK_FLOAT = 10;
    public final static int VTK_DOUBLE = 11;
    public final static int VTK_ID = 12;

    // VTK interpolation
    public final static int VTK_NEAREST_INTERPOLATION = 0;
    public final static int VTK_LINEAR_INTERPOLATION = 1;
    public final static int VTK_CUBIC_INTERPOLATION = 2;

    // VTK bounding box
    public final static int VTK_FLY_OUTER_EDGES = 0;
    public final static int VTK_FLY_CLOSEST_TRIAD = 1;
    public final static int VTK_FLY_FURTHEST_TRIAD = 2;
    public final static int VTK_FLY_STATIC_TRIAD = 3;
    public final static int VTK_FLY_STATIC_EDGES = 4;

    public final static int VTK_TICKS_INSIDE = 0;
    public final static int VTK_TICKS_OUTSIDE = 1;
    public final static int VTK_TICKS_BOTH = 2;

    public final static int VTK_GRID_LINES_ALL = 0;
    public final static int VTK_GRID_LINES_CLOSEST = 1;
    public final static int VTK_GRID_LINES_FURTHEST = 2;

    /**
     * Returns the VTK type corresponding to the specified DataType
     */
    public static int getVtkType(DataType type)
    {
        switch (type)
        {
            default:
            case UBYTE:
            case BYTE:
                return VTK_UNSIGNED_CHAR;

            // FIXME: signed char not supported by VTK java wrapper ??
            // case BYTE:
            // return VTK_CHAR;
            // return VTK_SIGNED_CHAR;

            case USHORT:
                return VTK_UNSIGNED_SHORT;
            case SHORT:
                return VTK_SHORT;
            case UINT:
                return VTK_UNSIGNED_INT;
            case INT:
                return VTK_INT;
            case ULONG:
                return VTK_UNSIGNED_LONG;
            case LONG:
                return VTK_LONG;
            case FLOAT:
                return VTK_FLOAT;
            case DOUBLE:
                return VTK_DOUBLE;
        }
    }

    public static vtkDataArray getVtkArray(Object array, boolean signed)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return getUCharArray((byte[]) array);
            case SHORT:
                if (signed)
                    return getUShortArray((short[]) array);
                return getShortArray((short[]) array);
            case INT:
                if (signed)
                    return getUIntArray((int[]) array);
                return getIntArray((int[]) array);
            case LONG:
                if (signed)
                    return getULongArray((long[]) array);
                return getLongArray((long[]) array);
            case FLOAT:
                return getFloatArray((float[]) array);
            case DOUBLE:
                return getDoubleArray((double[]) array);
            default:
                return null;
        }
    }

    public static vtkUnsignedCharArray getUCharArray(byte[] array)
    {
        final vtkUnsignedCharArray result = new vtkUnsignedCharArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkUnsignedShortArray getUShortArray(short[] array)
    {
        final vtkUnsignedShortArray result = new vtkUnsignedShortArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkUnsignedIntArray getUIntArray(int[] array)
    {
        final vtkUnsignedIntArray result = new vtkUnsignedIntArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkUnsignedLongArray getULongArray(long[] array)
    {
        final vtkUnsignedLongArray result = new vtkUnsignedLongArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkShortArray getShortArray(short[] array)
    {
        final vtkShortArray result = new vtkShortArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkIntArray getIntArray(int[] array)
    {
        final vtkIntArray result = new vtkIntArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkLongArray getLongArray(long[] array)
    {
        final vtkLongArray result = new vtkLongArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkFloatArray getFloatArray(float[] array)
    {
        final vtkFloatArray result = new vtkFloatArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkDoubleArray getDoubleArray(double[] array)
    {
        final vtkDoubleArray result = new vtkDoubleArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkIdTypeArray getIdTypeArray(int[] array)
    {
        final vtkIdTypeArray result = new vtkIdTypeArray();
        final vtkIntArray iarray = getIntArray(array);

        result.DeepCopy(iarray);

        return result;
    }

    public static int[] getArray(vtkIdTypeArray array)
    {
        final vtkIntArray iarray = new vtkIntArray();

        iarray.DeepCopy(array);

        return iarray.GetJavaArray();
    }

    /**
     * Transforms a vtkCollection to an array
     */
    public static vtkObject[] vtkCollectionToArray(vtkCollection collection)
    {
        final vtkObject[] result = new vtkObject[collection.GetNumberOfItems()];

        collection.InitTraversal();
        for (int i = 0; i < result.length; i++)
            result[i] = collection.GetNextItemAsObject();

        return result;
    }

    /**
     * Get vtkPoints from double[]
     */
    public static vtkPoints getPoints(double[] points)
    {
        final vtkPoints result = new vtkPoints();
        final vtkDoubleArray array = getDoubleArray(points);

        array.SetNumberOfComponents(3);
        result.SetData(array);

        return result;
    }

    /**
     * Get vtkPoints from double[][3]
     */
    public static vtkPoints getPoints(double[][] points)
    {
        return getPoints(Array2DUtil.toDoubleArray1D(points));
    }

    /**
     * Get vtkPoints from float[]
     */
    public static vtkPoints getPoints(float[] points)
    {
        final vtkPoints result = new vtkPoints();
        final vtkFloatArray array = getFloatArray(points);

        array.SetNumberOfComponents(3);
        result.SetData(array);

        return result;
    }

    /**
     * Get vtkPoints from float[][3]
     */
    public static vtkPoints getPoints(float[][] points)
    {
        return getPoints(Array2DUtil.toFloatArray1D(points));
    }

    /**
     * Get vtkPoints from int[]
     */
    public static vtkPoints getPoints(int[] points)
    {
        final vtkPoints result = new vtkPoints();
        final vtkIntArray array = getIntArray(points);

        array.SetNumberOfComponents(3);
        result.SetData(array);

        return result;
    }

    /**
     * Get vtkPoints from int[][3]
     */
    public static vtkPoints getPoints(int[][] points)
    {
        return getPoints(Array2DUtil.toIntArray1D(points));
    }

    /**
     * Get vtkCellArray from a 1D prepared cells array ( {n, i1, i2, ..., n, i1, i2,...} )
     */
    public static vtkCellArray getCells(int numCell, int[] cells)
    {
        final vtkCellArray result = new vtkCellArray();

        result.SetCells(numCell, getIdTypeArray(cells));

        return result;
    }

    /**
     * Returns the <i>vtkProp</i> from the specified <i>Layer</i> object.<br>
     * Returns a 0 sized array if the specified layer is <code>null</code> or does not contains any vtkProp.
     */
    public static vtkProp[] getLayerProps(Layer layer)
    {
        if (layer != null)
        {
            // add painter actor from the vtk render
            final Overlay overlay = layer.getOverlay();

            if (overlay instanceof VtkPainter)
                return ((VtkPainter) overlay).getProps();
        }

        return new vtkProp[0];
    }

    /**
     * Returns all <i>vtkProp</i> from the specified list of <i>Layer</i> object.<br>
     * Returns a 0 sized array if specified layers does not contains any vtkProp.
     */
    public static vtkProp[] getLayersProps(List<Layer> layers)
    {
        final List<vtkProp[]> layersProps = new ArrayList<vtkProp[]>();
        int totalSize = 0;

        for (Layer layer : layers)
        {
            if (layer != null)
            {
                // add painter actor from the vtk render
                final Overlay overlay = layer.getOverlay();

                if (overlay instanceof VtkPainter)
                {
                    final vtkProp[] props = ((VtkPainter) overlay).getProps();

                    if (props.length > 0)
                    {
                        layersProps.add(props);
                        totalSize += props.length;
                    }
                }
            }
        }

        final vtkProp[] result = new vtkProp[totalSize];
        int ind = 0;

        for (vtkProp[] props : layersProps)
        {
            final int size = props.length;

            System.arraycopy(props, 0, result, ind, size);
            ind += size;
        }

        return result;
    }

    /**
     * Return all actor / view prop from the specified renderer
     */
    public static vtkProp[] getProps(vtkRenderer renderer)
    {
        if (renderer == null)
            return new vtkProp[0];

        final vtkPropCollection collection = renderer.GetViewProps();
        final vtkProp[] result = new vtkProp[collection.GetNumberOfItems()];

        collection.InitTraversal();
        for (int i = 0; i < result.length; i++)
            result[i] = collection.GetNextProp();

        return result;
    }

    /**
     * Return true if the renderer contains the specified actor / view prop
     */
    public static boolean hasProp(vtkRenderer renderer, vtkProp actor)
    {
        if ((renderer == null) || (actor == null))
            return false;

        return renderer.HasViewProp(actor) != 0;
    }

    /**
     * @deprecated Use {@link #hasProp(vtkRenderer, vtkProp)} instead.
     */
    @Deprecated
    public static boolean findProp(vtkRenderer renderer, vtkProp actor)
    {
        return hasProp(renderer, actor);
    }

    /**
     * @deprecated Use {@link #hasProp(vtkRenderer, vtkProp)} instead.
     */
    @Deprecated
    public static boolean findActor(vtkRenderer renderer, vtkActor actor)
    {
        if ((renderer == null) || (actor == null))
            return false;

        final vtkActorCollection actors = renderer.GetActors();

        actors.InitTraversal();
        for (int i = 0; i < actors.GetNumberOfItems(); i++)
        {
            final vtkActor curActor = actors.GetNextActor();

            // already present --> exit
            if (curActor == actor)
                return true;

            // // search in sub actor
            // if (findActor(curActor, actor))
            // return true;
        }

        return false;
    }

    /**
     * @deprecated Use {@link #hasProp(vtkRenderer, vtkProp)} instead.
     */
    @Deprecated
    public static boolean findActor2D(vtkRenderer renderer, vtkActor2D actor)
    {
        if ((renderer == null) || (actor == null))
            return false;

        final vtkActor2DCollection actors = renderer.GetActors2D();

        actors.InitTraversal();
        for (int i = 0; i < actors.GetNumberOfItems(); i++)
        {
            final vtkActor2D curActor = actors.GetNextActor2D();

            // already present --> exit
            if (curActor == actor)
                return true;

            // // search in sub actor
            // if (findActor2D(curActor, actor))
            // return true;
        }

        return false;
    }

    /**
     * Add an actor (vtkProp) to the specified renderer.<br>
     * If the actor is already existing in the renderer then no operation is done.
     */
    public static void addProp(vtkRenderer renderer, vtkProp prop)
    {
        if ((renderer == null) || (prop == null))
            return;

        // actor not yet present in renderer ? --> add it
        if (renderer.HasViewProp(prop) == 0)
            renderer.AddViewProp(prop);
    }

    /**
     * @deprecated Use {@link #addProp(vtkRenderer, vtkProp)} instead.
     */
    @Deprecated
    public static void addActor(vtkRenderer renderer, vtkActor actor)
    {
        if ((renderer == null) || (actor == null))
            return;

        // actor not yet present in renderer ? --> add it
        if (!VtkUtil.findActor(renderer, actor))
            renderer.AddActor(actor);
    }

    /**
     * @deprecated Use {@link #addProp(vtkRenderer, vtkProp)} instead.
     */
    @Deprecated
    public static void addActor2D(vtkRenderer renderer, vtkActor2D actor)
    {
        if ((renderer == null) || (actor == null))
            return;

        // actor not yet present in renderer ? --> add it
        if (!VtkUtil.findActor2D(renderer, actor))
            renderer.AddActor2D(actor);
    }

    /**
     * Add an array of actor (vtkProp) to the specified renderer.<br>
     * If an actor is already existing in the renderer then nothing is done for this actor.
     */
    public static void addProps(vtkRenderer renderer, vtkProp[] props)
    {
        if ((renderer == null) || (props == null))
            return;

        for (vtkProp prop : props)
        {
            // actor not yet present in renderer ? --> add it
            if (renderer.HasViewProp(prop) == 0)
                renderer.AddViewProp(prop);
        }
    }

    /**
     * Remove an actor from the specified renderer.
     */
    public static void removeProp(vtkRenderer renderer, vtkProp actor)
    {
        renderer.RemoveViewProp(actor);
    }

    /**
     * Return a 1D cells array from a 2D indexes array
     */
    public static int[] prepareCells(int[][] indexes)
    {
        final int len = indexes.length;

        int total_len = 0;
        for (int i = 0; i < len; i++)
            total_len += indexes[i].length + 1;

        final int[] result = new int[total_len];

        int offset = 0;
        for (int i = 0; i < len; i++)
        {
            final int[] s_cells = indexes[i];
            final int s_len = s_cells.length;

            result[offset++] = s_len;
            for (int j = 0; j < s_len; j++)
                result[offset++] = s_cells[j];
        }

        return result;
    }

    /**
     * Return a 1D cells array from a 1D indexes array and num vertex per cell (polygon)
     */
    public static int[] prepareCells(int numVertexPerCell, int[] indexes)
    {
        final int num_cells = indexes.length / numVertexPerCell;
        final int[] result = new int[num_cells * (numVertexPerCell + 1)];

        int off_dst = 0;
        int off_src = 0;
        for (int i = 0; i < num_cells; i++)
        {
            result[off_dst++] = numVertexPerCell;

            for (int j = 0; j < numVertexPerCell; j++)
                result[off_dst++] = indexes[off_src + j];

            off_src += numVertexPerCell;
        }

        return result;
    }

    /**
     * Creates and returns a 3D binary (0/1 values) {@link vtkImageData} object corresponding to the ROI 3D boolean mask
     * (C dimension is not considered) at specified T position.
     * 
     * @param roi
     *        the roi we want to retrieve the vtkImageData mask
     * @param sz
     *        the Z size to use for ROI with infinite Z dimension (if ROI has a finite Z dimension then ROI Z size is
     *        used).
     * @param t
     *        the T position we want to retrieve the 3D mask data
     */
    public static vtkImageData getBinaryImageData(ROI roi, int sz, int t)
    {
        final vtkImageData result;

        final Rectangle5D bounds5d = roi.getBounds5D();
        final int sizeX;
        final int sizeY;
        final int sizeZ;
        final int x;
        final int y;
        final int z;
        final int c;

        x = (int) bounds5d.getX();
        y = (int) bounds5d.getY();
        sizeX = (int) (bounds5d.getMaxX() - x);
        sizeY = (int) (bounds5d.getMaxY() - y);
        if (bounds5d.isInfiniteZ())
        {
            z = 0;
            sizeZ = sz;
        }
        else
        {
            z = (int) bounds5d.getZ();
            sizeZ = (int) (bounds5d.getMaxZ() - z);
        }
        if (bounds5d.isInfiniteC())
            c = 0;
        else
            c = (int) bounds5d.getC();

        long totalSize = sizeX;
        totalSize *= sizeY;
        totalSize *= sizeZ;

        if (totalSize > Integer.MAX_VALUE)
            throw new RuntimeException("Can't allocate array (size > 2^31)");

        // build java array
        final int sizeXY = sizeX * sizeY;
        final byte[] array = new byte[(int) totalSize];
        int offset = 0;

        if (bounds5d.isInfiniteZ())
        {
            final boolean[] mask = roi.getBooleanMask2D(x, y, sizeX, sizeY, 0, t, c, true);

            for (int curZ = z; curZ < (z + sizeZ); curZ++)
            {
                for (int i = 0; i < sizeXY; i++)
                    array[offset++] = mask[i] ? (byte) 1 : (byte) 0;
            }
        }
        else
        {
            for (int curZ = z; curZ < (z + sizeZ); curZ++)
            {
                final boolean[] mask = roi.getBooleanMask2D(x, y, sizeX, sizeY, curZ, t, c, true);

                for (int i = 0; i < sizeXY; i++)
                    array[offset++] = mask[i] ? (byte) 1 : (byte) 0;
            }
        }

        // create a new image data structure
        result = new vtkImageData();
        result.SetDimensions(sizeX, sizeY, sizeZ);
        result.SetExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);
        // pre-allocate data
        result.AllocateScalars(VTK_UNSIGNED_CHAR, 1);
        // set data
        ((vtkUnsignedCharArray) result.GetPointData().GetScalars()).SetJavaArray(array);

        return result;
    }

    /**
     * Build and return volume image data from given Sequence object.
     * 
     * @param sequence
     *        the sequence object we want to get volume image data
     * @param posT
     *        frame index
     * @param posC
     *        channel index (-1 for all channel)
     */
    public static vtkImageData getImageData(Sequence sequence, int posT, int posC)
            throws TooLargeArrayException, OutOfMemoryError
    {
        if ((sequence == null) || sequence.isEmpty())
            return null;

        final Object data;
        final vtkImageData result;

        if (posC == -1)
        {
            data = sequence.getDataCopyCXYZ(posT);
            result = VtkUtil.getImageData(data, sequence.getDataType_(), sequence.getSizeX(), sequence.getSizeY(),
                    sequence.getSizeZ(), sequence.getSizeC());
        }
        else
        {
            data = sequence.getDataCopyXYZ(posT, posC);
            result = VtkUtil.getImageData(data, sequence.getDataType_(), sequence.getSizeX(), sequence.getSizeY(),
                    sequence.getSizeZ(), 1);
        }

        return result;
    }

    /**
     * Creates and returns a {@link vtkImageData} object from the specified 1D array data.
     */
    public static vtkImageData getImageData(Object data, DataType dataType, int sizeX, int sizeY, int sizeZ, int sizeC)
    {
        final vtkImageData result;
        final vtkDataArray array;

        // create a new image data structure
        result = new vtkImageData();
        result.SetDimensions(sizeX, sizeY, sizeZ);
        result.SetExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);
        // pre-allocate data
        result.AllocateScalars(getVtkType(dataType), sizeC);
        // get array structure
        array = result.GetPointData().GetScalars();

        switch (dataType)
        {
            case UBYTE:
            case BYTE:
                ((vtkUnsignedCharArray) array).SetJavaArray((byte[]) data);
                break;
            case USHORT:
                ((vtkUnsignedShortArray) array).SetJavaArray((short[]) data);
                break;
            case SHORT:
                ((vtkShortArray) array).SetJavaArray((short[]) data);
                break;
            case UINT:
                ((vtkUnsignedIntArray) array).SetJavaArray((int[]) data);
                break;
            case INT:
                ((vtkIntArray) array).SetJavaArray((int[]) data);
                break;
            case FLOAT:
                ((vtkFloatArray) array).SetJavaArray((float[]) data);
                break;
            case DOUBLE:
                ((vtkDoubleArray) array).SetJavaArray((double[]) data);
                break;
        }

        return result;
    }

    /**
     * Create a 3D surface in VTK polygon format from the input VTK image.
     * 
     * @param imageData
     *        the input image to construct surface from
     * @param threshold
     *        the threshold intensity value used to build the surface
     */
    public static vtkPolyData getSurfaceFromImage(vtkImageData imageData, double threshold)
    {
        vtkImageData out;
        vtkPolyData result;

        final int[] extent = imageData.GetExtent();
        extent[0]--; // min X
        extent[1]++; // max X
        extent[2]--; // min Y
        extent[3]++; // max Y
        extent[4]--; // min Z
        extent[5]++; // max Z

        // pad on all sides to guarantee closed meshes
        final vtkImageConstantPad pad = new vtkImageConstantPad();

        pad.SetOutputWholeExtent(extent);
        pad.SetInputData(imageData);
        pad.Update();

        out = pad.GetOutput();
        // do not delete input image
        pad.Delete();

        final vtkContourFilter contourFilter = new vtkContourFilter();
        contourFilter.SetInputData(out);
        contourFilter.SetValue(0, threshold);
        contourFilter.Update();
        result = contourFilter.GetOutput();
        contourFilter.GetInput().Delete();
        contourFilter.Delete();

        // final vtkMarchingCubes marchingCubes = new vtkMarchingCubes();
        //
        // marchingCubes.SetInputData(out);
        // marchingCubes.SetValue(0, threshold);
        // marchingCubes.Update();
        //
        // // get the poly data result
        // result = marchingCubes.GetOutput();
        // marchingCubes.GetInput().Delete();
        // marchingCubes.Delete();

        // if (keepLargest)
        // {
        // final vtkPolyDataConnectivityFilter cc = new vtkPolyDataConnectivityFilter();
        //
        // cc.SetInputData(result);
        // cc.SetExtractionModeToLargestRegion();
        // cc.Update();
        //
        // result = cc.GetOutput();
        // cc.GetInput().Delete();
        // cc.Delete();
        // }
        //
        // if (simplifyMesh)
        // {
        // final vtkDecimatePro dec = new vtkDecimatePro();
        //
        // dec.SetInputData(result);
        // dec.PreserveTopologyOn();
        // dec.SetTargetReduction(0.9);
        // dec.Update();
        //
        // result = dec.GetOutput();
        // dec.GetInput().Delete();
        // dec.Delete();
        // }
        //
        // if (smoothness > 0)
        // {
        // final vtkSmoothPolyDataFilter smoother = new vtkSmoothPolyDataFilter();
        //
        // smoother.SetInputData(result);
        // smoother.SetRelaxationFactor(0.3);
        // smoother.FeatureEdgeSmoothingOff();
        // smoother.BoundarySmoothingOn();
        // smoother.SetNumberOfIterations(smoothness);
        // smoother.Update();
        //
        // result = smoother.GetOutput();
        // smoother.GetInput().Delete();
        // smoother.Delete();
        // }

        return result;
    }

    /**
     * Creates and returns the color map in {@link vtkColorTransferFunction} format from the
     * specified {@link LUTChannel}.
     */
    public static vtkColorTransferFunction getColorMap(LUTChannel lutChannel)
    {
        final IcyColorMap colorMap = lutChannel.getColorMap();
        final Scaler scaler = lutChannel.getScaler();

        // SCALAR COLOR FUNCTION
        final vtkColorTransferFunction result = new vtkColorTransferFunction();

        result.SetRange(scaler.getLeftIn(), scaler.getRightIn());
        for (int i = 0; i < IcyColorMap.SIZE; i++)
        {
            result.AddRGBPoint(scaler.unscale(i), colorMap.getNormalizedRed(i), colorMap.getNormalizedGreen(i),
                    colorMap.getNormalizedBlue(i));
        }

        return result;
    }

    /**
     * Creates and returns the opacity map in {@link vtkPiecewiseFunction} format from the specified {@link LUTChannel}.
     */
    public static vtkPiecewiseFunction getOpacityMap(LUTChannel lutChannel)
    {
        final IcyColorMap colorMap = lutChannel.getColorMap();
        final Scaler scaler = lutChannel.getScaler();

        // SCALAR OPACITY FUNCTION
        final vtkPiecewiseFunction result = new vtkPiecewiseFunction();

        if (colorMap.isEnabled())
        {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                result.AddPoint(scaler.unscale(i), colorMap.getNormalizedAlpha(i));
        }
        else
        {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                result.AddPoint(scaler.unscale(i), 0d);
        }

        return result;
    }

    /**
     * Creates and returns a binary color map in {@link vtkColorTransferFunction} format where 0 value is black and 1 is
     * set to specified color.
     */
    public static vtkColorTransferFunction getBinaryColorMap(Color color)
    {
        // SCALAR COLOR FUNCTION
        final vtkColorTransferFunction result = new vtkColorTransferFunction();

        result.SetRange(0, 1);
        result.AddRGBPoint(0d, 0d, 0d, 0d);
        result.AddRGBPoint(1d, color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d);

        return result;
    }

    /**
     * Creates and returns a binary opacity map in {@link vtkPiecewiseFunction} format where 0 is 100% transparent and 1
     * to the specified opacity value.
     */
    public static vtkPiecewiseFunction getBinaryOpacityMap(double opacity)
    {
        // SCALAR OPACITY FUNCTION
        final vtkPiecewiseFunction result = new vtkPiecewiseFunction();

        result.AddPoint(0d, 1d);
        result.AddPoint(1d, opacity);

        return result;
    }

    /**
     * Set the Color of the specified {@link vtkPolyData} object.
     * 
     * @param polyData
     *        the vtkPolyData we want to change color
     * @param color
     *        the color to set
     * @param canvas
     *        the VtkCanvas object to lock during the color change operation for safety (can be <code>null</code> if we
     *        don't need to lock the VtkCanvas here)
     */
    public static void setPolyDataColor(vtkPolyData polyData, Color color, VtkCanvas canvas)
    {
        final int numPts = polyData.GetNumberOfPoints();
        vtkUnsignedCharArray colors = null;

        // try to recover colors object
        if (polyData.GetPointData() != null)
        {
            final vtkDataArray dataArray = polyData.GetPointData().GetScalars();

            if (dataArray instanceof vtkUnsignedCharArray)
                colors = (vtkUnsignedCharArray) dataArray;
            // delete it
            else if (dataArray != null)
                dataArray.Delete();
        }

        // colors is not correctly defined ? --> reallocate
        if ((colors == null) || (colors.GetNumberOfTuples() != numPts) || (colors.GetNumberOfComponents() != 3))
        {
            // delete first
            if (colors != null)
                colors.Delete();

            // and reallocate
            colors = new vtkUnsignedCharArray();
            colors.SetNumberOfComponents(3);
            colors.SetNumberOfTuples(numPts);
            // set colors array
            polyData.GetPointData().SetScalars(colors);
        }

        final int len = numPts * 3;

        final byte r = (byte) color.getRed();
        final byte g = (byte) color.getGreen();
        final byte b = (byte) color.getBlue();
        final byte[] data = new byte[len];

        for (int i = 0; i < len; i += 3)
        {
            data[i + 0] = r;
            data[i + 1] = g;
            data[i + 2] = b;
        }

        final IcyVtkPanel vtkPanel = (canvas != null) ? canvas.getVtkPanel() : null;

        if (vtkPanel != null)
        {
            vtkPanel.lock();
            try
            {
                colors.SetJavaArray(data);
                colors.Modified();
            }
            finally
            {
                vtkPanel.unlock();
            }
        }
        else
        {
            colors.SetJavaArray(data);
            colors.Modified();
        }
    }

    /**
     * Returns a cube polydata object representing the specified bounding box coordinate
     * 
     * @see #setOutlineBounds(vtkPolyData, double, double, double, double, double, double, VtkCanvas)
     */
    public static vtkPolyData getOutline(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax)
    {
        final double points[][] = new double[8][3];
        final int indexes[][] = {{0, 2, 3, 1}, {4, 5, 7, 6}, {0, 1, 5, 4}, {1, 3, 7, 5}, {0, 4, 6, 2}, {3, 2, 6, 7}};

        for (int i = 0; i < 8; i++)
        {
            points[i][0] = ((i & 1) == 0) ? xMin : xMax;
            points[i][1] = ((i & 2) == 0) ? yMin : yMax;
            points[i][2] = ((i & 4) == 0) ? zMin : zMax;
        }

        final vtkCellArray vCells = VtkUtil.getCells(6, prepareCells(indexes));
        final vtkPoints vPoints = VtkUtil.getPoints(points);
        final vtkPolyData result = new vtkPolyData();

        result.SetPolys(vCells);
        result.SetPoints(vPoints);

        return result;
    }

    /**
     * Set the bounds of specified outline polydata object (previously created with <i>VtkUtil.getOutline(..)</i>)
     * 
     * @param canvas
     *        the VtkCanvas object to lock during the color change operation for safety (can be <code>null</code> if we
     *        don't need to lock the VtkCanvas here)
     * @return <code>false</code> if the specified polydata object is not a valid outline object
     * @see #getOutline(double, double, double, double, double, double)
     */
    public static boolean setOutlineBounds(vtkPolyData outline, double xMin, double xMax, double yMin, double yMax,
            double zMin, double zMax, VtkCanvas canvas)
    {
        final vtkPoints previousPoints = outline.GetPoints();

        // not valid
        if ((previousPoints != null) && (previousPoints.GetNumberOfPoints() != 8))
            return false;

        final double newPoints[][] = new double[8][3];
        for (int i = 0; i < 8; i++)
        {
            newPoints[i][0] = ((i & 1) == 0) ? xMin : xMax;
            newPoints[i][1] = ((i & 2) == 0) ? yMin : yMax;
            newPoints[i][2] = ((i & 4) == 0) ? zMin : zMax;
        }

        final vtkPoints points = getPoints(newPoints);
        final IcyVtkPanel vtkPanel = (canvas != null) ? canvas.getVtkPanel() : null;

        if (vtkPanel != null)
        {
            vtkPanel.lock();
            try
            {
                // rebuild points
                outline.SetPoints(points);
                // changed
                outline.Modified();
                // delete previous points
                if (previousPoints != null)
                    previousPoints.Delete();
            }
            finally
            {
                vtkPanel.unlock();
            }
        }
        else
        {
            // rebuild points
            outline.SetPoints(points);
            // changed
            outline.Modified();
            // delete previous points
            if (previousPoints != null)
                previousPoints.Delete();
        }

        return true;
    }
}

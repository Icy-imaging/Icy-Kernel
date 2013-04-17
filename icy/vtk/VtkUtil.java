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
package icy.vtk;

import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkActor2DCollection;
import vtk.vtkActorCollection;
import vtk.vtkCellArray;
import vtk.vtkDataArray;
import vtk.vtkDoubleArray;
import vtk.vtkFloatArray;
import vtk.vtkIdTypeArray;
import vtk.vtkIntArray;
import vtk.vtkLongArray;
import vtk.vtkPoints;
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

    /**
     * Add an actor to the specified renderer.<br>
     * If the actor is already existing in the renderer then no operation is done.
     */
    public static void addProp(vtkRenderer renderer, vtkProp prop)
    {
        if ((renderer == null) || (prop == null))
            return;

        // actor not yet present in renderer ? --> add it
        if (!VtkUtil.findProp(renderer, prop))
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
     * Remove an actor from the specified renderer.
     */
    public static void removeProp(vtkRenderer renderer, vtkProp actor)
    {
        renderer.RemoveViewProp(actor);
    }

    /**
     * Return true if the renderer contains the specified actor
     */
    public static boolean findProp(vtkRenderer renderer, vtkProp actor)
    {
        if ((renderer == null) || (actor == null))
            return false;

        final vtkPropCollection actors = renderer.GetViewProps();

        actors.InitTraversal();
        for (int i = 0; i < actors.GetNumberOfItems(); i++)
        {
            final vtkProp curActor = actors.GetNextProp();

            // already present --> exit
            if (curActor == actor)
                return true;
        }

        return false;
    }

    /**
     * @deprecated Use {@link #findProp(vtkRenderer, vtkProp)} instead.
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
     * @deprecated Use {@link #findProp(vtkRenderer, vtkProp)} instead.
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
     * Get vtkCellArray from a 1D prepared cells array ( {n, i1, i2, ..., n, i1, i2,...} )
     */
    public static vtkCellArray getCells(int numCell, int[] cells)
    {
        final vtkCellArray result = new vtkCellArray();

        result.SetCells(numCell, getIdTypeArray(cells));

        return result;
    }
}

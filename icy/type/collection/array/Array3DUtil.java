/**
 * 
 */
package icy.type.collection.array;

import icy.type.DataType;

/**
 * @author Stephane
 */
public class Array3DUtil
{

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(byte[][][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array2DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(short[][][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array2DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(int[][][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array2DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(long[][][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array2DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(float[][][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array2DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(double[][][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array2DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Create a new 3D array with specified data type and length
     */
    public static Object[][] createArray(DataType dataType, int len)
    {
        switch (dataType.getJavaType())
        {
            case BYTE:
                return new byte[len][][];
            case SHORT:
                return new short[len][][];
            case INT:
                return new int[len][][];
            case LONG:
                return new int[len][][];
            case FLOAT:
                return new float[len][][];
            case DOUBLE:
                return new double[len][][];
            default:
                return null;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static Object[][] createArray(int dataType, int len)
    {
        return createArray(DataType.getDataType(dataType), len);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension byte array.
     */
    public static byte[] toByteArray1D(byte[][][] in)
    {
        return toByteArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension short array.
     */
    public static short[] toShortArray1D(short[][][] in)
    {
        return toShortArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension int array.
     */
    public static int[] toIntArray1D(int[][][] in)
    {
        return toIntArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension float array.
     */
    public static float[] toFloatArray1D(float[][][] in)
    {
        return toFloatArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension double array.
     */
    public static double[] toDoubleArray1D(double[][][] in)
    {
        return toDoubleArray1D(in, null, 0);
    }

    /**
     * Return the 3 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static byte[] toByteArray1D(byte[][][] in, byte[] out, int offset)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final byte[][] s_in = in[i];

                if (s_in != null)
                {
                    Array2DUtil.toByteArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 3 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static short[] toShortArray1D(short[][][] in, short[] out, int offset)
    {
        final short[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final short[][] s_in = in[i];

                if (s_in != null)
                {
                    Array2DUtil.toShortArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 3 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static int[] toIntArray1D(int[][][] in, int[] out, int offset)
    {
        final int[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final int[][] s_in = in[i];

                if (s_in != null)
                {
                    Array2DUtil.toIntArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 3 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static long[] toLongArray1D(long[][][] in, long[] out, int offset)
    {
        final long[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final long[][] s_in = in[i];

                if (s_in != null)
                {
                    Array2DUtil.toLongArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 3 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static float[] toFloatArray1D(float[][][] in, float[] out, int offset)
    {
        final float[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final float[][] s_in = in[i];

                if (s_in != null)
                {
                    Array2DUtil.toFloatArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 3 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static double[] toDoubleArray1D(double[][][] in, double[] out, int offset)
    {
        final double[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final double[][] s_in = in[i];

                if (s_in != null)
                {
                    Array2DUtil.toDoubleArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

}

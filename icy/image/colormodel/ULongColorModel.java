/**
 * 
 */
package icy.image.colormodel;

import icy.image.lut.LUT;
import icy.type.DataType;
import icy.type.TypeUtil;

/**
 * @author Stephane
 */
public class ULongColorModel extends IcyColorModel
{
    /**
     * Define a new ULongColorModel
     * 
     * @param numComponents
     *        number of color component
     * @param bits
     */
    public ULongColorModel(int numComponents, int[] bits)
    {
        super(numComponents, DataType.ULONG, bits);
    }

    @Override
    public int getRGB(Object pixel)
    {
        final long[] pix = (long[]) pixel;
        final int[] scaledData = new int[numComponents];

        for (int comp = 0; comp < numComponents; comp++)
            scaledData[comp] = (int) colormapScalers[comp].scale(TypeUtil.unsign(pix[comp]));

        return colorSpace.toRGBUnnorm(scaledData);
    }

    /**
     * Same as getRGB but by using the specified LUT instead of internal one
     * 
     * @see java.awt.image.ColorModel#getRGB(java.lang.Object)
     */
    @Override
    public int getRGB(Object pixel, LUT lut)
    {
        final long[] pix = (long[]) pixel;
        final int[] scaledData = new int[numComponents];

        for (int comp = 0; comp < numComponents; comp++)
            scaledData[comp] = (int) lut.getLutBand(comp).getScaler().scale(TypeUtil.unsign(pix[comp]));

        return lut.getColorSpace().toRGBUnnorm(scaledData);
    }

    @Override
    public int[] getComponents(Object pixel, int[] components, int offset)
    {
        final int[] result;

        if (components == null)
            result = new int[offset + numComponents];
        else
        {
            if ((components.length - offset) < numComponents)
                throw new IllegalArgumentException("Length of components array < number of components in model");

            result = components;
        }

        final long data[] = (long[]) pixel;
        final int len = data.length;

        for (int i = 0; i < len; i++)
            result[offset + i] = (int) data[i];

        return result;
    }

    @Override
    public Object getDataElements(int[] components, int offset, Object obj)
    {
        if ((components.length - offset) < numComponents)
            throw new IllegalArgumentException("Component array too small" + " (should be " + numComponents);

        final long[] pixel;
        final int len = components.length;

        if (obj == null)
            pixel = new long[numComponents];
        else
            pixel = (long[]) obj;

        for (int i = 0; i < len; i++)
            pixel[i] = components[offset + i];

        return pixel;
    }

    @Override
    public Object getDataElements(float[] normComponents, int offset, Object obj)
    {
        final long[] pixel;

        if (obj == null)
            pixel = new long[numComponents];
        else
            pixel = (long[]) obj;

        for (int c = 0, nc = offset; c < numComponents; c++, nc++)
            pixel[c] = TypeUtil.toLong(normalScalers[c].unscale(normComponents[nc]));

        return pixel;
    }

    @Override
    public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset)
    {
        final float[] result;

        if (normComponents == null)
            result = new float[numComponents + normOffset];
        else
            result = normComponents;

        final long[] data = (long[]) pixel;

        for (int c = 0, nc = normOffset; c < numComponents; c++, nc++)
            result[nc] = (float) normalScalers[c].scale(TypeUtil.unsign(data[c]));

        return result;
    }
}

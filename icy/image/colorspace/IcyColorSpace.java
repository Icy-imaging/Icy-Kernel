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
package icy.image.colorspace;

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.image.colormap.FromRGBColorMap;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.IcyColorMapEvent;
import icy.image.colormap.IcyColorMapListener;
import icy.image.colormap.LinearColorMap;
import icy.image.colormodel.IcyColorModel;
import icy.type.DataType;
import icy.type.collection.array.ArrayUtil;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public class IcyColorSpace extends ColorSpace implements ChangeListener, IcyColorMapListener
{
    /**
	 * 
	 */
    private static final long serialVersionUID = 6413334779215415163L;

    /**
     * RGB colorSpace
     */
    private static ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /**
     * toRGB colormaps
     */
    private final IcyColorMap[] toRGBmaps;
    /**
     * fromRGB colormaps
     */
    private final FromRGBColorMap[] fromRGBmaps;

    /**
     * use alpha
     */
    // private boolean alphaEnabled;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    /**
     * Create an icy colorspace object
     * 
     * @param numComponents
     *        number of color component
     */
    public IcyColorSpace(int numComponents)
    {
        super((numComponents > 1) ? 10 + numComponents : ColorSpace.TYPE_GRAY, numComponents);

        if (numComponents == 0)
            throw new IllegalArgumentException("numComponents must be > 0");

        // allocating toRGB colormaps
        toRGBmaps = new IcyColorMap[numComponents];
        for (int i = 0; i < numComponents; i++)
        {
            final IcyColorMap colormap = new IcyColorMap("component " + i);
            toRGBmaps[i] = colormap;
            // add listener
            colormap.addListener(this);
        }

        // allocating fromRGB colormaps
        fromRGBmaps = new FromRGBColorMap[4];
        for (int i = 0; i < 4; i++)
            fromRGBmaps[i] = new FromRGBColorMap(numComponents);

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // // alpha is enabled by default
        // alphaEnabled = true;

        beginUpdate();
        try
        {
            // define default colormaps depending the number of component
            switch (numComponents)
            {
                case 1:
                    // Gray
                    setColorMap(0, LinearColorMap.white_, true);
                    break;

                case 2:
                    // Red / Green
                    setColorMap(0, LinearColorMap.red_, true);
                    setColorMap(1, LinearColorMap.green_, true);
                    break;

                case 3:
                    // RGB
                    setColorMap(0, LinearColorMap.red_, true);
                    setColorMap(1, LinearColorMap.green_, true);
                    setColorMap(2, LinearColorMap.blue_, true);
                    break;

                case 4:
                    // ARGB
                    setColorMap(0, LinearColorMap.red_, true);
                    setColorMap(1, LinearColorMap.green_, true);
                    setColorMap(2, LinearColorMap.blue_, true);
                    setColorMap(3, LinearColorMap.alpha_, true);
                    break;

                case 5:
                    // RGB CM
                    setColorMap(0, LinearColorMap.red_, true);
                    setColorMap(1, LinearColorMap.green_, true);
                    setColorMap(2, LinearColorMap.blue_, true);
                    setColorMap(3, LinearColorMap.cyan_, true);
                    setColorMap(4, LinearColorMap.magenta_, true);
                    break;

                case 6:
                    // RGB CMY
                    setColorMap(0, LinearColorMap.red_, true);
                    setColorMap(1, LinearColorMap.green_, true);
                    setColorMap(2, LinearColorMap.blue_, true);
                    setColorMap(3, LinearColorMap.cyan_, true);
                    setColorMap(4, LinearColorMap.magenta_, true);
                    setColorMap(5, LinearColorMap.yellow_, true);
                    break;

                default:
                    // RGB CMY W
                    setColorMap(0, LinearColorMap.red_, true);
                    setColorMap(1, LinearColorMap.green_, true);
                    setColorMap(2, LinearColorMap.blue_, true);
                    setColorMap(3, LinearColorMap.cyan_, true);
                    setColorMap(4, LinearColorMap.magenta_, true);
                    setColorMap(5, LinearColorMap.yellow_, true);
                    setColorMap(6, LinearColorMap.white_, true);
                    break;
            }

            // black map for the rest
            for (int i = 7; i < numComponents; i++)
                setColorMap(i, LinearColorMap.black_, true);
        }
        finally
        {
            endUpdate();
        }

        // generate fromRGB maps
        generateFromRGBColorMaps();
    }

    /**
     * Return true if the colorspace's colormap contains an alpha component<br>
     * This is different from the isAlphaEnabled flag
     */
    public boolean hasAlphaComponent()
    {
        for (int comp = 0; comp < toRGBmaps.length; comp++)
            if (toRGBmaps[comp].getType() == IcyColorMapType.ALPHA)
                return true;

        return false;
    }

    // /**
    // * @return the alphaEnabled
    // */
    // public boolean isAlphaEnabled()
    // {
    // return alphaEnabled;
    // }
    //
    // /**
    // * @param alphaEnabled
    // * the alphaEnabled to set
    // */
    // public void setAlphaEnabled(boolean alphaEnabled)
    // {
    // this.alphaEnabled = alphaEnabled;
    // }

    /**
     * Generate FromRGB colormaps from ToRGB ones
     */
    private void generateFromRGBColorMaps()
    {
        for (int comp = 0; comp < toRGBmaps.length; comp++)
        {
            final IcyColorMap toRGBmap = toRGBmaps[comp];
            final float step = 1.0f / FromRGBColorMap.COLORMAP_MAX;

            for (float intensity = 0.0f; intensity <= 1.0f; intensity += step)
            {
                // blue
                fromRGBmaps[0].setFromRGBColor(comp, intensity, toRGBmap.getNormalizedBlue(intensity));
                // green
                fromRGBmaps[1].setFromRGBColor(comp, intensity, toRGBmap.getNormalizedGreen(intensity));
                // red
                fromRGBmaps[2].setFromRGBColor(comp, intensity, toRGBmap.getNormalizedRed(intensity));
                // alpha
                fromRGBmaps[3].setFromRGBColor(comp, intensity, toRGBmap.getNormalizedAlpha(intensity));
            }
        }
    }

    /**
     * @see java.awt.color.ColorSpace#fromCIEXYZ(float[])
     */
    @Override
    public float[] fromCIEXYZ(float[] colorvalue)
    {
        return fromRGB(sRGB.fromCIEXYZ(colorvalue));
    }

    /**
     * @see java.awt.color.ColorSpace#toCIEXYZ(float[])
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue)
    {
        return sRGB.toCIEXYZ(toRGB(colorvalue));
    }

    /**
     * @see java.awt.color.ColorSpace#fromRGB(float[])
     */
    @Override
    public float[] fromRGB(float[] rgb)
    {
        final int numComponents = getNumComponents();
        final float[] result = new float[numComponents];

        final FromRGBColorMap blueMap = fromRGBmaps[0];
        final FromRGBColorMap greenMap = fromRGBmaps[1];
        final FromRGBColorMap redMap = fromRGBmaps[2];
        final FromRGBColorMap alphaMap = fromRGBmaps[3];

        final float blue = rgb[0];
        final float green = rgb[1];
        final float red = rgb[2];
        final float alpha;
        if (rgb.length > 3)
            alpha = rgb[3];
        else
            alpha = 1.0f;

        for (int comp = 0; comp < numComponents; comp++)
        {
            result[comp] = blueMap.getFromRGBColor(comp, blue);
            result[comp] += greenMap.getFromRGBColor(comp, green);
            result[comp] += redMap.getFromRGBColor(comp, red);
            result[comp] *= alphaMap.getFromRGBColor(comp, alpha);
            // limit
            if (result[comp] > 1.0f)
                result[comp] = 1.0f;
        }

        return result;
    }

    /**
     * return normalized component values from ARGB packed integer values
     * 
     * @param rgb
     * @return float[]
     */
    public float[] fromRGB(int rgb)
    {
        final int numComponents = getNumComponents();
        final float[] result = new float[numComponents];

        final FromRGBColorMap blueMap = fromRGBmaps[0];
        final FromRGBColorMap greenMap = fromRGBmaps[1];
        final FromRGBColorMap redMap = fromRGBmaps[2];
        final FromRGBColorMap alphaMap = fromRGBmaps[3];

        final int red, grn, blu, alp;
        alp = (rgb >> 24) & 0xFF;
        red = (rgb >> 16) & 0xFF;
        grn = (rgb >> 8) & 0xFF;
        blu = rgb & 0xFF;

        for (int comp = 0; comp < numComponents; comp++)
        {
            result[comp] = blueMap.maps[comp][blu];
            result[comp] += greenMap.maps[comp][grn];
            result[comp] += redMap.maps[comp][red];
            result[comp] *= alphaMap.maps[comp][alp];
            // limit
            if (result[comp] > 1.0f)
                result[comp] = 1.0f;
        }

        return result;
    }

    /**
     * return unnormalized ARGB values from colorMap scaled components values
     * 
     * @param colorvalue
     * @return ARGB as int
     */
    public int toRGBUnnorm(final int[] colorvalue)
    {
        final int numComponents = Math.min(getNumComponents(), colorvalue.length);

        // default alpha
        float alpha = 1f;
        // default max local alpha
        float maxLocalAlpha = 0f;
        // default RGB
        int r = 0, g = 0, b = 0;

        for (int comp = 0; comp < numComponents; comp++)
        {
            final IcyColorMap cm = toRGBmaps[comp];

            if (cm.isEnabled())
            {
                final int value = colorvalue[comp];

                final float alphaValue = cm.alpha.mapf[value];

                // alpha channel ?
                if (cm.getType() == IcyColorMapType.ALPHA)
                    alpha = alphaValue;
                else if (alphaValue > maxLocalAlpha)
                    maxLocalAlpha = alphaValue;

                final int premulRGB[] = cm.getPremulRGB()[value];

                b += premulRGB[0];
                g += premulRGB[1];
                r += premulRGB[2];
            }
        }

        // final alpha = alpha component value * maximum local alpha value
        final int a = (int) (alpha * maxLocalAlpha * IcyColorMap.MAX_LEVEL);

        if (a != 0)
        {
            final int inv = (1 << (IcyColorMap.COLORMAP_BITS + 8)) / a;

            // normalize on alpha
            b = (b * inv) >> 8;
            g = (g * inv) >> 8;
            r = (r * inv) >> 8;
        }

        return ((b > IcyColorMap.MAX_LEVEL) ? IcyColorMap.MAX_LEVEL : b)
                | (((g > IcyColorMap.MAX_LEVEL) ? IcyColorMap.MAX_LEVEL : g) << 8)
                | (((r > IcyColorMap.MAX_LEVEL) ? IcyColorMap.MAX_LEVEL : r) << 16) | (a << 24);
    }

    @Override
    public float[] toRGB(float[] colorvalue)
    {
        final float[] result = new float[4];
        final int numComponents = Math.min(getNumComponents(), colorvalue.length);

        // default alpha
        float alpha = 1f;
        // default max local alpha
        float maxLocalAlpha = 0f;
        // default RGB
        float rf = 0f, gf = 0f, bf = 0f;

        for (int comp = 0; comp < numComponents; comp++)
        {
            final IcyColorMap cm = toRGBmaps[comp];

            if (cm.isEnabled())
            {
                final int value = (int) (colorvalue[comp] * IcyColorMap.MAX_INDEX);
                final float alphaValue = cm.alpha.mapf[value];

                // alpha channel ?
                if (cm.getType() == IcyColorMapType.ALPHA)
                    alpha = alphaValue;
                else if (alphaValue > maxLocalAlpha)
                    maxLocalAlpha = alphaValue;

                final float premulRGBNorm[] = cm.getPremulRGBNorm()[value];

                bf += premulRGBNorm[0];
                gf += premulRGBNorm[1];
                rf += premulRGBNorm[2];
            }
        }

        // final alpha = alpha component value * maximum local alpha value
        final float af = alpha * maxLocalAlpha;

        if (af != 0f)
        {
            final float inv = 1f / af;

            // normalize on alpha
            bf *= inv;
            gf *= inv;
            rf *= inv;
        }

        result[0] = (bf > 1f) ? 1f : bf;
        result[1] = (gf > 1f) ? 1f : gf;
        result[2] = (rf > 1f) ? 1f : rf;
        result[3] = af;

        return result;
    }

    /**
     * Set 8 bit ARGB data in an ARGB buffer from a scaled input buffer
     * 
     * @param unnormSrc
     *        source buffer containing unnormalized values ([0..255] range) for each component
     * @param dest
     *        ARGB components buffer
     */
    public void fillARGBBuffer(int[][] unnormSrc, int[] dest, int offset, int length)
    {
        final int numComponents = getNumComponents();

        if (numComponents > 0)
        {
            final int[] input = new int[numComponents];

            for (int i = 0; i < length; i++)
            {
                // get data value
                for (int comp = 0; comp < numComponents; comp++)
                    input[comp] = unnormSrc[comp][i];

                // convert to RGBA
                dest[offset + i] = toRGBUnnorm(input);
            }
        }
    }

    /**
     * Set 8 bit ARGB data in an ARGB buffer from a scaled input buffer
     * 
     * @param unnormSrc
     *        source buffer containing unnormalized values ([0..255] range) for each component
     * @param dest
     *        ARGB components buffer
     */
    public void fillARGBBuffer(int[][] unnormSrc, int[] dest)
    {
        if ((unnormSrc == null) || (dest == null))
        {
            throw new IllegalArgumentException("Parameters 'unnormSrc' and 'destARGB' should not be null !");
        }

        final int numComponents = getNumComponents();

        if (unnormSrc.length != numComponents)
        {
            throw new IllegalArgumentException("Parameters 'unnormSrc' size is [" + unnormSrc.length + "][..] where ["
                    + numComponents + "][..] is expected !");
        }

        if (numComponents > 0)
        {
            final int size = unnormSrc[0].length;
            final int[] input = new int[numComponents];

            for (int i = 0; i < size; i++)
            {
                // get data value
                for (int comp = 0; comp < numComponents; comp++)
                    input[comp] = unnormSrc[comp][i];

                // convert to RGBA
                dest[i] = toRGBUnnorm(input);
            }
        }
    }

    /**
     * Return the number of component of colorSpace
     */
    @Override
    public int getNumComponents()
    {
        return toRGBmaps.length;
    }

    /**
     * Return the colormap of the specified component.
     */
    public IcyColorMap getColorMap(int component)
    {
        return toRGBmaps[component];
    }

    /**
     * @deprecated Use {@link #getColorMap(int)} instead (different case).
     */
    @Deprecated
    public IcyColorMap getColormap(int component)
    {
        return getColorMap(component);
    }

    /**
     * Set the colormap for the specified component (actually copy the content of source colormap).
     * 
     * @param component
     *        component we want to set the colormap
     * @param colorMap
     *        source colorMap
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMap(int component, IcyColorMap colorMap, boolean setAlpha)
    {
        toRGBmaps[component].copyFrom(colorMap, setAlpha);
    }

    /**
     * @deprecated Use {@link #setColorMap(int, IcyColorMap, boolean)} instead.
     */
    @Deprecated
    public void setColormap(int component, IcyColorMap map)
    {
        setColorMap(component, map, true);
    }

    /**
     * @deprecated Use <code>setColormap(channel, map)</code> instead.
     */
    @Deprecated
    public void copyColormap(int component, IcyColorMap map, boolean copyName, boolean copyAlpha)
    {
        setColorMap(component, map, copyAlpha);

        if (copyName)
            toRGBmaps[component].setName(map.getName());
    }

    /**
     * @deprecated Use <code>setColormap(channel, map)</code> instead.
     */
    @Deprecated
    public void copyColormap(int component, IcyColorMap map, boolean copyName)
    {
        setColorMap(component, map, true);

        if (copyName)
            toRGBmaps[component].setName(map.getName());
    }

    /**
     * @deprecated Use <code>setColormap(channel, map)</code> instead.
     */
    @Deprecated
    public void copyColormap(int component, IcyColorMap map)
    {
        setColorMap(component, map, true);
    }

    /**
     * Return the RGB inverse colormap for specified RGB component.
     */
    public FromRGBColorMap getFromRGBMap(int component)
    {
        return fromRGBmaps[component];
    }

    /**
     * Set the RGB colormaps from a compatible colorModel.
     */
    public void setColorMaps(ColorModel cm)
    {
        if (cm instanceof IcyColorModel)
            setColorMaps((IcyColorSpace) cm.getColorSpace(), true);
        else
        {
            // get datatype and numComponent of source colorModel
            final Object srcElem = cm.getDataElements(0x0, null);
            final DataType srcDataType = ArrayUtil.getDataType(srcElem);
            final int srcNumComponents = ArrayUtil.getLength(srcElem);
            final DataType dataType = DataType.getDataTypeFromDataBufferType(cm.getTransferType());
            final int numComponents = getNumComponents();

            // can't recover colormap if we have different dataType or numComponents
            if ((srcNumComponents != numComponents) || (srcDataType != dataType))
                return;

            final boolean hasAlpha = cm.hasAlpha();

            for (int comp = 0; comp < numComponents; comp++)
            {
                final IcyColorMap map = toRGBmaps[comp];

                map.beginUpdate();
                try
                {
                    // set type
                    if (hasAlpha && (comp == (numComponents - 1)))
                        map.setType(IcyColorMapType.ALPHA);
                    else
                        map.setType(IcyColorMapType.RGB);

                    for (int index = 0; index < IcyColorMap.SIZE; index++)
                    {
                        switch (dataType.getJavaType())
                        {
                            case BYTE:
                            {
                                final byte bvalues[] = new byte[numComponents];

                                // build an pixel element
                                for (int i = 0; i < numComponents; i++)
                                {
                                    if (i == comp)
                                        bvalues[i] = (byte) (index * (1 << 8) / IcyColorMap.SIZE);
                                    else if (hasAlpha && (i == (numComponents - 1)))
                                        bvalues[i] = (byte) (IcyColorMap.MAX_INDEX * (1 << 8) / IcyColorMap.SIZE);
                                    else
                                        bvalues[i] = 0;
                                }

                                // set colormap data
                                map.setAlpha(index, (short) cm.getAlpha(bvalues));
                                map.setRed(index, (short) cm.getRed(bvalues));
                                map.setGreen(index, (short) cm.getGreen(bvalues));
                                map.setBlue(index, (short) cm.getBlue(bvalues));
                                break;
                            }

                            case SHORT:
                            {
                                final short svalues[] = new short[numComponents];

                                // build an pixel element
                                for (int i = 0; i < numComponents; i++)
                                {
                                    if (i == comp)
                                        svalues[i] = (short) (index * (1 << 16) / IcyColorMap.SIZE);
                                    else if (hasAlpha && (i == (numComponents - 1)))
                                        svalues[i] = (short) (IcyColorMap.MAX_INDEX * (1 << 16) / IcyColorMap.SIZE);
                                    else
                                        svalues[i] = 0;
                                }

                                // set colormap data
                                map.setAlpha(index, (short) cm.getAlpha(svalues));
                                map.setRed(index, (short) cm.getRed(svalues));
                                map.setGreen(index, (short) cm.getGreen(svalues));
                                map.setBlue(index, (short) cm.getBlue(svalues));
                                break;
                            }

                            case INT:
                            {
                                final int ivalues[] = new int[numComponents];

                                // build an pixel element
                                for (int i = 0; i < numComponents; i++)
                                {
                                    if (i == comp)
                                        ivalues[i] = (index * (1 << 32) / IcyColorMap.SIZE);
                                    else if (hasAlpha && (i == (numComponents - 1)))
                                        ivalues[i] = (IcyColorMap.MAX_INDEX * (1 << 32) / IcyColorMap.SIZE);
                                    else
                                        ivalues[i] = 0;
                                }

                                // set colormap data
                                map.setAlpha(index, (short) cm.getAlpha(ivalues));
                                map.setRed(index, (short) cm.getRed(ivalues));
                                map.setGreen(index, (short) cm.getGreen(ivalues));
                                map.setBlue(index, (short) cm.getBlue(ivalues));
                                break;
                            }

                            case LONG:
                            {
                                final long lvalues[] = new long[numComponents];

                                // build an pixel element
                                for (int i = 0; i < numComponents; i++)
                                {
                                    if (i == comp)
                                        lvalues[i] = (index * (1 << 32) / IcyColorMap.SIZE);
                                    else if (hasAlpha && (i == (numComponents - 1)))
                                        lvalues[i] = (IcyColorMap.MAX_INDEX * (1 << 32) / IcyColorMap.SIZE);
                                    else
                                        lvalues[i] = 0;
                                }

                                // set colormap data
                                map.setAlpha(index, (short) cm.getAlpha(lvalues));
                                map.setRed(index, (short) cm.getRed(lvalues));
                                map.setGreen(index, (short) cm.getGreen(lvalues));
                                map.setBlue(index, (short) cm.getBlue(lvalues));
                                break;
                            }

                            case FLOAT:
                            {
                                final float fvalues[] = new float[numComponents];

                                // build an pixel element
                                for (int i = 0; i < numComponents; i++)
                                {
                                    if (i == comp)
                                        fvalues[i] = (float) index / (float) IcyColorMap.SIZE;
                                    else if (hasAlpha && (i == (numComponents - 1)))
                                        fvalues[i] = (IcyColorMap.MAX_INDEX / (float) IcyColorMap.SIZE);
                                    else
                                        fvalues[i] = 0;
                                }

                                // set colormap data
                                map.setAlpha(index, (short) cm.getAlpha(fvalues));
                                map.setRed(index, (short) cm.getRed(fvalues));
                                map.setGreen(index, (short) cm.getGreen(fvalues));
                                map.setBlue(index, (short) cm.getBlue(fvalues));
                                break;
                            }

                            case DOUBLE:
                            {
                                final double dvalues[] = new double[numComponents];

                                // build an pixel element
                                for (int i = 0; i < numComponents; i++)
                                {
                                    if (i == comp)
                                        dvalues[i] = (double) index / (double) IcyColorMap.SIZE;
                                    else if (hasAlpha && (i == (numComponents - 1)))
                                        dvalues[i] = (IcyColorMap.MAX_INDEX / (double) IcyColorMap.SIZE);
                                    else
                                        dvalues[i] = 0;
                                }

                                // set colormap data
                                map.setAlpha(index, (short) cm.getAlpha(dvalues));
                                map.setRed(index, (short) cm.getRed(dvalues));
                                map.setGreen(index, (short) cm.getGreen(dvalues));
                                map.setBlue(index, (short) cm.getBlue(dvalues));
                                break;
                            }
                        }
                    }
                }
                finally
                {
                    map.endUpdate();
                }
            }
        }
    }

    /**
     * @deprecated Use {@link #setColorMaps(ColorModel)} instead (different case).
     */
    @Deprecated
    public void setColormaps(ColorModel cm)
    {
        setColorMaps(cm);
    }

    /**
     * @deprecated Use {@link #setColorMaps(ColorModel)} instead.
     */
    @Deprecated
    public void copyColormaps(ColorModel cm)
    {
        setColorMaps(cm);
    }

    /**
     * Set colormaps from specified colorSpace (do a copy).
     * 
     * @param source
     *        source colorspace to copy the colormaps from
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMaps(IcyColorSpace source, boolean setAlpha)
    {
        final int numComponents = Math.min(source.getNumComponents(), getNumComponents());

        beginUpdate();
        try
        {
            // copy colormap
            for (int comp = 0; comp < numComponents; comp++)
                setColorMap(comp, source.getColorMap(comp), setAlpha);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @deprecated Use {@link #setColorMaps(IcyColorSpace, boolean)} instead.
     */
    @Deprecated
    public void setColormaps(IcyColorSpace source)
    {
        setColorMaps(source, true);
    }

    /**
     * @deprecated Use {@link #setColorMaps(IcyColorSpace, boolean)} instead.
     */
    @Deprecated
    public void copyColormaps(IcyColorSpace source)
    {
        setColorMaps(source, true);
    }

    /**
     * get index of the specified colormap
     * 
     * @param colormap
     * @return index
     */
    public int indexOfColorMap(IcyColorMap colormap)
    {
        for (int i = 0; i < toRGBmaps.length; i++)
            if (toRGBmaps[i].equals(colormap))
                return i;

        return -1;
    }

    @Override
    public String getName(int idx)
    {
        // TODO: should get name from metadata
        return "Component #" + idx;
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(IcyColorSpaceListener listener)
    {
        listeners.add(IcyColorSpaceListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(IcyColorSpaceListener listener)
    {
        listeners.remove(IcyColorSpaceListener.class, listener);
    }

    /**
     * fire event
     */
    public void fireEvent(IcyColorSpaceEvent e)
    {
        for (IcyColorSpaceListener listener : listeners.getListeners(IcyColorSpaceListener.class))
            listener.colorSpaceChanged(e);
    }

    /**
     * called when colorspace has changed (afaik when a colormap has changed)
     */
    private void changed(int component)
    {
        final IcyColorMap colorMap = getColorMap(component);

        // we can have only 1 alpha colormap
        if (colorMap != null)
        {
            // alpha type colormap ?
            if (colorMap.getType() == IcyColorMapType.ALPHA)
            {
                // check that others colormap are non alpha
                for (IcyColorMap map : toRGBmaps)
                {
                    if (map != colorMap)
                    {
                        // we have another ALPHA colormap ?
                        if (map.getType() == IcyColorMapType.ALPHA)
                            // set it to RGB
                            map.setType(IcyColorMapType.RGB);
                    }
                }
            }
        }

        // handle changed via updater object
        updater.changed(new IcyColorSpaceEvent(this, component));
    }

    /**
     * process on colorspace change
     */
    @Override
    public void onChanged(EventHierarchicalChecker compare)
    {
        final IcyColorSpaceEvent event = (IcyColorSpaceEvent) compare;

        // recalculate fromRGB colormaps
        generateFromRGBColorMaps();

        // notify listener we have changed
        fireEvent(event);
    }

    @Override
    public void colorMapChanged(IcyColorMapEvent e)
    {
        final int index = indexOfColorMap(e.getColormap());

        // colormap found ? raise a "changed" event
        if (index != -1)
            changed(index);
    }

    /**
     * @see icy.common.UpdateEventHandler#beginUpdate()
     */
    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    /**
     * @see icy.common.UpdateEventHandler#endUpdate()
     */
    public void endUpdate()
    {
        updater.endUpdate();
    }

    /**
     * @see icy.common.UpdateEventHandler#isUpdating()
     */
    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

}

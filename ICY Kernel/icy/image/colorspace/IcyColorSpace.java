/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.image.colorspace;

import icy.common.EventHierarchicalChecker;
import icy.common.IcyChangedListener;
import icy.common.UpdateEventHandler;
import icy.image.colormap.FromRGBColorMap;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.IcyColorMapEvent;
import icy.image.colormap.IcyColorMapListener;
import icy.image.colormap.LinearColorMap;
import icy.image.colormodel.IcyColorModel;
import icy.image.colorspace.IcyColorSpaceEvent.IcyColorSpaceEventType;
import icy.type.TypeUtil;
import icy.type.collection.array.ArrayUtil;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public class IcyColorSpace extends ColorSpace implements IcyChangedListener, IcyColorMapListener
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
    private boolean alphaEnabled;

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

        // alpha is enabled by default
        alphaEnabled = true;

        beginUpdate();
        try
        {
            // define default colormaps depending the number of component
            switch (numComponents)
            {
                // Gray
                case 1:
                    copyColormap(0, LinearColorMap.white_);
                    break;

                // Red / Green
                case 2:
                    copyColormap(0, LinearColorMap.red_);
                    copyColormap(1, LinearColorMap.green_);
                    break;

                // RGB
                case 3:
                    copyColormap(0, LinearColorMap.red_);
                    copyColormap(1, LinearColorMap.green_);
                    copyColormap(2, LinearColorMap.blue_);
                    break;

                // ARGB
                default:
                    copyColormap(0, LinearColorMap.red_);
                    copyColormap(1, LinearColorMap.green_);
                    copyColormap(2, LinearColorMap.blue_);
                    copyColormap(3, LinearColorMap.alpha_);
            }

            // black map for the rest
            for (int i = 4; i < numComponents; i++)
                copyColormap(i, LinearColorMap.black_);
        }
        finally
        {
            endUpdate();
        }

        // generate fromRGB maps
        generateFromRGBColorMaps();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        // remove listeners
        for (int i = 0; i < toRGBmaps.length; i++)
            toRGBmaps[i].removeListener(this);

        super.finalize();
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

    /**
     * @return the alphaEnabled
     */
    public boolean isAlphaEnabled()
    {
        return alphaEnabled;
    }

    /**
     * @param alphaEnabled
     *        the alphaEnabled to set
     */
    public void setAlphaEnabled(boolean alphaEnabled)
    {
        this.alphaEnabled = alphaEnabled;
    }

    /**
     * Generate FromRGB colormaps from ToRGB ones
     */
    public void generateFromRGBColorMaps()
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
    public int toRGBUnnorm(int[] colorvalue)
    {
        final int numComponents = getNumComponents();
        final float invMaxAlpha;
        int finalAlpha;

        // get max alpha
        if (alphaEnabled)
        {
            finalAlpha = -1;
            // get max alpha
            float maxAlpha = 0f;
            for (int comp = 0; comp < numComponents; comp++)
            {
                final IcyColorMap cm = toRGBmaps[comp];
                final float alphaValue = cm.alpha.mapf[colorvalue[comp]];

                // alpha channel ?
                if (cm.getType() == IcyColorMapType.ALPHA)
                    finalAlpha = (int) (alphaValue * IcyColorMap.MAX_LEVEL);
                else if (alphaValue > maxAlpha)
                    maxAlpha = alphaValue;
            }

            // no alpha component ? use the maximum alpha value
            if (finalAlpha == -1)
                finalAlpha = (int) (maxAlpha * IcyColorMap.MAX_LEVEL);

            if (maxAlpha > 0f)
                invMaxAlpha = IcyColorMap.MAX_LEVEL / maxAlpha;
            else
                invMaxAlpha = 0f;
        }
        else
        {
            finalAlpha = IcyColorMap.MAX_LEVEL;
            invMaxAlpha = IcyColorMap.MAX_LEVEL;
        }

        float rf, gf, bf;

        // default
        rf = 0f;
        gf = 0f;
        bf = 0f;

        for (int comp = 0; comp < numComponents; comp++)
        {
            final IcyColorMap cm = toRGBmaps[comp];
            final int value = colorvalue[comp];

            switch (cm.getType())
            {
                case GRAY:
                    final float grayValue = cm.gray.mapf[value] * cm.alpha.mapf[value] * invMaxAlpha;
                    bf += grayValue;
                    gf += grayValue;
                    rf += grayValue;
                    break;

                case RGB:
                    final float alpha = cm.alpha.mapf[value] * invMaxAlpha;
                    bf += cm.blue.mapf[value] * alpha;
                    gf += cm.green.mapf[value] * alpha;
                    rf += cm.red.mapf[value] * alpha;
                    break;
            }
        }

        final int b = Math.min(IcyColorMap.MAX_LEVEL, (int) bf);
        final int g = Math.min(IcyColorMap.MAX_LEVEL, (int) gf);
        final int r = Math.min(IcyColorMap.MAX_LEVEL, (int) rf);

        return b | (g << 8) | (r << 16) | (finalAlpha << 24);
    }

    /**
     * @see java.awt.color.ColorSpace#toRGB(float[])
     */
    @Override
    public float[] toRGB(float[] colorvalue)
    {
        final int numComponents = getNumComponents();
        final float[] result = new float[4];
        final int[] intvalues = new int[numComponents];
        final float invMaxAlpha;
        int finalAlpha;

        // unnorm color values
        for (int comp = 0; comp < numComponents; comp++)
            intvalues[comp] = (int) (colorvalue[comp] * IcyColorMap.MAX_INDEX);

        // default
        result[0] = 0f;
        result[1] = 0f;
        result[2] = 0f;

        // get max alpha
        if (alphaEnabled)
        {
            finalAlpha = -1;
            // get max alpha
            float maxAlpha = 0f;
            for (int comp = 0; comp < numComponents; comp++)
            {
                final IcyColorMap cm = toRGBmaps[comp];
                final float alphaValue = cm.alpha.mapf[intvalues[comp]];

                // alpha channel ?
                if (cm.getType() == IcyColorMapType.ALPHA)
                    finalAlpha = (int) (alphaValue * IcyColorMap.MAX_LEVEL);
                else if (alphaValue > maxAlpha)
                    maxAlpha = alphaValue;
            }

            // no alpha component ? use the maximum alpha value
            if (finalAlpha == -1)
                finalAlpha = (int) (maxAlpha * IcyColorMap.MAX_LEVEL);

            if (maxAlpha > 0f)
                invMaxAlpha = IcyColorMap.MAX_LEVEL / maxAlpha;
            else
                invMaxAlpha = 0f;
        }
        else
        {
            finalAlpha = IcyColorMap.MAX_LEVEL;
            invMaxAlpha = IcyColorMap.MAX_LEVEL;
        }

        for (int comp = 0; comp < numComponents; comp++)
        {
            final IcyColorMap cm = toRGBmaps[comp];
            final int value = intvalues[comp];

            switch (cm.getType())
            {
                case GRAY:
                    final float grayValue = cm.gray.mapf[value] * cm.alpha.mapf[value] * invMaxAlpha;
                    result[0] += grayValue;
                    result[1] += grayValue;
                    result[2] += grayValue;
                    break;

                case RGB:
                    final float alpha = cm.alpha.mapf[value] * invMaxAlpha;
                    result[0] += cm.blue.mapf[value] * alpha;
                    result[1] += cm.green.mapf[value] * alpha;
                    result[2] += cm.red.mapf[value] * alpha;
                    break;
            }
        }

        // limit
        if (result[0] > 1.0f)
            result[0] = 1.0f;
        if (result[1] > 1.0f)
            result[1] = 1.0f;
        if (result[2] > 1.0f)
            result[2] = 1.0f;

        // alpha
        result[3] = finalAlpha;

        return result;
    }

    /**
     * Set 8 bit ARGB data in an ARGB buffer from a scaled input buffer
     * 
     * @param unnormSrc
     *        source buffer containing unnormalized values ([0..255] range) for each component
     * @param dest
     *        ARGB components buffer ([4][0..size-1])
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
     *        ARGB components buffer ([4][0..size-1])
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
     * Return the toRGB colormap of specified component
     */
    public IcyColorMap getColormap(int component)
    {
        return toRGBmaps[component];
    }

    /**
     * Set the toRGB colormap of specified component
     */
    public void copyColormap(int component, IcyColorMap srcColorMap, boolean copyName)
    {
        final IcyColorMap dstColormap = toRGBmaps[component];

        dstColormap.copyFrom(srcColorMap);

        if (copyName)
            dstColormap.setName(srcColorMap.getName());
    }

    /**
     * Set the toRGB colormap of specified component
     */
    public void copyColormap(int component, IcyColorMap map)
    {
        copyColormap(component, map, false);
    }

    /**
     * Return the fromRGB colormap of specified RGB component
     */
    public FromRGBColorMap getFromRGBMap(int component)
    {
        return fromRGBmaps[component];
    }

    /**
     * Set the toRGB colormaps from a compatible colorModel
     */
    public void copyColormaps(ColorModel cm)
    {
        if (cm instanceof IcyColorModel)
            copyColormaps((IcyColorSpace) cm.getColorSpace());
        else
        {
            // get datatype and numComponent of source colorModel
            final Object srcElem = cm.getDataElements(0x0, null);
            final int srcDataType = TypeUtil.getDataType(srcElem);
            final int srcNumComponents = ArrayUtil.getLength(srcElem);
            final int dataType = cm.getTransferType();
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
                        switch (dataType)
                        {
                            case TypeUtil.TYPE_BYTE:
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

                            case TypeUtil.TYPE_SHORT:
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

                            case TypeUtil.TYPE_INT:
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

                            case TypeUtil.TYPE_FLOAT:
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

                            case TypeUtil.TYPE_DOUBLE:
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
     * Copy colormap from specified colorSpace
     */
    public void copyColormaps(IcyColorSpace source)
    {
        final int numComponents = Math.min(source.getNumComponents(), getNumComponents());

        beginUpdate();
        try
        {
            // copy colormap
            for (int comp = 0; comp < numComponents; comp++)
                copyColormap(comp, source.getColormap(comp));
            // copy alpha indicator
            setAlphaEnabled(source.isAlphaEnabled());
        }
        finally
        {
            endUpdate();
        }
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
        final IcyColorMap colorMap = getColormap(component);

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
        updater.changed(new IcyColorSpaceEvent(this, IcyColorSpaceEventType.CHANGED, component));
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

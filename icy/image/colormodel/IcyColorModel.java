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
package icy.image.colormodel;

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.image.colormap.IcyColorMap;
import icy.image.colormodel.IcyColorModelEvent.IcyColorModelEventType;
import icy.image.colorspace.IcyColorSpace;
import icy.image.colorspace.IcyColorSpaceEvent;
import icy.image.colorspace.IcyColorSpaceListener;
import icy.image.lut.LUT;
import icy.math.Scaler;
import icy.math.ScalerEvent;
import icy.math.ScalerListener;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.type.collection.array.Array1DUtil;

import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public abstract class IcyColorModel extends ColorModel implements ScalerListener, IcyColorSpaceListener, ChangeListener
{
    /**
     * scalers for normalization
     */
    protected final Scaler[] normalScalers;
    /**
     * scalers for colorMap
     */
    protected final Scaler[] colormapScalers;

    /**
     * data type
     */
    protected final DataType dataType;

    /**
     * overridden variables
     */
    protected final int numComponents;
    protected final IcyColorSpace colorSpace;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    /**
     * Default constructor
     */
    IcyColorModel(int numComponents, DataType dataType, int[] bits)
    {
        super(dataType.getBitSize(), bits, new IcyColorSpace(numComponents), true, false, TRANSLUCENT, dataType
                .toDataBufferType());

        if (numComponents == 0)
            throw new IllegalArgumentException("Number of components should be > 0");

        // overridden variable
        this.numComponents = numComponents;
        colorSpace = (IcyColorSpace) getColorSpace();

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // data type information
        this.dataType = dataType;

        // get default min and max for datatype
        final double[] defaultBounds = dataType.getDefaultBounds();
        final double min = defaultBounds[0];
        final double max = defaultBounds[1];
        // float type flag
        final boolean isFloat = dataType.isFloat();

        // allocating scalers
        normalScalers = new Scaler[numComponents];
        colormapScalers = new Scaler[numComponents];
        // defining scalers
        for (int i = 0; i < numComponents; i++)
        {
            // scale for normalization
            normalScalers[i] = new Scaler(min, max, 0f, 1f, !isFloat);
            // scale for colormap
            colormapScalers[i] = new Scaler(min, max, 0f, IcyColorMap.MAX_INDEX, !isFloat);
            // add listener to the colormap scalers only
            colormapScalers[i].addListener(this);
        }

        // add the listener to colorSpace
        colorSpace.addListener(this);
    }

    /**
     * @deprecated use {@link #IcyColorModel(int, DataType, int[])} instead
     */
    @Deprecated
    IcyColorModel(int numComponents, int dataType, boolean signed, int[] bits)
    {
        this(numComponents, DataType.getDataType(dataType, signed), bits);
    }

    /**
     * Creates a new ColorModel with the given color component and image data type
     * 
     * @param numComponents
     *        number of component
     * @param dataType
     *        the type of image data (see {@link DataType})
     * @return a IcyColorModel object
     */
    public static IcyColorModel createInstance(int numComponents, DataType dataType)
    {
        // define bits size
        final int bits = dataType.getBitSize();
        // we have to fake one more extra component for alpha in ColorModel class
        final int numComponentFixed = numComponents + 1;
        final int[] componentBits = new int[numComponentFixed];

        for (int i = 0; i < numComponentFixed; i++)
            componentBits[i] = bits;

        switch (dataType)
        {
            case UBYTE:
                return new UByteColorModel(numComponents, componentBits);
            case BYTE:
                return new ByteColorModel(numComponents, componentBits);
            case USHORT:
                return new UShortColorModel(numComponents, componentBits);
            case SHORT:
                return new ShortColorModel(numComponents, componentBits);
            case UINT:
                return new UIntColorModel(numComponents, componentBits);
            case INT:
                return new IntColorModel(numComponents, componentBits);
            case ULONG:
                return new ULongColorModel(numComponents, componentBits);
            case LONG:
                return new LongColorModel(numComponents, componentBits);
            case FLOAT:
                return new FloatColorModel(numComponents, componentBits);
            case DOUBLE:
                return new DoubleColorModel(numComponents, componentBits);
            default:
                throw new IllegalArgumentException("Unsupported data type !");
        }
    }

    /**
     * @deprecated use {@link #createInstance(int, DataType)} instead
     */
    @Deprecated
    public static IcyColorModel createInstance(int numComponents, int dataType, boolean signed)
    {
        return createInstance(numComponents, DataType.getDataType(dataType, signed));
    }

    /**
     * Creates a new ColorModel from a given icyColorModel
     * 
     * @param colorModel
     *        icyColorModel
     * @param copyColormap
     *        flag to indicate if we want to copy colormaps from the given icyColorModel
     * @param copyBounds
     *        flag to indicate if we want to copy bounds from the given icyColorModel
     * @return a IcyColorModel object
     */
    public static IcyColorModel createInstance(IcyColorModel colorModel, boolean copyColormap, boolean copyBounds)
    {
        final IcyColorModel result = IcyColorModel.createInstance(colorModel.getNumComponents(),
                colorModel.getDataType_());

        result.beginUpdate();
        try
        {
            // copy colormaps from colorModel ?
            if (copyColormap)
                result.setColorMaps(colorModel);
            // copy bounds from colorModel ?
            if (copyBounds)
                result.setBounds(colorModel);
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Create default ColorModel : 4 components, unsigned byte data type
     */
    public static IcyColorModel createInstance()
    {
        return createInstance(4, DataType.UBYTE);
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h)
    {
        return new BandedSampleModel(transferType, w, h, getNumComponents());
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h)
    {
        final SampleModel sm = createCompatibleSampleModel(w, h);

        return Raster.createWritableRaster(sm, sm.createDataBuffer(), null);
    }

    /**
     * Create a writable raster from specified data and size.<br>
     */
    public WritableRaster createWritableRaster(Object[] data, int w, int h)
    {
        final SampleModel sm = createCompatibleSampleModel(w, h);

        switch (dataType)
        {
            case UBYTE:
            case BYTE:
                return Raster.createWritableRaster(sm, new DataBufferByte((byte[][]) data, w * h), null);
            case SHORT:
                return Raster.createWritableRaster(sm, new DataBufferShort((short[][]) data, w * h), null);
            case USHORT:
                return Raster.createWritableRaster(sm, new DataBufferUShort((short[][]) data, w * h), null);
            case UINT:
            case INT:
                return Raster.createWritableRaster(sm, new DataBufferInt((int[][]) data, w * h), null);
            case FLOAT:
                return Raster.createWritableRaster(sm, new DataBufferFloat((float[][]) data, w * h), null);
            case DOUBLE:
                return Raster.createWritableRaster(sm, new DataBufferDouble((double[][]) data, w * h), null);
            default:
                throw new IllegalArgumentException(
                        "IcyColorModel.createWritableRaster(..) error : unsupported data type : " + dataType);
        }
    }

    /**
     * Set bounds from specified {@link IcyColorModel}
     */
    public void setBounds(IcyColorModel source)
    {
        beginUpdate();
        try
        {
            for (int i = 0; i < numComponents; i++)
            {
                final Scaler srcNormalScaler = source.getNormalScalers()[i];
                final Scaler dstNormalScaler = normalScalers[i];
                final Scaler srcColorMapScaler = source.getColormapScalers()[i];
                final Scaler dstColorMapScaler = colormapScalers[i];

                dstNormalScaler.beginUpdate();
                try
                {
                    dstNormalScaler.setAbsLeftRightIn(srcNormalScaler.getAbsLeftIn(), srcNormalScaler.getAbsRightIn());
                    dstNormalScaler.setLeftRightIn(srcNormalScaler.getLeftIn(), srcNormalScaler.getRightIn());
                    dstNormalScaler.setLeftRightOut(srcNormalScaler.getLeftOut(), srcNormalScaler.getRightOut());
                }
                finally
                {
                    dstNormalScaler.endUpdate();
                }

                dstColorMapScaler.beginUpdate();
                try
                {
                    dstColorMapScaler.setAbsLeftRightIn(srcColorMapScaler.getAbsLeftIn(),
                            srcColorMapScaler.getAbsRightIn());
                    dstColorMapScaler.setLeftRightIn(srcColorMapScaler.getLeftIn(), srcColorMapScaler.getRightIn());
                    dstColorMapScaler.setLeftRightOut(srcColorMapScaler.getLeftOut(), srcColorMapScaler.getRightOut());
                }
                finally
                {
                    dstColorMapScaler.endUpdate();
                }
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @deprecated Use {@link #setBounds(IcyColorModel)} instead.
     */
    @Deprecated
    public void copyBounds(IcyColorModel source)
    {
        setBounds(source);
    }

    /**
     * Return the toRGB colormap of specified RGB component
     */
    public IcyColorMap getColorMap(int component)
    {
        return getIcyColorSpace().getColorMap(component);
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
     * Set the toRGB colormaps from a compatible colorModel.
     * 
     * @param source
     *        source ColorModel to copy colormap from
     */
    public void setColorMaps(ColorModel source)
    {
        getIcyColorSpace().setColorMaps(source);
    }

    /**
     * @deprecated Use {@link #setColorMaps(ColorModel)} instead (different case).
     */
    @Deprecated
    public void setColormaps(ColorModel source)
    {
        setColorMaps(source);
    }

    /**
     * @deprecated Use {@link #setColorMaps(ColorModel)} instead.
     */
    @Deprecated
    public void copyColormap(ColorModel source)
    {
        setColorMaps(source);
    }

    /**
     * Set the toRGB colormap of specified component (actually copy the content).
     * 
     * @param component
     *        component we want to set the colormap
     * @param map
     *        source colormap to copy
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMap(int component, IcyColorMap map, boolean setAlpha)
    {
        getIcyColorSpace().setColorMap(component, map, setAlpha);
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
     * @see java.awt.image.ColorModel#getAlpha(int)
     */
    @Override
    public int getAlpha(int pixel)
    {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    /**
     * @see java.awt.image.ColorModel#getBlue(int)
     */
    @Override
    public int getBlue(int pixel)
    {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    /**
     * @see java.awt.image.ColorModel#getGreen(int)
     */
    @Override
    public int getGreen(int pixel)
    {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    /**
     * @see java.awt.image.ColorModel#getRed(int)
     */
    @Override
    public int getRed(int pixel)
    {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    @Override
    public abstract int getRGB(Object inData);

    public abstract int getRGB(Object pixel, LUT lut);

    /**
	 * 
	 */
    @Override
    public int getBlue(Object pixel)
    {
        return getRGB(pixel) & 0xFF;
    }

    /**
	 * 
	 */
    @Override
    public int getGreen(Object pixel)
    {
        return (getRGB(pixel) >> 8) & 0xFF;
    }

    /**
	 * 
	 */
    @Override
    public int getRed(Object pixel)
    {
        return (getRGB(pixel) >> 16) & 0xFF;
    }

    /**
	 * 
	 */
    @Override
    public int getAlpha(Object pixel)
    {
        return (getRGB(pixel) >> 24) & 0xFF;
    }

    /**
     * @see java.awt.image.ColorModel#getComponents(int, int[], int)
     */
    @Override
    public int[] getComponents(int pixel, int[] components, int offset)
    {
        throw new IllegalArgumentException("Not supported in this ColorModel");
    }

    /**
     * @see java.awt.image.ColorModel#getComponents(Object, int[], int)
     */
    @Override
    public abstract int[] getComponents(Object pixel, int[] components, int offset);

    /**
     * @see java.awt.image.ColorModel#getNormalizedComponents(Object, float[], int)
     */
    @Override
    public abstract float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset);

    /**
     * @see java.awt.image.ColorModel#getNormalizedComponents(int[], int, float[], int)
     */
    @Override
    public float[] getNormalizedComponents(int[] components, int offset, float[] normComponents, int normOffset)
    {
        if ((components.length - offset) < numComponents)
            throw new IllegalArgumentException("Incorrect number of components.  Expecting " + numComponents);

        final float[] result = Array1DUtil.allocIfNull(normComponents, numComponents + normOffset);

        for (int i = 0; i < numComponents; i++)
            result[normOffset + i] = (float) normalScalers[i].scale(components[offset + i]);

        return result;
    }

    /**
     * @see java.awt.image.ColorModel#getUnnormalizedComponents(float[], int, int[], int)
     */
    @Override
    public int[] getUnnormalizedComponents(float[] normComponents, int normOffset, int[] components, int offset)
    {
        if ((normComponents.length - normOffset) < numComponents)
            throw new IllegalArgumentException("Incorrect number of components.  Expecting " + numComponents);

        final int[] result = Array1DUtil.allocIfNull(components, numComponents + offset);

        for (int i = 0; i < numComponents; i++)
            result[offset + i] = (int) normalScalers[i].unscale(normComponents[normOffset + i]);

        return result;
    }

    /**
     * @see java.awt.image.ColorModel#getDataElement(int[], int)
     */
    @Override
    public int getDataElement(int[] components, int offset)
    {
        throw new IllegalArgumentException("Not supported in this ColorModel");
    }

    /**
     * @see java.awt.image.ColorModel#getDataElement(float[], int)
     */
    @Override
    public int getDataElement(float[] normComponents, int normOffset)
    {
        throw new IllegalArgumentException("Not supported in this ColorModel");
    }

    /**
     * @see java.awt.image.ColorModel#getDataElements(int[], int, Object)
     */
    @Override
    public abstract Object getDataElements(int[] components, int offset, Object obj);

    /**
     * @see java.awt.image.ColorModel#getDataElements(int, Object)
     */
    @Override
    public Object getDataElements(int rgb, Object pixel)
    {
        return getDataElements(colorSpace.fromRGB(rgb), 0, pixel);
    }

    /**
     * @see java.awt.image.ColorModel#getDataElements(float[], int, Object)
     */
    @Override
    public abstract Object getDataElements(float[] normComponents, int normOffset, Object obj);

    /**
	 * 
	 */
    @Override
    public ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied)
    {
        // nothing to do
        return this;
    }

    /**
     * Scale input value for colormap indexing
     */
    protected double colormapScale(int component, double value)
    {
        return colormapScalers[component].scale(value);
    }

    /**
     * Tests if the specified <code>Object</code> is an instance of <code>ColorModel</code> and if
     * it equals this <code>ColorModel</code>.
     * 
     * @param obj
     *        the <code>Object</code> to test for equality
     * @return <code>true</code> if the specified <code>Object</code> is an instance of
     *         <code>ColorModel</code> and equals this <code>ColorModel</code>; <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IcyColorModel)
            return isCompatible((IcyColorModel) obj);

        return false;
    }

    /**
     * 
     */
    public boolean isCompatible(IcyColorModel cm)
    {
        return (getNumComponents() == cm.getNumComponents()) && (getDataType_() == cm.getDataType_());
    }

    /**
	 * 
	 */
    @Override
    public boolean isCompatibleRaster(Raster raster)
    {
        final SampleModel sm = raster.getSampleModel();
        final int[] bits = getComponentSize();

        if (sm instanceof ComponentSampleModel)
        {
            if (sm.getNumBands() != numComponents)
                return false;

            for (int i = 0; i < bits.length; i++)
                if (sm.getSampleSize(i) < bits[i])
                    return false;

            return (raster.getTransferType() == transferType);
        }

        return false;
    }

    /**
	 * 
	 */
    @Override
    public boolean isCompatibleSampleModel(SampleModel sm)
    {
        // Must have the same number of components
        if (numComponents != sm.getNumBands())
            return false;
        if (sm.getTransferType() != transferType)
            return false;

        return true;
    }

    /**
     * @return the IcyColorSpace
     */
    public IcyColorSpace getIcyColorSpace()
    {
        return colorSpace;
    }

    /**
     * @return the normalScalers
     */
    public Scaler[] getNormalScalers()
    {
        return normalScalers;
    }

    /**
     * @return the colormapScalers
     */
    public Scaler[] getColormapScalers()
    {
        return colormapScalers;
    }

    /**
     * Returns the number of components in this <code>ColorModel</code>.<br>
     * Note that alpha is embedded so we always have NumColorComponent = NumComponent
     * 
     * @return the number of components in this <code>ColorModel</code>
     */
    @Override
    public int getNumComponents()
    {
        return numComponents;
    }

    /**
     * @deprecated use {@link #getDataType_()} instead
     */
    @Deprecated
    public int getDataType()
    {
        return TypeUtil.dataBufferTypeToDataType(transferType);
    }

    /**
     * Return data type for this colormodel
     * 
     * @see DataType
     */
    public DataType getDataType_()
    {
        return dataType;
    }

    /**
     * return default component bounds for this colormodel
     */
    public double[] getDefaultComponentBounds()
    {
        return dataType.getDefaultBounds();
    }

    /**
     * Get component absolute minimum value
     */
    public double getComponentAbsMinValue(int component)
    {
        // use the normal scaler
        return normalScalers[component].getAbsLeftIn();
    }

    /**
     * Get component absolute maximum value
     */
    public double getComponentAbsMaxValue(int component)
    {
        // use the normal scaler
        return normalScalers[component].getAbsRightIn();
    }

    /**
     * Get component absolute bounds (min and max values)
     */
    public double[] getComponentAbsBounds(int component)
    {
        final double[] result = new double[2];

        result[0] = getComponentAbsMinValue(component);
        result[1] = getComponentAbsMaxValue(component);

        return result;
    }

    /**
     * Get component user minimum value
     */
    public double getComponentUserMinValue(int component)
    {
        // use the normal scaler
        return normalScalers[component].getLeftIn();
    }

    /**
     * Get user component user maximum value
     */
    public double getComponentUserMaxValue(int component)
    {
        // use the normal scaler
        return normalScalers[component].getRightIn();
    }

    /**
     * Get component user bounds (min and max values)
     */
    public double[] getComponentUserBounds(int component)
    {
        final double[] result = new double[2];

        result[0] = getComponentUserMinValue(component);
        result[1] = getComponentUserMaxValue(component);

        return result;
    }

    /**
     * Set component absolute minimum value
     */
    public void setComponentAbsMinValue(int component, double min)
    {
        // update both scalers
        normalScalers[component].setAbsLeftIn(min);
        colormapScalers[component].setAbsLeftIn(min);
    }

    /**
     * Set component absolute maximum value
     */
    public void setComponentAbsMaxValue(int component, double max)
    {
        // update both scalers
        normalScalers[component].setAbsRightIn(max);
        colormapScalers[component].setAbsRightIn(max);
    }

    /**
     * Set component absolute bounds (min and max values)
     */
    public void setComponentAbsBounds(int component, double[] bounds)
    {
        setComponentAbsBounds(component, bounds[0], bounds[1]);
    }

    /**
     * Set component absolute bounds (min and max values)
     */
    public void setComponentAbsBounds(int component, double min, double max)
    {
        // update both scalers
        normalScalers[component].setAbsLeftRightIn(min, max);
        colormapScalers[component].setAbsLeftRightIn(min, max);
    }

    /**
     * Set component user minimum value
     */
    public void setComponentUserMinValue(int component, double min)
    {
        // update both scalers
        normalScalers[component].setLeftIn(min);
        colormapScalers[component].setLeftIn(min);
    }

    /**
     * Set component user maximum value
     */
    public void setComponentUserMaxValue(int component, double max)
    {
        // update both scalers
        normalScalers[component].setRightIn(max);
        colormapScalers[component].setRightIn(max);
    }

    /**
     * Set component user bounds (min and max values)
     */
    public void setComponentUserBounds(int component, double[] bounds)
    {
        setComponentUserBounds(component, bounds[0], bounds[1]);
    }

    /**
     * Set component user bounds (min and max values)
     */
    public void setComponentUserBounds(int component, double min, double max)
    {
        // update both scalers
        normalScalers[component].setLeftRightIn(min, max);
        colormapScalers[component].setLeftRightIn(min, max);
    }

    /**
     * Set components absolute bounds (min and max values)
     */
    public void setComponentsAbsBounds(double[][] bounds)
    {
        final int numComponents = getNumComponents();

        if (bounds.length != numComponents)
            throw new IllegalArgumentException("bounds.length != ColorModel.numComponents");

        for (int component = 0; component < numComponents; component++)
            setComponentAbsBounds(component, bounds[component]);
    }

    /**
     * Set components user bounds (min and max values)
     */
    public void setComponentsUserBounds(double[][] bounds)
    {
        final int numComponents = getNumComponents();

        if (bounds.length != numComponents)
            throw new IllegalArgumentException("bounds.length != ColorModel.numComponents");

        for (int component = 0; component < numComponents; component++)
            setComponentUserBounds(component, bounds[component]);
    }

    /**
     * Return true if colorModel is float data type
     */
    public boolean isFloatDataType()
    {
        return dataType.isFloat();
    }

    /**
     * Return true if colorModel data type is signed
     */
    public boolean isSignedDataType()
    {
        return dataType.isSigned();
    }

    /**
     * Return true if color maps associated to this {@link IcyColorModel} are all linear map.
     * 
     * @see IcyColorMap#isLinear()
     */
    public boolean hasLinearColormaps()
    {
        for (int c = 0; c < numComponents; c++)
            if (!getColorMap(c).isLinear())
                return false;

        return true;
    }

    /**
     * Returns the <code>String</code> representation of the contents of this
     * <code>ColorModel</code>object.
     * 
     * @return a <code>String</code> representing the contents of this <code>ColorModel</code>
     *         object.
     */
    @Override
    public String toString()
    {
        return new String("ColorModel: dataType = " + dataType + " numComponents = " + numComponents
                + " color space = " + colorSpace);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(IcyColorModelListener listener)
    {
        listeners.add(IcyColorModelListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(IcyColorModelListener listener)
    {
        listeners.remove(IcyColorModelListener.class, listener);
    }

    /**
     * fire event
     * 
     * @param e
     */
    public void fireEvent(IcyColorModelEvent e)
    {
        for (IcyColorModelListener listener : listeners.getListeners(IcyColorModelListener.class))
            listener.colorModelChanged(e);
    }

    /**
     * process on colormodel change
     */
    @Override
    public void onChanged(EventHierarchicalChecker compare)
    {
        final IcyColorModelEvent event = (IcyColorModelEvent) compare;

        // notify listener we have changed
        fireEvent(event);
    }

    @Override
    public void scalerChanged(ScalerEvent e)
    {
        // only listening colormapScalers
        final int ind = Scaler.indexOf(colormapScalers, e.getScaler());

        // handle changed via updater object
        if (ind != -1)
            updater.changed(new IcyColorModelEvent(this, IcyColorModelEventType.SCALER_CHANGED, ind));
    }

    @Override
    public void colorSpaceChanged(IcyColorSpaceEvent e)
    {
        // handle changed via updater object
        updater.changed(new IcyColorModelEvent(this, IcyColorModelEventType.COLORMAP_CHANGED, e.getComponent()));
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    public void endUpdate()
    {
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }
}

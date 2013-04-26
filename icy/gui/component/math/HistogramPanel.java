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
package icy.gui.component.math;

import icy.gui.component.BorderedPanel;
import icy.math.ArrayMath;
import icy.math.Histogram;
import icy.math.MathUtil;
import icy.type.collection.array.Array1DUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.EventListener;

import javax.swing.BorderFactory;

/**
 * @author Stephane
 */
public class HistogramPanel extends BorderedPanel
{
    public static interface HistogramPanelListener extends EventListener
    {
        /**
         * histogram need to be refreshed (send values for recalculation)
         */
        public void histogramNeedRefresh(HistogramPanel source);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3932807727576675217L;

    protected static final int BORDER_WIDTH = 2;
    protected static final int BORDER_HEIGHT = 2;
    protected static final int MIN_SIZE = 16;

    /**
     * internal histogram
     */
    Histogram histogram;
    /**
     * histogram data cache
     */
    private double[] histogramData;

    /**
     * histogram properties
     */
    double minValue;
    double maxValue;
    boolean integer;

    /**
     * display properties
     */
    boolean logScaling;
    boolean useLAFColors;
    Color color;
    Color backgroundColor;

    /**
     * ratios
     */
    double dataToPixelRatio;
    double pixelToDataRatio;
    double pixelToHistoRatio;

    /**
     * internals
     */
    boolean updating;

    /**
     * Create a new histogram panel for the specified value range.<br>
     * By default it uses a Logarithm representation (modifiable via {@link #setLogScaling(boolean)}
     * 
     * @param minValue
     * @param maxValue
     * @param integer
     */
    public HistogramPanel(double minValue, double maxValue, boolean integer)
    {
        super();

        setBorder(BorderFactory.createEmptyBorder(BORDER_HEIGHT, BORDER_WIDTH, BORDER_HEIGHT, BORDER_WIDTH));

        setMinimumSize(new Dimension(100, 100));
        setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        histogram = new Histogram(0d, 1d, 1, true);
        histogramData = new double[0];

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.integer = integer;

        logScaling = true;
        useLAFColors = true;
        // default drawing color
        color = Color.white;
        backgroundColor = Color.darkGray;

        dataToPixelRatio = 0d;
        pixelToDataRatio = 0d;
        pixelToHistoRatio = 0d;

        buildHistogram();

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                // rebuild histogram
                buildHistogram();
            }
        });

        updating = false;
    }

    /**
     * Reset histogram.<br>
     * Call {@link #done()} when histogram calculation is completed<br>
     * so the panel can refresh its display.
     */
    public void reset()
    {
        histogram.reset();
        // start histogram calculation
        updating = true;
    }

    /**
     * @see icy.math.Histogram#addValue(double)
     */
    public void addValue(double value)
    {
        histogram.addValue(value);
    }

    /**
     * @see icy.math.Histogram#addValues(java.lang.Object, boolean)
     */
    public void addValues(Object array, boolean signed)
    {
        histogram.addValues(array, signed);
    }

    /**
     * @see icy.math.Histogram#addValues(byte[], boolean)
     */
    public void addValues(byte[] array, boolean signed)
    {
        histogram.addValues(array, signed);
    }

    /**
     * @see icy.math.Histogram#addValues(short[], boolean)
     */
    public void addValues(short[] array, boolean signed)
    {
        histogram.addValues(array, signed);
    }

    /**
     * @see icy.math.Histogram#addValues(int[], boolean)
     */
    public void addValues(int[] array, boolean signed)
    {
        histogram.addValues(array, signed);
    }

    /**
     * @see icy.math.Histogram#addValues(long[], boolean)
     */
    public void addValues(long[] array, boolean signed)
    {
        histogram.addValues(array, signed);
    }

    /**
     * @see icy.math.Histogram#addValues(float[])
     */
    public void addValues(float[] array)
    {
        histogram.addValues(array);
    }

    /**
     * @see icy.math.Histogram#addValues(double[])
     */
    public void addValues(double[] array)
    {
        histogram.addValues(array);
    }

    /**
     * Returns the adjusted size (linear / log normalized) of the specified bin.
     * 
     * @see #getBinSize(int)
     */
    public double getAdjustedBinSize(int index)
    {
        if (index < histogramData.length)
            return histogramData[index];

        return 0d;
    }

    /**
     * Returns the size of the specified bin (number of element in the bin)
     * 
     * @see icy.math.Histogram#getBinSize(int)
     */
    public int getBinSize(int index)
    {
        return histogram.getBinSize(index);
    }

    /**
     * @see icy.math.Histogram#getBinNumber()
     */
    public int getBinNumber()
    {
        return histogram.getBinNumber();
    }

    /**
     * @see icy.math.Histogram#getBinWidth()
     */
    public double getBinWidth()
    {
        return histogram.getBinWidth();
    }

    /**
     * @see icy.math.Histogram#getBins()
     */
    public int[] getBins()
    {
        return histogram.getBins();
    }

    /**
     * Return true when {@link #reset()} method has been call without ending {@link #done()}.<br>
     * That mean the histogram is being recomputed.
     */
    public boolean isUpdating()
    {
        return updating;
    }

    /**
     * Invoking this method mean we completed the histogram calculation.
     */
    public void done()
    {
        // end histogram calculation
        updating = false;

        refreshDataCache();
    }

    /**
     * Returns the minimum allowed value of the histogram.
     */
    public double getMinValue()
    {
        return minValue;
    }

    /**
     * Returns the maximum allowed value of the histogram.
     */
    public double getMaxValue()
    {
        return maxValue;
    }

    /**
     * Returns true if the input value are integer values only.<br>
     * This is used to adapt the bin number of histogram..
     */
    public boolean isIntegerType()
    {
        return integer;
    }

    /**
     * Returns true if histogram is displayed with LOG scaling
     */
    public boolean getLogScaling()
    {
        return logScaling;
    }

    /**
     * Returns true if histogram use LAF color scheme.
     * 
     * @see #getColor()
     * @see #getBackgroundColor()
     */
    public boolean getUseLAFColors()
    {
        return useLAFColors;
    }

    /**
     * Returns the drawing color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Returns the background color
     */
    public Color getBackgroundColor()
    {
        return color;
    }

    /**
     * Get histogram object
     */
    public Histogram getHistogram()
    {
        return histogram;
    }

    /**
     * Get computed histogram data
     */
    public double[] getHistogramData()
    {
        return histogramData;
    }

    /**
     * Set minimum, maximum and integer values at once
     */
    public void setMinMaxIntValues(double min, double max, boolean integer)
    {
        if ((minValue != min) || (maxValue != max) || (this.integer != integer))
        {
            minValue = min;
            maxValue = max;
            this.integer = integer;

            buildHistogram();
        }
    }

    /**
     * Set the minimum allowed value of the histogram.
     */
    public void setMinValue(double value)
    {
        if (minValue != value)
        {
            minValue = value;
            buildHistogram();
        }
    }

    /**
     * Set the maximum allowed value of the histogram.
     */
    public void setMaxValue(double value)
    {
        if (minValue != value)
        {
            minValue = value;
            buildHistogram();
        }
    }

    /**
     * Set true if the input value are integer values only.<br>
     * This is used to adapt the bin number of histogram.
     */
    public void setIntegerType(boolean value)
    {
        if (integer != value)
        {
            integer = value;
            buildHistogram();
        }
    }

    /**
     * Set to true to display histogram with LOG scaling (else it uses linear scaling).
     */
    public void setLogScaling(boolean value)
    {
        if (logScaling != value)
        {
            logScaling = value;
            refreshDataCache();
        }
    }

    /**
     * Set to true to use LAF color scheme.
     * 
     * @see #setColor(Color)
     * @see #setBackgroundColor(Color)
     */
    public void setUseLAFColors(boolean value)
    {
        if (useLAFColors != value)
        {
            useLAFColors = value;
            repaint();
        }
    }

    /**
     * Set the drawing color
     */
    public void setColor(Color value)
    {
        if (!color.equals(value))
        {
            color = value;
            if (!useLAFColors)
                repaint();
        }
    }

    /**
     * Set the background color
     */
    public void setBackgroundColor(Color value)
    {
        if (!backgroundColor.equals(value))
        {
            backgroundColor = value;
            if (!useLAFColors)
                repaint();
        }
    }

    protected void buildHistogram()
    {
        // create temporary histogram
        final Histogram newHisto = new Histogram(minValue, maxValue, Math.max(getClientWidth(), MIN_SIZE), integer);

        // histogram properties changed ?
        if (!hasSameProperties(newHisto))
        {
            // set new histogram
            histogram = newHisto;
            // recalculate ratios
            refreshRatios();
            // notify listeners so they can fill it
            fireHistogramNeedRefresh();
        }
        else
            // only recalculate ratios
            refreshRatios();
    }

    /**
     * Return true if specified histogram has same bounds and number of bin than current one
     */
    protected boolean hasSameProperties(Histogram h)
    {
        if (histogram == null)
            return false;

        return (histogram.getBinNumber() == h.getBinNumber()) && (histogram.getMinValue() == h.getMinValue())
                && (histogram.getMaxValue() == h.getMaxValue()) && (histogram.isIntegerType() == h.isIntegerType());
    }

    /**
     * update histogram data cache
     */
    protected void refreshDataCache()
    {
        if (histogram == null)
            return;

        // get histogram data
        final double[] newHistogramData = Array1DUtil.intArrayToDoubleArray(histogram.getBins(), false);

        // we want all values to >= 1
        final double min = ArrayMath.min(newHistogramData);
        MathUtil.add(newHistogramData, min + 1f);
        // log
        if (logScaling)
            MathUtil.log(newHistogramData);
        // normalize data
        MathUtil.normalize(newHistogramData);

        synchronized (histogramData)
        {
            histogramData = newHistogramData;
        }

        // request repaint
        repaint();
    }

    /**
     * Convert a data value to the corresponding pixel position
     */
    public int dataToPixel(double value)
    {
        return (int) Math.round(((value - minValue) * dataToPixelRatio)) + getClientX();
    }

    /**
     * Convert a pixel position to corresponding data value
     */
    public double pixelToData(int value)
    {
        final double data = ((value - getClientX()) * pixelToDataRatio) + minValue;
        return Math.min(Math.max(data, minValue), maxValue);
    }

    /**
     * Convert a pixel position to corresponding bin index
     */
    public int pixelToBin(int value)
    {
        final int index = (int) Math.round((value - getClientX()) * pixelToHistoRatio);
        return Math.min(Math.max(index, 0), histogram.getBinNumber() - 1);
    }

    /**
     * update ratios
     */
    protected void refreshRatios()
    {
        final double histogramRange = histogram.getBinNumber() - 1;
        final double pixelRange = Math.max(getClientWidth() - 1, 32);
        final double dataRange = maxValue - minValue;

        if (dataRange != 0d)
            dataToPixelRatio = pixelRange / dataRange;
        else
            dataToPixelRatio = 0d;

        if (pixelRange != 0d)
        {
            pixelToDataRatio = dataRange / pixelRange;
            pixelToHistoRatio = histogramRange / pixelRange;
        }
        else
        {
            pixelToDataRatio = 0d;
            pixelToHistoRatio = 0d;
        }
    }

    /**
     * Notify all listeners that histogram need to be recomputed
     */
    protected void fireHistogramNeedRefresh()
    {
        for (HistogramPanelListener l : listenerList.getListeners(HistogramPanelListener.class))
            l.histogramNeedRefresh(this);
    }

    public void addListener(HistogramPanelListener l)
    {
        listenerList.add(HistogramPanelListener.class, l);
    }

    public void removeListener(HistogramPanelListener l)
    {
        listenerList.remove(HistogramPanelListener.class, l);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        // always recalculate ratios as width can change before resize event
        refreshRatios();

        final Color fc;
        final Color bc;

        if (useLAFColors)
        {
            fc = getForeground();
            bc = getBackground();
        }
        else
        {
            fc = color;
            bc = backgroundColor;
        }

        final Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(fc);
        g2.setBackground(bc);

        // background color
        if (isOpaque())
            g2.clearRect(0, 0, getWidth(), getHeight());

        // not yet computed
        if (histogramData.length != 0)
        {
            synchronized (histogramData)
            {
                final int histoRange = histogramData.length - 1;
                final int hRange = getClientHeight() - 1;
                final int bottom = getClientY() + hRange;
                final int l = getClientX();
                final int r = l + getClientWidth();
                final double ratio = pixelToHistoRatio;

                for (int i = l; i < r; i++)
                {
                    int index = (int) Math.round((i - l) * ratio);

                    if (index < 0)
                        index = 0;
                    else if (index > histoRange)
                        index = histoRange;

                    g2.drawLine(i, bottom, i, bottom - (int) Math.round(histogramData[index] * hRange));
                }
            }
        }

        if ((histogramData.length == 0) || updating)
        {
            final int x = (getWidth() / 2) - 60;
            final int y = (getHeight() / 2) - 20;

            g2.drawString("computing...", x, y);
        }

        g2.dispose();
    }
}

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
package icy.gui.component;

import java.awt.BorderLayout;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class RangeComponent extends JPanel implements ChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7244476681262628392L;

    final protected JSpinner lowSpinner;
    final protected JSpinner highSpinner;
    final protected RangeSlider slider;

    public RangeComponent(int orientation, double min, double max, double step)
    {
        super();

        lowSpinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
        lowSpinner.setToolTipText("Set low bound");
        highSpinner = new JSpinner(new SpinnerNumberModel(max, min, max, step));
        highSpinner.setToolTipText("Set high bound");
        slider = new RangeSlider(orientation);
        updateSliderModel();

        lowSpinner.addChangeListener(this);
        highSpinner.addChangeListener(this);
        slider.addChangeListener(this);

        setLayout(new BorderLayout());

        if (orientation == SwingConstants.VERTICAL)
        {
            add(lowSpinner, BorderLayout.SOUTH);
            add(slider, BorderLayout.CENTER);
            add(highSpinner, BorderLayout.NORTH);
        }
        else
        {
            add(lowSpinner, BorderLayout.WEST);
            add(slider, BorderLayout.CENTER);
            add(highSpinner, BorderLayout.EAST);
        }

        validate();
    }

    public RangeComponent(double min, double max, double step)
    {
        this(SwingConstants.HORIZONTAL, min, max, step);
    }

    public RangeComponent(int orientation)
    {
        this(orientation, 0d, 100d, 1d);
    }

    public RangeComponent()
    {
        this(SwingConstants.HORIZONTAL, 0d, 100d, 1d);
    }

    public JSpinner getLowSpinner()
    {
        return lowSpinner;
    }

    public JSpinner getHighSpinner()
    {
        return highSpinner;
    }

    public RangeSlider getSlider()
    {
        return slider;
    }

    public SpinnerNumberModel getLowModel()
    {
        return (SpinnerNumberModel) lowSpinner.getModel();
    }

    public SpinnerNumberModel getHighModel()
    {
        return (SpinnerNumberModel) highSpinner.getModel();
    }

    private double getRange()
    {
        return getMax() - getMin();
    }

    private int getSliderRange()
    {
        return slider.getMaximum() - slider.getMinimum();
    }

    private int spinnerToSlider(double value)
    {
        final double spinnerRange = getRange();

        if (spinnerRange == 0)
            return 0;

        return (int) ((value - getMin()) * getSliderRange() / spinnerRange);
    }

    private double sliderToSpinner(int value)
    {
        final int sliderRange = getSliderRange();

        if (sliderRange == 0)
            return 0d;

        return (value * getRange() / sliderRange) + getMin();
    }

    private void updateSliderModel()
    {
        final int sliderRange = (int) Math.round(getRange() / getStep());

        slider.setModel(new DefaultBoundedRangeModel(0, 0, 0, sliderRange));
        slider.setLowerValue(spinnerToSlider(getLow()));
        slider.setUpperValue(spinnerToSlider(getHigh()));
    }

    /**
     * Set the lower and higher range value.
     * 
     * @see #setLow(double)
     * @see #setHigh(double)
     * @see #setMin(double)
     * @see #setMax(double)
     */
    public void setLowHigh(double low, double high)
    {
        getLowModel().setValue(Double.valueOf(low));
        getHighModel().setValue(Double.valueOf(high));
    }

    /**
     * Get the lower range value.
     * 
     * @see #getHigh()
     * @see #getMin()
     */
    public double getLow()
    {
        return getLowModel().getNumber().doubleValue();
    }

    /**
     * Get the higher range value.
     * 
     * @see #getLow()
     * @see #getMax()
     */
    public double getHigh()
    {
        return getHighModel().getNumber().doubleValue();
    }

    /**
     * Set the lower range value.
     * 
     * @see #setHigh(double)
     * @see #setMin(double)
     */
    public void setLow(double value)
    {
        getLowModel().setValue(Double.valueOf(value));
    }

    /**
     * Set the higher range value.
     * 
     * @see #setLow(double)
     * @see #setMax(double)
     */
    public void setHigh(double value)
    {
        getHighModel().setValue(Double.valueOf(value));
    }

    /**
     * Return true if the range use integer number
     */
    public boolean isInteger()
    {
        final SpinnerNumberModel model = getLowModel();
        final Number value = model.getNumber();
        final Number step = model.getStepSize();

        return (value.doubleValue() == value.longValue()) && (step.doubleValue() == step.longValue());
    }

    /**
     * Get range minimum value.
     * 
     * @see #getMax()
     * @see #getLow()
     */
    public double getMin()
    {
        return ((Double) getLowModel().getMinimum()).doubleValue();
    }

    /**
     * Get range minimum value.
     * 
     * @see #getMin()
     * @see #getHigh()
     */
    public double getMax()
    {
        return ((Double) getHighModel().getMaximum()).doubleValue();
    }

    /**
     * Get range step value.
     * 
     * @see #getMin()
     * @see #getMax()
     */
    public double getStep()
    {
        return getLowModel().getStepSize().doubleValue();
    }

    /**
     * Set range bounds and step value.
     * 
     * @see #setMin(double)
     * @see #setMax(double)
     * @see #setStep(double)
     */
    public void setMinMaxStep(double min, double max, double step)
    {
        final double low = Math.max(Math.min(getLow(), max), min);
        final double high = Math.max(Math.min(getHigh(), max), min);

        lowSpinner.setModel(new SpinnerNumberModel(low, min, max, step));
        highSpinner.setModel(new SpinnerNumberModel(high, min, max, step));

        updateSliderModel();
    }

    /**
     * Set range bounds value.
     * 
     * @see #setMin(double)
     * @see #setMax(double)
     * @see #setLow(double)
     * @see #setHigh(double)
     */
    public void setMinMax(double min, double max)
    {
        setMinMaxStep(min, max, getStep());
    }

    /**
     * Set range minimum value.
     * 
     * @see #setMax(double)
     * @see #setLow(double)
     */
    public void setMin(double value)
    {
        setMinMaxStep(value, getMax(), getStep());
    }

    /**
     * Set range maximum value.
     * 
     * @see #setMin(double)
     * @see #setHigh(double)
     */
    public void setMax(double value)
    {
        setMinMaxStep(getMin(), value, getStep());
    }

    /**
     * Set range step value.
     * 
     * @see #setMin(double)
     * @see #setMax(double)
     */
    public void setStep(double value)
    {
        setMinMaxStep(getMin(), getMax(), value);
    }

    /**
     * Set slider visible or not.
     */
    public void setSliderVisible(boolean value)
    {
        if (value)
            add(slider, BorderLayout.CENTER);
        else
            add(new JLabel(" - "), BorderLayout.CENTER);
    }

    @Override
    public void setToolTipText(String text)
    {
        slider.setToolTipText(text);

        super.setToolTipText(text);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        lowSpinner.setEnabled(enabled);
        highSpinner.setEnabled(enabled);
        slider.setEnabled(enabled);

        super.setEnabled(enabled);
    }

    protected void fireChangedEvent(ChangeEvent event)
    {
        for (ChangeListener listener : getListeners(ChangeListener.class))
            listener.stateChanged(event);
    }

    public void addChangeListener(ChangeListener listener)
    {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener)
    {
        listenerList.remove(ChangeListener.class, listener);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        final Object source = e.getSource();
        final double low = getLow();
        final double high = getHigh();

        if (source == lowSpinner)
        {
            slider.setLowerValue(spinnerToSlider(low));

            if (high < low)
                setHigh(low);

            getHighModel().setMinimum(Double.valueOf(low));
        }
        else if (source == highSpinner)
        {
            slider.setUpperValue(spinnerToSlider(high));

            if (low > high)
                setLow(high);

            getLowModel().setMaximum(Double.valueOf(high));
        }
        else if (source == slider)
        {
            setLow(sliderToSpinner(slider.getLowerValue()));
            setHigh(sliderToSpinner(slider.getUpperValue()));
        }

        fireChangedEvent(e);
    }
}

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
package icy.gui.component;

import icy.gui.component.ui.RangeSliderUI;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * <p>
 * Note that RangeSlider makes use of the default BoundedRangeModel, which supports an inner range
 * defined by a value and an extent. The upper value returned by RangeSlider is simply the lower
 * value plus the extent.
 * </p>
 */
public class RangeSlider extends JSlider
{
    /**
     * 
     */
    private static final long serialVersionUID = 2079286476964629269L;

    /**
     * Creates a range slider with the specified orientation and the
     * specified minimum, maximum, initial values and extend.
     * The orientation can be
     * either <code>SwingConstants.VERTICAL</code> or <code>SwingConstants.HORIZONTAL</code>.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data handles any issues that may
     * arise from improperly setting the minimum, initial, and maximum values on the slider. See the
     * {@code BoundedRangeModel} documentation for details.
     * 
     * @param orientation
     *        the orientation of the slider
     * @param min
     *        the minimum value of the slider
     * @param max
     *        the maximum value of the slider
     * @param low
     *        the lower range value of the slider
     * @param high
     *        the higher range value of the slider
     * @throws IllegalArgumentException
     *         if orientation is not one of {@code VERTICAL}, {@code HORIZONTAL}
     * @see BoundedRangeModel
     * @see #setOrientation
     * @see #setMinimum
     * @see #setMaximum
     * @see #setLowerValue
     * @see #setUpperValue
     */
    public RangeSlider(int orientation, int min, int max, int low, int high)
    {
        super(orientation, min, max, low);
        // remove focus as we cannot choose which bound to move
        super.setFocusable(false);
        setExtent(high);
    }

    /**
     * Creates a horizontal range slider using the specified min, max and value.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data handles any issues that may
     * arise from improperly setting the minimum, initial, and maximum values on the slider. See the
     * {@code BoundedRangeModel} documentation for details.
     * 
     * @param min
     *        the minimum value of the slider
     * @param max
     *        the maximum value of the slider
     * @param low
     *        the lower range value of the slider
     * @param high
     *        the higher range value of the slider
     * @see BoundedRangeModel
     * @see #setMinimum
     * @see #setMaximum
     * @see #setLowerValue
     * @see #setUpperValue
     */
    public RangeSlider(int min, int max, int low, int high)
    {
        this(HORIZONTAL, min, max, low, high);
    }

    /**
     * Creates a horizontal range slider using the specified min and max
     * with an initial value equal to the average of the min plus max.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data handles any issues that may
     * arise from improperly setting the minimum and maximum values on the slider. See the
     * {@code BoundedRangeModel} documentation for details.
     * 
     * @param min
     *        the minimum value of the slider
     * @param max
     *        the maximum value of the slider
     * @see BoundedRangeModel
     * @see #setMinimum
     * @see #setMaximum
     */
    public RangeSlider(int min, int max)
    {
        this(HORIZONTAL, min, max, (min + max) / 2, 0);
    }

    /**
     * Creates a range slider using the specified orientation with the
     * range {@code 0} to {@code 100} and an initial value of {@code 50}.
     * The orientation can be
     * either <code>SwingConstants.VERTICAL</code> or <code>SwingConstants.HORIZONTAL</code>.
     * 
     * @param orientation
     *        the orientation of the slider
     * @throws IllegalArgumentException
     *         if orientation is not one of {@code VERTICAL}, {@code HORIZONTAL}
     * @see #setOrientation
     */
    public RangeSlider(int orientation)
    {
        this(orientation, 0, 100, 40, 20);
    }

    /**
     * Creates a horizontal range slider with the range 0 to 100 and
     * an initial value of 50.
     */
    public RangeSlider()
    {
        this(HORIZONTAL, 0, 100, 40, 20);
    }

    @Override
    public void setFocusable(boolean focusable)
    {
        // not focusable
        super.setFocusable(false);
    }

    /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI()
    {
        if (SubstanceLookAndFeel.isCurrentLookAndFeel())
        {
            setUI(new RangeSliderUI(this));
            // Update UI for slider labels. This must be called after updating the
            // UI of the slider. Refer to JSlider.updateUI().
            updateLabelUIs();
        }
        else
            super.updateUI();
    }

    /**
     * Returns the lower value in the range.
     */
    @Override
    public int getValue()
    {
        return super.getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value)
    {
        int oldValue = getValue();
        if (oldValue == value)
            return;

        // Compute new value and extent to maintain upper value.
        int oldExtent = getExtent();
        int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
        int newExtent = oldExtent + oldValue - newValue;

        // Set new value and extent, and fire a single change event.
        getModel().setRangeProperties(newValue, newExtent, getMinimum(), getMaximum(), getValueIsAdjusting());
    }

    /**
     * Returns the lower value in the range.
     */
    public int getLowerValue()
    {
        return getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    public void setLowerValue(int value)
    {
        setValue(value);
    }

    /**
     * Returns the upper value in the range.
     */
    public int getUpperValue()
    {
        return getValue() + getExtent();
    }

    /**
     * Sets the upper value in the range.
     */
    public void setUpperValue(int value)
    {
        // Compute new extent.
        int lowerValue = getValue();
        int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);

        // Set extent to set upper value.
        setExtent(newExtent);
    }
}

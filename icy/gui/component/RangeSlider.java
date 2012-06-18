/**
 * The MIT License
 * Copyright (c) 2010 Ernest Yu. All rights reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * Modified for Icy (Stephane Dallongeville)
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

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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Component to display and modify a numeric (double) value
 * 
 * @author Yoann Le Montagner & Stephane
 */
public class NumberTextField extends IcyTextField
{
    /**
     * 
     */
    private static final long serialVersionUID = 3043750422009529874L;

    /**
     * Listener interface
     */
    public static interface ValueChangeListener
    {
        /**
         * Method triggered when the numeric value in the component changes
         */
        public void valueChanged(double newValue, boolean validate);
    }

    private double _value;
    private List<ValueChangeListener> _listeners;

    /**
     * Constructor
     */
    public NumberTextField()
    {
        super();
        _value = 0;
        _listeners = new ArrayList<ValueChangeListener>();
    }

    /**
     * Add a new listener
     */
    public void addValueListener(ValueChangeListener l)
    {
        _listeners.add(l);
    }

    /**
     * Remove a listener
     */
    public void removeValueListener(ValueChangeListener l)
    {
        _listeners.remove(l);
    }

    /**
     * Retrieve the value as a double
     */
    public double getNumericValue()
    {
        return _value;
    }

    /**
     * Set the value
     */
    public void setNumericValue(double value)
    {
        setText(Double.toString(value));
    }

    protected void valueChanged(boolean validate)
    {
        fireValueChangedEvent(validate);
    }

    @Override
    protected void textChanged(boolean validate)
    {
        super.textChanged(validate);

        double oldValue = _value;

        try
        {
            final String text = getText();
            _value = text.isEmpty() ? 0.0 : Double.parseDouble(text);
            setForeground(Color.BLACK);
        }
        catch (NumberFormatException err)
        {
            setForeground(Color.RED);
        }

        if (validate)
            valueChanged(validate);
        else if (_value != oldValue)
            valueChanged(false);
    }

    /**
     * Fire the value changed event
     */
    private void fireValueChangedEvent(boolean validate)
    {
        for (ValueChangeListener l : _listeners)
            l.valueChanged(_value, validate);
    }
}

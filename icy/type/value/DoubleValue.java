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
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class DoubleValue extends AbstractValue<Double>
{
    public DoubleValue(Double value)
    {
        super(value);
    }

    public DoubleValue(double value)
    {
        this(Double.valueOf(value));
    }

    @Override
    public Double getDefaultValue()
    {
        return Double.valueOf(0d);
    }

    @Override
    public int compareTo(Double d)
    {
        return value.compareTo(d);
    }

    @Override
    public boolean loadFromString(String s)
    {
        // empty string --> default value
        if (StringUtil.isEmpty(s))
        {
            value = getDefaultValue();
            return true;
        }

        try
        {
            value = Double.valueOf(Double.parseDouble(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

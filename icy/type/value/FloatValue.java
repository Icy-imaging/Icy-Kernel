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
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class FloatValue extends AbstractValue<Float>
{
    public FloatValue(Float value)
    {
        super(value);
    }

    public FloatValue(float value)
    {
        this(Float.valueOf(value));
    }

    @Override
    public Float getDefaultValue()
    {
        return Float.valueOf(0f);
    }

    @Override
    public int compareTo(Float f)
    {
        return value.compareTo(f);
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
            value = Float.valueOf(Float.parseFloat(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

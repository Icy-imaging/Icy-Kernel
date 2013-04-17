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
public class BooleanValue extends AbstractValue<Boolean>
{
    public BooleanValue(Boolean value)
    {
        super(value);
    }

    public BooleanValue(boolean value)
    {
        this(Boolean.valueOf(value));
    }

    @Override
    public Boolean getDefaultValue()
    {
        return Boolean.FALSE;
    }

    @Override
    public int compareTo(Boolean b)
    {
        return value.compareTo(b);
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

        // true
        if (s.equalsIgnoreCase(Boolean.TRUE.toString()))
        {
            value = Boolean.TRUE;
            return true;
        }
        // false
        if (s.equalsIgnoreCase(Boolean.FALSE.toString()))
        {
            value = Boolean.FALSE;
            return true;
        }

        return false;
    }
}

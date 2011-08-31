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
package icy.image.colormap;

import java.awt.Color;

/**
 * @author stephane
 */
public class JETColorMap extends IcyColorMap
{
    public JETColorMap()
    {
        super("JET");
        int index;

        beginUpdate();
        try
        {
            index = 0;
            setRGBControlPoint(index, new Color(0f, 0f, 0.5f));

            index += 32;
            setRGBControlPoint(index, new Color(0f, 0f, 1f));

            index += 64;
            setRGBControlPoint(index, new Color(0f, 1f, 1f));

            index += 63;
            setRGBControlPoint(index, new Color(1f, 1f, 0f));

            index += 64;
            setRGBControlPoint(index, new Color(1f, 0f, 0f));

            index += 32;
            setRGBControlPoint(index, new Color(0.5f, 0f, 0f));
        }
        finally
        {
            endUpdate();
        }
    }
}

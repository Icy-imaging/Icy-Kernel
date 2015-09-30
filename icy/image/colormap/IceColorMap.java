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
package icy.image.colormap;

import java.awt.Color;

/**
 * @author Stephane
 */
public class IceColorMap extends IcyColorMap
{
    private static final short[] ice_red = {0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217, 229,
            242, 250, 250, 250, 250, 251, 250, 250, 250, 250, 251, 251, 243, 230};
    private static final short[] ice_green = {156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93, 81,
            87, 92, 97, 95, 93, 93, 90, 85, 69, 64, 54, 47, 35, 19, 0, 4, 0};
    private static final short[] ice_blue = {140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251, 250,
            250, 245, 230, 230, 222, 202, 180, 163, 142, 123, 114, 106, 94, 84, 64, 26, 27};

    public IceColorMap()
    {
        super("Ice");

        beginUpdate();
        try
        {
            final int nColors = ice_red.length;
            final float scale = (float) IcyColorMap.MAX_INDEX / (nColors - 1);

            for (int i = 0; i < nColors; i++)
            {
                final int index = Math.round(i * scale);
                setRGBControlPoint(index, new Color(ice_red[i], ice_green[i], ice_blue[i]));
            }
        }
        finally
        {
            endUpdate();
        }
    }
}

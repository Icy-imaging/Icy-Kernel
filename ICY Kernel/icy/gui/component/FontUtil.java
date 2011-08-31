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
package icy.gui.component;

import java.awt.Font;

/**
 * @author stephane
 */
public class FontUtil
{
    /**
     * Change the Font
     */
    public static Font setName(Font font, String name)
    {
        if (font != null)
            return new Font(name, font.getStyle(), font.getSize());

        return null;
    }

    /**
     * Change the size of Font
     */
    public static Font setSize(Font font, int size)
    {
        if (font != null)
            return font.deriveFont((float) size);

        return null;
    }

    /**
     * Change the style of Font
     */
    public static Font setStyle(Font font, int style)
    {
        if (font != null)
            return font.deriveFont(style);

        return null;
    }

}

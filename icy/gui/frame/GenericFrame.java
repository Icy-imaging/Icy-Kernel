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
package icy.gui.frame;

import java.awt.Dimension;

import javax.swing.JComponent;

/**
 * @author Stephane
 */
public class GenericFrame extends IcyFrame
{
    public GenericFrame(String title, final JComponent component)
    {
        super(title, true, true, false, false);

        final Dimension d = component.getPreferredSize();

        // border
        d.height += 40;
        d.width += 8;
        // minimum wanted size
        d.height = Math.max(d.height, 80);
        d.width = Math.max(d.width, 100);

        setPreferredSize(d);
        add(component);

        pack();
        validate();

        setVisible(true);
    }

    public GenericFrame(JComponent component)
    {
        this(component.getName(), component);
    }
}

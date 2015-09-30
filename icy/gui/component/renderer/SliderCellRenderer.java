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
package icy.gui.component.renderer;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author Stephane
 */
public class SliderCellRenderer extends JSlider implements TableCellRenderer, TreeCellRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -3945777382338073635L;

    public SliderCellRenderer()
    {
        super(0, 1000);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        if (value instanceof Integer)
        {
            final int intValue = ((Integer) value).intValue();

            if (getValue() != intValue)
                setValue(intValue);
        }

        return this;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus)
    {
        if (value instanceof Integer)
        {
            final int intValue = ((Integer) value).intValue();

            if (getValue() != intValue)
                setValue(intValue);
        }

        return this;
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void invalidate()
    {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void validate()
    {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void revalidate()
    {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height)
    {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(Rectangle r)
    {
    }

    /**
     * Overridden for performance reasons.
     * 
     * @since 1.5
     */
    @Override
    public void repaint()
    {
    }
}

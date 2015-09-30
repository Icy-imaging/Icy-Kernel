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
package icy.gui.component.editor;

import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

import sun.swing.DefaultLookup;

/**
 * @author Stephane
 */
public class VisibleCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor
{
    /**
     * 
     */
    private static final long serialVersionUID = -3974658249790735980L;

    protected JLabel label;
    int iconSize;
    boolean visible;

    public VisibleCellEditor(int iconSize)
    {
        label = new JLabel();

        label.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                visible = !visible;
                stopCellEditing();
            }
        });

        this.iconSize = iconSize;
        visible = true;
    }

    @Override
    public Object getCellEditorValue()
    {
        return Boolean.valueOf(visible);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(new IcyIcon(ResourceUtil.ICON_VISIBLE, iconSize));
        else
            label.setIcon(new IcyIcon(ResourceUtil.ICON_NOT_VISIBLE, iconSize));

        if (isSelected)
        {
            label.setForeground(table.getSelectionForeground());
            label.setBackground(table.getSelectionBackground());
        }
        else
        {
            Color background = table.getBackground();
            if (background == null || background instanceof javax.swing.plaf.UIResource)
            {
                Color alternateColor = DefaultLookup.getColor(label, label.getUI(), "Table.alternateRowColor");
                if (alternateColor != null && row % 2 == 0)
                    background = alternateColor;
            }

            label.setForeground(table.getForeground());
            label.setBackground(background);
        }

        return label;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
            boolean leaf, int row)
    {
        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(new IcyIcon(ResourceUtil.ICON_VISIBLE, iconSize));
        else
            label.setIcon(new IcyIcon(ResourceUtil.ICON_NOT_VISIBLE, iconSize));

        label.setForeground(tree.getForeground());
        label.setBackground(tree.getBackground());

        return label;
    }
}

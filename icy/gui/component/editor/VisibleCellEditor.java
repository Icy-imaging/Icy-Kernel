/**
 * 
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

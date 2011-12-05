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
    boolean visible;

    public VisibleCellEditor()
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
        final int h = table.getColumnModel().getColumn(column).getWidth() - 2;

        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(new IcyIcon(ResourceUtil.ICON_VISIBLE, h));
        else
            label.setIcon(new IcyIcon(ResourceUtil.ICON_NOT_VISIBLE, h));

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
        final int h = tree.getRowHeight() - 2;

        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(new IcyIcon(ResourceUtil.ICON_VISIBLE, h));
        else
            label.setIcon(new IcyIcon(ResourceUtil.ICON_NOT_VISIBLE, h));

        label.setForeground(tree.getForeground());
        label.setBackground(tree.getBackground());

        return label;
    }
}

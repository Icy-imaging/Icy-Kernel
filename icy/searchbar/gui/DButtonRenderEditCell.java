package icy.searchbar.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;

public class DButtonRenderEditCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,
        ActionListener
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected,
            final boolean hasFocus, int row, int column)
    {
        if (isSelected)
            return (JButton) value;
        JLabel label = new JLabel();
        SubstanceColorScheme cs = SubstanceColorSchemeUtilities.getColorScheme(table, ComponentState.ENABLED);
        label.setBackground(cs.getTextBackgroundFillColor());
        label.setOpaque(true);
        return label;
    }

    @Override
    public Object getCellEditorValue()
    {
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, final boolean isSelected, int row,
            int column)
    {
        if (isSelected)
        {
            JButton button = (JButton) value;
            button.removeActionListener(this);
            button.addActionListener(this);
            return button;
        }
        JLabel label = new JLabel();
        SubstanceColorScheme cs = SubstanceColorSchemeUtilities.getColorScheme(table, ComponentState.ENABLED);
        label.setBackground(cs.getTextBackgroundFillColor());
        label.setOpaque(true);
        return label;
    }

    @Override
    public boolean isCellEditable(EventObject e)
    {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent)
    {
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        fireEditingStopped();
    }
}

/**
 * 
 */
package icy.gui.component.editor;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

/**
 * @author Stephane
 */
public class SliderCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor, ChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -4946926120641246542L;

    private JSlider slider;

    /**
     * internals
     */
    private JTable table;
    private int row;
    private int column;
    boolean liveUpdate;

    /**
     * Create a SliderEditor for JTable or JTree.
     * 
     * @param liveUpdate
     *        set to true if you want live update on slider change
     */
    public SliderCellEditor(boolean liveUpdate)
    {
        slider = new JSlider(0, 1000);

        slider.addChangeListener(this);
        slider.setRequestFocusEnabled(false);

        table = null;
        this.liveUpdate = liveUpdate;
    }

    @Override
    public Object getCellEditorValue()
    {
        return Integer.valueOf(slider.getValue());
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (slider.getValueIsAdjusting())
        {
            // not very elegant but needed for live update on JTable
            if (liveUpdate && (table != null))
            {
                final int value = slider.getValue();

                if (((Integer) table.getValueAt(row, column)).intValue() != value)
                    table.setValueAt(Integer.valueOf(value), row, column);
            }
        }
        else
            stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        this.table = table;
        this.row = row;
        this.column = column;

        final int intValue = ((Integer) value).intValue();

        if (slider.getValue() != intValue)
            slider.setValue(intValue);

        return slider;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
            boolean leaf, int row)
    {
        final int intValue = ((Integer) value).intValue();

        if (slider.getValue() != intValue)
            slider.setValue(intValue);

        return slider;
    }
}
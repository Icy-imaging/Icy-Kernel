/**
 * 
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
        final int intValue = ((Integer) value).intValue();

        if (getValue() != intValue)
            setValue(intValue);

        return this;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus)
    {
        final int intValue = ((Integer) value).intValue();

        if (getValue() != intValue)
            setValue(intValue);

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

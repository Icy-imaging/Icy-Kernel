package icy.gui.menu.search;

import icy.search.SearchResultProducer;

import java.awt.Component;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

public class SearchProducerTableCellRenderer extends SubstanceDefaultTableCellRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -8677464337382665200L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof SearchResultProducer)
        {
            final SearchResultProducer producer = (SearchResultProducer) value;

            setText("<html><b>" + producer.getName() + "</b>");
            setToolTipText(producer.getTooltipText());
        }
        else
        {
            setText(null);
            setToolTipText(null);
            setIcon(null);
        }
        
        return this;
    }
}

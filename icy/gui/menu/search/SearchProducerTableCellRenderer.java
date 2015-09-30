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
package icy.gui.menu.search;

import icy.gui.util.ComponentUtil;
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

            setText(producer.getName());
            ComponentUtil.setFontBold(this);
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

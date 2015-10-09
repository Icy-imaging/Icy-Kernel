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

import icy.image.ImageUtil;
import icy.search.SearchResult;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.ColorUIResource;

import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;

/**
 * This class is a renderer to display the filtered data.
 * 
 * @author Thomas Provoost & Stephane
 */
public class SearchResultTableCellRenderer extends SubstanceDefaultTableCellRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -6758382699884570205L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof SearchResult)
        {
            final SearchResult result = (SearchResult) value;

            final String title = result.getTitle();
            final String description = result.getDescription();
            final Image img = result.getImage();
            final int cellWidth = (int) (table.getCellRect(row, column, false).width * 0.90);
            String text;

            if (img != null)
                setIcon(new ImageIcon(ImageUtil.scale(img, 32, 32)));
            else
                setIcon(null);

            if (StringUtil.isEmpty(title))
                text = "<b>Unknow</b>";
            else
                text = "<b>" + title + "</b>";
            if (!StringUtil.isEmpty(description))
                text += "<br>" + GraphicsUtil.limitStringFor(table, description, cellWidth);
            setText("<html>" + text);

            setToolTipText(result.getTooltip());
            setVerticalAlignment(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);

            // override enabled state
            if (!result.isEnabled())
            {
                final ComponentState state;

                if (isSelected)
                    state = ComponentState.DISABLED_SELECTED;
                else
                    state = ComponentState.DISABLED_UNSELECTED;

                final SubstanceColorScheme colorScheme = SubstanceColorSchemeUtilities.getColorScheme(table, state);

                // modify foreground
                setForeground(new ColorUIResource(colorScheme.getForegroundColor()));
                // disable result
                setEnabled(false);
            }
            else
                setEnabled(table.isEnabled());
        }

        return this;
    }
}

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
                text = "Unknow";
            else
                text = title;
            if (!StringUtil.isEmpty(description))
                text = "<b>" + text + "</b><br>" + GraphicsUtil.limitStringFor(table, description, cellWidth);
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

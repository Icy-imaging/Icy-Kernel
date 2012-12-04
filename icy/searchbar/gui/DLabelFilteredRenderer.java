package icy.searchbar.gui;

import icy.searchbar.SearchBar;
import icy.searchbar.common.TextUtil;
import icy.searchbar.interfaces.SBLink;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;

/**
 * This class is a renderer to display the filtered data.
 * 
 * @author Thomas Provoost
 */
public class DLabelFilteredRenderer extends DefaultTableCellRenderer
{

    private SearchBar filter;
    private SBLink flink;
    private boolean isSelected;
    SubstanceColorScheme cs;

    public DLabelFilteredRenderer(SearchBar filter)
    {
        this.filter = filter;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object ovalue, boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        this.flink = (SBLink) ovalue;
        this.isSelected = isSelected;

        String labelValue = flink.getLabel();
        String filterText = filter.getText();

        labelValue = "<html>" + TextUtil.highlight(labelValue, filterText) + "</html>";
        setText(labelValue);

        if (isSelected)
        {
            cs = SubstanceLookAndFeel.getCurrentSkin(this).getColorScheme(new JMenuItem(),
                    ColorSchemeAssociationKind.HIGHLIGHT, ComponentState.SELECTED);
        }
        else if (hasFocus)
        {
            cs = SubstanceColorSchemeUtilities.getColorScheme(table, ComponentState.SELECTED);
        }
        else
        {
            cs = SubstanceColorSchemeUtilities.getColorScheme(table, ComponentState.ENABLED);
        }
        setBackground(cs.getTextBackgroundFillColor());
        setForeground(cs.getForegroundColor());
        return this;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (!isSelected)
        {
            super.paintComponent(g);
        }
        else
        {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            Color c1 = getBackground();
            Color c2 = cs.getBackgroundFillColor();
            // Paint a gradient from top to bottom
            g2.setColor(c1);
            g2.fillRect(0, 0, w, h / 2);

            g2.setColor(c2);
            g2.fillRect(0, h / 2, w, h);

            setOpaque(false);
            super.paintComponent(g2);

            if (flink != null && flink.getProvider() != null)
            {
                FontMetrics fm = g.getFontMetrics();
                g2.setFont(g.getFont().deriveFont(Font.BOLD, 8));

                // calculates necessary size
                String tooltip = flink.getProvider().getTooltipText();
                int txtW = fm.charsWidth(tooltip.toCharArray(), 0, tooltip.length());

                Color cTxt = cs.getForegroundColor();
                cTxt = new Color(cTxt.getRed(), cTxt.getGreen(), cTxt.getBlue(), 150);
                g2.setColor(cTxt);
                g2.drawString(tooltip, w / 2 - txtW / 2, h - 2);
            }
            g2.dispose();
            setOpaque(true);
        }
    }
}

package icy.searchbar.gui;

import java.awt.Color;
import java.awt.Component;
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

public class DLabelRenderer extends DefaultTableCellRenderer
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private SubstanceColorScheme cs;
    private boolean isSelected;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        this.isSelected = isSelected;
        setText((String) value);
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
        Color background = cs.getTextBackgroundFillColor();
        setBackground(background);
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
            super.paintComponent(g);
            setOpaque(true);
            g2.dispose();
        }
    }
}

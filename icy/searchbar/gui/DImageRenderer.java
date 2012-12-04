package icy.searchbar.gui;

import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;

public class DImageRenderer extends DefaultTableCellRenderer
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private boolean isSelected;

    private SubstanceColorScheme cs;

    public static final int IMAGE_SIZE = 40;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        this.isSelected = isSelected;

        BufferedImage img = (BufferedImage) value;
        if (img != null)
        {
            IcyBufferedImage iimg = IcyBufferedImage.createFrom(img);
            // if (isSelected)
            // iimg = IcyBufferedImageUtil.scale(iimg, IMAGE_SIZE * 2,
            // IMAGE_SIZE * 2);
            // else
            iimg = IcyBufferedImageUtil.scale(iimg, IMAGE_SIZE, IMAGE_SIZE);
            setIcon(new ImageIcon(iimg));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        else
        {
            setIcon(null);
        }
        if (isSelected)
        {
            cs = SubstanceLookAndFeel.getCurrentSkin(this).getColorScheme(new JMenuItem(),
                    ColorSchemeAssociationKind.HIGHLIGHT, ComponentState.SELECTED);
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

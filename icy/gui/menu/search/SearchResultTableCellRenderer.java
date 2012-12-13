package icy.gui.menu.search;

import icy.image.ImageUtil;
import icy.search.SearchResult;
import icy.util.StringUtil;

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

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
                text = "<b>" + text + "</b><br>" + description;
            setText("<html>" + text);

            setToolTipText(result.getTooltip());
            setVerticalAlignment(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);
            setEnabled(result.isEnabled());
        }

        return this;
    }
    // @Override
    // protected void paintComponent(Graphics g)
    // {
    // super.paintComponent(g);
    //
    // if (!isSelected)
    // {
    // super.paintComponent(g);
    // }
    // else
    // {
    // int w = getWidth();
    // int h = getHeight();
    // Graphics2D g2 = (Graphics2D) g.create();
    // Color c1 = getBackground();
    // Color c2 = cs.getBackgroundFillColor();
    // // Paint a gradient from top to bottom
    // g2.setColor(c1);
    // g2.fillRect(0, 0, w, h / 2);
    //
    // g2.setColor(c2);
    // g2.fillRect(0, h / 2, w, h);
    //
    // setOpaque(false);
    // super.paintComponent(g2);
    //
    // if (flink != null && flink.getProducer() != null)
    // {
    // FontMetrics fm = g.getFontMetrics();
    // g2.setFont(g.getFont().deriveFont(Font.BOLD, 8));
    //
    // // calculates necessary size
    // String tooltip = flink.getProducer().getTooltipText();
    // int txtW = fm.charsWidth(tooltip.toCharArray(), 0, tooltip.length());
    //
    // Color cTxt = cs.getForegroundColor();
    // cTxt = new Color(cTxt.getRed(), cTxt.getGreen(), cTxt.getBlue(), 150);
    // g2.setColor(cTxt);
    // g2.drawString(tooltip, w / 2 - txtW / 2, h - 2);
    // }
    // g2.dispose();
    // setOpaque(true);
    // }
    // }
}

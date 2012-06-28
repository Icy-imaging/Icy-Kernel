package icy.gui.component;

import icy.gui.util.LookAndFeelUtil;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.util.ColorUtil;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class ThumbnailComponent extends JPanel implements MouseListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 6742578649112198581L;

    // GUI
    private TitledBorder border;
    private JLabel imageComp;
    private JLabel titleLabel;
    private JLabel infosLabel;
    private JLabel infos2Label;

    // state
    private boolean selected;
    private boolean focused;
    private JPanel panel;
    private JSeparator separator;
    private JPanel panel_1;

    /**
     * Create the panel.
     */
    public ThumbnailComponent()
    {
        super();

        initialize();

        panel.setPreferredSize(new Dimension(160, 140));

        addMouseListener(this);

        selected = false;
        focused = false;
    }

    private void initialize()
    {
        border = BorderFactory.createTitledBorder("");
        setBorder(border);

        setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        imageComp = new JLabel();
        imageComp.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(imageComp);

        add(panel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout(0, 0));

        separator = new JSeparator();
        southPanel.add(separator, BorderLayout.NORTH);

        add(southPanel, BorderLayout.SOUTH);

        panel_1 = new JPanel();
        southPanel.add(panel_1);
        panel_1.setLayout(new GridLayout(0, 1, 0, 0));

        titleLabel = new JLabel();
        panel_1.add(titleLabel);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText("title");
        titleLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontBold(titleLabel);
        infosLabel = new JLabel();
        panel_1.add(infosLabel);
        infosLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infosLabel.setText("info");
        infosLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontSize(infosLabel, 11);
        infos2Label = new JLabel();
        panel_1.add(infos2Label);
        infos2Label.setHorizontalAlignment(SwingConstants.CENTER);
        infos2Label.setText("info 2");
        infos2Label.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontSize(infos2Label, 11);
    }

    public void setImage(Image img)
    {
        final float ix = img.getWidth(null);
        final float iy = img.getHeight(null);

        if ((ix != 0f) && (iy != 0f))
        {
            final float sx = imageComp.getWidth() / ix;
            final float sy = imageComp.getHeight() / iy;
            final float s = Math.min(sx, sy);

            imageComp.setIcon(ResourceUtil.getImageIcon(ImageUtil
                    .scaleImageQuality(img, (int) (ix * s), (int) (iy * s))));
        }
        else
            imageComp.setIcon(ResourceUtil.getImageIcon(ResourceUtil.ICON_DELETE));
    }

    /**
     * @return the selected
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * @param value
     *        the selected to set
     */
    public void setSelected(boolean value)
    {
        if (selected != value)
        {
            selected = value;
            repaint();
        }
    }

    public boolean isFocused()
    {
        return focused;
    }

    private void setFocused(boolean value)
    {
        if (focused != value)
        {
            focused = value;
            repaint();
        }
    }

    public String getTitle()
    {
        return titleLabel.getText();
    }

    public String getInfos()
    {
        return infosLabel.getText();
    }

    public String getInfos2()
    {
        return infos2Label.getText();
    }

    public void setTitle(String value)
    {
        titleLabel.setText(value);
    }

    public void setInfos(String value)
    {
        infosLabel.setText(value);
    }

    public void setInfos2(String value)
    {
        infos2Label.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (focused)
        {
            final Graphics2D g2 = (Graphics2D) g.create();
            final int w = getWidth();
            final int h = getHeight();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            g2.setColor(LookAndFeelUtil.getForeground(this));
            g2.fillRect(4, 4, w - 8, h - 8);

            g2.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g)
    {
        super.paintBorder(g);

        if (selected)
        {
            final Graphics2D g2 = (Graphics2D) g.create();
            final int w = getWidth();
            final int h = getHeight();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setColor(ColorUtil.mix(Color.cyan, LookAndFeelUtil.getForeground(this)));
            g2.drawRect(0, 0, w - 1, h - 1);
            g2.drawRect(1, 1, w - 3, h - 3);
            g2.drawRect(2, 2, w - 5, h - 5);
            g2.drawRect(3, 3, w - 7, h - 7);

            g2.dispose();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        setSelected(!isSelected());
    }

    @Override
    public void mousePressed(MouseEvent e)
    {

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        setFocused(true);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        setFocused(false);
    }

}

package icy.gui.component;

import icy.image.ImageUtil;
import icy.resource.ResourceUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class ThumbnailComponent extends JToggleButton
{
    /**
     * 
     */
    private static final long serialVersionUID = 6742578649112198581L;

    // GUI
    private JLabel imageComp;
    private JLabel titleLabel;
    private JLabel infosLabel;
    private JLabel infos2Label;

    /**
     * Create the thumbnail.
     * 
     * @param selectable
     *        If true then the thumbnail component can be selected as a toggle button.
     */
    public ThumbnailComponent(boolean selectable)
    {
        super();

        initialize();

        setEnabled(selectable);
    }

    private void initialize()
    {
        setLayout(new BorderLayout());

        imageComp = new JLabel();
        imageComp.setHorizontalAlignment(SwingConstants.CENTER);
        imageComp.setOpaque(false);
        imageComp.setPreferredSize(new Dimension(160, 140));

        add(imageComp, BorderLayout.CENTER);

        final JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setLayout(new GridLayout(0, 1, 0, 0));

        titleLabel = new JLabel();
        southPanel.add(titleLabel);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText("");
        titleLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontBold(titleLabel);
        infosLabel = new JLabel();
        southPanel.add(infosLabel);
        infosLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infosLabel.setText("");
        infosLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontSize(infosLabel, 11);
        infos2Label = new JLabel();
        southPanel.add(infos2Label);
        infos2Label.setHorizontalAlignment(SwingConstants.CENTER);
        infos2Label.setText("");
        infos2Label.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontSize(infos2Label, 11);

        add(southPanel, BorderLayout.SOUTH);
    }

    public void setImage(Image img)
    {
        if (img == null)
        {
            imageComp.setIcon(null);
            return;
        }

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

}

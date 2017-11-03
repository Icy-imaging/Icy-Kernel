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
package icy.gui.component;

import icy.gui.util.ComponentUtil;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

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
    private ImageComponent imageComp;
    private JLabel titleLabel;
    private JLabel infosLabel;
    private JLabel infos2Label;
    private boolean shortDisplay;

    /**
     * Create the thumbnail.
     * 
     * @param selectable
     *        If true then the thumbnail component can be selected as a toggle button.
     */
    public ThumbnailComponent(boolean selectable)
    {
        super();

        shortDisplay = false;

        setMinimumSize(new Dimension(120, 12));
        setPreferredSize(new Dimension(160, 160));

        initialize();

        setEnabled(selectable);
    }

    private void initialize()
    {
        setMargin(new Insets(2, 2, 2, 2));
        setLayout(new BorderLayout());

        imageComp = new ImageComponent();
        add(imageComp, BorderLayout.CENTER);

        final JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        GridBagLayout gbl_southPanel = new GridBagLayout();
        gbl_southPanel.columnWidths = new int[] {0, 0};
        gbl_southPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_southPanel.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gbl_southPanel.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        southPanel.setLayout(gbl_southPanel);

        titleLabel = new JLabel();
        GridBagConstraints gbc_titleLabel = new GridBagConstraints();
        gbc_titleLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_titleLabel.insets = new Insets(0, 0, 0, 0);
        gbc_titleLabel.gridx = 0;
        gbc_titleLabel.gridy = 0;
        southPanel.add(titleLabel, gbc_titleLabel);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText(" ");
        titleLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontBold(titleLabel);
        infosLabel = new JLabel();
        GridBagConstraints gbc_infosLabel = new GridBagConstraints();
        gbc_infosLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_infosLabel.insets = new Insets(0, 0, 0, 0);
        gbc_infosLabel.gridx = 0;
        gbc_infosLabel.gridy = 1;
        southPanel.add(infosLabel, gbc_infosLabel);
        infosLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infosLabel.setText(" ");
        infosLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontSize(infosLabel, 11);
        infos2Label = new JLabel();
        GridBagConstraints gbc_infos2Label = new GridBagConstraints();
        gbc_infos2Label.fill = GridBagConstraints.HORIZONTAL;
        gbc_infos2Label.gridx = 0;
        gbc_infos2Label.gridy = 2;
        southPanel.add(infos2Label, gbc_infos2Label);
        infos2Label.setHorizontalAlignment(SwingConstants.CENTER);
        infos2Label.setText(" ");
        infos2Label.setHorizontalTextPosition(SwingConstants.LEADING);
        ComponentUtil.setFontSize(infos2Label, 11);

        add(southPanel, BorderLayout.SOUTH);
    }

    public void setImage(Image img)
    {
        if (img == null)
        {
            imageComp.setImage(null);
            return;
        }

        Image image = img;

        // be sure image data are ready
        ImageUtil.waitImageReady(image);

        float ix = image.getWidth(null);
        float iy = image.getHeight(null);

        if ((ix <= 0f) || (iy <= 0f))
        {
            image = ResourceUtil.ICON_DELETE;
            ix = image.getWidth(null);
            iy = image.getHeight(null);
        }

        if ((imageComp.getWidth() != 0) && (imageComp.getHeight() != 0))
        {
            final float sx = imageComp.getWidth() / ix;
            final float sy = imageComp.getHeight() / iy;
            final float s = Math.min(sx, sy);
            final int w = (int) (ix * s);
            final int h = (int) (iy * s);

            if ((w > 0) && (h > 0))
                image = ImageUtil.scaleQuality(img, w, h);
        }

        imageComp.setImage(image);
    }

    /**
     * @return the shortDisplay property
     * @see #setShortDisplay(boolean)
     */
    public boolean getShortDisplay()
    {
        return shortDisplay;
    }

    /**
     * When set to true, only 'infos' is visible otherwise title, infos and infos2 are all visible
     */
    public void setShortDisplay(boolean value)
    {
        if (shortDisplay != value)
        {
            shortDisplay = value;

            titleLabel.setVisible(!value);
            infos2Label.setVisible(!value);
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
}

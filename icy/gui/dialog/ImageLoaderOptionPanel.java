/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.gui.dialog;

import icy.gui.component.ThumbnailComponent;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.resource.ResourceUtil;
import icy.type.DataType;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import loci.formats.ImageReader;

public class ImageLoaderOptionPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 4180367632912879286L;

    private ThumbnailComponent preview;
    private JPanel separateSeqPanel;
    private JCheckBox separateSeqCheck;
    private JLabel lblAutoDimension;
    private JCheckBox autoOrderCheck;

    // internals
    private boolean autoOrderEnable;

    /**
     * Create the panel.
     */
    public ImageLoaderOptionPanel(boolean separate, boolean autoOrder)
    {
        super();

        autoOrderEnable = true;
        initialize(separate, autoOrder);
    }

    private void initialize(boolean separate, boolean autoOrder)
    {
        setBorder(BorderFactory.createTitledBorder((Border) null));
        setLayout(new BorderLayout());

        preview = new ThumbnailComponent(false);

        add(preview, BorderLayout.CENTER);

        separateSeqPanel = new JPanel();
        add(separateSeqPanel, BorderLayout.SOUTH);
        GridBagLayout gbl_separateSeqPanel = new GridBagLayout();
        gbl_separateSeqPanel.columnWidths = new int[] {135, 21, 0};
        gbl_separateSeqPanel.rowHeights = new int[] {21, 0, 0};
        gbl_separateSeqPanel.columnWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        gbl_separateSeqPanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        separateSeqPanel.setLayout(gbl_separateSeqPanel);
        GridBagConstraints gbc_lblLoadInSeparated = new GridBagConstraints();
        gbc_lblLoadInSeparated.anchor = GridBagConstraints.WEST;
        gbc_lblLoadInSeparated.insets = new Insets(0, 0, 5, 5);
        gbc_lblLoadInSeparated.gridx = 0;
        gbc_lblLoadInSeparated.gridy = 0;
        JLabel lblLoadInSeparated = new JLabel("Load in separated sequences");
        lblLoadInSeparated.setToolTipText("All images are opened in its own sequence");
        separateSeqPanel.add(lblLoadInSeparated, gbc_lblLoadInSeparated);

        // setting GUI
        separateSeqCheck = new JCheckBox();
        lblLoadInSeparated.setLabelFor(separateSeqCheck);
        separateSeqCheck.setSelected(separate);
        separateSeqCheck.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateAutoOrderEnableState();
            }
        });
        GridBagConstraints gbc_separateSeqCheck = new GridBagConstraints();
        gbc_separateSeqCheck.insets = new Insets(0, 0, 0, 0);
        gbc_separateSeqCheck.anchor = GridBagConstraints.NORTHWEST;
        gbc_separateSeqCheck.gridx = 1;
        gbc_separateSeqCheck.gridy = 0;
        separateSeqPanel.add(separateSeqCheck, gbc_separateSeqCheck);

        lblAutoDimension = new JLabel("Automatic ordering");
        lblAutoDimension.setToolTipText("Try to automatically set Z, T, C position of an image from their file name");
        GridBagConstraints gbc_lblAutoDimension = new GridBagConstraints();
        gbc_lblAutoDimension.anchor = GridBagConstraints.WEST;
        gbc_lblAutoDimension.insets = new Insets(0, 0, 0, 5);
        gbc_lblAutoDimension.gridx = 0;
        gbc_lblAutoDimension.gridy = 1;
        separateSeqPanel.add(lblAutoDimension, gbc_lblAutoDimension);

        autoOrderCheck = new JCheckBox("");
        lblAutoDimension.setLabelFor(autoOrderCheck);
        autoOrderCheck.setSelected(autoOrder);
        GridBagConstraints gbc_autoOrderCheck = new GridBagConstraints();
        gbc_autoOrderCheck.gridx = 1;
        gbc_autoOrderCheck.gridy = 1;

        separateSeqPanel.add(autoOrderCheck, gbc_autoOrderCheck);
    }

    void updateAutoOrderEnableState()
    {
        autoOrderCheck.setEnabled(autoOrderEnable && !isSeparateSequenceSelected());
    }

    ThumbnailComponent getPreview()
    {
        return preview;
    }

    public void setSeparateSequenceEnabled(boolean value)
    {
        separateSeqCheck.setEnabled(value);
    }

    public void setAutoOrderEnabled(boolean value)
    {
        autoOrderEnable = value;
        updateAutoOrderEnableState();
    }

    public boolean isSeparateSequenceSelected()
    {
        return separateSeqCheck.isSelected();
    }

    public boolean isAutoOrderSelected()
    {
        return autoOrderCheck.isSelected();
    }

    public void updatePreview(String fileId)
    {
        preview.setImage(null);
        preview.setTitle("loading...");
        preview.setInfos("");
        preview.setInfos2("");

        final ImageReader reader = new ImageReader();

        try
        {
            reader.setId(fileId);
            reader.setSeries(0);

            final int sizeC = reader.getSizeC();

            final IcyBufferedImage img = IcyBufferedImage.createThumbnailFrom(reader, reader.getSizeZ() / 2,
                    reader.getSizeT() / 2);
            preview.setImage(IcyBufferedImageUtil.getARGBImage(img));
            preview.setTitle(reader.getFormat());
            preview.setInfos(reader.getSizeX() + " x " + reader.getSizeY() + " - " + reader.getSizeZ() + "Z x "
                    + reader.getSizeT() + "T");
            preview.setInfos2(sizeC + ((sizeC > 1) ? " channels (" : " channel (")
                    + DataType.getDataTypeFromFormatToolsType(reader.getPixelType()) + ")");
        }
        catch (Exception e)
        {
            // error image, we just totally ignore error here...
            preview.setImage(ResourceUtil.ICON_DELETE);
            preview.setTitle("Cannot read file");
            preview.setInfos("");
            preview.setInfos2("");
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }
}

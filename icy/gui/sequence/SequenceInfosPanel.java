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
package icy.gui.sequence;

import icy.gui.component.IcyTextField;
import icy.gui.component.button.IcyButton;
import icy.gui.frame.GenericFrame;
import icy.gui.main.ActiveSequenceListener;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class SequenceInfosPanel extends JPanel implements ActiveSequenceListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -6123324347914804260L;

    // GUI
    private JLabel dimensionLabel;
    private JLabel resXLabel;
    private JLabel resYLabel;
    private JLabel resZLabel;
    private JLabel resTLabel;
    private JLabel sizeLabel;
    private JLabel channelLabel;

    private IcyButton editBtn;
    private IcyButton detailBtn;

    private JLabel pathLabel;
    private IcyTextField pathField;
    private IcyTextField nameField;

    // internals
    private final Runnable infosRefresher;

    public SequenceInfosPanel()
    {
        super();

        initialize();

        editBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // it should be the current focused sequence
                final Sequence seq = Icy.getMainInterface().getActiveSequence();

                if (seq != null)
                    new SequencePropertiesDialog(seq);
            }
        });

        detailBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // it should be the current focused sequence
                final Sequence seq = Icy.getMainInterface().getActiveSequence();

                if (seq != null)
                {
                    final GenericFrame g = new GenericFrame(seq.getName() + " - Metadata", new SequenceMetadataPanel(
                            seq));

                    g.addToDesktopPane();
                    g.center();
                    g.requestFocus();
                }
            }
        });

        infosRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateInfosInternal(Icy.getMainInterface().getActiveSequence());
                    }
                });
            }
        };

        updateInfosInternal(null);
    }

    public void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 40, 40, 40, 0};
        gridBagLayout.rowHeights = new int[] {18, 0, 18, 18, 18, 18, 18, 18, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JLabel lbl_name = new JLabel("Name");
        lbl_name.setToolTipText("Sequence name");
        GridBagConstraints gbc_lbl_name = new GridBagConstraints();
        gbc_lbl_name.anchor = GridBagConstraints.WEST;
        gbc_lbl_name.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_name.insets = new Insets(0, 0, 2, 5);
        gbc_lbl_name.gridx = 0;
        gbc_lbl_name.gridy = 0;
        add(lbl_name, gbc_lbl_name);

        nameField = new IcyTextField();
        nameField.setOpaque(false);
        nameField.setBorder(null);
        nameField.setEditable(false);
        GridBagConstraints gbc_nameField = new GridBagConstraints();
        gbc_nameField.anchor = GridBagConstraints.WEST;
        gbc_nameField.gridwidth = 3;
        gbc_nameField.insets = new Insets(0, 0, 2, 5);
        gbc_nameField.gridx = 1;
        gbc_nameField.gridy = 0;
        add(nameField, gbc_nameField);

        pathLabel = new JLabel("Path");
        pathLabel.setToolTipText("Sequence file path");
        GridBagConstraints gbc_pathLabel = new GridBagConstraints();
        gbc_pathLabel.fill = GridBagConstraints.VERTICAL;
        gbc_pathLabel.anchor = GridBagConstraints.WEST;
        gbc_pathLabel.insets = new Insets(0, 0, 2, 5);
        gbc_pathLabel.gridx = 0;
        gbc_pathLabel.gridy = 1;
        add(pathLabel, gbc_pathLabel);

        pathField = new IcyTextField();
        pathField.setOpaque(false);
        pathField.setBorder(null);
        pathField.setEditable(false);
        GridBagConstraints gbc_pathField = new GridBagConstraints();
        gbc_pathField.anchor = GridBagConstraints.WEST;
        gbc_pathField.gridwidth = 3;
        gbc_pathField.insets = new Insets(0, 0, 2, 0);
        gbc_pathField.gridx = 1;
        gbc_pathField.gridy = 1;
        add(pathField, gbc_pathField);

        JLabel lbl_dim = new JLabel("Dimension");
        lbl_dim.setToolTipText("Size of X, Y, Z and T dimension");
        GridBagConstraints gbc_lbl_dim = new GridBagConstraints();
        gbc_lbl_dim.anchor = GridBagConstraints.WEST;
        gbc_lbl_dim.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_dim.insets = new Insets(0, 0, 2, 5);
        gbc_lbl_dim.gridx = 0;
        gbc_lbl_dim.gridy = 2;
        add(lbl_dim, gbc_lbl_dim);

        dimensionLabel = new JLabel();
        dimensionLabel.setText("---");
        GridBagConstraints gbc_dimensionLabel = new GridBagConstraints();
        gbc_dimensionLabel.gridwidth = 3;
        gbc_dimensionLabel.fill = GridBagConstraints.BOTH;
        gbc_dimensionLabel.insets = new Insets(0, 0, 2, 0);
        gbc_dimensionLabel.gridx = 1;
        gbc_dimensionLabel.gridy = 2;
        add(dimensionLabel, gbc_dimensionLabel);

        JLabel lbl_channel = new JLabel("Channel");
        lbl_channel.setToolTipText("Number of channel - data type");
        GridBagConstraints gbc_lbl_channel = new GridBagConstraints();
        gbc_lbl_channel.anchor = GridBagConstraints.WEST;
        gbc_lbl_channel.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_channel.insets = new Insets(0, 0, 2, 5);
        gbc_lbl_channel.gridx = 0;
        gbc_lbl_channel.gridy = 3;
        add(lbl_channel, gbc_lbl_channel);

        channelLabel = new JLabel();
        channelLabel.setText("---");
        GridBagConstraints gbc_channelLabel = new GridBagConstraints();
        gbc_channelLabel.gridwidth = 3;
        gbc_channelLabel.fill = GridBagConstraints.BOTH;
        gbc_channelLabel.insets = new Insets(0, 0, 2, 0);
        gbc_channelLabel.gridx = 1;
        gbc_channelLabel.gridy = 3;
        add(channelLabel, gbc_channelLabel);

        JLabel lbl_size = new JLabel("Size");
        lbl_size.setToolTipText("Size");
        GridBagConstraints gbc_lbl_size = new GridBagConstraints();
        gbc_lbl_size.anchor = GridBagConstraints.WEST;
        gbc_lbl_size.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_size.insets = new Insets(0, 0, 2, 5);
        gbc_lbl_size.gridx = 0;
        gbc_lbl_size.gridy = 4;
        add(lbl_size, gbc_lbl_size);

        sizeLabel = new JLabel();
        sizeLabel.setText("---");
        GridBagConstraints gbc_sizeLabel = new GridBagConstraints();
        gbc_sizeLabel.gridwidth = 3;
        gbc_sizeLabel.fill = GridBagConstraints.BOTH;
        gbc_sizeLabel.insets = new Insets(0, 0, 2, 0);
        gbc_sizeLabel.gridx = 1;
        gbc_sizeLabel.gridy = 4;
        add(sizeLabel, gbc_sizeLabel);

        JLabel lbl_psx = new JLabel("Pixel size");
        lbl_psx.setToolTipText("Pixel size for X, Y, Z dimension");
        GridBagConstraints gbc_lbl_psx = new GridBagConstraints();
        gbc_lbl_psx.anchor = GridBagConstraints.WEST;
        gbc_lbl_psx.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_psx.insets = new Insets(0, 0, 2, 5);
        gbc_lbl_psx.gridx = 0;
        gbc_lbl_psx.gridy = 5;
        add(lbl_psx, gbc_lbl_psx);

        resXLabel = new JLabel();
        resXLabel.setText("---");
        GridBagConstraints gbc_resXLabel = new GridBagConstraints();
        gbc_resXLabel.fill = GridBagConstraints.BOTH;
        gbc_resXLabel.insets = new Insets(0, 0, 2, 5);
        gbc_resXLabel.gridx = 1;
        gbc_resXLabel.gridy = 5;
        add(resXLabel, gbc_resXLabel);

        resYLabel = new JLabel();
        resYLabel.setText("---");
        GridBagConstraints gbc_resYLabel = new GridBagConstraints();
        gbc_resYLabel.fill = GridBagConstraints.BOTH;
        gbc_resYLabel.insets = new Insets(0, 0, 2, 5);
        gbc_resYLabel.gridx = 2;
        gbc_resYLabel.gridy = 5;
        add(resYLabel, gbc_resYLabel);

        resZLabel = new JLabel();
        resZLabel.setText("---");
        GridBagConstraints gbc_resZLabel = new GridBagConstraints();
        gbc_resZLabel.fill = GridBagConstraints.BOTH;
        gbc_resZLabel.insets = new Insets(0, 0, 2, 0);
        gbc_resZLabel.gridx = 3;
        gbc_resZLabel.gridy = 5;
        add(resZLabel, gbc_resZLabel);

        JLabel lbl_time = new JLabel("Time interval");
        lbl_time.setToolTipText("Time Interval");
        GridBagConstraints gbc_lbl_time = new GridBagConstraints();
        gbc_lbl_time.anchor = GridBagConstraints.WEST;
        gbc_lbl_time.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_time.insets = new Insets(0, 0, 2, 5);
        gbc_lbl_time.gridx = 0;
        gbc_lbl_time.gridy = 6;
        add(lbl_time, gbc_lbl_time);

        resTLabel = new JLabel();
        resTLabel.setText("---");
        GridBagConstraints gbc_resTLabel = new GridBagConstraints();
        gbc_resTLabel.gridwidth = 3;
        gbc_resTLabel.fill = GridBagConstraints.BOTH;
        gbc_resTLabel.insets = new Insets(0, 0, 2, 0);
        gbc_resTLabel.gridx = 1;
        gbc_resTLabel.gridy = 6;
        add(resTLabel, gbc_resTLabel);

        editBtn = new IcyButton("Edit", new IcyIcon(ResourceUtil.ICON_DOCEDIT));
        editBtn.setToolTipText("Edit sequence properties");

        GridBagConstraints gbc_editBtn = new GridBagConstraints();
        gbc_editBtn.gridwidth = 2;
        gbc_editBtn.fill = GridBagConstraints.BOTH;
        gbc_editBtn.insets = new Insets(0, 0, 0, 5);
        gbc_editBtn.gridx = 0;
        gbc_editBtn.gridy = 7;
        add(editBtn, gbc_editBtn);

        detailBtn = new IcyButton("Show metadata", new IcyIcon(ResourceUtil.ICON_PROPERTIES));
        detailBtn.setText("Metadata");
        detailBtn.setToolTipText("Show all associated metadata informations");

        GridBagConstraints gbc_detailBtn = new GridBagConstraints();
        gbc_detailBtn.gridwidth = 2;
        gbc_detailBtn.fill = GridBagConstraints.BOTH;
        gbc_detailBtn.gridx = 2;
        gbc_detailBtn.gridy = 7;
        add(detailBtn, gbc_detailBtn);
    }

    public void updateInfos()
    {
        ThreadUtil.runSingle(infosRefresher);
    }

    public void updateInfosInternal(Sequence sequence)
    {
        if (sequence != null)
        {
            final int sizeX = sequence.getSizeX();
            final int sizeY = sequence.getSizeY();
            final int sizeZ = sequence.getSizeZ();
            final int sizeT = sequence.getSizeT();
            final int sizeC = sequence.getSizeC();

            final double pxSizeX = sequence.getPixelSizeX();
            final double pxSizeY = sequence.getPixelSizeY();
            final double pxSizeZ = sequence.getPixelSizeZ();

            final String path = sequence.getFilename();

            nameField.setText(StringUtil.limit(sequence.getName(), 28, true));
            // path
            if (StringUtil.isEmpty(path))
            {
                pathLabel.setVisible(false);
                pathField.setVisible(false);
            }
            else
            {
                pathLabel.setVisible(true);
                pathField.setVisible(true);
                pathField.setText(StringUtil.limit(path, 28, true));
            }
            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(sizeC + " - " + sequence.getDataType_().toLongString());
            sizeLabel.setText(UnitUtil.getBytesString((double) sizeX * (double) sizeY * sizeZ * sizeT * sizeC
                    * sequence.getDataType_().getSize()));
            resXLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeX, 2, UnitPrefix.MICRO));
            resYLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeY, 2, UnitPrefix.MICRO));
            resZLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeZ, 2, UnitPrefix.MICRO));
            resTLabel.setText(UnitUtil.displayTimeAsStringWithUnits(sequence.getTimeInterval() * 1000d, false));

            nameField.setToolTipText(sequence.getName());
            pathField.setToolTipText(path);
            dimensionLabel.setToolTipText("Size X : " + sizeX + "   Size Y : " + sizeY + "   Size Z : " + sizeZ
                    + "   Size T : " + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels - " + sequence.getDataType_());
            else
                channelLabel.setToolTipText(sizeC + " channel - " + sequence.getDataType_());
            sizeLabel.setToolTipText(sizeLabel.getText());

            resXLabel.setToolTipText("X pixel resolution: " + resXLabel.getText());
            resYLabel.setToolTipText("Y pixel resolution: " + resYLabel.getText());
            resZLabel.setToolTipText("Z pixel resolution: " + resZLabel.getText());
            resTLabel.setToolTipText("T time resolution: " + resTLabel.getText());

            editBtn.setEnabled(true);
            detailBtn.setEnabled(true);
        }
        else
        {
            pathLabel.setVisible(false);
            pathField.setVisible(false);

            nameField.setText("-");
            dimensionLabel.setText("-");
            channelLabel.setText("-");
            sizeLabel.setText("-");
            resXLabel.setText("-");
            resYLabel.setText("-");
            resZLabel.setText("-");
            resTLabel.setText("-");

            nameField.setToolTipText("");
            dimensionLabel.setToolTipText("");
            channelLabel.setToolTipText("");
            sizeLabel.setToolTipText("");
            resXLabel.setToolTipText("X pixel resolution");
            resYLabel.setToolTipText("Y pixel resolution");
            resZLabel.setToolTipText("Z pixel resolution");
            resTLabel.setToolTipText("T time resolution");

            editBtn.setEnabled(false);
            detailBtn.setEnabled(false);
        }

        revalidate();
    }

    @Override
    public void sequenceActivated(Sequence sequence)
    {
        updateInfos();
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        // nothing to do here
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        switch (event.getSourceType())
        {
            case SEQUENCE_DATA:
            case SEQUENCE_TYPE:
            case SEQUENCE_META:
                updateInfos();
                break;
        }
    }
}

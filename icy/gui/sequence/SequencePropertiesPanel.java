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
package icy.gui.sequence;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.NumberTextField;
import icy.gui.util.ComponentUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class SequencePropertiesPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -1568878218022361239L;

    private IcyTextField nameField;
    NumberTextField tfPxSizeX;
    NumberTextField tfPxSizeY;
    private NumberTextField tfPxSizeZ;
    private JComboBox cbPxSizeX;
    JComboBox cbPxSizeY;
    private JComboBox cbPxSizeZ;
    private NumberTextField tfTimeInterval;
    private JPanel panelChannels;
    private IcyTextField[] tfsChannels;
    private JLabel lblX;
    private JLabel lblY;
    private JLabel lblZ;
    JCheckBox checkLinked;
    private JComboBox cbTimeUnit;
    private JLabel lblValue;
    private JPanel panelPosition;
    private NumberTextField positionXField;
    private NumberTextField positionYField;
    private NumberTextField positionZField;
    private JComboBox posXUnitComboBox;
    private JComboBox posYUnitComboBox;
    private JComboBox posZUnitComboBox;
    private JPanel panelPixelSize;
    private JPanel panelTimeInterval;

    /**
     * Create the panel.
     */
    public SequencePropertiesPanel()
    {
        super();

        // set ComboBox model
        final UnitPrefix[] upValues = UnitPrefix.values();
        final String[] cbModel = new String[upValues.length];

        for (int i = 0; i < upValues.length; ++i)
            cbModel[i] = upValues[i].toString() + "m";

        initialize(cbModel);
    }

    private void initialize(String[] cbModel)
    {
        setLayout(new BorderLayout(0, 0));

        JPanel panelMain = new JPanel();
        panelMain.setBorder(new EmptyBorder(4, 4, 0, 4));
        add(panelMain, BorderLayout.NORTH);
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

        JPanel panelName = new JPanel();
        panelName.setBorder(new TitledBorder(null, "Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelMain.add(panelName);
        panelName.setLayout(new BoxLayout(panelName, BoxLayout.LINE_AXIS));

        nameField = new IcyTextField();
        nameField.setPreferredSize(new Dimension(200, 20));
        nameField.setMinimumSize(new Dimension(80, 20));
        panelName.add(nameField);

        panelPixelSize = new JPanel();
        panelPixelSize.setBorder(new TitledBorder(null, "Pixel Size", TitledBorder.LEADING, TitledBorder.TOP, null,
                null));
        panelMain.add(panelPixelSize);
        GridBagLayout gbl_panelPixelSize = new GridBagLayout();
        gbl_panelPixelSize.columnWidths = new int[] {60, 80, 40, 60, 0};
        gbl_panelPixelSize.rowHeights = new int[] {0, 0, 0, 0};
        gbl_panelPixelSize.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_panelPixelSize.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelPixelSize.setLayout(gbl_panelPixelSize);

        lblX = new JLabel("X");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.anchor = GridBagConstraints.WEST;
        gbc_lblX.fill = GridBagConstraints.VERTICAL;
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        panelPixelSize.add(lblX, gbc_lblX);

        tfPxSizeX = new NumberTextField();
        tfPxSizeX.setColumns(4);
        GridBagConstraints gbc_tfPxSizeX = new GridBagConstraints();
        gbc_tfPxSizeX.fill = GridBagConstraints.BOTH;
        gbc_tfPxSizeX.insets = new Insets(0, 0, 5, 5);
        gbc_tfPxSizeX.gridx = 1;
        gbc_tfPxSizeX.gridy = 0;
        panelPixelSize.add(tfPxSizeX, gbc_tfPxSizeX);
        tfPxSizeX.addTextChangeListener(new TextChangeListener()
        {
            @Override
            public void textChanged(IcyTextField source, boolean validate)
            {
                if (checkLinked.isSelected())
                    tfPxSizeY.setText(tfPxSizeX.getText());
            }
        });
        tfPxSizeX.setToolTipText("X pixel size");
        cbPxSizeX = new JComboBox(cbModel);
        GridBagConstraints gbc_cbPxSizeX = new GridBagConstraints();
        gbc_cbPxSizeX.fill = GridBagConstraints.BOTH;
        gbc_cbPxSizeX.insets = new Insets(0, 0, 5, 5);
        gbc_cbPxSizeX.gridx = 2;
        gbc_cbPxSizeX.gridy = 0;
        panelPixelSize.add(cbPxSizeX, gbc_cbPxSizeX);

        lblY = new JLabel("Y");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.anchor = GridBagConstraints.WEST;
        gbc_lblY.fill = GridBagConstraints.VERTICAL;
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 0;
        gbc_lblY.gridy = 1;
        panelPixelSize.add(lblY, gbc_lblY);

        tfPxSizeY = new NumberTextField();
        tfPxSizeY.setColumns(4);
        GridBagConstraints gbc_tfPxSizeY = new GridBagConstraints();
        gbc_tfPxSizeY.fill = GridBagConstraints.BOTH;
        gbc_tfPxSizeY.insets = new Insets(0, 0, 5, 5);
        gbc_tfPxSizeY.gridx = 1;
        gbc_tfPxSizeY.gridy = 1;
        panelPixelSize.add(tfPxSizeY, gbc_tfPxSizeY);
        tfPxSizeY.setToolTipText("Y pixel size");
        cbPxSizeY = new JComboBox(cbModel);
        GridBagConstraints gbc_cbPxSizeY = new GridBagConstraints();
        gbc_cbPxSizeY.fill = GridBagConstraints.BOTH;
        gbc_cbPxSizeY.insets = new Insets(0, 0, 5, 5);
        gbc_cbPxSizeY.gridx = 2;
        gbc_cbPxSizeY.gridy = 1;
        panelPixelSize.add(cbPxSizeY, gbc_cbPxSizeY);

        checkLinked = new JCheckBox("link X/Y");
        GridBagConstraints gbc_checkLinked = new GridBagConstraints();
        gbc_checkLinked.anchor = GridBagConstraints.WEST;
        gbc_checkLinked.fill = GridBagConstraints.VERTICAL;
        gbc_checkLinked.insets = new Insets(0, 0, 5, 0);
        gbc_checkLinked.gridx = 3;
        gbc_checkLinked.gridy = 1;
        panelPixelSize.add(checkLinked, gbc_checkLinked);

        lblZ = new JLabel("Z");
        GridBagConstraints gbc_lblZ = new GridBagConstraints();
        gbc_lblZ.anchor = GridBagConstraints.WEST;
        gbc_lblZ.fill = GridBagConstraints.VERTICAL;
        gbc_lblZ.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ.gridx = 0;
        gbc_lblZ.gridy = 2;
        panelPixelSize.add(lblZ, gbc_lblZ);

        tfPxSizeZ = new NumberTextField();
        tfPxSizeZ.setColumns(4);
        GridBagConstraints gbc_tfPxSizeZ = new GridBagConstraints();
        gbc_tfPxSizeZ.fill = GridBagConstraints.BOTH;
        gbc_tfPxSizeZ.insets = new Insets(0, 0, 0, 5);
        gbc_tfPxSizeZ.gridx = 1;
        gbc_tfPxSizeZ.gridy = 2;
        panelPixelSize.add(tfPxSizeZ, gbc_tfPxSizeZ);
        tfPxSizeZ.setToolTipText("Z pixel size");
        cbPxSizeZ = new JComboBox(cbModel);
        GridBagConstraints gbc_cbPxSizeZ = new GridBagConstraints();
        gbc_cbPxSizeZ.fill = GridBagConstraints.BOTH;
        gbc_cbPxSizeZ.insets = new Insets(0, 0, 0, 5);
        gbc_cbPxSizeZ.gridx = 2;
        gbc_cbPxSizeZ.gridy = 2;
        panelPixelSize.add(cbPxSizeZ, gbc_cbPxSizeZ);
        checkLinked.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (checkLinked.isSelected())
                {
                    tfPxSizeY.setEnabled(false);
                    tfPxSizeY.setText(tfPxSizeX.getText());
                    cbPxSizeY.setEnabled(false);
                }
                else
                {
                    tfPxSizeY.setEnabled(true);
                    cbPxSizeY.setEnabled(true);
                }
            }
        });

        panelTimeInterval = new JPanel();
        panelTimeInterval.setBorder(new TitledBorder(null, "Time Interval", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        panelMain.add(panelTimeInterval);
        GridBagLayout gbl_panelTimeInterval = new GridBagLayout();
        gbl_panelTimeInterval.columnWidths = new int[] {60, 80, 40, 60, 0};
        gbl_panelTimeInterval.rowHeights = new int[] {0, 0};
        gbl_panelTimeInterval.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_panelTimeInterval.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panelTimeInterval.setLayout(gbl_panelTimeInterval);

        lblValue = new JLabel("Value");
        GridBagConstraints gbc_lblValue = new GridBagConstraints();
        gbc_lblValue.anchor = GridBagConstraints.WEST;
        gbc_lblValue.fill = GridBagConstraints.VERTICAL;
        gbc_lblValue.insets = new Insets(0, 0, 0, 5);
        gbc_lblValue.gridx = 0;
        gbc_lblValue.gridy = 0;
        panelTimeInterval.add(lblValue, gbc_lblValue);

        tfTimeInterval = new NumberTextField();
        tfTimeInterval.setColumns(4);
        GridBagConstraints gbc_tfTimeInterval = new GridBagConstraints();
        gbc_tfTimeInterval.fill = GridBagConstraints.BOTH;
        gbc_tfTimeInterval.insets = new Insets(0, 0, 0, 5);
        gbc_tfTimeInterval.gridx = 1;
        gbc_tfTimeInterval.gridy = 0;
        panelTimeInterval.add(tfTimeInterval, gbc_tfTimeInterval);
        tfTimeInterval.setToolTipText("T time resolution");

        cbTimeUnit = new JComboBox(new String[] {"h", "min", "s", "ms"});
        GridBagConstraints gbc_cbTimeUnit = new GridBagConstraints();
        gbc_cbTimeUnit.insets = new Insets(0, 0, 0, 5);
        gbc_cbTimeUnit.fill = GridBagConstraints.BOTH;
        gbc_cbTimeUnit.gridx = 2;
        gbc_cbTimeUnit.gridy = 0;
        panelTimeInterval.add(cbTimeUnit, gbc_cbTimeUnit);
        cbTimeUnit.setSelectedIndex(2);

        panelPosition = new JPanel();
        panelPosition.setBorder(new TitledBorder(null, "Position", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelMain.add(panelPosition);
        GridBagLayout gbl_panelPosition = new GridBagLayout();
        gbl_panelPosition.columnWidths = new int[] {60, 80, 40, 60, 0};
        gbl_panelPosition.rowHeights = new int[] {0, 0, 0, 0};
        gbl_panelPosition.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_panelPosition.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelPosition.setLayout(gbl_panelPosition);

        JLabel lblX_1 = new JLabel("X");
        GridBagConstraints gbc_lblX_1 = new GridBagConstraints();
        gbc_lblX_1.fill = GridBagConstraints.VERTICAL;
        gbc_lblX_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblX_1.anchor = GridBagConstraints.WEST;
        gbc_lblX_1.gridx = 0;
        gbc_lblX_1.gridy = 0;
        panelPosition.add(lblX_1, gbc_lblX_1);

        positionXField = new NumberTextField();
        positionXField.setColumns(4);
        positionXField.setToolTipText("Image position / offset X");
        GridBagConstraints gbc_positionXfield = new GridBagConstraints();
        gbc_positionXfield.insets = new Insets(0, 0, 5, 5);
        gbc_positionXfield.fill = GridBagConstraints.BOTH;
        gbc_positionXfield.gridx = 1;
        gbc_positionXfield.gridy = 0;
        panelPosition.add(positionXField, gbc_positionXfield);

        posXUnitComboBox = new JComboBox(cbModel);
        GridBagConstraints gbc_posXUnitComboBox = new GridBagConstraints();
        gbc_posXUnitComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_posXUnitComboBox.fill = GridBagConstraints.BOTH;
        gbc_posXUnitComboBox.gridx = 2;
        gbc_posXUnitComboBox.gridy = 0;
        panelPosition.add(posXUnitComboBox, gbc_posXUnitComboBox);

        JLabel lblY_1 = new JLabel("Y");
        GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.fill = GridBagConstraints.VERTICAL;
        gbc_lblY_1.anchor = GridBagConstraints.WEST;
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.gridx = 0;
        gbc_lblY_1.gridy = 1;
        panelPosition.add(lblY_1, gbc_lblY_1);

        positionYField = new NumberTextField();
        positionYField.setColumns(4);
        positionYField.setToolTipText("Image position / offset Y");
        GridBagConstraints gbc_positionYField = new GridBagConstraints();
        gbc_positionYField.insets = new Insets(0, 0, 5, 5);
        gbc_positionYField.fill = GridBagConstraints.BOTH;
        gbc_positionYField.gridx = 1;
        gbc_positionYField.gridy = 1;
        panelPosition.add(positionYField, gbc_positionYField);

        posYUnitComboBox = new JComboBox(cbModel);
        GridBagConstraints gbc_posYUnitComboBox = new GridBagConstraints();
        gbc_posYUnitComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_posYUnitComboBox.fill = GridBagConstraints.BOTH;
        gbc_posYUnitComboBox.gridx = 2;
        gbc_posYUnitComboBox.gridy = 1;
        panelPosition.add(posYUnitComboBox, gbc_posYUnitComboBox);

        JLabel lblZ_1 = new JLabel("Z");
        GridBagConstraints gbc_lblZ_1 = new GridBagConstraints();
        gbc_lblZ_1.fill = GridBagConstraints.VERTICAL;
        gbc_lblZ_1.anchor = GridBagConstraints.WEST;
        gbc_lblZ_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ_1.gridx = 0;
        gbc_lblZ_1.gridy = 2;
        panelPosition.add(lblZ_1, gbc_lblZ_1);

        positionZField = new NumberTextField();
        positionZField.setColumns(4);
        positionZField.setToolTipText("Image position / offset Z");
        GridBagConstraints gbc_positionZField = new GridBagConstraints();
        gbc_positionZField.insets = new Insets(0, 0, 0, 5);
        gbc_positionZField.fill = GridBagConstraints.BOTH;
        gbc_positionZField.gridx = 1;
        gbc_positionZField.gridy = 2;
        panelPosition.add(positionZField, gbc_positionZField);

        posZUnitComboBox = new JComboBox(cbModel);
        GridBagConstraints gbc_posZUnitComboBox = new GridBagConstraints();
        gbc_posZUnitComboBox.insets = new Insets(0, 0, 0, 5);
        gbc_posZUnitComboBox.fill = GridBagConstraints.BOTH;
        gbc_posZUnitComboBox.gridx = 2;
        gbc_posZUnitComboBox.gridy = 2;
        panelPosition.add(posZUnitComboBox, gbc_posZUnitComboBox);

        panelChannels = new JPanel();
        panelChannels.setBorder(new TitledBorder(null, "Channels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelMain.add(panelChannels);
        panelChannels.setLayout(new BoxLayout(panelChannels, BoxLayout.Y_AXIS));
    }

    public void setSequence(Sequence sequence)
    {
        // name
        nameField.setText(sequence.getName());

        // pixel size
        final double pxSizeX = sequence.getPixelSizeX();
        final double pxSizeY = sequence.getPixelSizeY();
        final double pxSizeZ = sequence.getPixelSizeZ();

        final UnitPrefix pxSizeXUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MICRO);
        final UnitPrefix pxSizeYUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MICRO);
        final UnitPrefix pxSizeZUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MICRO);

        cbPxSizeX.setSelectedItem(pxSizeXUnit.toString() + "m");
        cbPxSizeY.setSelectedItem(pxSizeYUnit.toString() + "m");
        cbPxSizeZ.setSelectedItem(pxSizeZUnit.toString() + "m");

        tfPxSizeX.setText(StringUtil.toString(UnitUtil.getValueInUnit(pxSizeX, UnitPrefix.MICRO, pxSizeXUnit)));
        tfPxSizeY.setText(StringUtil.toString(UnitUtil.getValueInUnit(pxSizeY, UnitPrefix.MICRO, pxSizeYUnit)));
        tfPxSizeZ.setText(StringUtil.toString(UnitUtil.getValueInUnit(pxSizeZ, UnitPrefix.MICRO, pxSizeZUnit)));

        if (tfPxSizeX.getText().equals(tfPxSizeY.getText())
                && cbPxSizeX.getSelectedIndex() == cbPxSizeY.getSelectedIndex())
        {
            checkLinked.doClick();
        }

        // get timeInterval in ms
        double timeInterval = sequence.getTimeInterval() * 1000d;
        TimeUnit unit = UnitUtil.getBestTimeUnit(timeInterval);

        switch (unit)
        {
            case MILLISECONDS:
                tfTimeInterval.setText(StringUtil.toString(timeInterval));
                cbTimeUnit.setSelectedIndex(3);
                break;

            case SECONDS:
                tfTimeInterval.setText(StringUtil.toString(timeInterval / 1000));
                cbTimeUnit.setSelectedIndex(2);
                break;

            case MINUTES:
                tfTimeInterval.setText(StringUtil.toString(timeInterval / 60000));
                cbTimeUnit.setSelectedIndex(1);
                break;

            case HOURS:
                tfTimeInterval.setText(StringUtil.toString(timeInterval / 3600000));
                cbTimeUnit.setSelectedIndex(0);
                break;
        }

        // position
        final double posX = sequence.getPositionX();
        final double posY = sequence.getPositionY();
        final double posZ = sequence.getPositionZ();

        final UnitPrefix posXUnit = UnitUtil.getBestUnit(posX, UnitPrefix.MICRO);
        final UnitPrefix posYUnit = UnitUtil.getBestUnit(posY, UnitPrefix.MICRO);
        final UnitPrefix posZUnit = UnitUtil.getBestUnit(posZ, UnitPrefix.MICRO);

        posXUnitComboBox.setSelectedItem(posXUnit.toString() + "m");
        posYUnitComboBox.setSelectedItem(posYUnit.toString() + "m");
        posZUnitComboBox.setSelectedItem(posZUnit.toString() + "m");

        positionXField.setText(StringUtil.toString(UnitUtil.getValueInUnit(posX, UnitPrefix.MICRO, posXUnit)));
        positionYField.setText(StringUtil.toString(UnitUtil.getValueInUnit(posY, UnitPrefix.MICRO, posYUnit)));
        positionZField.setText(StringUtil.toString(UnitUtil.getValueInUnit(posZ, UnitPrefix.MICRO, posZUnit)));

        // channel name
        final int sizeC = sequence.getSizeC();

        panelChannels.removeAll();

        tfsChannels = new IcyTextField[sizeC];

        for (int c = 0; c < sizeC; c++)
        {
            final JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

            final JLabel label = new JLabel("Channel " + c + " name");
            label.setToolTipText("Channel " + c + " name");
            ComponentUtil.setFixedWidth(label, 100);

            final IcyTextField field = new IcyTextField();
            field.setText(sequence.getChannelName(c));

            panel.add(label);
            panel.add(field);

            tfsChannels[c] = field;
            panelChannels.add(panel);
        }

        panelChannels.revalidate();
    }

    public String getNameFieldValue()
    {
        return nameField.getText();
    }

    public double getPixelSizeXFieldValue()
    {
        return tfPxSizeX.getNumericValue();
    }

    public UnitPrefix getPixelSizeXUnit()
    {
        return UnitPrefix.values()[cbPxSizeX.getSelectedIndex()];
    }

    public double getPixelSizeYFieldValue()
    {
        if (checkLinked.isSelected())
            return tfPxSizeX.getNumericValue();

        return tfPxSizeY.getNumericValue();
    }

    public UnitPrefix getPixelSizeYUnit()
    {
        if (checkLinked.isSelected())
            return UnitPrefix.values()[cbPxSizeX.getSelectedIndex()];

        return UnitPrefix.values()[cbPxSizeY.getSelectedIndex()];
    }

    public double getPixelSizeZFieldValue()
    {
        return tfPxSizeZ.getNumericValue();
    }

    public UnitPrefix getPixelSizeZUnit()
    {
        return UnitPrefix.values()[cbPxSizeZ.getSelectedIndex()];
    }

    public double getTimeIntervalFieldValue()
    {
        return tfTimeInterval.getNumericValue();
    }

    public int getTimeIntervalUnit()
    {
        return cbTimeUnit.getSelectedIndex();
    }

    public double getPositionXValue()
    {
        return positionXField.getNumericValue();
    }

    public UnitPrefix getPositionXUnit()
    {
        return UnitPrefix.values()[posXUnitComboBox.getSelectedIndex()];
    }

    public double getPositionYValue()
    {
        return positionYField.getNumericValue();
    }

    public UnitPrefix getPositionYUnit()
    {
        return UnitPrefix.values()[posYUnitComboBox.getSelectedIndex()];
    }

    public double getPositionZValue()
    {
        return positionZField.getNumericValue();
    }

    public UnitPrefix getPositionZUnit()
    {
        return UnitPrefix.values()[posZUnitComboBox.getSelectedIndex()];
    }

    public String getChannelNameFieldValue(int index)
    {
        return tfsChannels[index].getText();
    }
}

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
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.util.ComponentUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
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
    IcyTextField tfPxSizeX;
    IcyTextField tfPxSizeY;
    private IcyTextField tfPxSizeZ;
    private JComboBox cbPxSizeX;
    JComboBox cbPxSizeY;
    private JComboBox cbPxSizeZ;
    private IcyTextField tfTimeInterval;
    private JPanel panelChannels;
    private IcyTextField[] tfsChannels;
    private JLabel lblX;
    private JLabel lblY;
    private JLabel lblZ;
    private JPanel panelPxSizeX;
    private JPanel panelPxSizeY;
    private JPanel panelPxSizeZ;
    private JPanel panelPxSizeT;
    JCheckBox checkLinked;
    private JPanel panelPxSizeXLeft;
    private Component horizontalGlue;
    private JPanel panelPxSizeXRight;
    private JPanel panelPxSizeYLeft;
    private JPanel panelPxSizeYRight;
    private JPanel panelPxSizeZLeft;
    private JPanel panelPxSizeZRight;
    private JPanel panelPxSizeTLeft;
    private JPanel panelPxSizeTRight;
    private Component horizontalGlue_1;
    private JComboBox cbTimeUnit;
    private Component horizontalGlue_2;
    private JPanel panelTimeInterval;
    private JLabel lblValue;

    /**
     * Create the panel.
     */
    public SequencePropertiesPanel()
    {
        super();

        initialize();
    }

    void initialize()
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

        GridLayout gl_panel_2 = new GridLayout();
        gl_panel_2.setColumns(1);
        gl_panel_2.setRows(3);
        JPanel panelPxSizeConfig = new JPanel(gl_panel_2);
        panelPxSizeConfig.setBorder(new TitledBorder(null, "Pixel Size Config", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        panelMain.add(panelPxSizeConfig);

        UnitPrefix[] upValues = UnitPrefix.values();
        String[] cbModel = new String[upValues.length];
        for (int i = 0; i < upValues.length; ++i)
            cbModel[i] = upValues[i].toString() + "m";

        panelPxSizeX = new JPanel(new GridLayout());
        panelPxSizeConfig.add(panelPxSizeX);

        panelPxSizeXLeft = new JPanel();
        panelPxSizeX.add(panelPxSizeXLeft);
        panelPxSizeXLeft.setLayout(new BorderLayout(0, 0));

        lblX = new JLabel("X: ");
        panelPxSizeXLeft.add(lblX, BorderLayout.WEST);

        tfPxSizeX = new IcyTextField();
        tfPxSizeX.addTextChangeListener(new TextChangeListener()
        {
            @Override
            public void textChanged(IcyTextField source, boolean validate)
            {
                if (checkLinked.isSelected())
                    tfPxSizeY.setText(tfPxSizeX.getText());
            }
        });
        tfPxSizeX.setToolTipText("X pixel size.");
        panelPxSizeXLeft.add(tfPxSizeX);

        panelPxSizeXRight = new JPanel(new GridLayout());
        panelPxSizeX.add(panelPxSizeXRight);
        cbPxSizeX = new JComboBox(cbModel);
        panelPxSizeXRight.add(cbPxSizeX);

        horizontalGlue = Box.createHorizontalGlue();
        panelPxSizeXRight.add(horizontalGlue);

        panelPxSizeY = new JPanel(new GridLayout());
        panelPxSizeConfig.add(panelPxSizeY);

        panelPxSizeYLeft = new JPanel();
        panelPxSizeY.add(panelPxSizeYLeft);
        panelPxSizeYLeft.setLayout(new BorderLayout(0, 0));

        lblY = new JLabel("Y: ");
        panelPxSizeYLeft.add(lblY, BorderLayout.WEST);

        tfPxSizeY = new IcyTextField();
        panelPxSizeYLeft.add(tfPxSizeY);
        tfPxSizeY.setPreferredSize(new Dimension(60, 20));
        tfPxSizeY.setToolTipText("Y pixel size.");

        panelPxSizeYRight = new JPanel();
        panelPxSizeY.add(panelPxSizeYRight);
        panelPxSizeYRight.setLayout(new GridLayout(0, 2, 0, 0));
        cbPxSizeY = new JComboBox(cbModel);
        panelPxSizeYRight.add(cbPxSizeY);

        checkLinked = new JCheckBox("link X/Y");
        panelPxSizeYRight.add(checkLinked);
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

        panelPxSizeZ = new JPanel(new GridLayout());
        panelPxSizeConfig.add(panelPxSizeZ);

        panelPxSizeZLeft = new JPanel();
        panelPxSizeZ.add(panelPxSizeZLeft);
        panelPxSizeZLeft.setLayout(new BorderLayout(0, 0));

        lblZ = new JLabel("Z: ");
        panelPxSizeZLeft.add(lblZ, BorderLayout.WEST);

        tfPxSizeZ = new IcyTextField();
        panelPxSizeZLeft.add(tfPxSizeZ);
        tfPxSizeZ.setPreferredSize(new Dimension(40, 20));
        tfPxSizeZ.setMinimumSize(new Dimension(40, 20));
        tfPxSizeZ.setToolTipText("Z pixel size.");
        panelPxSizeZRight = new JPanel();
        panelPxSizeZ.add(panelPxSizeZRight);
        panelPxSizeZRight.setLayout(new GridLayout(0, 2, 0, 0));
        cbPxSizeZ = new JComboBox(cbModel);
        panelPxSizeZRight.add(cbPxSizeZ);

        horizontalGlue_1 = Box.createHorizontalGlue();
        panelPxSizeZRight.add(horizontalGlue_1);

        panelTimeInterval = new JPanel();
        panelTimeInterval.setBorder(new TitledBorder(null, "Time Interval", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        panelMain.add(panelTimeInterval);
        panelTimeInterval.setLayout(new BorderLayout(0, 0));

        panelPxSizeT = new JPanel(new GridLayout());
        panelTimeInterval.add(panelPxSizeT);

        panelPxSizeTLeft = new JPanel();
        panelPxSizeT.add(panelPxSizeTLeft);
        panelPxSizeTLeft.setLayout(new BorderLayout(0, 0));

        lblValue = new JLabel("Value: ");
        panelPxSizeTLeft.add(lblValue, BorderLayout.WEST);

        tfTimeInterval = new IcyTextField();
        panelPxSizeTLeft.add(tfTimeInterval, BorderLayout.CENTER);
        tfTimeInterval.setPreferredSize(new Dimension(40, 20));
        tfTimeInterval.setMinimumSize(new Dimension(40, 20));
        tfTimeInterval.setToolTipText("T time resolution (in ms).");

        panelPxSizeTRight = new JPanel();
        panelPxSizeT.add(panelPxSizeTRight);
        panelPxSizeTRight.setLayout(new GridLayout(0, 2, 0, 0));

        cbTimeUnit = new JComboBox(new String[] {"h", "min", "s", "ms"});
        cbTimeUnit.setSelectedIndex(2);
        panelPxSizeTRight.add(cbTimeUnit);

        horizontalGlue_2 = Box.createHorizontalGlue();
        panelPxSizeTRight.add(horizontalGlue_2);

        panelChannels = new JPanel();
        panelChannels.setBorder(new TitledBorder(null, "Channels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelMain.add(panelChannels);
        panelChannels.setLayout(new BoxLayout(panelChannels, BoxLayout.Y_AXIS));
    }

    public void setSequence(Sequence sequence)
    {
        nameField.setText(sequence.getName());

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
        return StringUtil.parseDouble(tfPxSizeX.getText(), 1d);
    }

    public UnitPrefix getPixelSizeXUnit()
    {
        return UnitPrefix.values()[cbPxSizeX.getSelectedIndex()];
    }

    public double getPixelSizeYFieldValue()
    {
        if (checkLinked.isSelected())
            return StringUtil.parseDouble(tfPxSizeX.getText(), 1d);

        return StringUtil.parseDouble(tfPxSizeY.getText(), 1d);
    }

    public UnitPrefix getPixelSizeYUnit()
    {
        if (checkLinked.isSelected())
            return UnitPrefix.values()[cbPxSizeX.getSelectedIndex()];

        return UnitPrefix.values()[cbPxSizeY.getSelectedIndex()];
    }

    public double getPixelSizeZFieldValue()
    {
        return StringUtil.parseDouble(tfPxSizeZ.getText(), 1d);
    }

    public UnitPrefix getPixelSizeZUnit()
    {
        return UnitPrefix.values()[cbPxSizeZ.getSelectedIndex()];
    }

    public double getTimeIntervalFieldValue()
    {
        return StringUtil.parseDouble(tfTimeInterval.getText(), 1d);
    }

    public int getTimeIntervalUnit()
    {
        return cbTimeUnit.getSelectedIndex();
    }

    public String getChannelNameFieldValue(int index)
    {
        return tfsChannels[index].getText();
    }
}

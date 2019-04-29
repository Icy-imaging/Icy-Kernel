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
package icy.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import icy.preferences.CanvasPreferences;
import icy.preferences.GeneralPreferences;

/**
 * @author Stephane
 */
public class GUICanvasPreferencePanel extends PreferencePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 7070251589085892036L;

    public static final String NODE_NAME = "GUI & Canvas";

    /**
     * gui
     */
    private JCheckBox filteringCheckBox;
    private JCheckBox invertWheelAxisCheckBox;
    private JSpinner wheelAxisSensitivity;
    private JCheckBox alwaysOnTopCheckBox;
    private JSpinner uiFontSizeSpinner;

    /**
     * @param parent
     */
    GUICanvasPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        initialize();
        load();
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 80, 4, 0};
        gridBagLayout.rowHeights = new int[] {23, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        mainPanel.setLayout(gridBagLayout);

        alwaysOnTopCheckBox = new JCheckBox("Application window always on top");
        GridBagConstraints gbc_alwaysOnTopCheckBox = new GridBagConstraints();
        gbc_alwaysOnTopCheckBox.anchor = GridBagConstraints.NORTHWEST;
        gbc_alwaysOnTopCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_alwaysOnTopCheckBox.gridx = 0;
        gbc_alwaysOnTopCheckBox.gridy = 0;
        mainPanel.add(alwaysOnTopCheckBox, gbc_alwaysOnTopCheckBox);

        filteringCheckBox = new JCheckBox("Enable image filtering");
        filteringCheckBox.setToolTipText("Enable image filtering to improve rendering quality");
        GridBagConstraints gbc_filteringCheckBox = new GridBagConstraints();
        gbc_filteringCheckBox.anchor = GridBagConstraints.NORTHWEST;
        gbc_filteringCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_filteringCheckBox.gridx = 0;
        gbc_filteringCheckBox.gridy = 1;
        mainPanel.add(filteringCheckBox, gbc_filteringCheckBox);

        invertWheelAxisCheckBox = new JCheckBox("Invert mouse wheel axis");
        invertWheelAxisCheckBox.setToolTipText("Invert the mouse wheel axis for canvas operation");
        GridBagConstraints gbc_invertWheelAxisCheckBox = new GridBagConstraints();
        gbc_invertWheelAxisCheckBox.anchor = GridBagConstraints.NORTHWEST;
        gbc_invertWheelAxisCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_invertWheelAxisCheckBox.gridx = 0;
        gbc_invertWheelAxisCheckBox.gridy = 2;
        mainPanel.add(invertWheelAxisCheckBox, gbc_invertWheelAxisCheckBox);

        JLabel label = new JLabel(" GUI font size   ");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 0;
        gbc_label.gridy = 3;
        mainPanel.add(label, gbc_label);

        uiFontSizeSpinner = new JSpinner(new SpinnerNumberModel(7, 7, 24, 1));
        uiFontSizeSpinner.setToolTipText("");
        GridBagConstraints gbc_uiFontSizeSpinner = new GridBagConstraints();
        gbc_uiFontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_uiFontSizeSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_uiFontSizeSpinner.gridx = 1;
        gbc_uiFontSizeSpinner.gridy = 3;
        mainPanel.add(uiFontSizeSpinner, gbc_uiFontSizeSpinner);

        GridBagConstraints gbc_lblMouseWheelSensivity = new GridBagConstraints();
        gbc_lblMouseWheelSensivity.anchor = GridBagConstraints.WEST;
        gbc_lblMouseWheelSensivity.insets = new Insets(0, 0, 5, 5);
        gbc_lblMouseWheelSensivity.gridx = 0;
        gbc_lblMouseWheelSensivity.gridy = 4;
        JLabel lblMouseWheelSensivity = new JLabel(" Mouse wheel sensivity");
        mainPanel.add(lblMouseWheelSensivity, gbc_lblMouseWheelSensivity);

        wheelAxisSensitivity = new JSpinner(new SpinnerNumberModel(5d, 1d, 10d, 0.5d));
        wheelAxisSensitivity.setToolTipText("Set mouse wheel sensivity for canvas operation (1-10)");
        GridBagConstraints gbc_wheelAxisSensitivity = new GridBagConstraints();
        gbc_wheelAxisSensitivity.fill = GridBagConstraints.HORIZONTAL;
        gbc_wheelAxisSensitivity.insets = new Insets(0, 0, 5, 5);
        gbc_wheelAxisSensitivity.gridx = 1;
        gbc_wheelAxisSensitivity.gridy = 4;
        mainPanel.add(wheelAxisSensitivity, gbc_wheelAxisSensitivity);

        mainPanel.validate();
    }

    @Override
    protected void load()
    {
        wheelAxisSensitivity.setValue(Double.valueOf(CanvasPreferences.getMouseWheelSensitivity()));
        invertWheelAxisCheckBox.setSelected(CanvasPreferences.getInvertMouseWheelAxis());
        filteringCheckBox.setSelected(CanvasPreferences.getFiltering());
        alwaysOnTopCheckBox.setSelected(GeneralPreferences.getAlwaysOnTop());
        uiFontSizeSpinner.setValue(Integer.valueOf(GeneralPreferences.getGuiFontSize()));
    }

    @Override
    protected void save()
    {
        CanvasPreferences.setMouseWheelSensitivity(((Double) wheelAxisSensitivity.getValue()).doubleValue());
        CanvasPreferences.setInvertMouseWheelAxis(invertWheelAxisCheckBox.isSelected());
        CanvasPreferences.setFiltering(filteringCheckBox.isSelected());

        boolean booleanValue = alwaysOnTopCheckBox.isSelected();
        Icy.getMainInterface().setAlwaysOnTop(booleanValue);
        GeneralPreferences.setAlwaysOnTop(booleanValue);

        int intValue = ((Integer) uiFontSizeSpinner.getValue()).intValue();
        LookAndFeelUtil.setFontSize(intValue);
        GeneralPreferences.setGuiFontSize(intValue);
    }
}

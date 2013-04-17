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
package icy.gui.preferences;

import icy.gui.util.GuiUtil;
import icy.preferences.CanvasPreferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Stephane
 */
public class CanvasPreferencePanel extends PreferencePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 7070251589085892036L;

    public static final String NODE_NAME = "Canvas";

    /**
     * gui
     */
    private final JCheckBox filteringCheckBox;
    private final JCheckBox invertWheelAxisCheckBox;
    private final JSpinner wheelAxisSensitivity;

    /**
     * @param parent
     */
    CanvasPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        filteringCheckBox = new JCheckBox("Enable filtering");
        filteringCheckBox.setToolTipText("Enable image filtering to improve image quality");
        invertWheelAxisCheckBox = new JCheckBox("Invert mouse wheel axis");
        invertWheelAxisCheckBox.setToolTipText("Invert the mouse wheel axis for canvas operation");
        wheelAxisSensitivity = new JSpinner(new SpinnerNumberModel(5d, 1d, 10d, 0.5d));
        wheelAxisSensitivity.setToolTipText("Set mouse wheel sensivity for canvas operation (1-10)");

        load();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(GuiUtil.createLineBoxPanel(filteringCheckBox, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(invertWheelAxisCheckBox, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" Mouse wheel sensivity "), wheelAxisSensitivity,
                Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.validate();
    }

    @Override
    protected void load()
    {
        wheelAxisSensitivity.setValue(Double.valueOf(CanvasPreferences.getMouseWheelSensitivity()));
        invertWheelAxisCheckBox.setSelected(CanvasPreferences.getInvertMouseWheelAxis());
        filteringCheckBox.setSelected(CanvasPreferences.getFiltering());
    }

    @Override
    protected void save()
    {
        CanvasPreferences.setMouseWheelSensitivity(((Double) wheelAxisSensitivity.getValue()).doubleValue());
        CanvasPreferences.setInvertMouseWheelAxis(invertWheelAxisCheckBox.isSelected());
        CanvasPreferences.setFiltering(filteringCheckBox.isSelected());
    }

}

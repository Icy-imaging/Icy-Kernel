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
package icy.gui.inspector;

import icy.action.RoiActions;
import icy.clipboard.Clipboard;
import icy.clipboard.Clipboard.ClipboardListener;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.SpecialValueSpinner;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.component.model.SpecialValueSpinnerModel;
import icy.gui.inspector.RoisPanel.ROIInfo;
import icy.main.Icy;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.roi.edit.BoundsROIEdit;
import icy.roi.edit.PositionROIEdit;
import icy.roi.edit.PropertyROIsEdit;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class RoiControlPanel extends JPanel implements ColorChangeListener, TextChangeListener, ClipboardListener,
        ChangeListener, ActionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7403770406075917063L;

    // GUI
    JLabel posCFieldLabel;
    JLabel posTFieldLabel;
    JLabel posZFieldLabel;
    JLabel posZSpinnerLabel;
    JLabel posTSpinnerLabel;
    JLabel posCSpinnerLabel;
    IcyTextField posXField;
    IcyTextField posYField;
    IcyTextField posTField;
    IcyTextField posZField;
    IcyTextField sizeXField;
    IcyTextField sizeZField;
    IcyTextField sizeYField;
    IcyTextField sizeTField;
    IcyTextField posCField;
    IcyTextField sizeCField;
    JSeparator separator;
    JSeparator separator_1;
    JSeparator separator_2;
    Component horizontalGlue;
    Component horizontalGlue_1;
    SpecialValueSpinner posZSpinner;
    SpecialValueSpinner posTSpinner;
    SpecialValueSpinner posCSpinner;
    ColorChooserButton colorButton;
    JSlider alphaSlider;
    IcyButton notButton;
    IcyButton orButton;
    IcyButton andButton;
    IcyButton xorButton;
    IcyButton subButton;
    IcyButton deleteButton;
    IcyButton loadButton;
    IcyButton saveButton;
    IcyButton copyButton;
    IcyButton pasteButton;
    IcyButton copyLinkButton;
    IcyButton pasteLinkButton;
    IcyButton xlsExportButton;
    JSpinner strokeSpinner;
    JCheckBox displayNameCheckBox;

    // internals
    private final RoisPanel roisPanel;
    final Semaphore modifyingRoi;
    private List<ROI> modifiedRois;
    final Runnable roiActionsRefresher;
    final Runnable roiPropertiesRefresher;

    public RoiControlPanel(RoisPanel roisPanel)
    {
        super();

        this.roisPanel = roisPanel;

        modifyingRoi = new Semaphore(1);
        modifiedRois = null;

        roiActionsRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshROIActionsInternal();
            }
        };
        roiPropertiesRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshROIPropertiesInternal();
            }
        };

        initialize();

        colorButton.addColorChangeListener(this);
        strokeSpinner.addChangeListener(this);
        alphaSlider.addChangeListener(this);
        displayNameCheckBox.addActionListener(this);

        posXField.addTextChangeListener(this);
        posYField.addTextChangeListener(this);
        posZField.addTextChangeListener(this);
        posZSpinner.addChangeListener(this);
        posTField.addTextChangeListener(this);
        posTSpinner.addChangeListener(this);
        posCField.addTextChangeListener(this);
        posCSpinner.addChangeListener(this);
        sizeXField.addTextChangeListener(this);
        sizeYField.addTextChangeListener(this);
        sizeZField.addTextChangeListener(this);
        sizeTField.addTextChangeListener(this);
        sizeCField.addTextChangeListener(this);

        Clipboard.addListener(this);

        refreshROIActionsInternal();
        refreshROIPropertiesInternal();
    }

    private void initialize()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel actionPanel = new JPanel();
        actionPanel.setBorder(new TitledBorder(null, "Action", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(actionPanel);
        GridBagLayout gbl_actionPanel = new GridBagLayout();
        gbl_actionPanel.columnWidths = new int[] {0, 0, 0, 60, 0};
        gbl_actionPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
        gbl_actionPanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_actionPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        actionPanel.setLayout(gbl_actionPanel);

        JToolBar toolBar = new JToolBar();
        toolBar.setRollover(true);
        GridBagConstraints gbc_toolBar = new GridBagConstraints();
        gbc_toolBar.anchor = GridBagConstraints.WEST;
        gbc_toolBar.gridwidth = 4;
        gbc_toolBar.insets = new Insets(0, 0, 5, 0);
        gbc_toolBar.gridx = 0;
        gbc_toolBar.gridy = 0;
        actionPanel.add(toolBar, gbc_toolBar);
        toolBar.setFloatable(false);

        loadButton = new IcyButton(RoiActions.loadAction);
        loadButton.setHideActionText(true);
        toolBar.add(loadButton);
        saveButton = new IcyButton(RoiActions.saveAction);
        saveButton.setHideActionText(true);
        toolBar.add(saveButton);
        separator = new JSeparator();
        separator.setMaximumSize(new Dimension(2, 32767));
        separator.setOrientation(SwingConstants.VERTICAL);
        toolBar.add(separator);
        copyButton = new IcyButton(RoiActions.copyAction);
        copyButton.setHideActionText(true);
        toolBar.add(copyButton);
        pasteButton = new IcyButton(RoiActions.pasteAction);
        pasteButton.setHideActionText(true);
        toolBar.add(pasteButton);
        copyLinkButton = new IcyButton(RoiActions.copyLinkAction);
        copyLinkButton.setHideActionText(true);
        toolBar.add(copyLinkButton);
        pasteLinkButton = new IcyButton(RoiActions.pasteLinkAction);
        pasteLinkButton.setHideActionText(true);
        toolBar.add(pasteLinkButton);
        separator_1 = new JSeparator();
        separator_1.setMaximumSize(new Dimension(2, 32767));
        separator_1.setOrientation(SwingConstants.VERTICAL);
        toolBar.add(separator_1);

        xlsExportButton = new IcyButton(RoiActions.xlsExportAction);
        toolBar.add(xlsExportButton);

        horizontalGlue_1 = Box.createHorizontalGlue();
        toolBar.add(horizontalGlue_1);

        JToolBar toolBar_1 = new JToolBar();
        toolBar_1.setRollover(true);
        GridBagConstraints gbc_toolBar_1 = new GridBagConstraints();
        gbc_toolBar_1.anchor = GridBagConstraints.WEST;
        gbc_toolBar_1.insets = new Insets(0, 0, 5, 0);
        gbc_toolBar_1.gridwidth = 4;
        gbc_toolBar_1.gridx = 0;
        gbc_toolBar_1.gridy = 1;
        actionPanel.add(toolBar_1, gbc_toolBar_1);
        toolBar_1.setFloatable(false);

        notButton = new IcyButton(RoiActions.boolNotAction);
        notButton.setHideActionText(true);
        toolBar_1.add(notButton);
        orButton = new IcyButton(RoiActions.boolOrAction);
        orButton.setHideActionText(true);
        toolBar_1.add(orButton);
        andButton = new IcyButton(RoiActions.boolAndAction);
        andButton.setHideActionText(true);
        toolBar_1.add(andButton);
        xorButton = new IcyButton(RoiActions.boolXorAction);
        xorButton.setHideActionText(true);
        toolBar_1.add(xorButton);
        subButton = new IcyButton(RoiActions.boolSubtractAction);
        subButton.setToolTipText("Create 2 ROIs representing the result of (A - B) and (B - A)");
        subButton.setHideActionText(true);
        toolBar_1.add(subButton);

        separator_2 = new JSeparator();
        separator_2.setPreferredSize(new Dimension(8, 2));
        separator_2.setMaximumSize(new Dimension(8, 32767));
        separator_2.setOrientation(SwingConstants.VERTICAL);
        toolBar_1.add(separator_2);
        deleteButton = new IcyButton(RoiActions.deleteAction);
        toolBar_1.add(deleteButton);

        horizontalGlue = Box.createHorizontalGlue();
        toolBar_1.add(horizontalGlue);

        final JLabel lblColor = new JLabel("Color");
        GridBagConstraints gbc_lblColor = new GridBagConstraints();
        gbc_lblColor.anchor = GridBagConstraints.WEST;
        gbc_lblColor.insets = new Insets(0, 0, 5, 5);
        gbc_lblColor.gridx = 0;
        gbc_lblColor.gridy = 2;
        actionPanel.add(lblColor, gbc_lblColor);

        colorButton = new ColorChooserButton();
        GridBagConstraints gbc_colorButton = new GridBagConstraints();
        gbc_colorButton.anchor = GridBagConstraints.WEST;
        gbc_colorButton.insets = new Insets(0, 0, 5, 5);
        gbc_colorButton.gridx = 1;
        gbc_colorButton.gridy = 2;
        actionPanel.add(colorButton, gbc_colorButton);
        colorButton.setToolTipText("ROI color");

        JLabel lblContentOpacity = new JLabel("Opacity");
        GridBagConstraints gbc_lblContentOpacity = new GridBagConstraints();
        gbc_lblContentOpacity.anchor = GridBagConstraints.WEST;
        gbc_lblContentOpacity.insets = new Insets(0, 0, 5, 5);
        gbc_lblContentOpacity.gridx = 2;
        gbc_lblContentOpacity.gridy = 2;
        actionPanel.add(lblContentOpacity, gbc_lblContentOpacity);

        alphaSlider = new JSlider();
        GridBagConstraints gbc_alphaSlider = new GridBagConstraints();
        gbc_alphaSlider.fill = GridBagConstraints.HORIZONTAL;
        gbc_alphaSlider.insets = new Insets(0, 0, 5, 0);
        gbc_alphaSlider.gridx = 3;
        gbc_alphaSlider.gridy = 2;
        actionPanel.add(alphaSlider, gbc_alphaSlider);
        alphaSlider.setPreferredSize(new Dimension(80, 20));
        alphaSlider.setMaximumSize(new Dimension(32767, 20));
        alphaSlider.setMinimumSize(new Dimension(36, 20));
        alphaSlider.setFocusable(false);
        alphaSlider.setToolTipText("ROI content opacity");

        JLabel lblNewLabel = new JLabel("Stroke");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 3;
        actionPanel.add(lblNewLabel, gbc_lblNewLabel);

        strokeSpinner = new JSpinner();
        strokeSpinner.setToolTipText("ROI stroke size (visualization only)");
        strokeSpinner.setModel(new SpinnerNumberModel(1.0, 1.0, 9.0, 1.0));
        GridBagConstraints gbc_strokeSpinner = new GridBagConstraints();
        gbc_strokeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_strokeSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_strokeSpinner.gridx = 1;
        gbc_strokeSpinner.gridy = 3;
        actionPanel.add(strokeSpinner, gbc_strokeSpinner);

        displayNameCheckBox = new JCheckBox("Display ROI name");
        displayNameCheckBox.setMargin(new Insets(2, 0, 2, 2));
        displayNameCheckBox.setIconTextGap(10);
        displayNameCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        GridBagConstraints gbc_displayNameCheckBox = new GridBagConstraints();
        gbc_displayNameCheckBox.anchor = GridBagConstraints.WEST;
        gbc_displayNameCheckBox.gridwidth = 2;
        gbc_displayNameCheckBox.gridx = 2;
        gbc_displayNameCheckBox.gridy = 3;
        actionPanel.add(displayNameCheckBox, gbc_displayNameCheckBox);

        JPanel positionAndSizePanel = new JPanel();
        add(positionAndSizePanel);
        GridBagLayout gbl_positionAndSizePanel = new GridBagLayout();
        gbl_positionAndSizePanel.columnWidths = new int[] {0, 0, 0};
        gbl_positionAndSizePanel.rowHeights = new int[] {0, 0};
        gbl_positionAndSizePanel.columnWeights = new double[] {1.0, 1.0, Double.MIN_VALUE};
        gbl_positionAndSizePanel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        positionAndSizePanel.setLayout(gbl_positionAndSizePanel);

        JPanel positionPanel = new JPanel();
        positionPanel.setBorder(new TitledBorder(null, "Position", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_positionPanel = new GridBagConstraints();
        gbc_positionPanel.anchor = GridBagConstraints.NORTH;
        gbc_positionPanel.insets = new Insets(0, 0, 0, 5);
        gbc_positionPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_positionPanel.gridx = 0;
        gbc_positionPanel.gridy = 0;
        positionAndSizePanel.add(positionPanel, gbc_positionPanel);
        GridBagLayout gbl_positionPanel = new GridBagLayout();
        gbl_positionPanel.columnWidths = new int[] {20, 0, 0};
        gbl_positionPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_positionPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gbl_positionPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        positionPanel.setLayout(gbl_positionPanel);

        final JLabel lblX = new JLabel("X");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        positionPanel.add(lblX, gbc_lblX);

        posXField = new IcyTextField();
        GridBagConstraints gbc_posXField = new GridBagConstraints();
        gbc_posXField.insets = new Insets(0, 0, 5, 0);
        gbc_posXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posXField.gridx = 1;
        gbc_posXField.gridy = 0;
        positionPanel.add(posXField, gbc_posXField);
        posXField.setToolTipText("X position of the ROI");
        posXField.setColumns(8);

        final JLabel lblY = new JLabel("Y");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 0;
        gbc_lblY.gridy = 1;
        positionPanel.add(lblY, gbc_lblY);

        posYField = new IcyTextField();
        GridBagConstraints gbc_posYField = new GridBagConstraints();
        gbc_posYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posYField.insets = new Insets(0, 0, 5, 0);
        gbc_posYField.gridx = 1;
        gbc_posYField.gridy = 1;
        positionPanel.add(posYField, gbc_posYField);
        posYField.setToolTipText("Y position of the ROI");
        posYField.setColumns(8);

        posZFieldLabel = new JLabel("Z");
        GridBagConstraints gbc_posZFieldLabel = new GridBagConstraints();
        gbc_posZFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posZFieldLabel.gridx = 0;
        gbc_posZFieldLabel.gridy = 2;
        positionPanel.add(posZFieldLabel, gbc_posZFieldLabel);

        posZField = new IcyTextField();
        GridBagConstraints gbc_posZField = new GridBagConstraints();
        gbc_posZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posZField.insets = new Insets(0, 0, 5, 0);
        gbc_posZField.gridx = 1;
        gbc_posZField.gridy = 2;
        positionPanel.add(posZField, gbc_posZField);
        posZField.setVisible(false);
        posZField.setToolTipText("Z position of the ROI");
        posZField.setColumns(8);

        posZSpinnerLabel = new JLabel("Z");
        GridBagConstraints gbc_posZSpinnerLabel = new GridBagConstraints();
        gbc_posZSpinnerLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posZSpinnerLabel.gridx = 0;
        gbc_posZSpinnerLabel.gridy = 3;
        positionPanel.add(posZSpinnerLabel, gbc_posZSpinnerLabel);

        posZSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        GridBagConstraints gbc_posZSpinner = new GridBagConstraints();
        gbc_posZSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_posZSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_posZSpinner.gridx = 1;
        gbc_posZSpinner.gridy = 3;
        positionPanel.add(posZSpinner, gbc_posZSpinner);
        posZSpinner.setToolTipText("Attach the ROI to a specific Z slice (set to -1 for ALL)");

        posTFieldLabel = new JLabel("T");
        GridBagConstraints gbc_posTFieldLabel = new GridBagConstraints();
        gbc_posTFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posTFieldLabel.gridx = 0;
        gbc_posTFieldLabel.gridy = 4;
        positionPanel.add(posTFieldLabel, gbc_posTFieldLabel);

        posTField = new IcyTextField();
        GridBagConstraints gbc_posTField = new GridBagConstraints();
        gbc_posTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posTField.insets = new Insets(0, 0, 5, 0);
        gbc_posTField.gridx = 1;
        gbc_posTField.gridy = 4;
        positionPanel.add(posTField, gbc_posTField);
        posTField.setVisible(false);
        posTField.setToolTipText("T position of the ROI");
        posTField.setColumns(8);

        posTSpinnerLabel = new JLabel("T");
        GridBagConstraints gbc_posTSpinnerLabel = new GridBagConstraints();
        gbc_posTSpinnerLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posTSpinnerLabel.gridx = 0;
        gbc_posTSpinnerLabel.gridy = 5;
        positionPanel.add(posTSpinnerLabel, gbc_posTSpinnerLabel);

        posTSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        GridBagConstraints gbc_posTSpinner = new GridBagConstraints();
        gbc_posTSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_posTSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_posTSpinner.gridx = 1;
        gbc_posTSpinner.gridy = 5;
        positionPanel.add(posTSpinner, gbc_posTSpinner);
        posTSpinner.setToolTipText("Attach the ROI to a specific T frame (set to -1 for ALL)");

        posCFieldLabel = new JLabel("C");
        GridBagConstraints gbc_posCFieldLabel = new GridBagConstraints();
        gbc_posCFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posCFieldLabel.gridx = 0;
        gbc_posCFieldLabel.gridy = 6;
        positionPanel.add(posCFieldLabel, gbc_posCFieldLabel);

        posCField = new IcyTextField();
        GridBagConstraints gbc_posCField = new GridBagConstraints();
        gbc_posCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posCField.insets = new Insets(0, 0, 5, 0);
        gbc_posCField.gridx = 1;
        gbc_posCField.gridy = 6;
        positionPanel.add(posCField, gbc_posCField);
        posCField.setVisible(false);
        posCField.setToolTipText("C position of the ROI");
        posCField.setColumns(8);

        posCSpinnerLabel = new JLabel("C");
        GridBagConstraints gbc_posCSpinnerLabel = new GridBagConstraints();
        gbc_posCSpinnerLabel.insets = new Insets(0, 0, 0, 5);
        gbc_posCSpinnerLabel.gridx = 0;
        gbc_posCSpinnerLabel.gridy = 7;
        positionPanel.add(posCSpinnerLabel, gbc_posCSpinnerLabel);

        posCSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        GridBagConstraints gbc_posCSpinner = new GridBagConstraints();
        gbc_posCSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_posCSpinner.gridx = 1;
        gbc_posCSpinner.gridy = 7;
        positionPanel.add(posCSpinner, gbc_posCSpinner);
        posCSpinner.setToolTipText("Attach the ROI to a specific C channel (set to -1 for ALL)");

        JPanel sizePanel = new JPanel();
        sizePanel.setBorder(new TitledBorder(null, "Dimension", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_sizePanel = new GridBagConstraints();
        gbc_sizePanel.anchor = GridBagConstraints.NORTH;
        gbc_sizePanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizePanel.gridx = 1;
        gbc_sizePanel.gridy = 0;
        positionAndSizePanel.add(sizePanel, gbc_sizePanel);

        GridBagLayout gbl_sizePanel = new GridBagLayout();
        gbl_sizePanel.columnWidths = new int[] {20, 0, 0};
        gbl_sizePanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gbl_sizePanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gbl_sizePanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        sizePanel.setLayout(gbl_sizePanel);

        final JLabel lblNewLabel_2 = new JLabel("X");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 0;
        sizePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        sizeXField = new IcyTextField();
        GridBagConstraints gbc_sizeXField = new GridBagConstraints();
        gbc_sizeXField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeXField.gridx = 1;
        gbc_sizeXField.gridy = 0;
        sizePanel.add(sizeXField, gbc_sizeXField);
        sizeXField.setToolTipText("Size of dimension X for the ROI");
        sizeXField.setColumns(8);

        final JLabel lblY_1 = new JLabel("Y");
        GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.gridx = 0;
        gbc_lblY_1.gridy = 1;
        sizePanel.add(lblY_1, gbc_lblY_1);

        sizeYField = new IcyTextField();
        GridBagConstraints gbc_sizeYField = new GridBagConstraints();
        gbc_sizeYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeYField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeYField.gridx = 1;
        gbc_sizeYField.gridy = 1;
        sizePanel.add(sizeYField, gbc_sizeYField);
        sizeYField.setToolTipText("Size of dimension Y for the ROI");
        sizeYField.setColumns(8);

        JLabel sizeZFieldLabel = new JLabel("Z");
        GridBagConstraints gbc_sizeZFieldLabel = new GridBagConstraints();
        gbc_sizeZFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_sizeZFieldLabel.gridx = 0;
        gbc_sizeZFieldLabel.gridy = 2;
        sizePanel.add(sizeZFieldLabel, gbc_sizeZFieldLabel);

        sizeZField = new IcyTextField();
        GridBagConstraints gbc_sizeZField = new GridBagConstraints();
        gbc_sizeZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeZField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeZField.gridx = 1;
        gbc_sizeZField.gridy = 2;
        sizePanel.add(sizeZField, gbc_sizeZField);
        sizeZField.setToolTipText("Size of dimension Z for the ROI");
        sizeZField.setColumns(8);

        JLabel sizeTFieldLabel = new JLabel("T");
        GridBagConstraints gbc_sizeTFieldLabel = new GridBagConstraints();
        gbc_sizeTFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_sizeTFieldLabel.gridx = 0;
        gbc_sizeTFieldLabel.gridy = 3;
        sizePanel.add(sizeTFieldLabel, gbc_sizeTFieldLabel);

        sizeTField = new IcyTextField();
        GridBagConstraints gbc_sizeTField = new GridBagConstraints();
        gbc_sizeTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeTField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeTField.gridx = 1;
        gbc_sizeTField.gridy = 3;
        sizePanel.add(sizeTField, gbc_sizeTField);
        sizeTField.setToolTipText("Size of dimension T for the ROI");
        sizeTField.setColumns(8);

        JLabel sizeCFieldLabel = new JLabel("C");
        GridBagConstraints gbc_sizeCFieldLabel = new GridBagConstraints();
        gbc_sizeCFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_sizeCFieldLabel.gridx = 0;
        gbc_sizeCFieldLabel.gridy = 4;
        sizePanel.add(sizeCFieldLabel, gbc_sizeCFieldLabel);

        sizeCField = new IcyTextField();
        GridBagConstraints gbc_sizeCField = new GridBagConstraints();
        gbc_sizeCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeCField.gridx = 1;
        gbc_sizeCField.gridy = 4;
        sizePanel.add(sizeCField, gbc_sizeCField);
        sizeCField.setToolTipText("Size of dimension C for the ROI");
        sizeCField.setColumns(8);
    }

    public void selectionChanged()
    {
        refreshROIActionsAndProperties();
    }

    /**
     * Get the visible ROI in the ROI control panel
     */
    List<ROI> getVisibleRois()
    {
        return roisPanel.getVisibleRois();
    }

    /**
     * Get the selected ROI in the ROI control panel
     */
    List<ROI> getSelectedRois()
    {
        return getSelectedRois(true);
    }

    /**
     * Get the selected ROI in the ROI control panel.
     * 
     * @param wantReadOnly
     *        If <code>true</code> the returned list will also contains ROI in Read-Only state
     */
    List<ROI> getSelectedRois(boolean wantReadOnly)
    {
        final List<ROI> selected = roisPanel.getSelectedRois();

        if (wantReadOnly)
            return selected;

        final List<ROI> result = new ArrayList<ROI>();

        for (ROI roi : selected)
            if (!roi.isReadOnly())
                result.add(roi);

        return result;
    }

    /**
     * Refresh the ROI actions state.
     */
    public void refreshROIActions()
    {
        ThreadUtil.runSingle(roiActionsRefresher);
    }

    /**
     * Refresh the ROI actions state (internal)
     */
    void refreshROIActionsInternal()
    {
        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        final List<ROI> selectedRois = getSelectedRois();
        final ROI roi = (selectedRois.size() > 0) ? selectedRois.get(0) : null;

        boolean readOnly = true;
        // set read only flag
        for (ROI r : selectedRois)
            readOnly &= r.isReadOnly();

        final boolean hasSequence = (sequence != null);
        final boolean hasSelected = (roi != null);
        final boolean twoSelected = (selectedRois.size() == 2);
        final boolean multiSelect = (selectedRois.size() > 1);
        final boolean singleSelect = hasSelected && !multiSelect;
        final boolean canSetBounds = hasSelected ? roi.canSetBounds() : false;
        final boolean canSetPosition = hasSelected ? roi.canSetPosition() : false;
        final boolean hasROIinClipboard = Clipboard.isType(Clipboard.TYPE_ROILIST);
        final boolean hasROILinkinClipboard = Clipboard.isType(Clipboard.TYPE_ROILINKLIST);
        final boolean editable = !readOnly;
        final int dim = hasSelected ? roi.getDimension() : 0;

        // wait a bit to avoid eating too much time with refresh
        ThreadUtil.sleep(1);

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                modifyingRoi.acquireUninterruptibly();
                try
                {
                    if (hasSequence)
                    {
                        ((SpecialValueSpinnerModel) posZSpinner.getModel()).setMaximum(Integer.valueOf(sequence
                                .getSizeZ() - 1));
                        ((SpecialValueSpinnerModel) posTSpinner.getModel()).setMaximum(Integer.valueOf(sequence
                                .getSizeT() - 1));
                        ((SpecialValueSpinnerModel) posCSpinner.getModel()).setMaximum(Integer.valueOf(sequence
                                .getSizeC() - 1));
                    }
                    else
                    {
                        ((SpecialValueSpinnerModel) posZSpinner.getModel()).setMaximum(Integer.valueOf(0));
                        ((SpecialValueSpinnerModel) posTSpinner.getModel()).setMaximum(Integer.valueOf(0));
                        ((SpecialValueSpinnerModel) posCSpinner.getModel()).setMaximum(Integer.valueOf(0));
                    }
                }
                finally
                {
                    modifyingRoi.release();
                }

                posXField.setEnabled(singleSelect && canSetPosition && editable);
                posYField.setEnabled(singleSelect && canSetPosition && editable);
                posZField.setEnabled(singleSelect && canSetPosition && editable);
                posTField.setEnabled(singleSelect && canSetPosition && editable);
                posCField.setEnabled(singleSelect && canSetPosition && editable);
                posZSpinner.setEnabled(singleSelect && canSetPosition && editable);
                posTSpinner.setEnabled(singleSelect && canSetPosition && editable);
                posCSpinner.setEnabled(singleSelect && canSetPosition && editable);
                sizeXField.setEnabled(singleSelect && canSetBounds && editable);
                sizeYField.setEnabled(singleSelect && canSetBounds && editable && (dim > 1));
                sizeZField.setEnabled(singleSelect && canSetBounds && editable && (dim > 2));
                sizeTField.setEnabled(singleSelect && canSetBounds && editable && (dim > 3));
                sizeCField.setEnabled(singleSelect && canSetBounds && editable && (dim > 4));

                if (dim > 2)
                {
                    posZField.setVisible(true);
                    posZFieldLabel.setVisible(true);
                    posZSpinner.setVisible(false);
                    posZSpinnerLabel.setVisible(false);
                }
                else
                {
                    posZField.setVisible(false);
                    posZFieldLabel.setVisible(false);
                    posZSpinner.setVisible(true);
                    posZSpinnerLabel.setVisible(true);
                }

                if (dim > 3)
                {
                    posTField.setVisible(true);
                    posTFieldLabel.setVisible(true);
                    posTSpinner.setVisible(false);
                    posTSpinnerLabel.setVisible(false);
                }
                else
                {
                    posTField.setVisible(false);
                    posTFieldLabel.setVisible(false);
                    posTSpinner.setVisible(true);
                    posTSpinnerLabel.setVisible(true);
                }

                if (dim > 4)
                {
                    posCField.setVisible(true);
                    posCFieldLabel.setVisible(true);
                    posCSpinner.setVisible(false);
                    posCSpinnerLabel.setVisible(false);
                }
                else
                {
                    posCField.setVisible(false);
                    posCFieldLabel.setVisible(false);
                    posCSpinner.setVisible(true);
                    posCSpinnerLabel.setVisible(true);
                }

                colorButton.setEnabled(hasSelected && editable);
                strokeSpinner.setEnabled(hasSelected && editable);
                alphaSlider.setEnabled(hasSelected);

                loadButton.setEnabled(hasSequence);
                saveButton.setEnabled(hasSelected);
                copyButton.setEnabled(hasSelected);
                pasteButton.setEnabled(hasROIinClipboard);
                copyLinkButton.setEnabled(hasSelected);
                pasteLinkButton.setEnabled(hasROILinkinClipboard);

                deleteButton.setEnabled(hasSelected && editable);

                notButton.setEnabled(singleSelect);
                orButton.setEnabled(multiSelect);
                andButton.setEnabled(multiSelect);
                xorButton.setEnabled(multiSelect);
                subButton.setEnabled(twoSelected);

                xlsExportButton.setEnabled(getVisibleRois().size() > 0);

                displayNameCheckBox.setEnabled(hasSelected);
            }
        });
    }

    /**
     * Refresh ROI properties
     */
    public void refreshROIProperties()
    {
        ThreadUtil.runSingle(roiPropertiesRefresher);
    }

    /**
     * Refresh ROI properties (internal)
     */
    void refreshROIPropertiesInternal()
    {
        final List<ROI> rois = getSelectedRois();
        final ROI roi = (rois.size() > 0) ? rois.get(0) : null;
        final ROIInfo roiInfo = (roi != null) ? roisPanel.getROIInfo(roi) : null;

        // wait a bit to avoid eating too much time with refresh
        ThreadUtil.sleep(1);

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                modifyingRoi.acquireUninterruptibly();
                try
                {
                    if (roi != null)
                    {
                        colorButton.setColor(roi.getColor());
                        strokeSpinner.setValue(Double.valueOf(roi.getStroke()));
                        alphaSlider.setValue((int) (roi.getOpacity() * 100));
                        displayNameCheckBox.setSelected(roi.getShowName());
                    }
                    else
                    {
                        // no ROI selected
                        colorButton.setColor(Color.gray);
                        strokeSpinner.setValue(Double.valueOf(1d));
                        alphaSlider.setValue(0);
                        displayNameCheckBox.setSelected(false);
                    }

                    if (roiInfo != null)
                    {
                        double value;

                        posXField.setText(StringUtil.toString(roiInfo.getPositionX()));
                        posYField.setText(StringUtil.toString(roiInfo.getPositionY()));
                        value = roiInfo.getPositionZ();
                        posZSpinner.setValue(Integer.valueOf((int) value));
                        posZField.setText(StringUtil.toString(value));
                        value = roiInfo.getPositionT();
                        posTSpinner.setValue(Integer.valueOf((int) value));
                        posTField.setText(StringUtil.toString(value));
                        value = roiInfo.getPositionC();
                        posCSpinner.setValue(Integer.valueOf((int) value));
                        posCField.setText(StringUtil.toString(value));

                        sizeXField.setText(roiInfo.getSizeXAsString());
                        sizeYField.setText(roiInfo.getSizeYAsString());
                        sizeZField.setText(roiInfo.getSizeZAsString());
                        sizeTField.setText(roiInfo.getSizeTAsString());
                        sizeCField.setText(roiInfo.getSizeCAsString());
                    }
                    else
                    {
                        posXField.setText("");
                        posYField.setText("");
                        posZField.setText("");
                        posTField.setText("");
                        posCField.setText("");
                        posZSpinner.setValue(Integer.valueOf(0));
                        posTSpinner.setValue(Integer.valueOf(0));
                        posCSpinner.setValue(Integer.valueOf(0));

                        sizeXField.setText("");
                        sizeYField.setText("");
                        sizeZField.setText("");
                        sizeTField.setText("");
                        sizeCField.setText("");
                    }
                }
                finally
                {
                    modifyingRoi.release();
                }
            }
        });
    }

    /**
     * Refresh ROI actions and properties
     */
    public void refreshROIActionsAndProperties()
    {
        ThreadUtil.runSingle(roiActionsRefresher);
        ThreadUtil.runSingle(roiPropertiesRefresher);
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        // source not anymore enable --> cancel validation
        if (!source.isEnabled())
            return;

        // keep trace of modified ROI and wait for validation
        if (!validate)
        {
            modifiedRois = getSelectedRois();
            return;
        }

        // at this point the text is validated...

        // can't edit multiple ROI at same time (should not arrive)
        if ((modifiedRois == null) || (modifiedRois.size() != 1))
            return;

        // get the ROI we were modifying
        final ROI roi = modifiedRois.get(0);

        // can't edit read only ROI (should not arrive)
        if (roi.isReadOnly())
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        try
        {
            // position fields ?
            if ((source == posXField) || (source == posYField) || (source == posZField) || (source == posTField)
                    || (source == posCField))
            {
                // get current ROI position
                final Point5D savePosition = roi.getPosition5D();
                Point5D position = (Point5D) savePosition.clone();

                // roi support position change ?
                if (roi.canSetPosition())
                {
                    final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                    if (!Double.isNaN(value))
                    {
                        if (source == posXField)
                            position.setX(value);
                        else if (source == posYField)
                            position.setY(value);
                        else if (source == posZField)
                            position.setZ(value);
                        else if (source == posTField)
                            position.setT(value);
                        else
                            position.setC(value);

                        roi.setPosition5D(position);
                        // update position with ROI accepted values
                        position = roi.getPosition5D();

                        // add position change to undo manager
                        Icy.getMainInterface().getUndoManager().addEdit(new PositionROIEdit(roi, savePosition));
                    }
                }

                double p;

                // fix field value if needed
                if (source == posXField)
                    p = position.getX();
                else if (source == posYField)
                    p = position.getY();
                else if (source == posZField)
                    p = position.getZ();
                else if (source == posTField)
                    p = position.getT();
                else
                    p = position.getC();

                // change infinite by -1
                if (p == Double.NEGATIVE_INFINITY)
                    p = -1d;

                source.setText(Double.toString(p));
            }
            // size fields ?
            else if ((source == sizeXField) || (source == sizeYField) || (source == sizeZField)
                    || (source == sizeTField) || (source == sizeCField))
            {
                // get current ROI size
                final Rectangle5D saveBounds = roi.getBounds5D();
                Rectangle5D bounds = (Rectangle5D) saveBounds.clone();

                // roi support size change ?
                if (roi.canSetBounds())
                {
                    final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                    if (!Double.isNaN(value))
                    {
                        if (source == sizeXField)
                            bounds.setSizeX(value);
                        else if (source == sizeYField)
                            bounds.setSizeY(value);
                        else if (source == sizeZField)
                            bounds.setSizeZ(value);
                        else if (source == sizeTField)
                            bounds.setSizeT(value);
                        else
                            bounds.setSizeC(value);

                        roi.setBounds5D(bounds);
                        // update bounds with ROI accepted values
                        bounds = roi.getBounds5D();

                        // add position change to undo manager
                        Icy.getMainInterface().getUndoManager().addEdit(new BoundsROIEdit(roi, saveBounds));
                    }
                }

                final double p;

                // fix field value if needed
                if (source == sizeXField)
                    p = bounds.getSizeX();
                else if (source == sizeYField)
                    p = bounds.getSizeY();
                else if (source == sizeZField)
                    p = bounds.getSizeZ();
                else if (source == sizeTField)
                    p = bounds.getSizeT();
                else
                    p = bounds.getSizeC();

                source.setText(Double.toString(p));
            }
        }
        finally
        {
            modifyingRoi.release();
        }
    }

    @Override
    public void colorChanged(ColorChooserButton source)
    {
        // source not anymore enable --> cancel change
        if (!source.isEnabled())
            return;

        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        if (sequence == null)
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        final List<ROI> rois = getSelectedRois(false);
        final List<Object> oldValues = new ArrayList<Object>();
        final Color color = source.getColor();

        sequence.beginUpdate();
        try
        {
            // set new color
            for (ROI roi : rois)
            {
                // save previous color
                oldValues.add(roi.getColor());
                roi.setColor(color);
            }
        }
        finally
        {
            sequence.endUpdate();
            modifyingRoi.release();
        }

        // add color change to undo manager
        sequence.addUndoableEdit(new PropertyROIsEdit(rois, ROI.PROPERTY_COLOR, oldValues, color));
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (!(e.getSource() instanceof JComponent))
            return;

        final JComponent source = (JComponent) e.getSource();

        // source not anymore enable --> cancel change
        if (!source.isEnabled())
            return;

        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        if (sequence == null)
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        try
        {
            if (source == strokeSpinner)
            {
                final List<ROI> rois = getSelectedRois(false);
                final List<Object> oldValues = new ArrayList<Object>();
                final double stroke = ((Double) strokeSpinner.getValue()).doubleValue();

                sequence.beginUpdate();
                try
                {
                    for (ROI roi : rois)
                    {
                        // save previous stroke
                        oldValues.add(Double.valueOf(roi.getStroke()));
                        roi.setStroke(stroke);
                    }
                }
                finally
                {
                    sequence.endUpdate();
                }

                // add stroke change to undo manager
                sequence.addUndoableEdit(new PropertyROIsEdit(rois, ROI.PROPERTY_STROKE, oldValues, Double
                        .valueOf(stroke)));
            }
            else if (source == alphaSlider)
            {
                final List<ROI> rois = getSelectedRois(true);
                final List<Object> oldValues = new ArrayList<Object>();
                final float opacity = alphaSlider.getValue() / 100f;

                sequence.beginUpdate();
                try
                {
                    for (ROI roi : rois)
                    {
                        // save previous opacity
                        oldValues.add(Float.valueOf(roi.getOpacity()));
                        roi.setOpacity(opacity);
                    }
                }
                finally
                {
                    sequence.endUpdate();
                }

                // add opacity change to undo manager
                sequence.addUndoableEdit(new PropertyROIsEdit(rois, ROI.PROPERTY_OPACITY, oldValues, Float
                        .valueOf(opacity)));
            }
            else if ((source == posZSpinner) || (source == posTSpinner) || (source == posCSpinner))
            {
                final List<ROI> rois = getSelectedRois();

                // can't edit multiple ROI at same time (should not arrive)
                if ((rois == null) || (rois.size() != 1))
                    return;

                // get the ROI we were modifying
                final ROI roi = rois.get(0);

                // can't edit read only ROI (should not arrive)
                if (roi.isReadOnly())
                    return;

                final SpecialValueSpinner spinner = (SpecialValueSpinner) source;

                // get current ROI position
                final Point5D savePosition = roi.getPosition5D();
                Point5D position = (Point5D) savePosition.clone();

                // roi support position change ?
                if (roi.canSetPosition())
                {
                    final double value = ((Integer) spinner.getValue()).intValue();

                    if (source == posZSpinner)
                        position.setZ(value);
                    else if (source == posTSpinner)
                        position.setT(value);
                    else
                        position.setC(value);

                    roi.setPosition5D(position);
                    // update position with ROI accepted values
                    position = roi.getPosition5D();

                    // add position change to undo manager
                    sequence.addUndoableEdit(new PositionROIEdit(roi, savePosition));
                }

                double p;

                // fix field value if needed
                if (source == posZSpinner)
                    p = position.getZ();
                else if (source == posTSpinner)
                    p = position.getT();
                else
                    p = position.getC();

                // change infinite by -1
                if (p == Double.NEGATIVE_INFINITY)
                    p = -1d;

                spinner.setValue(Integer.valueOf((int) p));
            }
        }
        finally
        {
            modifyingRoi.release();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (!(e.getSource() instanceof JComponent))
            return;

        final JComponent source = (JComponent) e.getSource();

        // source not anymore enable --> cancel change
        if (!source.isEnabled())
            return;

        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        if (sequence == null)
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        try
        {
            if (source == displayNameCheckBox)
            {
                sequence.beginUpdate();
                try
                {
                    final boolean display = displayNameCheckBox.isSelected();

                    for (ROI roi : getSelectedRois(false))
                        roi.setShowName(display);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }
        finally
        {
            modifyingRoi.release();
        }
    }

    @Override
    public void clipboardChanged()
    {
        refreshROIActions();
    }

    // one of the selected ROI changed
    public void roiChanged(ROIEvent event)
    {
        switch (event.getType())
        {
            case ROI_CHANGED:
                // refresh the properties
                refreshROIProperties();
                break;

            case FOCUS_CHANGED:
                // nothing to do here
                break;

            case PROPERTY_CHANGED:
                final String propertyName = event.getPropertyName();

                if (ROI.PROPERTY_READONLY.equals(propertyName))
                    refreshROIActions();
                break;

            case SELECTION_CHANGED:
                // handle externally with the setSelectedROI() method
                break;
        }
    }

}

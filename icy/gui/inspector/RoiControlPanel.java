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

import icy.clipboard.Clipboard;
import icy.clipboard.Clipboard.ClipboardListener;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.component.model.SpecialValueSpinnerModel;
import icy.gui.component.swing.SpecialValueSpinner;
import icy.gui.menu.action.RoiActions;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class RoiControlPanel extends JPanel implements ColorChangeListener, TextChangeListener, ClipboardListener,
        ChangeListener, ROIListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7403770406075917063L;

    // GUI
    private JLabel sizeZFieldLabel;
    private JLabel sizeTFieldLabel;
    private JLabel sizeCFieldLabel;
    private JLabel posCFieldLabel;
    private JLabel posTFieldLabel;
    private JLabel posZFieldLabel;
    private IcyTextField nameField;
    private IcyTextField posXField;
    private IcyTextField posYField;
    private IcyTextField posTField;
    private IcyTextField posZField;
    private IcyTextField sizeXField;
    private IcyTextField sizeZField;
    private IcyTextField sizeYField;
    private IcyTextField sizeTField;
    private IcyTextField posCField;
    private IcyTextField sizeCField;
    private SpecialValueSpinner posZSpinner;
    private SpecialValueSpinner posTSpinner;
    private SpecialValueSpinner posCSpinner;
    private ColorChooserButton colorButton;
    private JSlider alphaSlider;
    private JLabel lblContentOpacity;
    private IcyButton notButton;
    private IcyButton orButton;
    private IcyButton andButton;
    private IcyButton xorButton;
    private IcyButton subButton;
    private IcyButton deleteButton;
    private JPanel generalPanel;
    private RoiExtraInfoPanel infosPanel;
    private JLabel lblBoolean;
    private JLabel lblGeneral;
    private IcyButton loadButton;
    private IcyButton saveButton;
    private IcyButton copyButton;
    private IcyButton pasteButton;
    private IcyButton copyLinkButton;
    private IcyButton pasteLinkButton;

    // internals
    private final Semaphore modifyingRoi;
    private List<ROI> modifiedRois;
    private List<ROI> selectedRois;
    final Runnable roiActionsRefresher;
    final Runnable roiPropertiesRefresher;

    public RoiControlPanel()
    {
        super();

        modifyingRoi = new Semaphore(1);
        modifiedRois = null;
        selectedRois = new ArrayList<ROI>();

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

        nameField.addTextChangeListener(this);

        colorButton.addColorChangeListener(this);
        alphaSlider.addChangeListener(this);

        Clipboard.addListener(this);

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

        buildActionMap();

        refreshROIActionsInternal();
        refreshROIPropertiesInternal();
    }

    void buildActionMap()
    {
        final InputMap imap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final ActionMap amap = getActionMap();

        imap.put(RoiActions.unselectAction.getKeyStroke(), RoiActions.unselectAction.getName());
        imap.put(RoiActions.deleteAction.getKeyStroke(), RoiActions.deleteAction.getName());
        // also allow backspace key for delete operation here
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), RoiActions.deleteAction.getName());
        imap.put(RoiActions.copyAction.getKeyStroke(), RoiActions.copyAction.getName());
        imap.put(RoiActions.pasteAction.getKeyStroke(), RoiActions.pasteAction.getName());
        imap.put(RoiActions.copyLinkAction.getKeyStroke(), RoiActions.copyLinkAction.getName());
        imap.put(RoiActions.pasteLinkAction.getKeyStroke(), RoiActions.pasteLinkAction.getName());

        amap.put(RoiActions.unselectAction.getName(), RoiActions.unselectAction);
        amap.put(RoiActions.deleteAction.getName(), RoiActions.deleteAction);
        amap.put(RoiActions.copyAction.getName(), RoiActions.copyAction);
        amap.put(RoiActions.pasteAction.getName(), RoiActions.pasteAction);
        amap.put(RoiActions.copyLinkAction.getName(), RoiActions.copyLinkAction);
        amap.put(RoiActions.pasteLinkAction.getName(), RoiActions.pasteLinkAction);
    }

    private void initialize()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        final JPanel booleanOpPanel = new JPanel();
        booleanOpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Operation",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(booleanOpPanel);
        GridBagLayout gbl_booleanOpPanel = new GridBagLayout();
        gbl_booleanOpPanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        gbl_booleanOpPanel.rowHeights = new int[] {0, 0, 0};
        gbl_booleanOpPanel.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_booleanOpPanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        booleanOpPanel.setLayout(gbl_booleanOpPanel);

        lblGeneral = new JLabel("General");
        GridBagConstraints gbc_lblGeneral = new GridBagConstraints();
        gbc_lblGeneral.anchor = GridBagConstraints.EAST;
        gbc_lblGeneral.insets = new Insets(0, 0, 5, 5);
        gbc_lblGeneral.gridx = 0;
        gbc_lblGeneral.gridy = 0;
        booleanOpPanel.add(lblGeneral, gbc_lblGeneral);

        loadButton = new IcyButton(RoiActions.loadAction);
        loadButton.setText(null);
        GridBagConstraints gbc_loadRoiButton = new GridBagConstraints();
        gbc_loadRoiButton.insets = new Insets(0, 0, 5, 5);
        gbc_loadRoiButton.gridx = 1;
        gbc_loadRoiButton.gridy = 0;
        booleanOpPanel.add(loadButton, gbc_loadRoiButton);

        saveButton = new IcyButton(RoiActions.saveAction);
        saveButton.setText(null);
        GridBagConstraints gbc_saveRoiButton = new GridBagConstraints();
        gbc_saveRoiButton.insets = new Insets(0, 0, 5, 5);
        gbc_saveRoiButton.gridx = 2;
        gbc_saveRoiButton.gridy = 0;
        booleanOpPanel.add(saveButton, gbc_saveRoiButton);

        copyButton = new IcyButton(RoiActions.copyAction);
        copyButton.setText(null);
        GridBagConstraints gbc_copyButton = new GridBagConstraints();
        gbc_copyButton.insets = new Insets(0, 0, 5, 5);
        gbc_copyButton.gridx = 3;
        gbc_copyButton.gridy = 0;
        booleanOpPanel.add(copyButton, gbc_copyButton);

        pasteButton = new IcyButton(RoiActions.pasteAction);
        pasteButton.setText(null);
        GridBagConstraints gbc_pasteButton = new GridBagConstraints();
        gbc_pasteButton.insets = new Insets(0, 0, 5, 5);
        gbc_pasteButton.gridx = 4;
        gbc_pasteButton.gridy = 0;
        booleanOpPanel.add(pasteButton, gbc_pasteButton);

        copyLinkButton = new IcyButton(RoiActions.copyLinkAction);
        copyLinkButton.setText(null);
        GridBagConstraints gbc_copyLinkButton = new GridBagConstraints();
        gbc_copyLinkButton.insets = new Insets(0, 0, 5, 5);
        gbc_copyLinkButton.gridx = 5;
        gbc_copyLinkButton.gridy = 0;
        booleanOpPanel.add(copyLinkButton, gbc_copyLinkButton);

        pasteLinkButton = new IcyButton(RoiActions.pasteLinkAction);
        pasteLinkButton.setText(null);
        GridBagConstraints gbc_pasteLinkButton = new GridBagConstraints();
        gbc_pasteLinkButton.anchor = GridBagConstraints.WEST;
        gbc_pasteLinkButton.insets = new Insets(0, 0, 5, 0);
        gbc_pasteLinkButton.gridx = 6;
        gbc_pasteLinkButton.gridy = 0;
        booleanOpPanel.add(pasteLinkButton, gbc_pasteLinkButton);

        lblBoolean = new JLabel("Boolean");
        GridBagConstraints gbc_lblBoolean = new GridBagConstraints();
        gbc_lblBoolean.anchor = GridBagConstraints.EAST;
        gbc_lblBoolean.insets = new Insets(0, 0, 0, 5);
        gbc_lblBoolean.gridx = 0;
        gbc_lblBoolean.gridy = 1;
        booleanOpPanel.add(lblBoolean, gbc_lblBoolean);

        notButton = new IcyButton(RoiActions.boolNotAction);
        notButton.setText(null);
        GridBagConstraints gbc_notButton = new GridBagConstraints();
        gbc_notButton.insets = new Insets(0, 0, 0, 5);
        gbc_notButton.gridx = 1;
        gbc_notButton.gridy = 1;
        booleanOpPanel.add(notButton, gbc_notButton);

        orButton = new IcyButton(RoiActions.boolOrAction);
        orButton.setText(null);
        GridBagConstraints gbc_orButton = new GridBagConstraints();
        gbc_orButton.insets = new Insets(0, 0, 0, 5);
        gbc_orButton.gridx = 2;
        gbc_orButton.gridy = 1;
        booleanOpPanel.add(orButton, gbc_orButton);

        andButton = new IcyButton(RoiActions.boolAndAction);
        andButton.setText(null);
        GridBagConstraints gbc_andButton = new GridBagConstraints();
        gbc_andButton.insets = new Insets(0, 0, 0, 5);
        gbc_andButton.gridx = 3;
        gbc_andButton.gridy = 1;
        booleanOpPanel.add(andButton, gbc_andButton);

        xorButton = new IcyButton(RoiActions.boolXorAction);
        xorButton.setText(null);
        GridBagConstraints gbc_xorButton = new GridBagConstraints();
        gbc_xorButton.insets = new Insets(0, 0, 0, 5);
        gbc_xorButton.gridx = 4;
        gbc_xorButton.gridy = 1;
        booleanOpPanel.add(xorButton, gbc_xorButton);

        subButton = new IcyButton(RoiActions.boolSubtractAction);
        subButton.setText(null);
        GridBagConstraints gbc_subButton = new GridBagConstraints();
        gbc_subButton.insets = new Insets(0, 0, 0, 5);
        gbc_subButton.gridx = 5;
        gbc_subButton.gridy = 1;
        booleanOpPanel.add(subButton, gbc_subButton);

        deleteButton = new IcyButton(RoiActions.deleteAction);
        deleteButton.setText(null);
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.anchor = GridBagConstraints.WEST;
        gbc_deleteButton.gridx = 6;
        gbc_deleteButton.gridy = 1;
        booleanOpPanel.add(deleteButton, gbc_deleteButton);

        generalPanel = new JPanel();
        generalPanel.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(generalPanel);
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[] {0, 32, 0, 0, 120, 0, 0};
        gbl_panel_1.rowHeights = new int[] {0, 0, 0};
        gbl_panel_1.columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel_1.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        generalPanel.setLayout(gbl_panel_1);

        final JLabel lblNewLabel = new JLabel("Name");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        generalPanel.add(lblNewLabel, gbc_lblNewLabel);

        nameField = new IcyTextField();
        GridBagConstraints gbc_nameField = new GridBagConstraints();
        gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameField.gridwidth = 5;
        gbc_nameField.insets = new Insets(0, 0, 5, 0);
        gbc_nameField.gridx = 1;
        gbc_nameField.gridy = 0;
        generalPanel.add(nameField, gbc_nameField);
        nameField.setColumns(10);

        final JLabel lblColor = new JLabel("Color");
        GridBagConstraints gbc_lblColor = new GridBagConstraints();
        gbc_lblColor.anchor = GridBagConstraints.EAST;
        gbc_lblColor.insets = new Insets(0, 0, 0, 5);
        gbc_lblColor.gridx = 0;
        gbc_lblColor.gridy = 1;
        generalPanel.add(lblColor, gbc_lblColor);

        colorButton = new ColorChooserButton();
        colorButton.setToolTipText("Select the ROI color");
        GridBagConstraints gbc_colorButton = new GridBagConstraints();
        gbc_colorButton.insets = new Insets(0, 0, 0, 5);
        gbc_colorButton.gridx = 1;
        gbc_colorButton.gridy = 1;
        generalPanel.add(colorButton, gbc_colorButton);

        lblContentOpacity = new JLabel("Opacity");
        GridBagConstraints gbc_lblContentOpacity = new GridBagConstraints();
        gbc_lblContentOpacity.anchor = GridBagConstraints.EAST;
        gbc_lblContentOpacity.insets = new Insets(0, 0, 0, 5);
        gbc_lblContentOpacity.gridx = 3;
        gbc_lblContentOpacity.gridy = 1;
        generalPanel.add(lblContentOpacity, gbc_lblContentOpacity);

        alphaSlider = new JSlider();
        alphaSlider.setPreferredSize(new Dimension(80, 20));
        alphaSlider.setMaximumSize(new Dimension(32767, 20));
        alphaSlider.setMinimumSize(new Dimension(36, 20));
        alphaSlider.setFocusable(false);
        alphaSlider.setToolTipText("Change the ROI content opacity display");
        GridBagConstraints gbc_slider = new GridBagConstraints();
        gbc_slider.fill = GridBagConstraints.HORIZONTAL;
        gbc_slider.insets = new Insets(0, 0, 0, 5);
        gbc_slider.gridx = 4;
        gbc_slider.gridy = 1;
        generalPanel.add(alphaSlider, gbc_slider);

        final JPanel positionPanel = new JPanel();
        add(positionPanel);
        positionPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Position",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        GridBagLayout gbl_positionPanel = new GridBagLayout();
        gbl_positionPanel.columnWidths = new int[] {20, 0, 20, 0, 20, 0, 20, 0, 20, 0, 20, 0, 0};
        gbl_positionPanel.rowHeights = new int[] {0, 0, 0};
        gbl_positionPanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0,
                Double.MIN_VALUE};
        gbl_positionPanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        positionPanel.setLayout(gbl_positionPanel);

        final JLabel lblX = new JLabel("X");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        positionPanel.add(lblX, gbc_lblX);

        posXField = new IcyTextField();
        posXField.setToolTipText("X position of the ROI");
        GridBagConstraints gbc_posXField = new GridBagConstraints();
        gbc_posXField.gridwidth = 5;
        gbc_posXField.insets = new Insets(0, 0, 5, 5);
        gbc_posXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posXField.gridx = 1;
        gbc_posXField.gridy = 0;
        positionPanel.add(posXField, gbc_posXField);
        posXField.setColumns(10);

        posZFieldLabel = new JLabel("Z");

        final JLabel lblY = new JLabel("Y");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 6;
        gbc_lblY.gridy = 0;
        positionPanel.add(lblY, gbc_lblY);

        posYField = new IcyTextField();
        posYField.setToolTipText("Y position of the ROI");
        GridBagConstraints gbc_posYField = new GridBagConstraints();
        gbc_posYField.gridwidth = 5;
        gbc_posYField.insets = new Insets(0, 0, 5, 0);
        gbc_posYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posYField.gridx = 7;
        gbc_posYField.gridy = 0;
        positionPanel.add(posYField, gbc_posYField);
        posYField.setColumns(10);
        GridBagConstraints gbc_posZFieldLabel = new GridBagConstraints();
        gbc_posZFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_posZFieldLabel.gridx = 0;
        gbc_posZFieldLabel.gridy = 1;
        positionPanel.add(posZFieldLabel, gbc_posZFieldLabel);

        posCFieldLabel = new JLabel("C");

        posTFieldLabel = new JLabel("T");

        JPanel panelPosZ = new JPanel();
        panelPosZ.setBorder(null);
        GridBagConstraints gbc_panelPosZ = new GridBagConstraints();
        gbc_panelPosZ.fill = GridBagConstraints.HORIZONTAL;
        gbc_panelPosZ.gridwidth = 3;
        gbc_panelPosZ.insets = new Insets(0, 0, 0, 5);
        gbc_panelPosZ.gridx = 1;
        gbc_panelPosZ.gridy = 1;
        positionPanel.add(panelPosZ, gbc_panelPosZ);
        panelPosZ.setLayout(new BoxLayout(panelPosZ, BoxLayout.LINE_AXIS));

        posZField = new IcyTextField();
        panelPosZ.add(posZField);
        posZField.setVisible(false);
        posZField.setToolTipText("Z position of the ROI");
        posZField.setColumns(10);

        posZSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        panelPosZ.add(posZSpinner);
        posZSpinner.setToolTipText("Attach the ROI to a specific Z slice (set to -1 for ALL)");
        GridBagConstraints gbc_posTFieldLabel = new GridBagConstraints();
        gbc_posTFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_posTFieldLabel.gridx = 4;
        gbc_posTFieldLabel.gridy = 1;
        positionPanel.add(posTFieldLabel, gbc_posTFieldLabel);

        JPanel panelPosT = new JPanel();
        panelPosT.setBorder(null);
        GridBagConstraints gbc_panelPosT = new GridBagConstraints();
        gbc_panelPosT.gridwidth = 3;
        gbc_panelPosT.fill = GridBagConstraints.HORIZONTAL;
        gbc_panelPosT.insets = new Insets(0, 0, 0, 5);
        gbc_panelPosT.gridx = 5;
        gbc_panelPosT.gridy = 1;
        positionPanel.add(panelPosT, gbc_panelPosT);
        panelPosT.setLayout(new BoxLayout(panelPosT, BoxLayout.LINE_AXIS));

        posTField = new IcyTextField();
        panelPosT.add(posTField);
        posTField.setVisible(false);
        posTField.setToolTipText("T position of the ROI");
        posTField.setColumns(10);

        posTSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        panelPosT.add(posTSpinner);
        posTSpinner.setToolTipText("Attach the ROI to a specific T frame (set to -1 for ALL)");
        GridBagConstraints gbc_posCFieldLabel = new GridBagConstraints();
        gbc_posCFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_posCFieldLabel.gridx = 8;
        gbc_posCFieldLabel.gridy = 1;
        positionPanel.add(posCFieldLabel, gbc_posCFieldLabel);

        JPanel panelPosC = new JPanel();
        panelPosC.setBorder(null);
        GridBagConstraints gbc_panelPosC = new GridBagConstraints();
        gbc_panelPosC.gridwidth = 3;
        gbc_panelPosC.fill = GridBagConstraints.HORIZONTAL;
        gbc_panelPosC.gridx = 9;
        gbc_panelPosC.gridy = 1;
        positionPanel.add(panelPosC, gbc_panelPosC);
        panelPosC.setLayout(new BoxLayout(panelPosC, BoxLayout.LINE_AXIS));

        posCField = new IcyTextField();
        panelPosC.add(posCField);
        posCField.setVisible(false);
        posCField.setToolTipText("C position of the ROI");
        posCField.setColumns(10);

        posCSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        panelPosC.add(posCSpinner);
        posCSpinner.setToolTipText("Attach the ROI to a specific C channel (set to -1 for ALL)");

        JPanel sizePanel = new JPanel();
        add(sizePanel);
        sizePanel.setBorder(new TitledBorder(null, "Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagLayout gbl_sizePanel = new GridBagLayout();
        gbl_sizePanel.columnWidths = new int[] {20, 0, 20, 0, 20, 0, 20, 0, 20, 0, 20, 0, 0};
        gbl_sizePanel.rowHeights = new int[] {0, 0, 0};
        gbl_sizePanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0,
                Double.MIN_VALUE};
        gbl_sizePanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        sizePanel.setLayout(gbl_sizePanel);

        final JLabel lblNewLabel_2 = new JLabel("X");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 0;
        sizePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        sizeXField = new IcyTextField();
        GridBagConstraints gbc_sizeXField = new GridBagConstraints();
        gbc_sizeXField.gridwidth = 5;
        gbc_sizeXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeXField.insets = new Insets(0, 0, 5, 5);
        gbc_sizeXField.gridx = 1;
        gbc_sizeXField.gridy = 0;
        sizePanel.add(sizeXField, gbc_sizeXField);
        sizeXField.setToolTipText("Size of dimension X for the ROI");
        sizeXField.setColumns(10);

        final JLabel lblY_1 = new JLabel("Y");
        GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.gridx = 6;
        gbc_lblY_1.gridy = 0;
        sizePanel.add(lblY_1, gbc_lblY_1);

        sizeYField = new IcyTextField();
        GridBagConstraints gbc_sizeYField = new GridBagConstraints();
        gbc_sizeYField.gridwidth = 5;
        gbc_sizeYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeYField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeYField.gridx = 7;
        gbc_sizeYField.gridy = 0;
        sizePanel.add(sizeYField, gbc_sizeYField);
        sizeYField.setToolTipText("Size of dimension Y for the ROI");
        sizeYField.setColumns(10);

        sizeZFieldLabel = new JLabel("Z");
        GridBagConstraints gbc_sizeZFieldLabel = new GridBagConstraints();
        gbc_sizeZFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_sizeZFieldLabel.gridx = 0;
        gbc_sizeZFieldLabel.gridy = 1;
        sizePanel.add(sizeZFieldLabel, gbc_sizeZFieldLabel);

        sizeZField = new IcyTextField();
        GridBagConstraints gbc_sizeZField = new GridBagConstraints();
        gbc_sizeZField.gridwidth = 3;
        gbc_sizeZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeZField.insets = new Insets(0, 0, 0, 5);
        gbc_sizeZField.gridx = 1;
        gbc_sizeZField.gridy = 1;
        sizePanel.add(sizeZField, gbc_sizeZField);
        sizeZField.setToolTipText("Size of dimension Z for the ROI");
        sizeZField.setColumns(10);

        sizeTFieldLabel = new JLabel("T");
        GridBagConstraints gbc_sizeTFieldLabel = new GridBagConstraints();
        gbc_sizeTFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_sizeTFieldLabel.gridx = 4;
        gbc_sizeTFieldLabel.gridy = 1;
        sizePanel.add(sizeTFieldLabel, gbc_sizeTFieldLabel);

        sizeTField = new IcyTextField();
        GridBagConstraints gbc_sizeTField = new GridBagConstraints();
        gbc_sizeTField.gridwidth = 3;
        gbc_sizeTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeTField.insets = new Insets(0, 0, 0, 5);
        gbc_sizeTField.gridx = 5;
        gbc_sizeTField.gridy = 1;
        sizePanel.add(sizeTField, gbc_sizeTField);
        sizeTField.setToolTipText("Size of dimension T for the ROI");
        sizeTField.setColumns(10);

        sizeCFieldLabel = new JLabel("C");
        GridBagConstraints gbc_sizeCFieldLabel = new GridBagConstraints();
        gbc_sizeCFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_sizeCFieldLabel.gridx = 8;
        gbc_sizeCFieldLabel.gridy = 1;
        sizePanel.add(sizeCFieldLabel, gbc_sizeCFieldLabel);

        sizeCField = new IcyTextField();
        GridBagConstraints gbc_sizeCField = new GridBagConstraints();
        gbc_sizeCField.gridwidth = 3;
        gbc_sizeCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeCField.gridx = 9;
        gbc_sizeCField.gridy = 1;
        sizePanel.add(sizeCField, gbc_sizeCField);
        sizeCField.setToolTipText("Size of dimension C for the ROI");
        sizeCField.setColumns(10);

        infosPanel = new RoiExtraInfoPanel();
        infosPanel.setBorder(new TitledBorder(null, "Extra informations", TitledBorder.LEADING, TitledBorder.TOP, null,
                null));
        add(infosPanel);
    }

    /**
     * Get the selected ROI in the ROI control panel.<br>
     * This actually returns selected ROI from the ROI table in ROI panel (cached).
     */
    public List<ROI> getSelectedRois()
    {
        return new ArrayList<ROI>(selectedRois);
    }

    /**
     * Set the selected ROI in the ROI control panel.<br>
     * This actually refresh the GUI to reflect informations about the new selected ROI(s).
     */
    public void setSelectedRois(List<ROI> rois)
    {
        final int newSelectedSize = rois.size();
        final int oldSelectedSize = selectedRois.size();

        // easy optimization
        if ((newSelectedSize == 0) && (oldSelectedSize == 0))
            return;

        // same selection size ?
        if (newSelectedSize == oldSelectedSize)
        {
            // same selection, don't need to update it
            if (new HashSet<ROI>(rois).containsAll(selectedRois))
                return;
        }

        // remove listener
        for (ROI roi : selectedRois)
            roi.removeListener(this);

        selectedRois = rois;

        // add listener
        for (ROI roi : rois)
            roi.addListener(this);

        // refresh all
        refreshROIActionsAndProperties();
    }

    /**
     * Refresh the ROI actions state.
     */
    public void refreshROIActions()
    {
        ThreadUtil.bgRun(roiActionsRefresher, true);
    }

    void refreshPosToolTip(int dim)
    {
        if (dim > 1)
            posYField.setToolTipText("Y position of the ROI");
        else
            posYField.setToolTipText("Attach the ROI to a specific Y line (set to -1 for all lines)");
        if (dim > 2)
            posZField.setToolTipText("Z position of the ROI");
        else
            posZField.setToolTipText("Attach the ROI to a specific Z slice (set to -1 for all slices)");
        if (dim > 3)
            posTField.setToolTipText("T position of the ROI");
        else
            posTField.setToolTipText("Attach the ROI to a specific T frame (set to -1 for all frames)");
        if (dim > 4)
            posCField.setToolTipText("C position of the ROI");
        else
            posCField.setToolTipText("Attach the ROI to a specific C channel (set to -1 for all channels)");
    }

    /**
     * Refresh the ROI actions state (internal)
     */
    void refreshROIActionsInternal()
    {
        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        final List<ROI> rois = getSelectedRois();
        final ROI roi = (rois.size() > 0) ? rois.get(0) : null;

        boolean readOnly = true;
        // set read only flag
        for (ROI r : rois)
            readOnly &= r.isReadOnly();

        final boolean hasSequence = (sequence != null);
        final boolean hasSelected = (roi != null);
        final boolean twoSelected = (rois.size() == 2);
        final boolean multiSelect = (rois.size() > 1);
        final boolean singleSelect = hasSelected && !multiSelect;
        final boolean canSetBounds = hasSelected ? roi.canSetBounds() : false;
        final boolean canSetPosition = hasSelected ? roi.canSetPosition() : false;
        final boolean hasROIinClipboard = Clipboard.isType(Clipboard.TYPE_ROILIST);
        final boolean hasROILinkinClipboard = Clipboard.isType(Clipboard.TYPE_ROILINKLIST);
        final boolean editable = !readOnly;
        final int dim = hasSelected ? roi.getDimension() : 0;

        nameField.setEnabled(singleSelect && editable);
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

        if (hasSequence)
        {
            ((SpecialValueSpinnerModel) posZSpinner.getModel()).setMaximum(Integer.valueOf(sequence.getSizeZ() - 1));
            ((SpecialValueSpinnerModel) posTSpinner.getModel()).setMaximum(Integer.valueOf(sequence.getSizeT() - 1));
            ((SpecialValueSpinnerModel) posCSpinner.getModel()).setMaximum(Integer.valueOf(sequence.getSizeC() - 1));
        }
        else
        {
            ((SpecialValueSpinnerModel) posZSpinner.getModel()).setMaximum(Integer.valueOf(0));
            ((SpecialValueSpinnerModel) posTSpinner.getModel()).setMaximum(Integer.valueOf(0));
            ((SpecialValueSpinnerModel) posCSpinner.getModel()).setMaximum(Integer.valueOf(0));
        }

        if (dim > 2)
        {
            posZField.setVisible(true);
            posZSpinner.setVisible(false);
        }
        else
        {
            posZField.setVisible(false);
            posZSpinner.setVisible(true);
        }

        if (dim > 3)
        {
            posTField.setVisible(true);
            posTSpinner.setVisible(false);
        }
        else
        {
            posTField.setVisible(false);
            posTSpinner.setVisible(true);
        }

        if (dim > 4)
        {
            posCField.setVisible(true);
            posCSpinner.setVisible(false);
        }
        else
        {
            posCField.setVisible(false);
            posCSpinner.setVisible(true);
        }

        colorButton.setEnabled(hasSelected && editable);
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

        refreshPosToolTip(dim);
    }

    /**
     * Refresh ROI properties
     */
    public void refreshROIProperties()
    {
        ThreadUtil.bgRunSingle(roiPropertiesRefresher, true);
    }

    /**
     * Refresh ROI properties (internal)
     */
    void refreshROIPropertiesInternal()
    {
        final List<ROI> rois = getSelectedRois();
        final ROI roi = (rois.size() > 0) ? rois.get(0) : null;

        modifyingRoi.acquireUninterruptibly();
        try
        {
            if (roi != null)
            {
                final String name = roi.getName();

                nameField.setText(name);
                colorButton.setColor(roi.getColor());
                alphaSlider.setValue((int) (roi.getOpacity() * 100));

                final Rectangle5D bounds = roi.getBounds5D();

                final double x = bounds.getX();
                final double y = bounds.getY();
                final double z = bounds.getZ();
                final double t = bounds.getT();
                final double c = bounds.getC();
                final double sx = bounds.getSizeX();
                final double sy = bounds.getSizeY();
                final double sz = bounds.getSizeZ();
                final double st = bounds.getSizeT();
                final double sc = bounds.getSizeC();

                // special case of infinite X dimension
                if (sx == Double.POSITIVE_INFINITY)
                {
                    posXField.setText("-1");
                    sizeXField.setText("infinite");
                }
                else
                {
                    posXField.setText(StringUtil.toString(MathUtil.roundSignificant(x, 5, true)));
                    sizeXField.setText(StringUtil.toString(MathUtil.roundSignificant(sx, 5, true)));
                }
                // special case of infinite Y dimension
                if (sy == Double.POSITIVE_INFINITY)
                {
                    posYField.setText("-1");
                    sizeYField.setText("infinite");
                }
                else
                {
                    posYField.setText(StringUtil.toString(MathUtil.roundSignificant(y, 5, true)));
                    sizeYField.setText(StringUtil.toString(MathUtil.roundSignificant(sy, 5, true)));
                }
                // special case of infinite Z dimension
                if (sz == Double.POSITIVE_INFINITY)
                {
                    posZField.setText("-1");
                    posZSpinner.setValue(Integer.valueOf(-1));
                    sizeZField.setText("infinite");
                }
                else
                {
                    posZField.setText(StringUtil.toString(MathUtil.roundSignificant(z, 5, true)));
                    posZSpinner.setValue(Integer.valueOf((int) z));
                    sizeZField.setText(StringUtil.toString(MathUtil.roundSignificant(sz, 5, true)));
                }
                // special case of infinite T dimension
                if (st == Double.POSITIVE_INFINITY)
                {
                    posTField.setText("-1");
                    posTSpinner.setValue(Integer.valueOf(-1));
                    sizeTField.setText("infinite");
                }
                else
                {
                    posTField.setText(StringUtil.toString(MathUtil.roundSignificant(t, 5, true)));
                    posTSpinner.setValue(Integer.valueOf((int) t));
                    sizeTField.setText(StringUtil.toString(MathUtil.roundSignificant(st, 5, true)));
                }
                // special case of infinite C dimension
                if (sc == Double.POSITIVE_INFINITY)
                {
                    posCField.setText("-1");
                    posCSpinner.setValue(Integer.valueOf(-1));
                    sizeCField.setText("infinite");
                }
                else
                {
                    posCField.setText(StringUtil.toString(MathUtil.roundSignificant(c, 5, true)));
                    posCSpinner.setValue(Integer.valueOf((int) c));
                    sizeCField.setText(StringUtil.toString(MathUtil.roundSignificant(sc, 5, true)));
                }
            }
            else
            {
                // no ROI selected
                nameField.setText("");

                colorButton.setColor(Color.gray);
                alphaSlider.setValue(0);

                posXField.setText("");
                posYField.setText("");
                posZField.setText("");
                posTField.setText("");

                sizeXField.setText("");
                sizeYField.setText("");
                sizeZField.setText("");
                sizeTField.setText("");
            }

            // refresh ROI infos
            infosPanel.refresh(Icy.getMainInterface().getActiveViewer(), rois);
        }
        finally
        {
            modifyingRoi.release();
        }
    }

    /**
     * Refresh ROI actions and properties
     */
    public void refreshROIActionsAndProperties()
    {
        ThreadUtil.bgRunSingle(roiActionsRefresher, true);
        ThreadUtil.bgRunSingle(roiPropertiesRefresher, true);
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
            // simple name change
            if (source == nameField)
                roi.setName(source.getText());
            // position fields ?
            else if ((source == posXField) || (source == posYField) || (source == posZField) || (source == posTField)
                    || (source == posCField))
            {
                // get current ROI position
                Point5D position = roi.getPosition5D();

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
                Rectangle5D bounds = roi.getBounds5D();

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

        sequence.beginUpdate();
        try
        {
            // color changed
            final Color color = source.getColor();

            for (ROI roi : getSelectedRois())
                if (!roi.isReadOnly())
                    roi.setColor(color);
        }
        finally
        {
            sequence.endUpdate();
            modifyingRoi.release();
        }
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
            if (source == alphaSlider)
            {
                sequence.beginUpdate();
                try
                {
                    final float opacity = alphaSlider.getValue() / 100f;

                    for (ROI roi : getSelectedRois())
                        if (!roi.isReadOnly())
                            roi.setOpacity(opacity);
                }
                finally
                {
                    sequence.endUpdate();
                }
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
                Point5D position = roi.getPosition5D();

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
    public void clipboardChanged()
    {
        refreshROIActions();
    }

    // one of the selected ROI changed
    @Override
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
                else if (ROI.PROPERTY_NAME.equals(propertyName))
                    ThreadUtil.bgRunSingle(roiPropertiesRefresher, true);
                break;

            case SELECTION_CHANGED:
                // handle externally with the setSelectedROI() method
                break;
        }
    }
}

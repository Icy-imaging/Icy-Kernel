/**
 * 
 */
package icy.gui.inspector;

import icy.clipboard.Clipboard;
import icy.clipboard.Clipboard.ClipboardListener;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.PopupPanel;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.menu.action.RoiActions;
import icy.image.IntensityInfo;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI2DLine;
import icy.roi.ROI2DRectShape;
import icy.roi.ROI3D;
import icy.roi.ROI4D;
import icy.roi.ROI5D;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * @author Stephane
 */
public class RoiControlPanel extends JPanel implements ColorChangeListener, TextChangeListener, ClipboardListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7403770406075917063L;

    // GUI
    private IcyTextField nameField;
    private IcyTextField posXField;
    private IcyTextField posYField;
    private IcyTextField posTField;
    private IcyTextField posZField;
    private IcyTextField sizeXField;
    private IcyTextField sizeZField;
    private IcyTextField sizeYField;
    private IcyTextField sizeTField;
    ColorChooserButton colorButton;
    private ColorChooserButton selectedColorButton;
    private IcyButton orButton;
    private IcyButton andButton;
    private IcyButton xorButton;
    private IcyButton subButton;
    private IcyButton deleteButton;

    // internal
    final RoisPanel roisPanel;
    IntensityInfo intensityInfos[];
    boolean isRoiPropertiesAdjusting;
    private JPanel generalPanel;
    private RoiExtraInfoPanel infosPanel;
    private PopupPanel popupPanel;
    private JLabel lblBoolean;
    private JLabel lblGeneral;
    private IcyButton copyButton;
    private IcyButton pasteButton;
    private IcyButton saveButton;
    private IcyButton loadButton;

    public RoiControlPanel(RoisPanel panel)
    {
        super();

        roisPanel = panel;
        isRoiPropertiesAdjusting = false;
        intensityInfos = null;

        initialize();

        nameField.addTextChangeListener(this);

        colorButton.addColorChangeListener(this);
        selectedColorButton.addColorChangeListener(this);

        posXField.addTextChangeListener(this);
        posYField.addTextChangeListener(this);
        posZField.addTextChangeListener(this);
        posTField.addTextChangeListener(this);
        sizeXField.addTextChangeListener(this);
        sizeYField.addTextChangeListener(this);
        sizeZField.addTextChangeListener(this);
        sizeTField.addTextChangeListener(this);

        Clipboard.addListener(this);
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
        gbl_booleanOpPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_booleanOpPanel.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_booleanOpPanel.rowWeights = new double[] {1.0, 1.0, 0.0, Double.MIN_VALUE};
        booleanOpPanel.setLayout(gbl_booleanOpPanel);

        lblGeneral = new JLabel("General");
        GridBagConstraints gbc_lblGeneral = new GridBagConstraints();
        gbc_lblGeneral.anchor = GridBagConstraints.WEST;
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

        deleteButton = new IcyButton(RoiActions.deleteAction);
        deleteButton.setText(null);
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.insets = new Insets(0, 0, 5, 0);
        gbc_deleteButton.fill = GridBagConstraints.BOTH;
        gbc_deleteButton.gridx = 6;
        gbc_deleteButton.gridy = 0;
        booleanOpPanel.add(deleteButton, gbc_deleteButton);

        lblBoolean = new JLabel("Boolean");
        GridBagConstraints gbc_lblBoolean = new GridBagConstraints();
        gbc_lblBoolean.anchor = GridBagConstraints.WEST;
        gbc_lblBoolean.insets = new Insets(0, 0, 5, 5);
        gbc_lblBoolean.gridx = 0;
        gbc_lblBoolean.gridy = 1;
        booleanOpPanel.add(lblBoolean, gbc_lblBoolean);

        orButton = new IcyButton(RoiActions.boolOrAction);
        orButton.setText(null);
        GridBagConstraints gbc_orButton = new GridBagConstraints();
        gbc_orButton.fill = GridBagConstraints.BOTH;
        gbc_orButton.insets = new Insets(0, 0, 5, 5);
        gbc_orButton.gridx = 1;
        gbc_orButton.gridy = 1;
        booleanOpPanel.add(orButton, gbc_orButton);

        andButton = new IcyButton(RoiActions.boolAndAction);
        andButton.setText(null);
        GridBagConstraints gbc_andButton = new GridBagConstraints();
        gbc_andButton.fill = GridBagConstraints.BOTH;
        gbc_andButton.insets = new Insets(0, 0, 5, 5);
        gbc_andButton.gridx = 2;
        gbc_andButton.gridy = 1;
        booleanOpPanel.add(andButton, gbc_andButton);

        xorButton = new IcyButton(RoiActions.boolXorAction);
        xorButton.setText(null);
        GridBagConstraints gbc_xorButton = new GridBagConstraints();
        gbc_xorButton.fill = GridBagConstraints.BOTH;
        gbc_xorButton.insets = new Insets(0, 0, 5, 5);
        gbc_xorButton.gridx = 3;
        gbc_xorButton.gridy = 1;
        booleanOpPanel.add(xorButton, gbc_xorButton);

        subButton = new IcyButton(RoiActions.boolSubtractAction);
        subButton.setText(null);
        GridBagConstraints gbc_subButton = new GridBagConstraints();
        gbc_subButton.insets = new Insets(0, 0, 5, 5);
        gbc_subButton.fill = GridBagConstraints.BOTH;
        gbc_subButton.gridx = 4;
        gbc_subButton.gridy = 1;
        booleanOpPanel.add(subButton, gbc_subButton);

        generalPanel = new JPanel();
        generalPanel.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(generalPanel);
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
        gbl_panel_1.rowHeights = new int[] {0, 0, 0};
        gbl_panel_1.columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel_1.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        generalPanel.setLayout(gbl_panel_1);

        final JLabel lblNewLabel = new JLabel("Name");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
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
        gbc_lblColor.anchor = GridBagConstraints.WEST;
        gbc_lblColor.insets = new Insets(0, 0, 0, 5);
        gbc_lblColor.gridx = 0;
        gbc_lblColor.gridy = 1;
        generalPanel.add(lblColor, gbc_lblColor);

        colorButton = new ColorChooserButton();
        GridBagConstraints gbc_colorButton = new GridBagConstraints();
        gbc_colorButton.fill = GridBagConstraints.VERTICAL;
        gbc_colorButton.anchor = GridBagConstraints.WEST;
        gbc_colorButton.insets = new Insets(0, 0, 0, 5);
        gbc_colorButton.gridx = 1;
        gbc_colorButton.gridy = 1;
        generalPanel.add(colorButton, gbc_colorButton);

        final JLabel lblNewLabel_1 = new JLabel("Selected color");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
        gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_1.gridx = 3;
        gbc_lblNewLabel_1.gridy = 1;
        generalPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        selectedColorButton = new ColorChooserButton();
        GridBagConstraints gbc_selectedColorButton = new GridBagConstraints();
        gbc_selectedColorButton.fill = GridBagConstraints.VERTICAL;
        gbc_selectedColorButton.insets = new Insets(0, 0, 0, 5);
        gbc_selectedColorButton.anchor = GridBagConstraints.WEST;
        gbc_selectedColorButton.gridx = 4;
        gbc_selectedColorButton.gridy = 1;
        generalPanel.add(selectedColorButton, gbc_selectedColorButton);

        final JPanel positionPanel = new JPanel();
        positionPanel.setBorder(new TitledBorder(null, "Position", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(positionPanel);
        GridBagLayout gbl_positionPanel = new GridBagLayout();
        gbl_positionPanel.columnWidths = new int[] {20, 0, 20, 0, 0};
        gbl_positionPanel.rowHeights = new int[] {0, 0, 0};
        gbl_positionPanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_positionPanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        positionPanel.setLayout(gbl_positionPanel);

        final JLabel lblX = new JLabel("X");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.anchor = GridBagConstraints.EAST;
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        positionPanel.add(lblX, gbc_lblX);

        posXField = new IcyTextField();
        posXField.setToolTipText("");
        GridBagConstraints gbc_posXField = new GridBagConstraints();
        gbc_posXField.insets = new Insets(0, 0, 5, 5);
        gbc_posXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posXField.gridx = 1;
        gbc_posXField.gridy = 0;
        positionPanel.add(posXField, gbc_posXField);
        posXField.setColumns(10);

        final JLabel lblY = new JLabel("Y");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.anchor = GridBagConstraints.EAST;
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 2;
        gbc_lblY.gridy = 0;
        positionPanel.add(lblY, gbc_lblY);

        posYField = new IcyTextField();
        posYField.setToolTipText("");
        GridBagConstraints gbc_posYField = new GridBagConstraints();
        gbc_posYField.insets = new Insets(0, 0, 5, 0);
        gbc_posYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posYField.gridx = 3;
        gbc_posYField.gridy = 0;
        positionPanel.add(posYField, gbc_posYField);
        posYField.setColumns(10);

        final JLabel lblZ = new JLabel("Z");
        GridBagConstraints gbc_lblZ = new GridBagConstraints();
        gbc_lblZ.anchor = GridBagConstraints.EAST;
        gbc_lblZ.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ.gridx = 0;
        gbc_lblZ.gridy = 1;
        positionPanel.add(lblZ, gbc_lblZ);

        posZField = new IcyTextField();
        posZField.setToolTipText("Attach the ROI to a specific Z slice (-1 = all)");
        GridBagConstraints gbc_posZField = new GridBagConstraints();
        gbc_posZField.insets = new Insets(0, 0, 0, 5);
        gbc_posZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posZField.gridx = 1;
        gbc_posZField.gridy = 1;
        positionPanel.add(posZField, gbc_posZField);
        posZField.setColumns(10);

        final JLabel lblT = new JLabel("T");
        GridBagConstraints gbc_lblT = new GridBagConstraints();
        gbc_lblT.anchor = GridBagConstraints.EAST;
        gbc_lblT.insets = new Insets(0, 0, 0, 5);
        gbc_lblT.gridx = 2;
        gbc_lblT.gridy = 1;
        positionPanel.add(lblT, gbc_lblT);

        posTField = new IcyTextField();
        posTField.setToolTipText("Attach the ROI to a specific T frame (-1 = all)");
        GridBagConstraints gbc_posTField = new GridBagConstraints();
        gbc_posTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posTField.gridx = 3;
        gbc_posTField.gridy = 1;
        positionPanel.add(posTField, gbc_posTField);
        posTField.setColumns(10);

        final JPanel sizePanel = new JPanel();
        sizePanel.setBorder(new TitledBorder(null, "Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(sizePanel);
        GridBagLayout gbl_sizePanel = new GridBagLayout();
        gbl_sizePanel.columnWidths = new int[] {20, 0, 20, 0, 0};
        gbl_sizePanel.rowHeights = new int[] {0, 0, 0};
        gbl_sizePanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_sizePanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        sizePanel.setLayout(gbl_sizePanel);

        final JLabel lblNewLabel_2 = new JLabel("X");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 0;
        sizePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        sizeXField = new IcyTextField();
        GridBagConstraints gbc_sizeXField = new GridBagConstraints();
        gbc_sizeXField.insets = new Insets(0, 0, 5, 5);
        gbc_sizeXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeXField.gridx = 1;
        gbc_sizeXField.gridy = 0;
        sizePanel.add(sizeXField, gbc_sizeXField);
        sizeXField.setColumns(10);

        final JLabel lblY_1 = new JLabel("Y");
        GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.anchor = GridBagConstraints.EAST;
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.gridx = 2;
        gbc_lblY_1.gridy = 0;
        sizePanel.add(lblY_1, gbc_lblY_1);

        sizeYField = new IcyTextField();
        GridBagConstraints gbc_sizeYField = new GridBagConstraints();
        gbc_sizeYField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeYField.gridx = 3;
        gbc_sizeYField.gridy = 0;
        sizePanel.add(sizeYField, gbc_sizeYField);
        sizeYField.setColumns(10);

        final JLabel lblZ_1 = new JLabel("Z");
        lblZ_1.setVisible(false);
        GridBagConstraints gbc_lblZ_1 = new GridBagConstraints();
        gbc_lblZ_1.anchor = GridBagConstraints.EAST;
        gbc_lblZ_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ_1.gridx = 0;
        gbc_lblZ_1.gridy = 1;
        sizePanel.add(lblZ_1, gbc_lblZ_1);

        sizeZField = new IcyTextField();
        sizeZField.setVisible(false);
        GridBagConstraints gbc_sizeZField = new GridBagConstraints();
        gbc_sizeZField.insets = new Insets(0, 0, 0, 5);
        gbc_sizeZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeZField.gridx = 1;
        gbc_sizeZField.gridy = 1;
        sizePanel.add(sizeZField, gbc_sizeZField);
        sizeZField.setColumns(10);

        final JLabel lblT_1 = new JLabel("T");
        lblT_1.setVisible(false);
        GridBagConstraints gbc_lblT_1 = new GridBagConstraints();
        gbc_lblT_1.anchor = GridBagConstraints.EAST;
        gbc_lblT_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblT_1.gridx = 2;
        gbc_lblT_1.gridy = 1;
        sizePanel.add(lblT_1, gbc_lblT_1);

        sizeTField = new IcyTextField();
        sizeTField.setVisible(false);
        GridBagConstraints gbc_sizeTField = new GridBagConstraints();
        gbc_sizeTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeTField.gridx = 3;
        gbc_sizeTField.gridy = 1;
        sizePanel.add(sizeTField, gbc_sizeTField);
        sizeTField.setColumns(10);

        infosPanel = new RoiExtraInfoPanel();

        popupPanel = new PopupPanel();
        popupPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        popupPanel.setExpanded(true);
        popupPanel.setToolTipText("Extras informations about the current selected ROI");
        popupPanel.setTitle("Extras informations");
        add(popupPanel);

        final JPanel panel = popupPanel.getMainPanel();
        panel.setLayout(new BorderLayout(0, 0));
        panel.add(infosPanel);
    }

    public void refresh()
    {
        isRoiPropertiesAdjusting = true;
        try
        {
            // default
            nameField.setText("");

            colorButton.setColor(Color.gray);
            selectedColorButton.setColor(Color.gray);

            posXField.setText("");
            posYField.setText("");
            posZField.setText("");
            posTField.setText("");

            sizeXField.setText("");
            sizeYField.setText("");
            sizeZField.setText("");
            sizeTField.setText("");

            sizeZField.setVisible(false);
            sizeTField.setVisible(false);

            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
            {
                final ArrayList<ROI> rois = roisPanel.getSelectedRois();

                boolean editable = false;
                for (ROI r : rois)
                    editable |= r.isEditable();

                final boolean hasSelected = (rois.size() > 0);
                final ROI roi;

                if (hasSelected)
                    roi = rois.get(0);
                else
                    roi = null;

                final boolean twoSelected = (rois.size() == 2);
                final boolean severalsSelected = (rois.size() > 1);
                final boolean singleSelect = hasSelected && !severalsSelected;
                final boolean isRoi2d = roi instanceof ROI2D;
                final boolean isRoi2dResizeable = (roi instanceof ROI2DLine) || (roi instanceof ROI2DRectShape);

                nameField.setEnabled(singleSelect && editable);
                posXField.setEnabled(singleSelect && isRoi2d && editable);
                posYField.setEnabled(singleSelect && isRoi2d && editable);
                posZField.setEnabled(singleSelect && isRoi2d && editable);
                posTField.setEnabled(singleSelect && isRoi2d && editable);
                sizeXField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeYField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeZField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeTField.setEnabled(singleSelect && isRoi2dResizeable && editable);

                colorButton.setEnabled(hasSelected && editable);
                selectedColorButton.setEnabled(hasSelected && editable);

                loadButton.setEnabled(true);
                saveButton.setEnabled(hasSelected);
                copyButton.setEnabled(hasSelected);
                pasteButton.setEnabled(Clipboard.hasObjects(RoiActions.ID_ROI_COPY_CLIPBOARD, false));

                deleteButton.setEnabled(hasSelected && editable);

                orButton.setEnabled(severalsSelected);
                andButton.setEnabled(severalsSelected);
                xorButton.setEnabled(severalsSelected);
                subButton.setEnabled(twoSelected);

                if (hasSelected)
                {
                    final String name = roi.getName();

                    // handle it manually as setText doesn't check for equality
                    nameField.setText(name);

                    colorButton.setColor(roi.getColor());
                    selectedColorButton.setColor(roi.getSelectedColor());

                    if (roi instanceof ROI2D)
                    {
                        final ROI2D roi2d = (ROI2D) roi;
                        final Rectangle2D bounds = roi2d.getBounds2D();

                        final String posX = StringUtil.toString(MathUtil.roundSignificant(bounds.getX(), 5, true));
                        final String posY = StringUtil.toString(MathUtil.roundSignificant(bounds.getY(), 5, true));
                        final String posZ = StringUtil.toString(roi2d.getZ());
                        final String posT = StringUtil.toString(roi2d.getT());

                        final String sizeX = StringUtil.toString(MathUtil.roundSignificant(bounds.getWidth(), 5, true));
                        final String sizeY = StringUtil
                                .toString(MathUtil.roundSignificant(bounds.getHeight(), 5, true));

                        posXField.setText(posX);
                        posYField.setText(posY);
                        posZField.setText(posZ);
                        posTField.setText(posT);

                        sizeXField.setText(sizeX);
                        sizeYField.setText(sizeY);
                    }
                    else
                    {
                        // not supported yet
                    }
                }

                // refresh ROI infos
                infosPanel.refresh(sequence, roi);
            }
            else
            {
                nameField.setEnabled(false);
                posXField.setEnabled(false);
                posYField.setEnabled(false);
                posZField.setEnabled(false);
                posTField.setEnabled(false);
                sizeXField.setEnabled(false);
                sizeYField.setEnabled(false);
                sizeZField.setEnabled(false);
                sizeTField.setEnabled(false);

                colorButton.setEnabled(false);
                selectedColorButton.setEnabled(false);

                loadButton.setEnabled(false);
                saveButton.setEnabled(false);
                copyButton.setEnabled(false);
                pasteButton.setEnabled(false);
                deleteButton.setEnabled(false);

                orButton.setEnabled(false);
                andButton.setEnabled(false);
                xorButton.setEnabled(false);
                subButton.setEnabled(false);

                // refresh ROI infos
                infosPanel.refresh(null, null);
            }
        }
        finally
        {
            isRoiPropertiesAdjusting = false;
        }
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (isRoiPropertiesAdjusting)
            return;
        if (!validate)
            return;

        final ArrayList<ROI> selectedROI = RoiControlPanel.this.roisPanel.getSelectedRois();

        if (selectedROI.size() == 1)
        {
            final ROI roi = selectedROI.get(0);

            if (roi.isEditable())
            {
                if ((source == nameField) && nameField.isEnabled())
                    roi.setName(source.getText());

                if (roi instanceof ROI2D)
                {
                    final ROI2D roi2d = (ROI2D) roi;
                    final Rectangle2D roiBounds = roi2d.getBounds2D();

                    if ((source == posXField) && posXField.isEnabled())
                    {
                        final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                        if (!Double.isNaN(value))
                            roi2d.setPosition(new Point2D.Double(value, roiBounds.getY()));
                        source.setText(Double.toString(roi2d.getPosition2D().getX()));
                    }
                    if ((source == posYField) && posYField.isEnabled())
                    {
                        final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                        if (!Double.isNaN(value))
                            roi2d.setPosition(new Point2D.Double(roiBounds.getX(), value));
                        source.setText(Double.toString(roi2d.getPosition2D().getY()));
                    }
                    if ((source == posZField) && posZField.isEnabled())
                    {
                        roi2d.setZ(Math.max(-1, StringUtil.parseInt(source.getText(), -1)));
                        source.setText(Integer.toString(roi2d.getZ()));
                    }
                    if ((source == posTField) && posTField.isEnabled())
                    {
                        roi2d.setT(Math.max(-1, StringUtil.parseInt(source.getText(), -1)));
                        source.setText(Integer.toString(roi2d.getT()));
                    }

                    if ((roi2d instanceof ROI2DLine) || (roi2d instanceof ROI2DRectShape))
                    {
                        if ((source == sizeXField) && sizeXField.isEnabled())
                        {
                            final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                            if (!Double.isNaN(value))
                            {
                                final Rectangle2D r = new Rectangle2D.Double(roiBounds.getX(), roiBounds.getY(), value,
                                        roiBounds.getHeight());

                                if (roi2d instanceof ROI2DLine)
                                    ((ROI2DLine) roi2d).setBounds2D(r);
                                else
                                    ((ROI2DRectShape) roi2d).setBounds2D(r);
                            }

                            if (roi2d instanceof ROI2DLine)
                                source.setText(Double.toString(((ROI2DLine) roi2d).getBounds2D().getWidth()));
                            else
                                source.setText(Double.toString(((ROI2DRectShape) roi2d).getBounds2D().getWidth()));
                        }
                        if ((source == sizeYField) && sizeYField.isEnabled())
                        {
                            final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                            if (!Double.isNaN(value))
                            {
                                final Rectangle2D r = new Rectangle2D.Double(roiBounds.getX(), roiBounds.getY(),
                                        roiBounds.getWidth(), value);

                                if (roi2d instanceof ROI2DLine)
                                    ((ROI2DLine) roi2d).setBounds2D(r);
                                else
                                    ((ROI2DRectShape) roi2d).setBounds2D(r);
                            }

                            if (roi2d instanceof ROI2DLine)
                                source.setText(Double.toString(((ROI2DLine) roi2d).getBounds2D().getHeight()));
                            else
                                source.setText(Double.toString(((ROI2DRectShape) roi2d).getBounds2D().getHeight()));
                        }
                    }
                }

                if (roi instanceof ROI3D)
                {
                    // not yet supported
                }

                if (roi instanceof ROI4D)
                {
                    // not yet supported
                }

                if (roi instanceof ROI5D)
                {
                    // not yet supported
                }
            }
        }
    }

    @Override
    public void colorChanged(ColorChooserButton source)
    {
        if (isRoiPropertiesAdjusting)
            return;

        if (source.isEnabled())
        {
            final Color color = source.getColor();
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final ArrayList<ROI> selectedROI = roisPanel.getSelectedRois();

            sequence.beginUpdate();
            try
            {
                for (ROI roi : selectedROI)
                {
                    if (roi.isEditable())
                        if (source == colorButton)
                            roi.setColor(color);
                        else
                            roi.setSelectedColor(color);
                }
            }
            finally
            {
                sequence.endUpdate();
            }
        }
    }

    @Override
    public void clipboardChanged()
    {
        refresh();
    }
}

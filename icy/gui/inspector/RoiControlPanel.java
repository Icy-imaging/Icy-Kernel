/**
 * 
 */
package icy.gui.inspector;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyButton;
import icy.math.MathUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI2DLine;
import icy.roi.ROI2DRectShape;
import icy.sequence.Sequence;
import icy.util.ShapeUtil.ShapeOperation;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * @author Stephane
 */
public class RoiControlPanel extends JPanel implements ColorChangeListener, TextChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7403770406075917063L;

    // GUI
    private IcyTextField nameField;
    private IcyTextField posXField;
    private IcyTextField posYField;
    private IcyTextField posCField;
    private IcyTextField posTField;
    private IcyTextField posZField;
    private IcyTextField sizeXField;
    private IcyTextField sizeZField;
    private IcyTextField sizeYField;
    private IcyTextField sizeCField;
    private IcyTextField sizeTField;
    private JLabel surfacePixelLabel;
    private JLabel intensityMinLabel;
    private JLabel surfaceUnitLabel;
    private JLabel intensityMeanLabel;
    private JLabel intensityMaxLabel;
    ColorChooserButton colorButton;
    private ColorChooserButton selectedColorButton;
    private IcyButton orButton;
    private IcyButton andButton;
    private IcyButton xorButton;
    private IcyButton subButton;
    private IcyButton deleteButton;

    // internal
    final RoisPanel roisPanel;
    boolean isRoiPropertiesAdjusting;

    public RoiControlPanel(RoisPanel roisPanel)
    {
        super();

        this.roisPanel = roisPanel;
        isRoiPropertiesAdjusting = false;

        initialize();

        nameField.addTextChangeListener(this);

        colorButton.addColorChangeListener(this);
        selectedColorButton.addColorChangeListener(this);

        posXField.addTextChangeListener(this);
        posYField.addTextChangeListener(this);
        posCField.addTextChangeListener(this);
        posZField.addTextChangeListener(this);
        posTField.addTextChangeListener(this);
        sizeXField.addTextChangeListener(this);
        sizeYField.addTextChangeListener(this);
        sizeCField.addTextChangeListener(this);
        sizeZField.addTextChangeListener(this);
        sizeTField.addTextChangeListener(this);

        orButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = RoiControlPanel.this.roisPanel.getSequence();

                // OR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = RoiControlPanel.this.roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.OR);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });
        andButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = RoiControlPanel.this.roisPanel.getSequence();

                // AND operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = RoiControlPanel.this.roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.AND);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });
        xorButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = RoiControlPanel.this.roisPanel.getSequence();

                // XOR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = RoiControlPanel.this.roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.XOR);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });
        subButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = RoiControlPanel.this.roisPanel.getSequence();

                // SUB operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = RoiControlPanel.this.roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    // Subtraction work only when 2 ROI are selected
                    if (selectedROI2D.length != 2)
                        return;

                    final ROI mergeROI = ROI2D.subtract(selectedROI2D[0], selectedROI2D[1]);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = RoiControlPanel.this.roisPanel.getSequence();

                sequence.beginUpdate();
                try
                {
                    // delete selected rois
                    for (ROI roi : RoiControlPanel.this.roisPanel.getSelectedRois())
                        if (roi.isEditable())
                            sequence.removeROI(roi);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

    }

    private void initialize()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {0, 0, 16, 0, 0, 0, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0};
        gbl_panel.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        final JLabel lblNewLabel = new JLabel("Name");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        panel.add(lblNewLabel, gbc_lblNewLabel);

        nameField = new IcyTextField();
        GridBagConstraints gbc_nameField = new GridBagConstraints();
        gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameField.gridwidth = 5;
        gbc_nameField.insets = new Insets(0, 0, 5, 0);
        gbc_nameField.gridx = 1;
        gbc_nameField.gridy = 0;
        panel.add(nameField, gbc_nameField);
        nameField.setColumns(10);

        final JLabel lblColor = new JLabel("Color");
        GridBagConstraints gbc_lblColor = new GridBagConstraints();
        gbc_lblColor.anchor = GridBagConstraints.EAST;
        gbc_lblColor.insets = new Insets(0, 0, 0, 5);
        gbc_lblColor.gridx = 0;
        gbc_lblColor.gridy = 1;
        panel.add(lblColor, gbc_lblColor);

        colorButton = new ColorChooserButton();
        GridBagConstraints gbc_colorButton = new GridBagConstraints();
        gbc_colorButton.insets = new Insets(0, 0, 0, 5);
        gbc_colorButton.gridx = 1;
        gbc_colorButton.gridy = 1;
        panel.add(colorButton, gbc_colorButton);

        final JLabel lblNewLabel_1 = new JLabel("Selected color");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_1.gridx = 3;
        gbc_lblNewLabel_1.gridy = 1;
        panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        selectedColorButton = new ColorChooserButton();
        GridBagConstraints gbc_selectedColorButton = new GridBagConstraints();
        gbc_selectedColorButton.insets = new Insets(0, 0, 0, 5);
        gbc_selectedColorButton.gridx = 4;
        gbc_selectedColorButton.gridy = 1;
        panel.add(selectedColorButton, gbc_selectedColorButton);

        final JPanel panel_4 = new JPanel();
        panel_4.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Action", TitledBorder.LEADING,
                TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(panel_4);
        GridBagLayout gbl_panel_4 = new GridBagLayout();
        gbl_panel_4.columnWidths = new int[] {0, 16, 20, 20, 20, 20, 16, 20, 0};
        gbl_panel_4.rowHeights = new int[] {20, 0};
        gbl_panel_4.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panel_4.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panel_4.setLayout(gbl_panel_4);

        JLabel lblBooleanOperation = new JLabel("Boolean operation");
        GridBagConstraints gbc_lblBooleanOperation = new GridBagConstraints();
        gbc_lblBooleanOperation.insets = new Insets(0, 0, 0, 5);
        gbc_lblBooleanOperation.gridx = 0;
        gbc_lblBooleanOperation.gridy = 0;
        panel_4.add(lblBooleanOperation, gbc_lblBooleanOperation);

        orButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_OR));
        orButton.setFlat(true);
        orButton.setToolTipText("Create a new ROI representing the union of selected ROIs");
        GridBagConstraints gbc_orButton = new GridBagConstraints();
        gbc_orButton.fill = GridBagConstraints.BOTH;
        gbc_orButton.insets = new Insets(0, 0, 0, 5);
        gbc_orButton.gridx = 2;
        gbc_orButton.gridy = 0;
        panel_4.add(orButton, gbc_orButton);

        andButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_AND));
        andButton.setFlat(true);
        andButton.setToolTipText("Create a new ROI representing the intersection of selected ROIs");
        GridBagConstraints gbc_andButton = new GridBagConstraints();
        gbc_andButton.fill = GridBagConstraints.BOTH;
        gbc_andButton.insets = new Insets(0, 0, 0, 5);
        gbc_andButton.gridx = 3;
        gbc_andButton.gridy = 0;
        panel_4.add(andButton, gbc_andButton);

        xorButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_XOR));
        xorButton.setFlat(true);
        xorButton.setToolTipText("Create a new ROI representing the exclusive union of selected ROIs");
        GridBagConstraints gbc_xorButton = new GridBagConstraints();
        gbc_xorButton.fill = GridBagConstraints.BOTH;
        gbc_xorButton.insets = new Insets(0, 0, 0, 5);
        gbc_xorButton.gridx = 4;
        gbc_xorButton.gridy = 0;
        panel_4.add(xorButton, gbc_xorButton);

        subButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_SUB));
        subButton.setFlat(true);
        subButton.setToolTipText("Create a new ROI from the result of first ROI minus the second ROI");
        GridBagConstraints gbc_subButton = new GridBagConstraints();
        gbc_subButton.fill = GridBagConstraints.BOTH;
        gbc_subButton.insets = new Insets(0, 0, 0, 5);
        gbc_subButton.gridx = 5;
        gbc_subButton.gridy = 0;
        panel_4.add(subButton, gbc_subButton);

        deleteButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DELETE));
        deleteButton.setFlat(true);
        deleteButton.setToolTipText("Delete selected ROI(s)");
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.fill = GridBagConstraints.BOTH;
        gbc_deleteButton.gridx = 7;
        gbc_deleteButton.gridy = 0;
        panel_4.add(deleteButton, gbc_deleteButton);

        final JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Position", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel_1);
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[] {20, 0, 20, 0, 20, 0, 0};
        gbl_panel_1.rowHeights = new int[] {0, 0, 0};
        gbl_panel_1.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel_1.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        panel_1.setLayout(gbl_panel_1);

        final JLabel lblX = new JLabel("X");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.anchor = GridBagConstraints.EAST;
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        panel_1.add(lblX, gbc_lblX);

        posXField = new IcyTextField();
        posXField.setToolTipText("");
        GridBagConstraints gbc_posXField = new GridBagConstraints();
        gbc_posXField.insets = new Insets(0, 0, 5, 5);
        gbc_posXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posXField.gridx = 1;
        gbc_posXField.gridy = 0;
        panel_1.add(posXField, gbc_posXField);
        posXField.setColumns(10);

        final JLabel lblY = new JLabel("Y");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.anchor = GridBagConstraints.EAST;
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 2;
        gbc_lblY.gridy = 0;
        panel_1.add(lblY, gbc_lblY);

        posYField = new IcyTextField();
        posYField.setToolTipText("");
        GridBagConstraints gbc_posYField = new GridBagConstraints();
        gbc_posYField.insets = new Insets(0, 0, 5, 5);
        gbc_posYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posYField.gridx = 3;
        gbc_posYField.gridy = 0;
        panel_1.add(posYField, gbc_posYField);
        posYField.setColumns(10);

        final JLabel lblZ = new JLabel("Z");
        GridBagConstraints gbc_lblZ = new GridBagConstraints();
        gbc_lblZ.anchor = GridBagConstraints.EAST;
        gbc_lblZ.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ.gridx = 0;
        gbc_lblZ.gridy = 1;
        panel_1.add(lblZ, gbc_lblZ);

        posZField = new IcyTextField();
        posZField.setToolTipText("Attach the ROI to a specific Z slice (-1 = all)");
        GridBagConstraints gbc_posZField = new GridBagConstraints();
        gbc_posZField.insets = new Insets(0, 0, 0, 5);
        gbc_posZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posZField.gridx = 1;
        gbc_posZField.gridy = 1;
        panel_1.add(posZField, gbc_posZField);
        posZField.setColumns(10);

        final JLabel lblT = new JLabel("T");
        GridBagConstraints gbc_lblT = new GridBagConstraints();
        gbc_lblT.anchor = GridBagConstraints.EAST;
        gbc_lblT.insets = new Insets(0, 0, 0, 5);
        gbc_lblT.gridx = 2;
        gbc_lblT.gridy = 1;
        panel_1.add(lblT, gbc_lblT);

        posTField = new IcyTextField();
        posTField.setToolTipText("Attach the ROI to a specific T frame (-1 = all)");
        GridBagConstraints gbc_posTField = new GridBagConstraints();
        gbc_posTField.insets = new Insets(0, 0, 0, 5);
        gbc_posTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posTField.gridx = 3;
        gbc_posTField.gridy = 1;
        panel_1.add(posTField, gbc_posTField);
        posTField.setColumns(10);

        final JLabel lblC = new JLabel("C");
        lblC.setVisible(false);
        GridBagConstraints gbc_lblC = new GridBagConstraints();
        gbc_lblC.anchor = GridBagConstraints.EAST;
        gbc_lblC.insets = new Insets(0, 0, 0, 5);
        gbc_lblC.gridx = 4;
        gbc_lblC.gridy = 1;
        panel_1.add(lblC, gbc_lblC);

        posCField = new IcyTextField();
        posCField.setVisible(false);
        GridBagConstraints gbc_posCField = new GridBagConstraints();
        gbc_posCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posCField.gridx = 5;
        gbc_posCField.gridy = 1;
        panel_1.add(posCField, gbc_posCField);
        posCField.setColumns(10);

        final JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel_2);
        GridBagLayout gbl_panel_2 = new GridBagLayout();
        gbl_panel_2.columnWidths = new int[] {20, 0, 20, 0, 20, 0, 0};
        gbl_panel_2.rowHeights = new int[] {0, 0, 0};
        gbl_panel_2.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel_2.rowWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        panel_2.setLayout(gbl_panel_2);

        final JLabel lblNewLabel_2 = new JLabel("X");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 0;
        panel_2.add(lblNewLabel_2, gbc_lblNewLabel_2);

        sizeXField = new IcyTextField();
        GridBagConstraints gbc_sizeXField = new GridBagConstraints();
        gbc_sizeXField.insets = new Insets(0, 0, 5, 5);
        gbc_sizeXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeXField.gridx = 1;
        gbc_sizeXField.gridy = 0;
        panel_2.add(sizeXField, gbc_sizeXField);
        sizeXField.setColumns(10);

        final JLabel lblY_1 = new JLabel("Y");
        GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.anchor = GridBagConstraints.EAST;
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.gridx = 2;
        gbc_lblY_1.gridy = 0;
        panel_2.add(lblY_1, gbc_lblY_1);

        sizeYField = new IcyTextField();
        GridBagConstraints gbc_sizeYField = new GridBagConstraints();
        gbc_sizeYField.insets = new Insets(0, 0, 5, 5);
        gbc_sizeYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeYField.gridx = 3;
        gbc_sizeYField.gridy = 0;
        panel_2.add(sizeYField, gbc_sizeYField);
        sizeYField.setColumns(10);

        final JLabel lblZ_1 = new JLabel("Z");
        lblZ_1.setVisible(false);
        GridBagConstraints gbc_lblZ_1 = new GridBagConstraints();
        gbc_lblZ_1.anchor = GridBagConstraints.EAST;
        gbc_lblZ_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ_1.gridx = 0;
        gbc_lblZ_1.gridy = 1;
        panel_2.add(lblZ_1, gbc_lblZ_1);

        sizeZField = new IcyTextField();
        sizeZField.setVisible(false);
        GridBagConstraints gbc_sizeZField = new GridBagConstraints();
        gbc_sizeZField.insets = new Insets(0, 0, 0, 5);
        gbc_sizeZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeZField.gridx = 1;
        gbc_sizeZField.gridy = 1;
        panel_2.add(sizeZField, gbc_sizeZField);
        sizeZField.setColumns(10);

        final JLabel lblT_1 = new JLabel("T");
        lblT_1.setVisible(false);
        GridBagConstraints gbc_lblT_1 = new GridBagConstraints();
        gbc_lblT_1.anchor = GridBagConstraints.EAST;
        gbc_lblT_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblT_1.gridx = 2;
        gbc_lblT_1.gridy = 1;
        panel_2.add(lblT_1, gbc_lblT_1);

        sizeTField = new IcyTextField();
        sizeTField.setVisible(false);
        GridBagConstraints gbc_sizeTField = new GridBagConstraints();
        gbc_sizeTField.insets = new Insets(0, 0, 0, 5);
        gbc_sizeTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeTField.gridx = 3;
        gbc_sizeTField.gridy = 1;
        panel_2.add(sizeTField, gbc_sizeTField);
        sizeTField.setColumns(10);

        final JLabel lblC_1 = new JLabel("C");
        lblC_1.setVisible(false);
        GridBagConstraints gbc_lblC_1 = new GridBagConstraints();
        gbc_lblC_1.anchor = GridBagConstraints.EAST;
        gbc_lblC_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblC_1.gridx = 4;
        gbc_lblC_1.gridy = 1;
        panel_2.add(lblC_1, gbc_lblC_1);

        sizeCField = new IcyTextField();
        sizeCField.setVisible(false);
        GridBagConstraints gbc_sizeCField = new GridBagConstraints();
        gbc_sizeCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeCField.gridx = 5;
        gbc_sizeCField.gridy = 1;
        panel_2.add(sizeCField, gbc_sizeCField);
        sizeCField.setColumns(10);

        final JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Surface / Intensity",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(panel_3);
        GridBagLayout gbl_panel_3 = new GridBagLayout();
        gbl_panel_3.columnWidths = new int[] {0, 0, 10, 0, 0, 10, 0, 0, 0};
        gbl_panel_3.rowHeights = new int[] {0, 0, 0};
        gbl_panel_3.columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel_3.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        panel_3.setLayout(gbl_panel_3);

        final JLabel lblPixel = new JLabel("pixels");
        GridBagConstraints gbc_lblPixel = new GridBagConstraints();
        gbc_lblPixel.insets = new Insets(0, 0, 5, 5);
        gbc_lblPixel.anchor = GridBagConstraints.EAST;
        gbc_lblPixel.gridx = 0;
        gbc_lblPixel.gridy = 0;
        panel_3.add(lblPixel, gbc_lblPixel);

        surfacePixelLabel = new JLabel("0");
        surfacePixelLabel.setToolTipText("Surface in number of pixel contained or intersected by the ROI");
        GridBagConstraints gbc_surfacePixelLabel = new GridBagConstraints();
        gbc_surfacePixelLabel.anchor = GridBagConstraints.EAST;
        gbc_surfacePixelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_surfacePixelLabel.gridx = 1;
        gbc_surfacePixelLabel.gridy = 0;
        panel_3.add(surfacePixelLabel, gbc_surfacePixelLabel);

        final JLabel lblUnit = new JLabel("mm");
        GridBagConstraints gbc_lblUnit = new GridBagConstraints();
        gbc_lblUnit.anchor = GridBagConstraints.EAST;
        gbc_lblUnit.insets = new Insets(0, 0, 5, 5);
        gbc_lblUnit.gridx = 3;
        gbc_lblUnit.gridy = 0;
        panel_3.add(lblUnit, gbc_lblUnit);

        surfaceUnitLabel = new JLabel("0");
        surfaceUnitLabel.setToolTipText("Surface contained or intersected by the ROI");
        GridBagConstraints gbc_surfaceUnitLabel = new GridBagConstraints();
        gbc_surfaceUnitLabel.anchor = GridBagConstraints.EAST;
        gbc_surfaceUnitLabel.insets = new Insets(0, 0, 5, 5);
        gbc_surfaceUnitLabel.gridx = 4;
        gbc_surfaceUnitLabel.gridy = 0;
        panel_3.add(surfaceUnitLabel, gbc_surfaceUnitLabel);

        final JLabel lblNewLabel_4 = new JLabel("min");
        GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
        gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_4.gridx = 0;
        gbc_lblNewLabel_4.gridy = 1;
        panel_3.add(lblNewLabel_4, gbc_lblNewLabel_4);

        intensityMinLabel = new JLabel("0");
        intensityMinLabel.setToolTipText("Minimum intensity");
        GridBagConstraints gbc_intensityMinLabel = new GridBagConstraints();
        gbc_intensityMinLabel.anchor = GridBagConstraints.EAST;
        gbc_intensityMinLabel.insets = new Insets(0, 0, 0, 5);
        gbc_intensityMinLabel.gridx = 1;
        gbc_intensityMinLabel.gridy = 1;
        panel_3.add(intensityMinLabel, gbc_intensityMinLabel);

        final JLabel lblMean = new JLabel("mean");
        GridBagConstraints gbc_lblMean = new GridBagConstraints();
        gbc_lblMean.anchor = GridBagConstraints.EAST;
        gbc_lblMean.insets = new Insets(0, 0, 0, 5);
        gbc_lblMean.gridx = 3;
        gbc_lblMean.gridy = 1;
        panel_3.add(lblMean, gbc_lblMean);

        intensityMeanLabel = new JLabel("0");
        intensityMeanLabel.setToolTipText("Mean intensity");
        GridBagConstraints gbc_intensityMeanLabel = new GridBagConstraints();
        gbc_intensityMeanLabel.anchor = GridBagConstraints.EAST;
        gbc_intensityMeanLabel.insets = new Insets(0, 0, 0, 5);
        gbc_intensityMeanLabel.gridx = 4;
        gbc_intensityMeanLabel.gridy = 1;
        panel_3.add(intensityMeanLabel, gbc_intensityMeanLabel);

        final JLabel lblMax = new JLabel("max");
        GridBagConstraints gbc_lblMax = new GridBagConstraints();
        gbc_lblMax.anchor = GridBagConstraints.EAST;
        gbc_lblMax.insets = new Insets(0, 0, 0, 5);
        gbc_lblMax.gridx = 6;
        gbc_lblMax.gridy = 1;
        panel_3.add(lblMax, gbc_lblMax);

        intensityMaxLabel = new JLabel("0");
        intensityMaxLabel.setToolTipText("Maximum intensity");
        GridBagConstraints gbc_intensityMaxLabel = new GridBagConstraints();
        gbc_intensityMaxLabel.anchor = GridBagConstraints.EAST;
        gbc_intensityMaxLabel.gridx = 7;
        gbc_intensityMaxLabel.gridy = 1;
        panel_3.add(intensityMaxLabel, gbc_intensityMaxLabel);
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
            posCField.setText("");
            posZField.setText("");
            posTField.setText("");

            sizeXField.setText("");
            sizeYField.setText("");
            sizeCField.setText("");
            sizeZField.setText("");
            sizeTField.setText("");

            sizeCField.setVisible(false);
            sizeZField.setVisible(false);
            sizeTField.setVisible(false);

            final Sequence sequence = roisPanel.getSequence();

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
                posCField.setEnabled(singleSelect && isRoi2d && editable);
                posZField.setEnabled(singleSelect && isRoi2d && editable);
                posTField.setEnabled(singleSelect && isRoi2d && editable);
                sizeXField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeYField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeCField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeZField.setEnabled(singleSelect && isRoi2dResizeable && editable);
                sizeTField.setEnabled(singleSelect && isRoi2dResizeable && editable);

                colorButton.setEnabled(hasSelected && editable);
                selectedColorButton.setEnabled(hasSelected && editable);

                orButton.setEnabled(severalsSelected);
                andButton.setEnabled(severalsSelected);
                xorButton.setEnabled(severalsSelected);
                subButton.setEnabled(twoSelected);
                deleteButton.setEnabled(hasSelected && editable);

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
                        final String posC = StringUtil.toString(roi2d.getC());
                        final String posZ = StringUtil.toString(roi2d.getZ());
                        final String posT = StringUtil.toString(roi2d.getT());

                        final String sizeX = StringUtil.toString(MathUtil.roundSignificant(bounds.getWidth(), 5, true));
                        final String sizeY = StringUtil
                                .toString(MathUtil.roundSignificant(bounds.getHeight(), 5, true));

                        posXField.setText(posX);
                        posYField.setText(posY);
                        posCField.setText(posC);
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
            }
            else
            {
                nameField.setEnabled(false);
                posXField.setEnabled(false);
                posYField.setEnabled(false);
                posCField.setEnabled(false);
                posZField.setEnabled(false);
                posTField.setEnabled(false);
                sizeXField.setEnabled(false);
                sizeYField.setEnabled(false);
                sizeCField.setEnabled(false);
                sizeZField.setEnabled(false);
                sizeTField.setEnabled(false);

                colorButton.setEnabled(false);
                selectedColorButton.setEnabled(false);

                orButton.setEnabled(false);
                andButton.setEnabled(false);
                xorButton.setEnabled(false);
                subButton.setEnabled(false);
                deleteButton.setEnabled(false);
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
                    }
                    if ((source == posYField) && posYField.isEnabled())
                    {
                        final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                        if (!Double.isNaN(value))
                            roi2d.setPosition(new Point2D.Double(roiBounds.getX(), value));
                    }
                    if ((source == posCField) && posCField.isEnabled())
                        roi2d.setC(Math.max(-1, StringUtil.parseInt(source.getText(), -1)));
                    if ((source == posZField) && posZField.isEnabled())
                        roi2d.setZ(Math.max(-1, StringUtil.parseInt(source.getText(), -1)));
                    if ((source == posTField) && posTField.isEnabled())
                        roi2d.setT(Math.max(-1, StringUtil.parseInt(source.getText(), -1)));

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
                        }
                    }
                }
                else
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
            final Sequence sequence = RoiControlPanel.this.roisPanel.getSequence();
            final ArrayList<ROI> selectedROI = RoiControlPanel.this.roisPanel.getSelectedRois();

            sequence.beginUpdate();
            try
            {
                for (ROI roi : selectedROI)
                    if (roi.isEditable())
                        if (source == colorButton)
                            roi.setColor(color);
                        else
                            roi.setSelectedColor(color);
            }
            finally
            {
                sequence.endUpdate();
            }
        }
    }

}

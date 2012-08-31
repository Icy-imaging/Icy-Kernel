package icy.gui.sequence.tools;

import icy.gui.component.sequence.SequencePreviewPanel;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.math.UnitUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SequenceResizePanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1939174803542745108L;

    enum SizeUnit
    {
        PIXEL, PERCENT, MILLIM, MICROM
    }

    enum ResolutionUnit
    {
        MILLIM_PIXEL, MICROM_PIXEL, PIXEL_MILLIM, PIXEL_MICROM
    }

    private class OriginalModel implements SequenceModel
    {
        public OriginalModel()
        {
            super();
        }

        @Override
        public int getSizeX()
        {
            return getMaxSizeX();
        }

        @Override
        public int getSizeY()
        {
            return getMaxSizeY();
        }

        @Override
        public int getSizeZ()
        {
            return sequence.getSizeZ();
        }

        @Override
        public int getSizeT()
        {
            return sequence.getSizeT();
        }

        @Override
        public int getSizeC()
        {
            return sequence.getSizeC();
        }

        @Override
        public Image getImage(int t, int z)
        {
            return sequence.getImage(t, z);
        }

        @Override
        public Image getImage(int t, int z, int c)
        {
            return sequence.getImage(t, z, c);
        }
    }

    private class ResultModel implements SequenceModel
    {
        public ResultModel()
        {
            super();
        }

        @Override
        public int getSizeX()
        {
            return getMaxSizeX();
        }

        @Override
        public int getSizeY()
        {
            return getMaxSizeY();
        }

        @Override
        public int getSizeZ()
        {
            return sequence.getSizeZ();
        }

        @Override
        public int getSizeT()
        {
            return sequence.getSizeT();
        }

        @Override
        public int getSizeC()
        {
            return sequence.getSizeC();
        }

        @Override
        public Image getImage(int t, int z)
        {
            try
            {
                return IcyBufferedImageUtil.scale(sequence.getImage(t, z), getNewWidth(), getNewHeight(),
                        resizeContentCheckBox.isSelected(), positionAlignmentPanel.getXAlign(),
                        positionAlignmentPanel.getYAlign(), getFilterType());
            }
            catch (OutOfMemoryError e)
            {
                return null;
            }
        }

        @Override
        public Image getImage(int t, int z, int c)
        {
            return ((IcyBufferedImage) getImage(t, z)).getImage(c);
        }
    }

    final Sequence sequence;

    // GUI
    JCheckBox keepRatioCheckBox;
    private JComboBox resizeMethodComboBox;
    JCheckBox resizeContentCheckBox;
    private JLabel contentAlignLabel;
    PositionAlignmentPanel positionAlignmentPanel;
    private JSpinner heightSpinner;
    JSpinner widthSpinner;
    private SequencePreviewPanel originalPreview;
    private SequencePreviewPanel resultPreview;
    private JPanel infoPanel;
    JTextField widthField;
    JTextField heightField;
    private JTextField sizeField;
    private JComboBox sizeUnitComboBox;
    private JComboBox resolutionUnitComboBox;
    private JSpinner resolutionSpinner;

    // internal
    ResolutionUnit previousResolutionUnit;

    /**
     * Create the panel.
     */
    public SequenceResizePanel(Sequence sequence)
    {
        super();

        this.sequence = sequence;

        initialize();

        setNewWidth(sequence.getSizeX());
        setNewHeight(sequence.getSizeY());
        resolutionSpinner.setValue(Double.valueOf(sequence.getPixelSizeX()));

        originalPreview.setFitToView(false);
        resultPreview.setFitToView(false);
        originalPreview.setModel(new OriginalModel());
        resultPreview.setModel(new ResultModel());

        previousResolutionUnit = getResolutionUnit();

        updateGUI();
        updatePreview();

        final ActionListener actionUpdatePreview = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updatePreview();
            }
        };
        final ActionListener actionUpdateBoth = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateGUI();
                updatePreview();
            }
        };

        final ChangeListener spinnerChangeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // maintain ratio
                if (keepRatioCheckBox.isSelected())
                {
                    final Sequence seq = SequenceResizePanel.this.sequence;

                    if (e.getSource() == widthSpinner)
                    {
                        // adjust height
                        final double ratio = (double) getNewWidth() / (double) seq.getWidth();
                        setNewHeight((int) Math.round(seq.getHeight() * ratio));
                    }
                    else
                    {
                        // adjust width
                        final double ratio = (double) getNewHeight() / (double) seq.getHeight();
                        setNewWidth((int) Math.round(seq.getWidth() * ratio));
                    }
                }

                updatePreview();
            }
        };

        resizeContentCheckBox.addActionListener(actionUpdateBoth);
        resizeMethodComboBox.addActionListener(actionUpdatePreview);
        positionAlignmentPanel.addActionListener(actionUpdatePreview);
        heightSpinner.addChangeListener(spinnerChangeListener);
        widthSpinner.addChangeListener(spinnerChangeListener);

        sizeUnitComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // convert width and height in new unit
                final int w = Integer.parseInt(widthField.getText());
                final int h = Integer.parseInt(heightField.getText());

                setNewWidth(w);
                setNewHeight(h);
            }
        });

        resolutionUnitComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateResolution();
            }
        });
    }

    private void initialize()
    {
        setLayout(null);

        infoPanel = new JPanel();
        infoPanel.setBounds(0, 0, 354, 83);
        add(infoPanel);
        infoPanel
                .setBorder(new TitledBorder(null, "Size / Memory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        infoPanel.setLayout(null);

        final JLabel lblOriginalWidth = new JLabel("Width");
        lblOriginalWidth.setBounds(23, 20, 43, 14);
        infoPanel.add(lblOriginalWidth);
        lblOriginalWidth.setToolTipText("");

        final JLabel lblNewLabel_3 = new JLabel("Height");
        lblNewLabel_3.setBounds(115, 20, 75, 14);
        infoPanel.add(lblNewLabel_3);
        lblNewLabel_3.setToolTipText("");

        final JLabel lblNewLabel_2 = new JLabel("Size");
        lblNewLabel_2.setBounds(223, 20, 86, 14);
        infoPanel.add(lblNewLabel_2);
        lblNewLabel_2.setToolTipText("");

        widthField = new JTextField();
        widthField.setToolTipText("Width in pixel");
        widthField.setText("0000");
        widthField.setEditable(false);
        widthField.setBounds(21, 40, 75, 20);
        infoPanel.add(widthField);
        widthField.setColumns(10);

        heightField = new JTextField();
        heightField.setToolTipText("Height in pixel");
        heightField.setText("0");
        heightField.setEditable(false);
        heightField.setBounds(115, 40, 75, 20);
        infoPanel.add(heightField);
        heightField.setColumns(10);

        sizeField = new JTextField();
        sizeField.setToolTipText("Memory size");
        sizeField.setText("0.0B");
        sizeField.setEditable(false);
        sizeField.setBounds(223, 40, 86, 20);
        infoPanel.add(sizeField);
        sizeField.setColumns(10);

        JPanel settingPanel = new JPanel();
        settingPanel.setBounds(0, 81, 354, 208);
        add(settingPanel);
        settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Setting",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        settingPanel.setLayout(null);

        final JLabel lblWidth = new JLabel("Width");
        lblWidth.setBounds(21, 26, 75, 14);
        settingPanel.add(lblWidth);

        widthSpinner = new JSpinner();
        widthSpinner.setBounds(21, 45, 75, 20);
        widthSpinner.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
        widthSpinner.setToolTipText("New width to set");
        settingPanel.add(widthSpinner);

        final JLabel lblNewLabel = new JLabel("Height");
        lblNewLabel.setBounds(21, 82, 75, 14);
        settingPanel.add(lblNewLabel);

        heightSpinner = new JSpinner();
        lblNewLabel.setLabelFor(heightSpinner);
        heightSpinner.setBounds(21, 100, 75, 20);
        heightSpinner.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
        heightSpinner.setToolTipText("New height to set");
        settingPanel.add(heightSpinner);

        keepRatioCheckBox = new JCheckBox("Keep ratio");
        keepRatioCheckBox.setBounds(223, 26, 87, 23);
        keepRatioCheckBox.setToolTipText("Keep original aspect ratio");
        keepRatioCheckBox.setSelected(true);
        settingPanel.add(keepRatioCheckBox);

        resizeContentCheckBox = new JCheckBox("Resize content");
        resizeContentCheckBox.setBounds(223, 52, 110, 23);
        resizeContentCheckBox.setToolTipText("Define if content is resized or not");
        resizeContentCheckBox.setSelected(true);
        settingPanel.add(resizeContentCheckBox);

        resizeMethodComboBox = new JComboBox();
        resizeMethodComboBox.setBounds(233, 82, 100, 20);
        resizeMethodComboBox.setToolTipText("Filter method used to resize content");
        settingPanel.add(resizeMethodComboBox);
        resizeMethodComboBox.setModel(new DefaultComboBoxModel(new String[] {"Nearest", "Bilinear", "Bicubic"}));

        contentAlignLabel = new JLabel("Content alignment");
        contentAlignLabel.setBounds(223, 83, 100, 18);
        contentAlignLabel.setToolTipText("Set content alignment (only when content is not resized)");
        settingPanel.add(contentAlignLabel);

        positionAlignmentPanel = new PositionAlignmentPanel();
        positionAlignmentPanel.setBounds(233, 110, 100, 90);
        settingPanel.add(positionAlignmentPanel);

        sizeUnitComboBox = new JComboBox();
        sizeUnitComboBox.setToolTipText("Width / Height unit");
        sizeUnitComboBox.setModel(new DefaultComboBoxModel(new String[] {"pixel", "%", "mm", "\u00B5m"}));
        sizeUnitComboBox.setSelectedIndex(0);
        sizeUnitComboBox.setBounds(115, 71, 87, 20);
        settingPanel.add(sizeUnitComboBox);

        final JSeparator separator = new JSeparator();
        separator.setOrientation(SwingConstants.VERTICAL);
        separator.setBounds(105, 52, 2, 60);
        settingPanel.add(separator);

        final JSeparator separator_1 = new JSeparator();
        separator_1.setBounds(105, 82, 8, 2);
        settingPanel.add(separator_1);

        final JLabel lblResolution = new JLabel("Resolution XY");
        lblResolution.setBounds(21, 148, 75, 14);
        settingPanel.add(lblResolution);

        resolutionSpinner = new JSpinner();
        resolutionSpinner.setModel(new SpinnerNumberModel(1d, 0.0001d, Double.MAX_VALUE, 0.01d));
        resolutionSpinner.setToolTipText("Pixel resolution for X and Y dimension");
        resolutionSpinner.setBounds(21, 168, 75, 20);
        settingPanel.add(resolutionSpinner);

        resolutionUnitComboBox = new JComboBox();
        resolutionUnitComboBox.setToolTipText("Resolution unit");
        resolutionUnitComboBox.setModel(new DefaultComboBoxModel(new String[] {"mm/pixel", "\u00B5m/pixel", "pixel/mm",
                "pixel/\u00B5m"}));
        resolutionUnitComboBox.setSelectedIndex(0);
        resolutionUnitComboBox.setBounds(115, 168, 87, 20);
        settingPanel.add(resolutionUnitComboBox);

        final JPanel previewPanel = new JPanel();
        previewPanel.setBounds(0, 288, 354, 161);
        previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(previewPanel);
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.LINE_AXIS));

        originalPreview = new SequencePreviewPanel("Original");
        previewPanel.add(originalPreview);

        resultPreview = new SequencePreviewPanel("Result");
        previewPanel.add(resultPreview);
    }

    void updateGUI()
    {
        final boolean resizeContent = resizeContentCheckBox.isSelected();

        if (resizeContent)
        {
            resizeMethodComboBox.setVisible(true);
            resizeMethodComboBox.setEnabled(true);
            contentAlignLabel.setEnabled(false);
            positionAlignmentPanel.setEnabled(false);
            contentAlignLabel.setVisible(false);
            positionAlignmentPanel.setVisible(false);
        }
        else
        {
            resizeMethodComboBox.setEnabled(false);
            resizeMethodComboBox.setVisible(false);
            contentAlignLabel.setVisible(true);
            positionAlignmentPanel.setVisible(true);
            contentAlignLabel.setEnabled(true);
            positionAlignmentPanel.setEnabled(true);
        }
    }

    void updatePreview()
    {
        final int w = getNewWidth();
        final int h = getNewHeight();

        widthField.setText(Integer.toString(w));
        heightField.setText(Integer.toString(h));
        sizeField.setText(UnitUtil.getBytesString(w * h * sequence.getSizeC() * sequence.getSizeZ()
                * sequence.getSizeT() * sequence.getDataType_().getSize()));

        originalPreview.imageChanged();
        resultPreview.imageChanged();
    }

    public Sequence getSequence()
    {
        return sequence;
    }

    public boolean getResizeContent()
    {
        return resizeContentCheckBox.isSelected();
    }

    /**
     * Returns the selected horizontal alignment.<br>
     * Possible values are <code>SwingConstants.LEFT / CENTER / RIGHT</code>
     **/
    public int getXAlign()
    {
        return positionAlignmentPanel.getXAlign();
    }

    /**
     * Return the selected vertical alignment.<br>
     * Possible values are <code>SwingConstants.TOP / CENTER / BOTTOM</code>
     **/
    public int getYAlign()
    {
        return positionAlignmentPanel.getYAlign();
    }

    public FilterType getFilterType()
    {
        switch (resizeMethodComboBox.getSelectedIndex())
        {
            default:
            case 0:
                return FilterType.NEAREST;
            case 1:
                return FilterType.BILINEAR;
            case 2:
                return FilterType.BICUBIC;
        }
    }

    public SizeUnit getSizeUnit()
    {
        switch (sizeUnitComboBox.getSelectedIndex())
        {
            default:
            case 0:
                return SizeUnit.PIXEL;
            case 1:
                return SizeUnit.PERCENT;
            case 2:
                return SizeUnit.MILLIM;
            case 3:
                return SizeUnit.MICROM;
        }
    }

    public ResolutionUnit getResolutionUnit()
    {
        switch (resolutionUnitComboBox.getSelectedIndex())
        {
            default:
            case 0:
                return ResolutionUnit.MILLIM_PIXEL;
            case 1:
                return ResolutionUnit.MICROM_PIXEL;
            case 2:
                return ResolutionUnit.PIXEL_MILLIM;
            case 3:
                return ResolutionUnit.PIXEL_MICROM;
        }
    }

    public int unitToPixel(double value, int originPixel, SizeUnit unit)
    {
        final double mmPerPixel = getResolution();

        switch (unit)
        {
            default:
            case PIXEL:
                return (int) Math.round(value);
            case PERCENT:
                return (int) Math.round((originPixel * value) / 100d);
            case MILLIM:
                return (int) Math.round(value / mmPerPixel);
            case MICROM:
                return (int) Math.round(value / (mmPerPixel * 1000d));
        }
    }

    public double pixelToUnit(int value, int originPixel, SizeUnit unit)
    {
        final double mmPerPixel = getResolution();

        switch (unit)
        {
            default:
            case PIXEL:
                return value;
            case PERCENT:
                return (int) Math.round((value * 100d) / originPixel);
            case MILLIM:
                return (int) (value * mmPerPixel);
            case MICROM:
                return (int) (value * mmPerPixel * 1000d);
        }
    }

    /**
     * Get resolution in mm per pixel.
     */
    public double getResolution()
    {
        final double value = ((Double) resolutionSpinner.getValue()).doubleValue();

        switch (getResolutionUnit())
        {
            default:
            case MILLIM_PIXEL:
                return value;

            case MICROM_PIXEL:
                return value / 1000d;

            case PIXEL_MILLIM:
                return 1d / value;

            case PIXEL_MICROM:
                return 1d / (value * 1000d);
        }
    }

    void updateResolution()
    {
        double resol = ((Double) resolutionSpinner.getValue()).doubleValue();

        // convert to mm / pixel
        switch (previousResolutionUnit)
        {
            case MICROM_PIXEL:
                resol /= 1000d;
                break;
            case PIXEL_MILLIM:
                resol = 1d / resol;
                break;
            case PIXEL_MICROM:
                resol = 1d / (resol * 1000d);
                break;
        }

        previousResolutionUnit = getResolutionUnit();

        // convert back to wanted unit
        switch (previousResolutionUnit)
        {
            case MICROM_PIXEL:
                resol *= 1000d;
                break;
            case PIXEL_MILLIM:
                resol = 1d / resol;
                break;
            case PIXEL_MICROM:
                resol = 1d / (resol * 1000d);
                break;
        }
        
        resol = Math.max(0.000001, resol);

        resolutionSpinner.setValue(Double.valueOf(resol));

    }

    public double getSpinnerSizeValue(JSpinner spinner)
    {
        switch (getSizeUnit())
        {
            default:
            case PIXEL:
                return ((Integer) spinner.getValue()).intValue();

            case PERCENT:
            case MILLIM:
            case MICROM:
                return ((Double) spinner.getValue()).doubleValue();
        }
    }

    public int getNewWidth()
    {
        final int result = unitToPixel(getSpinnerSizeValue(widthSpinner), sequence.getSizeX(), getSizeUnit());

        return Math.min(65535, Math.max(1, result));
    }

    public int getNewHeight()
    {
        final int result = unitToPixel(getSpinnerSizeValue(heightSpinner), sequence.getSizeY(), getSizeUnit());

        return Math.min(Math.max(1, result), 65535);
    }

    void setSpinnerSizeValue(JSpinner spinner, double value)
    {
        switch (getSizeUnit())
        {
            default:
            case PIXEL:
                spinner.setModel(new SpinnerNumberModel((int) value, 0, 65535, 1));
                break;

            case PERCENT:
                spinner.setModel(new SpinnerNumberModel(value, 0d, Double.MAX_VALUE, 1d));
                break;

            case MILLIM:
            case MICROM:
                spinner.setModel(new SpinnerNumberModel(value, 0d, Double.MAX_VALUE, 0.01d));
                break;
        }
    }

    void setNewWidth(int value)
    {
        setSpinnerSizeValue(widthSpinner, pixelToUnit(value, sequence.getSizeX(), getSizeUnit()));
    }

    void setNewHeight(int value)
    {
        setSpinnerSizeValue(heightSpinner, pixelToUnit(value, sequence.getSizeY(), getSizeUnit()));
    }

    public int getMaxSizeX()
    {
        return Math.max(getNewWidth(), sequence.getSizeX());
    }

    public int getMaxSizeY()
    {
        return Math.max(getNewHeight(), sequence.getSizeY());
    }
}

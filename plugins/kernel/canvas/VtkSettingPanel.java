package plugins.kernel.canvas;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.NumberTextField;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;
import icy.vtk.VtkImageVolume.VtkVolumeMapperType;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class VtkSettingPanel extends JPanel implements ActionListener, TextChangeListener, ColorChangeListener

{
    /**
     * 
     */
    private static final long serialVersionUID = 6433369095311474470L;

    public static final String PROPERTY_BG_COLOR = "renderBGColor";
    public static final String PROPERTY_MAPPER = "volumeMapper";
    public static final String PROPERTY_BLENDING = "volumeBlending";
    public static final String PROPERTY_SAMPLE = "volumeSample";
    public static final String PROPERTY_INTERPOLATION = "volumeInterpolation";
    public static final String PROPERTY_AMBIENT = "volumeAmbient";
    public static final String PROPERTY_DIFFUSE = "volumeDiffuse";
    public static final String PROPERTY_SPECULAR = "volumeSpecular";

    /**
     * GUI
     */
    private ColorChooserButton bgColorButton;
    private JComboBox volumeMapperComboBox;
    private JComboBox volumeBlendingComboBox;
    private JComboBox volumeSampleComboBox;
    private JComboBox volumeInterpolationComboBox;
    private NumberTextField volumeAmbientField;
    private NumberTextField volumeSpecularField;
    private NumberTextField volumeDiffuseField;

    /**
     * Create the panel.
     */
    public VtkSettingPanel()
    {
        super();

        initialize();

        updateState();

        bgColorButton.addColorChangeListener(this);

        volumeMapperComboBox.addActionListener(this);
        volumeBlendingComboBox.addActionListener(this);
        volumeInterpolationComboBox.addActionListener(this);
        volumeSampleComboBox.addActionListener(this);

        volumeAmbientField.addTextChangeListener(this);
        volumeDiffuseField.addTextChangeListener(this);
        volumeSpecularField.addTextChangeListener(this);
    }

    protected void initialize()
    {
        setBorder(new EmptyBorder(2, 0, 2, 0));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblBackground = new JLabel("Background color");
        lblBackground.setToolTipText("Change background color");
        GridBagConstraints gbc_lblBackground = new GridBagConstraints();
        gbc_lblBackground.anchor = GridBagConstraints.WEST;
        gbc_lblBackground.insets = new Insets(0, 0, 5, 5);
        gbc_lblBackground.gridx = 0;
        gbc_lblBackground.gridy = 0;
        add(lblBackground, gbc_lblBackground);

        bgColorButton = new ColorChooserButton();
        bgColorButton.setToolTipText("Change background color");
        GridBagConstraints gbc_bgColorButton = new GridBagConstraints();
        gbc_bgColorButton.anchor = GridBagConstraints.WEST;
        gbc_bgColorButton.insets = new Insets(0, 0, 5, 5);
        gbc_bgColorButton.gridx = 1;
        gbc_bgColorButton.gridy = 0;
        add(bgColorButton, gbc_bgColorButton);

        final JLabel lblMapper = new JLabel("Mapper");
        lblMapper.setToolTipText("Select volume rendering method");
        GridBagConstraints gbc_lblMapper = new GridBagConstraints();
        gbc_lblMapper.anchor = GridBagConstraints.WEST;
        gbc_lblMapper.insets = new Insets(0, 0, 5, 5);
        gbc_lblMapper.gridx = 0;
        gbc_lblMapper.gridy = 1;
        add(lblMapper, gbc_lblMapper);

        volumeMapperComboBox = new JComboBox();
        volumeMapperComboBox.setToolTipText("Select volume rendering method");
        volumeMapperComboBox.setModel(new DefaultComboBoxModel(VtkVolumeMapperType.values()));
        GridBagConstraints gbc_volumeMapperComboBox = new GridBagConstraints();
        gbc_volumeMapperComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeMapperComboBox.gridwidth = 3;
        gbc_volumeMapperComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeMapperComboBox.gridx = 1;
        gbc_volumeMapperComboBox.gridy = 1;
        add(volumeMapperComboBox, gbc_volumeMapperComboBox);

        final JLabel lblInterpolation = new JLabel("Interpolation  ");
        lblInterpolation.setToolTipText("Select volume rendering interpolation method");
        GridBagConstraints gbc_lblInterpolation = new GridBagConstraints();
        gbc_lblInterpolation.anchor = GridBagConstraints.WEST;
        gbc_lblInterpolation.insets = new Insets(0, 0, 5, 5);
        gbc_lblInterpolation.gridx = 0;
        gbc_lblInterpolation.gridy = 2;
        add(lblInterpolation, gbc_lblInterpolation);

        volumeInterpolationComboBox = new JComboBox();
        volumeInterpolationComboBox.setToolTipText("Select volume rendering interpolation method");
        volumeInterpolationComboBox.setMaximumRowCount(7);
        volumeInterpolationComboBox.setModel(new DefaultComboBoxModel(new String[] {"Nearest (Fast)", "Linear",
                "Cubic (Slow)"}));
        volumeInterpolationComboBox.setSelectedIndex(1);
        GridBagConstraints gbc_volumeInterpolationComboBox = new GridBagConstraints();
        gbc_volumeInterpolationComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeInterpolationComboBox.gridwidth = 3;
        gbc_volumeInterpolationComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeInterpolationComboBox.gridx = 1;
        gbc_volumeInterpolationComboBox.gridy = 2;
        add(volumeInterpolationComboBox, gbc_volumeInterpolationComboBox);

        JLabel lblBlending = new JLabel("Blending");
        lblBlending.setToolTipText("Select volume rendering blending method");
        GridBagConstraints gbc_lblBlending = new GridBagConstraints();
        gbc_lblBlending.anchor = GridBagConstraints.WEST;
        gbc_lblBlending.insets = new Insets(0, 0, 5, 5);
        gbc_lblBlending.gridx = 0;
        gbc_lblBlending.gridy = 3;
        add(lblBlending, gbc_lblBlending);

        volumeBlendingComboBox = new JComboBox();
        volumeBlendingComboBox.setToolTipText("Select volume rendering blending method");
        volumeBlendingComboBox.setModel(new DefaultComboBoxModel(VtkVolumeBlendType.values()));
        GridBagConstraints gbc_volumeBlendingComboBox = new GridBagConstraints();
        gbc_volumeBlendingComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeBlendingComboBox.gridwidth = 3;
        gbc_volumeBlendingComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeBlendingComboBox.gridx = 1;
        gbc_volumeBlendingComboBox.gridy = 3;
        add(volumeBlendingComboBox, gbc_volumeBlendingComboBox);

        final JLabel lblSample = new JLabel("Sample");
        lblSample.setToolTipText("Set volume sample resolution (raycaster mapper only)");
        GridBagConstraints gbc_lblSample = new GridBagConstraints();
        gbc_lblSample.anchor = GridBagConstraints.WEST;
        gbc_lblSample.insets = new Insets(0, 0, 5, 5);
        gbc_lblSample.gridx = 0;
        gbc_lblSample.gridy = 4;
        add(lblSample, gbc_lblSample);

        volumeSampleComboBox = new JComboBox();
        volumeSampleComboBox
                .setToolTipText("Use low value for fine (but slow) rendering and high value for fast (but coarse) rendering");
        volumeSampleComboBox.setMaximumRowCount(11);
        volumeSampleComboBox.setModel(new DefaultComboBoxModel(new String[] {"Auto", "1 (Slow)", "2", "3", "4", "5",
                "6", "7", "8", "9", "10 (Fast)"}));
        volumeSampleComboBox.setSelectedIndex(0);
        GridBagConstraints gbc_volumeSampleComboBox = new GridBagConstraints();
        gbc_volumeSampleComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeSampleComboBox.gridwidth = 3;
        gbc_volumeSampleComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeSampleComboBox.gridx = 1;
        gbc_volumeSampleComboBox.gridy = 4;
        add(volumeSampleComboBox, gbc_volumeSampleComboBox);

        JLabel lblShading = new JLabel("Light");
        lblShading.setToolTipText("Set volume light properties");
        GridBagConstraints gbc_lblShading = new GridBagConstraints();
        gbc_lblShading.insets = new Insets(0, 0, 0, 5);
        gbc_lblShading.anchor = GridBagConstraints.WEST;
        gbc_lblShading.gridx = 0;
        gbc_lblShading.gridy = 5;
        add(lblShading, gbc_lblShading);

        volumeAmbientField = new NumberTextField();
        volumeAmbientField.setToolTipText("Ambient lighting coefficient");
        GridBagConstraints gbc_volumeAmbientField = new GridBagConstraints();
        gbc_volumeAmbientField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeAmbientField.insets = new Insets(0, 0, 0, 5);
        gbc_volumeAmbientField.gridx = 1;
        gbc_volumeAmbientField.gridy = 5;
        add(volumeAmbientField, gbc_volumeAmbientField);
        volumeAmbientField.setColumns(4);

        volumeDiffuseField = new NumberTextField();
        volumeDiffuseField.setToolTipText("Diffuse lighting coefficient");
        GridBagConstraints gbc_volumeDiffuseField = new GridBagConstraints();
        gbc_volumeDiffuseField.insets = new Insets(0, 0, 0, 5);
        gbc_volumeDiffuseField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeDiffuseField.gridx = 2;
        gbc_volumeDiffuseField.gridy = 5;
        add(volumeDiffuseField, gbc_volumeDiffuseField);
        volumeDiffuseField.setColumns(4);

        volumeSpecularField = new NumberTextField();
        volumeSpecularField.setToolTipText("Specular lighting coefficient");
        GridBagConstraints gbc_volumeSpecularField = new GridBagConstraints();
        gbc_volumeSpecularField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeSpecularField.gridx = 3;
        gbc_volumeSpecularField.gridy = 5;
        add(volumeSpecularField, gbc_volumeSpecularField);
        volumeSpecularField.setColumns(4);
    }

    protected void updateState()
    {
        // if (isBoundingBoxVisible())
        // {
        // boundingBoxGridCheckBox.setEnabled(true);
        // boundingBoxRulerCheckBox.setEnabled(true);
        // }
        // else
        // {
        // boundingBoxGridCheckBox.setEnabled(false);
        // boundingBoxRulerCheckBox.setEnabled(false);
        // }

        switch (getVolumeMapperType())
        {
            case RAYCAST_CPU_FIXEDPOINT:
            case RAYCAST_GPU_OPENGL:
                volumeSampleComboBox.setEnabled(true);
                volumeBlendingComboBox.setEnabled(true);
                break;
            case TEXTURE2D_OPENGL:
            case TEXTURE3D_OPENGL:
                volumeSampleComboBox.setEnabled(false);
                volumeBlendingComboBox.setEnabled(false);
                break;
        }
    }

    public Color getBackgroundColor()
    {
        return bgColorButton.getColor();
    }

    public void setBackgroundColor(Color value)
    {
        bgColorButton.setColor(value);
    }

    public VtkVolumeMapperType getVolumeMapperType()
    {
        if (volumeMapperComboBox.getSelectedIndex() == -1)
            return null;

        return (VtkVolumeMapperType) volumeMapperComboBox.getSelectedItem();
    }

    public void setVolumeMapperType(VtkVolumeMapperType value)
    {
        volumeMapperComboBox.setSelectedItem(value);
    }

    public int getVolumeInterpolation()
    {
        return volumeInterpolationComboBox.getSelectedIndex();
    }

    public void setVolumeInterpolation(int value)
    {
        volumeInterpolationComboBox.setSelectedIndex(value);
    }

    public VtkVolumeBlendType getVolumeBlendingMode()
    {
        if (volumeBlendingComboBox.getSelectedIndex() == -1)
            return null;

        return (VtkVolumeBlendType) volumeBlendingComboBox.getSelectedItem();
    }

    public void setVolumeBlendingMode(VtkVolumeBlendType value)
    {
        volumeBlendingComboBox.setSelectedItem(value);
    }

    public int getVolumeSample()
    {
        return volumeSampleComboBox.getSelectedIndex();
    }

    public void setVolumeSample(int value)
    {
        volumeSampleComboBox.setSelectedIndex(value);
    }

    public double getVolumeAmbient()
    {
        return volumeAmbientField.getNumericValue();
    }

    public void setVolumeAmbient(double value)
    {
        volumeAmbientField.setNumericValue(value);
    }

    public double getVolumeDiffuse()
    {
        return volumeDiffuseField.getNumericValue();
    }

    public void setVolumeDiffuse(double value)
    {
        volumeDiffuseField.setNumericValue(value);
    }

    public double getVolumeSpecular()
    {
        return volumeSpecularField.getNumericValue();
    }

    public void setVolumeSpecular(double value)
    {
        volumeSpecularField.setNumericValue(value);
    }

    @Override
    public void colorChanged(ColorChooserButton source)
    {
        if (source == bgColorButton)
            firePropertyChange(PROPERTY_BG_COLOR, null, source.getColor());
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();

        if (source == volumeMapperComboBox)
            firePropertyChange(PROPERTY_MAPPER, null, volumeMapperComboBox.getSelectedItem());
        else if (source == volumeBlendingComboBox)
            firePropertyChange(PROPERTY_BLENDING, null, volumeBlendingComboBox.getSelectedItem());
        else if (source == volumeSampleComboBox)
            firePropertyChange(PROPERTY_SAMPLE, -1, volumeSampleComboBox.getSelectedIndex());
        else if (source == volumeInterpolationComboBox)
            firePropertyChange(PROPERTY_INTERPOLATION, -1, volumeInterpolationComboBox.getSelectedIndex());

        updateState();
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (!validate)
            return;

        if (source == volumeAmbientField)
            firePropertyChange(PROPERTY_AMBIENT, -1d, volumeAmbientField.getNumericValue());
        else if (source == volumeDiffuseField)
            firePropertyChange(PROPERTY_DIFFUSE, -1d, volumeDiffuseField.getNumericValue());
        else if (source == volumeSpecularField)
            firePropertyChange(PROPERTY_SPECULAR, -1d, volumeSpecularField.getNumericValue());

        updateState();
    }
}

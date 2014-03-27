package plugins.kernel.canvas;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.NumberTextField;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;
import icy.vtk.VtkImageVolume.VtkVolumeMapperType;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VtkVolumeSettingPanel extends JPanel implements ActionListener, TextChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -3491883285769658325L;

    public static final String PROPERTY_MAPPER = "volumeMapper";
    public static final String PROPERTY_BLENDING = "volumeBlending";
    public static final String PROPERTY_SAMPLE = "volumeSample";
    public static final String PROPERTY_INTERPOLATION = "volumeInterpolation";
    public static final String PROPERTY_SHADING = "volumeShading";
    public static final String PROPERTY_AMBIENT = "volumeAmbient";
    public static final String PROPERTY_DIFFUSE = "volumeDiffuse";
    public static final String PROPERTY_SPECULAR_INTENSITY = "volumeSpecularIntensity";
    public static final String PROPERTY_SPECULAR_POWER = "volumeSpecularPower";

    /**
     * GUI
     */
    private JComboBox mapperComboBox;
    private JComboBox blendingComboBox;
    private JComboBox sampleComboBox;
    private JComboBox interpolationComboBox;
    private JCheckBox shadingCheckBox;
    private NumberTextField ambientField;
    private NumberTextField diffuseField;
    private NumberTextField specularIntensityField;
    private NumberTextField specularPowerField;

    /**
     * Create the panel.
     */
    public VtkVolumeSettingPanel()
    {
        super();

        initialize();

        mapperComboBox.addActionListener(this);
        blendingComboBox.addActionListener(this);
        interpolationComboBox.addActionListener(this);
        sampleComboBox.addActionListener(this);
        shadingCheckBox.addActionListener(this);

        ambientField.addTextChangeListener(this);
        diffuseField.addTextChangeListener(this);
        specularIntensityField.addTextChangeListener(this);
        specularPowerField.addTextChangeListener(this);
    }

    private void initialize()
    {
        setBorder(null);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblMapper = new JLabel("Mapper");
        GridBagConstraints gbc_lblMapper = new GridBagConstraints();
        gbc_lblMapper.anchor = GridBagConstraints.WEST;
        gbc_lblMapper.insets = new Insets(0, 0, 0, 5);
        gbc_lblMapper.gridx = 0;
        gbc_lblMapper.gridy = 0;
        add(lblMapper, gbc_lblMapper);

        mapperComboBox = new JComboBox();
        mapperComboBox.setModel(new DefaultComboBoxModel(VtkVolumeMapperType.values()));
        GridBagConstraints gbc_mapperComboBox = new GridBagConstraints();
        gbc_mapperComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_mapperComboBox.gridwidth = 3;
        gbc_mapperComboBox.insets = new Insets(0, 0, 0, 5);
        gbc_mapperComboBox.gridx = 1;
        gbc_mapperComboBox.gridy = 0;
        add(mapperComboBox, gbc_mapperComboBox);

        JLabel lblBlending = new JLabel("Blending");
        GridBagConstraints gbc_lblBlending = new GridBagConstraints();
        gbc_lblBlending.anchor = GridBagConstraints.WEST;
        gbc_lblBlending.insets = new Insets(0, 0, 0, 5);
        gbc_lblBlending.gridx = 0;
        gbc_lblBlending.gridy = 1;
        add(lblBlending, gbc_lblBlending);

        blendingComboBox = new JComboBox();
        blendingComboBox.setModel(new DefaultComboBoxModel(VtkVolumeBlendType.values()));
        GridBagConstraints gbc_blendingComboBox = new GridBagConstraints();
        gbc_blendingComboBox.gridwidth = 3;
        gbc_blendingComboBox.insets = new Insets(0, 0, 0, 5);
        gbc_blendingComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_blendingComboBox.gridx = 1;
        gbc_blendingComboBox.gridy = 1;
        add(blendingComboBox, gbc_blendingComboBox);

        final JLabel lblInterpolation = new JLabel("Interpolation");
        GridBagConstraints gbc_lblInterpolation = new GridBagConstraints();
        gbc_lblInterpolation.anchor = GridBagConstraints.WEST;
        gbc_lblInterpolation.insets = new Insets(0, 0, 0, 5);
        gbc_lblInterpolation.gridx = 0;
        gbc_lblInterpolation.gridy = 2;
        add(lblInterpolation, gbc_lblInterpolation);

        interpolationComboBox = new JComboBox();
        interpolationComboBox.setMaximumRowCount(7);
        interpolationComboBox.setModel(new DefaultComboBoxModel(new String[] {"Nearest (Fast)", "Linear", "Cubic (Slow)"}));
        interpolationComboBox.setSelectedIndex(1);
        GridBagConstraints gbc_interpolationComboBox = new GridBagConstraints();
        gbc_interpolationComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_interpolationComboBox.gridwidth = 3;
        gbc_interpolationComboBox.insets = new Insets(0, 0, 0, 5);
        gbc_interpolationComboBox.gridx = 1;
        gbc_interpolationComboBox.gridy = 2;
        add(interpolationComboBox, gbc_interpolationComboBox);

        final JLabel lblSample = new JLabel("Sample");
        lblSample.setToolTipText("Sample resolution (raycaster mapper only)");
        GridBagConstraints gbc_lblSample = new GridBagConstraints();
        gbc_lblSample.anchor = GridBagConstraints.WEST;
        gbc_lblSample.insets = new Insets(0, 0, 0, 5);
        gbc_lblSample.gridx = 0;
        gbc_lblSample.gridy = 3;
        add(lblSample, gbc_lblSample);

        sampleComboBox = new JComboBox();
        sampleComboBox
                .setToolTipText("Use low value for fine (but slow) rendering and high value for fast (but coarse) rendering");
        sampleComboBox.setMaximumRowCount(11);
        sampleComboBox.setModel(new DefaultComboBoxModel(new String[] {"Auto", "1 (Slow)", "2", "3", "4", "5", "6",
                "7", "8", "9", "10 (Fast)"}));
        sampleComboBox.setSelectedIndex(0);
        GridBagConstraints gbc_sampleComboBox = new GridBagConstraints();
        gbc_sampleComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_sampleComboBox.gridwidth = 3;
        gbc_sampleComboBox.insets = new Insets(0, 0, 0, 5);
        gbc_sampleComboBox.gridx = 1;
        gbc_sampleComboBox.gridy = 3;
        add(sampleComboBox, gbc_sampleComboBox);

        final JLabel lblShading = new JLabel("Shading");
        lblShading.setToolTipText("Enable shading for the volume");
        GridBagConstraints gbc_lblShading = new GridBagConstraints();
        gbc_lblShading.anchor = GridBagConstraints.WEST;
        gbc_lblShading.insets = new Insets(0, 0, 0, 5);
        gbc_lblShading.gridx = 0;
        gbc_lblShading.gridy = 4;
        add(lblShading, gbc_lblShading);

        shadingCheckBox = new JCheckBox("");
        GridBagConstraints gbc_shadingCheckBox = new GridBagConstraints();
        gbc_shadingCheckBox.anchor = GridBagConstraints.WEST;
        gbc_shadingCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_shadingCheckBox.gridx = 1;
        gbc_shadingCheckBox.gridy = 4;
        add(shadingCheckBox, gbc_shadingCheckBox);

        JLabel lblAmbientLight = new JLabel("Ambient / Diffuse  ");
        lblAmbientLight.setToolTipText("Ambient and diffuse lighting coefficient");
        GridBagConstraints gbc_lblAmbientLight = new GridBagConstraints();
        gbc_lblAmbientLight.anchor = GridBagConstraints.WEST;
        gbc_lblAmbientLight.insets = new Insets(0, 0, 0, 5);
        gbc_lblAmbientLight.gridx = 0;
        gbc_lblAmbientLight.gridy = 5;
        add(lblAmbientLight, gbc_lblAmbientLight);

        ambientField = new NumberTextField();
        ambientField.setToolTipText("Ambient lighting coefficient");
        GridBagConstraints gbc_ambientField = new GridBagConstraints();
        gbc_ambientField.insets = new Insets(0, 0, 0, 5);
        gbc_ambientField.fill = GridBagConstraints.HORIZONTAL;
        gbc_ambientField.gridx = 1;
        gbc_ambientField.gridy = 5;
        add(ambientField, gbc_ambientField);
        ambientField.setColumns(4);

        diffuseField = new NumberTextField();
        diffuseField.setToolTipText("Diffuse lighting coefficient");
        GridBagConstraints gbc_diffuseField = new GridBagConstraints();
        gbc_diffuseField.insets = new Insets(0, 0, 0, 5);
        gbc_diffuseField.fill = GridBagConstraints.HORIZONTAL;
        gbc_diffuseField.gridx = 2;
        gbc_diffuseField.gridy = 5;
        add(diffuseField, gbc_diffuseField);
        diffuseField.setColumns(4);

        final JLabel lblSpecularIntensity = new JLabel("Specular");
        lblSpecularIntensity.setToolTipText("Specular lighting coefficient and power");
        GridBagConstraints gbc_lblSpecularIntensity = new GridBagConstraints();
        gbc_lblSpecularIntensity.insets = new Insets(0, 0, 0, 5);
        gbc_lblSpecularIntensity.anchor = GridBagConstraints.WEST;
        gbc_lblSpecularIntensity.gridx = 0;
        gbc_lblSpecularIntensity.gridy = 6;
        add(lblSpecularIntensity, gbc_lblSpecularIntensity);

        specularIntensityField = new NumberTextField();
        specularIntensityField.setToolTipText("Specular lighting coefficient");
        GridBagConstraints gbc_specularIntensityField = new GridBagConstraints();
        gbc_specularIntensityField.anchor = GridBagConstraints.WEST;
        gbc_specularIntensityField.insets = new Insets(0, 0, 0, 5);
        gbc_specularIntensityField.gridx = 1;
        gbc_specularIntensityField.gridy = 6;
        add(specularIntensityField, gbc_specularIntensityField);
        specularIntensityField.setColumns(4);

        specularPowerField = new NumberTextField();
        specularPowerField.setToolTipText("Specular power");
        GridBagConstraints gbc_specularPowerField = new GridBagConstraints();
        gbc_specularPowerField.insets = new Insets(0, 0, 0, 5);
        gbc_specularPowerField.anchor = GridBagConstraints.WEST;
        gbc_specularPowerField.gridx = 2;
        gbc_specularPowerField.gridy = 6;
        add(specularPowerField, gbc_specularPowerField);
        specularPowerField.setColumns(4);
    }

    protected void updateState()
    {
        switch (getMapperType())
        {
            case RAYCAST_CPU_FIXEDPOINT:
            case RAYCAST_GPU:
            case RAYCAST_GPU_OPENGL:
                blendingComboBox.setEnabled(true);
                break;

            default:
                blendingComboBox.setSelectedIndex(0);
                blendingComboBox.setEnabled(false);
                break;
        }
    }

    public VtkVolumeMapperType getMapperType()
    {
        if (mapperComboBox.getSelectedIndex() == -1)
            return null;

        return (VtkVolumeMapperType) mapperComboBox.getSelectedItem();
    }

    public void setMapperType(VtkVolumeMapperType value)
    {
        mapperComboBox.setSelectedItem(value);
    }

    public int getInterpolation()
    {
        return interpolationComboBox.getSelectedIndex();
    }

    public void setInterpolation(int value)
    {
        interpolationComboBox.setSelectedIndex(value);
    }

    public VtkVolumeBlendType getBlending()
    {
        if (blendingComboBox.getSelectedIndex() == -1)
            return null;

        return (VtkVolumeBlendType) blendingComboBox.getSelectedItem();
    }

    public void setBlending(VtkVolumeBlendType value)
    {
        blendingComboBox.setSelectedItem(value);
    }

    public int getSample()
    {
        return sampleComboBox.getSelectedIndex();
    }

    public void setSample(int value)
    {
        sampleComboBox.setSelectedIndex(value);
    }

    public boolean isShadingEnable()
    {
        return shadingCheckBox.isSelected();
    }

    public void setShadingEnable(boolean value)
    {
        shadingCheckBox.setSelected(value);
    }

    public double getAmbient()
    {
        return ambientField.getNumericValue();
    }

    public void setAmbient(double value)
    {
        ambientField.setNumericValue(value);
    }

    public double getDiffuse()
    {
        return diffuseField.getNumericValue();
    }

    public void setDiffuse(double value)
    {
        diffuseField.setNumericValue(value);
    }

    public double getSpecularIntensity()
    {
        return specularIntensityField.getNumericValue();
    }

    public void setSpecularIntensity(double value)
    {
        specularIntensityField.setNumericValue(value);
    }

    public double getSpecularPower()
    {
        return specularPowerField.getNumericValue();
    }

    public void setSpecularPower(double value)
    {
        specularPowerField.setNumericValue(value);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();

        if (source == mapperComboBox)
        {
            firePropertyChange(PROPERTY_MAPPER, null, mapperComboBox.getSelectedItem());
            updateState();
        }
        else if (source == interpolationComboBox)
            firePropertyChange(PROPERTY_INTERPOLATION, -1, interpolationComboBox.getSelectedIndex());
        else if (source == blendingComboBox)
            firePropertyChange(PROPERTY_BLENDING, -1, blendingComboBox.getSelectedIndex());
        else if (source == sampleComboBox)
            firePropertyChange(PROPERTY_SAMPLE, -1, sampleComboBox.getSelectedIndex());
        else if (source == shadingCheckBox)
            firePropertyChange(PROPERTY_SHADING, !shadingCheckBox.isSelected(), shadingCheckBox.isSelected());
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (!validate)
            return;

        if (source == ambientField)
            firePropertyChange(PROPERTY_AMBIENT, 0d, ambientField.getNumericValue());
        else if (source == diffuseField)
            firePropertyChange(PROPERTY_DIFFUSE, 0d, diffuseField.getNumericValue());
        else if (source == specularIntensityField)
            firePropertyChange(PROPERTY_SPECULAR_INTENSITY, 0d, specularIntensityField.getNumericValue());
        else if (source == specularPowerField)
            firePropertyChange(PROPERTY_SPECULAR_POWER, 0d, specularPowerField.getNumericValue());
    }
}

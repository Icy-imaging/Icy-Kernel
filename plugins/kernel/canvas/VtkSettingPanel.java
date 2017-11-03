package plugins.kernel.canvas;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.NumberTextField;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyToggleButton;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.vtk.VtkImageVolume;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.EventListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
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

    protected static final Image ICON_GPU = ResourceUtil.getAlphaIconAsImage("gpu.png");
    protected static final Image ICON_SHADING = ResourceUtil.getColorIconAsImage("shading.png");

    public static final String PROPERTY_BG_COLOR = "renderBGColor";
    public static final String PROPERTY_MAPPER = "volumeMapper";
    public static final String PROPERTY_BLENDING = "volumeBlending";
    public static final String PROPERTY_SAMPLE = "volumeSample";
    public static final String PROPERTY_INTERPOLATION = "volumeInterpolation";
    public static final String PROPERTY_SHADING = "shading";
    public static final String PROPERTY_AMBIENT = "volumeAmbient";
    public static final String PROPERTY_DIFFUSE = "volumeDiffuse";
    public static final String PROPERTY_SPECULAR = "volumeSpecular";

    /**
     * GUI
     */
    private ColorChooserButton bgColorButton;
    private JCheckBox gpuMapperCheckBox;
    private JComboBox volumeBlendingComboBox;
    private JComboBox volumeSampleComboBox;
    private JComboBox volumeInterpolationComboBox;
    private IcyToggleButton shadingButton;
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
        gpuMapperCheckBox.addActionListener(this);
        volumeBlendingComboBox.addActionListener(this);
        volumeInterpolationComboBox.addActionListener(this);
        volumeSampleComboBox.addActionListener(this);
        shadingButton.addActionListener(this);

        volumeAmbientField.addTextChangeListener(this);
        volumeDiffuseField.addTextChangeListener(this);
        volumeSpecularField.addTextChangeListener(this);
    }

    protected void initialize()
    {
        setBorder(new EmptyBorder(2, 0, 2, 0));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblBackground = new JLabel("Background color ");
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

        gpuMapperCheckBox = new JCheckBox("New check box");
        gpuMapperCheckBox.setFocusable(false);
        gpuMapperCheckBox.setIconTextGap(8);
        gpuMapperCheckBox.setText("GPU rendering");
        gpuMapperCheckBox.setToolTipText("Enable GPU volume rendering");
        GridBagConstraints gbc_gpuMapperCheckBox = new GridBagConstraints();
        gbc_gpuMapperCheckBox.anchor = GridBagConstraints.EAST;
        gbc_gpuMapperCheckBox.gridwidth = 2;
        gbc_gpuMapperCheckBox.insets = new Insets(0, 0, 5, 0);
        gbc_gpuMapperCheckBox.gridx = 2;
        gbc_gpuMapperCheckBox.gridy = 0;
        add(gpuMapperCheckBox, gbc_gpuMapperCheckBox);

        final JLabel lblInterpolation = new JLabel("Interpolation  ");
        lblInterpolation.setToolTipText("Select volume rendering interpolation method");
        GridBagConstraints gbc_lblInterpolation = new GridBagConstraints();
        gbc_lblInterpolation.anchor = GridBagConstraints.WEST;
        gbc_lblInterpolation.insets = new Insets(0, 0, 5, 5);
        gbc_lblInterpolation.gridx = 0;
        gbc_lblInterpolation.gridy = 1;
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
        gbc_volumeInterpolationComboBox.gridy = 1;
        add(volumeInterpolationComboBox, gbc_volumeInterpolationComboBox);

        JLabel lblBlending = new JLabel("Blending");
        lblBlending.setToolTipText("Select volume rendering blending method");
        GridBagConstraints gbc_lblBlending = new GridBagConstraints();
        gbc_lblBlending.anchor = GridBagConstraints.WEST;
        gbc_lblBlending.insets = new Insets(0, 0, 5, 5);
        gbc_lblBlending.gridx = 0;
        gbc_lblBlending.gridy = 2;
        add(lblBlending, gbc_lblBlending);

        volumeBlendingComboBox = new JComboBox();
        volumeBlendingComboBox.setToolTipText("Select volume rendering blending method");
        volumeBlendingComboBox.setModel(new DefaultComboBoxModel(VtkVolumeBlendType.values()));
        GridBagConstraints gbc_volumeBlendingComboBox = new GridBagConstraints();
        gbc_volumeBlendingComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeBlendingComboBox.gridwidth = 3;
        gbc_volumeBlendingComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeBlendingComboBox.gridx = 1;
        gbc_volumeBlendingComboBox.gridy = 2;
        add(volumeBlendingComboBox, gbc_volumeBlendingComboBox);

        final JLabel lblSample = new JLabel("Sample");
        lblSample.setToolTipText("Set volume sample resolution (raycaster mapper only)");
        GridBagConstraints gbc_lblSample = new GridBagConstraints();
        gbc_lblSample.anchor = GridBagConstraints.WEST;
        gbc_lblSample.insets = new Insets(0, 0, 5, 5);
        gbc_lblSample.gridx = 0;
        gbc_lblSample.gridy = 3;
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
        gbc_volumeSampleComboBox.gridy = 3;
        add(volumeSampleComboBox, gbc_volumeSampleComboBox);

        shadingButton = new IcyToggleButton(new IcyIcon(ICON_SHADING, false));
        shadingButton.setIconTextGap(8);
        shadingButton.setText("Shading");
        shadingButton.setFocusable(false);
        shadingButton.setToolTipText("Enable volume shading");
        GridBagConstraints gbc_shadingBtn = new GridBagConstraints();
        gbc_shadingBtn.anchor = GridBagConstraints.WEST;
        gbc_shadingBtn.insets = new Insets(0, 0, 0, 5);
        gbc_shadingBtn.gridx = 0;
        gbc_shadingBtn.gridy = 4;
        add(shadingButton, gbc_shadingBtn);

        volumeAmbientField = new NumberTextField();
        volumeAmbientField.setToolTipText("Ambient lighting coefficient");
        GridBagConstraints gbc_volumeAmbientField = new GridBagConstraints();
        gbc_volumeAmbientField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeAmbientField.insets = new Insets(0, 0, 0, 5);
        gbc_volumeAmbientField.gridx = 1;
        gbc_volumeAmbientField.gridy = 4;
        add(volumeAmbientField, gbc_volumeAmbientField);
        volumeAmbientField.setColumns(3);

        volumeDiffuseField = new NumberTextField();
        volumeDiffuseField.setToolTipText("Diffuse lighting coefficient");
        GridBagConstraints gbc_volumeDiffuseField = new GridBagConstraints();
        gbc_volumeDiffuseField.insets = new Insets(0, 0, 0, 5);
        gbc_volumeDiffuseField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeDiffuseField.gridx = 2;
        gbc_volumeDiffuseField.gridy = 4;
        add(volumeDiffuseField, gbc_volumeDiffuseField);
        volumeDiffuseField.setColumns(3);

        volumeSpecularField = new NumberTextField();
        volumeSpecularField.setToolTipText("Specular lighting coefficient");
        GridBagConstraints gbc_volumeSpecularField = new GridBagConstraints();
        gbc_volumeSpecularField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeSpecularField.gridx = 3;
        gbc_volumeSpecularField.gridy = 4;
        add(volumeSpecularField, gbc_volumeSpecularField);
        volumeSpecularField.setColumns(3);
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

        // switch (getVolumeMapperType())
        // {
        // case RAYCAST_CPU_FIXEDPOINT:
        // case RAYCAST_GPU_OPENGL:
        // volumeSampleComboBox.setEnabled(true);
        // volumeBlendingComboBox.setEnabled(true);
        // break;
        // case TEXTURE2D_OPENGL:
        // case TEXTURE3D_OPENGL:
        // volumeSampleComboBox.setEnabled(false);
        // volumeBlendingComboBox.setEnabled(false);
        // break;
        // }
    }

    public Color getBackgroundColor()
    {
        return bgColorButton.getColor();
    }

    public void setBackgroundColor(Color value)
    {
        bgColorButton.setColor(value);
    }

    public boolean getGPURendering()
    {
        return gpuMapperCheckBox.isSelected();
    }

    public void setGPURendering(boolean value)
    {
        gpuMapperCheckBox.setSelected(value);
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

    /**
     * @see VtkImageVolume#getShade()
     */
    public boolean getVolumeShading()
    {
        return shadingButton.isSelected();
    }

    /**
     * @see VtkImageVolume#setShade(boolean)
     */
    public void setVolumeShading(boolean value)
    {
        if (shadingButton.isSelected() != value)
            shadingButton.doClick();
    }

    /**
     * Add a SettingChange listener
     */
    public void addSettingChangeListener(SettingChangeListener listener)
    {
        listenerList.add(SettingChangeListener.class, listener);
    }

    /**
     * Remove a SettingChange listener
     */
    public void removeSettingChangeListener(SettingChangeListener listener)
    {
        listenerList.remove(SettingChangeListener.class, listener);
    }

    public void fireSettingChange(Object source, String propertyName, Object oldValue, Object newValue)
    {
        if ((oldValue != null) && (newValue != null) && oldValue.equals(newValue))
            return;

        final PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
        final SettingChangeListener[] listeners = getListeners(SettingChangeListener.class);

        for (int i = 0; i < listeners.length; i++)
            listeners[i].settingChange(event);
    }

    @Override
    public void colorChanged(ColorChooserButton source)
    {
        if (source == bgColorButton)
            fireSettingChange(source, PROPERTY_BG_COLOR, null, source.getColor());
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();

        if (source == gpuMapperCheckBox)
            fireSettingChange(source, PROPERTY_MAPPER, Boolean.valueOf(!gpuMapperCheckBox.isSelected()),
                    Boolean.valueOf(gpuMapperCheckBox.isSelected()));
        else if (source == volumeBlendingComboBox)
            fireSettingChange(source, PROPERTY_BLENDING, null, volumeBlendingComboBox.getSelectedItem());
        else if (source == volumeSampleComboBox)
            fireSettingChange(source, PROPERTY_SAMPLE, Integer.valueOf(-1),
                    Integer.valueOf(volumeSampleComboBox.getSelectedIndex()));
        else if (source == volumeInterpolationComboBox)
            fireSettingChange(source, PROPERTY_INTERPOLATION, Integer.valueOf(-1),
                    Integer.valueOf(volumeInterpolationComboBox.getSelectedIndex()));
        else if (source == shadingButton)
            fireSettingChange(source, PROPERTY_SHADING, Integer.valueOf(-1),
                    Boolean.valueOf(shadingButton.isSelected()));

        updateState();
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (!validate)
            return;

        if (source == volumeAmbientField)
            fireSettingChange(source, PROPERTY_AMBIENT, Double.valueOf(-1d),
                    Double.valueOf(volumeAmbientField.getNumericValue()));
        else if (source == volumeDiffuseField)
            fireSettingChange(source, PROPERTY_DIFFUSE, Double.valueOf(-1d),
                    Double.valueOf(volumeDiffuseField.getNumericValue()));
        else if (source == volumeSpecularField)
            fireSettingChange(source, PROPERTY_SPECULAR, Double.valueOf(-1d),
                    Double.valueOf(volumeSpecularField.getNumericValue()));

        updateState();
    }

    public static interface SettingChangeListener extends EventListener
    {
        public void settingChange(PropertyChangeEvent evt);
    }
}

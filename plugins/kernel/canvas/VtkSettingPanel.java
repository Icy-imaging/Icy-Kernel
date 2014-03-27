package plugins.kernel.canvas;

import icy.util.StringUtil;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;
import icy.vtk.VtkImageVolume.VtkVolumeMapperType;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

public class VtkSettingPanel extends JPanel implements PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 6433369095311474470L;

    public static final String PROPERTY_BG_COLOR = VtkRenderSettingPanel.PROPERTY_BG_COLOR;
    public static final String PROPERTY_BOUNDINGBOX = VtkRenderSettingPanel.PROPERTY_BOUNDINGBOX;
    public static final String PROPERTY_BOUNDINGBOXGRID = VtkRenderSettingPanel.PROPERTY_BOUNDINGBOXGRID;
    public static final String PROPERTY_BOUNDINGBOXRULER = VtkRenderSettingPanel.PROPERTY_BOUNDINGBOXRULER;
    public static final String PROPERTY_AXIS = VtkRenderSettingPanel.PROPERTY_AXIS;

    public static final String PROPERTY_MAPPER = VtkVolumeSettingPanel.PROPERTY_MAPPER;
    public static final String PROPERTY_BLENDING = VtkVolumeSettingPanel.PROPERTY_BLENDING;
    public static final String PROPERTY_SAMPLE = VtkVolumeSettingPanel.PROPERTY_SAMPLE;
    public static final String PROPERTY_INTERPOLATION = VtkVolumeSettingPanel.PROPERTY_INTERPOLATION;
    public static final String PROPERTY_SHADING = VtkVolumeSettingPanel.PROPERTY_SHADING;
    public static final String PROPERTY_AMBIENT = VtkVolumeSettingPanel.PROPERTY_AMBIENT;
    public static final String PROPERTY_DIFFUSE = VtkVolumeSettingPanel.PROPERTY_DIFFUSE;
    public static final String PROPERTY_SPECULAR_INTENSITY = VtkVolumeSettingPanel.PROPERTY_SPECULAR_INTENSITY;
    public static final String PROPERTY_SPECULAR_POWER = VtkVolumeSettingPanel.PROPERTY_SPECULAR_POWER;

    private VtkRenderSettingPanel renderSetting;
    private VtkVolumeSettingPanel volumeSetting;

    /**
     * Create the panel.
     */
    public VtkSettingPanel()
    {
        super();

        initialize();

        renderSetting.addPropertyChangeListener(this);
        volumeSetting.addPropertyChangeListener(this);
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        renderSetting = new VtkRenderSettingPanel();
        GridBagConstraints gbc_renderSetting = new GridBagConstraints();
        gbc_renderSetting.anchor = GridBagConstraints.NORTH;
        gbc_renderSetting.fill = GridBagConstraints.HORIZONTAL;
        gbc_renderSetting.insets = new Insets(0, 0, 0, 0);
        gbc_renderSetting.gridx = 0;
        gbc_renderSetting.gridy = 0;
        add(renderSetting, gbc_renderSetting);

        volumeSetting = new VtkVolumeSettingPanel();
        GridBagConstraints gbc_volumeSetting = new GridBagConstraints();
        gbc_volumeSetting.anchor = GridBagConstraints.NORTH;
        gbc_volumeSetting.insets = new Insets(0, 0, 0, 0);
        gbc_volumeSetting.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeSetting.gridx = 0;
        gbc_volumeSetting.gridy = 1;
        add(volumeSetting, gbc_volumeSetting);
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#getBackgroundColor()
     */
    public Color getBackgroundColor()
    {
        return renderSetting.getBackgroundColor();
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#setBackgroundColor(java.awt.Color)
     */
    public void setBackgroundColor(Color value)
    {
        renderSetting.setBackgroundColor(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#isBoundingBoxVisible()
     */
    public boolean isBoundingBoxVisible()
    {
        return renderSetting.isBoundingBoxVisible();
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#setBoundingBoxVisible(boolean)
     */
    public void setBoundingBoxVisible(boolean value)
    {
        renderSetting.setBoundingBoxVisible(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#isBoundingBoxGridVisible()
     */
    public boolean isBoundingBoxGridVisible()
    {
        return renderSetting.isBoundingBoxGridVisible();
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#setBoundingBoxGridVisible(boolean)
     */
    public void setBoundingBoxGridVisible(boolean value)
    {
        renderSetting.setBoundingBoxGridVisible(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#isBoundingBoxRulerVisible()
     */
    public boolean isBoundingBoxRulesVisible()
    {
        return renderSetting.isBoundingBoxRulerVisible();
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#setBoundingBoxRulerVisible(boolean)
     */
    public void setBoundingBoxRulesVisible(boolean value)
    {
        renderSetting.setBoundingBoxRulerVisible(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#isAxisVisible()
     */
    public boolean isAxisVisible()
    {
        return renderSetting.isAxisVisible();
    }

    /**
     * @see plugins.kernel.canvas.VtkRenderSettingPanel#setAxisVisible(boolean)
     */
    public void setAxisVisible(boolean value)
    {
        renderSetting.setAxisVisible(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getBlending()
     */
    public VtkVolumeBlendType getVolumeBlendingMode()
    {
        return volumeSetting.getBlending();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setBlending(VtkVolumeBlendType)
     */
    public void setVolumeBlendingMode(VtkVolumeBlendType value)
    {
        volumeSetting.setBlending(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getSample()
     */
    public int getVolumeSample()
    {
        return volumeSetting.getSample();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setSample(int)
     */
    public void setVolumeSample(int value)
    {
        volumeSetting.setSample(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#isShadingEnable()
     */
    public boolean isVolumeShadingEnable()
    {
        return volumeSetting.isShadingEnable();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setShadingEnable(boolean)
     */
    public void setVolumeShadingEnable(boolean value)
    {
        volumeSetting.setShadingEnable(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getAmbient()
     */
    public double getVolumeAmbient()
    {
        return volumeSetting.getAmbient();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setAmbient(double)
     */
    public void setVolumeAmbient(double value)
    {
        volumeSetting.setAmbient(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getDiffuse()
     */
    public double getVolumeDiffuse()
    {
        return volumeSetting.getDiffuse();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setDiffuse(double)
     */
    public void setVolumeDiffuse(double value)
    {
        volumeSetting.setDiffuse(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getSpecularIntensity()
     */
    public double getVolumeSpecularIntensity()
    {
        return volumeSetting.getSpecularIntensity();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setSpecularIntensity(double)
     */
    public void setVolumeSpecularIntensity(double value)
    {
        volumeSetting.setSpecularIntensity(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getSpecularPower()
     */
    public double getVolumeSpecularPower()
    {
        return volumeSetting.getSpecularPower();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setSpecularPower(double)
     */
    public void setVolumeSpecularPower(double value)
    {
        volumeSetting.setSpecularPower(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getInterpolation()
     */
    public int getVolumeInterpolation()
    {
        return volumeSetting.getInterpolation();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setInterpolation(int)
     */
    public void setVolumeInterpolation(int value)
    {
        volumeSetting.setInterpolation(value);
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#getMapperType()
     */
    public VtkVolumeMapperType getVolumeMapperType()
    {
        return volumeSetting.getMapperType();
    }

    /**
     * @see plugins.kernel.canvas.VtkVolumeSettingPanel#setMapperType(icy.vtk.VtkImageVolume.VtkVolumeMapperType)
     */
    public void setVolumeMapperType(VtkVolumeMapperType value)
    {
        volumeSetting.setMapperType(value);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        final String propertyName = evt.getPropertyName();

        // forward property change event
        if (StringUtil.equals(propertyName, PROPERTY_AMBIENT) || StringUtil.equals(propertyName, PROPERTY_AXIS)
                || StringUtil.equals(propertyName, PROPERTY_BG_COLOR)
                || StringUtil.equals(propertyName, PROPERTY_BOUNDINGBOX)
                || StringUtil.equals(propertyName, PROPERTY_BOUNDINGBOXGRID)
                || StringUtil.equals(propertyName, PROPERTY_BOUNDINGBOXRULER)
                || StringUtil.equals(propertyName, PROPERTY_DIFFUSE)
                || StringUtil.equals(propertyName, PROPERTY_INTERPOLATION)
                || StringUtil.equals(propertyName, PROPERTY_MAPPER) || StringUtil.equals(propertyName, PROPERTY_SAMPLE)
                || StringUtil.equals(propertyName, PROPERTY_BLENDING)
                || StringUtil.equals(propertyName, PROPERTY_SHADING)
                || StringUtil.equals(propertyName, PROPERTY_SPECULAR_INTENSITY)
                || StringUtil.equals(propertyName, PROPERTY_SPECULAR_POWER))
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}

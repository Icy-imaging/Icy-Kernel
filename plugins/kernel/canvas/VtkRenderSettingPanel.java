package plugins.kernel.canvas;

import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class VtkRenderSettingPanel extends JPanel implements ColorChangeListener, ActionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2142296511143789244L;

    public static final String PROPERTY_BG_COLOR = "renderBGColor";
    public static final String PROPERTY_BOUNDINGBOX = "renderBoudingBox";
    public static final String PROPERTY_BOUNDINGBOXGRID = "renderBoudingBoxGrid";
    public static final String PROPERTY_BOUNDINGBOXRULER = "renderBoudingBoxRuler";
    public static final String PROPERTY_AXIS = "renderAxis";

    /**
     * GUI
     */
    private ColorChooserButton bgColorButton;
    private JCheckBox boundingBoxCheckBox;
    private JCheckBox boundingBoxRulerCheckBox;
    private JCheckBox axisCheckBox;
    private JCheckBox boundingBoxGridCheckBox;

    /**
     * Create the panel.
     */
    public VtkRenderSettingPanel()
    {
        super();

        initialize();

        updateState();

        bgColorButton.addColorChangeListener(this);
        boundingBoxCheckBox.addActionListener(this);
        boundingBoxGridCheckBox.addActionListener(this);
        boundingBoxRulerCheckBox.addActionListener(this);
        axisCheckBox.addActionListener(this);
    }

    private void initialize()
    {
        setBorder(null);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 60, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblBackground = new JLabel("Background color  ");
        lblBackground.setToolTipText("Change background color");
        GridBagConstraints gbc_lblBackground = new GridBagConstraints();
        gbc_lblBackground.anchor = GridBagConstraints.WEST;
        gbc_lblBackground.insets = new Insets(0, 0, 0, 5);
        gbc_lblBackground.gridx = 0;
        gbc_lblBackground.gridy = 0;
        add(lblBackground, gbc_lblBackground);

        bgColorButton = new ColorChooserButton();
        bgColorButton.setToolTipText("Change background color");
        GridBagConstraints gbc_bgColorButton = new GridBagConstraints();
        gbc_bgColorButton.insets = new Insets(0, 0, 0, 5);
        gbc_bgColorButton.gridx = 1;
        gbc_bgColorButton.gridy = 0;
        add(bgColorButton, gbc_bgColorButton);

        final JLabel lblSpecularIntensity = new JLabel("Bounding box");
        lblSpecularIntensity.setToolTipText("Enable / Disable bounding box display");
        GridBagConstraints gbc_lblSpecularIntensity = new GridBagConstraints();
        gbc_lblSpecularIntensity.anchor = GridBagConstraints.WEST;
        gbc_lblSpecularIntensity.insets = new Insets(0, 0, 0, 5);
        gbc_lblSpecularIntensity.gridx = 0;
        gbc_lblSpecularIntensity.gridy = 1;
        add(lblSpecularIntensity, gbc_lblSpecularIntensity);

        boundingBoxCheckBox = new JCheckBox("");
        boundingBoxCheckBox.setToolTipText("Enable / Disable bounding box display");
        GridBagConstraints gbc_boudingBoxCheckBox = new GridBagConstraints();
        gbc_boudingBoxCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_boudingBoxCheckBox.anchor = GridBagConstraints.WEST;
        gbc_boudingBoxCheckBox.gridx = 1;
        gbc_boudingBoxCheckBox.gridy = 1;
        add(boundingBoxCheckBox, gbc_boudingBoxCheckBox);

        boundingBoxGridCheckBox = new JCheckBox("Grid  ");
        boundingBoxGridCheckBox.setToolTipText("Enable / Disable bounding box grid display");
        boundingBoxGridCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        GridBagConstraints gbc_boundingBoxGridCheckBox = new GridBagConstraints();
        gbc_boundingBoxGridCheckBox.anchor = GridBagConstraints.WEST;
        gbc_boundingBoxGridCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_boundingBoxGridCheckBox.gridx = 2;
        gbc_boundingBoxGridCheckBox.gridy = 1;
        add(boundingBoxGridCheckBox, gbc_boundingBoxGridCheckBox);

        boundingBoxRulerCheckBox = new JCheckBox("Ruler  ");
        boundingBoxRulerCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        boundingBoxRulerCheckBox.setToolTipText("Enable / Disable bounding box ruler display");
        GridBagConstraints gbc_boundingBoxRulerCheckBox = new GridBagConstraints();
        gbc_boundingBoxRulerCheckBox.anchor = GridBagConstraints.WEST;
        gbc_boundingBoxRulerCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_boundingBoxRulerCheckBox.gridx = 3;
        gbc_boundingBoxRulerCheckBox.gridy = 1;
        add(boundingBoxRulerCheckBox, gbc_boundingBoxRulerCheckBox);

        final JLabel lblAxis = new JLabel("Axis");
        lblAxis.setToolTipText("Enable / Disable axis display");
        GridBagConstraints gbc_lblAxis = new GridBagConstraints();
        gbc_lblAxis.anchor = GridBagConstraints.WEST;
        gbc_lblAxis.insets = new Insets(0, 0, 0, 5);
        gbc_lblAxis.gridx = 0;
        gbc_lblAxis.gridy = 2;
        add(lblAxis, gbc_lblAxis);

        axisCheckBox = new JCheckBox("");
        axisCheckBox.setToolTipText("Enable / Disable axis display");
        GridBagConstraints gbc_axisCheckBox = new GridBagConstraints();
        gbc_axisCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_axisCheckBox.anchor = GridBagConstraints.WEST;
        gbc_axisCheckBox.gridx = 1;
        gbc_axisCheckBox.gridy = 2;
        add(axisCheckBox, gbc_axisCheckBox);
    }

    protected void updateState()
    {
        if (boundingBoxCheckBox.isSelected())
        {
            boundingBoxGridCheckBox.setEnabled(true);
            boundingBoxRulerCheckBox.setEnabled(true);
        }
        else
        {
            boundingBoxGridCheckBox.setEnabled(false);
            boundingBoxRulerCheckBox.setEnabled(false);
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

    public boolean isBoundingBoxVisible()
    {
        return boundingBoxCheckBox.isSelected();
    }

    public void setBoundingBoxVisible(boolean value)
    {
        boundingBoxCheckBox.setSelected(value);
    }

    public boolean isBoundingBoxGridVisible()
    {
        return boundingBoxGridCheckBox.isSelected();
    }

    public void setBoundingBoxGridVisible(boolean value)
    {
        boundingBoxGridCheckBox.setSelected(value);
    }

    public boolean isBoundingBoxRulerVisible()
    {
        return boundingBoxRulerCheckBox.isSelected();
    }

    public void setBoundingBoxRulerVisible(boolean value)
    {
        boundingBoxRulerCheckBox.setSelected(value);
    }

    public boolean isAxisVisible()
    {
        return axisCheckBox.isSelected();
    }

    public void setAxisVisible(boolean value)
    {
        axisCheckBox.setSelected(value);
    }

    @Override
    public void colorChanged(ColorChooserButton source)
    {
        firePropertyChange(PROPERTY_BG_COLOR, null, source.getColor());
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == boundingBoxCheckBox)
        {
            firePropertyChange(PROPERTY_BOUNDINGBOX, !boundingBoxCheckBox.isSelected(),
                    boundingBoxCheckBox.isSelected());
            updateState();
        }
        else if (e.getSource() == boundingBoxGridCheckBox)
        {
            firePropertyChange(PROPERTY_BOUNDINGBOXGRID, !boundingBoxGridCheckBox.isSelected(),
                    boundingBoxRulerCheckBox.isSelected());
            updateState();
        }
        else if (e.getSource() == boundingBoxRulerCheckBox)
        {
            firePropertyChange(PROPERTY_BOUNDINGBOXRULER, !boundingBoxRulerCheckBox.isSelected(),
                    boundingBoxRulerCheckBox.isSelected());
            updateState();
        }
        else if (e.getSource() == axisCheckBox)
            firePropertyChange(PROPERTY_AXIS, !axisCheckBox.isSelected(), axisCheckBox.isSelected());
    }
}

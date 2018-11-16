package icy.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.component.NumberTextField;
import icy.gui.component.NumberTextField.ValueChangeListener;
import icy.math.MathUtil;

public class SaverOptionPanel extends JPanel
{
    private JLabel overwriteMetaLabel;
    private JLabel fpsLabel;
    private JLabel multipleFilesLabel;
    private JCheckBox overwriteMetaCheckbox;
    private NumberTextField fpsField;
    private JCheckBox multipleFilesCheckbox;
    private NumberTextField msField;
    private JLabel msLabel;

    private boolean synching;

    public SaverOptionPanel()
    {
        super();

        initialize();

        fpsField.addValueListener(new ValueChangeListener()
        {
            public void valueChanged(double newValue, boolean validate)
            {
                if (validate)
                    syncTime(true);
            }
        });
        msField.addValueListener(new ValueChangeListener()
        {
            public void valueChanged(double newValue, boolean validate)
            {
                if (validate)
                    syncTime(false);
            }
        });

        synching = false;
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 50, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        multipleFilesLabel = new JLabel(" Save as multiple files");
        multipleFilesLabel.setToolTipText("Save each slice/frame in a separate file");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        add(multipleFilesLabel, gbc_lblNewLabel);

        multipleFilesCheckbox = new JCheckBox("");
        multipleFilesCheckbox.setToolTipText("Save each slice/frame in a separate file");
        GridBagConstraints gbc_multipleFilesCheckbox = new GridBagConstraints();
        gbc_multipleFilesCheckbox.anchor = GridBagConstraints.EAST;
        gbc_multipleFilesCheckbox.insets = new Insets(0, 0, 5, 0);
        gbc_multipleFilesCheckbox.gridx = 1;
        gbc_multipleFilesCheckbox.gridy = 0;
        add(multipleFilesCheckbox, gbc_multipleFilesCheckbox);

        fpsLabel = new JLabel(" Frame per second");
        fpsLabel.setToolTipText("Number of frame per second");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 1;
        add(fpsLabel, gbc_lblNewLabel_1);

        fpsField = new NumberTextField();
        fpsField.setInteger(true);
        fpsField.setHorizontalAlignment(SwingConstants.RIGHT);
        fpsField.setNumericValue(20.0);
        fpsField.setToolTipText("Number of frame per second");
        GridBagConstraints gbc_fpsField = new GridBagConstraints();
        gbc_fpsField.fill = GridBagConstraints.HORIZONTAL;
        gbc_fpsField.insets = new Insets(0, 0, 5, 0);
        gbc_fpsField.gridx = 1;
        gbc_fpsField.gridy = 1;
        add(fpsField, gbc_fpsField);

        msLabel = new JLabel(" Time interval (ms)");
        msLabel.setToolTipText("Time interval between 2 frames (ms)");
        GridBagConstraints gbc_msLabel = new GridBagConstraints();
        gbc_msLabel.anchor = GridBagConstraints.WEST;
        gbc_msLabel.insets = new Insets(0, 0, 5, 5);
        gbc_msLabel.gridx = 0;
        gbc_msLabel.gridy = 2;
        add(msLabel, gbc_msLabel);

        msField = new NumberTextField();
        msField.setHorizontalAlignment(SwingConstants.RIGHT);
        msField.setToolTipText("Time interval between 2 frames (ms)");
        GridBagConstraints gbc_msField = new GridBagConstraints();
        gbc_msField.fill = GridBagConstraints.HORIZONTAL;
        gbc_msField.insets = new Insets(0, 0, 5, 0);
        gbc_msField.gridx = 1;
        gbc_msField.gridy = 2;
        add(msField, gbc_msField);

        overwriteMetaLabel = new JLabel(" Overwrite metadata name");
        overwriteMetaLabel.setToolTipText("Overwrite metadata name with filename");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 3;
        add(overwriteMetaLabel, gbc_lblNewLabel_2);

        overwriteMetaCheckbox = new JCheckBox("");
        overwriteMetaCheckbox.setToolTipText("Overwrite metadata name with filename");
        GridBagConstraints gbc_overwriteMetaCheckbox = new GridBagConstraints();
        gbc_overwriteMetaCheckbox.anchor = GridBagConstraints.NORTHEAST;
        gbc_overwriteMetaCheckbox.gridx = 1;
        gbc_overwriteMetaCheckbox.gridy = 3;
        add(overwriteMetaCheckbox, gbc_overwriteMetaCheckbox);
    }

    void syncTime(boolean fromFPS)
    {
        if (synching)
            return;

        synching = true;
        try
        {
            if (fromFPS)
            {
                final double v = fpsField.getNumericValue();

                if (v > 0d)
                    msField.setNumericValue(MathUtil.roundSignificant(1000d / v, 5, true));
            }
            else
            {
                final double v = msField.getNumericValue();
                if (v > 0d)
                    fpsField.setNumericValue(MathUtil.roundSignificant(1000d / v, 5, true));
            }
        }
        finally
        {
            synching = false;
        }
    }

    public boolean isFramePerSecondVisible()
    {
        return fpsField.isVisible();
    }

    public void setFramePerSecondVisible(boolean value)
    {
        fpsLabel.setVisible(value);
        fpsField.setVisible(value);
        msLabel.setVisible(value);
        msField.setVisible(value);
    }

    public boolean isMultipleFilesVisible()
    {
        return multipleFilesCheckbox.isVisible();
    }

    public void setMultipleFilesVisible(boolean value)
    {
        multipleFilesLabel.setVisible(value);
        multipleFilesCheckbox.setVisible(value);
    }

    public void setForcedMultipleFilesOn()
    {
        multipleFilesCheckbox.setEnabled(false);
        multipleFilesCheckbox.setSelected(true);
    }

    public void setForcedMultipleFilesOff()
    {
        multipleFilesCheckbox.setEnabled(false);
        multipleFilesCheckbox.setSelected(false);
    }

    public void removeForcedMultipleFiles()
    {
        multipleFilesCheckbox.setEnabled(true);
        multipleFilesCheckbox.setSelected(false);
    }

    public boolean isOverwriteMetadataVisible()
    {
        return overwriteMetaCheckbox.isVisible();
    }

    public void setOverwriteMetadataVisible(boolean value)
    {
        overwriteMetaLabel.setVisible(value);
        overwriteMetaCheckbox.setVisible(value);
    }

    public int getFramePerSecond()
    {
        return (int) fpsField.getNumericValue();
    }

    public void setFramePerSecond(int value)
    {
        fpsField.setNumericValue(value);
    }

    /**
     * Return Time Interval in ms
     */
    public double getTimeInterval()
    {
        return msField.getNumericValue();
    }

    /**
     * Set Time Interval in ms
     */
    public void setTimeInterval(double value)
    {
        msField.setNumericValue(value);
    }

    public boolean getMultipleFiles()
    {
        return multipleFilesCheckbox.isSelected();
    }

    public void setMultipleFiles(boolean value)
    {
        multipleFilesCheckbox.setSelected(value);
    }

    public boolean getOverwriteMetadata()
    {
        return overwriteMetaCheckbox.isSelected();
    }

    public void setOverwriteMetadata(boolean value)
    {
        overwriteMetaCheckbox.setSelected(value);
    }
}

package icy.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SaverOptionPanel extends JPanel
{
    private JLabel overwriteMetaLabel;
    private JLabel fpsLabel;
    private JLabel multipleFilesLabel;
    private JCheckBox overwriteMetaCheckbox;
    private JSpinner fpsSpinner;
    private JCheckBox multipleFilesCheckbox;

    public SaverOptionPanel()
    {
        super();

        initialize();
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
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

        fpsSpinner = new JSpinner();
        fpsSpinner.setModel(new SpinnerNumberModel(15, 1, 120, 1));
        fpsSpinner.setToolTipText("Number of frame per second");
        GridBagConstraints gbc_fpsSpinner = new GridBagConstraints();
        gbc_fpsSpinner.anchor = GridBagConstraints.EAST;
        gbc_fpsSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_fpsSpinner.gridx = 1;
        gbc_fpsSpinner.gridy = 1;
        add(fpsSpinner, gbc_fpsSpinner);

        overwriteMetaLabel = new JLabel(" Overwrite metadata name");
        overwriteMetaLabel.setToolTipText("Overwrite metadata name with filename");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 2;
        add(overwriteMetaLabel, gbc_lblNewLabel_2);

        overwriteMetaCheckbox = new JCheckBox("");
        overwriteMetaCheckbox.setToolTipText("Overwrite metadata name with filename");
        GridBagConstraints gbc_overwriteMetaCheckbox = new GridBagConstraints();
        gbc_overwriteMetaCheckbox.anchor = GridBagConstraints.NORTHEAST;
        gbc_overwriteMetaCheckbox.gridx = 1;
        gbc_overwriteMetaCheckbox.gridy = 2;
        add(overwriteMetaCheckbox, gbc_overwriteMetaCheckbox);
    }

    public boolean isFramePerSecondVisible()
    {
        return fpsSpinner.isVisible();
    }

    public void setFramePerSecondVisible(boolean value)
    {
        fpsLabel.setVisible(value);
        fpsSpinner.setVisible(value);
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
        return ((Integer) fpsSpinner.getValue()).intValue();
    }

    public void setFramePerSecond(int value)
    {
        fpsSpinner.setValue(Integer.valueOf(value));
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

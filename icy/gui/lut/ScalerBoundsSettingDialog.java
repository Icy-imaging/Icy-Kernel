package icy.gui.lut;

import icy.gui.component.NumberTextField;
import icy.gui.dialog.ActionDialog;
import icy.gui.dialog.MessageDialog;
import icy.image.lut.LUT.LUTChannel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

public class ScalerBoundsSettingDialog extends ActionDialog
{
    final LUTChannel lutChannel;

    NumberTextField rangeMinField;
    NumberTextField rangeMaxField;
    NumberTextField lowBoundField;
    NumberTextField highBoundField;
    private JButton defaultBtn;

    public ScalerBoundsSettingDialog(LUTChannel lutChannel)
    {
        super("Histogram bounds");

        this.lutChannel = lutChannel;

        initialize();

        rangeMinField.setNumericValue(lutChannel.getMinBound());
        rangeMaxField.setNumericValue(lutChannel.getMaxBound());
        lowBoundField.setNumericValue(lutChannel.getMin());
        highBoundField.setNumericValue(lutChannel.getMax());

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final double rangeMin = rangeMinField.getNumericValue();
                final double rangeMax = rangeMaxField.getNumericValue();
                final double lowBound = lowBoundField.getNumericValue();
                final double highBound = highBoundField.getNumericValue();

                if (rangeMin >= rangeMax)
                {
                    MessageDialog.showDialog("Invalids settings",
                            "The maximum range value should be above the minimum range value",
                            MessageDialog.ERROR_MESSAGE);
                    return;
                }
                if (lowBound >= highBound)
                {
                    MessageDialog.showDialog("Invalids settings",
                            "The high bounds value should be above the low bounds value", MessageDialog.ERROR_MESSAGE);
                    return;
                }

                if ((lowBound < rangeMin) || (highBound > rangeMax))
                {
                    MessageDialog.showDialog("Invalids settings",
                            "The bounds values should be inside the defined range", MessageDialog.ERROR_MESSAGE);
                    return;
                }

                final LUTChannel lc = ScalerBoundsSettingDialog.this.lutChannel;

                lc.setMinBound(rangeMin);
                lc.setMaxBound(rangeMax);
                lc.setMin(lowBound);
                lc.setMax(highBound);

                dispose();
            }
        });

        setCloseAfterAction(false);
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {60, 0, 0, 10, 0};
        gridBagLayout.rowHeights = new int[] {23, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        mainPanel.setLayout(gridBagLayout);

        final JLabel lblRangeMin = new JLabel("Range");
        GridBagConstraints gbc_lblRangeMin = new GridBagConstraints();
        gbc_lblRangeMin.anchor = GridBagConstraints.EAST;
        gbc_lblRangeMin.insets = new Insets(0, 0, 5, 5);
        gbc_lblRangeMin.gridx = 0;
        gbc_lblRangeMin.gridy = 0;
        mainPanel.add(lblRangeMin, gbc_lblRangeMin);

        rangeMinField = new NumberTextField();
        GridBagConstraints gbc_rangeMinField = new GridBagConstraints();
        gbc_rangeMinField.insets = new Insets(0, 0, 5, 5);
        gbc_rangeMinField.fill = GridBagConstraints.HORIZONTAL;
        gbc_rangeMinField.gridx = 1;
        gbc_rangeMinField.gridy = 0;
        mainPanel.add(rangeMinField, gbc_rangeMinField);
        rangeMinField.setColumns(10);

        rangeMaxField = new NumberTextField();
        GridBagConstraints gbc_rangeMaxField = new GridBagConstraints();
        gbc_rangeMaxField.insets = new Insets(0, 0, 5, 5);
        gbc_rangeMaxField.fill = GridBagConstraints.HORIZONTAL;
        gbc_rangeMaxField.gridx = 2;
        gbc_rangeMaxField.gridy = 0;
        mainPanel.add(rangeMaxField, gbc_rangeMaxField);
        rangeMaxField.setColumns(10);

        final JLabel lblBoundMin = new JLabel("Bounds");
        GridBagConstraints gbc_lblBoundMin = new GridBagConstraints();
        gbc_lblBoundMin.anchor = GridBagConstraints.EAST;
        gbc_lblBoundMin.insets = new Insets(0, 0, 0, 5);
        gbc_lblBoundMin.gridx = 0;
        gbc_lblBoundMin.gridy = 1;
        mainPanel.add(lblBoundMin, gbc_lblBoundMin);

        lowBoundField = new NumberTextField();
        GridBagConstraints gbc_lowBoundField = new GridBagConstraints();
        gbc_lowBoundField.insets = new Insets(0, 0, 0, 5);
        gbc_lowBoundField.fill = GridBagConstraints.HORIZONTAL;
        gbc_lowBoundField.gridx = 1;
        gbc_lowBoundField.gridy = 1;
        mainPanel.add(lowBoundField, gbc_lowBoundField);
        lowBoundField.setColumns(10);

        highBoundField = new NumberTextField();
        GridBagConstraints gbc_highBoundField = new GridBagConstraints();
        gbc_highBoundField.insets = new Insets(0, 0, 0, 5);
        gbc_highBoundField.fill = GridBagConstraints.HORIZONTAL;
        gbc_highBoundField.gridx = 2;
        gbc_highBoundField.gridy = 1;
        mainPanel.add(highBoundField, gbc_highBoundField);
        highBoundField.setColumns(10);

        defaultBtn = new JButton("Default");
        GridBagConstraints gbc_defaultBtn = new GridBagConstraints();
        gbc_defaultBtn.anchor = GridBagConstraints.EAST;
        gbc_defaultBtn.insets = new Insets(0, 0, 0, 5);
        gbc_defaultBtn.gridx = 1;
        gbc_defaultBtn.gridy = 0;
        buttonPanel.add(defaultBtn, gbc_defaultBtn);
    }

}

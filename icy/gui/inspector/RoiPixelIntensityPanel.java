package icy.gui.inspector;

import icy.math.MathUtil;
import icy.roi.ROIUtil.ROIInfos;
import icy.util.StringUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;

public class RoiPixelIntensityPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -6005604991499221540L;

    // GUI
    private JLabel intensityMinLabel;
    private JLabel intensityMeanLabel;
    private JLabel intensityMaxLabel;
    private JLabel lblChannel;
    private JComboBox comboBox;
    private JButton btnCompute;

    /**
     * Create the panel.
     */
    public RoiPixelIntensityPanel()
    {
        super();

        initialize();
    }

    private void initialize()
    {
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
        gbl_panel_1.rowHeights = new int[] {0, 0, 0};
        gbl_panel_1.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panel_1.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        setLayout(gbl_panel_1);
        
        lblChannel = new JLabel("Channel");
        GridBagConstraints gbc_lblChannel = new GridBagConstraints();
        gbc_lblChannel.anchor = GridBagConstraints.WEST;
        gbc_lblChannel.gridwidth = 2;
        gbc_lblChannel.insets = new Insets(0, 0, 5, 5);
        gbc_lblChannel.gridx = 0;
        gbc_lblChannel.gridy = 0;
        add(lblChannel, gbc_lblChannel);
        
        comboBox = new JComboBox();
        comboBox.setModel(new DefaultComboBoxModel(new String[] {"0"}));
        comboBox.setToolTipText("Channel to compute");
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.gridwidth = 2;
        gbc_comboBox.insets = new Insets(0, 0, 5, 5);
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.gridx = 2;
        gbc_comboBox.gridy = 0;
        add(comboBox, gbc_comboBox);
        
        btnCompute = new JButton("Compute");
        btnCompute.setToolTipText("Compute min, mean and max intensity for selected channel");
        GridBagConstraints gbc_btnCompute = new GridBagConstraints();
        gbc_btnCompute.anchor = GridBagConstraints.EAST;
        gbc_btnCompute.gridwidth = 2;
        gbc_btnCompute.insets = new Insets(0, 0, 5, 5);
        gbc_btnCompute.gridx = 4;
        gbc_btnCompute.gridy = 0;
        add(btnCompute, gbc_btnCompute);

        JLabel label_2 = new JLabel("min");
        GridBagConstraints gbc_label_2 = new GridBagConstraints();
        gbc_label_2.insets = new Insets(0, 0, 0, 5);
        gbc_label_2.gridx = 0;
        gbc_label_2.gridy = 1;
        add(label_2, gbc_label_2);
        intensityMinLabel = new JLabel();
        GridBagConstraints gbc_intensityMinLabel = new GridBagConstraints();
        gbc_intensityMinLabel.anchor = GridBagConstraints.WEST;
        gbc_intensityMinLabel.insets = new Insets(0, 0, 0, 5);
        gbc_intensityMinLabel.gridx = 1;
        gbc_intensityMinLabel.gridy = 1;
        add(intensityMinLabel, gbc_intensityMinLabel);
        intensityMinLabel.setText("000");
        JLabel label = new JLabel("mean");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 0, 5);
        gbc_label.gridx = 2;
        gbc_label.gridy = 1;
        add(label, gbc_label);
        intensityMeanLabel = new JLabel();
        GridBagConstraints gbc_intensityMeanLabel = new GridBagConstraints();
        gbc_intensityMeanLabel.anchor = GridBagConstraints.WEST;
        gbc_intensityMeanLabel.insets = new Insets(0, 0, 0, 5);
        gbc_intensityMeanLabel.gridx = 3;
        gbc_intensityMeanLabel.gridy = 1;
        add(intensityMeanLabel, gbc_intensityMeanLabel);
        intensityMeanLabel.setText("000");
        JLabel label_1 = new JLabel("max");
        GridBagConstraints gbc_label_1 = new GridBagConstraints();
        gbc_label_1.insets = new Insets(0, 0, 0, 5);
        gbc_label_1.gridx = 4;
        gbc_label_1.gridy = 1;
        add(label_1, gbc_label_1);
        intensityMaxLabel = new JLabel();
        GridBagConstraints gbc_intensityMaxLabel = new GridBagConstraints();
        gbc_intensityMaxLabel.anchor = GridBagConstraints.WEST;
        gbc_intensityMaxLabel.gridx = 5;
        gbc_intensityMaxLabel.gridy = 1;
        add(intensityMaxLabel, gbc_intensityMaxLabel);
        intensityMaxLabel.setText("000");
    }
    
    void refreshInfos(ROIInfos infos)
    {
        if (infos != null)
        {
            intensityMinLabel.setText(StringUtil.toString(MathUtil.roundSignificant(infos.minIntensity, 5)));
            intensityMinLabel.setToolTipText(StringUtil.toString(infos.minIntensity));
            intensityMeanLabel.setText(StringUtil.toString(MathUtil.roundSignificant(infos.meanIntensity, 5)));
            intensityMeanLabel.setToolTipText(StringUtil.toString(infos.meanIntensity));
            intensityMaxLabel.setText(StringUtil.toString(MathUtil.roundSignificant(infos.maxIntensity, 5)));
            intensityMaxLabel.setToolTipText(StringUtil.toString(infos.maxIntensity));
        }
        else
        {
            intensityMinLabel.setText("");
            intensityMinLabel.setToolTipText("");
            intensityMeanLabel.setText("");
            intensityMeanLabel.setToolTipText("");
            intensityMaxLabel.setText("");
            intensityMaxLabel.setToolTipText("");
        }
    }


}

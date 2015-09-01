/**
 * 
 */
package icy.gui.sequence.tools;

import icy.image.IcyBufferedImageUtil.FilterType;
import icy.sequence.Sequence;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author Stephane
 */
public class SequenceResizePanel extends SequenceBaseResizePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 5366610917009978874L;

    private JComboBox filterComboBox;
    private JLabel lblFilterType;

    public SequenceResizePanel(Sequence sequence)
    {
        super(sequence);

        keepRatioCheckBox.setSelected(true);

        filterComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updatePreview();
            }
        });
    }

    @Override
    protected void initialize()
    {
        super.initialize();

        lblFilterType = new JLabel("Filter type");
        GridBagConstraints gbc_lblFilterType = new GridBagConstraints();
        gbc_lblFilterType.fill = GridBagConstraints.BOTH;
        gbc_lblFilterType.insets = new Insets(0, 0, 5, 5);
        gbc_lblFilterType.gridx = 5;
        gbc_lblFilterType.gridy = 0;
        settingPanel.add(lblFilterType, gbc_lblFilterType);

        filterComboBox = new JComboBox();
        filterComboBox.setModel(new DefaultComboBoxModel(new String[] {"Nearest", "Bilinear", "Bicubic"}));
        filterComboBox.setSelectedIndex(1);
        GridBagConstraints gbc_filterComboBox = new GridBagConstraints();
        gbc_filterComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_filterComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterComboBox.gridx = 5;
        gbc_filterComboBox.gridy = 1;
        settingPanel.add(filterComboBox, gbc_filterComboBox);
    }

    @Override
    public FilterType getFilterType()
    {
        switch (filterComboBox.getSelectedIndex())
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

    @Override
    public boolean getResizeContent()
    {
        return true;
    }

    @Override
    public int getXAlign()
    {
        return SwingConstants.CENTER;
    }

    @Override
    public int getYAlign()
    {
        return SwingConstants.CENTER;
    }
}

package icy.gui.sequence.tools;

import icy.image.IcyBufferedImageUtil.FilterType;
import icy.sequence.Sequence;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

public class SequenceCanvasResizePanel extends SequenceBaseResizePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1607237098458182628L;
    
    private JLabel lblNewLabel_1;
    private PositionAlignmentPanel positionAlignmentPanel;

    public SequenceCanvasResizePanel(Sequence sequence)
    {
        super(sequence);

        keepRatioCheckBox.setSelected(false);

        positionAlignmentPanel.addActionListener(new ActionListener()
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

        lblNewLabel_1 = new JLabel("Content alignment");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
        gbc_lblNewLabel_1.gridx = 5;
        gbc_lblNewLabel_1.gridy = 0;
        settingPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        positionAlignmentPanel = new PositionAlignmentPanel();
        GridBagConstraints gbc_positionAlignmentPanel = new GridBagConstraints();
        gbc_positionAlignmentPanel.gridheight = 4;
        gbc_positionAlignmentPanel.insets = new Insets(0, 0, 5, 5);
        gbc_positionAlignmentPanel.fill = GridBagConstraints.BOTH;
        gbc_positionAlignmentPanel.gridx = 5;
        gbc_positionAlignmentPanel.gridy = 1;
        settingPanel.add(positionAlignmentPanel, gbc_positionAlignmentPanel);
    }

    @Override
    public FilterType getFilterType()
    {
        return null;
    }

    @Override
    public boolean getResizeContent()
    {
        return false;
    }

    @Override
    public int getXAlign()
    {
        return positionAlignmentPanel.getXAlign();
    }

    @Override
    public int getYAlign()
    {
        return positionAlignmentPanel.getYAlign();
    }
}

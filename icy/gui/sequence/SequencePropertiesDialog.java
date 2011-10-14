/**
 * 
 */
package icy.gui.sequence;

import icy.gui.dialog.ActionDialog;
import icy.main.Icy;
import icy.sequence.Sequence;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stephane
 */
public class SequencePropertiesDialog extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 5696186054980120411L;

    final SequencePropertiesPanel panel;

    public SequencePropertiesDialog(final Sequence sequence)
    {
        super("Sequence Properties");

        panel = new SequencePropertiesPanel();
        panel.setSequence(sequence);

        mainPanel.add(panel);
        mainPanel.validate();

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sequence.setName(panel.getNameFieldValue());
                sequence.setPixelSizeX(panel.getPixelSizeXFieldValue());
                sequence.setPixelSizeY(panel.getPixelSizeYFieldValue());
                sequence.setPixelSizeZ(panel.getPixelSizeZFieldValue());
                sequence.setTimeInterval(panel.getTimeIntervalFieldValue());
                for (int c = 0; c < sequence.getSizeC(); c++)
                    sequence.setChannelName(c, panel.getChannelNameFieldValue(c));
            }
        });

        setLocationRelativeTo(Icy.getMainInterface().getFrame());
        pack();
        setVisible(true);
    }
}

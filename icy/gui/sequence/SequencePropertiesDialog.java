/**
 * 
 */
package icy.gui.sequence;

import icy.gui.dialog.ActionDialog;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.sequence.Sequence;

import java.awt.BorderLayout;
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

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.validate();

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sequence.setName(panel.getNameFieldValue());
                sequence.setPixelSizeX(UnitUtil.getValueInUnit(panel.getPixelSizeXFieldValue(),
                        panel.getPixelSizeXUnit(), UnitPrefix.MICRO));
                sequence.setPixelSizeY(UnitUtil.getValueInUnit(panel.getPixelSizeYFieldValue(),
                        panel.getPixelSizeYUnit(), UnitPrefix.MICRO));
                sequence.setPixelSizeZ(UnitUtil.getValueInUnit(panel.getPixelSizeZFieldValue(),
                        panel.getPixelSizeZUnit(), UnitPrefix.MICRO));

                double valueInSec = panel.getTimeIntervalFieldValue();

                switch (panel.getTimeIntervalUnit())
                {
                    case 0:
                        valueInSec *= 60d;
                    case 1:
                        valueInSec *= 60d;
                        break;
                    case 3:
                        valueInSec /= 1000d;
                        break;
                }

                sequence.setTimeInterval(valueInSec);

                for (int c = 0; c < sequence.getSizeC(); c++)
                    sequence.setChannelName(c, panel.getChannelNameFieldValue(c));
            }
        });

        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        pack();

        setVisible(true);
    }
}

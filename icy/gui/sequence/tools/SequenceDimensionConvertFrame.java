/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.dialog.ActionDialog;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.ComponentUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

/**
 * Advanced conversion of Z and T dimension.
 * 
 * @author Stephane
 */
public class SequenceDimensionConvertFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -6541431462734831647L;

    final SequenceDimensionConvertPanel convertPanel;

    public SequenceDimensionConvertFrame(Sequence sequence)
    {
        super("Z / T dimension conversion");

        convertPanel = new SequenceDimensionConvertPanel(sequence);
        convertPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(convertPanel, BorderLayout.CENTER);
        validate();

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Converting Z / T dimension...");

                        SequenceUtil.adjustZT(convertPanel.getSequence(), convertPanel.getNewSizeZ(),
                                convertPanel.getNewSizeT(), convertPanel.isOrderReversed());

                        pf.close();
                    }
                });
            }
        });

        setSize(340, 400);
        ComponentUtil.center(this);

        setVisible(true);
    }
}
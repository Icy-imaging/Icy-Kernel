/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
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
public class SequenceDimensionConvertFrame extends ActionFrame
{
    final SequenceDimensionConvertPanel convertPanel;

    public SequenceDimensionConvertFrame(Sequence sequence)
    {
        super("Z / T dimension conversion", true);

        setTitleVisible(false);

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

        setSizeExternal(340, 400);
        setSizeInternal(340, 400);
        setVisible(true);
        addToMainDesktopPane();
        center();
        requestFocus();
    }
}
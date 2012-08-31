/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stephane
 */
public class SequenceResizeFrame extends ActionFrame
{
    final SequenceResizePanel resizePanel;

    public SequenceResizeFrame(Sequence sequence)
    {
        super("Resize sequence " + sequence.getName());

        // GUI
        setTitleVisible(false);

        resizePanel = new SequenceResizePanel(sequence);
        getMainPanel().add(resizePanel, BorderLayout.CENTER);
        validate();

        // action
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Resizing sequence...");
                        try
                        {
                            final Sequence seqIn = resizePanel.getSequence();
                            final double pixelSize = resizePanel.getResolution();
                            
                            // apply chosen pixel size
                            seqIn.setPixelSizeX(pixelSize);
                            seqIn.setPixelSizeY(pixelSize);
                            
                            Icy.getMainInterface().addSequence(
                                    SequenceUtil.scale(resizePanel.getSequence(), resizePanel.getNewWidth(),
                                            resizePanel.getNewHeight(), resizePanel.getResizeContent(),
                                            resizePanel.getXAlign(), resizePanel.getYAlign(),
                                            resizePanel.getFilterType()));
                        }
                        finally
                        {
                            pf.close();
                        }
                    }
                });
            }
        });

        setSizeExternal(362, 520);
        setSizeInternal(362, 520);
        addToMainDesktopPane();
        center();
        setVisible(true);
    }
}

/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

/**
 * @author Stephane
 */
public class SequenceDimensionAdjustFrame extends ActionFrame implements SequenceModel
{
    final Sequence sequence;
    final SequenceDimensionAdjustPanel rangePanel;
    final DimensionId dim;

    public SequenceDimensionAdjustFrame(Sequence sequence, DimensionId dim)
    {
        super("Adjust " + dim.toString() + " dimension", true);

        this.sequence = sequence;
        this.dim = dim;

        setTitleVisible(false);

        rangePanel = new SequenceDimensionAdjustPanel(dim);
        rangePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(rangePanel, BorderLayout.CENTER);
        validate();

        rangePanel.setModel(this);

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
                        final DimensionId dim = SequenceDimensionAdjustFrame.this.dim;
                        final ProgressFrame pf;

                        if (dim == DimensionId.Z)
                            pf = new ProgressFrame("Removing slices...");
                        else
                            pf = new ProgressFrame("Removing frames...");

                        final Sequence seq = SequenceDimensionAdjustFrame.this.sequence;

                        seq.beginUpdate();
                        try
                        {
                            int i;

                            if (dim == DimensionId.Z)
                            {
                                i = seq.getSizeZ() - 1;

                                for (; i > rangePanel.getRangeHigh(); i--)
                                    SequenceUtil.removeZAndShift(seq, i);
                                for (; i >= rangePanel.getRangeLow(); i--)
                                    if (!rangePanel.isIndexSelected(i))
                                        SequenceUtil.removeZAndShift(seq, i);
                                for (; i >= 0; i--)
                                    SequenceUtil.removeZAndShift(seq, i);
                            }
                            else
                            {
                                i = seq.getSizeT() - 1;

                                for (; i > rangePanel.getRangeHigh(); i--)
                                    SequenceUtil.removeTAndShift(seq, i);
                                for (; i >= rangePanel.getRangeLow(); i--)
                                    if (!rangePanel.isIndexSelected(i))
                                        SequenceUtil.removeTAndShift(seq, i);
                                for (; i >= 0; i--)
                                    SequenceUtil.removeTAndShift(seq, i);
                            }
                        }
                        finally
                        {
                            seq.endUpdate();
                            pf.close();
                        }
                    }
                });
            }
        });

        setSizeExternal(320, 360);
        setSizeInternal(320, 360);
        setVisible(true);
        addToMainDesktopPane();
        center();
        requestFocus();
    }

    /**
     * @wbp.parser.constructor
     */
    SequenceDimensionAdjustFrame()
    {
        this(new Sequence(), DimensionId.Z);
    }

    @Override
    public int getSizeX()
    {
        if (sequence != null)
            return sequence.getSizeX();

        return 0;
    }

    @Override
    public int getSizeY()
    {
        if (sequence != null)
            return sequence.getSizeY();

        return 0;
    }

    @Override
    public int getSizeZ()
    {
        if (sequence != null)
            return sequence.getSizeZ();

        return 0;
    }

    @Override
    public int getSizeT()
    {
        if (sequence != null)
            return sequence.getSizeT();

        return 0;
    }

    @Override
    public int getSizeC()
    {
        if (sequence != null)
            return sequence.getSizeC();

        return 0;
    }

    @Override
    public Image getImage(int t, int z)
    {
        if (sequence != null)
            return sequence.getImage(t, z);

        return null;
    }

    @Override
    public Image getImage(int t, int z, int c)
    {
        if (sequence != null)
            return sequence.getImage(t, z, c);

        return null;
    }
}

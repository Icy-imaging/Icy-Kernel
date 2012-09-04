/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;
import icy.sequence.SequenceUtil;
import icy.sequence.SequenceUtil.MergeCHelper;
import icy.sequence.SequenceUtil.MergeTHelper;
import icy.sequence.SequenceUtil.MergeZHelper;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

/**
 * @author Stephane
 */
public class SequenceDimensionMergeFrame extends ActionFrame implements SequenceModel
{
    final SequenceDimensionMergePanel mergePanel;

    public SequenceDimensionMergeFrame(DimensionId dim)
    {
        super(dim.toString() + " Dimension merge", true);

        setTitleVisible(false);

        mergePanel = new SequenceDimensionMergePanel(dim);
        mergePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(mergePanel, BorderLayout.CENTER);
        validate();

        mergePanel.setModel(this);

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
                        final ProgressFrame pf = new ProgressFrame("Merging sequences...");

                        final Sequence out;

                        switch (getDimensionId())
                        {
                            default:
                            case C:
                                out = SequenceUtil.concatC(mergePanel.getSequences(), mergePanel.getSelectedChannels(),
                                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled(), pf);
                                break;

                            case Z:
                                out = SequenceUtil.concatZ(mergePanel.getSequences(), mergePanel.isInterlaceEnabled(),
                                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled(), pf);
                                break;

                            case T:
                                out = SequenceUtil.concatT(mergePanel.getSequences(), mergePanel.isInterlaceEnabled(),
                                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled(), pf);
                                break;
                        }

                        Icy.getMainInterface().addSequence(out);

                        pf.close();
                    }
                });
            }
        });

        setSize(340, 520);
        setVisible(true);
        addToMainDesktopPane();
        center();
        requestFocus();
    }

    DimensionId getDimensionId()
    {
        return mergePanel.getDimensionId();
    }

    @Override
    public int getSizeX()
    {
        return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.X);
    }

    @Override
    public int getSizeY()
    {
        return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.Y);
    }

    @Override
    public int getSizeZ()
    {
        if (getDimensionId() != DimensionId.Z)
            return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.Z);

        int size = 0;
        for (Sequence seq : mergePanel.getSequences())
            size += seq.getSizeZ();

        return size;
    }

    @Override
    public int getSizeT()
    {
        if (getDimensionId() != DimensionId.T)
            return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.T);

        int size = 0;
        for (Sequence seq : mergePanel.getSequences())
            size += seq.getSizeT();

        return size;
    }

    @Override
    public int getSizeC()
    {
        int size = 0;

        if (getDimensionId() != DimensionId.C)
        {
            for (Sequence seq : mergePanel.getSequences())
                size = Math.max(size, seq.getSizeC());

            return size;
        }

        // in this case we have only single channel sequence
        return mergePanel.getSelectedChannels().length;
    }

    @Override
    public Image getImage(int t, int z)
    {
        final Sequence[] sequences = mergePanel.getSequences();

        final int sizeX = SequenceUtil.getMaxDim(sequences, DimensionId.X);
        final int sizeY = SequenceUtil.getMaxDim(sequences, DimensionId.Y);
        final int sizeC = getSizeC();

        switch (getDimensionId())
        {
            default:
            case C:

                return MergeCHelper.getImage(sequences, mergePanel.getSelectedChannels(), sizeX, sizeY, t, z,
                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled());

            case Z:
                return MergeZHelper.getImage(sequences, sizeX, sizeY, sizeC, t, z, mergePanel.isInterlaceEnabled(),
                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled());

            case T:
                return MergeTHelper.getImage(sequences, sizeX, sizeY, sizeC, t, z, mergePanel.isInterlaceEnabled(),
                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled());
        }
    }

    @Override
    public Image getImage(int t, int z, int c)
    {
        final IcyBufferedImage img = (IcyBufferedImage) getImage(t, z);

        if (img != null)
            return img.getImage(c);

        return null;
    }
}

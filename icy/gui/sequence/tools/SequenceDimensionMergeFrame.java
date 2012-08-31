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
    final DimensionId dim;

    public SequenceDimensionMergeFrame(DimensionId dim)
    {
        super(dim.toString() + " Dimension merge", true);

        this.dim = dim;

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

                        switch (SequenceDimensionMergeFrame.this.dim)
                        {
                            default:
                            case C:
                                out = SequenceUtil.concatC(getSequences(), mergePanel.isFillEmptyImageEnabled(),
                                        mergePanel.isFitImagesEnabled(), pf);
                                break;

                            case Z:
                                out = SequenceUtil.concatZ(getSequences(), mergePanel.isInterlaceEnabled(),
                                        mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled(), pf);
                                break;

                            case T:
                                out = SequenceUtil.concatT(getSequences(), mergePanel.isInterlaceEnabled(),
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

    Sequence[] getSequences()
    {
        return mergePanel.getSequences().toArray(new Sequence[0]);
    }

    @Override
    public int getSizeX()
    {
        return SequenceUtil.getMaxDim(getSequences(), DimensionId.X);
    }

    @Override
    public int getSizeY()
    {
        return SequenceUtil.getMaxDim(getSequences(), DimensionId.Y);
    }

    @Override
    public int getSizeZ()
    {
        if (dim != DimensionId.Z)
            return SequenceUtil.getMaxDim(getSequences(), DimensionId.Z);

        int size = 0;
        for (Sequence seq : getSequences())
            size += seq.getSizeZ();

        return size;
    }

    @Override
    public int getSizeT()
    {
        if (dim != DimensionId.T)
            return SequenceUtil.getMaxDim(getSequences(), DimensionId.T);

        int size = 0;
        for (Sequence seq : getSequences())
            size += seq.getSizeT();

        return size;
    }

    @Override
    public int getSizeC()
    {
        if (dim != DimensionId.C)
            return SequenceUtil.getMaxDim(getSequences(), DimensionId.C);

        int size = 0;
        for (Sequence seq : getSequences())
            size += seq.getSizeC();

        return size;
    }

    @Override
    public Image getImage(int t, int z)
    {
        final Sequence[] sequences = getSequences();

        final int sizeX = SequenceUtil.getMaxDim(sequences, DimensionId.X);
        final int sizeY = SequenceUtil.getMaxDim(sequences, DimensionId.Y);
        final int sizeC = SequenceUtil.getMaxDim(sequences, DimensionId.C);

        switch (dim)
        {
            default:
            case C:
                return MergeCHelper.getImage(sequences, sizeX, sizeY, t, z, mergePanel.isFillEmptyImageEnabled(),
                        mergePanel.isFitImagesEnabled());

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

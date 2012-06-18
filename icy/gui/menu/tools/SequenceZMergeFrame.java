/**
 * 
 */
package icy.gui.menu.tools;

import icy.gui.frame.ActionFrame;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;

import java.awt.Image;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class SequenceZMergeFrame extends ActionFrame implements SequenceModel, ChangeListener
{
    final SequenceDimensionMergePanel mergePanel;

    public SequenceZMergeFrame()
    {
        super("Z Dimension merge", true);

        mergePanel = new SequenceDimensionMergePanel();
        mergePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(mergePanel);
        validate();

        mergePanel.setModel(this);
        mergePanel.addChangeListener(this);

        setSize(340, 520);
        setVisible(true);
        addToMainDesktopPane();
        center();
        requestFocus();
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        mergePanel.previewDimensionChanged();
    }

    @Override
    public int getSizeX()
    {
        final List<Sequence> sequences = mergePanel.getSequences();

        int size = 0;
        for (Sequence seq : sequences)
            size = Math.max(size, seq.getSizeX());

        return size;
    }

    @Override
    public int getSizeY()
    {
        final List<Sequence> sequences = mergePanel.getSequences();

        int size = 0;
        for (Sequence seq : sequences)
            size = Math.max(size, seq.getSizeY());

        return size;
    }

    @Override
    public int getSizeZ()
    {
        final List<Sequence> sequences = mergePanel.getSequences();

        int size = 0;
        for (Sequence seq : sequences)
            size += seq.getSizeZ();

        return size;
    }

    @Override
    public int getSizeT()
    {
        final List<Sequence> sequences = mergePanel.getSequences();

        int size = 0;
        for (Sequence seq : sequences)
            size = Math.max(size, seq.getSizeT());

        return size;
    }

    @Override
    public int getSizeC()
    {
        final List<Sequence> sequences = mergePanel.getSequences();

        int size = 0;
        for (Sequence seq : sequences)
            size = Math.max(size, seq.getSizeC());

        return size;
    }

    private IcyBufferedImage getImageInternal(int t, int z)
    {
        final List<Sequence> sequences = mergePanel.getSequences();

        int zRemaining = z;
        for (Sequence seq : sequences)
        {
            final int sizeZ = seq.getSizeZ();

            // we found the sequence
            if (zRemaining < sizeZ)
            {
                IcyBufferedImage img = seq.getImage(t, zRemaining);

                if (mergePanel.isNoEmptyImageEnabled())
                {
                    int curT = t;
                    while ((img == null) && (curT > 0))
                        img = seq.getImage(--curT, zRemaining);
                }

                return img;
            }

            zRemaining -= sizeZ;
        }

        return null;
    }

    @Override
    public Image getImage(int t, int z)
    {
        return getImageInternal(t, z);
    }

    @Override
    public Image getImage(int t, int z, int c)
    {
        final IcyBufferedImage img = getImageInternal(t, z);

        if (img != null)
            return img.getImage(c);

        return null;
    }
}

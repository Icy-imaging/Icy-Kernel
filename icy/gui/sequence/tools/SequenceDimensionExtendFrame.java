/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.image.IcyBufferedImage;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;
import icy.sequence.SequenceUtil;
import icy.sequence.SequenceUtil.AddTHelper;
import icy.sequence.SequenceUtil.AddZHelper;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

/**
 * @author Stephane
 */
public class SequenceDimensionExtendFrame extends ActionFrame implements SequenceModel
{
    final SequenceDimensionExtendPanel extendPanel;
    final Sequence sequence;

    public SequenceDimensionExtendFrame(Sequence sequence, DimensionId dim)
    {
        super(dim.toString() + " Dimension extend", true);

        setTitleVisible(false);

        this.sequence = sequence;

        extendPanel = new SequenceDimensionExtendPanel(dim);
        extendPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        final int size = sequence.getSize(dim);

        extendPanel.setNewSize(size);
        extendPanel.setInsertPosition(size);
        extendPanel.setMaxDuplicate(size);

        mainPanel.add(extendPanel, BorderLayout.CENTER);
        validate();

        extendPanel.setModel(this);

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
                        final Sequence sequence = SequenceDimensionExtendFrame.this.sequence;
                        final ProgressFrame pf = new ProgressFrame("Extending sequence...");

                        switch (getDimensionId())
                        {
                            default:
                            case Z:
                                SequenceUtil.addZ(sequence, extendPanel.getInsertPosition(), extendPanel.getNewSize()
                                        - sequence.getSizeZ(), extendPanel.getDuplicateNumber());
                                break;

                            case T:
                                SequenceUtil.addT(sequence, extendPanel.getInsertPosition(), extendPanel.getNewSize()
                                        - sequence.getSizeT(), extendPanel.getDuplicateNumber());
                                break;
                        }

                        pf.close();
                    }
                });
            }
        });

        setSize(340, 400);
        setVisible(true);
        addToMainDesktopPane();
        center();
        requestFocus();
    }

    DimensionId getDimensionId()
    {
        return extendPanel.getDimensionId();
    }

    @Override
    public int getSizeX()
    {
        return sequence.getSizeX();
    }

    @Override
    public int getSizeY()
    {
        return sequence.getSizeX();
    }

    @Override
    public int getSizeZ()
    {
        if (getDimensionId() == DimensionId.Z)
            return extendPanel.getNewSize();

        return sequence.getSizeZ();
    }

    @Override
    public int getSizeT()
    {
        if (getDimensionId() == DimensionId.T)
            return extendPanel.getNewSize();

        return sequence.getSizeT();
    }

    @Override
    public int getSizeC()
    {
        if (getDimensionId() == DimensionId.C)
            return extendPanel.getNewSize();

        return sequence.getSizeC();
    }

    @Override
    public Image getImage(int t, int z)
    {
        switch (getDimensionId())
        {
            default:
            case Z:
                return AddZHelper.getExtendedImage(sequence, t, z, extendPanel.getInsertPosition(),
                        extendPanel.getNewSize() - sequence.getSizeZ(), extendPanel.getDuplicateNumber());

            case T:
                return AddTHelper.getExtendedImage(sequence, t, z, extendPanel.getInsertPosition(),
                        extendPanel.getNewSize() - sequence.getSizeT(), extendPanel.getDuplicateNumber());
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

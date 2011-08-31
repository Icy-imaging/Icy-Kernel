/**
 * 
 */
package icy.gui.inspector;

import icy.gui.component.ComponentUtil;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.IcyTextListener;
import icy.gui.util.GuiUtil;
import icy.math.MathUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceAdapter;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.sequence.WeakSequenceListener;
import icy.system.thread.ThreadUtil;
import icy.type.TypeUtil;

import java.lang.ref.WeakReference;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class SequenceInfosPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -6123324347914804260L;

    final IcyTextField nameLabel;
    final JLabel dimensionLabel;
    final JLabel sizeLabel;
    final JLabel channelLabel;

    /**
     * internals
     */
    WeakReference<Sequence> internalSequence;
    private final SequenceListener sequenceListener;
    private final WeakSequenceListener weakSequenceListener;

    public SequenceInfosPanel()
    {
        super(true);

        internalSequence = new WeakReference<Sequence>(null);

        // we use a JTextField as JLabel resize its container
        nameLabel = new IcyTextField();
        ComponentUtil.setPreferredWidth(nameLabel, 100);
        nameLabel.addTextListener(new IcyTextListener()
        {
            @Override
            public void textChanged(IcyTextField source)
            {
                final Sequence seq = internalSequence.get();

                if (seq != null)
                    seq.setName(source.getText());
            }
        });
        dimensionLabel = new JLabel();
        sizeLabel = new JLabel();
        channelLabel = new JLabel();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), GuiUtil.createFixedWidthLabel("Name", 68),
                nameLabel, Box.createHorizontalStrut(2), Box.createHorizontalGlue()));
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), GuiUtil.createFixedWidthLabel("Dim XYZT", 70),
                dimensionLabel, Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), GuiUtil.createFixedWidthLabel("Channel", 70),
                channelLabel, Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), GuiUtil.createFixedWidthLabel("Size", 70),
                sizeLabel, Box.createHorizontalStrut(4), Box.createHorizontalGlue()));

        updateInfos(null);

        sequenceListener = new SequenceAdapter()
        {
            @Override
            public void sequenceChanged(SequenceEvent event)
            {
                final SequenceEvent e = event;

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final Sequence sequence = e.getSequence();

                        switch (e.getSourceType())
                        {
                            case SEQUENCE_NAME:
                                final String name = sequence.getName();

                                if (!nameLabel.getText().equals(name))
                                {
                                    try
                                    {
                                        nameLabel.setText(name);
                                    }
                                    catch (IllegalStateException e)
                                    {
                                        // ignore as it can't sometime happen in multi threaded env

                                    }
                                }
                                break;

                            case SEQUENCE_DATA:
                            case SEQUENCE_TYPE:
                                ThreadUtil.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // simple refresh
                                        updateInfos(sequence);
                                    }
                                });
                                break;
                        }
                    }
                });
            }
        };

        weakSequenceListener = new WeakSequenceListener(sequenceListener);
    }

    public void setSequence(Sequence value)
    {
        if (internalSequence.get() != value)
        {
            final Sequence previousSequence = internalSequence.get();

            // unregister previous sequence listener if any
            if (previousSequence != null)
                previousSequence.removeListener(weakSequenceListener);

            // important to set internalSequence before updateInfos
            internalSequence = new WeakReference<Sequence>(value);

            updateInfos(value);

            // register new listener
            if (value != null)
                value.addListener(weakSequenceListener);
        }
    }

    public void updateInfos(Sequence seq)
    {
        if (seq != null)
        {
            final String name = seq.getName();
            final long size = seq.getSizeX() * seq.getSizeY() * seq.getSizeZ() * seq.getSizeT() * seq.getSizeC()
                    * TypeUtil.sizeOf(seq.getDataType());

            if (!nameLabel.getText().equals(name))
                nameLabel.setText(name);
            nameLabel.setEnabled(true);
            nameLabel.setEditable(true);
            dimensionLabel.setText(seq.getSizeX() + " x " + seq.getSizeY() + " x " + seq.getSizeZ() + " x "
                    + seq.getSizeT());
            channelLabel.setText(seq.getSizeC() + " (" + TypeUtil.toString(seq.getDataType(), seq.isSignedDataType())
                    + ")");
            sizeLabel.setText(MathUtil.getBytesString(size));

            nameLabel.setToolTipText(nameLabel.getText());
            dimensionLabel.setToolTipText("Size X : " + seq.getSizeX() + "   SizeY : " + seq.getSizeY()
                    + "   Size Z : " + seq.getSizeZ() + "   Size T : " + seq.getSizeT());
            if (seq.getSizeC() > 1)
                channelLabel.setToolTipText(seq.getSizeC() + " channels ("
                        + TypeUtil.toString(seq.getDataType(), seq.isSignedDataType()) + ")");
            else
                channelLabel.setToolTipText(seq.getSizeC() + " channel ("
                        + TypeUtil.toString(seq.getDataType(), seq.isSignedDataType()) + ")");
            sizeLabel.setToolTipText(sizeLabel.getText());
        }
        else
        {
            nameLabel.setText("-");
            nameLabel.setEditable(false);
            nameLabel.setEnabled(false);
            dimensionLabel.setText("-");
            channelLabel.setText("-");
            sizeLabel.setText("-");

            nameLabel.setToolTipText(nameLabel.getText());
            dimensionLabel.setToolTipText(dimensionLabel.getText());
            channelLabel.setToolTipText(channelLabel.getText());
            sizeLabel.setToolTipText(sizeLabel.getText());
        }
    }
}

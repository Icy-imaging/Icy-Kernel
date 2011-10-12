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
import icy.util.StringUtil;

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

    final IcyTextField nameField;
    final JLabel dimensionLabel;
    final IcyTextField resXField;
    final IcyTextField resYField;
    final IcyTextField resZField;
    final IcyTextField resTField;
    final JLabel sizeLabel;
    final JLabel channelLabel;

    /**
     * internals
     */
    WeakReference<Sequence> internalSequence;
    private final SequenceListener sequenceListener;
    private final WeakSequenceListener weakSequenceListener;

    boolean updatingName;
    boolean updatingRes;

    public SequenceInfosPanel()
    {
        super(true);

        updatingName = false;
        updatingRes = false;

        internalSequence = new WeakReference<Sequence>(null);

        nameField = new IcyTextField();
        // we have to set a preferred size else it resize its container on enlarging
        ComponentUtil.setPreferredWidth(nameField, 100);
        nameField.addTextListener(new IcyTextListener()
        {
            @Override
            public void textChanged(IcyTextField source)
            {
                if (!updatingName)
                {
                    final Sequence seq = internalSequence.get();

                    if (seq != null)
                        seq.setName(source.getText());
                }
            }
        });

        resXField = new IcyTextField();
        resXField.addTextListener(new IcyTextListener()
        {
            @Override
            public void textChanged(IcyTextField source)
            {
                if (!updatingRes)
                {
                    final Sequence seq = internalSequence.get();

                    if (seq != null)
                        seq.setPixelSizeX(StringUtil.parseDouble(resXField.getText(), 1d));
                }
            }
        });
        resYField = new IcyTextField();
        resYField.addTextListener(new IcyTextListener()
        {
            @Override
            public void textChanged(IcyTextField source)
            {
                if (!updatingRes)
                {
                    final Sequence seq = internalSequence.get();

                    if (seq != null)
                        seq.setPixelSizeY(StringUtil.parseDouble(resYField.getText(), 1d));
                }
            }
        });
        resZField = new IcyTextField();
        resZField.addTextListener(new IcyTextListener()
        {
            @Override
            public void textChanged(IcyTextField source)
            {
                if (!updatingRes)
                {
                    final Sequence seq = internalSequence.get();

                    if (seq != null)
                        seq.setPixelSizeZ(StringUtil.parseDouble(resZField.getText(), 1d));
                }
            }
        });
        resTField = new IcyTextField();
        resTField.addTextListener(new IcyTextListener()
        {
            @Override
            public void textChanged(IcyTextField source)
            {
                if (!updatingRes)
                {
                    final Sequence seq = internalSequence.get();

                    if (seq != null)
                        seq.setPixelSizeT(StringUtil.parseDouble(resTField.getText(), 1d));
                }
            }
        });

        dimensionLabel = new JLabel();
        sizeLabel = new JLabel();
        channelLabel = new JLabel();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel label;

        label = GuiUtil.createFixedWidthLabel("Name", 68);
        label.setToolTipText("Sequence name");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, nameField, Box.createHorizontalStrut(2),
                Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Dimension", 70);
        label.setToolTipText("Size of X, Y, Z and T dimension");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, dimensionLabel,
                Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Channel", 70);
        label.setToolTipText("Number of channel (and data type)");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, channelLabel, Box.createHorizontalStrut(4),
                Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Size", 70);
        label.setToolTipText("Size");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, sizeLabel, Box.createHorizontalStrut(4),
                Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Pixel size", 68);
        label.setToolTipText("Pixel size for X, Y, Z dimension (in mm) and time resolution for T dimension (in ms)");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resXField, resYField, resZField, resTField,
                Box.createHorizontalStrut(2), Box.createHorizontalGlue()));

        updateAllInfos(null);

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
                                updateName(sequence);
                                break;

                            case SEQUENCE_DATA:
                            case SEQUENCE_TYPE:
                                updateAllInfos(sequence);
                                break;

                            case SEQUENCE_META:
                                updateResolutions(sequence);
                                break;
                        }
                    }
                });
            }
        };

        // weak reference --> released when SequenceInfosPanel is released
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

            updateAllInfos(value);

            // register new listener
            if (value != null)
                value.addListener(weakSequenceListener);
        }
    }

    public void updateAllInfos(Sequence seq)
    {
        updateName(seq);
        updateResolutions(seq);

        if (seq != null)
        {
            final int sizeX = seq.getSizeX();
            final int sizeY = seq.getSizeY();
            final int sizeZ = seq.getSizeZ();
            final int sizeT = seq.getSizeT();
            final int sizeC = seq.getSizeC();

            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(sizeC + " (" + TypeUtil.toString(seq.getDataType(), seq.isSignedDataType()) + ")");
            sizeLabel.setText(MathUtil.getBytesString(sizeX * sizeY * sizeZ * sizeT * sizeC
                    * TypeUtil.sizeOf(seq.getDataType())));

            dimensionLabel.setToolTipText("Size X : " + sizeX + "   Size Y : " + sizeY + "   Size Z : " + sizeZ
                    + "   Size T : " + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels ("
                        + TypeUtil.toString(seq.getDataType(), seq.isSignedDataType()) + ")");
            else
                channelLabel.setToolTipText(sizeC + " channel ("
                        + TypeUtil.toString(seq.getDataType(), seq.isSignedDataType()) + ")");
            sizeLabel.setToolTipText(sizeLabel.getText());
        }
        else
        {
            dimensionLabel.setText("-");
            channelLabel.setText("-");
            sizeLabel.setText("-");

            dimensionLabel.setToolTipText(dimensionLabel.getText());
            channelLabel.setToolTipText(channelLabel.getText());
            sizeLabel.setToolTipText(sizeLabel.getText());
        }
    }

    public void updateName(Sequence sequence)
    {
        updatingName = true;
        try
        {
            if (sequence != null)
            {
                try
                {
                    nameField.setText(sequence.getName());
                }
                catch (IllegalStateException e)
                {
                    // ignore as it can't sometime happen in multi threaded env

                }

                nameField.setToolTipText(sequence.getName());
                nameField.setEnabled(true);
            }
            else
            {
                nameField.setText("-");
                nameField.setToolTipText("");
                nameField.setEnabled(false);
            }
        }
        finally
        {
            updatingName = false;
        }
    }

    public void updateResolutions(Sequence sequence)
    {
        updatingRes = true;
        try
        {
            if (sequence != null)
            {
                try
                {
                    resXField.setText(StringUtil.toString(sequence.getPixelSizeX()));
                    resYField.setText(StringUtil.toString(sequence.getPixelSizeY()));
                    resZField.setText(StringUtil.toString(sequence.getPixelSizeZ()));
                    resTField.setText(StringUtil.toString(sequence.getPixelSizeT()));
                }
                catch (IllegalStateException e)
                {
                    // ignore as it can't sometime happen in multi threaded env
                }

                resXField.setToolTipText("X pixel resolution (in mm) : " + resXField.getText());
                resYField.setToolTipText("Y pixel resolution (in mm) : " + resYField.getText());
                resZField.setToolTipText("Z pixel resolution (in mm) : " + resZField.getText());
                resTField.setToolTipText("T time resolution (in ms) : " + resTField.getText());

                resXField.setEnabled(true);
                resYField.setEnabled(true);
                resZField.setEnabled(true);
                resTField.setEnabled(true);
            }
            else
            {
                resXField.setText("-");
                resYField.setText("-");
                resZField.setText("-");
                resTField.setText("-");

                resXField.setToolTipText("X pixel resolution (in mm)");
                resYField.setToolTipText("Y pixel resolution (in mm)");
                resZField.setToolTipText("Z pixel resolution (in mm)");
                resTField.setToolTipText("T time resolution (in ms)");

                resXField.setEnabled(false);
                resYField.setEnabled(false);
                resZField.setEnabled(false);
                resTField.setEnabled(false);
            }
        }
        finally
        {
            updatingRes = false;
        }
    }
}

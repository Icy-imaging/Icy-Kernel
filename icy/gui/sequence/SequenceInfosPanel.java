/**
 * 
 */
package icy.gui.sequence;

import icy.gui.component.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.sequence.Sequence;
import icy.sequence.SequenceAdapter;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.sequence.WeakSequenceListener;
import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

    final JLabel nameLabel;
    final JLabel dimensionLabel;
    final JLabel resXLabel;
    final JLabel resYLabel;
    final JLabel resZLabel;
    final JLabel resTLabel;
    final JLabel sizeLabel;
    final JLabel channelLabel;

    final JButton editBtn;
    boolean pxSizeYdifferent = true;
    
    /**
     * internals
     */
    WeakReference<Sequence> internalSequence;
    private final SequenceListener sequenceListener;
    private final WeakSequenceListener weakSequenceListener;

    public SequenceInfosPanel()
    {
        super(true);

        nameLabel = new JLabel();
        ComponentUtil.setFixedWidth(nameLabel, 160);
        resXLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resXLabel, 48);
        resYLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resYLabel, 48);
        resZLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resZLabel, 48);
        resTLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resTLabel, 48);
        dimensionLabel = new JLabel();
        sizeLabel = new JLabel();
        channelLabel = new JLabel();

        editBtn = new JButton("Edit Properties");
        editBtn.setToolTipText("Edit sequence properties");
        editBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence seq = internalSequence.get();

                if (seq != null)
                    new SequencePropertiesDialog(seq);
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        reloadInfoPanel();

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
                            case SEQUENCE_DATA:
                            case SEQUENCE_TYPE:
                            case SEQUENCE_META:
                                updateInfos(sequence);
                                break;
                        }
                    }
                });
            }
        };

        // weak reference --> released when SequenceInfosPanel is released
        weakSequenceListener = new WeakSequenceListener(sequenceListener);
        internalSequence = new WeakReference<Sequence>(null);
    }

    public void reloadInfoPanel() 
    {
    	removeAll();
    	JLabel label;

        label = GuiUtil.createFixedWidthLabel("Name", 70);
        label.setToolTipText("Sequence name");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, nameLabel, Box.createHorizontalStrut(4),
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
        label = GuiUtil.createFixedWidthLabel("Pixel size", 70);
        
        if (pxSizeYdifferent) {
        	label.setToolTipText("Pixel size for X, Y, Z dimension (in mm) and time resolution for T dimension (in ms)");
        	add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resXLabel, Box.createHorizontalStrut(4),resYLabel,Box.createHorizontalStrut(4), resZLabel, Box.createHorizontalStrut(4), resTLabel,
                Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        }
        else {
        	label.setToolTipText("Pixel size for XY, Z dimension (in mm) and time resolution for T dimension (in ms)");
        	add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resXLabel, Box.createHorizontalStrut(4),resZLabel,Box.createHorizontalStrut(4), resTLabel,
                    Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        }
        add(GuiUtil.createLineBoxPanel(editBtn));
        revalidate();
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

    public void updateInfos(Sequence sequence)
    {
        if (sequence != null)
        {
            final int sizeX = sequence.getSizeX();
            final int sizeY = sequence.getSizeY();
            final int sizeZ = sequence.getSizeZ();
            final int sizeT = sequence.getSizeT();
            final int sizeC = sequence.getSizeC();
            
            final double pxSizeX = sequence.getPixelSizeX();
            final double pxSizeY = sequence.getPixelSizeY();
            final double pxSizeZ = sequence.getPixelSizeZ();
            
            final UnitPrefix pxSizeXUnit = UnitUtil.getBestUnit(pxSizeX,UnitPrefix.MILLI);
            final UnitPrefix pxSizeYUnit = UnitUtil.getBestUnit(pxSizeY,UnitPrefix.MILLI);

            nameLabel.setText(sequence.getName());
            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(sizeC + " (" + sequence.getDataType_() + ")");
            sizeLabel.setText(UnitUtil.getBytesString(sizeX * sizeY * sizeZ * sizeT * sizeC
                    * sequence.getDataType_().getSize()));
            resXLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeX, 2, UnitPrefix.MILLI));
            resYLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeY, 2, UnitPrefix.MILLI));
            resZLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeZ, 2, UnitPrefix.MILLI));
            resTLabel.setText(Double.toString(sequence.getTimeInterval()));
            
            pxSizeYdifferent = !(pxSizeX == pxSizeY && pxSizeXUnit == pxSizeYUnit);

            nameLabel.setToolTipText(sequence.getName());
            dimensionLabel.setToolTipText("Size X: " + sizeX + "   Size Y: " + sizeY + "   Size Z: " + sizeZ
                    + "   Size T: " + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels (" + sequence.getDataType_() + ")");
            else
                channelLabel.setToolTipText(sizeC + " channel (" + sequence.getDataType_() + ")");
            sizeLabel.setToolTipText(sizeLabel.getText());
            
            if (pxSizeYdifferent) 
            	resXLabel.setToolTipText("X pixel resolution: " + resXLabel.getText());
            else
            	resXLabel.setToolTipText("X / Y pixel resolution: " + resXLabel.getText());
            resYLabel.setToolTipText("Y pixel resolution: " + resYLabel.getText());
            resZLabel.setToolTipText("Z pixel resolution: " + resZLabel.getText());
            resTLabel.setToolTipText("T time resolution: " + resTLabel.getText() + " ms");

            editBtn.setEnabled(true);
            reloadInfoPanel();
        }
        else
        {
            nameLabel.setText("-");
            dimensionLabel.setText("-");
            channelLabel.setText("-");
            sizeLabel.setText("-");
            resXLabel.setText("-");
            resYLabel.setText("-");
            resZLabel.setText("-");
            resTLabel.setText("-");

            nameLabel.setToolTipText("");
            dimensionLabel.setToolTipText("");
            channelLabel.setToolTipText("");
            sizeLabel.setToolTipText("");
            resXLabel.setToolTipText("X pixel resolution (in mm)");
            resYLabel.setToolTipText("Y pixel resolution (in mm)");
            resZLabel.setToolTipText("Z pixel resolution (in mm)");
            resTLabel.setToolTipText("T time resolution (in ms)");

            editBtn.setEnabled(false);
        }

        revalidate();
    }
}

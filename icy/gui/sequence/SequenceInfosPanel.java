/**
 * 
 */
package icy.gui.sequence;

import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.frame.GenericFrame;
import icy.gui.inspector.InspectorPanel.InspectorSubPanel;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.resource.ResourceUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * @author Stephane
 */
public class SequenceInfosPanel extends InspectorSubPanel
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

    final IcyButton editBtn;
    final IcyButton detailBtn;

    final SingleProcessor processor;

    boolean pxSizeYdifferent = true;

    public SequenceInfosPanel()
    {
        super();

        processor = new SingleProcessor(true);

        nameLabel = new JLabel();
        ComponentUtil.setFixedWidth(nameLabel, 160);
        resXLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resXLabel, 48);
        resYLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resYLabel, 48);
        resZLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resZLabel, 48);
        resTLabel = new JLabel();
        dimensionLabel = new JLabel();
        sizeLabel = new JLabel();
        channelLabel = new JLabel();

        editBtn = new IcyButton("Edit properties", ResourceUtil.ICON_DOCEDIT);
        editBtn.setToolTipText("Edit sequence properties");
        editBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // it should be the current focused sequence
                final Sequence seq = Icy.getMainInterface().getFocusedSequence();

                if (seq != null)
                    new SequencePropertiesDialog(seq);
            }
        });

        detailBtn = new IcyButton("Show details", ResourceUtil.ICON_PROPERTIES);
        detailBtn.setToolTipText("Show all associated metadata informations");
        detailBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // it should be the current focused sequence
                final Sequence seq = Icy.getMainInterface().getFocusedSequence();

                if (seq != null)
                {
                    final GenericFrame g = new GenericFrame(seq.getName() + " - Metadata", new SequenceMetadataPanel(
                            seq));

                    g.addToMainDesktopPane();
                    g.center();
                    g.requestFocus();
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        rebuild();
        updateInfos(null);
    }

    public void rebuild()
    {
        removeAll();

        JLabel label;

        label = GuiUtil.createFixedWidthLabel("Name", 90);
        label.setToolTipText("Sequence name");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, nameLabel, Box.createHorizontalStrut(4),
                Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Dimension", 90);
        label.setToolTipText("Size of X, Y, Z and T dimension");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, dimensionLabel,
                Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Channel", 90);
        label.setToolTipText("Number of channel - data type");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, channelLabel, Box.createHorizontalStrut(4),
                Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Size", 90);
        label.setToolTipText("Size");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, sizeLabel, Box.createHorizontalStrut(4),
                Box.createHorizontalGlue()));
        label = GuiUtil.createFixedWidthLabel("Pixel size", 90);

        if (pxSizeYdifferent)
        {
            label.setToolTipText("Pixel size for X, Y, Z dimension");
            add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resXLabel,
                    Box.createHorizontalStrut(4), resYLabel, Box.createHorizontalStrut(4), resZLabel,
                    Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        }
        else
        {
            label.setToolTipText("Pixel size for X/Y and Z dimension");
            add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resXLabel,
                    Box.createHorizontalStrut(4), resZLabel, Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        }
        label = GuiUtil.createFixedWidthLabel("Time interval", 90);
        label.setToolTipText("Time Interval");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resTLabel, Box.createHorizontalStrut(4),
                Box.createHorizontalGlue()));
        add(Box.createVerticalStrut(4));
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), detailBtn, Box.createHorizontalGlue(), editBtn,
                Box.createHorizontalStrut(4)));
        add(Box.createVerticalStrut(4));

        revalidate();
    }

    public void updateInfos(final Sequence sequence)
    {
        processor.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                updateInfosInternal(sequence);
            }
        }, true);
    }

    public void updateInfosInternal(Sequence sequence)
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

            final UnitPrefix pxSizeXUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MILLI);
            final UnitPrefix pxSizeYUnit = UnitUtil.getBestUnit(pxSizeY, UnitPrefix.MILLI);

            nameLabel.setText(sequence.getName());
            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(sizeC + " - " + sequence.getDataType_());
            sizeLabel.setText(UnitUtil.getBytesString(sizeX * sizeY * sizeZ * sizeT * sizeC
                    * sequence.getDataType_().getSize()));
            resXLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeX, 2, UnitPrefix.MILLI));
            resYLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeY, 2, UnitPrefix.MILLI));
            resZLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeZ, 2, UnitPrefix.MILLI));
            resTLabel.setText(UnitUtil.displayTimeAsStringWithUnits(sequence.getTimeInterval(), false));

            pxSizeYdifferent = !(pxSizeX == pxSizeY && pxSizeXUnit == pxSizeYUnit);

            nameLabel.setToolTipText(sequence.getName());
            dimensionLabel.setToolTipText("Size X: " + sizeX + " Size Y: " + sizeY + " Size Z: " + sizeZ + " Size T: "
                    + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels - " + sequence.getDataType_());
            else
                channelLabel.setToolTipText(sizeC + " channel - " + sequence.getDataType_());
            sizeLabel.setToolTipText(sizeLabel.getText());

            if (pxSizeYdifferent)
                resXLabel.setToolTipText("X pixel resolution: " + resXLabel.getText());
            else
                resXLabel.setToolTipText("X / Y pixel resolution: " + resXLabel.getText());
            resYLabel.setToolTipText("Y pixel resolution: " + resYLabel.getText());
            resZLabel.setToolTipText("Z pixel resolution: " + resZLabel.getText());
            resTLabel.setToolTipText("T time resolution: " + resTLabel.getText());

            editBtn.setEnabled(true);
            detailBtn.setEnabled(true);

            rebuild();
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
            resXLabel.setToolTipText("X pixel resolution");
            resYLabel.setToolTipText("Y pixel resolution");
            resZLabel.setToolTipText("Z pixel resolution");
            resTLabel.setToolTipText("T time resolution");

            editBtn.setEnabled(false);
            detailBtn.setEnabled(false);
        }

        revalidate();
    }

    @Override
    public void viewerFocused(Viewer viewer)
    {

    }

    @Override
    public void focusedViewerChanged(ViewerEvent event)
    {

    }

    @Override
    public void sequenceFocused(Sequence sequence)
    {
        updateInfos(sequence);
    }

    @Override
    public void focusedSequenceChanged(SequenceEvent event)
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
                    case SEQUENCE_DATA:
                    case SEQUENCE_TYPE:
                    case SEQUENCE_META:
                        updateInfos(sequence);
                        break;
                }
            }
        });
    }
}

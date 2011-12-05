/**
 * 
 */
package icy.gui.sequence;

import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.inspector.InspectorPanel.InspectorSubPanel;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.resource.ResourceUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
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

    public SequenceInfosPanel()
    {
        super();

        nameLabel = new JLabel();
        ComponentUtil.setFixedWidth(nameLabel, 160);
        resXLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resXLabel, 40);
        resYLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resYLabel, 40);
        resZLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resZLabel, 40);
        resTLabel = new JLabel();
        ComponentUtil.setPreferredWidth(resTLabel, 40);
        dimensionLabel = new JLabel();
        sizeLabel = new JLabel();
        channelLabel = new JLabel();

        editBtn = new IcyButton(ResourceUtil.ICON_DOCEDIT, 20);
        editBtn.setToolTipText("Edit sequence properties");
        editBtn.setFlat(true);
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

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

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
        label.setToolTipText("Pixel size for X, Y, Z dimension (in mm) and time resolution for T dimension (in ms)");
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), label, resXLabel, resYLabel, resZLabel, resTLabel,
                Box.createHorizontalStrut(4), Box.createHorizontalGlue()));
        add(GuiUtil.createLineBoxPanel(Box.createHorizontalGlue(), Box.createHorizontalStrut(4), editBtn,
                Box.createHorizontalStrut(4)));

        updateInfos(null);
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

            nameLabel.setText(sequence.getName());
            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(sizeC + " (" + sequence.getDataType_() + ")");
            sizeLabel.setText(MathUtil.getBytesString(sizeX * sizeY * sizeZ * sizeT * sizeC
                    * sequence.getDataType_().getSize()));
            resXLabel.setText(Double.toString(sequence.getPixelSizeX()));
            resYLabel.setText(Double.toString(sequence.getPixelSizeY()));
            resZLabel.setText(Double.toString(sequence.getPixelSizeZ()));
            resTLabel.setText(Double.toString(sequence.getTimeInterval()));

            nameLabel.setToolTipText(sequence.getName());
            dimensionLabel.setToolTipText("Size X : " + sizeX + "   Size Y : " + sizeY + "   Size Z : " + sizeZ
                    + "   Size T : " + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels (" + sequence.getDataType_() + ")");
            else
                channelLabel.setToolTipText(sizeC + " channel (" + sequence.getDataType_() + ")");
            sizeLabel.setToolTipText(sizeLabel.getText());
            resXLabel.setToolTipText("X pixel resolution (in mm) : " + resXLabel.getText());
            resYLabel.setToolTipText("Y pixel resolution (in mm) : " + resYLabel.getText());
            resZLabel.setToolTipText("Z pixel resolution (in mm) : " + resZLabel.getText());
            resTLabel.setToolTipText("T time resolution (in ms) : " + resTLabel.getText());

            editBtn.setEnabled(true);
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
}

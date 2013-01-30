/**
 * 
 */
package icy.gui.sequence;

import icy.gui.component.button.IcyButton;
import icy.gui.frame.GenericFrame;
import icy.gui.inspector.InspectorPanel.InspectorSubPanel;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

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

    private JLabel nameLabel;
    private JLabel dimensionLabel;
    private JLabel resXLabel;
    private JLabel resYLabel;
    private JLabel resZLabel;
    private JLabel resTLabel;
    private JLabel sizeLabel;
    private JLabel channelLabel;

    private IcyButton editBtn;
    private IcyButton detailBtn;

    final SingleProcessor processor;

    public SequenceInfosPanel()
    {
        super();

        initialize();

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

        processor = new SingleProcessor(true, "Sequence infos GUI");
        // we want the processor to stay alive for some time
        processor.setKeepAliveTime(10, TimeUnit.MINUTES);

        updateInfos(null);
    }

    public void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 40, 40, 40, 0};
        gridBagLayout.rowHeights = new int[] {18, 18, 18, 18, 18, 18, 18, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JLabel lbl_name = new JLabel("Name");
        lbl_name.setToolTipText("Sequence name");
        GridBagConstraints gbc_lbl_name = new GridBagConstraints();
        gbc_lbl_name.anchor = GridBagConstraints.WEST;
        gbc_lbl_name.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_name.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_name.gridx = 0;
        gbc_lbl_name.gridy = 0;
        add(lbl_name, gbc_lbl_name);

        nameLabel = new JLabel();
        nameLabel.setText("---");
        GridBagConstraints gbc_nameLabel = new GridBagConstraints();
        gbc_nameLabel.gridwidth = 3;
        gbc_nameLabel.anchor = GridBagConstraints.WEST;
        gbc_nameLabel.fill = GridBagConstraints.VERTICAL;
        gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_nameLabel.gridx = 1;
        gbc_nameLabel.gridy = 0;
        add(nameLabel, gbc_nameLabel);

        JLabel lbl_dim = new JLabel("Dimension");
        lbl_dim.setToolTipText("Size of X, Y, Z and T dimension");
        GridBagConstraints gbc_lbl_dim = new GridBagConstraints();
        gbc_lbl_dim.anchor = GridBagConstraints.WEST;
        gbc_lbl_dim.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_dim.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_dim.gridx = 0;
        gbc_lbl_dim.gridy = 1;
        add(lbl_dim, gbc_lbl_dim);

        dimensionLabel = new JLabel();
        dimensionLabel.setText("---");
        GridBagConstraints gbc_dimensionLabel = new GridBagConstraints();
        gbc_dimensionLabel.gridwidth = 3;
        gbc_dimensionLabel.anchor = GridBagConstraints.WEST;
        gbc_dimensionLabel.fill = GridBagConstraints.VERTICAL;
        gbc_dimensionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_dimensionLabel.gridx = 1;
        gbc_dimensionLabel.gridy = 1;
        add(dimensionLabel, gbc_dimensionLabel);

        JLabel lbl_channel = new JLabel("Channel");
        lbl_channel.setToolTipText("Number of channel - data type");
        GridBagConstraints gbc_lbl_channel = new GridBagConstraints();
        gbc_lbl_channel.anchor = GridBagConstraints.WEST;
        gbc_lbl_channel.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_channel.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_channel.gridx = 0;
        gbc_lbl_channel.gridy = 2;
        add(lbl_channel, gbc_lbl_channel);

        channelLabel = new JLabel();
        channelLabel.setText("---");
        GridBagConstraints gbc_channelLabel = new GridBagConstraints();
        gbc_channelLabel.gridwidth = 3;
        gbc_channelLabel.anchor = GridBagConstraints.WEST;
        gbc_channelLabel.fill = GridBagConstraints.VERTICAL;
        gbc_channelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_channelLabel.gridx = 1;
        gbc_channelLabel.gridy = 2;
        add(channelLabel, gbc_channelLabel);

        JLabel lbl_size = new JLabel("Size");
        lbl_size.setToolTipText("Size");
        GridBagConstraints gbc_lbl_size = new GridBagConstraints();
        gbc_lbl_size.anchor = GridBagConstraints.WEST;
        gbc_lbl_size.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_size.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_size.gridx = 0;
        gbc_lbl_size.gridy = 3;
        add(lbl_size, gbc_lbl_size);

        sizeLabel = new JLabel();
        sizeLabel.setText("---");
        GridBagConstraints gbc_sizeLabel = new GridBagConstraints();
        gbc_sizeLabel.gridwidth = 3;
        gbc_sizeLabel.anchor = GridBagConstraints.WEST;
        gbc_sizeLabel.fill = GridBagConstraints.VERTICAL;
        gbc_sizeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_sizeLabel.gridx = 1;
        gbc_sizeLabel.gridy = 3;
        add(sizeLabel, gbc_sizeLabel);

        JLabel lbl_psx = new JLabel("Pixel size");
        lbl_psx.setToolTipText("Pixel size for X, Y, Z dimension");
        GridBagConstraints gbc_lbl_psx = new GridBagConstraints();
        gbc_lbl_psx.anchor = GridBagConstraints.WEST;
        gbc_lbl_psx.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_psx.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_psx.gridx = 0;
        gbc_lbl_psx.gridy = 4;
        add(lbl_psx, gbc_lbl_psx);

        resXLabel = new JLabel();
        resXLabel.setText("---");
        GridBagConstraints gbc_resXLabel = new GridBagConstraints();
        gbc_resXLabel.anchor = GridBagConstraints.WEST;
        gbc_resXLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resXLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resXLabel.gridx = 1;
        gbc_resXLabel.gridy = 4;
        add(resXLabel, gbc_resXLabel);

        resYLabel = new JLabel();
        resYLabel.setText("---");
        GridBagConstraints gbc_resYLabel = new GridBagConstraints();
        gbc_resYLabel.anchor = GridBagConstraints.WEST;
        gbc_resYLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resYLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resYLabel.gridx = 2;
        gbc_resYLabel.gridy = 4;
        add(resYLabel, gbc_resYLabel);

        resZLabel = new JLabel();
        resZLabel.setText("---");
        GridBagConstraints gbc_resZLabel = new GridBagConstraints();
        gbc_resZLabel.anchor = GridBagConstraints.WEST;
        gbc_resZLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resZLabel.insets = new Insets(0, 0, 5, 0);
        gbc_resZLabel.gridx = 3;
        gbc_resZLabel.gridy = 4;
        add(resZLabel, gbc_resZLabel);

        JLabel lbl_time = new JLabel("Time interval");
        lbl_time.setToolTipText("Time Interval");
        GridBagConstraints gbc_lbl_time = new GridBagConstraints();
        gbc_lbl_time.anchor = GridBagConstraints.WEST;
        gbc_lbl_time.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_time.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_time.gridx = 0;
        gbc_lbl_time.gridy = 5;
        add(lbl_time, gbc_lbl_time);

        resTLabel = new JLabel();
        resTLabel.setText("---");
        GridBagConstraints gbc_resTLabel = new GridBagConstraints();
        gbc_resTLabel.gridwidth = 3;
        gbc_resTLabel.anchor = GridBagConstraints.WEST;
        gbc_resTLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resTLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resTLabel.gridx = 1;
        gbc_resTLabel.gridy = 5;
        add(resTLabel, gbc_resTLabel);

        editBtn = new IcyButton("Edit properties", new IcyIcon(ResourceUtil.ICON_DOCEDIT));
        editBtn.setToolTipText("Edit sequence properties");

        GridBagConstraints gbc_editBtn = new GridBagConstraints();
        gbc_editBtn.gridwidth = 2;
        gbc_editBtn.anchor = GridBagConstraints.WEST;
        gbc_editBtn.fill = GridBagConstraints.VERTICAL;
        gbc_editBtn.insets = new Insets(0, 0, 0, 5);
        gbc_editBtn.gridx = 0;
        gbc_editBtn.gridy = 6;
        add(editBtn, gbc_editBtn);

        detailBtn = new IcyButton("Show details", new IcyIcon(ResourceUtil.ICON_PROPERTIES));
        detailBtn.setToolTipText("Show all associated metadata informations");

        GridBagConstraints gbc_detailBtn = new GridBagConstraints();
        gbc_detailBtn.gridwidth = 2;
        gbc_detailBtn.insets = new Insets(0, 0, 0, 5);
        gbc_detailBtn.anchor = GridBagConstraints.EAST;
        gbc_detailBtn.fill = GridBagConstraints.VERTICAL;
        gbc_detailBtn.gridx = 2;
        gbc_detailBtn.gridy = 6;
        add(detailBtn, gbc_detailBtn);
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

            nameLabel.setText(StringUtil.limit(sequence.getName(), 28, true));
            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(sizeC + " - " + sequence.getDataType_());
            sizeLabel.setText(UnitUtil.getBytesString((double) sizeX * (double) sizeY * sizeZ * sizeT * sizeC
                    * sequence.getDataType_().getSize()));
            resXLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeX, 2, UnitPrefix.MICRO));
            resYLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeY, 2, UnitPrefix.MICRO));
            resZLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeZ, 2, UnitPrefix.MICRO));
            resTLabel.setText(UnitUtil.displayTimeAsStringWithUnits(sequence.getTimeInterval() * 1000d, false));

            nameLabel.setToolTipText(sequence.getName());
            dimensionLabel.setToolTipText("Size X : " + sizeX + "   Size Y : " + sizeY + "   Size Z : " + sizeZ
                    + "   Size T : " + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels - " + sequence.getDataType_());
            else
                channelLabel.setToolTipText(sizeC + " channel - " + sequence.getDataType_());
            sizeLabel.setToolTipText(sizeLabel.getText());

            resXLabel.setToolTipText("X pixel resolution: " + resXLabel.getText());
            resYLabel.setToolTipText("Y pixel resolution: " + resYLabel.getText());
            resZLabel.setToolTipText("Z pixel resolution: " + resZLabel.getText());
            resTLabel.setToolTipText("T time resolution: " + resTLabel.getText());

            editBtn.setEnabled(true);
            detailBtn.setEnabled(true);
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
    public void focusChanged(Viewer viewer)
    {

    }

    @Override
    public void focusedViewerChanged(ViewerEvent event)
    {

    }

    @Override
    public void focusChanged(Sequence sequence)
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

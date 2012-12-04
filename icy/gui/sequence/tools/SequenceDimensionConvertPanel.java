/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.component.sequence.SequencePreviewPanel;
import icy.image.IcyBufferedImage;
import icy.sequence.AbstractSequenceModel;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class SequenceDimensionConvertPanel extends JPanel
{
    private class SequenceDimensionConvertPanelModel extends AbstractSequenceModel
    {
        public SequenceDimensionConvertPanelModel()
        {
            super();
        }

        @Override
        public Image getImage(int t, int z)
        {
            return SequenceUtil.AdjustZTHelper
                    .getImage(sequence, t, z, getNewSizeZ(), getNewSizeT(), isOrderReversed());
        }

        @Override
        public int getSizeX()
        {
            return sequence.getSizeX();
        }

        @Override
        public int getSizeY()
        {
            return sequence.getSizeY();
        }

        @Override
        public int getSizeZ()
        {
            return getNewSizeZ();
        }

        @Override
        public int getSizeT()
        {
            return getNewSizeT();
        }

        @Override
        public int getSizeC()
        {
            return sequence.getSizeC();
        }

        @Override
        public Image getImage(int t, int z, int c)
        {
            return ((IcyBufferedImage) getImage(t, z)).getImage(c);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6906749224563258829L;

    // GUI
    private JRadioButton ztRadioButton;
    private JRadioButton tzRadioButton;
    JSlider sizeZSlider;
    JSlider sizeTSlider;
    JSpinner sizeZSpinner;
    JSpinner sizeTSpinner;
    SequencePreviewPanel previewPane;
    private ButtonGroup dimensionGroup;

    // internals
    final Sequence sequence;
    boolean changingZ;
    boolean changingT;

    public SequenceDimensionConvertPanel(Sequence sequence)
    {
        super();

        this.sequence = sequence;
        changingZ = false;
        changingT = false;

        initialize();

        dimensionGroup = new ButtonGroup();
        dimensionGroup.add(tzRadioButton);
        dimensionGroup.add(ztRadioButton);
        dimensionGroup.setSelected(ztRadioButton.getModel(), true);

        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();
        final int sizeZT = sizeZ * sizeT;

        sizeZSlider.setModel(new DefaultBoundedRangeModel(sizeZ, 0, 1, sizeZT));
        sizeZSpinner.setModel(new SpinnerNumberModel(sizeZ, 1, sizeZT, 1));
        sizeTSlider.setModel(new DefaultBoundedRangeModel(sizeT, 0, 1, sizeZT));
        sizeTSpinner.setModel(new SpinnerNumberModel(sizeT, 1, sizeZT, 1));

        tzRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                previewPane.imageChanged();
            }
        });

        ztRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                previewPane.imageChanged();
            }
        });

        sizeZSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if (changingT)
                    return;

                final int newSize = sizeZSlider.getValue();

                sizeZSpinner.setValue(Integer.valueOf(newSize));

                sizeZChanged();
            }
        });
        sizeZSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if (changingT)
                    return;

                final int newSize = ((Integer) sizeZSpinner.getValue()).intValue();

                sizeZSlider.setValue(newSize);

                sizeZChanged();
            }
        });

        sizeTSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if (changingZ)
                    return;

                final int newSize = sizeTSlider.getValue();

                sizeTSpinner.setValue(Integer.valueOf(newSize));

                sizeTChanged();
            }
        });
        sizeTSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if (changingZ)
                    return;

                final int newSize = ((Integer) sizeTSpinner.getValue()).intValue();

                sizeTSlider.setValue(newSize);

                sizeTChanged();
            }
        });

        previewPane.setModel(new SequenceDimensionConvertPanelModel());
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {20, 0, 20, 0, 0, 0, 80, 20, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblNewLabel = new JLabel("Dimension order");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 1;
        gbc_lblNewLabel.gridy = 0;
        add(lblNewLabel, gbc_lblNewLabel);

        ztRadioButton = new JRadioButton("Z - T");
        GridBagConstraints gbc_ztRadioButton = new GridBagConstraints();
        gbc_ztRadioButton.fill = GridBagConstraints.BOTH;
        gbc_ztRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_ztRadioButton.gridx = 3;
        gbc_ztRadioButton.gridy = 0;
        add(ztRadioButton, gbc_ztRadioButton);

        tzRadioButton = new JRadioButton("T - Z");
        GridBagConstraints gbc_tzRadioButton = new GridBagConstraints();
        gbc_tzRadioButton.gridwidth = 2;
        gbc_tzRadioButton.fill = GridBagConstraints.BOTH;
        gbc_tzRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_tzRadioButton.gridx = 4;
        gbc_tzRadioButton.gridy = 0;
        add(tzRadioButton, gbc_tzRadioButton);

        final JLabel lblNewLabel_1 = new JLabel("Size Z");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
        lblNewLabel_1.setToolTipText("Size of Z dimension");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 1;
        gbc_lblNewLabel_1.gridy = 1;
        add(lblNewLabel_1, gbc_lblNewLabel_1);

        sizeZSpinner = new JSpinner();
        sizeZSpinner.setToolTipText("Size of Z dimension");
        GridBagConstraints gbc_zSpinner = new GridBagConstraints();
        gbc_zSpinner.gridwidth = 2;
        gbc_zSpinner.fill = GridBagConstraints.BOTH;
        gbc_zSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_zSpinner.gridx = 3;
        gbc_zSpinner.gridy = 1;
        add(sizeZSpinner, gbc_zSpinner);

        sizeZSlider = new JSlider();
        GridBagConstraints gbc_zSlider = new GridBagConstraints();
        gbc_zSlider.gridwidth = 2;
        gbc_zSlider.fill = GridBagConstraints.BOTH;
        gbc_zSlider.insets = new Insets(0, 0, 5, 5);
        gbc_zSlider.gridx = 5;
        gbc_zSlider.gridy = 1;
        add(sizeZSlider, gbc_zSlider);

        final JLabel lblNewLabel_2 = new JLabel("Size T");
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.TRAILING);
        lblNewLabel_2.setToolTipText("Size of T dimension");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 1;
        gbc_lblNewLabel_2.gridy = 2;
        add(lblNewLabel_2, gbc_lblNewLabel_2);

        sizeTSpinner = new JSpinner();
        sizeTSpinner.setToolTipText("Size of T dimension");
        GridBagConstraints gbc_tSpinner = new GridBagConstraints();
        gbc_tSpinner.gridwidth = 2;
        gbc_tSpinner.fill = GridBagConstraints.BOTH;
        gbc_tSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_tSpinner.gridx = 3;
        gbc_tSpinner.gridy = 2;
        add(sizeTSpinner, gbc_tSpinner);

        sizeTSlider = new JSlider();
        GridBagConstraints gbc_tSlider = new GridBagConstraints();
        gbc_tSlider.gridwidth = 2;
        gbc_tSlider.insets = new Insets(0, 0, 5, 5);
        gbc_tSlider.fill = GridBagConstraints.BOTH;
        gbc_tSlider.gridx = 5;
        gbc_tSlider.gridy = 2;
        add(sizeTSlider, gbc_tSlider);

        previewPane = new SequencePreviewPanel(true);
        previewPane.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_previewPane = new GridBagConstraints();
        gbc_previewPane.gridwidth = 8;
        gbc_previewPane.insets = new Insets(0, 0, 0, 5);
        gbc_previewPane.fill = GridBagConstraints.BOTH;
        gbc_previewPane.gridx = 0;
        gbc_previewPane.gridy = 3;
        add(previewPane, gbc_previewPane);
    }

    void sizeZChanged()
    {
        final int sizeZT = sequence.getSizeZ() * sequence.getSizeT();
        final int newSizeZ = getNewSizeZ();
        int newSizeT = sizeZT / newSizeZ;

        if ((sizeZT % newSizeZ) != 0)
            newSizeT++;

        changingZ = true;
        try
        {
            sizeTSpinner.setValue(Integer.valueOf(newSizeT));
            sizeTSlider.setValue(newSizeT);
            previewPane.dimensionChanged();
        }
        finally
        {
            changingZ = false;
        }
    }

    void sizeTChanged()
    {
        final int sizeZT = sequence.getSizeZ() * sequence.getSizeT();
        final int newSizeT = getNewSizeT();
        int newSizeZ = sizeZT / newSizeT;

        if ((sizeZT % newSizeT) != 0)
            newSizeZ++;

        changingT = true;
        try
        {
            sizeZSpinner.setValue(Integer.valueOf(newSizeZ));
            sizeZSlider.setValue(newSizeZ);
            previewPane.dimensionChanged();
        }
        finally
        {
            changingT = false;
        }
    }

    public Sequence getSequence()
    {
        return sequence;
    }

    public boolean isOrderReversed()
    {
        return dimensionGroup.getSelection() == tzRadioButton.getModel();
    }

    public int getNewSizeZ()
    {
        return ((Integer) sizeZSpinner.getValue()).intValue();
    }

    public int getNewSizeT()
    {
        return ((Integer) sizeTSpinner.getValue()).intValue();
    }

}

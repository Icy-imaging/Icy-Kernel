/**
 * 
 */
package icy.gui.inspector;

import icy.gui.viewer.Viewer;
import icy.image.IntensityInfo;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class RoiExtraInfoPanel extends JPanel
{
    private class RoiInfosCalculator implements Runnable
    {
        final Sequence sequence;
        final Viewer viewer;
        final ROI roi;

        public RoiInfosCalculator(Sequence sequence, ROI roi)
        {
            super();

            this.sequence = sequence;
            this.viewer = Icy.getMainInterface().getFocusedViewer();
            if (roi != null)
                this.roi = roi.getCopy();
            else
                this.roi = null;
        }

        @Override
        public void run()
        {
            try
            {
                final IntensityInfo intensityInfos[];
                final double perimeter;
                final double volume;
                final int dim;

                if ((viewer != null) && (viewer.getSequence() == sequence) && (sequence != null)
                        && (roi instanceof ROI2D))
                {
                    final ROI2D roi2d = (ROI2D) roi;

                    // better to use ROI position ?
                    // roi2d.setZ(viewer.getZ());
                    // roi2d.setT(viewer.getT());

                    final int sizeC = sequence.getSizeC();
                    intensityInfos = new IntensityInfo[sizeC];

                    for (int c = 0; c < sizeC; c++)
                    {
                        roi2d.setC(c);
                        intensityInfos[c] = ROIUtil.getIntensityInfo(sequence, roi2d);
                    }
                }
                else
                    intensityInfos = null;

                if ((sequence != null) && (roi != null))
                {
                    perimeter = roi.getPerimeter();
                    volume = roi.getVolume();
                    dim = roi.getDimension();
                }
                else
                {
                    perimeter = -1;
                    volume = -1;
                    dim = 1;
                }

                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshIntensityInfos(intensityInfos);
                        refreshInfos(sequence, perimeter, volume, dim);
                    }
                });
            }
            catch (Exception e)
            {
                // we can have some exception here as this is an asynch process (just ignore)

            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -7796039523699617091L;

    /**
     * GUI
     */
    private JLabel perimeterValueLabel;
    private JLabel volumeValueLabel;
    private JLabel lblMinIntensity;
    private JLabel lblMeanIntensity;
    private JLabel lblMaxIntensity;
    private JLabel intensityMinLabel;
    private JLabel intensityMeanLabel;
    private JLabel intensityMaxLabel;
    private Component horizontalStrut;

    /**
     * Internals
     */
    final private SingleProcessor processor;

    public RoiExtraInfoPanel()
    {
        super();

        initialize();

        processor = new SingleProcessor(true, "ROI infos calculator");
        processor.setPriority(Thread.MIN_PRIORITY);

        refreshIntensityInfos(null);
        refreshInfos(null, -1, -1, 1);

        validate();
    }

    void initialize()
    {
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {80, 46, 10, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gbl_panel.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gbl_panel);

        JLabel lblPerimeter = new JLabel("Perimeter");
        lblPerimeter.setToolTipText("Perimeter of selected ROI");
        GridBagConstraints gbc_lblPerimeter = new GridBagConstraints();
        gbc_lblPerimeter.anchor = GridBagConstraints.EAST;
        gbc_lblPerimeter.insets = new Insets(0, 0, 5, 5);
        gbc_lblPerimeter.gridx = 0;
        gbc_lblPerimeter.gridy = 0;
        add(lblPerimeter, gbc_lblPerimeter);

        perimeterValueLabel = new JLabel("0");
        GridBagConstraints gbc_perimeterValueLabel = new GridBagConstraints();
        gbc_perimeterValueLabel.anchor = GridBagConstraints.EAST;
        gbc_perimeterValueLabel.insets = new Insets(0, 0, 5, 5);
        gbc_perimeterValueLabel.gridx = 1;
        gbc_perimeterValueLabel.gridy = 0;
        add(perimeterValueLabel, gbc_perimeterValueLabel);

        horizontalStrut = Box.createHorizontalStrut(10);
        GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
        gbc_horizontalStrut.insets = new Insets(0, 0, 5, 0);
        gbc_horizontalStrut.gridx = 2;
        gbc_horizontalStrut.gridy = 0;
        add(horizontalStrut, gbc_horizontalStrut);

        JLabel lblVolume = new JLabel("Volume");
        lblVolume.setToolTipText("Volume of selected ROI");
        GridBagConstraints gbc_lblVolume = new GridBagConstraints();
        gbc_lblVolume.anchor = GridBagConstraints.EAST;
        gbc_lblVolume.insets = new Insets(0, 0, 5, 5);
        gbc_lblVolume.gridx = 0;
        gbc_lblVolume.gridy = 1;
        add(lblVolume, gbc_lblVolume);

        volumeValueLabel = new JLabel("0");
        GridBagConstraints gbc_volumeValueLabel = new GridBagConstraints();
        gbc_volumeValueLabel.anchor = GridBagConstraints.EAST;
        gbc_volumeValueLabel.insets = new Insets(0, 0, 5, 5);
        gbc_volumeValueLabel.gridx = 1;
        gbc_volumeValueLabel.gridy = 1;
        add(volumeValueLabel, gbc_volumeValueLabel);

        lblMinIntensity = new JLabel("Min intensity");
        lblMinIntensity.setToolTipText("Minimum pixel intensity of selected ROI");
        GridBagConstraints gbc_lblMinIntensity = new GridBagConstraints();
        gbc_lblMinIntensity.anchor = GridBagConstraints.EAST;
        gbc_lblMinIntensity.insets = new Insets(0, 0, 5, 5);
        gbc_lblMinIntensity.gridx = 0;
        gbc_lblMinIntensity.gridy = 2;
        add(lblMinIntensity, gbc_lblMinIntensity);

        intensityMinLabel = new JLabel();
        intensityMinLabel.setText("0");
        GridBagConstraints gbc_label_3 = new GridBagConstraints();
        gbc_label_3.anchor = GridBagConstraints.EAST;
        gbc_label_3.insets = new Insets(0, 0, 5, 5);
        gbc_label_3.gridx = 1;
        gbc_label_3.gridy = 2;
        add(intensityMinLabel, gbc_label_3);

        lblMeanIntensity = new JLabel("Mean intensity");
        lblMeanIntensity.setToolTipText("Mean pixel intensity of selected ROI");
        GridBagConstraints gbc_lblMeanIntensity = new GridBagConstraints();
        gbc_lblMeanIntensity.anchor = GridBagConstraints.EAST;
        gbc_lblMeanIntensity.insets = new Insets(0, 0, 5, 5);
        gbc_lblMeanIntensity.gridx = 0;
        gbc_lblMeanIntensity.gridy = 3;
        add(lblMeanIntensity, gbc_lblMeanIntensity);

        intensityMeanLabel = new JLabel();
        intensityMeanLabel.setText("0");
        GridBagConstraints gbc_label_4 = new GridBagConstraints();
        gbc_label_4.anchor = GridBagConstraints.EAST;
        gbc_label_4.insets = new Insets(0, 0, 5, 5);
        gbc_label_4.gridx = 1;
        gbc_label_4.gridy = 3;
        add(intensityMeanLabel, gbc_label_4);

        lblMaxIntensity = new JLabel("Max intensity");
        lblMaxIntensity.setToolTipText("Maximum pixel intensity of selected ROI");
        GridBagConstraints gbc_lblMaxIntensity = new GridBagConstraints();
        gbc_lblMaxIntensity.anchor = GridBagConstraints.EAST;
        gbc_lblMaxIntensity.insets = new Insets(0, 0, 0, 5);
        gbc_lblMaxIntensity.gridx = 0;
        gbc_lblMaxIntensity.gridy = 4;
        add(lblMaxIntensity, gbc_lblMaxIntensity);

        intensityMaxLabel = new JLabel();
        intensityMaxLabel.setText("0");
        GridBagConstraints gbc_label_5 = new GridBagConstraints();
        gbc_label_5.insets = new Insets(0, 0, 0, 5);
        gbc_label_5.anchor = GridBagConstraints.EAST;
        gbc_label_5.gridx = 1;
        gbc_label_5.gridy = 4;
        add(intensityMaxLabel, gbc_label_5);
    }

    public void refresh(Sequence sequence, ROI roi)
    {
        processor.addTask(new RoiInfosCalculator(sequence, roi));
    }

    /**
     * Update perimeter and volume informations.<br>
     * Default unit is micrometer.
     */
    void refreshInfos(Sequence sequence, double perimeter, double volume, int roiDim)
    {
        if ((sequence == null) || (perimeter == -1d))
        {
            perimeterValueLabel.setText("");
            perimeterValueLabel.setToolTipText("");
        }
        else
        {
            perimeterValueLabel.setText(sequence.calculateSize(perimeter, roiDim - 1, 5));
            perimeterValueLabel.setToolTipText(StringUtil.toString(perimeter) + " pixel(s)");
        }

        if ((sequence == null) || (volume == -1d))
        {
            volumeValueLabel.setText("");
            volumeValueLabel.setToolTipText("");
        }
        else
        {
            volumeValueLabel.setText(sequence.calculateSize(volume, roiDim, 5));
            volumeValueLabel.setToolTipText(StringUtil.toString(volume) + " pixel(s)");
        }
    }

    void refreshIntensityInfos(IntensityInfo values[])
    {
        if (values != null)
        {
            String min;
            String mean;
            String max;

            if (values.length > 1)
            {
                min = "[";
                mean = "[";
                max = "[";
            }
            else
            {
                min = "";
                mean = "";
                max = "";
            }

            String mint = "";
            String meant = "";
            String maxt = "";

            for (int ch = 0; ch < values.length - 1; ch++)
            {
                final IntensityInfo info = values[ch];

                min += StringUtil.toString(MathUtil.roundSignificant(info.minIntensity, 5)) + "  ";
                mean += StringUtil.toString(MathUtil.roundSignificant(info.meanIntensity, 5)) + "  ";
                max += StringUtil.toString(MathUtil.roundSignificant(info.maxIntensity, 5)) + "  ";

                mint += "Channel " + ch + ": " + StringUtil.toString(MathUtil.roundSignificant(info.minIntensity, 8))
                        + "   ";
                meant += "Channel " + ch + ": " + StringUtil.toString(MathUtil.roundSignificant(info.meanIntensity, 8))
                        + "   ";
                maxt += "Channel " + ch + ": " + StringUtil.toString(MathUtil.roundSignificant(info.maxIntensity, 8))
                        + "    ";
            }

            int ch = values.length - 1;
            final IntensityInfo info = values[ch];

            min += StringUtil.toString(MathUtil.roundSignificant(info.minIntensity, 5));
            mean += StringUtil.toString(MathUtil.roundSignificant(info.meanIntensity, 5));
            max += StringUtil.toString(MathUtil.roundSignificant(info.maxIntensity, 5));

            mint += "Channel " + ch + ": " + StringUtil.toString(info.minIntensity);
            meant += "Channel " + ch + ": " + StringUtil.toString(info.meanIntensity);
            maxt += "Channel " + ch + ": " + StringUtil.toString(info.maxIntensity);

            if (values.length > 1)
            {
                min += "]";
                mean += "]";
                max += "]";
            }

            intensityMinLabel.setText(min);
            intensityMinLabel.setToolTipText(mint);
            intensityMeanLabel.setText(mean);
            intensityMeanLabel.setToolTipText(meant);
            intensityMaxLabel.setText(max);
            intensityMaxLabel.setToolTipText(maxt);
        }
        else
        {
            intensityMinLabel.setText("");
            intensityMinLabel.setToolTipText("");
            intensityMeanLabel.setText("");
            intensityMeanLabel.setToolTipText("");
            intensityMaxLabel.setText("");
            intensityMaxLabel.setToolTipText("");
        }
    }
}

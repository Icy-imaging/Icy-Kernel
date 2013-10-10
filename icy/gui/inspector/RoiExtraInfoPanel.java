/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.inspector;

import icy.gui.viewer.Viewer;
import icy.image.IntensityInfo;
import icy.math.MathUtil;
import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.StringUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class RoiExtraInfoPanel extends JPanel
{
    private class RoiInfosCalculator implements Runnable
    {
        final Viewer viewer;
        final List<ROI> rois;

        public RoiInfosCalculator(Viewer viewer, List<ROI> rois)
        {
            super();

            this.viewer = viewer;
            this.rois = rois;
        }

        @Override
        public void run()
        {
            try
            {
                final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;
                final IntensityInfo intensityInfos[];
                final double perimeter;
                final double volume;
                final int dim;

                if ((sequence != null) && (rois != null) && (rois.size() > 0))
                {
                    final int sizeC = sequence.getSizeC();

                    intensityInfos = new IntensityInfo[sizeC];
                    for (int c = 0; c < sizeC; c++)
                        intensityInfos[c] = new IntensityInfo();

                    // calculate sum of intensity infos
                    for (ROI roi : rois)
                    {
                        final Rectangle5D bounds = roi.getBounds5D().createIntersection(sequence.getBounds5D());
                        // better to use canvas position ?
                        // viewer.getZ();
                        // viewer.getT();

                        for (int c = (int) bounds.getMinC(); c < bounds.getMaxC(); c++)
                        {
                            final IntensityInfo inf = ROIUtil.getIntensityInfo(sequence, roi, -1, -1, c);
                            final IntensityInfo sum = intensityInfos[c];

                            sum.minIntensity += inf.minIntensity;
                            sum.meanIntensity += inf.meanIntensity;
                            sum.maxIntensity += inf.maxIntensity;
                        }
                    }

                    // calculate sum of perimeter and volume
                    dim = rois.get(0).getDimension();
                    double p = 0d;
                    double v = 0d;
                    for (ROI roi : rois)
                    {
                        if (roi.getDimension() != dim)
                        {
                            p = -1d;
                            v = -1d;
                            break;
                        }

                        p += roi.getNumberOfContourPoints();
                        v += roi.getNumberOfPoints();
                    }

                    perimeter = p;
                    volume = v;
                }
                else
                {
                    intensityInfos = null;
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
            catch (Throwable e)
            {
                // we can have some exception here as this is an asynch process (just ignore)
                if (e instanceof OutOfMemoryError)
                {
                    System.err.println("Cannot compute ROI infos: Not enought memory !");

                    // clear ROI infos to not give wrong values...
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            refreshIntensityInfos(null);
                            refreshInfos(null, -1, -1, 0);
                        }
                    });
                }
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
    private JLabel perimeterLabel;
    private JLabel areaLabel;
    private JLabel perimeterValueLabel;
    private JLabel areaValueLabel;
    private JLabel lblMinIntensity;
    private JLabel lblMeanIntensity;
    private JLabel lblMaxIntensity;
    private JLabel intensityMinLabel;
    private JLabel intensityMeanLabel;
    private JLabel intensityMaxLabel;

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
        processor.setKeepAliveTime(5, TimeUnit.SECONDS);

        refreshIntensityInfos(null);
        refreshInfos(null, -1, -1, 0);

        validate();
    }

    void initialize()
    {
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {80, 46, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gbl_panel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gbl_panel);

        perimeterLabel = new JLabel("Perimeter");
        perimeterLabel.setToolTipText("Total perimeter of selected ROI");
        GridBagConstraints gbc_perimeterLabel = new GridBagConstraints();
        gbc_perimeterLabel.anchor = GridBagConstraints.EAST;
        gbc_perimeterLabel.insets = new Insets(0, 0, 5, 5);
        gbc_perimeterLabel.gridx = 0;
        gbc_perimeterLabel.gridy = 0;
        add(perimeterLabel, gbc_perimeterLabel);

        perimeterValueLabel = new JLabel("0");
        GridBagConstraints gbc_perimeterValueLabel = new GridBagConstraints();
        gbc_perimeterValueLabel.anchor = GridBagConstraints.EAST;
        gbc_perimeterValueLabel.insets = new Insets(0, 0, 5, 0);
        gbc_perimeterValueLabel.gridx = 1;
        gbc_perimeterValueLabel.gridy = 0;
        add(perimeterValueLabel, gbc_perimeterValueLabel);

        areaLabel = new JLabel("Area");
        areaLabel.setToolTipText("Total area of selected ROI");
        GridBagConstraints gbc_areaLabel = new GridBagConstraints();
        gbc_areaLabel.anchor = GridBagConstraints.EAST;
        gbc_areaLabel.insets = new Insets(0, 0, 5, 5);
        gbc_areaLabel.gridx = 0;
        gbc_areaLabel.gridy = 1;
        add(areaLabel, gbc_areaLabel);

        areaValueLabel = new JLabel("0");
        GridBagConstraints gbc_areaValueLabel = new GridBagConstraints();
        gbc_areaValueLabel.anchor = GridBagConstraints.EAST;
        gbc_areaValueLabel.insets = new Insets(0, 0, 5, 0);
        gbc_areaValueLabel.gridx = 1;
        gbc_areaValueLabel.gridy = 1;
        add(areaValueLabel, gbc_areaValueLabel);

        lblMinIntensity = new JLabel("Min intensity");
        lblMinIntensity.setToolTipText("Minimum pixel intensity of selected ROI(s)");
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
        gbc_label_3.insets = new Insets(0, 0, 5, 0);
        gbc_label_3.gridx = 1;
        gbc_label_3.gridy = 2;
        add(intensityMinLabel, gbc_label_3);

        lblMeanIntensity = new JLabel("Mean intensity");
        lblMeanIntensity.setToolTipText("Mean pixel intensity of selected ROI(s)");
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
        gbc_label_4.insets = new Insets(0, 0, 5, 0);
        gbc_label_4.gridx = 1;
        gbc_label_4.gridy = 3;
        add(intensityMeanLabel, gbc_label_4);

        lblMaxIntensity = new JLabel("Max intensity");
        lblMaxIntensity.setToolTipText("Maximum pixel intensity of selected ROI(s)");
        GridBagConstraints gbc_lblMaxIntensity = new GridBagConstraints();
        gbc_lblMaxIntensity.anchor = GridBagConstraints.EAST;
        gbc_lblMaxIntensity.insets = new Insets(0, 0, 0, 5);
        gbc_lblMaxIntensity.gridx = 0;
        gbc_lblMaxIntensity.gridy = 4;
        add(lblMaxIntensity, gbc_lblMaxIntensity);

        intensityMaxLabel = new JLabel();
        intensityMaxLabel.setText("0");
        GridBagConstraints gbc_label_5 = new GridBagConstraints();
        gbc_label_5.anchor = GridBagConstraints.EAST;
        gbc_label_5.gridx = 1;
        gbc_label_5.gridy = 4;
        add(intensityMaxLabel, gbc_label_5);
    }

    public void refresh(Viewer viewer, List<ROI> roi)
    {
        processor.addTask(new RoiInfosCalculator(viewer, roi));
    }

    /**
     * Update perimeter and volume informations.<br>
     * Default unit is micrometer.
     */
    void refreshInfos(Sequence sequence, double perimeter, double volume, int roiDim)
    {
        switch (roiDim)
        {
            case 1:
                perimeterLabel.setText("Length");
                perimeterLabel.setToolTipText("Total length of selected ROI(s)");
                areaLabel.setText("Perimeter");
                areaLabel.setToolTipText("Total perimeter of selected ROI(s)");
                break;
            case 3:
                perimeterLabel.setText("Surface area");
                perimeterLabel.setToolTipText("Total surface area of selected ROI(s)");
                areaLabel.setText("Volume");
                areaLabel.setToolTipText("Total volume of selected ROI(s)");
                break;
            default:
                perimeterLabel.setText("Perimeter");
                perimeterLabel.setToolTipText("Total perimeter of selected ROI(s)");
                areaLabel.setText("Area");
                areaLabel.setToolTipText("Total area of selected ROI(s)");
                break;
        }

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
            areaValueLabel.setText("");
            areaValueLabel.setToolTipText("");
        }
        else
        {
            areaValueLabel.setText(sequence.calculateSize(volume, roiDim, 5));
            areaValueLabel.setToolTipText(StringUtil.toString(volume) + " pixel(s)");
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

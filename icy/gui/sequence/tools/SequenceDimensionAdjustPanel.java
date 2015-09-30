/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Icy. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package icy.gui.sequence.tools;

import icy.gui.component.RangeComponent;
import icy.gui.component.sequence.SequencePreviewPanel;
import icy.sequence.DimensionId;
import icy.sequence.SequenceModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EventListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SequenceDimensionAdjustPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 5670426025630944193L;

    public interface RangeChangeListener extends EventListener
    {
        public void rangeChanged();
    }

    protected DimensionId dim;

    // GUI
    protected SequencePreviewPanel sequencePreviewPanel;
    protected RangeComponent range;
    private JPanel extractionRulePanel;
    protected JSpinner extractSpinner;
    protected JSpinner loopSpinner;

    // internal
    double oldLow;
    double oldHigh;

    /**
     * Create the panel.
     */
    public SequenceDimensionAdjustPanel(DimensionId dim)
    {
        super();

        if ((dim != DimensionId.Z) && (dim != DimensionId.T))
            throw new IllegalArgumentException("Only Z or T dimension allowed");

        this.dim = dim;

        initialize();

        if (dim == DimensionId.Z)
        {
            sequencePreviewPanel.getMainPanel().setBorder(
                    new TitledBorder(null, "Select Z interval", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            range = new RangeComponent(SwingConstants.VERTICAL);
            range.setToolTipText("Select Z interval");
        }
        else
        {
            sequencePreviewPanel.getMainPanel().setBorder(
                    new TitledBorder(null, "Select T interval", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            range = new RangeComponent(SwingConstants.HORIZONTAL);
            range.setToolTipText("Select T interval");
        }

        range.setMinMaxStep(0, 0, 1);
        range.setVisible(false);
        range.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                final double low = range.getLow();
                final double high = range.getHigh();

                if (oldLow != low)
                {
                    // low value changed
                    if (SequenceDimensionAdjustPanel.this.dim == DimensionId.Z)
                        sequencePreviewPanel.setPositionZ((int) low);
                    else
                        sequencePreviewPanel.setPositionT((int) low);
                }
                else if (oldHigh != high)
                {
                    // high value changed
                    if (SequenceDimensionAdjustPanel.this.dim == DimensionId.Z)
                        sequencePreviewPanel.setPositionZ((int) high);
                    else
                        sequencePreviewPanel.setPositionT((int) high);
                }

                oldLow = low;
                oldHigh = high;

                fireRangeChangedEvent();
            }
        });

        if (dim == DimensionId.Z)
        {
            // replace z slider by z range component
            final JPanel panel = sequencePreviewPanel.getZPanel();
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(range, BorderLayout.CENTER);
        }
        else
        {
            // replace t slider by t range component
            final JPanel panel = sequencePreviewPanel.getTPanel();
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(range, BorderLayout.CENTER);
        }

        extractSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                final SpinnerNumberModel model = (SpinnerNumberModel) loopSpinner.getModel();
                final int currentValue = ((Integer) model.getValue()).intValue();
                final int min = getExtractValue();

                // adjust minimum value
                loopSpinner.setModel(new SpinnerNumberModel(Math.max(min, currentValue), min, (int) range.getMax(), 1));

                fireRangeChangedEvent();
            }
        });
        loopSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                fireRangeChangedEvent();
            }
        });
    }

    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        sequencePreviewPanel = new SequencePreviewPanel();
        add(sequencePreviewPanel, BorderLayout.CENTER);

        extractionRulePanel = new JPanel();
        extractionRulePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Extraction rule",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(extractionRulePanel, BorderLayout.SOUTH);
        GridBagLayout gbl_extractionRulePanel = new GridBagLayout();
        gbl_extractionRulePanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0};
        gbl_extractionRulePanel.rowHeights = new int[] {20, 0};
        gbl_extractionRulePanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 2.0, Double.MIN_VALUE};
        gbl_extractionRulePanel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        extractionRulePanel.setLayout(gbl_extractionRulePanel);

        JLabel lblKeep = new JLabel("Keep");
        GridBagConstraints gbc_lblKeep = new GridBagConstraints();
        gbc_lblKeep.anchor = GridBagConstraints.EAST;
        gbc_lblKeep.insets = new Insets(0, 0, 0, 5);
        gbc_lblKeep.gridx = 0;
        gbc_lblKeep.gridy = 0;
        extractionRulePanel.add(lblKeep, gbc_lblKeep);

        extractSpinner = new JSpinner();
        extractSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        extractSpinner
                .setToolTipText("Number of frames to keep every N frames. 'Keep 1 every 2' means keep every other frame.");
        GridBagConstraints gbc_extractSpinner = new GridBagConstraints();
        gbc_extractSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_extractSpinner.anchor = GridBagConstraints.NORTH;
        gbc_extractSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_extractSpinner.gridx = 1;
        gbc_extractSpinner.gridy = 0;
        extractionRulePanel.add(extractSpinner, gbc_extractSpinner);

        JLabel lblEvery = new JLabel("every");
        GridBagConstraints gbc_lblEvery = new GridBagConstraints();
        gbc_lblEvery.anchor = GridBagConstraints.EAST;
        gbc_lblEvery.insets = new Insets(0, 0, 0, 5);
        gbc_lblEvery.gridx = 2;
        gbc_lblEvery.gridy = 0;
        extractionRulePanel.add(lblEvery, gbc_lblEvery);

        loopSpinner = new JSpinner();
        loopSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        loopSpinner.setToolTipText("Size for the extraction rule loop.");
        GridBagConstraints gbc_loopSpinner = new GridBagConstraints();
        gbc_loopSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_loopSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_loopSpinner.anchor = GridBagConstraints.NORTH;
        gbc_loopSpinner.gridx = 3;
        gbc_loopSpinner.gridy = 0;
        extractionRulePanel.add(loopSpinner, gbc_loopSpinner);
    }

    public DimensionId getDimensionId()
    {
        return dim;
    }

    public boolean isPreviewVisible()
    {
        return sequencePreviewPanel.isVisible();
    }

    public void setPreviewVisible(boolean value)
    {
        sequencePreviewPanel.setVisible(value);
    }

    private void setMax(int value)
    {
        range.setMinMaxStep(0, value, 1);
        range.setLowHigh(0, value);
        range.setVisible(value > 0);

        extractSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(value, 1), 1));
        loopSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(value, 1), 1));
    }

    public SequenceModel getModel()
    {
        return sequencePreviewPanel.getModel();

    }

    public void setModel(SequenceModel model)
    {
        if (getModel() != model)
        {
            sequencePreviewPanel.setModel(model);
            dimensionChangedInternal();
        }
    }

    public void dimensionChanged()
    {
        dimensionChangedInternal();
        sequencePreviewPanel.dimensionChanged();
    }

    private void dimensionChangedInternal()
    {
        final SequenceModel model = getModel();

        if (model != null)
        {
            if (dim == DimensionId.Z)
                setMax(Math.max(0, model.getSizeZ() - 1));
            else
                setMax(Math.max(0, model.getSizeT() - 1));
        }
        else
            setMax(0);
    }

    public void imageChanged()
    {
        sequencePreviewPanel.imageChanged();
    }

    public int getRangeLow()
    {
        return (int) range.getLow();
    }

    public int getRangeHigh()
    {
        return (int) range.getHigh();
    }

    public boolean isIndexSelected(int index)
    {
        if (index < getRangeLow())
            return false;
        if (index > getRangeHigh())
            return false;

        return ((index - getRangeLow()) % getLoopValue()) < getExtractValue();
    }

    public int getExtractValue()
    {
        return ((Integer) extractSpinner.getValue()).intValue();
    }

    public int getLoopValue()
    {
        return ((Integer) loopSpinner.getValue()).intValue();
    }

    protected void fireRangeChangedEvent()
    {
        for (RangeChangeListener listener : getListeners(RangeChangeListener.class))
            listener.rangeChanged();
    }

    public void addRangeChangeListener(RangeChangeListener listener)
    {
        listenerList.add(RangeChangeListener.class, listener);
    }

    public void removeRangeChangeListener(RangeChangeListener listener)
    {
        listenerList.remove(RangeChangeListener.class, listener);
    }
}

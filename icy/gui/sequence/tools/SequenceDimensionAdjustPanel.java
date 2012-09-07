package icy.gui.sequence.tools;

import icy.gui.component.RangeComponent;
import icy.gui.component.sequence.SequencePreviewPanel;
import icy.sequence.DimensionId;
import icy.sequence.SequenceModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
                final int currentMin = ((Integer) model.getMinimum()).intValue();
                final int currentValue = ((Integer) model.getValue()).intValue();
                final int min = getExtractValue();

                // adjust minimum value
                if (currentMin < min)
                    loopSpinner.setModel(new SpinnerNumberModel(Math.max(min, currentValue), min, (int) range.getMax(),
                            1));

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
        FlowLayout fl_extractionRulePanel = new FlowLayout();
        fl_extractionRulePanel.setAlignment(FlowLayout.LEADING);
        extractionRulePanel.setLayout(fl_extractionRulePanel);

        JLabel lblKeep = new JLabel("Keep");
        extractionRulePanel.add(lblKeep);

        extractSpinner = new JSpinner();
        extractSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        extractSpinner
                .setToolTipText("Number of frames to keep every N frames. 'Keep 1 every 2' means keep every other frame.");
        extractionRulePanel.add(extractSpinner);

        JLabel lblEvery = new JLabel("every");
        extractionRulePanel.add(lblEvery);

        loopSpinner = new JSpinner();
        loopSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        loopSpinner.setToolTipText("Size for the extraction rule loop.");
        extractionRulePanel.add(loopSpinner);
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
                setMax(model.getSizeZ());
            else
                setMax(model.getSizeT());
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

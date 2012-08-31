package icy.gui.component.sequence;

import icy.gui.component.RangeComponent;
import icy.sequence.DimensionId;
import icy.sequence.SequenceModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SequenceRangePreviewPanel extends JPanel
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
    protected JSpinner keepSpinner;
    protected JSpinner ignoreSpinner;

    // internal
    double oldLow;
    double oldHigh;

    /**
     * Create the panel.
     */
    public SequenceRangePreviewPanel(DimensionId dim)
    {
        super();

        if ((dim != DimensionId.Z) && (dim != DimensionId.T))
            throw new IllegalArgumentException("Only Z or T dimension allowed");

        this.dim = dim;

        initialize();

        if (dim == DimensionId.Z)
        {
            sequencePreviewPanel.mainPanel.setBorder(new TitledBorder(null, "Select Z interval", TitledBorder.LEADING,
                    TitledBorder.TOP, null, null));
            range = new RangeComponent(SwingConstants.VERTICAL);
            range.setToolTipText("Select Z interval");
        }
        else
        {
            sequencePreviewPanel.mainPanel.setBorder(new TitledBorder(null, "Select T interval", TitledBorder.LEADING,
                    TitledBorder.TOP, null, null));
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
                    if (SequenceRangePreviewPanel.this.dim == DimensionId.Z)
                        sequencePreviewPanel.setPositionZ((int) low);
                    else
                        sequencePreviewPanel.setPositionT((int) low);
                }
                else if (oldHigh != high)
                {
                    // high value changed
                    if (SequenceRangePreviewPanel.this.dim == DimensionId.Z)
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
            panel.add(range);
        }
        else
        {
            // replace t slider by t range component
            final JPanel panel = sequencePreviewPanel.getTPanel();
            panel.removeAll();
            panel.add(range);
        }

        final ChangeListener changeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                fireRangeChangedEvent();
            }
        };

        keepSpinner.addChangeListener(changeListener);
        ignoreSpinner.addChangeListener(changeListener);
    }

    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        sequencePreviewPanel = new SequencePreviewPanel();
        add(sequencePreviewPanel, BorderLayout.CENTER);

        extractionRulePanel = new JPanel();
        extractionRulePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Loop extraction rule", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(extractionRulePanel, BorderLayout.SOUTH);

        FlowLayout fl_extractionRulePanel = (FlowLayout) extractionRulePanel.getLayout();
        fl_extractionRulePanel.setAlignment(FlowLayout.LEADING);

        JLabel label = new JLabel("Keep");
        label.setToolTipText("Number of image to keep");
        extractionRulePanel.add(label);

        keepSpinner = new JSpinner();
        keepSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        keepSpinner.setToolTipText("Number of image to keep");
        extractionRulePanel.add(keepSpinner);

        Component horizontalStrut_1 = Box.createHorizontalStrut(8);
        extractionRulePanel.add(horizontalStrut_1);

        JLabel label_1 = new JLabel("Ignore");
        label_1.setToolTipText("Number of image to ignore");
        extractionRulePanel.add(label_1);

        ignoreSpinner = new JSpinner();
        ignoreSpinner.setToolTipText("Number of image to ignore");
        extractionRulePanel.add(ignoreSpinner);
    }

    public boolean isPreviewVisible()
    {
        return sequencePreviewPanel.isVisible();
    }

    public void setPreviewVisible(boolean value)
    {
        sequencePreviewPanel.setVisible(value);
    }

    private void setMaxZ(int value)
    {
        if (dim == DimensionId.Z)
        {
            range.setMinMaxStep(0, value, 1);
            range.setLowHigh(0, value);
            range.setVisible(value > 0);

            if (value > 0)
            {
                keepSpinner.setModel(new SpinnerNumberModel(1, 1, value + 1, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, value, 1));
            }
            else
            {
                keepSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
            }
        }
    }

    private void setMaxT(int value)
    {
        if (dim == DimensionId.T)
        {
            range.setMinMaxStep(0, value, 1);
            range.setLowHigh(0, value);
            range.setVisible(value > 0);

            if (value > 0)
            {
                keepSpinner.setModel(new SpinnerNumberModel(1, 1, value + 1, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, value, 1));
            }
            else
            {
                keepSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
            }
        }
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
            setMaxZ(model.getSizeZ() - 1);
            setMaxT(model.getSizeT() - 1);
        }
        else
        {
            setMaxZ(0);
            setMaxT(0);
        }
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

        return ((index - getRangeLow()) % getLoopValue()) < getKeepValue();
    }

    public int getKeepValue()
    {
        return ((Integer) keepSpinner.getValue()).intValue();
    }

    public int getIgnoreValue()
    {
        return ((Integer) ignoreSpinner.getValue()).intValue();
    }

    public int getLoopValue()
    {
        return getKeepValue() + getIgnoreValue();
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

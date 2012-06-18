package icy.gui.component.sequence;

import icy.gui.component.RangeComponent;
import icy.sequence.SequenceModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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

    protected boolean dimZ;

    protected SequencePreviewPanel sequencePreviewPanel;
    protected RangeComponent zRange;
    protected RangeComponent tRange;
    private JPanel extractionRulePanel;
    protected JCheckBox useExtractionRule;
    protected JSpinner keepSpinner;
    protected JSpinner ignoreSpinner;

    /**
     * Create the panel.
     */
    public SequenceRangePreviewPanel(boolean dimZ)
    {
        super();

        this.dimZ = dimZ;

        initialize();

        final ChangeListener changeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                fireRangeChangedEvent();
            }
        };

        zRange = new RangeComponent(SwingConstants.VERTICAL);
        zRange.setMinMaxStep(0, 0, 1);
        zRange.setVisible(false);
        zRange.setToolTipText("Select Z range");
        zRange.addChangeListener(changeListener);

        tRange = new RangeComponent();
        tRange.setMinMaxStep(0, 0, 1);
        tRange.setVisible(false);
        tRange.setToolTipText("Select T range");
        tRange.addChangeListener(changeListener);

        if (dimZ)
        {
            // replace z slider by z range component
            final JPanel panel = sequencePreviewPanel.getZPanel();
            panel.removeAll();
            panel.add(zRange);
            // sequencePreviewPanel.add(zRange, BorderLayout.WEST);
        }
        else
        {
            // replace t slider by t range component
            final JPanel panel = sequencePreviewPanel.getTPanel();
            panel.removeAll();
            panel.add(tRange);
            // sequencePreviewPanel.add(tRange, BorderLayout.SOUTH);
        }

        useExtractionRule.setEnabled(false);
        useExtractionRule.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean selected = useExtractionRule.isSelected();

                keepSpinner.setEnabled(selected);
                ignoreSpinner.setEnabled(selected);
                fireRangeChangedEvent();
            }
        });

        keepSpinner.addChangeListener(changeListener);
        ignoreSpinner.addChangeListener(changeListener);
    }

    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        sequencePreviewPanel = new SequencePreviewPanel();
        add(sequencePreviewPanel, BorderLayout.CENTER);

        extractionRulePanel = new JPanel();
        add(extractionRulePanel, BorderLayout.SOUTH);

        FlowLayout fl_extractionRulePanel = (FlowLayout) extractionRulePanel.getLayout();
        fl_extractionRulePanel.setAlignment(FlowLayout.LEADING);

        useExtractionRule = new JCheckBox("Use extraction rule");
        useExtractionRule
                .setToolTipText("Permit to define a specific extraction loop rule (ex: keep 1, ignore 2, keep 1, ...)");
        extractionRulePanel.add(useExtractionRule);

        Component horizontalStrut = Box.createHorizontalStrut(8);
        extractionRulePanel.add(horizontalStrut);

        JLabel label = new JLabel("Keep");
        extractionRulePanel.add(label);

        keepSpinner = new JSpinner();
        keepSpinner.setToolTipText("Number of image to keep");
        keepSpinner.setEnabled(false);
        extractionRulePanel.add(keepSpinner);

        Component horizontalStrut_1 = Box.createHorizontalStrut(8);
        extractionRulePanel.add(horizontalStrut_1);

        JLabel label_1 = new JLabel("Ignore");
        extractionRulePanel.add(label_1);

        ignoreSpinner = new JSpinner();
        ignoreSpinner.setToolTipText("Number of image to ignore");
        ignoreSpinner.setEnabled(false);
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
        zRange.setMinMaxStep(0, value, 1);
        zRange.setLowHigh(0, value);
        zRange.setVisible(value > 0);

        if (dimZ)
        {
            if (value > 0)
            {
                useExtractionRule.setEnabled(true);
                keepSpinner.setModel(new SpinnerNumberModel(value, 1, value, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, value, 1));
            }
            else
            {
                useExtractionRule.setSelected(false);
                useExtractionRule.setEnabled(false);
                keepSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
            }
        }
    }

    private void setMaxT(int value)
    {
        tRange.setMinMaxStep(0, value, 1);
        tRange.setLowHigh(0, value);
        tRange.setVisible(value > 0);

        if (!dimZ)
        {
            if (value > 0)
            {
                useExtractionRule.setEnabled(true);
                keepSpinner.setModel(new SpinnerNumberModel(value, 1, value, 1));
                ignoreSpinner.setModel(new SpinnerNumberModel(0, 0, value, 1));
            }
            else
            {
                useExtractionRule.setSelected(false);
                useExtractionRule.setEnabled(false);
                keepSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
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

    public int getRangeMax()
    {
        if (dimZ)
            return (int) zRange.getMax();
        return (int) tRange.getMax();
    }

    public boolean isRangeIndexSelected(int index)
    {
        if (index < 0)
            return false;
        if (index >= getRangeMax())
            return false;

        return (index % getLoopValue() < getKeepValue());
    }

    public int getRangeNumSelected()
    {
        int result = getRangeMax() + 1;
        final int lv = getLoopValue();
        final int kv = getKeepValue();

        return ((result / lv) * kv) + Math.min(result % lv, kv);
    }

    public int getKeepValue()
    {
        if (useExtractionRule.isEnabled() && useExtractionRule.isSelected())
            return ((Integer) keepSpinner.getValue()).intValue();
        return getRangeMax() + 1;
    }

    public int getIgnoreValue()
    {
        if (useExtractionRule.isEnabled() && useExtractionRule.isSelected())
            return ((Integer) ignoreSpinner.getValue()).intValue();
        return 0;
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

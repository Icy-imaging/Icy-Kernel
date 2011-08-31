/**
 * 
 */
package icy.gui.component;

import javax.media.jai.util.Range;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class RangeComponent extends JPanel implements ChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7244476681262628392L;

    final private JSpinner startSpinner;
    final private JSpinner endSpinner;
    final private JLabel startLabel;
    final private JLabel endLabel;

    public RangeComponent()
    {
        super();

        startSpinner = new JSpinner();
        endSpinner = new JSpinner();

        startSpinner.addChangeListener(this);
        endSpinner.addChangeListener(this);

        startLabel = new JLabel("start");
        endLabel = new JLabel("end");

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(startLabel);
        add(startSpinner);
        add(new JLabel(" - "));
        add(endLabel);
        add(endSpinner);

        setLabelVisible(true);

        revalidate();
    }

    public RangeComponent(double start, double end, double step)
    {
        this();
        setAbsoluteRange(start, end, step);
    }

    public RangeComponent(int start, int end, int step)
    {
        this();
        setAbsoluteRange(start, end, step);
    }

    private SpinnerNumberModel getStartModel()
    {
        return (SpinnerNumberModel) startSpinner.getModel();
    }

    private SpinnerNumberModel getEndModel()
    {
        return (SpinnerNumberModel) endSpinner.getModel();
    }

    public Number getStart()
    {
        return getStartModel().getNumber();
    }

    public Number getEnd()
    {
        return getEndModel().getNumber();
    }

    public Range getRange()
    {
        final Number start = getStart();

        return new Range(start.getClass(), (Comparable<?>) start, (Comparable<?>) getEnd());
    }

    public void setAbsoluteRange(Range range)
    {
        startSpinner.setModel(new SpinnerNumberModel((Number) range.getMinValue(), range.getMinValue(), range
                .getMaxValue(), new Integer(1)));
        endSpinner.setModel(new SpinnerNumberModel((Number) range.getMaxValue(), range.getMinValue(), range
                .getMaxValue(), new Integer(1)));
    }

    public void setAbsoluteRange(int start, int end, int step)
    {
        startSpinner.setModel(new SpinnerNumberModel(start, start, end, step));
        endSpinner.setModel(new SpinnerNumberModel(end, start, end, step));
    }

    public void setAbsoluteRange(double start, double end, double step)
    {
        startSpinner.setModel(new SpinnerNumberModel(start, start, end, step));
        endSpinner.setModel(new SpinnerNumberModel(end, start, end, step));
    }

    public void setAbsoluteStart(int start)
    {
        getStartModel().setMinimum(Integer.valueOf(start));
    }

    public void setAbsoluteStart(double start)
    {
        getStartModel().setMinimum(new Double(start));
    }

    public void setAbsoluteEnd(int end)
    {
        getEndModel().setMinimum(Integer.valueOf(end));
    }

    public void setAbsoluteEnd(double end)
    {
        getEndModel().setMinimum(new Double(end));
    }

    public void setRange(Range range)
    {
        startSpinner.setValue(range.getMinValue());
        endSpinner.setValue(range.getMaxValue());
    }

    public void setRange(int start, int end)
    {
        startSpinner.setValue(Integer.valueOf(start));
        endSpinner.setValue(Integer.valueOf(end));
    }

    public void setRange(double start, double end)
    {
        startSpinner.setValue(new Double(start));
        endSpinner.setValue(new Double(end));
    }

    public void setLabelVisible(boolean value)
    {
        startLabel.setVisible(value);
        endLabel.setVisible(value);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        startSpinner.setEnabled(enabled);
        endSpinner.setEnabled(enabled);

        super.setEnabled(enabled);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == startSpinner)
        {
            final Comparable startValue = (Comparable) startSpinner.getValue();
            final Comparable endValue = (Comparable) endSpinner.getValue();

            if (endValue.compareTo(startValue) < 0)
                endSpinner.setValue(startValue);

            getEndModel().setMinimum(startValue);
        }
        else
        {
            final Comparable startValue = (Comparable) startSpinner.getValue();
            final Comparable endValue = (Comparable) endSpinner.getValue();

            if (startValue.compareTo(endValue) > 0)
                startSpinner.setValue(endValue);

            getStartModel().setMaximum(endValue);
        }
    }
}

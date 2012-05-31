/**
 * 
 */
package icy.gui.component;

import java.awt.BorderLayout;

import javax.swing.DefaultBoundedRangeModel;
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

    final private JSpinner lowSpinner;
    final private JSpinner highSpinner;
    final private RangeSlider slider;

    public RangeComponent(double min, double max, double step)
    {
        super();

        lowSpinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
        highSpinner = new JSpinner(new SpinnerNumberModel(max, min, max, step));
        slider = new RangeSlider();
        slider.setFocusPainted(false);
        updateSliderModel();

        lowSpinner.addChangeListener(this);
        highSpinner.addChangeListener(this);
        slider.addChangeListener(this);

        setLayout(new BorderLayout());

        add(lowSpinner, BorderLayout.WEST);
        add(slider, BorderLayout.CENTER);
        add(highSpinner, BorderLayout.EAST);

        validate();
    }

    public RangeComponent()
    {
        this(0d, 100d, 1d);
    }

    private SpinnerNumberModel getLowModel()
    {
        return (SpinnerNumberModel) lowSpinner.getModel();
    }

    private SpinnerNumberModel getHighModel()
    {
        return (SpinnerNumberModel) highSpinner.getModel();
    }

    private double getRange()
    {
        return getMax() - getMin();
    }

    private int getSliderRange()
    {
        return slider.getMaximum() - slider.getMinimum();
    }

    private int spinnerToSlider(double value)
    {
        final double spinnerRange = getRange();

        if (spinnerRange == 0)
            return 0;

        return (int) ((value - getMin()) * getSliderRange() / spinnerRange);
    }

    private double sliderToSpinner(int value)
    {
        final int sliderRange = getSliderRange();

        if (sliderRange == 0)
            return 0d;

        return (value * getRange() / sliderRange) + getMin();
    }

    private void updateSliderModel()
    {
        final int sliderRange = (int) Math.round(getRange() / getStep());

        slider.setModel(new DefaultBoundedRangeModel(0, 0, 0, sliderRange));
        slider.setLowerValue(spinnerToSlider(getLow()));
        slider.setUpperValue(spinnerToSlider(getHigh()));
    }

    public double getLow()
    {
        return getLowModel().getNumber().doubleValue();
    }

    public double getHigh()
    {
        return getHighModel().getNumber().doubleValue();
    }

    public void setLow(double value)
    {
        getLowModel().setValue(Double.valueOf(value));
    }

    public void setHigh(double value)
    {
        getHighModel().setValue(Double.valueOf(value));
    }

    /**
     * Return true if the range use integer number
     */
    public boolean isInteger()
    {
        final SpinnerNumberModel model = getLowModel();
        final Number value = model.getNumber();
        final Number step = model.getStepSize();

        return (value.doubleValue() == value.longValue()) && (step.doubleValue() == step.longValue());
    }

    public double getMin()
    {
        return ((Double) getLowModel().getMinimum()).doubleValue();
    }

    public double getMax()
    {
        return ((Double) getHighModel().getMaximum()).doubleValue();
    }

    public double getStep()
    {
        return getLowModel().getStepSize().doubleValue();
    }

    public void setMinMaxStep(double min, double max, double step)
    {
        final double low = Math.max(Math.min(getLow(), max), min);
        final double high = Math.max(Math.min(getHigh(), max), min);

        lowSpinner.setModel(new SpinnerNumberModel(low, min, max, step));
        highSpinner.setModel(new SpinnerNumberModel(high, min, max, step));

        updateSliderModel();
    }

    public void setMinMax(double min, double max)
    {
        setMinMaxStep(min, max, getStep());
    }

    public void setMin(double value)
    {
        setMinMaxStep(value, getMax(), getStep());
    }

    public void setMax(double value)
    {
        setMinMaxStep(getMin(), value, getStep());
    }

    public void setStep(double value)
    {
        setMinMaxStep(getMin(), getMax(), value);
    }

    public void setSliderVisible(boolean value)
    {
        if (value)
            add(slider, BorderLayout.CENTER);
        else
            add(new JLabel(" - "), BorderLayout.CENTER);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        lowSpinner.setEnabled(enabled);
        highSpinner.setEnabled(enabled);
        slider.setEnabled(enabled);

        super.setEnabled(enabled);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        final Object source = e.getSource();
        final double low = getLow();
        final double high = getHigh();

        if (source == lowSpinner)
        {
            slider.setLowerValue(spinnerToSlider(low));

            if (high < low)
                setHigh(low);

            getHighModel().setMinimum(Double.valueOf(low));
        }
        else if (source == highSpinner)
        {
            slider.setUpperValue(spinnerToSlider(high));

            if (low > high)
                setLow(high);

            getLowModel().setMaximum(Double.valueOf(high));
        }
        else if (source == slider)
        {
            setLow(sliderToSpinner(slider.getLowerValue()));
            setHigh(sliderToSpinner(slider.getUpperValue()));
        }
    }
}

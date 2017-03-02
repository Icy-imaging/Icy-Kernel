package icy.gui.component;

import icy.gui.component.NumberTextField.ValueChangeListener;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Component letting user to define a range (RangeSlider + 2 inputs fields)
 * 
 * @author Stephane
 */
public class Region2DComponent extends JPanel implements ValueChangeListener
{
    protected NumberTextField xStartField;
    protected NumberTextField yStartField;
    protected NumberTextField widthField;
    protected NumberTextField heightField;

    public Region2DComponent(double x, double y, double w, double h, boolean integer)
    {
        super();

        initialize();
        yStartField.setInteger(integer);
        widthField.setInteger(integer);
        heightField.setInteger(integer);
        xStartField.setNumericValue(x);
        yStartField.setNumericValue(y);
        widthField.setNumericValue(w);
        heightField.setNumericValue(h);

    }

    public Region2DComponent(double x, double y, double w, double h)
    {
        this(x, y, w, h, false);
    }

    public Region2DComponent(boolean integer)
    {
        this(0d, 0d, 100d, 100d, integer);
    }

    public Region2DComponent()
    {
        this(0d, 0d, 100d, 100d, false);
    }

    protected void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {40, 40, 0, 40, 40, 0};
        gridBagLayout.rowHeights = new int[] {0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        xStartField = new NumberTextField();
        xStartField.setToolTipText("Start X position of the region");

        xStartField.addValueListener(this);

        GridBagConstraints gbc_xStartField = new GridBagConstraints();
        gbc_xStartField.fill = GridBagConstraints.BOTH;
        gbc_xStartField.insets = new Insets(0, 0, 0, 5);
        gbc_xStartField.gridx = 0;
        gbc_xStartField.gridy = 0;
        add(xStartField, gbc_xStartField);
        yStartField = new NumberTextField();
        yStartField.setToolTipText("Start Y position of the region");
        yStartField.addValueListener(this);
        GridBagConstraints gbc_yStartField = new GridBagConstraints();
        gbc_yStartField.fill = GridBagConstraints.BOTH;
        gbc_yStartField.insets = new Insets(0, 0, 0, 5);
        gbc_yStartField.gridx = 1;
        gbc_yStartField.gridy = 0;
        add(yStartField, gbc_yStartField);
        heightField = new NumberTextField();
        heightField.setToolTipText("Height of the region");
        heightField.addValueListener(this);
        widthField = new NumberTextField();
        widthField.setToolTipText("Width of the region");
        widthField.addValueListener(this);

        JLabel sepLabel = new JLabel("-");
        GridBagConstraints gbc_sepLabel = new GridBagConstraints();
        gbc_sepLabel.fill = GridBagConstraints.VERTICAL;
        gbc_sepLabel.insets = new Insets(0, 0, 0, 5);
        gbc_sepLabel.gridx = 2;
        gbc_sepLabel.gridy = 0;
        add(sepLabel, gbc_sepLabel);
        GridBagConstraints gbc_widthField = new GridBagConstraints();
        gbc_widthField.fill = GridBagConstraints.BOTH;
        gbc_widthField.insets = new Insets(0, 0, 0, 5);
        gbc_widthField.gridx = 3;
        gbc_widthField.gridy = 0;
        add(widthField, gbc_widthField);
        GridBagConstraints gbc_heightField = new GridBagConstraints();
        gbc_heightField.fill = GridBagConstraints.BOTH;
        gbc_heightField.gridx = 4;
        gbc_heightField.gridy = 0;
        add(heightField, gbc_heightField);

        validate();
    }

    /**
     * Set 2D region values.
     */
    public void setRegion(double x, double y, double w, double h)
    {
        xStartField.setNumericValue(x);
        yStartField.setNumericValue(y);
        widthField.setNumericValue(w);
        heightField.setNumericValue(h);
    }

    /**
     * Set 2D region values.
     */
    public void setRegion(int x, int y, int w, int h)
    {
        xStartField.setNumericValue(x);
        yStartField.setNumericValue(y);
        widthField.setNumericValue(w);
        heightField.setNumericValue(h);
    }

    /**
     * Get 2D region values.
     */
    public Rectangle2D getRegion()
    {
        if (isInteger())
            return new Rectangle((int) xStartField.getNumericValue(), (int) yStartField.getNumericValue(),
                    (int) widthField.getNumericValue(), (int) heightField.getNumericValue());

        return new Rectangle2D.Double(xStartField.getNumericValue(), yStartField.getNumericValue(),
                widthField.getNumericValue(), heightField.getNumericValue());
    }

    /**
     * Return true if the range use integer number
     */
    public boolean isInteger()
    {
        return xStartField.isInteger();
    }

    /**
     * Return true if the range use integer number
     */
    public void setInteger(boolean value)
    {
        xStartField.setInteger(value);
        yStartField.setInteger(value);
        widthField.setInteger(value);
        heightField.setInteger(value);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        xStartField.setEnabled(enabled);
        yStartField.setEnabled(enabled);
        widthField.setEnabled(enabled);
        heightField.setEnabled(enabled);

        super.setEnabled(enabled);
    }

    protected void fireChangedEvent()
    {
        final ChangeEvent event = new ChangeEvent(this);

        for (ChangeListener listener : getListeners(ChangeListener.class))
            listener.stateChanged(event);
    }

    public void addChangeListener(ChangeListener listener)
    {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener)
    {
        listenerList.remove(ChangeListener.class, listener);
    }

    @Override
    public void valueChanged(double newValue, boolean validate)
    {
        if (validate)
            fireChangedEvent();
    }
}
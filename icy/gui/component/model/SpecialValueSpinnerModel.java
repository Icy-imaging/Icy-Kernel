/**
 * 
 */
package icy.gui.component.model;

import javax.swing.SpinnerNumberModel;

/**
 * @author Stephane
 */
public class SpecialValueSpinnerModel extends SpinnerNumberModel
{
    /**
     * 
     */
    private static final long serialVersionUID = -8088583643848930402L;

    private Number special;
    private String specialText;

    public SpecialValueSpinnerModel()
    {
        this(Integer.valueOf(0), null, null, Integer.valueOf(1), Integer.valueOf(0), null);
    }

    public SpecialValueSpinnerModel(int special, String specialText)
    {
        this(Integer.valueOf(0), null, null, Integer.valueOf(1), Integer.valueOf(special), specialText);
    }

    public SpecialValueSpinnerModel(double value, double minimum, double maximum, double stepSize, double special,
            String specialText)
    {
        this(new Double(value), new Double(minimum), new Double(maximum), new Double(stepSize), new Double(special),
                specialText);
    }

    public SpecialValueSpinnerModel(int value, int minimum, int maximum, int stepSize, int special, String specialText)
    {
        this(Integer.valueOf(value), Integer.valueOf(minimum), Integer.valueOf(maximum), Integer.valueOf(stepSize),
                Integer.valueOf(special), specialText);
    }

    public SpecialValueSpinnerModel(Number value, Comparable minimum, Comparable maximum, Number stepSize,
            Number special, String specialText)
    {
        super(value, minimum, maximum, stepSize);

        this.special = special;
        this.specialText = specialText;
    }

    /**
     * Returns the special value which is used to display special text.
     * 
     * @see #getSpecialText()
     */
    public Number getSpecialValue()
    {
        return special;
    }

    /**
     * Returns the special text which is display when special value is selected.
     * 
     * @see #getSpecialValue()
     */
    public String getSpecialText()
    {
        return specialText;
    }
}

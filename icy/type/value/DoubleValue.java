/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class DoubleValue extends AbstractValue<Double>
{
    public DoubleValue(Double value)
    {
        super(value);
    }

    public DoubleValue(double value)
    {
        this(Double.valueOf(value));
    }

    @Override
    public Double getDefaultValue()
    {
        return Double.valueOf(0d);
    }

    @Override
    public int compareTo(Double d)
    {
        return value.compareTo(d);
    }

    @Override
    public boolean loadFromString(String s)
    {
        // empty string --> default value
        if (StringUtil.isEmpty(s))
        {
            value = getDefaultValue();
            return true;
        }

        try
        {
            value = Double.valueOf(Double.parseDouble(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

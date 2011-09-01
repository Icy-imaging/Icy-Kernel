/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class FloatValue extends AbstractValue<Float>
{
    public FloatValue(Float value)
    {
        super(value);
    }

    public FloatValue(float value)
    {
        this(Float.valueOf(value));
    }

    @Override
    public Float getDefaultValue()
    {
        return Float.valueOf(0f);
    }

    @Override
    public int compareTo(Float f)
    {
        return value.compareTo(f);
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
            value = Float.valueOf(Float.parseFloat(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

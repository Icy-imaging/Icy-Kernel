/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class IntegerValue extends AbstractValue<Integer>
{
    public IntegerValue(Integer value)
    {
        super(value);
    }

    public IntegerValue(int value)
    {
        this(Integer.valueOf(value));
    }

    @Override
    public Integer getDefaultValue()
    {
        return Integer.valueOf(0);
    }

    @Override
    public int compareTo(Integer i)
    {
        return value.compareTo(i);
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
            value = Integer.valueOf(Integer.parseInt(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

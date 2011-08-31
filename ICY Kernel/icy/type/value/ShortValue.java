/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class ShortValue extends AbstractValue<Short>
{
    public ShortValue(Short value)
    {
        super(value);
    }

    public ShortValue(short value)
    {
        this(Short.valueOf(value));
    }

    @Override
    public Short getDefaultValue()
    {
        return Short.valueOf((short) 0);
    }

    @Override
    public int compareTo(Short s)
    {
        return value.compareTo(s);
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
            value = Short.valueOf(Short.parseShort(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class LongValue extends AbstractValue<Long>
{
    public LongValue(Long value)
    {
        super(value);
    }

    public LongValue(long value)
    {
        this(Long.valueOf(value));
    }

    @Override
    public Long getDefaultValue()
    {
        return Long.valueOf(0L);
    }

    @Override
    public int compareTo(Long l)
    {
        return value.compareTo(l);
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
            value = Long.valueOf(Long.parseLong(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

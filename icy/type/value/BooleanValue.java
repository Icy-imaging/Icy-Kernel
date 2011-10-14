/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class BooleanValue extends AbstractValue<Boolean>
{
    public BooleanValue(Boolean value)
    {
        super(value);
    }

    public BooleanValue(boolean value)
    {
        this(Boolean.valueOf(value));
    }

    @Override
    public Boolean getDefaultValue()
    {
        return Boolean.FALSE;
    }

    @Override
    public int compareTo(Boolean b)
    {
        return value.compareTo(b);
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

        // true
        if (s.equalsIgnoreCase(Boolean.TRUE.toString()))
        {
            value = Boolean.TRUE;
            return true;
        }
        // false
        if (s.equalsIgnoreCase(Boolean.FALSE.toString()))
        {
            value = Boolean.FALSE;
            return true;
        }

        return false;
    }
}

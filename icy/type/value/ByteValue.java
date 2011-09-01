/**
 * 
 */
package icy.type.value;

import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class ByteValue extends AbstractValue<Byte>
{
    public ByteValue(Byte value)
    {
        super(value);
    }

    public ByteValue(byte value)
    {
        this(Byte.valueOf(value));
    }

    @Override
    public Byte getDefaultValue()
    {
        return Byte.valueOf((byte) 0);
    }

    @Override
    public int compareTo(Byte s)
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
            value = Byte.valueOf(Byte.parseByte(s));
            return true;
        }
        catch (NumberFormatException E)
        {
            return false;
        }
    }
}

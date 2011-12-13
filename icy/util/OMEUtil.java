/**
 * 
 */
package icy.util;

import icy.type.TypeUtil;
import ome.xml.model.primitives.PositiveFloat;

/**
 * @author Stephane
 */
public class OMEUtil
{
    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return 0 if specified object is null.
     */
    public static double getValue(PositiveFloat obj, double defaultValue)
    {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getDouble(obj.getValue(), defaultValue);
    }

    /**
     * Return a PositiveFloat object representing the specified value
     */
    public static PositiveFloat getPositiveFloat(double value)
    {
        return new PositiveFloat(Double.valueOf(value));
    }
}

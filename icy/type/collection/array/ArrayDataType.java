/**
 * 
 */
package icy.type.collection.array;

import icy.type.DataType;

/**
 * @deprecated Use {@link ArrayType} instead
 */
@Deprecated
public class ArrayDataType extends ArrayType
{
    /**
     * @deprecated Use {@link ArrayType#getArrayInfo(Object)} instead
     */
    @Deprecated
    public static ArrayDataType getArrayDataType(Object array)
    {
        return ArrayUtil.getArrayDataType(array);
    }

    public ArrayDataType(DataType dataType, int dim)
    {
        super(dataType, dim);
    }
}

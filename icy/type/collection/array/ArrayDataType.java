/**
 * 
 */
package icy.type.collection.array;

import icy.type.DataType;

/**
 * Class to define array data type.<br>
 * 
 * @see DataType
 * @author Stephane
 */
public class ArrayDataType
{
    /**
     * Return the ArrayDataType for the specified array (passed as Object)
     */
    public static ArrayDataType getArrayDataType(Object array)
    {
        return ArrayUtil.getArrayDataType(array);
    }

    private final DataType dataType;
    private final int dim;

    /**
     * @param dataType
     *        data type
     * @param dim
     *        dimension number
     */
    public ArrayDataType(DataType dataType, int dim)
    {
        this.dataType = dataType;
        this.dim = dim;
    }

    /**
     * Return the data type for this array
     */
    public DataType getDataType()
    {
        return dataType;
    }

    /**
     * Return number of dimension
     */
    public int getDim()
    {
        return dim;
    }

    /**
     * Return true if specified array data type is equals to current array data type
     */
    public boolean isSame(ArrayDataType arrayDataType)
    {
        return (arrayDataType.getDataType() == dataType) && (arrayDataType.getDim() == dim);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ArrayDataType)
            return isSame((ArrayDataType) obj);

        return super.equals(obj);
    }
}

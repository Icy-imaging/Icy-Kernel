/**
 * 
 */
package icy.type.collection.array;

import icy.type.DataType;

/**
 * Class which define the following array informations :<br>
 * <code>native data type</code><br>
 * <code>dimension number</code>
 * 
 * @see DataType
 * @author Stephane
 */
public class ArrayType
{
    /**
     * Return the ArrayDataType for the specified array (passed as Object)
     */
    public static ArrayType getArrayInfo(Object array)
    {
        return ArrayUtil.getArrayType(array);
    }

    private DataType dataType;
    private int dim;

    /**
     * @param dataType
     *        data type
     * @param dim
     *        dimension number
     */
    public ArrayType(DataType dataType, int dim)
    {
        super();

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
     * @param dataType
     *        the dataType to set
     */
    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }

    /**
     * @param dim
     *        the dim to set
     */
    public void setDim(int dim)
    {
        this.dim = dim;
    }

    /**
     * Return true if specified array data type is equals to current array data type
     */
    public boolean isSame(ArrayType arrayType)
    {
        return (arrayType.dataType == dataType) && (arrayType.dim == dim);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ArrayType)
            return isSame((ArrayType) obj);

        return super.equals(obj);
    }
}

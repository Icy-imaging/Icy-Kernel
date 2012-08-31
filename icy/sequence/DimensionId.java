/**
 * 
 */
package icy.sequence;

/**
 * @author Stephane
 */
public enum DimensionId
{
    NULL, X, Y, C, Z, T;

    @Override
    public String toString()
    {
        switch (this)
        {
            case X:
                return "X";
            case Y:
                return "Y";
            case C:
                return "C";
            case Z:
                return "Z";
            case T:
                return "T";
            default:
                return "NULL";
        }
    }
}

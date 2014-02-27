/**
 * 
 */
package icy.image;

import icy.sequence.Sequence;

/**
 * Define a channel position for {@link Sequence} class.
 * 
 * @author Stephane
 */
public class ChannelPosition extends ImagePosition
{
    public static final char C_ID_0 = 'C';
    public static final char C_ID_1 = 'W';

    protected int c;

    public ChannelPosition(int t, int z, int c)
    {
        super(t, z);

        this.c = c;
    }

    public ChannelPosition(ChannelPosition cp)
    {
        this(cp.t, cp.z, cp.c);
    }

    public ChannelPosition()
    {
        this(-1, -1, -1);
    }

    public void copyFrom(ChannelPosition cp)
    {
        t = cp.t;
        z = cp.z;
        c = cp.c;
    }

    @Override
    public void switchLeft()
    {
        t = z;
        z = c;
        c = 0;
    }

    @Override
    public void switchRight()
    {
        c = z;
        z = t;
        t = 0;
    }

    /**
     * @return the c
     */
    public int getC()
    {
        return c;
    }

    /**
     * @param c
     *        the c to set
     */
    public void setC(int c)
    {
        this.c = c;
    }

    public void set(int t, int z, int c)
    {
        super.set(t, z);
        this.c = c;
    }

    @Override
    public int get(char ident)
    {
        final char id = Character.toUpperCase(ident);

        switch (id)
        {
            case C_ID_0:
            case C_ID_1:
                return c;
        }

        return super.get(ident);
    }

    public static boolean isValidIdentStatic(char ident)
    {
        final char id = Character.toUpperCase(ident);

        return ImagePosition.isValidIdentStatic(ident) || (id == C_ID_0) || (id == C_ID_1);
    }

    @Override
    public boolean isValidIdent(char ident)
    {
        return isValidIdentStatic(ident);
    }

    public boolean isCUndefined()
    {
        return (c == -1);
    }

    @Override
    public boolean isUndefined()
    {
        return isCUndefined() || super.isUndefined();
    }

    /**
     * Return first undefined position with following priority C -> T -> Z
     */
    public char getAlternateFirstEmptyPos()
    {
        // check in own position
        if (isCUndefined())
            return C_ID_0;

        return super.getFirstEmptyPos();
    }

    /**
     * Return first undefined position with following priority T -> Z -> C
     */
    @Override
    public char getFirstEmptyPos()
    {
        final char result = super.getFirstEmptyPos();

        // parent doesn't have any spare position
        if (result == ' ')
        {
            // check in own position
            if (isCUndefined())
                return C_ID_0;
        }

        return result;
    }

    /**
     * Return last undefined position with following priority Z -> T -> C
     */
    public char getAlternateLastEmptyPos()
    {
        final char result = super.getLastEmptyPos();

        // parent doesn't have any spare position
        if (result == ' ')
        {
            // check in own position
            if (isCUndefined())
                return C_ID_0;
        }

        return result;
    }

    /**
     * Return last undefined position with following priority C -> Z -> T
     */
    @Override
    public char getLastEmptyPos()
    {
        // check in own position
        if (isCUndefined())
            return C_ID_0;

        return super.getLastEmptyPos();
    }

    public boolean isSamePos(ChannelPosition cp, char posIdent)
    {
        final char id = Character.toUpperCase(posIdent);

        switch (id)
        {
            case C_ID_0:
            case C_ID_1:
                if ((t == -1) || (z == -1) || (c == -1))
                    return false;
                return (cp.t == t) && (cp.z == z) && (cp.c == c);

        }

        return super.isSamePos(cp, posIdent);
    }

    /**
     * Compare to another ImagePosition with following priority T -> Z -> C
     */
    @Override
    public int compareTo(ImagePosition o)
    {
        final int result = super.compareTo(o);

        if ((result == 0) && (o instanceof ChannelPosition))
        {
            final int cp = ((ChannelPosition) o).c;

            if (c > cp)
                return 1;
            if (c < cp)
                return -1;
        }

        return result;
    }

    /**
     * Compare to another BandPosition with following priority C -> T -> Z
     */
    public int alternateCompareTo(ChannelPosition cp)
    {
        final int oc = cp.c;

        if (c > oc)
            return 1;
        if (c < oc)
            return -1;

        return super.compareTo(cp);
    }
}

/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.image;

/**
 * @author Stephane
 */
public class BandPosition extends ImagePosition
{
    public static final char C_ID = 'C';
    public static final char C_ID_ALTERNATE = 'B';

    protected int c;

    /**
     * @param t
     * @param z
     */
    public BandPosition(int t, int z, int c)
    {
        super(t, z);

        this.c = c;
    }

    public BandPosition()
    {
        this(-1, -1, -1);
    }

    public void copyFrom(BandPosition bp)
    {
        t = bp.t;
        z = bp.z;
        c = bp.c;
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
            case C_ID:
            case C_ID_ALTERNATE:
                return c;
        }

        return super.get(ident);
    }

    @Override
    public boolean isValidIdent(char ident)
    {
        final char id = Character.toUpperCase(ident);

        return super.isValidIdent(ident) || (id == C_ID) || (id == C_ID_ALTERNATE);
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
            return C_ID;

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
                return C_ID;
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
                return C_ID;
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
            return C_ID;

        return super.getLastEmptyPos();
    }

    public boolean isSamePos(BandPosition bp, char posIdent)
    {
        final char id = Character.toUpperCase(posIdent);

        switch (id)
        {
            case C_ID:
            case C_ID_ALTERNATE:
                if ((t == -1) || (z == -1) || (c == -1))
                    return false;
                return (bp.t == t) && (bp.z == z) && (bp.c == c);

        }

        return super.isSamePos(bp, posIdent);
    }

    /**
     * Compare to another ImagePosition with following priority T -> Z -> C
     */
    @Override
    public int compareTo(ImagePosition ip)
    {
        final int result = super.compareTo(ip);

        if ((result == 0) && (ip instanceof BandPosition))
        {
            final int oc = ((BandPosition) ip).c;

            if (c > oc)
                return 1;
            if (c < oc)
                return -1;
        }

        return result;
    }

    /**
     * Compare to another BandPosition with following priority C -> T -> Z
     */
    public int alternateCompareTo(BandPosition bp)
    {
        final int oc = bp.c;

        if (c > oc)
            return 1;
        if (c < oc)
            return -1;

        return super.compareTo(bp);
    }

}

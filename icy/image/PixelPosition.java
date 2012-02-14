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
public class PixelPosition extends BandPosition
{
    public static final char X_ID = 'X';
    public static final char Y_ID = 'Y';

    private int x;
    private int y;

    /**
     * @param t
     * @param z
     * @param c
     */
    public PixelPosition(int t, int z, int c, int x, int y)
    {
        super(t, z, c);

        this.x = x;
        this.y = y;
    }

    public PixelPosition()
    {
        this(-1, -1, -1, -1, -1);
    }

    public void copyFrom(PixelPosition pp)
    {
        t = pp.t;
        z = pp.z;
        c = pp.c;
        y = pp.y;
        x = pp.x;
    }

    @Override
    public void switchLeft()
    {
        t = z;
        z = c;
        c = y;
        y = x;
        x = 0;
    }

    @Override
    public void switchRight()
    {
        x = y;
        y = c;
        c = z;
        z = t;
        t = 0;
    }

    /**
     * @return the x
     */
    public int getX()
    {
        return x;
    }

    /**
     * @param x
     *        the x to set
     */
    public void setX(int x)
    {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY()
    {
        return y;
    }

    /**
     * @param y
     *        the y to set
     */
    public void setY(int y)
    {
        this.y = y;
    }

    public void set(int t, int z, int c, int x, int y)
    {
        super.set(t, z, c);
        this.x = x;
        this.y = y;
    }

    @Override
    public int get(char ident)
    {
        final char id = Character.toUpperCase(ident);

        switch (id)
        {
            case X_ID:
                return x;

            case Y_ID:
                return y;
        }

        return super.get(ident);
    }

    public static boolean isValidIdentStatic(char ident)
    {
        final char id = Character.toUpperCase(ident);

        return BandPosition.isValidIdentStatic(ident) || (id == X_ID) || (id == Y_ID);
    }

    @Override
    public boolean isValidIdent(char ident)
    {
        return super.isValidIdentStatic(ident);
    }

    public boolean isXUndefined()
    {
        return (x == -1);
    }

    public boolean isYUndefined()
    {
        return (y == -1);
    }

    @Override
    public boolean isUndefined()
    {
        return isXUndefined() || isYUndefined() || super.isUndefined();
    }

    /**
     * Return first undefined position (T -> Z -> C -> Y -> X)
     */
    @Override
    public char getFirstEmptyPos()
    {
        final char result = super.getFirstEmptyPos();

        // parent doesn't have any spare position
        if (result == ' ')
        {
            // check in own position
            if (isYUndefined())
                return Y_ID;
            if (isXUndefined())
                return X_ID;
        }

        return result;
    }

    /**
     * Return last undefined position (X -> Y -> C -> Z -> T)
     */
    @Override
    public char getLastEmptyPos()
    {
        // check in own position
        if (isXUndefined())
            return X_ID;
        if (isYUndefined())
            return Y_ID;

        return super.getFirstEmptyPos();
    }

    public boolean isSamePos(PixelPosition pp, char posIdent)
    {
        final char id = Character.toUpperCase(posIdent);

        switch (id)
        {
            case X_ID:
                if ((t == -1) || (z == -1) || (c == -1) || (x == -1))
                    return false;
                return (pp.t == t) && (pp.z == z) && (pp.c == c) && (pp.x == x);

            case Y_ID:
                if ((t == -1) || (z == -1) || (c == -1) || (x == -1) || (y == -1))
                    return false;
                return (pp.t == t) && (pp.z == z) && (pp.c == c) && (pp.x == x) && (pp.y == y);
        }

        return super.isSamePos(pp, posIdent);
    }

    @Override
    public int compareTo(Object o)
    {
        final int result = super.compareTo(o);

        if ((result == 0) && (o instanceof PixelPosition))
        {
            final PixelPosition pp = (PixelPosition) o;

            final int ox = pp.x;
            final int oy = pp.y;

            if (x > ox)
                return 1;
            if (x < ox)
                return -1;
            if (y > oy)
                return 1;
            if (y < oy)
                return -1;
        }

        return result;
    }
}

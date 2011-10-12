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
public class ImagePosition implements Comparable<ImagePosition>
{
    public static final char T_ID = 'T';
    public static final char Z_ID = 'Z';

    protected int t;
    protected int z;

    /**
     * @param t
     * @param z
     */
    public ImagePosition(int t, int z)
    {
        super();

        this.t = t;
        this.z = z;
    }

    public ImagePosition()
    {
        this(-1, -1);
    }

    public void copyFrom(ImagePosition ip)
    {
        t = ip.t;
        z = ip.z;
    }

    public void switchLeft()
    {
        t = z;
        z = 0;
    }

    public void switchRight()
    {
        z = t;
        t = 0;
    }

    /**
     * @return the t
     */
    public int getT()
    {
        return t;
    }

    /**
     * @param t
     *        the t to set
     */
    public void setT(int t)
    {
        this.t = t;
    }

    /**
     * @return the z
     */
    public int getZ()
    {
        return z;
    }

    /**
     * @param z
     *        the z to set
     */
    public void setZ(int z)
    {
        this.z = z;
    }

    public void set(int t, int z)
    {
        this.t = t;
        this.z = z;
    }

    public int get(char ident)
    {
        final char id = Character.toUpperCase(ident);

        switch (id)
        {
            case T_ID:
                return t;

            case Z_ID:
                return z;
        }

        return -1;
    }

    public boolean isValidIdent(char ident)
    {
        final char id = Character.toUpperCase(ident);

        return (id == T_ID) || (id == Z_ID);
    }

    public boolean isTUndefined()
    {
        return (t == -1);
    }

    public boolean isZUndefined()
    {
        return (z == -1);
    }

    public boolean isUndefined()
    {
        return isTUndefined() || isZUndefined();
    }

    public char getFirstEmptyPos()
    {
        if (isTUndefined())
            return T_ID;
        if (isZUndefined())
            return Z_ID;

        // no empty pos
        return ' ';
    }

    public char getLastEmptyPos()
    {
        if (isZUndefined())
            return Z_ID;
        if (isTUndefined())
            return T_ID;

        // no empty pos
        return ' ';
    }

    public boolean isSamePos(ImagePosition ip, char posIdent)
    {
        final char id = Character.toUpperCase(posIdent);

        switch (id)
        {
            case T_ID:
                if (t == -1)
                    return false;
                return (ip.t == t);

            case Z_ID:
                if ((t == -1) || (z == -1))
                    return false;
                return (ip.t == t) && (ip.z == z);
        }

        return false;
    }

    @Override
    public int compareTo(ImagePosition ip)
    {
        final int ot = ip.t;
        final int oz = ip.z;

        if (t > ot)
            return 1;
        if (t < ot)
            return -1;
        if (z > oz)
            return 1;
        if (z < oz)
            return -1;

        return 0;
    }

}

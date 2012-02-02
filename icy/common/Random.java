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
package icy.common;

/**
 * @deprecated uses {@link icy.util.Random} instead
 */
public class Random
{
    /**
     * @deprecated uses {@link icy.util.Random#nextInt()} instead
     */
    public static int nextInt()
    {
        return icy.util.Random.nextInt();
    }

    /**
     * @deprecated uses {@link icy.util.Random#nextInt(int)} instead
     */
    public static int nextInt(int n)
    {
        return icy.util.Random.nextInt(n);
    }

    /**
     * @deprecated uses {@link icy.util.Random#nextBoolean()} instead
     */
    public static boolean nextBoolean()
    {
        return icy.util.Random.nextBoolean();
    }

    /**
     * @deprecated uses {@link icy.util.Random#nextDouble()} instead
     */
    public static double nextDouble()
    {
        return icy.util.Random.nextDouble();
    }

    /**
     * @deprecated uses {@link icy.util.Random#nextFloat()} instead
     */
    public static float nextFloat()
    {
        return icy.util.Random.nextFloat();
    }

    /**
     * @deprecated uses {@link icy.util.Random#nextLong()} instead
     */
    public static long nextLong()
    {
        return icy.util.Random.nextLong();
    }
}

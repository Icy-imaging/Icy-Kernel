/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.common;

/**
 * @deprecated Use {@link icy.util.Random} instead
 */
public class Random
{
    /**
     * @deprecated Use {@link icy.util.Random#nextInt()} instead
     */
    public static int nextInt()
    {
        return icy.util.Random.nextInt();
    }

    /**
     * @deprecated Use {@link icy.util.Random#nextInt(int)} instead
     */
    public static int nextInt(int n)
    {
        return icy.util.Random.nextInt(n);
    }

    /**
     * @deprecated Use {@link icy.util.Random#nextBoolean()} instead
     */
    public static boolean nextBoolean()
    {
        return icy.util.Random.nextBoolean();
    }

    /**
     * @deprecated Use {@link icy.util.Random#nextDouble()} instead
     */
    public static double nextDouble()
    {
        return icy.util.Random.nextDouble();
    }

    /**
     * @deprecated Use {@link icy.util.Random#nextFloat()} instead
     */
    public static float nextFloat()
    {
        return icy.util.Random.nextFloat();
    }

    /**
     * @deprecated Use {@link icy.util.Random#nextLong()} instead
     */
    public static long nextLong()
    {
        return icy.util.Random.nextLong();
    }
}

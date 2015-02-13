/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.util;

/**
 * Random utilities class.
 * 
 * @author Stephane
 */
public class Random
{
    private static final java.util.Random generator = new java.util.Random();

    /**
     * @see java.util.Random#nextInt()
     */
    public static int nextInt()
    {
        return generator.nextInt();
    }

    /**
     * @see java.util.Random#nextInt(int)
     */
    public static int nextInt(int n)
    {
        return generator.nextInt(n);
    }

    /**
     * @see java.util.Random#nextBoolean()
     */
    public static boolean nextBoolean()
    {
        return generator.nextBoolean();
    }

    /**
     * @see java.util.Random#nextDouble()
     */
    public static double nextDouble()
    {
        return generator.nextDouble();
    }

    /**
     * @see java.util.Random#nextFloat()
     */
    public static float nextFloat()
    {
        return generator.nextFloat();
    }

    /**
     * @see java.util.Random#nextLong()
     */
    public static long nextLong()
    {
        return generator.nextLong();
    }
}

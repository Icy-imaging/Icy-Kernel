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

    public static int nextInt()
    {
        return generator.nextInt();
    }

    public static int nextInt(int n)
    {
        return generator.nextInt(n);
    }

    public static boolean nextBoolean()
    {
        return generator.nextBoolean();
    }

    public static double nextDouble()
    {
        return generator.nextDouble();
    }

    public static float nextFloat()
    {
        return generator.nextFloat();
    }

    public static long nextLong()
    {
        return generator.nextLong();
    }
}

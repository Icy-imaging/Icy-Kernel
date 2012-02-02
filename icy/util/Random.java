/**
 * 
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

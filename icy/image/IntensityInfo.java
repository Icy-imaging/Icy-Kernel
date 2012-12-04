/**
 * 
 */
package icy.image;

/**
 * Intensity information
 * 
 * @author Stephane
 */
public class IntensityInfo
{
    /**
     * Minimum intensity
     */
    public double minIntensity;
    /**
     * Mean intensity
     */
    public double meanIntensity;
    /**
     * Maximum intensity
     */
    public double maxIntensity;

    public IntensityInfo()
    {
        super();

        minIntensity = 0d;
        meanIntensity = 0d;
        maxIntensity = 0d;
    }
}

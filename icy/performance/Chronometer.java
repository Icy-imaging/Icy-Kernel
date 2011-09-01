/**
 * 
 */
package icy.performance;

/**
 * Uses icy.system.profile.Chronometer class instead
 * 
 * @author Stephane
 * @deprecated
 */
@Deprecated
public class Chronometer
{
    private final icy.system.profile.Chronometer c;

    public Chronometer(String descriptionString)
    {
        c = new icy.system.profile.Chronometer(descriptionString);
    }

    /**
     * @see icy.system.profile.Chronometer#getNanos()
     */
    public long getNanos()
    {
        return c.getNanos();
    }

    /**
     * @see icy.system.profile.Chronometer#displayMs()
     */
    public void displayMs()
    {
        c.displayMs();
    }

    /**
     * @see icy.system.profile.Chronometer#displayInSeconds()
     */
    public void displayInSeconds()
    {
        c.displayInSeconds();
    }
}

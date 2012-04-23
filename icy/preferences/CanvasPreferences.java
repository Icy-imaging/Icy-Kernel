/**
 * 
 */
package icy.preferences;

/**
 * @author Stephane
 */
public class CanvasPreferences
{
    /**
     * preferences id
     */
    private static final String PREF_ID = "canvas";

    /**
     * id
     */
    private static final String ID_FILTERING = "filtering";
    private static final String ID_INVERT_MOUSEWHEEL_AXIS = "invertMouseWheelAxis";
    private static final String ID_MOUSEWHEEL_SENSIBILITY = "mouseWheelSensibility";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preferences
        preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    public static boolean getFiltering()
    {
        return preferences.getBoolean(ID_FILTERING, false);
    }

    public static void setFiltering(boolean value)
    {
        preferences.putBoolean(ID_FILTERING, value);
    }

    public static boolean getInvertMouseWheelAxis()
    {
        return preferences.getBoolean(ID_INVERT_MOUSEWHEEL_AXIS, false);
    }

    public static void setInvertMouseWheelAxis(boolean value)
    {
        preferences.putBoolean(ID_INVERT_MOUSEWHEEL_AXIS, value);
    }

    /**
     * Mouse wheel sensitivity (1-10)
     */
    public static double getMouseWheelSensitivity()
    {
        return preferences.getDouble(ID_MOUSEWHEEL_SENSIBILITY, 5d);
    }

    public static void setMouseWheelSensitivity(double value)
    {
        preferences.putDouble(ID_MOUSEWHEEL_SENSIBILITY, value);
    }

}

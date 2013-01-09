/**
 * 
 */
package icy.imagej.patches;

import javax.media.rtp.event.ApplicationEvent;

/**
 * @author Stephane
 */
public class MacAdapterMethods
{
    private MacAdapterMethods()
    {
        // prevent instantiation of utility class
    }

    /** Replaces {@link MacAdapter#handleAbout(com.apple.eawt.ApplicationEvent)}. */
    public static void handleAbout(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleOpenApplication(com.apple.eawt.ApplicationEvent)}. */
    public static void handleOpenApplication(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleOpenFile(com.apple.eawt.ApplicationEvent)}. */
    public static void handleOpenFile(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handlePreferences(com.apple.eawt.ApplicationEvent)}. */
    public static void handlePreferences(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handlePrintFile(com.apple.eawt.ApplicationEvent)}. */
    public static void handlePrintFile(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleQuit(com.apple.eawt.ApplicationEvent)}. */
    public static void handleQuit(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleReOpenApplication(com.apple.eawt.ApplicationEvent)}. */
    public static void handleReOpenApplication(final Object obj, Object event)
    {
        // do nothing
    }
}

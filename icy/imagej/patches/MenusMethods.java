/**
 * 
 */
package icy.imagej.patches;

import icy.imagej.ImageJWrapper;
import icy.main.Icy;
import ij.ImageJ;
import ij.Menus;

/**
 * Overrides {@link Menus} methods.
 * 
 * @author Stephane Dallongeville
 */
@SuppressWarnings("unused")
public class MenusMethods
{
    private MenusMethods()
    {
        // prevent instantiation of utility class
    }

    /** Appends {@link Menus#installUserPlugin(String, boolean)}. */
    public static void installUserPlugin(final Menus obj, String className, boolean force)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#updateMenus()}. */
    public static void updateMenus()
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#updateWindowMenuItem(String, String)}. */
    public static synchronized void updateWindowMenuItem(String oldLabel, String newLabel)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#addOpenRecentItem(String)}. */
    public static synchronized void addOpenRecentItem(String path)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#installPlugin(String, char, String, String, ImageJ)}. */
    public static int installPlugin(String plugin, char menuCode, String command, String shortcut, ImageJ ij, int result)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();

        return result;
    }
}

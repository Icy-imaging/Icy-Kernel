/**
 * 
 */
package icy.gui.util;

import java.awt.Font;

/**
 * Font utilities.
 * 
 * @author Stephane
 */
public class FontUtil
{
    /**
     * Change the Font
     */
    public static Font setName(Font font, String name)
    {
        if (font != null)
            return new Font(name, font.getStyle(), font.getSize());

        return null;
    }

    /**
     * Change the size of Font
     */
    public static Font setSize(Font font, int size)
    {
        if (font != null)
            return font.deriveFont((float) size);

        return null;
    }

    /**
     * Change the style of Font
     */
    public static Font setStyle(Font font, int style)
    {
        if (font != null)
            return font.deriveFont(style);

        return null;
    }
}

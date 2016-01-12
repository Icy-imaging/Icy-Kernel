/**
 * 
 */
package icy.util;

import icy.gui.dialog.IdConfirmDialog;

import javax.media.opengl.GLProfile;

/**
 * Utilities class for OpenGL.
 * 
 * @author Stephane
 */
public class OpenGLUtil
{
    /**
     * Returns <code>true</code> is the specified version of OpenGL is supported by the graphics card (or by its
     * driver).<br/>
     * Ex: if (isOpenGLSupported(2)) // test for OpenGL 2 compliance
     * 
     * @param version
     *        the version of OpenGL we want to test for (1 to 4)
     */
    public static boolean isOpenGLSupported(int version)
    {
        try
        {
            // get maximum supported GL profile
            final GLProfile glp = GLProfile.getMaximum(true);

            switch (version)
            {
                case 2:
                    return glp.isGL2();
                case 3:
                    return glp.isGL3();
                case 4:
                    return glp.isGL4();
            }

            return (version > 0) && (version <= 4);
        }
        catch (Exception e)
        {
            // OpenGL throwing error --> just report as not supported
            return false;
        }
    }
}

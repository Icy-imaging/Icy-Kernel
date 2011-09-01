/**
 * 
 */
package icy.canvas;

import icy.gui.viewer.Viewer;

/**
 * @author Stephane
 */
public abstract class IcyCanvas3D extends IcyCanvas
{
    /**
     * 
     */
    private static final long serialVersionUID = 6001100311244609559L;

    public IcyCanvas3D(Viewer viewer)
    {
        super(viewer);

        // default for 3D canvas
        posX = -1;
        posY = -1;
        posZ = -1;
    }
}
/**
 * 
 */
package plugins.kernel.canvas;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginCanvas;

/**
 * Plugin wrapper for Canvas2D
 * 
 * @author Stephane
 */
public class Canvas3DPlugin extends Plugin implements PluginCanvas
{
    @Override
    public IcyCanvas createCanvas(Viewer viewer)
    {
        return new Canvas3D(viewer);
    }

    @Override
    public String getCanvasClassName()
    {
        return Canvas3D.class.getName();
    }
}

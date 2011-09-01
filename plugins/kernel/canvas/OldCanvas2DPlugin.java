/**
 * 
 */
package plugins.kernel.canvas;

import icy.canvas.OldCanvas2D;
import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginCanvas;

/**
 * Plugin wrapper for Canvas2D
 * 
 * @author Stephane
 */
public class OldCanvas2DPlugin extends Plugin implements PluginCanvas
{
    @Override
    public IcyCanvas createCanvas(Viewer viewer)
    {
        return new OldCanvas2D(viewer);
    }

    @Override
    public String getCanvasClassName()
    {
        return OldCanvas2D.class.getName();
    }
}

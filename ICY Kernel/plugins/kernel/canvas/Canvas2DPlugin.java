/**
 * 
 */
package plugins.kernel.canvas;

import icy.canvas.IcyCanvas;
import icy.canvas.Canvas2D;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginCanvas;

/**
 * @author Stephane
 */
public class Canvas2DPlugin extends Plugin implements PluginCanvas
{
    @Override
    public String getCanvasClassName()
    {
        return Canvas2D.class.getName();
    }

    @Override
    public IcyCanvas createCanvas(Viewer viewer)
    {
        return new Canvas2D(viewer);
    }
}

package plugins.kernel.canvas;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginCanvas;

/**
 * Plugin wrapper for VtkCanvas
 * 
 * @author Stephane
 */
public class VtkCanvasPlugin extends Plugin implements PluginCanvas
{
    @Override
    public IcyCanvas createCanvas(Viewer viewer)
    {
        return new VtkCanvas(viewer);
    }

    @Override
    public String getCanvasClassName()
    {
        return VtkCanvas.class.getName();
    }
}

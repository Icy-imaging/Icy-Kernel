/**
 * 
 */
package icy.canvas;

import java.util.EventListener;

/**
 * @author Stephane
 */
public interface CanvasLayerListener extends EventListener
{
    public void canvasLayerChanged(CanvasLayerEvent event);
}

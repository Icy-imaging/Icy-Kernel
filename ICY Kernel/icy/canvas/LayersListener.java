/**
 * 
 */
package icy.canvas;

import java.util.EventListener;

/**
 * @author Stephane
 */
public interface LayersListener extends EventListener
{
    public void layersChanged(LayersEvent event);
}

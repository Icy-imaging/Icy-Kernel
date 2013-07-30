/**
 * 
 */
package icy.gui.main;

import icy.painter.Overlay;

import java.util.EventListener;

/**
 * Global {@link Overlay} listener class.
 * Used to listen add and remove event for all overlay.
 * 
 * @author Stephane
 */
public interface GlobalOverlayListener extends EventListener
{
    /**
     * An overlay was just added to its first sequence
     */
    public void overlayAdded(Overlay overlay);

    /**
     * An overlay was just removed from its last sequence
     */
    public void overlayRemoved(Overlay overlay);

}

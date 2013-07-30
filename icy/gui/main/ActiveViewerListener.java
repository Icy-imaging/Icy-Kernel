/**
 * 
 */
package icy.gui.main;

import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;

import java.util.EventListener;

/**
 * Listener interface for the current active {@link Viewer}.
 * 
 * @author Stephane
 */
public interface ActiveViewerListener extends EventListener
{
    /**
     * Viewer just get the active state.
     * This event is generally preceded by a {@link #viewerDeactivated(Viewer)} event describing
     * the viewer which actually lose activation.
     */
    public void viewerActivated(Viewer viewer);

    /**
     * Viewer just lost the active state.
     * This event is always followed by a {@link #viewerActivated(Viewer)} event describing the
     * new activated viewer.
     */
    public void viewerDeactivated(Viewer viewer);

    /**
     * The current active viewer has changed.
     */
    public void activeViewerChanged(ViewerEvent event);
}

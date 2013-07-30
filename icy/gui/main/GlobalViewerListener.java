/**
 * 
 */
package icy.gui.main;

import icy.gui.viewer.Viewer;

import java.util.EventListener;

/**
 * Global {@link Viewer} listener class.
 * Used to listen open, focus and close event for all viewer.
 * 
 * @author Stephane
 */
public interface GlobalViewerListener extends EventListener
{
    /**
     * Viewer was just opened.
     */
    public void viewerOpened(Viewer viewer);

    /**
     * Viewer was just closed.
     */
    public void viewerClosed(Viewer viewer);
    
}

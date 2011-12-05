/**
 * 
 */
package icy.gui.main;

import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;

import java.util.EventListener;

/**
 * @author Stephane
 */
public interface FocusedViewerListener extends EventListener
{
    /**
     * A viewer just got the focus
     */
    public void viewerFocused(Viewer viewer);

    /**
     * The focused viewer has changed
     */
    public void focusedViewerChanged(ViewerEvent event);
}

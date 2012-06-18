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
     * The focus just changed to another viewer.
     */
    public void focusChanged(Viewer viewer);

    /**
     * The focused viewer has changed.
     */
    public void focusedViewerChanged(ViewerEvent event);
}

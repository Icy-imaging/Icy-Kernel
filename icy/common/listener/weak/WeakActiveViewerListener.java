/**
 * 
 */
package icy.common.listener.weak;

import icy.gui.main.ActiveViewerListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.main.Icy;

/**
 * @author Stephane
 */
public class WeakActiveViewerListener extends WeakListener<ActiveViewerListener> implements ActiveViewerListener
{
    public WeakActiveViewerListener(ActiveViewerListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        Icy.getMainInterface().removeActiveViewerListener(this);
    }

    @Override
    public void viewerActivated(Viewer viewer)
    {
        final ActiveViewerListener listener = getListener(null);

        if (listener != null)
            listener.viewerActivated(viewer);
    }

    @Override
    public void viewerDeactivated(Viewer viewer)
    {
        final ActiveViewerListener listener = getListener(null);

        if (listener != null)
            listener.viewerDeactivated(viewer);
    }

    @Override
    public void activeViewerChanged(ViewerEvent event)
    {
        final ActiveViewerListener listener = getListener(null);

        if (listener != null)
            listener.activeViewerChanged(event);
    }
}

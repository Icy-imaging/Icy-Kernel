/**
 * 
 */
package icy.gui.viewer;

import icy.common.listener.weak.WeakListener;

/**
 * Weak listener wrapper for ViewerListener.
 * 
 * @author Stephane
 */
public class WeakViewerListener extends WeakListener<ViewerListener> implements ViewerListener
{
    public WeakViewerListener(ViewerListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Viewer) source).removeListener(this);
    }

    @Override
    public void viewerChanged(ViewerEvent event)
    {
        final ViewerListener listener = getListener(event.getSource());

        if (listener != null)
            listener.viewerChanged(event);
    }

    @Override
    public void viewerClosed(Viewer viewer)
    {
        final ViewerListener listener = getListener(viewer);

        if (listener != null)
            listener.viewerClosed(viewer);
    }
}

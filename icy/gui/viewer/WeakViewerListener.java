/**
 * 
 */
package icy.gui.viewer;

import java.lang.ref.WeakReference;

/**
 * Weak listener wrapper for ViewerListener interface
 * 
 * @author Stephane
 */
public class WeakViewerListener implements ViewerListener
{
    private final WeakReference<ViewerListener> listenerRef;

    public WeakViewerListener(ViewerListener listener)
    {
        super();

        listenerRef = new WeakReference<ViewerListener>(listener);
    }

    private ViewerListener getListener(Viewer viewer)
    {
        final ViewerListener listener = listenerRef.get();

        if ((listener == null) && (viewer != null))
            viewer.removeListener(this);

        return listener;
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

/**
 * 
 */
package icy.painter;

import icy.common.listener.weak.WeakListener;

/**
 * Weak wrapper for OverlayListener.
 * 
 * @author Stephane
 */
public class WeakOverlayListener extends WeakListener<OverlayListener> implements OverlayListener
{
    public WeakOverlayListener(OverlayListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Overlay) source).removeOverlayListener(this);
    }

    @Override
    public void overlayChanged(OverlayEvent event)
    {
        final OverlayListener listener = getListener(event.getSource());

        if (listener != null)
            listener.overlayChanged(event);
    }
}
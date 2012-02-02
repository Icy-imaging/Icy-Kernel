/**
 * 
 */
package icy.common.listener.weak;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Weak wrapper for ComponentListener.
 * 
 * @author Stephane
 */
public class WeakComponentListener extends WeakListener<ComponentListener> implements ComponentListener
{
    public WeakComponentListener(ComponentListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Component) source).removeComponentListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentResized(e);
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentMoved(e);
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentShown(e);
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentHidden(e);
    }
}

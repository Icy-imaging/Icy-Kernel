/**
 * 
 */
package icy.common.listener.weak;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;

/**
 * Weak wrapper for IcyFrameListener.
 * 
 * @author Stephane
 */
public class WeakIcyFrameListener extends WeakListener<IcyFrameListener> implements IcyFrameListener
{
    public WeakIcyFrameListener(IcyFrameListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((IcyFrame) source).removeFrameListener(this);
    }

    @Override
    public void icyFrameOpened(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameOpened(e);
    }

    @Override
    public void icyFrameClosing(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameClosing(e);
    }

    @Override
    public void icyFrameClosed(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameClosed(e);
    }

    @Override
    public void icyFrameIconified(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameIconified(e);
    }

    @Override
    public void icyFrameDeiconified(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameDeiconified(e);
    }

    @Override
    public void icyFrameActivated(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameActivated(e);
    }

    @Override
    public void icyFrameDeactivated(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameDeactivated(e);
    }

    @Override
    public void icyFrameInternalized(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameInternalized(e);
    }

    @Override
    public void icyFrameExternalized(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameExternalized(e);
    }
}

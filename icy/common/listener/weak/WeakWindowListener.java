/**
 * 
 */
package icy.common.listener.weak;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Stephane
 */
public class WeakWindowListener extends WeakListener<WindowListener> implements WindowListener
{
    public WeakWindowListener(WindowListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Window) source).removeWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowOpened(e);
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowClosing(e);
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowClosed(e);
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowIconified(e);
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowDeiconified(e);
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowActivated(e);
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowDeactivated(e);
    }

}

/**
 * 
 */
package icy.common.listener.weak;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * @author Thomas
 */
public class WeakWindowFocusListener extends WeakListener<WindowFocusListener> implements WindowFocusListener
{
    public WeakWindowFocusListener(WindowFocusListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Window) source).removeWindowFocusListener(this);
    }

    @Override
    public void windowGainedFocus(WindowEvent windowevent)
    {
        final WindowFocusListener listener = getListener(windowevent.getWindow());

        if (listener != null)
            listener.windowGainedFocus(windowevent);
    }

    @Override
    public void windowLostFocus(WindowEvent windowevent)
    {
        final WindowFocusListener listener = getListener(windowevent.getWindow());

        if (listener != null)
            listener.windowLostFocus(windowevent);
    }

}

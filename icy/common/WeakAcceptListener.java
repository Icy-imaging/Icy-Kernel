/**
 * 
 */
package icy.common;

import icy.main.Icy;

import java.lang.ref.WeakReference;

/**
 * @author Stephane
 */
public class WeakAcceptListener implements AcceptListener
{
    private final WeakReference<AcceptListener> listenerRef;

    public WeakAcceptListener(AcceptListener listener)
    {
        super();

        listenerRef = new WeakReference<AcceptListener>(listener);
    }

    public AcceptListener getListener()
    {
        final AcceptListener listener = listenerRef.get();

        if (listener == null)
            Icy.getMainInterface().removeCanExitListener(this);

        return listener;
    }

    @Override
    public boolean accept(Object source)
    {
        final AcceptListener listener = getListener();

        if (listener != null)
            return listener.accept(source);

        return true;
    }
}

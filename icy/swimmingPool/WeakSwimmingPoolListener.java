/**
 * 
 */
package icy.swimmingPool;

import icy.main.Icy;

import java.lang.ref.WeakReference;

/**
 * @author Stephane
 */
public class WeakSwimmingPoolListener implements SwimmingPoolListener
{
    private final WeakReference<SwimmingPoolListener> listenerRef;

    public WeakSwimmingPoolListener(SwimmingPoolListener listener)
    {
        super();

        listenerRef = new WeakReference<SwimmingPoolListener>(listener);
    }

    private SwimmingPoolListener getListener()
    {
        final SwimmingPoolListener listener = listenerRef.get();

        if (listener == null)
        {
            final SwimmingPool swimmingPool = Icy.getMainInterface().getSwimmingPool();

            if (swimmingPool != null)
                swimmingPool.removeListener(listener);
        }

        return listener;
    }

    @Override
    public void swimmingPoolChangeEvent(SwimmingPoolEvent event)
    {
        final SwimmingPoolListener listener = getListener();

        if (listener != null)
            listener.swimmingPoolChangeEvent(event);
    }

}

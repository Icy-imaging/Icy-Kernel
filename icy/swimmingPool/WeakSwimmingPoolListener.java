/**
 * 
 */
package icy.swimmingPool;

import icy.common.listener.weak.WeakListener;
import icy.main.Icy;

/**
 * Weak listener wrapper for SwimmingPoolListener.
 * 
 * @author Stephane
 */
public class WeakSwimmingPoolListener extends WeakListener<SwimmingPoolListener> implements SwimmingPoolListener
{
    public WeakSwimmingPoolListener(SwimmingPoolListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        final SwimmingPool swimmingPool = Icy.getMainInterface().getSwimmingPool();

        if (swimmingPool != null)
            swimmingPool.removeListener(this);
    }

    @Override
    public void swimmingPoolChangeEvent(SwimmingPoolEvent event)
    {
        final SwimmingPoolListener listener = getListener();

        if (listener != null)
            listener.swimmingPoolChangeEvent(event);
    }
}

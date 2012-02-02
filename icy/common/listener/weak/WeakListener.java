/**
 * 
 */
package icy.common.listener.weak;

import java.lang.ref.WeakReference;

/**
 * Base weak listener class.
 * 
 * @author Stephane
 */
public abstract class WeakListener<T>
{
    private final WeakReference<T> listenerRef;

    public WeakListener(T listener)
    {
        super();

        listenerRef = new WeakReference<T>(listener);
    }

    public T getListener(Object source)
    {
        final T listener = listenerRef.get();

        // no more listener --> remove weak object from list
        if (listener == null)
            removeListener(source);

        return listener;
    }

    public T getListener()
    {
        return getListener(null);
    }

    public abstract void removeListener(Object source);
}

/**
 * 
 */
package icy.gui.main;

import icy.main.Icy;

import java.lang.ref.WeakReference;

/**
 * Weak listener wrapper for MainListener interface
 * 
 * @author Stephane
 */
public class WeakMainListener implements MainListener
{
    private final WeakReference<MainListener> listenerRef;

    public WeakMainListener(MainListener listener)
    {
        super();

        listenerRef = new WeakReference<MainListener>(listener);
    }

    private MainListener getListener()
    {
        final MainListener listener = listenerRef.get();

        if (listener == null)
            Icy.getMainInterface().removeListener(this);

        return listener;
    }

    @Override
    public void pluginOpened(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.pluginOpened(event);
    }

    @Override
    public void pluginClosed(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.pluginClosed(event);
    }

    @Override
    public void viewerOpened(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.viewerOpened(event);
    }

    @Override
    public void viewerFocused(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.viewerFocused(event);
    }

    @Override
    public void viewerClosed(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.viewerClosed(event);
    }

    @Override
    public void sequenceOpened(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.sequenceOpened(event);
    }

    @Override
    public void sequenceFocused(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.sequenceFocused(event);
    }

    @Override
    public void sequenceClosed(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.sequenceClosed(event);
    }

    @Override
    public void roiAdded(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.roiAdded(event);
    }

    @Override
    public void roiRemoved(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.roiRemoved(event);
    }

    @Override
    public void painterAdded(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.painterAdded(event);
    }

    @Override
    public void painterRemoved(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.painterRemoved(event);
    }
}

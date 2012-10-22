package plugins.kernel.searchbar.interfaces;

import icy.util.StringUtil;

import java.util.ArrayList;

import org.w3c.dom.Element;

import plugins.kernel.searchbar.provider.ProviderListener;

/**
 * A provider represents a list of {@link SBLink} in the table. This provider is in charge of the
 * local and online requests asked by the user.
 * 
 * @author Thomas Provoost
 */
public abstract class SBProvider
{

    public static final boolean DEBUG = false;

    /** List of items. */
    protected ArrayList<SBLink> elements = new ArrayList<SBLink>();

    /** List of listeners */
    private ArrayList<ProviderListener> listeners = new ArrayList<ProviderListener>();

    private static int idCount = 0;

    protected int id = idCount++;

    protected boolean isRequestCancelled = false;

    /** Name of the provider */
    public abstract String getName();

    /** Get the elements */
    public ArrayList<SBLink> getElements()
    {
        return elements;
    }

    /**
     * Get the provider ID. Mostly used for debugging.
     * 
     * @return
     */
    public int getId()
    {
        return id;
    }

    /**
     * Perform the request: mostly put into elements the FLinks. However, this
     * method may be called several times in a row with thread. Be careful to
     * watch out for the cancelRequest() method.
     * 
     * @param filter
     *        : filter used for the request
     * @see SBProvider#cancelRequest()
     */
    public abstract void performLocalRequest(String filter);

    /**
     * Process the answer with online elements.
     * 
     * @param filter
     *        : text filter in the searchbar
     * @param result
     *        : XML Node in the answer
     */
    public abstract void processOnlineResult(String filter, Element result);

    /**
     * Cancel the previous request(s). This will be called at every caret update
     * done, before sending another request.
     */
    public abstract void cancelRequest();

    public synchronized void setCancelledRequest(boolean b)
    {
        isRequestCancelled = b;
    }

    /**
     * Provider is loaded (should be called at the end of processLocal <b>and</b> processOnline).
     */
    protected void loaded()
    {
        for (ProviderListener pl : listeners)
            pl.loadedProvider(this);
    }

    /**
     * Add a listener.
     * 
     * @param pl
     */
    public void addListener(ProviderListener pl)
    {
        listeners.add(pl);
    }

    /**
     * Remove a listener.
     * 
     * @param pl
     */
    public void removeListener(ProviderListener pl)
    {
        listeners.remove(pl);
    }

    /**
     * Returns the list of listeners on this provider.
     * 
     * @return an ArrayList of ProviderListener
     */
    public ArrayList<ProviderListener> getListeners()
    {
        return listeners;
    }

    /**
     * Removes all elements from the list of items.
     */
    public void clear()
    {
        elements.clear();
    }

    /**
     * Returns if current provider is cancelled.
     * 
     * @return
     */
    public boolean isRequestCancelled()
    {
        return isRequestCancelled;
    }

    /**
     * Returns the tooltip displayed on the menu (in small under the label).
     * 
     * @return
     */
    public String getTooltipText()
    {
        return "click to run";
    }

    protected boolean listConstains(SBLink link)
    {
        for (SBLink f : elements)
        {
            if (StringUtil.equals(f.getLabel(), link.getLabel()))
                return true;
        }
        return false;
    }
}

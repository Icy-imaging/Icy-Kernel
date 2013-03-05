package icy.search;

import java.awt.Image;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * Defines an item in the SearchResultPanel.
 * 
 * @author Thomas Provoost & Stephane Dallongeville
 */
public abstract class SearchResult implements Comparable<SearchResult>
{
    private final SearchResultProducer producer;

    public SearchResult(SearchResultProducer producer)
    {
        super();

        this.producer = producer;
    }

    /**
     * @return Returns the producer.
     */
    public SearchResultProducer getProducer()
    {
        return producer;
    }

    /**
     * Returns the title of the result.
     */
    public abstract String getTitle();

    /**
     * Returns the image of the result (can be null).
     */
    public abstract Image getImage();

    /**
     * Returns the description of the result.
     */
    public abstract String getDescription();

    /**
     * Returns the tooltip that will be displayed for this result.
     */
    public abstract String getTooltip();

    /**
     * Returns enabled state of the result.
     */
    public boolean isEnabled()
    {
        return true;
    }

    /**
     * Executes the associated action for this result.
     */
    public abstract void execute();

    /**
     * Executes the associated alternate action (right mouse button) for this result.
     */
    public abstract void executeAlternate();

    /**
     * Get the RichTooltip associated to the result.
     */
    public abstract RichTooltip getRichToolTip();

    /**
     * Default implementation
     */
    @Override
    public int compareTo(SearchResult o)
    {
        return getTitle().compareTo(o.getTitle());
    }

}

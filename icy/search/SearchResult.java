/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
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

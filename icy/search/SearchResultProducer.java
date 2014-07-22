/*
 * Copyright 2010-2013 Institut Pasteur.
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

import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The SearchResultProducer create {@link SearchResult} objects from given search keywords.<br>
 * These {@link SearchResult} are then consumed by a {@link SearchResultConsumer}.
 * 
 * @author Thomas Provoost & Stephane Dallongeville
 */
public abstract class SearchResultProducer implements Comparable<SearchResultProducer>
{
    private class SearchRunner implements Runnable
    {
        private final String[] words;
        private final SearchResultConsumer consumer;

        public SearchRunner(String[] words, SearchResultConsumer consumer)
        {
            super();

            this.words = words;
            this.consumer = consumer;
        }

        @Override
        public void run()
        {
            // perform search if we have at least one not empty keyword
            if ((words.length > 1) || !StringUtil.isEmpty(words[0]))
                doSearch(words, consumer);
            else
            {
                final boolean notEmpty;

                synchronized (results)
                {
                    // clear the list if necessary
                    notEmpty = !results.isEmpty();
                    if (notEmpty)
                        results.clear();
                }

                // avoid death lock by sending event after synchronization
                if (notEmpty)
                    consumer.resultsChanged(SearchResultProducer.this);
            }

            // search completed (do it after searching set to false)
            consumer.searchCompleted(SearchResultProducer.this);
        }
    }

    /** Result list */
    protected List<SearchResult> results;

    /** Internals */
    protected final SingleProcessor processor;

    public SearchResultProducer()
    {
        super();

        results = new ArrayList<SearchResult>();
        processor = new SingleProcessor(true);
    }

    /** Returns the result producer order */
    public int getOrder()
    {
        // default
        return 10;
    }

    /** Returns the result producer name */
    public abstract String getName();

    /**
     * Returns the tooltip displayed on the menu (in small under the label).
     */
    public String getTooltipText()
    {
        return "Click to run";
    }

    /** Returns the result list */
    public List<SearchResult> getResults()
    {
        return results;
    }

    /**
     * Performs the search request (asynchronous), mostly build the search result list.<br>
     * Only one search request should be processed at one time so take care of waiting for previous
     * search request completion.<br>
     * 
     * @param words
     *        Search keywords
     * @param consumer
     *        Search result consumer for this search request.<br>
     *        The consumer should be notified of new results by using the
     *        {@link SearchResultConsumer#resultsChanged(SearchResultProducer)} method.
     */
    public void search(String[] words, SearchResultConsumer consumer)
    {
        processor.submit(new SearchRunner(words, consumer));
    }

    /**
     * Performs the search request (internal).<br>
     * The method is responsible for filling the <code>results</code> list :<br>
     * - If no result correspond to the requested search then <code>results</code> should be
     * cleared.<br>
     * - Else it should contains the founds results.<br>
     * <code>results</code> variable access should be synchronized as it can be externally accessed.<br>
     * The method could return earlier if {@link #hasWaitingSearch()} returns true.
     * 
     * @param words
     *        Search keywords
     * @param consumer
     *        Search result consumer for this search request.<br>
     *        The consumer should be notified of new results by using the
     *        {@link SearchResultConsumer#resultsChanged(SearchResultProducer)} method.
     * @see #hasWaitingSearch()
     */
    public abstract void doSearch(String[] words, SearchResultConsumer consumer);

    /**
     * Wait for the search request to complete.
     */
    public void waitSearchComplete()
    {
        while (isSearching())
            ThreadUtil.sleep(1);
    }

    /**
     * Returns true if the search result producer is currently processing a search request.
     */
    public boolean isSearching()
    {
        return processor.isProcessing();
    }

    /**
     * Returns true if there is a waiting search pending.<br>
     * This method should be called during search process to cancel it if another search is waiting.
     */
    public boolean hasWaitingSearch()
    {
        return processor.hasWaitingTasks();
    }

    /**
     * Add the SearchResult to the result list.
     * 
     * @param result
     *        Result to add to the result list.
     * @param consumer
     *        If not null then consumer is notified about result change
     */
    public void addResult(SearchResult result, SearchResultConsumer consumer)
    {
        if (result != null)
        {
            synchronized (results)
            {
                results.add(result);
            }

            // notify change to consumer
            if (consumer != null)
                consumer.resultsChanged(this);
        }
    }

    @Override
    public int compareTo(SearchResultProducer o)
    {
        // sort on order
        return getOrder() - o.getOrder();
    }
}

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

import java.util.ArrayList;
import java.util.List;

import icy.system.IcyExceptionHandler;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

/**
 * The SearchResultProducer create {@link SearchResult} objects from given search keywords.<br>
 * These {@link SearchResult} are then consumed by a {@link SearchResultConsumer}.
 * 
 * @author Stephane Dallongeville
 */
public abstract class SearchResultProducer implements Comparable<SearchResultProducer>
{
    private class SearchRunner implements Runnable
    {
        private final String text;
        private final SearchResultConsumer consumer;

        public SearchRunner(String text, SearchResultConsumer consumer)
        {
            super();

            this.text = text;
            this.consumer = consumer;
        }

        @Override
        public void run()
        {
            // perform search if text is not empty
            if (!StringUtil.isEmpty(text))
            {
                try
                {
                    doSearch(text, consumer);
                }
                catch (Throwable t)
                {
                    // just display the exception and continue
                    IcyExceptionHandler.showErrorMessage(t, true, true);
                }
            }
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

    public static class SearchWord
    {
        public final String word;
        public final boolean mandatory;
        public final boolean reject;

        public SearchWord(String word)
        {
            super();

            if (word.startsWith("+"))
            {
                mandatory = true;
                reject = false;
                if (word.length() > 1)
                    this.word = word.substring(1);
                else
                    this.word = "";
            }
            else if (word.startsWith("-"))
            {
                mandatory = false;
                reject = true;
                if (word.length() > 1)
                    this.word = word.substring(1);
                else
                    this.word = "";
            }
            else
            {
                mandatory = false;
                reject = false;
                this.word = word;
            }
        }

        public boolean isEmpty()
        {
            return StringUtil.isEmpty(word);
        }

        public int length()
        {
            return word.length();
        }
    }

    public static List<SearchWord> getSearchWords(String text)
    {
        final List<String> words = StringUtil.split(text);
        final List<SearchWord> result = new ArrayList<SearchWord>();

        for (String w : words)
        {
            final SearchWord sw = new SearchWord(w);
            if (!sw.isEmpty())
                result.add(sw);
        }

        return result;
    }

    public static boolean getShortSearch(List<SearchWord> words)
    {
        return (words.size() == 1) && (words.get(0).length() <= 2);
    }

    /** Result list */
    protected List<SearchResult> results;

    /** Internals */
    protected final SingleProcessor processor;

    public SearchResultProducer()
    {
        super();

        results = new ArrayList<SearchResult>();
        processor = new SingleProcessor(true, this.getClass().getSimpleName());
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
     * @deprecated Use {@link #search(String, SearchResultConsumer)} instead.
     */
    @Deprecated
    public void search(String[] words, SearchResultConsumer consumer)
    {
        if (words.length > 0)
        {
            String t = words[0];

            for (int i = 1; i < words.length; i++)
                t += " " + words[i];

            processor.submit(new SearchRunner(t, consumer));
        }
        else
            processor.submit(new SearchRunner("", consumer));
    }

    /**
     * Performs the search request (asynchronous), mostly build the search result list.<br>
     * Only one search request should be processed at one time so take care of waiting for previous
     * search request completion.
     * 
     * @param text
     *        Search text, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     * @param consumer
     *        Search result consumer for this search request.<br>
     *        The consumer should be notified of new results by using the
     *        {@link SearchResultConsumer#resultsChanged(SearchResultProducer)} method.
     */
    public void search(String text, SearchResultConsumer consumer)
    {
        processor.submit(new SearchRunner(text, consumer));
    }

    /**
     * @deprecated Use {@link #doSearch(String, SearchResultConsumer)} instead
     */
    @Deprecated
    public void doSearch(String[] words, SearchResultConsumer consumer)
    {
        // default implementation, does nothing...
    }

    /**
     * Performs the search request (internal).<br>
     * The method is responsible for filling the <code>results</code> list :<br>
     * - If no result correspond to the requested search then <code>results</code> should be
     * cleared.<br>
     * - otherwise it should contains the founds results.<br>
     * <code>results</code> variable access should be synchronized as it can be externally accessed.<br>
     * The method could return earlier if {@link #hasWaitingSearch()} returns true.
     * 
     * @param text
     *        Search text, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     * @param consumer
     *        Search result consumer for this search request.<br>
     *        The consumer should be notified of new results by using the
     *        {@link SearchResultConsumer#resultsChanged(SearchResultProducer)} method.
     * @see #hasWaitingSearch()
     */
    public void doSearch(String text, SearchResultConsumer consumer)
    {
        // by default this implementation use separated word search for backward compatibility
        doSearch(text.split(" "), consumer);
    }

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

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

import org.w3c.dom.Document;

import icy.network.NetworkUtil;
import icy.network.WebInterface;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

/**
 * The OnlineSearchResultProducer is the basic class for {@link SearchResult} producer from online
 * results.<br>
 * It does use a single static instance to do the online search then dispatch XML result the
 * overriding class.
 * 
 * @author Stephane Dallongeville
 * @see SearchResultProducer
 */
public abstract class OnlineSearchResultProducer extends SearchResultProducer
{
    /**
     * @deprecated Use {@link WebInterface#doSearch(String, String)} instead
     */
    @Deprecated
    protected static final String SEARCH_URL = NetworkUtil.WEBSITE_URL + "search/search.php?search=";

    public static final long REQUEST_INTERVAL = 250;
    public static final long MAXIMUM_SEARCH_TIME = 5000;

    @Override
    public void doSearch(String text, SearchResultConsumer consumer)
    {
        // nothing to do here
        if (StringUtil.isEmpty(text))
            return;

        final long startTime = System.currentTimeMillis();

        // wait interval elapsed before sending request (avoid website request spam)
        while ((System.currentTimeMillis() - startTime) < REQUEST_INTERVAL)
        {
            ThreadUtil.sleep(10);
            // abort
            if (hasWaitingSearch())
                return;
        }

        int retry = 0;

        Document document = null;
        // let's 5 tries to get the result
        while (((System.currentTimeMillis() - startTime) < MAXIMUM_SEARCH_TIME) && (document == null) && (retry < 5))
        {
            try
            {
                // do the search request
                document = doSearchRequest(text);
            }
            catch (Exception e)
            {
                IcyExceptionHandler.showErrorMessage(e, false);
            }

            // abort
            if (hasWaitingSearch())
                return;

            // error ? --> wait a bit before retry
            if (document == null)
                ThreadUtil.sleep(100);

            retry++;
        }

        // can't get result from website or another search done --> abort
        if (hasWaitingSearch() || (document == null))
            return;

        doSearch(document, text, consumer);
    }

    /**
     * Default implementation for the search request, override it if needed
     * 
     * @throws Exception
     */
    protected Document doSearchRequest(String text) throws Exception
    {
        // TODO: deprecated, to remove
        // send request to website and get result
        // return XMLUtil.loadDocument(URLUtil.getURL(SEARCH_URL + URLEncoder.encode(text, "UTF-8")), true);

        // by default we use the default WEB interface search
        return WebInterface.doSearch(text);
    }

    /**
     * @deprecated Use {@link #doSearch(Document, String, SearchResultConsumer)} instead
     */
    @Deprecated
    public void doSearch(Document doc, String[] words, SearchResultConsumer consumer)
    {
        // default implementation, does nothing...
    }

    public void doSearch(Document doc, String text, SearchResultConsumer consumer)
    {
        // by default this implementation use separated word search for backward compatibility
        doSearch(doc, text.split(" "), consumer);
    }
}

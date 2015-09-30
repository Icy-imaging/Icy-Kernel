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

import icy.network.URLUtil;
import icy.system.thread.ThreadUtil;
import icy.util.XMLUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.w3c.dom.Document;

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
    protected static final String SEARCH_URL = "http://bioimageanalysis.org/icy/search/search.php?search=";
    protected static final long REQUEST_INTERVAL = 400;
    protected static final long MAXIMUM_SEARCH_TIME = 10000;

    // we want to have only one online search shared between all online providers
    protected static volatile boolean searchingOnline = false;
    protected static Document document = null;

    @Override
    public void doSearch(String[] words, SearchResultConsumer consumer)
    {
        if (searchingOnline)
        {
            // just wait while searching end
            while (searchingOnline)
            {
                // abort
                if (hasWaitingSearch())
                    return;
                ThreadUtil.sleep(10);
            }
        }
        else
        {
            // do the online search
            searchingOnline = true;
            try
            {
                document = null;
                String request = SEARCH_URL;

                try
                {
                    if (words.length > 0)
                        request += URLEncoder.encode(words[0], "UTF-8");
                    for (int i = 1; i < words.length; i++)
                        request += "%20" + URLEncoder.encode(words[i], "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    // can't encode
                    return;
                }

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

                // let's 5 tries to get the result
                while (((System.currentTimeMillis() - startTime) < MAXIMUM_SEARCH_TIME) && (document == null)
                        && (retry < 5))
                {
                    // we use an online request as website can search in plugin documentation
                    document = XMLUtil.loadDocument(URLUtil.getURL(request), false);

                    // abort
                    if (hasWaitingSearch())
                        return;

                    // error ? --> wait a bit before retry
                    if (document == null)
                        ThreadUtil.sleep(100);

                    retry++;
                }

                // can't get result from website --> exit
                if (document == null)
                    return;

                // already have other pending search --> exit
                if (hasWaitingSearch())
                    return;
            }
            finally
            {
                searchingOnline = false;
            }
        }

        final Document doc = document;

        // another search waiting or error --> exit
        if (hasWaitingSearch() || (doc == null))
            return;

        doSearch(doc, words, consumer);
    }

    public abstract void doSearch(Document doc, String[] words, SearchResultConsumer consumer);
}

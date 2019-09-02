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
package plugins.kernel.searchprovider;

import java.util.ArrayList;
import java.util.List;

import icy.gui.plugin.PluginDetailPanel;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;

/**
 * This class is used to provide installed plugin elements to the search engine.
 * 
 * @author Stephane
 */
public class LocalPluginSearchResultProducer extends SearchResultProducer
{
    /**
     * @author Stephane
     */
    public static class LocalPluginResult extends PluginSearchResult
    {
        public LocalPluginResult(SearchResultProducer provider, PluginDescriptor plugin, String text,
                List<SearchWord> words, int priority)
        {
            super(provider, plugin, text, words, priority);
        }

        @Override
        public String getTooltip()
        {
            if (plugin.isActionable())
                return "Left click: Run   -   Right click: Online documentation";

            return "Left click: Show detail   -   Right click: Online documentation";
        }

        @Override
        public void execute()
        {
            if (plugin.isActionable())
                PluginLauncher.start(plugin);
            else
                new PluginDetailPanel(plugin);
        }
    }

    @Override
    public int getOrder()
    {
        // should be close after kernel
        return 5;
    }

    @Override
    public String getName()
    {
        return "Installed plugins";
    }

    @Override
    public String getTooltipText()
    {
        return "Result(s) from installed plugins";
    }

    @Override
    public void doSearch(String text, SearchResultConsumer consumer)
    {
        final List<SearchWord> words = getSearchWords(text);

        if (words.isEmpty())
            return;

        final List<SearchResult> tmpResults = new ArrayList<SearchResult>();
        final boolean startWithOnly = getShortSearch(words);

        for (PluginDescriptor plugin : PluginLoader.getPlugins())
        {
            if (hasWaitingSearch())
                return;

            final int prio = searchInPlugin(plugin, words, startWithOnly);

            if (prio > 0)
                tmpResults.add(new LocalPluginResult(this, plugin, plugin.getDescription(), words, prio));
        }

        // use a copy to avoid future concurrent accesses
        results = new ArrayList<SearchResult>(tmpResults);
        consumer.resultsChanged(this);

        // load descriptions
        for (SearchResult result : tmpResults)
        {
            // abort
            if (hasWaitingSearch())
                return;

            ((LocalPluginResult) result).getPlugin().loadDescriptor();
            consumer.resultChanged(this, result);
        }

        // load images
        for (SearchResult result : tmpResults)
        {
            // abort
            if (hasWaitingSearch())
                return;

            ((LocalPluginResult) result).getPlugin().loadImages();
            consumer.resultChanged(this, result);
        }
    }

    public static int searchInPlugin(PluginDescriptor plugin, List<SearchWord> words, boolean startWithOnly)
    {
        int result = 0;

        // search for all word
        for (SearchWord sw : words)
        {
            final int r = searchInPlugin(plugin, sw.word, startWithOnly);

            // mandatory word not found ? --> reject
            if ((r == 0) && sw.mandatory)
                return 0;
            // reject word found ? --> reject
            if ((r > 0) && sw.reject)
                return 0;

            result += r;
        }

        // return score
        return result;
    }

    public static int searchInPlugin(PluginDescriptor plugin, String word, boolean startWithOnly)
    {
        if (plugin.getPluginClass() != null)
        {
            // we don't want abstract nor interface nor bundled plugin in results list
            if (plugin.isAbstract() || plugin.isInterface())
                return 0;
            // we don't want bundled plugin which are not actionable
            if (plugin.isBundled() && !plugin.isActionable())
                return 0;
        }

        final String wordlc = word.toLowerCase();
        final String name = plugin.getName().toLowerCase();
        int ind;

        ind = name.indexOf(wordlc);
        if (ind >= 0)
        {
            // plugin name start with keyword --> highest priority result
            if (ind == 0)
                return 10;
            // plugin name has a word starting by keyword --> high priority result
            else if (name.charAt(ind - 1) == ' ')
                return 9;
            // don't allow partial match for short search
            else if (startWithOnly)
                return 0;
            // name contains keyword --> high/medium priority result
            else
                return 8;
        }

        // more search...
        if (!startWithOnly)
        {
            final String description = plugin.getDescription().toLowerCase();

            ind = description.indexOf(wordlc);
            if (ind >= 0)
            {
                // plugin description start with keyword --> medium
                if (ind == 0)
                    return 5;
                // plugin description has a word starting by keyword --> medium/low priority result
                else if (description.charAt(ind - 1) == ' ')
                    return 4;
                // description contains keyword --> lowest priority
                else
                    return 1;
            }
        }

        // not found
        return 0;
    }
}
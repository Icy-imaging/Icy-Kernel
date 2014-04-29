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
package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginDetailPanel;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.search.OnlineSearchResultProducer;
import icy.search.SearchEngine;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;
import icy.system.thread.ThreadUtil;
import icy.util.XMLUtil;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import plugins.kernel.searchprovider.LocalPluginSearchResultProducer.LocalPluginResult;

/**
 * This class is used to provide online plugin elements to the search engine.
 * 
 * @author Stephane
 */
public class OnlinePluginSearchResultProducer extends OnlineSearchResultProducer
{
    /**
     * @author Stephane
     */
    public static class OnlinePluginResult extends PluginSearchResult
    {
        public OnlinePluginResult(SearchResultProducer provider, PluginDescriptor plugin, String text,
                String searchWords[], int priority)
        {
            super(provider, plugin, text, searchWords, priority);
        }

        @Override
        public String getTooltip()
        {
            return "Left click: Install and Run   -   Right click: Online documentation";
        }

        @Override
        public void execute()
        {
            // can take sometime, better to execute it in background
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    // plugin locally installed ? (result transfer to local plugin provider)
                    if (PluginLoader.isLoaded(plugin.getClassName()))
                    {
                        if (plugin.isActionable())
                            PluginLauncher.start(plugin);
                        else
                        {
                            ThreadUtil.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    new PluginDetailPanel(plugin);
                                }
                            });
                        }
                    }
                    else
                    {
                        // install and run the plugin (if user ok with install)
                        PluginInstaller.install(plugin, true);
                        // wait for installation complete
                        PluginInstaller.waitInstall();

                        // find the new installed plugin
                        final PluginDescriptor localPlugin = PluginLoader.getPlugin(plugin.getClassName());
                        // plugin found ?
                        if (localPlugin != null)
                        {
                            // launch it if actionable
                            if (localPlugin.isActionable())
                                PluginLauncher.start(localPlugin);
                            else
                            {
                                // just display info
                                ThreadUtil.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        new PluginDetailPanel(localPlugin);
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    private static final String ID_SEARCH_RESULT = "searchresult";
    private static final String ID_PLUGIN = "plugin";
    private static final String ID_CLASSNAME = "classname";
    // private static final String ID_NAME = "name";
    private static final String ID_TEXT = "string";

    @Override
    public int getOrder()
    {
        // should be right after local plugin
        return 6;
    }

    @Override
    public String getName()
    {
        return "Online plugins";
    }

    @Override
    public String getTooltipText()
    {
        return "Result(s) from online plugin";
    }

    @Override
    public void doSearch(Document doc, String[] words, SearchResultConsumer consumer)
    {
        // Online plugin loader failed --> exit
        if (!ensureOnlineLoaderLoaded())
            return;

        // no need to spent more time here...
        if (hasWaitingSearch())
            return;

        // get online plugins
        final List<PluginDescriptor> onlinePlugins = PluginRepositoryLoader.getPlugins();
        // get online result node
        final Element resultElement = XMLUtil.getElement(doc.getDocumentElement(), ID_SEARCH_RESULT);

        if (resultElement == null)
            return;

        // get the local plugin search provider from search engine
        final SearchEngine se = Icy.getMainInterface().getSearchEngine();
        LocalPluginSearchResultProducer lpsrp = null;

        if (se != null)
        {
            for (SearchResultProducer srp : se.getSearchResultProducers())
                if (srp instanceof LocalPluginSearchResultProducer)
                    lpsrp = (LocalPluginSearchResultProducer) srp;
        }

        final ArrayList<SearchResult> tmpResults = new ArrayList<SearchResult>();

        for (Element plugin : XMLUtil.getElements(resultElement, ID_PLUGIN))
        {
            // abort
            if (hasWaitingSearch())
                return;

            final SearchResult result = getResult(consumer, onlinePlugins, plugin, words, lpsrp);

            if (result != null)
                tmpResults.add(result);
        }

        results = new ArrayList<SearchResult>(tmpResults);
        consumer.resultsChanged(this);

        // load descriptions
        for (SearchResult result : tmpResults)
        {
            // abort
            if (hasWaitingSearch())
                return;

            ((OnlinePluginResult) result).getPlugin().loadDescriptor();
            consumer.resultChanged(this, result);
        }

        // load images
        for (SearchResult result : tmpResults)
        {
            // abort
            if (hasWaitingSearch())
                return;

            ((OnlinePluginResult) result).getPlugin().loadImages();
            consumer.resultChanged(this, result);
        }
    }

    private boolean ensureOnlineLoaderLoaded()
    {
        PluginRepositoryLoader.waitBasicLoaded();

        // repository loader failed --> retry once
        if (PluginRepositoryLoader.failed() && NetworkUtil.hasInternetAccess())
        {
            PluginRepositoryLoader.reload();
            PluginRepositoryLoader.waitBasicLoaded();
        }

        return !PluginRepositoryLoader.failed();
    }

    private OnlinePluginResult getResult(SearchResultConsumer consumer, List<PluginDescriptor> onlinePlugins,
            Element pluginNode, String words[], LocalPluginSearchResultProducer lpsrp)
    {
        final String className = XMLUtil.getElementValue(pluginNode, ID_CLASSNAME, "");
        final String text = XMLUtil.getElementValue(pluginNode, ID_TEXT, "");
        final boolean shortSearch = PluginSearchResultProducerHelper.getShortSearch(words);
        int priority;

        final PluginDescriptor localPlugin = PluginLoader.getPlugin(className);
        // exists in local ?
        if (localPlugin != null)
        {
            // if we have the local search provider, we try to add result if not already existing
            if (lpsrp != null)
            {
                final List<SearchResult> localResults = lpsrp.getResults();
                boolean alreadyExists = false;

                synchronized (localResults)
                {
                    for (SearchResult result : localResults)
                    {
                        if (((LocalPluginResult) result).getPlugin() == localPlugin)
                        {
                            alreadyExists = true;
                            break;
                        }
                    }
                }

                // not already present in local result --> add it
                if (!alreadyExists)
                {
                    priority = PluginSearchResultProducerHelper.searchInPlugin(localPlugin, words, shortSearch);

                    // not found in local description --> assume low priority
                    if (priority == 0)
                        priority = 1;

                    lpsrp.addResult(new LocalPluginResult(lpsrp, localPlugin, text, words, priority), consumer);
                }
            }

            // don't return it for online result
            return null;
        }

        final PluginDescriptor onlinePlugin = PluginDescriptor.getPlugin(onlinePlugins, className);
        // cannot be found in online ? --> no result
        if (onlinePlugin == null)
            return null;

        // try to get priority on result
        onlinePlugin.loadDescriptor();
        priority = PluginSearchResultProducerHelper.searchInPlugin(onlinePlugin, words, shortSearch);

        // only keep high priority info from local data
        if (priority <= 5)
            priority = 1;

        return new OnlinePluginResult(this, onlinePlugin, text, words, priority);
    }
}

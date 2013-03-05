package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginDetailPanel;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;

import java.util.ArrayList;

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
                String searchWords[], int priority)
        {
            super(provider, plugin, text, searchWords, priority);
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
    public void doSearch(String[] words, SearchResultConsumer consumer)
    {
        final boolean shortSearch = PluginSearchResultProducerHelper.getShortSearch(words);

        final ArrayList<SearchResult> tmpResults = new ArrayList<SearchResult>();

        for (PluginDescriptor plugin : PluginLoader.getPlugins())
        {
            if (hasWaitingSearch())
                return;

            final int prio = PluginSearchResultProducerHelper.searchInPlugin(plugin, words, shortSearch);

            if (prio > 0)
                tmpResults.add(new LocalPluginResult(this, plugin, plugin.getDescription(), words, prio));
        }

        results = tmpResults;
        consumer.resultsChanged(this);
    }
}
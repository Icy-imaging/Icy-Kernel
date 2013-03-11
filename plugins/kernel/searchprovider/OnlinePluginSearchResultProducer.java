package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginDetailPanel;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.network.URLUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.search.SearchEngine;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;
import icy.system.thread.ThreadUtil;
import icy.util.XMLUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import plugins.kernel.searchprovider.LocalPluginSearchResultProducer.LocalPluginResult;

/**
 * This class is used to provide online plugin elements to the search engine.
 * 
 * @author Thomas Provoost & Stephane
 */
public class OnlinePluginSearchResultProducer extends SearchResultProducer
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
                        // plugin found ? --> launch it !
                        if (localPlugin != null)
                            PluginLauncher.start(localPlugin);
                    }
                }
            });
        }
    }

    private static final String SEARCH_URL = "http://bioimageanalysis.org/icy/search/search.php?search=";

    private static final String ID_SEARCH_RESULT = "searchresult";
    private static final String ID_PLUGIN = "plugin";
    private static final String ID_CLASSNAME = "classname";
    // private static final String ID_NAME = "name";
    private static final String ID_TEXT = "string";

    private final long REQUEST_INTERVAL = 400;

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
    public void doSearch(String[] words, SearchResultConsumer consumer)
    {
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

        Document doc = null;
        int retry = 0;

        // let's 10 try to get the result
        while ((doc == null) && (retry < 10))
        {
            // we use an online request as website can search in plugin documentation
            doc = XMLUtil.loadDocument(URLUtil.getURL(request), false);

            // abort
            if (hasWaitingSearch())
                return;

            // error ? --> wait a bit before retry
            if (doc == null)
                ThreadUtil.sleep(50);
        }

        // can't get result from website --> exit
        if (doc == null)
            return;

        // Online plugin loader failed --> exit
        if (!ensureOnlineLoaderLoaded())
            return;

        if (hasWaitingSearch())
            return;

        // get online plugins
        final ArrayList<PluginDescriptor> onlinePlugins = PluginRepositoryLoader.getPlugins();
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

        results = tmpResults;
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

    private OnlinePluginResult getResult(SearchResultConsumer consumer, ArrayList<PluginDescriptor> onlinePlugins,
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
                final List<SearchResult> results = lpsrp.getResults();
                boolean alreadyExists = false;

                synchronized (results)
                {
                    for (SearchResult result : results)
                    {
                        if ((result instanceof LocalPluginResult)
                                && (((LocalPluginResult) result).getPlugin() == localPlugin))
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

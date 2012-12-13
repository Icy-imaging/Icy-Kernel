package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginRichToolTip;
import icy.network.NetworkUtil;
import icy.network.URLUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to provide online plugin elements to the search engine.
 * 
 * @author Thomas Provoost & Stephane
 */
public class OnlinePluginProvider extends SearchResultProducer
{
    /**
     * @author Stephane
     */
    private class OnlinePluginResult extends SearchResult
    {
        final PluginDescriptor plugin;
        private String description;

        public OnlinePluginResult(SearchResultProducer provider, PluginDescriptor plugin, String text,
                String searchWords[])
        {
            super(provider);

            this.plugin = plugin;

            int wi = 0;
            description = "";
            while (StringUtil.isEmpty(description) && (wi < searchWords.length))
            {
                // no more than 80 characters...
                description = StringUtil.trunc(text, searchWords[wi], 80);
                wi++;
            }

            if (!StringUtil.isEmpty(description))
            {
                // remove carriage return
                description = description.replace("\n", "");

                // highlight search keywords (only for more than 2 characters search)
                if ((searchWords.length > 1) || (searchWords[0].length() > 2))
                {
                    // highlight search keywords
                    for (String word : searchWords)
                        description = StringUtil.htmlBoldSubstring(description, word, true);
                }
            }
        }

        public PluginDescriptor getPlugin()
        {
            return plugin;
        }

        @Override
        public String getTitle()
        {
            return plugin.getName();
        }

        @Override
        public Image getImage()
        {
            final ImageIcon icon = plugin.getIcon();

            if (icon != null)
                return icon.getImage();

            return null;
        }

        @Override
        public String getDescription()
        {
            return description;
        }

        @Override
        public String getTooltip()
        {
            return "Left click: install & run / Right click: documentation";
            // return plugin.getDescription();
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
            });
        }

        @Override
        public void executeAlternate()
        {
            NetworkUtil.openURL(plugin.getWeb());
        }

        @Override
        public RichTooltip getRichToolTip()
        {
            return new PluginRichToolTip(plugin);
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
    public String getName()
    {
        return "Online plugins";
    }

    @Override
    public String getTooltipText()
    {
        return "Result coming from online plugin";
    }

    @Override
    protected void doSearch(String[] words, SearchResultConsumer consumer)
    {
        String request = SEARCH_URL;

        if (words.length > 0)
            request += words[0].replace("+", "%2B").replace("&", "%26").replace("@", "%40").replace("<", "%3C")
                    .replace(">", "%3E");
        if (words.length > 1)
        {
            for (int i = 1; i < words.length; i++)
                request += "%20"
                        + words[i].replace("+", "%2B").replace("&", "%26").replace("@", "%40").replace("<", "%3C")
                                .replace(">", "%3E");
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

        // System.out.println("Request: " + request);

        Document doc = null;
        int retry = 0;

        // let's 10 try to get the result
        while ((doc == null) && (retry < 10))
        {
            // we use an online request as website can search in plugin documention
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

        final ArrayList<SearchResult> tmpResults = new ArrayList<SearchResult>();

        for (Element plugin : XMLUtil.getElements(resultElement, ID_PLUGIN))
        {
            // abort
            if (hasWaitingSearch())
                return;

            final SearchResult result = getResult(onlinePlugins, plugin, words);

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

    private OnlinePluginResult getResult(ArrayList<PluginDescriptor> onlinePlugins, Element pluginNode, String words[])
    {
        final String className = XMLUtil.getElementValue(pluginNode, ID_CLASSNAME, "");
        final String text = XMLUtil.getElementValue(pluginNode, ID_TEXT, "");

        final PluginDescriptor localPlugin = PluginLoader.getPlugin(className);
        final PluginDescriptor onlinePlugin = PluginDescriptor.getPlugin(onlinePlugins, className);

        // exists in local ? --> don't return result in online
        if (localPlugin != null)
            return null;
        // cannot be found in online ? --> no result
        if (onlinePlugin == null)
            return null;

        return new OnlinePluginResult(this, onlinePlugin, text, words);
    }

}

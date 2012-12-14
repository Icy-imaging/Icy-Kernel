package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginDetailPanel;
import icy.gui.plugin.PluginRichToolTip;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;
import icy.util.StringUtil;

import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * This class is used to provide installed plugin elements to the search engine.
 * 
 * @author Stephane
 */
public class LocalPluginProvider extends SearchResultProducer
{
    /**
     * @author Stephane
     */
    private class LocalPluginResult extends SearchResult
    {
        private final PluginDescriptor plugin;
        private String description;

        public LocalPluginResult(SearchResultProducer provider, PluginDescriptor plugin, String searchWords[])
        {
            super(provider);

            this.plugin = plugin;

            final String desc = plugin.getDescription();
            int wi = 0;
            description = "";

            while (StringUtil.isEmpty(description) && (wi < searchWords.length))
            {
                // no more than 80 characters...
                description = StringUtil.trunc(desc, searchWords[wi], 80);
                wi++;
            }

            if (!StringUtil.isEmpty(description))
            {
                // remove carriage return
                description = description.replace("\n", "");

                // highlight search keywords (only for more than 2 characters search)
                if ((searchWords.length > 1) || (searchWords[0].length() > 2))
                {
                    for (String word : searchWords)
                        description = StringUtil.htmlBoldSubstring(description, word, true);
                }
            }
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
    protected void doSearch(String[] words, SearchResultConsumer consumer)
    {
        final boolean shortSearch = (words.length == 1) && (words[0].length() <= 2);

        final ArrayList<SearchResult> tmpResults = new ArrayList<SearchResult>();

        for (PluginDescriptor plugin : PluginLoader.getPlugins())
        {
            if (hasWaitingSearch())
                return;

            if (searchInPlugin(plugin, words, shortSearch))
                tmpResults.add(new LocalPluginResult(this, plugin, words));
        }

        results = tmpResults;
        consumer.resultsChanged(this);
    }

    private boolean searchInPlugin(PluginDescriptor plugin, String[] words, boolean startWithOnly)
    {
        // we accept plugin which contains all words only
        for (String word : words)
            if (!searchInPlugin(plugin, word, startWithOnly))
                return false;

        return words.length > 0;
    }

    private boolean searchInPlugin(PluginDescriptor plugin, String word, boolean startWithOnly)
    {
        final String wordlc = word.toLowerCase();
        final String name = plugin.getName().toLowerCase();
        final String description = plugin.getDescription().toLowerCase();

        if (startWithOnly)
        {
            // search in every word of the name
            for (String nameWord : name.split(" "))
                if (nameWord.startsWith(wordlc))
                    return true;

            return false;
        }

        // simple search in name and description
        return name.contains(wordlc) || description.contains(wordlc);
    }
}
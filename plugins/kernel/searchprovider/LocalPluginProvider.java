package plugins.kernel.searchprovider;

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
 * This class is used to provide plugin elements to the Finder.
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
        private String filteredDescription;

        public LocalPluginResult(SearchResultProducer provider, PluginDescriptor plugin, String searchWords[])
        {
            super(provider);

            this.plugin = plugin;

            final String desc = plugin.getDescription();
            int wi = 0;
            filteredDescription = "";

            while (StringUtil.isEmpty(filteredDescription) && (wi < searchWords.length))
            {
                filteredDescription = StringUtil.trunc(desc, searchWords[wi], 60, true);
                wi++;
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
            return filteredDescription;
        }

        @Override
        public String getTooltip()
        {
            return "Left click: run / Right click: documentation";
            // return plugin.getDescription();
        }

        @Override
        public void execute()
        {
            PluginLauncher.start(plugin);
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
        return "Result coming from installed plugins";
    }

    @Override
    protected void doSearch(String[] words, SearchResultConsumer consumer)
    {
        final boolean shortSearch = (words.length == 1) && (words[0].length() <= 2);

        final ArrayList<SearchResult> tmpResults = new ArrayList<SearchResult>();

        for (PluginDescriptor plugin : PluginLoader.getActionablePlugins())
        {
            if (hasWaitingSearch())
                return;

            if (searchInPlugin(plugin, words, shortSearch))
                tmpResults.add(new LocalPluginResult(this, plugin, words));
        }

        results = tmpResults;
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
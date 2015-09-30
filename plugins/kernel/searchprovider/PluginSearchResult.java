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

import icy.gui.plugin.PluginRichToolTip;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.search.SearchResult;
import icy.search.SearchResultProducer;
import icy.util.StringUtil;

import java.awt.Image;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.RichTooltip;

public abstract class PluginSearchResult extends SearchResult
{
    protected final PluginDescriptor plugin;
    protected final int priority;
    protected String description;

    public PluginSearchResult(SearchResultProducer provider, PluginDescriptor plugin, String text,
            String searchWords[], int priority)
    {
        super(provider);

        this.plugin = plugin;
        this.priority = priority;

        description = "";
        int wi = 0;
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
    public void executeAlternate()
    {
        final String url = plugin.getWeb();

        if (!StringUtil.isEmpty(url))
            NetworkUtil.openBrowser(url);
    }

    @Override
    public RichTooltip getRichToolTip()
    {
        return new PluginRichToolTip(plugin);
    }

    @Override
    public int compareTo(SearchResult o)
    {
        if (o instanceof PluginSearchResult)
            return ((PluginSearchResult) o).priority - priority;

        return super.compareTo(o);
    }
}

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

import icy.plugin.PluginDescriptor;
import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane
 */
public class PluginSearchResultProducerHelper
{
    public static class SearchWord
    {
        public final String word;
        public final boolean mandatory;
        public final boolean reject;

        public SearchWord(String word)
        {
            super();

            if (word.startsWith("+"))
            {
                mandatory = true;
                reject = false;
                if (word.length() > 1)
                    this.word = word.substring(1);
                else
                    this.word = "";
            }
            else if (word.startsWith("+"))
            {
                mandatory = false;
                reject = true;
                if (word.length() > 1)
                    this.word = word.substring(1);
                else
                    this.word = "";
            }
            else
            {
                mandatory = false;
                reject = false;
                this.word = word;
            }
        }

        public boolean isEmpty()
        {
            return StringUtil.isEmpty(word);
        }

        public int length()
        {
            return word.length();
        }
    }

    public static List<SearchWord> getSearchWords(String text)
    {
        final List<String> words = StringUtil.split(text);
        final List<SearchWord> result = new ArrayList<SearchWord>();

        for (String w : words)
        {
            final SearchWord sw = new SearchWord(w);
            if (!sw.isEmpty())
                result.add(sw);
        }

        return result;
    }

    public static boolean getShortSearch(List<SearchWord> words)
    {
        return (words.size() == 1) && (words.get(0).length() <= 2);
    }

    public static int searchInPlugin(PluginDescriptor plugin, List<SearchWord> words)
    {
        final boolean startWithOnly = PluginSearchResultProducerHelper.getShortSearch(words);
        int result = 0;

        // search for all word
        for (SearchWord sw : words)
        {
            final int r = searchInPlugin(plugin, sw.word, startWithOnly);

            // mandatory word not found ? --> reject
            if ((r == 0) && sw.mandatory)
                return 0;
            // reject word found ? --> reject
            else if ((r > 0) && sw.reject)
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

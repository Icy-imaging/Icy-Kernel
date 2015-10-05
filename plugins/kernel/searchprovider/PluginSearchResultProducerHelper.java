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

/**
 * @author Stephane
 */
public class PluginSearchResultProducerHelper
{
    static boolean getShortSearch(String[] words)
    {
        return (words.length == 1) && (words[0].length() <= 2);
    }

    static int searchInPlugin(PluginDescriptor plugin, String[] words, boolean startWithOnly)
    {
        int result = 0;

        // search for all word
        for (String word : words)
        {
            final int r = searchInPlugin(plugin, word, startWithOnly);

            // word not found ? --> reject
            if (r == 0)
                return 0;

            result += r;
        }

        // return  mean score
        return result / words.length;
    }

    static int searchInPlugin(PluginDescriptor plugin, String word, boolean startWithOnly)
    {
        if (plugin.getPluginClass() != null)
        {
            // we don't want abstract nor interface plugin in results list
            if (plugin.isAbstract() || plugin.isInterface())
                return 0;
        }

        final String wordlc = word.toLowerCase();
        final String name = plugin.getName().toLowerCase();
        final String description = plugin.getDescription().toLowerCase();

        // search in every word of the name
        final String nameWords[] = name.split(" ");

        if ((nameWords.length > 0) && nameWords[0].startsWith(wordlc))
            // plugin name start with keyword --> highest priority result
            return 10;

        for (int i = 1; i < nameWords.length; i++)
            // plugin name has a word starting by keyword --> high priority result
            if (nameWords[i].startsWith(wordlc))
                return 9;

        // more search...
        if (!startWithOnly)
        {
            // name contains keyword --> medium priority
            if (name.contains(wordlc))
                return 8;

            // search in every word of the description
            final String descWords[] = description.split(" ");

            for (int i = 0; i < descWords.length; i++)
                // plugin description has a word starting by keyword --> medium/low priority result
                if (descWords[i].startsWith(wordlc))
                    return 5;

            // description contains keyword --> lowest priority
            if (description.contains(wordlc))
                return 1;
        }

        // not found
        return 0;
    }
}

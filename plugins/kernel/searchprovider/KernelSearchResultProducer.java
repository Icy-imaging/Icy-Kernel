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

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.pushingpixels.flamingo.api.common.RichTooltip;

import icy.action.ActionManager;
import icy.action.IcyAbstractAction;
import icy.resource.icon.IcyIcon;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;
import icy.util.StringUtil;

/**
 * This class is used to provide kernel command elements to the search engine.
 * 
 * @author Stephane
 */
public class KernelSearchResultProducer extends SearchResultProducer
{
    public static class KernelSearchResult extends SearchResult
    {
        private final IcyAbstractAction action;
        private final int priority;
        private String description;

        public KernelSearchResult(SearchResultProducer provider, IcyAbstractAction action, List<SearchWord> searchWords,
                int priority, boolean startWithOnly)
        {
            super(provider);

            this.action = action;
            this.priority = priority;

            final String longDesc = action.getLongDescription();

            if (!StringUtil.isEmpty(longDesc))
            {
                final String[] lds = longDesc.split("\n");

                if (lds.length > 0)
                    // no more than 80 characters for description
                    description = StringUtil.limit(lds[0], 80, true);

                // highlight search keywords (only for more than 2 characters search)
                if (!startWithOnly)
                {
                    // highlight search keywords in description
                    for (SearchWord sw : searchWords)
                        description = StringUtil.htmlBoldSubstring(description, sw.word, true);
                }
            }
            else
                description = "";
        }

        public IcyAbstractAction getAction()
        {
            return action;
        }

        @Override
        public Image getImage()
        {
            final IcyIcon icon = action.getIcon();

            if (icon != null)
                return icon.getImage();

            return null;
        }

        @Override
        public String getTitle()
        {
            final String desc = action.getDescription();

            if (!StringUtil.isEmpty(desc))
                return desc;

            return action.getName();
        }

        @Override
        public String getDescription()
        {
            return description;
        }

        @Override
        public String getTooltip()
        {
            if (isEnabled())
                return "Click to execute the action";

            return "Inactive action";
            // return action.getLongDescription();
        }

        @Override
        public boolean isEnabled()
        {
            return action.isEnabled();
        }

        @Override
        public void execute()
        {
            action.execute();
        }

        @Override
        public RichTooltip getRichToolTip()
        {
            final String longDesc = action.getLongDescription();

            if (!StringUtil.isEmpty(longDesc))
            {
                if (longDesc.split("\n").length > 1)
                    return action.getRichToolTip();
            }

            return null;
        }

        @Override
        public void executeAlternate()
        {
            // nothing to do here...
        }

        @Override
        public int compareTo(SearchResult o)
        {
            if (o instanceof KernelSearchResult)
                return ((KernelSearchResult) o).priority - priority;

            return super.compareTo(o);
        }
    }

    @Override
    public int getOrder()
    {
        // should be first
        return 0;
    }

    @Override
    public String getName()
    {
        return "Command";
    }

    @Override
    public String getTooltipText()
    {
        return "Result(s) from the internal commands and actions";
    }

    @Override
    public void doSearch(String text, SearchResultConsumer consumer)
    {
        final List<SearchWord> words = getSearchWords(text);

        if (words.isEmpty())
            return;

        final List<SearchResult> tmpResults = new ArrayList<SearchResult>();
        final boolean startWithOnly = getShortSearch(words);

        for (IcyAbstractAction action : ActionManager.actions)
        {
            if (hasWaitingSearch())
                return;

            // action match filter
            final int prio = searchInAction(action, words, startWithOnly);

            if (prio > 0)
                tmpResults.add(new KernelSearchResult(this, action, words, prio, startWithOnly));
        }

        results = tmpResults;
        consumer.resultsChanged(this);
    }

    public static int searchInAction(IcyAbstractAction action, List<SearchWord> words, boolean startWithOnly)
    {
        int result = 0;

        // we accept action which contains all words only
        for (SearchWord sw : words)
        {
            final int r = searchInAction(action, sw.word, startWithOnly);

            // mandatory word not found ? --> reject
            if ((r == 0) && sw.mandatory)
                return 0;
            // reject word found ? --> reject
            if ((r > 0) && sw.reject)
                return 0;

            result += r;
        }

        // return mean score
        return result / words.size();
    }

    public static int searchInAction(IcyAbstractAction action, String word, boolean startWithOnly)
    {
        final String wordlc = word.trim().toLowerCase();
        String text;

        // text = action.getName();
        // if (!StringUtil.isEmpty(text) && text.toLowerCase().startsWith(wordlc))
        // return 10;
        text = action.getDescription();
        if (!StringUtil.isEmpty(text) && text.toLowerCase().startsWith(wordlc))
            return 8;
        text = action.getLongDescription();
        if (!StringUtil.isEmpty(text) && text.toLowerCase().startsWith(wordlc))
            return 5;

        if (!startWithOnly)
        {
            // text = action.getName();
            // if (!StringUtil.isEmpty(text) && text.toLowerCase().contains(wordlc))
            // return 9;
            text = action.getDescription();
            if (!StringUtil.isEmpty(text) && text.toLowerCase().contains(wordlc))
                return 7;
            text = action.getLongDescription();
            if (!StringUtil.isEmpty(text) && text.toLowerCase().contains(wordlc))
                return 3;
        }

        return 0;
    }
}

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

import icy.common.IcyAbstractAction;
import icy.gui.menu.action.FileActions;
import icy.gui.menu.action.GeneralActions;
import icy.gui.menu.action.PreferencesActions;
import icy.gui.menu.action.RoiActions;
import icy.gui.menu.action.SequenceOperationActions;
import icy.gui.menu.action.WindowActions;
import icy.resource.icon.IcyIcon;
import icy.search.SearchResult;
import icy.search.SearchResultConsumer;
import icy.search.SearchResultProducer;
import icy.util.StringUtil;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.pushingpixels.flamingo.api.common.RichTooltip;

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

        public KernelSearchResult(SearchResultProducer provider, IcyAbstractAction action, String searchWords[],
                int priority)
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
                if ((searchWords.length > 1) || (searchWords[0].length() > 2))
                {
                    // highlight search keywords in description
                    for (String word : searchWords)
                        description = StringUtil.htmlBoldSubstring(description, word, true);
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
            action.doAction();
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

    private static List<IcyAbstractAction> actions = null;

    private static synchronized void initActions()
    {
        // init actions
        if (actions == null)
        {
            actions = new ArrayList<IcyAbstractAction>();

            // add all kernels actions
            actions.addAll(FileActions.getAllActions());
            actions.addAll(GeneralActions.getAllActions());
            actions.addAll(PreferencesActions.getAllActions());
            actions.addAll(SequenceOperationActions.getAllActions());
            actions.addAll(RoiActions.getAllActions());
            actions.addAll(WindowActions.getAllActions());
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
    public void doSearch(String[] words, SearchResultConsumer consumer)
    {
        // ensure actions has been initialized
        initActions();

        if (hasWaitingSearch())
            return;

        final ArrayList<SearchResult> tmpResults = new ArrayList<SearchResult>();
        final boolean shortSearch = (words.length == 1) && (words[0].length() <= 2);

        for (IcyAbstractAction action : actions)
        {
            // abort
            if (hasWaitingSearch())
                return;

            // action match filter
            final int prio = searchInAction(action, words, shortSearch);

            if (prio > 0)
                tmpResults.add(new KernelSearchResult(this, action, words, prio));
        }

        results = tmpResults;
        consumer.resultsChanged(this);
    }

    public static int searchInAction(IcyAbstractAction action, String words[], boolean startWithOnly)
    {
        int result = 0;

        // we accept action which contains all words only
        for (String word : words)
        {
            final int r = searchInAction(action, word, startWithOnly);

            // word not found ? --> reject
            if (r == 0)
                return 0;

            result += r;
        }

        // return mean score
        return result / words.length;
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

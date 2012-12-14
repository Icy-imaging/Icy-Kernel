/**
 * 
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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * This class is used to provide kernel command elements to the search engine.
 * 
 * @author Stephane
 */
public class KernelSearchProvider extends SearchResultProducer
{
    private class KernelSearchResult extends SearchResult
    {
        private final IcyAbstractAction action;
        private String description;

        public KernelSearchResult(SearchResultProducer provider, IcyAbstractAction action, String searchWords[])
        {
            super(provider);

            this.action = action;

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
            action.doAction(new ActionEvent(action, 0, ""));
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
    public String getName()
    {
        return "Command";
    }

    @Override
    public String getTooltipText()
    {
        return "Result coming from the application internals commands and actions";
    }

    @Override
    protected void doSearch(String[] words, SearchResultConsumer consumer)
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
            if (searchInAction(action, words, shortSearch))
                tmpResults.add(new KernelSearchResult(this, action, words));
        }

        results = tmpResults;
        consumer.resultsChanged(this);
    }

    private boolean searchInAction(IcyAbstractAction action, String words[], boolean startWithOnly)
    {
        // we accept action which contains all words only
        for (String word : words)
            if (!searchInAction(action, word, startWithOnly))
                return false;

        return words.length > 0;
    }

    private boolean searchInAction(IcyAbstractAction action, String word, boolean startWithOnly)
    {
        final String wordlc = word.trim().toLowerCase();
        String text;

        if (startWithOnly)
        {
            // text = action.getName();
            // if (!StringUtil.isEmpty(text) && text.toLowerCase().startsWith(wordlc))
            // return true;
            text = action.getDescription();
            if (!StringUtil.isEmpty(text) && text.toLowerCase().startsWith(wordlc))
            {
                System.out.println(text);
                return true;
            }
            text = action.getLongDescription();
            if (!StringUtil.isEmpty(text) && text.toLowerCase().startsWith(wordlc))
            {
                System.out.println(text);
                return true;
            }
        }
        else
        {
            // text = action.getName();
            // if (!StringUtil.isEmpty(text) && text.toLowerCase().contains(wordlc))
            // return true;
            text = action.getDescription();
            if (!StringUtil.isEmpty(text) && text.toLowerCase().contains(wordlc))
                return true;
            text = action.getLongDescription();
            if (!StringUtil.isEmpty(text) && text.toLowerCase().contains(wordlc))
                return true;
        }

        return false;
    }

}

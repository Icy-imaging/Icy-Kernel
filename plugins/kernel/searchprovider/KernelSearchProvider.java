/**
 * 
 */
package plugins.kernel.searchprovider;

import icy.common.IcyAbstractAction;
import icy.gui.menu.action.FileActions;
import icy.gui.menu.action.GeneralActions;
import icy.gui.menu.action.PreferencesActions;
import icy.gui.menu.action.SequenceOperationActions;
import icy.gui.menu.action.WindowActions;
import icy.resource.icon.IcyIcon;
import icy.searchbar.interfaces.SBLink;
import icy.searchbar.interfaces.SBProvider;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.w3c.dom.Element;

/**
 * This class is used to provide kernel command elements to the Finder.
 * 
 * @author Stephane
 */
public class KernelSearchProvider extends SBProvider
{
    private class ActionLink extends SBLink
    {
        private final IcyAbstractAction action;

        public ActionLink(SBProvider provider, IcyAbstractAction action)
        {
            super(provider);

            this.action = action;
        }

        @Override
        public String getLabel()
        {
            final String desc = action.getDescription();
            final String longDesc = action.getLongDescription();

            String result = "<html><b>";

            if (!StringUtil.isEmpty(desc))
                result += desc + "</b>";
            else
                result += action.getName() + "</b>";
            if (!StringUtil.isEmpty(longDesc))
            {
                final String[] lds = longDesc.split("\n");

                if (lds.length > 0)
                    result += "<br>" + lds[0];
                if (lds.length > 1)
                    result += "...";
            }

            return result;
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
        public JButton getActionB()
        {
            return null;
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
            actions.addAll(WindowActions.getAllActions());
        }
    }

    private static boolean searchInAction(IcyAbstractAction action, String upperCaseText)
    {
        String text;

        text = action.getName();
        if (!StringUtil.isEmpty(text) && text.toUpperCase().contains(upperCaseText))
            return true;
        text = action.getDescription();
        if (!StringUtil.isEmpty(text) && text.toUpperCase().contains(upperCaseText))
            return true;
        text = action.getLongDescription();
        if (!StringUtil.isEmpty(text) && text.toUpperCase().contains(upperCaseText))
            return true;

        return false;
    }

    private volatile boolean processing = false;

    @Override
    public String getName()
    {
        return "Command";
    }

    @Override
    public String getTooltipText()
    {
        return "";
    }

    @Override
    public void performLocalRequest(String filter)
    {
        // ensure actions has been initialized
        initActions();

        isRequestCancelled = true;
        waitCompletion();

        final String upperCaseFilter = filter.toUpperCase();

        elements.clear();
        isRequestCancelled = false;
        processing = true;
        try
        {
            for (IcyAbstractAction action : actions)
            {
                // abort
                if (isRequestCancelled)
                    return;

                // action match filter
                if (searchInAction(action, upperCaseFilter))
                    elements.add(new ActionLink(this, action));
            }

            // done
            loaded();
        }
        finally
        {
            processing = false;
        }
    }

    @Override
    public void processOnlineResult(String filter, Element result)
    {
        // no online search
        loaded();
    }

    private void waitCompletion()
    {
        while (processing)
            ThreadUtil.sleep(10);
    }
}

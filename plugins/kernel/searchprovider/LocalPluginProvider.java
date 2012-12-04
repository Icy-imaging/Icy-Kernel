package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginRichToolTip;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.searchbar.common.TextUtil;
import icy.searchbar.interfaces.SBLink;
import icy.searchbar.interfaces.SBProvider;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JButton;

import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.w3c.dom.Element;

/**
 * This class is used to provide plugin elements to the Finder.
 * 
 * @author Thomas Provoost
 */
public class LocalPluginProvider extends SBProvider
{

    /**
     * This list is used for the second request, to be sure there is not twice
     * the same element in the table.
     */
    ArrayList<PluginDescriptor> pluginsAdded = new ArrayList<PluginDescriptor>();

    @Override
    public void performLocalRequest(String text)
    {
        boolean isShort = text.length() <= 2;
        elements.clear();
        pluginsAdded.clear();
        ArrayList<SBLink> elementsCopy = new ArrayList<SBLink>();
        ArrayList<PluginDescriptor> pluginsAddedCopy = new ArrayList<PluginDescriptor>();
        text = text.toLowerCase();
        for (PluginDescriptor pd : PluginLoader.getActionablePlugins())
        {
            if (isRequestCancelled)
                return;
            String name = pd.getName();
            String description = pd.getDescription();
            // String author = pd.getAuthor();
            if (isShort)
            {
                String nameL = name.toLowerCase();
                if (nameL.contains(text))
                {
                    for (String s : nameL.split(" ")) // search in every word of the name
                    {
                        if (s.startsWith(text))
                        {
                            elementsCopy.add(new LinkPlugin(this, pd, description, text));
                            pluginsAddedCopy.add(pd);
                            break;
                        }
                    }

                }
            }
            else
            {
                if (name.toLowerCase().contains(text) || description.toLowerCase().contains(text))
                {
                    elementsCopy.add(new LinkPlugin(this, pd, description, text));
                    pluginsAddedCopy.add(pd);
                    // } else if (author.toLowerCase().contains(text)) {
                    // pluginsAddedCopy.add(pd);
                    // elementsCopy.add(new LinkPlugin(this, pd, author, text));
                }
            }

        }
        if (!isRequestCancelled)
        {
            pluginsAdded.addAll(pluginsAddedCopy);
            elements.addAll(elementsCopy);
            loaded();
        }
    }

    @Override
    public String getName()
    {
        return "Local Plugins";
    }

    @Override
    public void cancelRequest()
    {
        super.cancelRequest();

        if (SBProvider.DEBUG)
            System.out.println(getName() + " " + getId() + " cancelled" + Calendar.getInstance().get(Calendar.SECOND)
                    + ":" + Calendar.getInstance().get(Calendar.MILLISECOND));
    }

    @Override
    public synchronized void processOnlineResult(String filter, Element result)
    {
        if (isRequestCancelled)
            return;
        // Element plugins = XMLUtil.getElement(result, "plugins");
        for (Element plugin : XMLUtil.getElements(result, "plugin"))
        {
            if (isRequestCancelled)
                return;
            processResult(filter, plugin);
        }
        if (isRequestCancelled)
            return;
        loaded();
    }

    private void processResult(String filter, Element plugin)
    {
        String name = XMLUtil.getElementValue(plugin, "name", "");
        String className = XMLUtil.getElementValue(plugin, "classname", "");
        String str = XMLUtil.getElementValue(plugin, "string", "");
        if ((StringUtil.isEmpty(name) || !name.toLowerCase().contains(filter.toLowerCase()))
                && (StringUtil.isEmpty(str) || !str.toLowerCase().contains(filter.toLowerCase()) || StringUtil
                        .isEmpty(className)))
            return;
        PluginDescriptor pd = PluginLoader.getPlugin(className);
        if (pd == null)
        {
            if (SBProvider.DEBUG)
                System.out.println(className + " not found.");
        }
        else if (!pd.loadDescriptor())
        {
            if (SBProvider.DEBUG)
                System.out.println(pd.getName() + ": impossible to load necessary information for documentation.");
        }
        else if (!pd.isActionable())
        {
            if (SBProvider.DEBUG)
                System.out.println(pd.getName() + ": not actionable.");
        }
        else
        {
            LinkPlugin lp = new LinkPlugin(this, pd, str, filter);
            if (!listConstains(lp))
                elements.add(lp);
        }

    }

    /**
     * @author Thomas Provoost
     */
    private class LinkPlugin extends SBLink
    {

        /**
		 * 
		 */
        private PluginDescriptor pd;
        private String truncText;

        /**
         * Text is not truncated.
         * 
         * @param provider
         * @param pd
         * @param text
         */
        public LinkPlugin(SBProvider provider, PluginDescriptor pd, String text)
        {
            super(provider);
            this.pd = pd;
            text = text.replaceAll("\\<.*?\\>", "");
            truncText = text;
        }

        /**
         * Truncated text.
         * 
         * @param provider
         * @param pd
         * @param fullText
         * @param filter
         */
        public LinkPlugin(SBProvider provider, PluginDescriptor pd, String fullText, String filter)
        {
            super(provider);
            this.pd = pd;
            if (fullText.isEmpty())
                fullText = pd.getDescription();
            fullText = fullText.replaceAll("\\<.*?\\>", "");
            truncText = TextUtil.truncateText(fullText, filter);
            truncText = "<br/>" + truncText;
        }

        @Override
        public String getLabel()
        {
            return pd.getName() + truncText;
        }

        @Override
        public Image getImage()
        {
            return pd.getIconAsImage();
        }

        @Override
        @Deprecated
        public void execute()
        {
            PluginLauncher.launch(pd);
        }

        // @Override
        // public PluginPopup getPopup()
        // {
        // // generate popup menu
        // return new PluginPopup(pd);
        // }

        @Override
        public RichTooltip getRichToolTip()
        {
            return new PluginRichToolTip(pd);
        }

        @Override
        public JButton getActionB()
        {

            boolean onlineFailed = PluginRepositoryLoader.failed();

            JButton itemPluginPageDoc = new JButton();
            itemPluginPageDoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            itemPluginPageDoc.setIcon(new IcyIcon(ResourceUtil.ICON_DOC, 32));
            itemPluginPageDoc.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    NetworkUtil.openURL("http://icy.bioimageanalysis.org/plugin/" + pd.getName());
                }
            });
            if (onlineFailed)
            {
                itemPluginPageDoc.setEnabled(false);
            }
            return itemPluginPageDoc;
        }

    }

    @Override
    public String getTooltipText()
    {
        return "left click: run / right click: documentation";
    }

    @Override
    protected boolean listConstains(SBLink link)
    {
        LinkPlugin plink = (LinkPlugin) link;
        for (SBLink p : elements)
        {
            if (StringUtil.equals(((LinkPlugin) p).pd.getClassName(), plink.pd.getClassName()))
                return true;
        }
        return false;
    }
}
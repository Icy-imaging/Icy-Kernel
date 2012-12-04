package plugins.kernel.searchprovider;

import icy.gui.plugin.PluginRichToolTip;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginRepositoryLoader.PluginRepositoryLoaderListener;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.searchbar.common.TextUtil;
import icy.searchbar.interfaces.SBLink;
import icy.searchbar.interfaces.SBProvider;
import icy.searchbar.provider.ProviderListener;
import icy.system.thread.ThreadUtil;
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
 * This class is used to provide plugin elements to the SearchBar found Online.
 * 
 * @author Thomas Provoost
 */
public class OnlinePluginProvider extends SBProvider implements PluginRepositoryLoaderListener
{

    /**
     * This list is used for the second request, to be sure there is not twice
     * the same element in the table.
     */
    ArrayList<PluginDescriptor> pluginsAdded = new ArrayList<PluginDescriptor>();

    public OnlinePluginProvider()
    {
        PluginRepositoryLoader.addListener(this);
    }

    @Override
    public void performLocalRequest(String text)
    {
        if (!isRequestCancelled)
        {
            loaded();
        }
    }

    @Override
    public String getName()
    {
        return "Online Plugins";
    }

    @Override
    public void cancelRequest()
    {
        super.cancelRequest();

        PluginRepositoryLoader.removeListener(this);
        if (SBProvider.DEBUG)
            System.out.println(getName() + " " + getId() + " cancelled" + Calendar.getInstance().get(Calendar.SECOND)
                    + ":" + Calendar.getInstance().get(Calendar.MILLISECOND));
    }

    /**
     * @author Thomas Provoost
     */
    private class LinkOPlugin extends SBLink
    {

        /** Reference to the plugin this item refers to. */
        private PluginDescriptor pd;
        private String truncText;

        public LinkOPlugin(SBProvider provider, PluginDescriptor pd, String text)
        {
            super(provider);
            this.pd = pd;
            this.truncText = text;
        }

        public LinkOPlugin(SBProvider provider, PluginDescriptor pd, String text, String request)
        {
            super(provider);
            this.pd = pd;
            if (text.isEmpty())
                text = pd.getDescription();
            text = text.replaceAll("\\<.*?\\>", "");
            truncText = TextUtil.truncateText(text, request);
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
        public void execute()
        {
            // install and run the plugin (if user ok with install)
            PluginInstaller.install(pd, true);
            PluginInstaller.addListener(new PluginInstaller.PluginInstallerListener()
            {

                @Override
                public void pluginRemoved(boolean success)
                {
                }

                @Override
                public void pluginInstalled(boolean success)
                {
                    if (success)
                        ThreadUtil.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                PluginLauncher.launch(PluginLoader.getPlugin(pd.getClassName()));
                            }
                        });
                    PluginInstaller.removeListener(this);
                }
            });
        }

        // @Override
        // public JWindow getPopup()
        // {
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
    public synchronized void processOnlineResult(String filter, Element result)
    {
        if (isRequestCancelled)
            return;
        PluginRepositoryLoader.waitBasicLoaded();
        if (isRequestCancelled)
            return;
        for (Element plugin : XMLUtil.getElements(result, "plugin"))
        {
            if (isRequestCancelled)
            {
                if (SBProvider.DEBUG)
                    System.out.println(this.getName() + ": request cancelled");
                return;
            }
            String name = XMLUtil.getElementValue(plugin, "name", "");
            String className = XMLUtil.getElementValue(plugin, "classname", "");
            String str = XMLUtil.getElementValue(plugin, "string", "");
            if (SBProvider.DEBUG)
                System.out.println(className);
            if ((StringUtil.isEmpty(name) || !name.toLowerCase().contains(filter.toLowerCase()))
                    && (StringUtil.isEmpty(str) || !str.toLowerCase().contains(filter.toLowerCase()) || StringUtil
                            .isEmpty(className)))
                continue;
            final PluginDescriptor pd = PluginRepositoryLoader.getPlugin(className);
            if (pd == null)
            {
                if (SBProvider.DEBUG)
                    System.out.println(className + " not found.");
            }
            else if (PluginLoader.getPlugin(className) != null)
            {
                if (SBProvider.DEBUG)
                    System.out.println(pd.getClassName() + ": already exists.");
            }
            else
            {
                pd.loadAll();
                for (ProviderListener l : getListeners())
                    l.updateDisplay();
                SBLink link = new LinkOPlugin(this, pd, str, filter);
                if (SBProvider.DEBUG)
                    System.out.println(name + " added");
                elements.add(link);
            }
        }
        if (isRequestCancelled)
            return;
        loaded();
    }

    @Override
    public String getTooltipText()
    {
        return "left click: import + run / right click: documentation";
    }

    @Override
    protected boolean listConstains(SBLink link)
    {
        if (!elements.isEmpty())
        {
            LinkOPlugin plink = (LinkOPlugin) link;
            for (SBLink p : elements)
            {
                if (StringUtil.equals(((LinkOPlugin) p).pd.getClassName(), plink.pd.getClassName()))
                    return true;
            }
        }
        return false;
    }

    private void pluginUpdated()
    {
        for (ProviderListener p : getListeners())
            p.providerItemChanged();
    }

    @Override
    public void pluginRepositeryLoaderChanged(PluginDescriptor plugin)
    {
        for (int i = 0; i < elements.size(); ++i)
        {
            if (((LinkOPlugin) elements.get(i)).pd == plugin)
            {
                pluginUpdated();
                return;
            }
        }
    }
}

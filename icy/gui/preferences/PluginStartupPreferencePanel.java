/**
 * 
 */
package icy.gui.preferences;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.preferences.PluginPreferences;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class PluginStartupPreferencePanel extends PluginListPreferencePanel implements PluginLoaderListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -3485972129754541852L;

    public static final String NODE_NAME = "Startup Plugin";

    final HashSet<String> actives;

    public PluginStartupPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PluginPreferencePanel.NODE_NAME);

        actives = new HashSet<String>();

        PluginLoader.addListener(this);

        // remove columns 2 and 3 (not used here)
        table.removeColumn(table.getColumn(columnIds[3]));

        filter.setVisible(false);
        action1Button.setVisible(false);
        action2Button.setVisible(false);
        refreshButton.setVisible(false);

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(new JLabel("Set plugin you want to be loaded at startup (only daemon plugin supported)."));
        topPanel.add(Box.createVerticalStrut(4));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        pluginsChanged();
    }

    @Override
    protected void closed()
    {
        super.closed();

        PluginLoader.removeListener(this);
    }

    @Override
    protected boolean isActive(PluginDescriptor plugin)
    {
        return actives.contains(plugin.getClassName());
    }

    @Override
    protected void setActive(PluginDescriptor plugin, boolean value)
    {
        final String className = plugin.getClassName();

        if (value)
        {
            if (!actives.contains(className))
                actives.add(className);
        }
        else
            actives.remove(className);
    }

    @Override
    protected void load()
    {
        actives.clear();
        for (String className : PluginPreferences.getActiveDaemons())
            actives.add(className);
    }

    @Override
    protected void save()
    {
        PluginPreferences.setActiveDaemons(new ArrayList<String>(actives));
    }

    @Override
    protected void doAction1(PluginDescriptor plugin)
    {
    }

    @Override
    protected void doAction2(PluginDescriptor plugin)
    {
    }

    @Override
    protected void repositoryChanged()
    {
        // do nothing here
    }

    @Override
    protected void reloadPlugins()
    {
        PluginLoader.reloadAsynch();
        updateButtonsState();
    }

    @Override
    protected String getStateValue(PluginDescriptor plugin)
    {
        return "";
    }

    @Override
    protected ArrayList<PluginDescriptor> getPlugins()
    {
        return PluginLoader.getDaemonPlugins();
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        super.updateButtonsStateInternal();

        if (PluginLoader.isLoading())
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
        }
        else
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
        }
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        pluginsChanged();
    }

}

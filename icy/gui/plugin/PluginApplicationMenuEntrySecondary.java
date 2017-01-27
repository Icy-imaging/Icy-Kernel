/**
 * 
 */
package icy.gui.plugin;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.resource.icon.IcyIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

/**
 * @author Stephane
 */
public class PluginApplicationMenuEntrySecondary extends RibbonApplicationMenuEntrySecondary
{
    public PluginApplicationMenuEntrySecondary(PluginDescriptor plugin, String description, ActionListener action,
            boolean alpha)
    {
        super(new IcyIcon(plugin.getIconAsImage(), alpha), description, action, CommandButtonKind.ACTION_ONLY);
    }

    public PluginApplicationMenuEntrySecondary(PluginDescriptor plugin, ActionListener action, boolean alpha)
    {
        super(new IcyIcon(plugin.getIconAsImage(), alpha), plugin.getName(), action, CommandButtonKind.ACTION_ONLY);
    }

    public PluginApplicationMenuEntrySecondary(PluginDescriptor plugin, String description, ActionListener action)
    {
        this(plugin, description, action, false);
    }

    public PluginApplicationMenuEntrySecondary(PluginDescriptor plugin, ActionListener action)
    {
        this(plugin, action, false);
    }

    public PluginApplicationMenuEntrySecondary(final PluginDescriptor plugin, String description)
    {
        this(plugin, description, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PluginLauncher.start(plugin);
            }
        });
    }

    public PluginApplicationMenuEntrySecondary(final PluginDescriptor plugin)
    {
        this(plugin, plugin.getName());
    }
}

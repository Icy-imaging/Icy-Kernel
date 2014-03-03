/**
 * 
 */
package icy.gui.plugin;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.resource.icon.BasicResizableIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

/**
 * @author Stephane
 */
public class PluginApplicationMenuEntrySecondary extends RibbonApplicationMenuEntrySecondary
{

    public PluginApplicationMenuEntrySecondary(PluginDescriptor plugin, String description, ActionListener action)
    {
        super(new BasicResizableIcon(plugin.getIcon()), description, action, CommandButtonKind.ACTION_ONLY);
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

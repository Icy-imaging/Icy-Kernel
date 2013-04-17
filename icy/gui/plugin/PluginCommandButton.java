/**
 * 
 */
package icy.gui.plugin;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.resource.icon.BasicResizableIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;

/**
 * Class helper to create plugin command button
 * 
 * @author Stephane
 */
public class PluginCommandButton
{
    /**
     * Set a plugin button with specified action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin, boolean doAction)
    {
        final String name = plugin.getName();
        final String className = plugin.getClassName();
        final ImageIcon plugIcon = plugin.getIcon();

        // update text & icon
        button.setText(name);
        button.setIcon(new BasicResizableIcon(plugIcon));
        // save class name here
        button.setName(className);

        button.setActionRichTooltip(new PluginRichToolTip(plugin));

        if (doAction)
        {
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final PluginDescriptor plugin = PluginLoader.getPlugin(((AbstractCommandButton) e.getSource())
                            .getName());

                    if (plugin != null)
                        PluginLauncher.start(plugin);
                }
            });
        }
    }

    /**
     * Set a plugin button with default action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin)
    {
        setButton(button, plugin, true);
    }

    /**
     * Build a plugin button
     */
    public static AbstractCommandButton createButton(PluginDescriptor plugin, boolean toggle, boolean doAction)
    {
        final AbstractCommandButton result;

        // build command button
        if (toggle)
            result = new IcyCommandToggleButton();
        else
            result = new IcyCommandButton();

        setButton(result, plugin, doAction);

        return result;
    }

    /**
     * Build a plugin button with default action (execute plugin)
     */
    public static IcyCommandButton createButton(PluginDescriptor plugin)
    {
        // build with default action listener
        return (IcyCommandButton) createButton(plugin, false, true);
    }

    /**
     * Build a plugin toggle button with default action (execute plugin) if enable.
     */
    public static IcyCommandToggleButton createToggleButton(PluginDescriptor plugin, boolean doAction)
    {
        return (IcyCommandToggleButton) createButton(plugin, true, doAction);
    }
}

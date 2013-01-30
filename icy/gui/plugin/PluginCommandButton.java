/**
 * 
 */
package icy.gui.plugin;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.resource.icon.BasicResizableIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;

/**
 * Class helper to create plugin command button
 * 
 * @author Stephane
 */
public class PluginCommandButton
{
    // Specific action class for plugin command button
    public static class PluginCommandAction implements ActionListener
    {
        final WeakReference<PluginDescriptor> pluginRef;

        public PluginCommandAction(PluginDescriptor plugin)
        {
            super();

            pluginRef = new WeakReference<PluginDescriptor>(plugin);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final PluginDescriptor plugin = pluginRef.get();

            if (plugin != null)
                PluginLauncher.start(plugin);
        }
    }

    /**
     * Set a plugin button with specified action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin, PluginCommandAction action)
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

        // remove previous action listeners
        final ActionListener[] listeners = button.getListeners(ActionListener.class);
        for (ActionListener listener : listeners)
            if (listener instanceof PluginCommandAction)
                button.removeActionListener(listener);

        if (action != null)
            button.addActionListener(action);
    }

    /**
     * Set a plugin button with default action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin)
    {
        setButton(button, plugin, new PluginCommandAction(plugin));
    }

    /**
     * Build a plugin button
     */
    public static AbstractCommandButton createButton(final PluginDescriptor plugin, PluginCommandAction action,
            boolean toggle)
    {
        final AbstractCommandButton result;

        // build command button
        if (toggle)
            result = new IcyCommandToggleButton();
        else
            result = new IcyCommandButton();

        setButton(result, plugin, action);

        return result;
    }

    /**
     * Build a plugin button with specified action
     */
    public static IcyCommandButton createButton(final PluginDescriptor plugin, PluginCommandAction action)
    {
        return (IcyCommandButton) createButton(plugin, action, false);
    }

    /**
     * Build a plugin button with default action (execute plugin)
     */
    public static IcyCommandButton createButton(final PluginDescriptor plugin)
    {
        // build with default action listener
        return (IcyCommandButton) createButton(plugin, new PluginCommandAction(plugin), false);
    }

    /**
     * Build a plugin toggle button with specified action
     */
    public static IcyCommandToggleButton createToggleButton(final PluginDescriptor plugin, PluginCommandAction action)
    {
        return (IcyCommandToggleButton) createButton(plugin, action, true);
    }

    /**
     * Build a plugin toggle button with default action (execute plugin)
     */
    public static IcyCommandToggleButton createToggleButton(final PluginDescriptor plugin)
    {
        return (IcyCommandToggleButton) createButton(plugin, new PluginCommandAction(plugin), true);
    }
}

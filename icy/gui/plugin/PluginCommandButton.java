/**
 * 
 */
package icy.gui.plugin;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.resource.icon.BasicResizableIcon;
import icy.util.StringUtil;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * Class helper to create plugin command button
 * 
 * @author Stephane
 */
public class PluginCommandButton
{
    // Specific action class for plugin command button
    public static abstract class PluginCommandAction implements ActionListener
    {

    }

    /**
     * Set a plugin button with specified action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin, PluginCommandAction action)
    {
        final String name = plugin.getName();
        final String className = plugin.getClassName();
        final String description = plugin.getDescription();
        final String website = plugin.getWeb();
        final String author = plugin.getAuthor();
        final ImageIcon plugIcon = plugin.getIcon();
        final Image plugImg = plugin.getImage();

        // udpate text & icon
        button.setText(name);
        button.setIcon(new BasicResizableIcon(plugIcon));
        // save class name here
        button.setName(className);

        // build richToolTip for command button
        final RichTooltip richToolTip = new RichTooltip();

        richToolTip.setTitle(name);
        if (plugIcon != PluginDescriptor.DEFAULT_ICON)
            richToolTip.setMainImage(plugIcon.getImage());

        if (!StringUtil.isEmpty(description))
            richToolTip.addDescriptionSection(description);
        if (!StringUtil.isEmpty(website))
            richToolTip.addDescriptionSection(website);
        if (!StringUtil.isEmpty(author))
            richToolTip.addDescriptionSection(author);

        if (plugImg != PluginDescriptor.DEFAULT_IMAGE)
            richToolTip.setFooterImage(plugin.getImage());

        button.setActionRichTooltip(richToolTip);

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
    public static void setButton(AbstractCommandButton button, final PluginDescriptor plugin)
    {
        setButton(button, plugin, new PluginCommandAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PluginLauncher.launch(plugin);
            }
        });
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
        return (IcyCommandButton) createButton(plugin, new PluginCommandAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PluginLauncher.launch(plugin);
            }
        }, false);
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
        return (IcyCommandToggleButton) createButton(plugin, new PluginCommandAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PluginLauncher.launch(plugin);
            }
        }, true);
    }
}

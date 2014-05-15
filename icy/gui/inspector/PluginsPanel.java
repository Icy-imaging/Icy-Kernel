/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.inspector;

import icy.gui.component.ImageComponent;
import icy.gui.component.button.IcyButton;
import icy.gui.main.GlobalPluginListener;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.abstract_.Plugin;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Stephane
 */
public class PluginsPanel extends JPanel implements GlobalPluginListener, Runnable
{
    private class PluginComponent extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6723991851130108797L;

        // we use a weak reference so the plugin can be released by GC
        final WeakReference<Plugin> plugin;

        /**
         * internals
         */
        final PluginDescriptor descriptor;

        /**
         * 
         */
        public PluginComponent(Plugin plugin)
        {
            super(true);

            this.plugin = new WeakReference<Plugin>(plugin);
            descriptor = plugin.getDescriptor();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEtchedBorder());
            ComponentUtil.setFixedHeight(this, 24);

            build();
        }

        /**
         * build the component
         */
        private void build()
        {
            removeAll();

            final JComponent image = new ImageComponent(descriptor.getIconAsImage());
            ComponentUtil.setFixedSize(image, new Dimension(20, 20));

            final JLabel label = new JLabel(descriptor.getName());
            label.setToolTipText(descriptor.getName() + " " + descriptor.getVersion());

            final IcyButton killButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_CLOSE, 16));
            killButton.setFlat(true);
            killButton.setEnabled(false);
            killButton.setToolTipText("kill plugin");
            ComponentUtil.setFixedSize(killButton, new Dimension(18, 18));
            killButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // kill plugin, not possible with our current implementation...
                }
            });

            add(image);
            add(Box.createHorizontalStrut(4));
            add(label);
            add(Box.createHorizontalGlue());
            add(killButton);
            add(Box.createHorizontalStrut(2));

            refresh();
        }

        void refresh()
        {
            validate();
        }

        /**
         * @return the plugin
         */
        public Plugin getPlugin()
        {
            return plugin.get();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8950935360929507468L;

    private final JPanel pluginsPanel;

    /**
     * 
     */
    public PluginsPanel()
    {
        super(true);

        pluginsPanel = new JPanel(true);
        pluginsPanel.setLayout(new BoxLayout(pluginsPanel, BoxLayout.PAGE_AXIS));

        setLayout(new BorderLayout());

        add(new JScrollPane(pluginsPanel), BorderLayout.CENTER);

        rebuildPluginPanel();

        validate();
        setVisible(true);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        Icy.getMainInterface().addGlobalPluginListener(this);
    }

    @Override
    public void removeNotify()
    {
        Icy.getMainInterface().removeGlobalPluginListener(this);

        super.removeNotify();
    }

    void rebuildPluginPanel()
    {
        pluginsPanel.removeAll();

        for (Plugin plugin : Icy.getMainInterface().getActivePlugins())
            pluginsPanel.add(new PluginComponent(plugin));
        pluginsPanel.add(Box.createVerticalGlue());

        pluginsPanel.validate();
        // as we use a scroll pane in tab, not nice...
        pluginsPanel.getParent().validate();
        pluginsPanel.getParent().repaint();
    }

    private PluginComponent getPluginComponent(Plugin plugin)
    {
        for (Component comp : pluginsPanel.getComponents())
        {
            if (comp instanceof PluginComponent)
            {
                final PluginComponent pluginComponent = (PluginComponent) comp;

                if (pluginComponent.getPlugin() == plugin)
                    return pluginComponent;
            }
        }

        return null;
    }

    @Override
    public void run()
    {
        // need to be done on EDT
        ThreadUtil.invokeNow(new Runnable()
        {

            @Override
            public void run()
            {
                rebuildPluginPanel();
            }
        });
    }

    @Override
    public void pluginStarted(Plugin plugin)
    {
        ThreadUtil.runSingle(this);
    }

    @Override
    public void pluginEnded(Plugin plugin)
    {
        ThreadUtil.runSingle(this);
    }
}

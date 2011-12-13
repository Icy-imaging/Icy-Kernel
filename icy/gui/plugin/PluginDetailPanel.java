/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.plugin;

import icy.common.Random;
import icy.gui.component.ComponentUtil;
import icy.gui.component.ImageComponent;
import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * @author stephane
 */
public class PluginDetailPanel extends IcyFrame
{
    private class ExecuteActionButton extends JButton implements ActionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3096619820228575930L;

        public ExecuteActionButton()
        {
            super("Execute");

            setVisible(plugin.isActionable());

            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            PluginLauncher.launch(plugin);
        }
    }

    private class CloseActionButton extends JButton implements ActionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4912851751410749786L;

        public CloseActionButton()
        {
            super("Close");

            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            close();
        }
    }

    final PluginDescriptor plugin;

    // private final ChangesLogActionButton changesLogButton;
    private final ExecuteActionButton executeButton;
    private final CloseActionButton closeButton;

    /**
     * @param plugin
     */
    public PluginDetailPanel(PluginDescriptor plugin)
    {
        super(plugin.getName() + " " + plugin.getVersion(), false, true);

        this.plugin = plugin;

        // changesLogButton = new ChangesLogActionButton();
        executeButton = new ExecuteActionButton();
        closeButton = new CloseActionButton();

        setPreferredSize(new Dimension(640, 480));

        build();

        addToMainDesktopPane();
        // random position for more fun
        setLocation(10 * Random.nextInt(20) + 40, 10 * Random.nextInt(10) + 40);
        setVisible(true);
        requestFocus();

        if (!plugin.isLoaded())
        {
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    // load plugin descriptor & images
                    PluginDetailPanel.this.plugin.loadDescriptor();
                    PluginDetailPanel.this.plugin.loadImages();

                    // rebuild interface
                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            build();
                        }
                    });
                }
            });
        }
    }

    private JPanel getTopPanel()
    {
        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        // image at left
        final ImageComponent image = new ImageComponent(plugin.getImage());
        ComponentUtil.setFixedSize(image, new Dimension(256, 256));
        panel.add(image);

        // infos panel at right
        final JPanel infos = new JPanel();

        infos.setLayout(new BoxLayout(infos, BoxLayout.PAGE_AXIS));
        infos.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        infos.add(GuiUtil.createTabBoldLabel("Author", 1));
        infos.add(GuiUtil.createTabLabel(plugin.getAuthor(), 32));
        infos.add(Box.createVerticalStrut(4));
        infos.add(GuiUtil.createTabBoldLabel("Web site", 1));
        infos.add(GuiUtil.createTabLabel(plugin.getWeb(), 32));
        infos.add(Box.createVerticalStrut(4));
        infos.add(GuiUtil.createTabBoldLabel("Email", 1));
        infos.add(GuiUtil.createTabLabel(plugin.getEmail(), 32));
        infos.add(Box.createVerticalStrut(4));
        // infos.add(createBoldLabel("Capabilities"));
        // infos.add(createBooleanLabel(PluginDescriptor.TYPE_IMAGE_ANALYSIS));
        // infos.add(createBooleanLabel(PluginDescriptor.TYPE_FILE));
        // infos.add(createBooleanLabel(PluginDescriptor.TYPE_CFG_PARAM));
        // infos.add(createBooleanLabel(PluginDescriptor.TYPE_ROI));
        // infos.add(createBooleanLabel(PluginDescriptor.TYPE_UNDOABLE));
        infos.add(GuiUtil.createTabBoldLabel("Description", 1));
        infos.add(GuiUtil.createTabArea(plugin.getDescription(), 32, 140));
        infos.add(Box.createVerticalGlue());

        panel.add(infos);

        return panel;
    }

    private JPanel getMedPanel()
    {
        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        panel.add(GuiUtil.createTabBoldLabel("ChangeLog", 1));
        panel.add(Box.createVerticalStrut(4));
        panel.add(GuiUtil.createTabArea(plugin.getChangesLog(), 32, 160));

        return panel;
    }

    private JPanel getBottomPanel()
    {
        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        // panel.add(Box.createHorizontalStrut(5));
        // panel.add(changesLogButton);
        panel.add(Box.createHorizontalStrut(4));
        // panel.add(Box.createHorizontalGlue());
        panel.add(executeButton);
        panel.add(Box.createHorizontalGlue());
        panel.add(Box.createHorizontalStrut(4));
        panel.add(closeButton);
        panel.add(Box.createHorizontalStrut(4));

        return panel;
    }

    void build()
    {
        getContentPane().removeAll();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        add(getTopPanel());
        add(getMedPanel());
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(Box.createVerticalStrut(8));
        add(getBottomPanel());
        add(Box.createVerticalStrut(6));

        pack();
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.swing.ICYFrame#updateUI()
     */
    @Override
    public void updateUI()
    {
        build();
    }
}

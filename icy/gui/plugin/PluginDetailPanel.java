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
package icy.gui.plugin;

import icy.gui.component.ImageComponent;
import icy.gui.frame.IcyFrame;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.system.thread.ThreadUtil;
import icy.util.Random;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * @author stephane
 */
public class PluginDetailPanel extends IcyFrame implements HyperlinkListener
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
            PluginLauncher.start(plugin);
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
     * gui
     */
    final JPanel imagePanel;
    final ImageComponent pluginImage;
    final JLabel pluginAuthorLabel;
    final JLabel pluginWebsiteLabel;
    final JLabel pluginEmailLabel;
    final JTextPane pluginDescriptionText;
    final JTextPane pluginChangeLogText;

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

        // build top panel
        pluginDescriptionText = new JTextPane();
        pluginDescriptionText.setContentType("text/html");
        pluginDescriptionText.setEditable(false);
        pluginDescriptionText.setOpaque(false);
        pluginDescriptionText.addHyperlinkListener(this);
        ComponentUtil.setFixedHeight(pluginDescriptionText, 256);

        final JScrollPane scDescription = new JScrollPane(pluginDescriptionText);
        scDescription.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        pluginImage = new ImageComponent(plugin.getImage());

        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        imagePanel.add(pluginImage, BorderLayout.CENTER);

        final JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(imagePanel, BorderLayout.WEST);
        topPanel.add(scDescription, BorderLayout.CENTER);

        // center panel
        pluginAuthorLabel = new JLabel();
        pluginWebsiteLabel = new JLabel();
        pluginEmailLabel = new JLabel();

        final JPanel infosPanel = new JPanel();
        infosPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
        infosPanel.setLayout(new BoxLayout(infosPanel, BoxLayout.PAGE_AXIS));

        infosPanel.add(GuiUtil.createTabBoldLabel("Author", 1));
        infosPanel.add(GuiUtil.createTabLabel(pluginAuthorLabel, 32));
        infosPanel.add(Box.createVerticalStrut(4));
        infosPanel.add(GuiUtil.createTabBoldLabel("Web site", 1));
        infosPanel.add(GuiUtil.createTabLabel(pluginWebsiteLabel, 32));
        infosPanel.add(Box.createVerticalStrut(4));
        infosPanel.add(GuiUtil.createTabBoldLabel("Email", 1));
        infosPanel.add(GuiUtil.createTabLabel(pluginEmailLabel, 32));
        infosPanel.add(Box.createVerticalStrut(4));
        infosPanel.add(Box.createVerticalGlue());

        pluginChangeLogText = new JTextPane();
        pluginChangeLogText.setContentType("text/html");
        pluginChangeLogText.setEditable(false);
        pluginChangeLogText.setOpaque(false);
        pluginChangeLogText.addHyperlinkListener(this);

        final JScrollPane scChangeLog = new JScrollPane(pluginChangeLogText);
        scChangeLog.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        final JPanel changeLogPanel = new JPanel(new BorderLayout());
        changeLogPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 4));

        changeLogPanel.add(GuiUtil.createTabBoldLabel("ChangeLog", 1), BorderLayout.NORTH);
        changeLogPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(32), scChangeLog), BorderLayout.CENTER);

        final JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
        centerPanel.add(infosPanel, BorderLayout.WEST);
        centerPanel.add(changeLogPanel, BorderLayout.CENTER);

        // bottom panel
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        buttonsPanel.add(executeButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(Box.createHorizontalStrut(4));
        buttonsPanel.add(closeButton);

        final JPanel bottomPanel = new JPanel(new BorderLayout());

        bottomPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
        bottomPanel.add(buttonsPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addToMainDesktopPane();
        // random position for more fun
        setLocation(10 * Random.nextInt(20) + 40, 10 * Random.nextInt(10) + 40);
        setVisible(true);
        requestFocus();

        if (!plugin.isAllLoaded())
        {
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    PluginDetailPanel.this.plugin.loadAll();

                    // rebuild interface
                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updateGui();
                        }
                    });
                }
            });
        }
        else
            updateGui();
    }

    void updateGui()
    {
        final Font sysFont = pluginAuthorLabel.getFont();
        final Image img = plugin.getImage();
        final String description = plugin.getDescription();
        final String changesLog = plugin.getChangesLog();
        final String author = plugin.getAuthor();
        final String email = plugin.getEmail();
        final String web = plugin.getWeb();

        pluginImage.setImage(img);

        if (StringUtil.isEmpty(description))
            pluginDescriptionText.setText("No description");
        else
        {
            if (StringUtil.containHtmlCR(description))
                pluginDescriptionText.setText(StringUtil.removeCR(description));
            else
                pluginDescriptionText.setText(StringUtil.toHtmlCR(description));

            pluginDescriptionText.setCaretPosition(0);
        }

        ComponentUtil.setJTextPaneFont(pluginDescriptionText, sysFont, Color.black);

        if (StringUtil.isEmpty(changesLog))
            pluginChangeLogText.setText("---");
        else
        {
            pluginChangeLogText.setText(StringUtil.toHtmlCR(changesLog));
            pluginChangeLogText.setCaretPosition(0);
        }
        ComponentUtil.setJTextPaneFont(pluginChangeLogText, new Font("courier", Font.PLAIN, 11), Color.black);
        if (StringUtil.isEmpty(author))
            pluginAuthorLabel.setText("---");
        else
            pluginAuthorLabel.setText(author);
        if (StringUtil.isEmpty(email))
            pluginEmailLabel.setText("---");
        else
            pluginEmailLabel.setText(email);
        if (StringUtil.isEmpty(web))
            pluginWebsiteLabel.setText("---");
        else
            pluginWebsiteLabel.setText(web);

        pack();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            NetworkUtil.openBrowser(e.getURL());
    }
}

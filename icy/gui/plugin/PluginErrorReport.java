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

import icy.gui.component.ComponentUtil;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.util.GuiUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginUpdater;
import icy.plugin.abstract_.Plugin;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

/**
 * This class create a report from a plugin crash and ask the
 * user if he wants to send it to the dev team of the plugin.
 * 
 * @author Fabrice de Chaumont & Stephane<br>
 */
public class PluginErrorReport
{
    public static void report(final PluginDescriptor plugin, final String message)
    {
        final CancelableProgressFrame info = new CancelableProgressFrame("Plugin '" + plugin.getName()
                + "' has crashed, searching for update...");

        // always do that in background process
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                PluginDescriptor onlinePlugin = null;

                try
                {
                    // load all online plugins
                    PluginRepositoryLoader.loadAll(false, false, false);

                    // search for update
                    if (!info.isCancelRequested())
                        onlinePlugin = PluginUpdater.checkUpdate(plugin);
                }
                finally
                {
                    info.close();
                }

                if (!info.isCancelRequested())
                {
                    // update found
                    if (onlinePlugin != null)
                    {
                        // confim and install
                        if (ConfirmDialog
                                .confirm(
                                        "Plugin update",
                                        "An update is available for this plugin.\n"
                                                + "It is highly recommended to install the update as you meet problem with current version.\n"
                                                + "Do you want to install the update ?"))
                            PluginInstaller.install(onlinePlugin, true);
                    }
                    else
                    {
                        // display report as no update were found
                        ThreadUtil.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showReport(plugin, message);
                            }
                        });
                    }
                }
            }
        });
    }

    static void showReport(final PluginDescriptor plugin, final String message)
    {
        final JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");

        try
        {
            textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), message,
                    new SimpleAttributeSet());
        }
        catch (BadLocationException e)
        {
            System.err.println("new PluginErrorReport(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        textPane.setCaretPosition(textPane.getStyledDocument().getLength());

        final JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(new TitledBorder("Detail of the message"));
        ComponentUtil.setFixedSize(scrollPane, new Dimension(600, 300));

        final JTextPane commentPane = new JTextPane();
        commentPane.setEditable(true);

        final JScrollPane scComment = new JScrollPane(commentPane);
        scComment.setBorder(new TitledBorder("Comment"));
        ComponentUtil.setFixedSize(scrollPane, new Dimension(600, 150));

        final JLabel label = new JLabel("<html><center><br>The plugin named <b>" + plugin.getName()
                + "</b> has encountered a problem.<br><br>"
                + "Reporting this problem is anonymous and will help improving this plugin" + "<br><br></html>",
                SwingConstants.CENTER);
        final JButton reportButton = new JButton("Report");
        final JButton closeButton = new JButton("Close");

        final JPanel panel = GuiUtil.generatePanel();
        panel.add(GuiUtil.besidesPanel(label));
        panel.add(GuiUtil.besidesPanel(new JLabel(" "), closeButton, new JLabel(" "), reportButton, new JLabel(" ")));
        panel.add(GuiUtil.besidesPanel(new JLabel(" ")));
        panel.add(GuiUtil.besidesPanel(scrollPane));
        panel.add(GuiUtil.besidesPanel(scComment));

        final IcyFrame icyFrame = GuiUtil.generateTitleFrame("Bug report", panel, new Dimension(200, 100), true, true,
                true, true);
        icyFrame.setPreferredSize(new Dimension(600, 600));

        icyFrame.pack();
        icyFrame.addToMainDesktopPane();
        icyFrame.center();
        icyFrame.setVisible(true);
        icyFrame.requestFocus();

        reportButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    final Document commentDoc = commentPane.getDocument();
                    final String comment = commentDoc.getText(0, commentDoc.getLength());

                    String error;

                    if (!StringUtil.isEmpty(comment))
                        error = "Comment:\n" + comment;
                    else
                        error = "";

                    final Document errorDoc = textPane.getDocument();
                    error = error + "\n\n" + errorDoc.getText(0, errorDoc.getLength());

                    Plugin.report(plugin, error);
                }
                catch (BadLocationException ex)
                {
                    System.err.println("Error while reporting error :");
                    IcyExceptionHandler.showErrorMessage(ex, true);
                }

                icyFrame.close();
            }
        });
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                icyFrame.close();
            }
        });
    }
}

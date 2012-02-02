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

import icy.gui.dialog.ConfirmDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.util.GuiUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginUpdater;
import icy.plugin.abstract_.Plugin;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.text.StyleConstants;

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
                    // search for update
                    if (!info.isCancelRequested())
                        onlinePlugin = PluginUpdater.getUpdate(plugin);
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

        // COMMENT PANE
        final JTextPane commentPane = new JTextPane();
        commentPane.setEditable(true);
        final Document doc = commentPane.getDocument();
        try {
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setItalic(attributes, true);
            StyleConstants.setForeground(attributes, Color.GRAY);
			doc.insertString(0, "Please type here your comment", attributes);
		} catch (BadLocationException e1) {
		}
        commentPane.setMinimumSize(new Dimension(0,200));
        commentPane.addMouseListener(new MouseAdapter() {
        	// Displays a message at the beginning that
        	// disappears when first clicked
        	boolean firstClickDone = false;
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if (!firstClickDone) {
        			commentPane.setText("");
        			SimpleAttributeSet attributes = new SimpleAttributeSet();
        			StyleConstants.setItalic(attributes, false);
        	        StyleConstants.setForeground(attributes, Color.BLACK);
        	        try {
						doc.insertString(0, " ", attributes);
					} catch (BadLocationException e1) {
					}
        			firstClickDone = true;
        		}
        	}
		});

        final JScrollPane scComment = new JScrollPane(commentPane);
        scComment.setBorder(new TitledBorder("Comment"));

        // Description
        final JLabel label = new JLabel("<html><br>The plugin named <b>" + plugin.getName()
                + "</b> has encountered a problem.<br><br>"
                + "Reporting this problem is anonymous and will help improving this plugin" + "<br><br></html>",
                SwingConstants.CENTER);
        final JButton reportButton = new JButton("Report");
        final JButton closeButton = new JButton("Close");
        final JButton btnShowDetails = new JButton("Show details");
        
        // North message + icon
        JPanel panelPluginAndMessage = new JPanel();
        panelPluginAndMessage.setLayout(new BoxLayout(panelPluginAndMessage, BoxLayout.X_AXIS));
        panelPluginAndMessage.add(new JLabel(plugin.getIcon()));
        panelPluginAndMessage.add(Box.createHorizontalStrut(10));
        panelPluginAndMessage.add(GuiUtil.besidesPanel(label));
        
        // north part
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.setBorder(BorderFactory.createEmptyBorder(4,10,4,4));
        panelNorth.add(panelPluginAndMessage);
        panelNorth.add(GuiUtil.besidesPanel(new JLabel(" "),btnShowDetails,new JLabel(" ")));

        // middle part
        final JPanel detailsPanel = GuiUtil.generatePanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
        detailsPanel.add(scComment);
        
        // generating the GUI
        final JPanel mainPanel = GuiUtil.generatePanel();
        mainPanel.setLayout(new BorderLayout());        
        mainPanel.add(panelNorth,BorderLayout.NORTH);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        mainPanel.add(GuiUtil.besidesPanel(new JLabel(" "), closeButton, new JLabel(" "), reportButton, new JLabel(" ")),BorderLayout.SOUTH);

        final IcyFrame icyFrame = GuiUtil.generateTitleFrame("Bug report", mainPanel, new Dimension(200, 70), true, true,
                true, true);

        icyFrame.setVisible(true);
        icyFrame.setSize(450,380);
        icyFrame.addToMainDesktopPane();
        icyFrame.requestFocus();
        icyFrame.center();
        Point centeredLocationOnScreen = icyFrame.getLocation();
        icyFrame.setLocation(centeredLocationOnScreen.x, centeredLocationOnScreen.y - 100);

        btnShowDetails.addActionListener(new ActionListener() {			
        	boolean active = false;
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				int w = icyFrame.getWidth();
		        int h = icyFrame.getHeight();
		        
		        // add / remove panel
				if (active) {
		        	detailsPanel.remove(scrollPane);
		        	btnShowDetails.setText("Show Details");
		        	icyFrame.setSize(w,h - (scrollPane.getHeight() + 10));
		        } else {
		        	detailsPanel.add(scrollPane,0);
		        	btnShowDetails.setText("Hide Details");
		        	icyFrame.setSize(w,h + 200);
		        }
		        active=!active;
		        detailsPanel.revalidate();
			}
		});
        
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

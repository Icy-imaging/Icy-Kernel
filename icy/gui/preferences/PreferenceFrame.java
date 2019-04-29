/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.gui.preferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import icy.gui.dialog.ConfirmDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.util.ComponentUtil;
import icy.gui.util.WindowPositionSaver;
import icy.main.Icy;
import icy.preferences.IcyPreferences;
import icy.util.StringUtil;

/**
 * @author fab & stephane
 */
public class PreferenceFrame extends IcyFrame implements TreeSelectionListener
{
    public static final String NODE_NAME = "Preferences";

    /** singleton frame */
    private static PreferenceFrame instance = null;

    // needed to keep trace of non singleton frame
    List<PreferencePanel> preferencePanels;

    private JTree tree;
    private JSplitPane mainPanel;
    private DefaultMutableTreeNode node;

    boolean needRestart;
    boolean doRestart;

    // we need to keep reference on it as the object only use weak reference
    WindowPositionSaver positionSaver;

    public PreferenceFrame(String section)
    {
        super(NODE_NAME, true, true, false, false);

        // frame already opened ?
        if (instance != null)
        {
            // just put it on front
            instance.toFront();
            return;
        }
        instance = this;

        positionSaver = new WindowPositionSaver(this, "frame/preference", new Point(100, 100), new Dimension(640, 480));

        needRestart = false;
        doRestart = false;

        preferencePanels = new ArrayList<PreferencePanel>();

        preferencePanels.add(new GeneralPreferencePanel(this));
        preferencePanels.add(new GUICanvasPreferencePanel(this));
        preferencePanels.add(new NetworkPreferencePanel(this));
        // preferencePanels.add(new ChatPreferencePanel(this));
        preferencePanels.add(new RepositoryPreferencePanel(this));
        preferencePanels.add(new PluginPreferencePanel(this));
        preferencePanels.add(new PluginLocalPreferencePanel(this));
        preferencePanels.add(new PluginOnlinePreferencePanel(this));
        preferencePanels.add(new PluginStartupPreferencePanel(this));
        preferencePanels.add(new WorkspacePreferencePanel(this));
        preferencePanels.add(new WorkspaceLocalPreferencePanel(this));
        preferencePanels.add(new WorkspaceOnlinePreferencePanel(this));
        // TODO : add here PreferencePanel type plugins

        // build selection tree
        node = new DefaultMutableTreeNode(NODE_NAME);

        for (PreferencePanel panel : preferencePanels)
            getNode(panel.getParentName()).add(panel.getNode());

        tree = new JTree(node);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // tree.setOpaque(true);
        tree.setRootVisible(false);
        // select first node
        tree.setSelectionRow(0);
        ComponentUtil.expandAllTree(tree);
        // disable double click expansion/reduction
        tree.setToggleClickCount(0);
        tree.addTreeSelectionListener(this);

        final JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setMinimumSize(new Dimension(136, 100));

        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, null);
        mainPanel.setDividerSize(6);
        mainPanel.setDividerLocation(150);

        // final JButton helpButton = new Help(NODE_NAME).getHelpButton("Help");
        final JButton resetButton = new JButton("Reset to default");
        final JButton cancelButton = new JButton("Cancel");
        final JButton okButton = new JButton("Ok");
        final JButton applyButton = new JButton("Apply");

        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // need confirmation
                if (ConfirmDialog.confirm("Reset all setting to default values ?"))
                {
                    // clear all preferences
                    IcyPreferences.clear();
                    // reload
                    for (PreferencePanel panel : preferencePanels)
                        panel.load();
                }
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // just close the frame
                close();
            }
        });

        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // set back panel informations in preferences
                for (PreferencePanel panel : preferencePanels)
                    panel.save();
                // validate the need restart state
                doRestart = needRestart;
                // then close the frame
                close();
            }
        });

        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // set back panel informations in preferences
                for (PreferencePanel panel : preferencePanels)
                    panel.save();
                // validate the need restart state
                doRestart = needRestart;
            }
        });

        final JPanel bottomPanel = new JPanel();

        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        // bottomPanel.add(helpButton);
        // bottomPanel.add(Box.createHorizontalStrut(8));
        bottomPanel.add(resetButton);
        bottomPanel.add(Box.createHorizontalStrut(8));
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(applyButton);
        bottomPanel.add(Box.createHorizontalStrut(8));
        bottomPanel.add(okButton);
        bottomPanel.add(Box.createHorizontalStrut(8));
        bottomPanel.add(cancelButton);

        final JPanel tmpPanel = new JPanel();
        tmpPanel.setLayout(new BoxLayout(tmpPanel, BoxLayout.PAGE_AXIS));
        tmpPanel.add(new JSeparator());
        tmpPanel.add(bottomPanel);

        setLayout(new BorderLayout());

        add(mainPanel, BorderLayout.CENTER);
        add(tmpPanel, BorderLayout.SOUTH);

        updateRightPanel();

        setMinimumSize(new Dimension(640, 440));
        setPreferredSize(new Dimension(800, 600));

        addToDesktopPane();
        setVisible(true);

        if (!StringUtil.isEmpty(section))
            setSelection(section);
    }

    @Override
    public void onClosed()
    {
        // test if we are on singleton frame
        if (preferencePanels != null)
        {
            for (PreferencePanel panel : preferencePanels)
                panel.closed();

            instance = null;

            // restart needed and validated, ask user
            if (doRestart)
            {
                Icy.confirmRestart();
                doRestart = false;
            }
        }

        super.onClosed();
    }

    private DefaultMutableTreeNode getNode(String name)
    {
        // first search directly on root node
        if (node.getUserObject().equals(name))
            return node;

        // then search in children
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            final DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(i);

            if (n.getUserObject().equals(name))
                return n;
        }

        return null;
    }

    private void updateRightPanel()
    {
        final Object selected = tree.getSelectionPath().getLastPathComponent();

        for (PreferencePanel panel : preferencePanels)
        {
            if (panel.getNode().equals(selected))
            {
                final int divLocation = mainPanel.getDividerLocation();

                panel.setVisible(true);
                mainPanel.setRightComponent(panel);
                // restore divider location (lost with setRightComponent)
                mainPanel.setDividerLocation(divLocation);
            }
            else
                panel.setVisible(true);
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        updateRightPanel();
    }

    public void setSelection(String selection)
    {
        for (PreferencePanel panel : preferencePanels)
        {
            final DefaultMutableTreeNode node = panel.getNode();

            if (node.getUserObject().equals(selection))
            {
                tree.setSelectionPath(ComponentUtil.buildTreePath(node));
                return;
            }
        }
    }

    public void setNeedRestart()
    {
        needRestart = true;
    }
}

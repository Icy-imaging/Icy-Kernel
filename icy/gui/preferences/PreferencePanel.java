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

import icy.gui.util.ComponentUtil;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Fabrice de Chaumont
 */
public abstract class PreferencePanel extends JPanel
{
    private static final long serialVersionUID = 4602116758638991276L;

    protected final PreferenceFrame parentFrame;

    protected final String parentName;
    protected final DefaultMutableTreeNode node;

    protected final JPanel mainPanel;

    public PreferencePanel(PreferenceFrame parentFrame, String name, String parentName)
    {
        super();

        setName(name);

        this.parentFrame = parentFrame;
        this.parentName = parentName;

        node = new DefaultMutableTreeNode(name);

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        final JLabel titleLabel = new JLabel("  " + name);

        ComponentUtil.increaseFontSize(titleLabel, 2);
        ComponentUtil.setFontBold(titleLabel);

        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * called when panel is closed
     */
    protected void closed()
    {

    }

    public PreferenceFrame getPreferenceFrame()
    {
        return parentFrame;
    }

    public PreferencePanel getPreferencePanel(Class<? extends PreferencePanel> type)
    {
        for (PreferencePanel panel : parentFrame.preferencePanels)
            if (type.isInstance(panel))
                return panel;

        return null;
    }

    public DefaultMutableTreeNode getNode()
    {
        return node;
    }

    public String getParentName()
    {
        return parentName;
    }

    protected abstract void load();

    protected abstract void save();

}

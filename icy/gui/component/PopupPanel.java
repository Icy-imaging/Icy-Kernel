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
package icy.gui.component;

import icy.gui.component.button.IcyToggleButton;
import icy.gui.util.ComponentUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.util.GraphicsUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Stephane
 */
public class PopupPanel extends JPanel
{
    private class PopupTitlePanel extends IcyToggleButton
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6311966421110920079L;

        public PopupTitlePanel(String text, Image image)
        {
            super(text, new IcyIcon(image, 14));

            setHorizontalAlignment(SwingConstants.LEADING);
            setFocusPainted(false);

            if (subPopupPanel)
                ComponentUtil.setFixedHeight(this, getTextSize().height);
            else
                ComponentUtil.setFontBold(this);

            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    refresh();
                }
            });
        }

        @Override
        public void setText(String text)
        {
            super.setText(text);

            updateIconTextGap();
        }

        @Override
        public void setIcon(Icon defaultIcon)
        {
            super.setIcon(defaultIcon);

            updateIconTextGap();
        }

        @Override
        public void setBounds(int x, int y, int width, int height)
        {
            super.setBounds(x, y, width, height);

            updateIconTextGap();
        }

        private Dimension getTextSize()
        {
            final String text = getText();

            if (text != null)
            {
                final Rectangle2D r = GraphicsUtil.getStringBounds(this, text);
                return new Dimension((int) r.getWidth(), (int) r.getHeight());
            }

            return new Dimension(0, 0);
        }

        private void updateIconTextGap()
        {
            final int width = getWidth();
            final Icon icon = getIcon();

            if ((width != 0) && (icon != null))
            {
                // adjust icon gap to new width
                int iconTextGap = (width - getTextSize().width) / 2;
                iconTextGap -= (icon.getIconWidth() + 10);
                setIconTextGap(iconTextGap);
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5208183544572376729L;

    protected final PopupTitlePanel topPanel;
    protected final JPanel mainPanel;

    protected final boolean subPopupPanel;

    /**
     * @deprecated Use {@link #PopupPanel(String, boolean)} instead
     */
    @Deprecated
    public PopupPanel(String title, int panelHeight, boolean subPopupPanel)
    {
        this(title, subPopupPanel);

    }

    /**
     * @deprecated Use {@link #PopupPanel(String, boolean)} instead
     */
    @Deprecated
    public PopupPanel(String title, int panelHeight)
    {
        this(title, false);
    }

    /**
     * Create a new popup panel with specified title.
     * 
     * @param title
     *        Panel title
     * @param subPanel
     *        Determine if this is an embedded popup panel or a normal one.
     */
    public PopupPanel(String title, boolean subPanel)
    {
        super();

        subPopupPanel = subPanel;

        topPanel = new PopupTitlePanel(title, ResourceUtil.ICON_PANEL_COLLAPSE);
        mainPanel = new JPanel();
        // if (panelHeight != -1)
        // ComponentUtil.setFixedHeight(mainPanel, panelHeight);

        setBorder(BorderFactory.createRaisedBevelBorder());
        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        refresh();
    }

    /**
     * Create a new popup panel with specified title.
     */
    public PopupPanel(String title)
    {
        this(title, false);
    }

    /**
     * @deprecated Use {@link #PopupPanel(String)} instead.
     */
    @Deprecated
    public PopupPanel()
    {
        this("no title", false);
    }

    public String getTitle()
    {
        return topPanel.getText();
    }

    public void setTitle(String value)
    {
        topPanel.setText(value);
    }

    /**
     * @return the title panel
     */
    public PopupTitlePanel getTitlePanel()
    {
        return topPanel;
    }

    /**
     * @return the mainPanel
     */
    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    /**
     * @return the collapsed
     */
    public boolean isCollapsed()
    {
        return !isExpanded();
    }

    /**
     * @return the collapsed
     */
    public boolean isExpanded()
    {
        return topPanel.isSelected();
    }

    /**
     * @param value
     *        the collapsed to set
     */
    public void setExpanded(boolean value)
    {
        if (topPanel.isSelected() != value)
        {
            topPanel.setSelected(value);
            refresh();
        }
    }

    /**
     * @return the subPopupPanel
     */
    public boolean isSubPopupPanel()
    {
        return subPopupPanel;
    }

    public void expand()
    {
        setExpanded(true);
    }

    public void collapse()
    {
        setExpanded(false);
    }

    void refresh()
    {
        if (subPopupPanel)
        {
            if (topPanel.isSelected())
                topPanel.setIcon(new IcyIcon(ResourceUtil.ICON_NODE_EXPANDED, 10));
            else
                topPanel.setIcon(new IcyIcon(ResourceUtil.ICON_NODE_COLLAPSED, 10));
        }
        else
        {
            if (topPanel.isSelected())
                topPanel.setIcon(new IcyIcon(ResourceUtil.ICON_NODE_EXPANDED, 14));
            else
                topPanel.setIcon(new IcyIcon(ResourceUtil.ICON_NODE_COLLAPSED, 14));
        }

        mainPanel.setVisible(topPanel.isSelected());
    }
}

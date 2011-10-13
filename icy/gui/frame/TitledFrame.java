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
package icy.gui.frame;

import icy.gui.component.IcyLogo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class TitledFrame extends IcyFrame
{
    protected final JPanel mainPanel;
    protected final IcyLogo logo;

    public TitledFrame(String title)
    {
        this(title, null, false, false, false, false);
    }

    public TitledFrame(String title, boolean resizable)
    {
        this(title, null, resizable, false, false, false);
    }

    public TitledFrame(String title, boolean resizable, boolean closable)
    {
        this(title, null, resizable, closable, false, false);
    }

    public TitledFrame(String title, boolean resizable, boolean closable, boolean maximizable)
    {
        this(title, null, resizable, closable, maximizable, false);
    }

    public TitledFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
    {
        this(title, null, resizable, closable, maximizable, iconifiable);
    }

    public TitledFrame(String title, Dimension dim, boolean resizable, boolean closable, boolean maximizable,
            boolean iconifiable)
    {
        super(title, resizable, closable, maximizable, iconifiable);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());

        logo = new IcyLogo(title, dim);

        add(logo, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * @return the mainPanel
     */
    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    /**
     * Display or not the ICY black title
     */
    public void setTitleVisible(boolean value)
    {
        logo.setVisible(value);
    }

    /**
     * Return true if ICY black title is visible
     */
    public boolean isTitleVisible()
    {
        return logo.isVisible();
    }
}

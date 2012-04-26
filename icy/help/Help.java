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
package icy.help;

import icy.gui.component.button.IcyCommandMenuButton;
import icy.network.NetworkUtil;
import icy.resource.icon.IcyIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.pushingpixels.flamingo.api.common.JCommandMenuButton;

/**
 * @author fab
 */
public class Help implements KeyListener, ActionListener
{
    /**
     * end of url to show
     */
    private String url;

    /**
     * @param url
     */
    public Help(String url)
    {
        this.url = url;
    }

    public void diplayHelp()
    {
        ShowHelp(url);
    }

    /**
     * Bind component and listen to Help Key ( F1 on win and apple + ? on MacOSX )
     * 
     * @param component
     * @param url
     */
    public Help(Component component, String url)
    {
        this.url = url;

        if (component != null)
        {
            component.addKeyListener(this);
            component.setFocusable(true);
        }
    }

    /**
     * return a JButton to place wherever you wish in your app.
     */
    public JButton getHelpButton()
    {
        JButton Helpbutton = new JButton("?");
        Helpbutton.addActionListener(this);
        return Helpbutton;
    }

    public JButton getHelpButton(String s)
    {
        JButton Helpbutton = new JButton(s);
        Helpbutton.addActionListener(this);
        return Helpbutton;
    }

    public JCommandMenuButton getCommandMenuButton(String s)
    {
        JCommandMenuButton Helpbutton = new JCommandMenuButton(s, null);
        Helpbutton.addActionListener(this);
        return Helpbutton;
    }

    public IcyCommandMenuButton getIcyCommandMenuButton(String s)
    {
        IcyCommandMenuButton Helpbutton = new IcyCommandMenuButton(s, new IcyIcon("browser"));
        Helpbutton.addActionListener(this);
        return Helpbutton;
    }

    public JMenuItem getMenuItem()
    {
        JMenuItem item = new JMenuItem("Help");
        item.addActionListener(this);
        return item;
    }

    /**
     * return a new menu item with a specific label.
     * 
     * @param description
     */
    public JMenuItem getMenuItem(String description)
    {
        JMenuItem item = new JMenuItem(description);
        item.addActionListener(this);
        return item;
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        ShowHelp(url);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if ((e.getKeyCode() == KeyEvent.VK_HELP) || (e.getKeyCode() == KeyEvent.VK_F1))
            ShowHelp(url);
    }

    private void ShowHelp(String url)
    {
        if (url != null)
            NetworkUtil.openURL("http://www.bioimageanalysis.org/icy/index.php?" + url);
    }

    @Override
    public void keyReleased(KeyEvent arg0)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

}

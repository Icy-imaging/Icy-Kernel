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
package icy.gui.util;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Swing utilities class.
 * 
 * @author Stephane
 */
public class SwingUtil
{
    /**
     * Class used to wrap a AWT CheckboxMenuItem in a Swing JCheckBoxMenuItem.
     * 
     * @author Stephane
     */
    private static class JCheckBoxMenuItemWrapper extends JCheckBoxMenuItem implements ActionListener, ItemListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3283063959812167447L;

        final CheckboxMenuItem checkboxMenuItem;

        public JCheckBoxMenuItemWrapper(CheckboxMenuItem checkboxMenuItem)
        {
            super(checkboxMenuItem.getLabel(), checkboxMenuItem.getState());

            // keep reference on source MenuItem
            this.checkboxMenuItem = checkboxMenuItem;

            setActionCommand(checkboxMenuItem.getActionCommand());

            checkboxMenuItem.addItemListener(this);

            addActionListener(this);
            addItemListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            // change source with original one
            e.setSource(checkboxMenuItem);

            // dispatch to original listeners
            for (ActionListener al : checkboxMenuItem.getActionListeners())
                al.actionPerformed(e);
        }

        @Override
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == checkboxMenuItem)
                setSelected(checkboxMenuItem.getState());
            else
            {
                final boolean state = isSelected();

                if (checkboxMenuItem.getState() != state)
                {
                    checkboxMenuItem.setState(isSelected());

                    // build event
                    final ItemEvent iv = new ItemEvent(checkboxMenuItem, ItemEvent.ITEM_STATE_CHANGED, getText(),
                            state ? ItemEvent.SELECTED : ItemEvent.DESELECTED);

                    // dispatch to original listeners
                    for (ItemListener il : checkboxMenuItem.getItemListeners())
                        il.itemStateChanged(iv);
                }
            }
        }
    }

    /**
     * Class used to wrap a AWT MenuItem in a Swing JMenuItem.
     * 
     * @author Stephane
     */
    private static class JMenuItemWrapper extends JMenuItem implements ActionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3283063959812167447L;

        final MenuItem menuItem;

        public JMenuItemWrapper(MenuItem menuItem)
        {
            super(menuItem.getLabel());

            // keep reference on source MenuItem
            this.menuItem = menuItem;

            setActionCommand(menuItem.getActionCommand());

            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            // change source with original one
            e.setSource(menuItem);

            // dispatch to original listeners
            for (ActionListener al : menuItem.getActionListeners())
                al.actionPerformed(e);
        }
    }

    /**
     * Convert a AWT MenuBar to a Swing JMenuBar.
     */
    public static JMenuBar getJMenuBar(MenuBar menuBar, boolean heavy)
    {
        final JMenuBar result = new JMenuBar();

        if (menuBar != null)
        {
            for (int i = 0; i < menuBar.getMenuCount(); i++)
                result.add(getJMenu(menuBar.getMenu(i), heavy));
        }

        return result;
    }

    /**
     * Convert a AWT Menu to a Swing JMenu.
     */
    public static JMenu getJMenu(Menu menu, boolean heavy)
    {
        final JMenu result = new JMenu();

        if (menu != null)
        {
            result.setText(menu.getLabel());
            if (heavy)
                result.getPopupMenu().setLightWeightPopupEnabled(false);

            for (int i = 0; i < menu.getItemCount(); i++)
                result.add(getJMenuItem(menu.getItem(i), heavy));
        }

        return result;
    }

    /**
     * Convert a AWT MenuItem to a Swing JMenuItem.
     */
    public static JMenuItem getJMenuItem(MenuItem menuItem, boolean heavy)
    {
        if (menuItem != null)
        {
            if (menuItem instanceof Menu)
                return getJMenu((Menu) menuItem, heavy);
            if (menuItem instanceof CheckboxMenuItem)
                return new JCheckBoxMenuItemWrapper((CheckboxMenuItem) menuItem);

            return new JMenuItemWrapper(menuItem);
        }

        return new JMenuItem();
    }
}

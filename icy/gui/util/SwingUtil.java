/**
 * 
 */
package icy.gui.util;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
     * Class used to wrap a MenuItem in a JMenuItem.
     * 
     * @author Stephane
     */
    private static class JMenuItemWrapper extends JMenuItem implements ActionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3283063959812167447L;

        MenuItem menuItem;

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
     * Convert a MenuBar to a JMenuBar.
     */
    public static JMenuBar getJMenuBar(MenuBar menuBar, boolean heavy)
    {
        final JMenuBar result = new JMenuBar();

        for (int i = 0; i < menuBar.getMenuCount(); i++)
            result.add(getJMenu(menuBar.getMenu(i), heavy));

        return result;
    }

    /**
     * Convert a Menu to a JMenu.
     */
    public static JMenu getJMenu(Menu menu, boolean heavy)
    {
        final JMenu result = new JMenu(menu.getLabel());

        if (heavy)
            result.getPopupMenu().setLightWeightPopupEnabled(false);

        for (int i = 0; i < menu.getItemCount(); i++)
            result.add(getJMenuItem(menu.getItem(i), heavy));

        return result;
    }

    /**
     * Convert a MenuItem to a JMenuItem.
     */
    public static JMenuItem getJMenuItem(MenuItem menuItem, boolean heavy)
    {
        if (menuItem instanceof Menu)
            return getJMenu((Menu) menuItem, heavy);

        return new JMenuItemWrapper(menuItem);
    }
}

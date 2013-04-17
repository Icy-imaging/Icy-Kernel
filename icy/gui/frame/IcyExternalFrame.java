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
package icy.gui.frame;

import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.util.ComponentUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JRootPane;

import org.pushingpixels.substance.internal.utils.SubstanceTitlePane;

/**
 * @author Stephane
 */
public class IcyExternalFrame extends JFrame
{
    /**
      * 
      */
    private static final long serialVersionUID = 9130936218505070807L;

    private class CloseAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4933605299188863452L;

        public CloseAction()
        {
            super("Close", new IcyIcon(ResourceUtil.ICON_CLOSE, 20), "Close window", KeyEvent.VK_F4, SystemUtil
                    .getMenuCtrlMask());
        }

        @Override
        public void doAction(ActionEvent e)
        {
            close();
        }
    }

    /**
     * internals
     */
    private SubstanceTitlePane titlePane;
    // private JMenuBar systemMenuBar;
    MenuCallback systemMenuCallback;
    private boolean titleBarVisible;
    private boolean closeItemVisible;
    private boolean initialized = false;

    /**
     * @param title
     * @throws HeadlessException
     */
    public IcyExternalFrame(String title) throws HeadlessException
    {
        super(title);

        getRootPane().addPropertyChangeListener("titlePane", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                // invoke later so the titlePane variable is up to date
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateTitlePane();
                    }
                });
            }
        });

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                // release the system menu callback as it can lead to some memory leak
                // (cycling reference)
                systemMenuCallback = null;
            }
        });
        setIconImages(ResourceUtil.getIcyIconImages());
        setVisible(false);

        systemMenuCallback = null;
        closeItemVisible = true;
        updateTitlePane(LookAndFeelUtil.getTitlePane(this));

        titleBarVisible = true;
        initialized = true;
    }

    /**
     * update internals informations linked to title pane with specified pane
     */
    protected void updateTitlePane(final SubstanceTitlePane pane)
    {
        // update pane save
        if (pane != null)
            titlePane = pane;
        // update menu
        // if (titlePane != null)
        // systemMenuBar = titlePane.getMenuBar();
        // refresh system menu whatever
        updateSystemMenu();
    }

    /**
     * update internals informations linked to title pane
     */
    protected void updateTitlePane()
    {
        if (initialized)
        {
            // title pane can have changed
            updateTitlePane(LookAndFeelUtil.getTitlePane(this));

            if (!titleBarVisible)
                setTitleBarVisible(false);
        }
    }

    /**
     * Refresh system menu
     */
    public void updateSystemMenu()
    {
        if (titlePane != null)
        {
            final JMenu menu;

            if (systemMenuCallback != null)
                menu = systemMenuCallback.getMenu();
            else
                menu = getDefaultSystemMenu();

            // ensure compatibility with heavyweight component
            menu.getPopupMenu().setLightWeightPopupEnabled(false);

            // rebuild menu
            titlePane.setSystemMenu(menu);
            // systemMenuBar.removeAll();
            // systemMenuBar.add(menu);
            // systemMenuBar.validate();
        }
    }

    public void setTitleBarVisible(boolean value)
    {
        if (value)
            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        else
            getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        validate();
    }

    /**
     * close frame
     */
    public void close()
    {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Implement isMinimized method
     */
    public boolean isMinimized()
    {
        return ComponentUtil.isMinimized(this);
    }

    /**
     * Implement isMaximized method
     */
    public boolean isMaximized()
    {
        return ComponentUtil.isMaximized(this);
    }

    /**
     * Implement setMinimized method
     */
    public void setMinimized(final boolean value)
    {
        ComponentUtil.setMinimized(this, value);
    }

    /**
     * Implement setMaximized method
     */
    public void setMaximized(final boolean value)
    {
        ComponentUtil.setMaximized(this, value);
    }

    /**
     * @return the titleBarVisible
     */
    public boolean isTitleBarVisible()
    {
        return getRootPane().getWindowDecorationStyle() != JRootPane.NONE;
    }

    /**
     * @return the closeItemVisible
     */
    public boolean isCloseItemVisible()
    {
        return closeItemVisible;
    }

    /**
     * @param value
     *        the closeItemVisible to set
     */
    public void setCloseItemVisible(boolean value)
    {
        if (closeItemVisible != value)
        {
            closeItemVisible = value;

            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    updateSystemMenu();
                }
            });
        }
    }

    /**
     * Return the default system menu
     */
    public JMenu getDefaultSystemMenu()
    {
        final JMenu result = new JMenu();

        if (closeItemVisible)
            result.add(new CloseAction());

        return result;
    }

    /**
     * @return the systemMenuCallback
     */
    public MenuCallback getSystemMenuCallback()
    {
        return systemMenuCallback;
    }

    /**
     * @param value
     *        the systemMenuCallback to set
     */
    public void setSystemMenuCallback(MenuCallback value)
    {
        if (systemMenuCallback != value)
        {
            systemMenuCallback = value;

            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    updateSystemMenu();
                }
            });
        }
    }

}

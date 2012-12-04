/**
 * 
 */
package icy.gui.frame;

import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.pushingpixels.substance.internal.utils.SubstanceInternalFrameTitlePane;

/**
 * @author Stephane
 */
public class IcyInternalFrame extends JInternalFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = -5445569637723054083L;

    private class CloseAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4933605299188863452L;

        public CloseAction()
        {
            super("Close", new IcyIcon(ResourceUtil.ICON_CLOSE, 20), "Close window", KeyEvent.VK_F4, SystemUtil
                    .getCtrlMask());
        }

        @Override
        public void doAction(ActionEvent e)
        {
            close(false);
        }
    }

    /**
     * internals
     */
    SubstanceInternalFrameTitlePane titlePane = null;
    // JMenu systemMenu;
    MenuCallback systemMenuCallback;
    private boolean titleBarVisible;
    private boolean closeItemVisible;
    private boolean initialized = false;

    /**
     * @param title
     * @param resizable
     * @param closable
     * @param maximizable
     * @param iconifiable
     */
    public IcyInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
    {
        super(title, resizable, closable, maximizable, iconifiable);

        addPropertyChangeListener("titlePane", new PropertyChangeListener()
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

        addInternalFrameListener(new InternalFrameAdapter()
        {
            @Override
            public void internalFrameClosed(InternalFrameEvent e)
            {
                // release the system menu callback as it can lead to some memory leak
                // (cycling reference)
                systemMenuCallback = null;
            }
        });

        setFrameIcon(ResourceUtil.ICON_ICY_16);
        setVisible(false);

        systemMenuCallback = null;
        closeItemVisible = closable;
        updateTitlePane(LookAndFeelUtil.getTitlePane(this));

        titleBarVisible = true;
        initialized = true;
    }

    /**
     * update internals informations linked to title pane with specified pane
     */
    protected void updateTitlePane(final SubstanceInternalFrameTitlePane pane)
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
     * update internals informations linked to title pane and title pane state
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
        if ((titlePane != null) && !isClosed())
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

    /**
     * Close the frame
     */
    public void close(boolean force)
    {
        if (force || isClosable())
            doDefaultCloseAction();
    }

    /**
     * Implement isMinimized method
     */
    public boolean isMinimized()
    {
        return isIcon();
    }

    /**
     * Implement isMaximized method
     */
    public boolean isMaximized()
    {
        return isMaximum();
    }

    /**
     * Implement setMinimized method
     */
    public void setMinimized(final boolean value)
    {
        // only relevant if state changed
        if (isMinimized() ^ value)
        {
            try
            {
                setIcon(value);
            }
            catch (PropertyVetoException e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
    }

    /**
     * Implement setMaximized method
     */
    public void setMaximized(final boolean value)
    {
        // have to check that else we obtain a null pointer exception
        if (getParent() == null)
            return;

        // only relevant if state changed
        if (isMaximized() ^ value)
        {
            try
            {
                // have to check for parent non null
                setMaximum(value);
            }
            catch (PropertyVetoException e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
    }

    /**
     * @return the titleBarVisible
     */
    public boolean isTitleBarVisible()
    {
        return titleBarVisible;
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

    @Override
    public void setClosable(boolean b)
    {
        super.setClosable(b);

        if (!b)
            setCloseItemVisible(false);
    }

    /**
     * @param value
     *        the titleBarVisible to set
     */
    public void setTitleBarVisible(boolean value)
    {
        if (value)
            LookAndFeelUtil.setTitlePane(this, titlePane);
        else
            LookAndFeelUtil.setTitlePane(this, null);

        revalidate();

        titleBarVisible = value;
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

/**
 * 
 */
package icy.gui.frame;

import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.component.ComponentUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
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
                    .getCtrlMask());
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            close();
        }
    }

    /**
     * internals
     */
    private SubstanceTitlePane titlePane;
    private JMenuBar systemMenuBar;
    private MenuCallback systemMenuCallback;
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
        if (titlePane != null)
            systemMenuBar = titlePane.getMenuBar();
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
        if ((systemMenuBar != null) && (systemMenuBar.getMenuCount() > 0))
        {
            final JMenu menu;

            if (systemMenuCallback != null)
                menu = systemMenuCallback.getMenu();
            else
                menu = getDefaultSystemMenu();

            // ensure compatibility with heavyweight component
            menu.getPopupMenu().setLightWeightPopupEnabled(false);

            // rebuild menu
            systemMenuBar.removeAll();
            systemMenuBar.add(menu);
            systemMenuBar.validate();
        }
    }

    // /**
    // * Return true if specified point is located on title bar icon (system icon)
    // */
    // boolean isOnSystemIcon(Point p)
    // {
    // if (titlePane == null)
    // return false;
    //
    // final int w = titlePane.getWidth();
    // final int h = titlePane.getHeight();
    // final Icon icon = getFrameIcon();
    // final int iw = icon.getIconWidth();
    // final int ih = icon.getIconHeight();
    //
    // final Rectangle rect;
    //
    // if (getComponentOrientation().isLeftToRight())
    // rect = new Rectangle(5, (h / 2) - (ih / 2), iw, ih);
    // else
    // rect = new Rectangle(w - (5 + iw), (h / 2) - (ih / 2), iw, ih);
    //
    // return rect.contains(p);
    // }

    public void setTitleBarVisible(boolean value)
    {
        if (value)
            // setUndecorated(false);
            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        else
            // setUndecorated(true);
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
        // always visible for external frame
        return true;
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

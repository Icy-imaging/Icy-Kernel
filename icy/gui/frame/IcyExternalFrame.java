/**
 * 
 */
package icy.gui.frame;

import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.component.ComponentUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.util.LookAndFeelUtil.WeakSubstanceSkinChangeListener;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.SystemUtil;
import icy.util.ClassUtil;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.pushingpixels.substance.api.skin.SkinChangeListener;
import org.pushingpixels.substance.internal.utils.SubstanceTitlePane;

/**
 * @author Stephane
 */
public class IcyExternalFrame extends JFrame implements SkinChangeListener
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
                    .getSystemCtrlMask());
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
    private boolean closeItemVisible;

    /**
     * @param title
     * @throws HeadlessException
     */
    public IcyExternalFrame(String title) throws HeadlessException
    {
        super(title);

        setIconImages(ResourceUtil.getIcyIconImages());
        setVisible(false);

        systemMenuCallback = null;
        closeItemVisible = true;
        updateTitlePane();

        // JFrame doesn't have updateUI() method so we have to listen LAF skin change
        LookAndFeelUtil.addSkinChangeListener(new WeakSubstanceSkinChangeListener(this));
    }

    /**
     * update internals informations linked to title pane with specified pane
     */
    protected void updateTitlePane(final SubstanceTitlePane pane)
    {
        // update pane save
        if (pane != null)
        {
            // title pane changed ?
            if (titlePane != pane)
            {
                // update
                titlePane = pane;

                // retrieve system menu bar
                try
                {
                    systemMenuBar = (JMenuBar) ClassUtil.getFieldObject(titlePane, "menuBar", true);
                }
                catch (Exception e)
                {
                    systemMenuBar = null;
                }
            }
        }

        // refresh system menu
        updateSystemMenu();
    }

    /**
     * update internals informations linked to title pane
     */
    protected void updateTitlePane()
    {
        updateTitlePane(LookAndFeelUtil.getTitlePane(this));
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
            updateSystemMenu();
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
            updateSystemMenu();
        }
    }

    @Override
    public void skinChanged()
    {
        // title pane can have changed
        updateTitlePane();
    }
}

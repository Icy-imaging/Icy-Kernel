/**
 * 
 */
package icy.gui.component;

import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import org.pushingpixels.substance.internal.ui.SubstanceTabbedPaneUI;

/**
 * @author Stephane
 */
public class JCheckTabbedPane extends JTabbedPane
{
    // public interface Check

    // used to find icon location
    private class MyTabbedPaneUI extends SubstanceTabbedPaneUI
    {
        public MyTabbedPaneUI()
        {
            super();
        }

        @Override
        protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect,
                boolean isSelected)
        {
            super.paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
            setIconRect(tabIndex, iconRect);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1274171822668858593L;

    private static final Image IMAGE_CHECK = ResourceUtil.getBlackIconAsImage("checkbox_checked.png", 16);
    private static final Image IMAGE_UNCHECK = ResourceUtil.getBlackIconAsImage("checkbox_unchecked.png", 16);

    boolean checkVisible;
    private ArrayList<Rectangle> iconsRect;

    /**
     * @param tabPlacement
     * @param tabLayoutPolicy
     */
    public JCheckTabbedPane(int tabPlacement, int tabLayoutPolicy)
    {
        super(tabPlacement, tabLayoutPolicy);

        init();
    }

    /**
     * @param tabPlacement
     */
    public JCheckTabbedPane(int tabPlacement)
    {
        super(tabPlacement);

        init();
    }

    public JCheckTabbedPane()
    {
        super();

        init();
    }

    private void init()
    {
        iconsRect = new ArrayList<Rectangle>();
        checkVisible = true;

        setUI(new MyTabbedPaneUI());

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (checkVisible)
                {
                    final int index = getSelectedIndex();

                    if ((index != -1) && (getIconRect(index).contains(e.getPoint())))
                        setCheckedTab(index, !isCheckedTab(index));
                }
            }
        });
    }

    void setIconRect(int index, Rectangle r)
    {
        while (iconsRect.size() <= index)
            iconsRect.add(new Rectangle());
        iconsRect.set(index, r);
    }

    Rectangle getIconRect(int index)
    {
        if (iconsRect.size() > index)
            return iconsRect.get(index);

        return new Rectangle();
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        super.insertTab(title, icon, component, tip, index);

        updateIcons();
    }

    @Override
    public void removeTabAt(int index)
    {
        super.removeTabAt(index);

        updateIcons();
    }

    /**
     * @return the checkVisible
     */
    public boolean isCheckVisible()
    {
        return checkVisible;
    }

    /**
     * @param value
     *        the checkVisible to set
     */
    public void setCheckVisible(boolean value)
    {
        if (checkVisible != value)
        {
            checkVisible = value;
            updateIcons();
        }
    }

    /**
     * Returns the check state of tab component at <code>index</code>.
     * 
     * @param index
     *        the tab index where the check state is queried
     * @return true if tab component at <code>index</code> is checked, false otherwise
     * @exception IndexOutOfBoundsException
     *            if index is out of range
     *            (index < 0 || index >= tab count)
     * @see #setCheckedTab
     */
    public boolean isCheckedTab(int index)
    {
        final Icon icon = getIconAt(index);

        if (icon instanceof IcyIcon)
            return ((IcyIcon) icon).getImage() == IMAGE_CHECK;

        return false;
    }

    /**
     * Sets the check state of tab component at <code>index</code>.
     * An internal exception is raised if there is no tab at that index.
     * 
     * @param index
     *        the tab index where the check state should be set
     * @param value
     *        check state of tab component
     * @exception IndexOutOfBoundsException
     *            if index is out of range
     *            (index < 0 || index >= tab count)
     * @see #isCheckedTab
     */
    public void setCheckedTab(int index, boolean value)
    {
        if (isCheckedTab(index) != value)
        {
            setIconAt(index, new IcyIcon(value ? IMAGE_CHECK : IMAGE_UNCHECK));
            // notify something changed
            fireStateChanged();
        }
    }

    /**
     * Update check icons
     */
    private void updateIcons()
    {
        if (checkVisible)
        {
            // update icons
            for (int i = 0; i < getTabCount(); i++)
            {
                if (isCheckedTab(i))
                    setIconAt(i, new IcyIcon(IMAGE_CHECK));
                else
                    setIconAt(i, new IcyIcon(IMAGE_UNCHECK));
            }
        }
        else
        {
            // update icons
            for (int i = 0; i < getTabCount(); i++)
                setIconAt(i, null);
        }
    }

    @Override
    public void updateUI()
    {
        setUI(new MyTabbedPaneUI());
    }
}

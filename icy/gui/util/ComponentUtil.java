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
package icy.gui.util;

import icy.gui.frame.IcyFrame;
import icy.network.NetworkUtil;
import icy.system.SystemUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * General component utilities class.
 * 
 * @author Stephane
 */
public class ComponentUtil
{
    public static void setPreferredWidth(Component c, int w)
    {
        c.setPreferredSize(new Dimension(w, c.getPreferredSize().height));
    }

    public static void setPreferredHeight(Component c, int h)
    {
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, h));
    }

    public static void setFixedSize(Component c, Dimension d)
    {
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        c.setPreferredSize(d);
    }

    public static void setFixedWidth(Component c, int w)
    {
        c.setMinimumSize(new Dimension(w, 0));
        c.setMaximumSize(new Dimension(w, 65535));
        c.setPreferredSize(new Dimension(w, c.getPreferredSize().height));
    }

    public static void setFixedHeight(Component c, int h)
    {
        c.setMinimumSize(new Dimension(0, h));
        c.setMaximumSize(new Dimension(65535, h));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, h));
    }

    public static void setPreferredWidth(IcyFrame frm, int w)
    {
        frm.setPreferredSize(new Dimension(w, frm.getPreferredSize().height));
    }

    public static void setPreferredHeight(IcyFrame frm, int h)
    {
        frm.setPreferredSize(new Dimension(frm.getPreferredSize().width, h));
    }

    public static void setFixedSize(IcyFrame frm, Dimension d)
    {
        frm.setMinimumSize(d);
        frm.setMaximumSize(d);
        frm.setPreferredSize(d);
    }

    public static void setFixedWidth(IcyFrame frm, int w)
    {
        frm.setMinimumSize(new Dimension(w, 0));
        frm.setMaximumSize(new Dimension(w, 65535));
        frm.setPreferredSize(new Dimension(w, frm.getPreferredSize().height));
    }

    public static void setFixedHeight(IcyFrame frm, int h)
    {
        frm.setMinimumSize(new Dimension(0, h));
        frm.setMaximumSize(new Dimension(65535, h));
        frm.setPreferredSize(new Dimension(frm.getPreferredSize().width, h));
    }

    public static void removeFixedSize(Component c)
    {
        c.setMinimumSize(new Dimension(0, 0));
        c.setMaximumSize(new Dimension(65535, 65535));
    }

    /**
     * Center specified component relative to its parent
     */
    public static void center(Component comp)
    {
        final Container parent = comp.getParent();

        if (parent != null)
        {
            final int x = (parent.getWidth() - comp.getWidth()) / 2;
            final int y = (parent.getHeight() - comp.getHeight()) / 2;

            // avoid negative coordinates when centering
            comp.setLocation((x < 0) ? 0 : x, (y < 0) ? 0 : y);
        }
    }

    /**
     * Center specified windows relative to its parent
     */
    public static void center(Window window)
    {
        window.setLocationRelativeTo(window.getParent());
    }

    /**
     * Center specified JInternalFrame
     */
    public static void center(JInternalFrame frame)
    {
        center((Component) frame);
    }

    /**
     * Center the Window on specified point
     */
    public static void centerOn(Window window, Point position)
    {
        final int x = position.x - (window.getWidth() / 2);
        final int y = position.y - (window.getHeight() / 2);

        // avoid negative coordinates when centering
        window.setLocation((x < 0) ? 0 : x, (y < 0) ? 0 : y);
    }

    /**
     * Center the JInternalFrame on specified point
     */
    public static void centerOn(JInternalFrame f, Point position)
    {
        centerOn((Component) f, position);
    }

    /**
     * Center specified component relative to its parent
     */
    public static void centerOn(Component comp, Point position)
    {
        final int x = position.x - (comp.getWidth() / 2);
        final int y = position.y - (comp.getHeight() / 2);

        // avoid negative coordinates when centering
        comp.setLocation((x < 0) ? 0 : x, (y < 0) ? 0 : y);
    }

    /**
     * Use f.center() instead
     * 
     * @deprecated
     */
    @Deprecated
    public static void center(IcyFrame f)
    {
        f.center();
    }

    public static void center(Component dst, Component src)
    {
        dst.setLocation(src.getX() + ((src.getWidth() - dst.getWidth()) / 2),
                src.getY() + ((src.getHeight() - dst.getHeight()) / 2));
    }

    public static void center(IcyFrame dst, Component src)
    {
        dst.setLocation(src.getX() + ((src.getWidth() - dst.getWidth()) / 2),
                src.getY() + ((src.getHeight() - dst.getHeight()) / 2));
    }

    public static void center(Component dst, IcyFrame src)
    {
        dst.setLocation(src.getX() + ((src.getWidth() - dst.getWidth()) / 2),
                src.getY() + ((src.getHeight() - dst.getHeight()) / 2));
    }

    public static void center(IcyFrame dst, IcyFrame src)
    {
        dst.setLocation(src.getX() + ((src.getWidth() - dst.getWidth()) / 2),
                src.getY() + ((src.getHeight() - dst.getHeight()) / 2));
    }

    /**
     * Returns the center position of the specified component.
     */
    public static Point2D.Double getCenter(Component c)
    {
        if (c != null)
        {
            final Rectangle r = c.getBounds();
            return new Point2D.Double(r.getX() + (r.getWidth() / 2d), r.getY() + (r.getHeight() / 2d));
        }

        return new Point2D.Double(0d, 0d);
    }

    /**
     * Returns all screen device where the specified component is currently displayed.<br>
     * Can return an empty list if given region do not intersect any screen device.
     * 
     * @see #getScreen(Component)
     * @see SystemUtil#getScreenDevices(Rectangle)
     */
    public static List<GraphicsDevice> getScreens(Component c)
    {
        return SystemUtil.getScreenDevices(c.getBounds());
    }

    /**
     * Returns the main screen device where the specified component is currently displayed.<br>
     * Can return <code>null</code> if component is not located on any screen device.
     * 
     * @see #getScreens(Component)
     * @see SystemUtil#getScreenDevice(Rectangle)
     * @see SystemUtil#getScreenDevice(Point)
     */
    public static GraphicsDevice getScreen(Component c)
    {
        final Point2D.Double pos2d = getCenter(c);
        final Point pos = new Point((int) pos2d.getX(), (int) pos2d.getY());

        // get screen on Component center first (better for multi screen)
        GraphicsDevice result = SystemUtil.getScreenDevice(pos);

        // cannot retrieve screen on center, just use component bounds then
        if (result == null)
            result = SystemUtil.getScreenDevice(c.getBounds());

        return result;
    }

    /**
     * Returns the new location of wanted bounds so it does not go outside the specified screen bounds.<br>
     * Returns <code>null</code> if the wanted bounds doesn't need position adjustment.
     */
    public static Point fixPosition(Rectangle wantedBounds, Rectangle screenBounds)
    {
        if (screenBounds.isEmpty())
            return null;

        final int margeX = 80;
        final int margeY = 40;

        int x = wantedBounds.x;
        int y = wantedBounds.y;
        int sx = screenBounds.x;
        int sy = screenBounds.y;
        int minX = (sx - wantedBounds.width) + margeX;
        int maxX = (sx + screenBounds.width) - margeX;
        int minY = sy;
//        int minY = (sy - wantedBounds.height) + margeY;
        int maxY = (sy + screenBounds.height) - margeY;

        if (y < minY)
            y = minY;
        else if (y > maxY)
            y = maxY;
        if (x < minX)
            x = minX;
        else if (x > maxX)
            x = maxX;

        final Point pos = wantedBounds.getLocation();

        // position changed ?
        if ((pos.x != x) || (pos.y != y))
            return new Point(x, y);

        return null;
    }

    /**
     * Fix the given bounds of specified component so it does not go completely off screen.<br>
     * Returns <code>true</code> if the bounds position has be adjusted.
     */
    public static boolean fixPosition(Component component, Rectangle wantedBounds)
    {
      final List<GraphicsDevice> screens = SystemUtil.getScreenDevices();

      // headless mode probably
      if (screens.isEmpty())
          return false;

      Point newPos = null;
      boolean useMainScreen = false;

      for (GraphicsDevice screen : screens)
      {
          final Point pt = fixPosition(wantedBounds, SystemUtil.getScreenBounds(screen, true));

          // this screen accept current position --> no need to adjust position
          if (pt == null)
              return false;
          
          // we already have an adjusted position ?
          if (newPos != null)
              useMainScreen = true;
          else
              newPos = pt;
      }

      // multiple possible position adjustment ? --> use main screen
      if (useMainScreen)
          newPos = fixPosition(wantedBounds, SystemUtil.getScreenBounds(getScreen(component), true));

      // got a new position ? --> set it
      if (newPos != null)
      {
          wantedBounds.setLocation(newPos);
          return true;
      }

      return false;

//        final List<GraphicsDevice> screens = getScreens(component);
//        Point newPos = null;
//        boolean useMainScreen = false;
//
//        for (GraphicsDevice screen : screens)
//        {
//            final Point pt = fixPosition(wantedBounds, SystemUtil.getScreenBounds(screen, true));
//
//            // this screen accept current position --> no need to adjust position
//            if (pt == null)
//                return false;
//
//            // we already have an adjusted position ?
//            if (newPos != null)
//                useMainScreen = true;
//            else
//                newPos = pt;
//        }
//
//        // use main screen
//        if (screens.isEmpty())
//            useMainScreen = true;
//
//        // multiple possible position adjustment ? --> use main screen
//        if (useMainScreen)
//            newPos = fixPosition(wantedBounds, SystemUtil.getScreenBounds(getScreen(component), true));
//
//        // got a new position ? --> set it
//        if (newPos != null)
//            wantedBounds.setLocation(newPos);
//
//        return true;
    }

    /**
     * Fix the given bounds of specified component so it does not go completely off screen.<br>
     * Returns <code>true</code> if component position has be adjusted.
     */
    public static boolean fixPosition(Component component)
    {
        final Rectangle bounds = component.getBounds();

        if (fixPosition(component, bounds))
        {
            component.setBounds(bounds);
            return true;
        }

        return false;
    }

    public static int getComponentIndex(Component c)
    {
        if (c != null)
        {
            final Container container = c.getParent();

            if (container != null)
                for (int i = 0; i < container.getComponentCount(); i++)
                    if (container.getComponent(i) == c)
                        return i;
        }

        return -1;
    }

    public static Point convertPoint(Component src, Point p, Component dst)
    {
        return SwingUtilities.convertPoint(src, p, dst);
    }

    public static Point convertPointFromScreen(Point p, Component c)
    {
        final Point result = new Point(p);

        SwingUtilities.convertPointFromScreen(result, c);

        return result;
    }

    public static Point convertPointToScreen(Point p, Component c)
    {
        final Point result = new Point(p);

        SwingUtilities.convertPointToScreen(result, c);

        return result;
    }

    public static boolean isOutside(Component c, Rectangle r)
    {
        return !r.intersects(c.getBounds());
    }

    public static boolean isInside(Component c, Rectangle r)
    {
        return r.contains(c.getBounds());
    }

    public static void increaseFontSize(Component c, int value)
    {
        setFontSize(c, c.getFont().getSize() + value);
    }

    public static void decreaseFontSize(Component c, int value)
    {
        setFontSize(c, c.getFont().getSize() - value);
    }

    public static void setFontSize(Component c, int fontSize)
    {
        c.setFont(FontUtil.setSize(c.getFont(), fontSize));
    }

    public static void setFontStyle(Component c, int fontStyle)
    {
        c.setFont(FontUtil.setStyle(c.getFont(), fontStyle));
    }

    public static void setFontBold(Component c)
    {
        setFontStyle(c, c.getFont().getStyle() | Font.BOLD);
    }

    public static void setJTextPaneFont(JTextPane tp, Font font, Color c)
    {
        final MutableAttributeSet attrs = tp.getInputAttributes();

        // Set the font family, size, and style, based on properties of
        // the Font object. Note that JTextPane supports a number of
        // character attributes beyond those supported by the Font class.
        // For example, underline, strike-through, super- and sub-script.
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

        // Set the font color
        StyleConstants.setForeground(attrs, c);

        // Retrieve the pane's document object
        StyledDocument doc = tp.getStyledDocument();

        // Replace the style for the entire document. We exceed the length
        // of the document by 1 so that text entered at the end of the
        // document uses the attributes.
        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
    }

    public static void setTickMarkers(JSlider slider)
    {
        final int min = slider.getMinimum();
        final int max = slider.getMaximum();
        final int delta = max - min;

        if (delta > 0)
        {
            final int sliderSize;
            if (slider.getOrientation() == SwingConstants.HORIZONTAL)
                sliderSize = slider.getPreferredSize().width;
            else
                sliderSize = slider.getPreferredSize().height;

            // adjust ticks space on slider
            final int majTick = findBestMajTickSpace(sliderSize, delta);

            slider.setMinorTickSpacing(Math.max(1, majTick / 5));
            slider.setMajorTickSpacing(majTick);
            slider.setLabelTable(slider.createStandardLabels(slider.getMajorTickSpacing(), majTick));
        }
    }

    private static int findBestMajTickSpace(int sliderSize, int delta)
    {
        final int values[] = {1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000, 2000, 2500, 5000};
        // wanted a major tick each ~40 pixels
        final int wantedMajTickSpace = delta / (sliderSize / 40);

        int min = Integer.MAX_VALUE;
        int bestValue = 1;

        // try with our predefined values
        for (int value : values)
        {
            final int dx = Math.abs(value - wantedMajTickSpace);

            if (dx < min)
            {
                min = dx;
                bestValue = value;
            }
        }

        return bestValue;
    }

    /**
     * Breaks the list of items in the specified menu, by creating sub-menus containing the
     * specified number of items, and a "More..." menu to access subsequent items.
     * 
     * @param menu
     *        the menu to break into smaller sub-menus
     * @param maxItemsPerMenu
     *        the maximum number of items to display in each sub-menu
     */
    public static void split(JMenu menu, int maxItemsPerMenu)
    {
        ArrayList<Component> components = new ArrayList<Component>(Arrays.asList(menu.getPopupMenu().getComponents()));

        if (components.size() > maxItemsPerMenu)
        {
            menu.removeAll();

            JMenu currentMenu = menu;

            while (components.size() > 0)
            {
                int n = Math.min(components.size(), maxItemsPerMenu - 1);

                for (int i = 0; i < n; i++)
                    currentMenu.add(components.remove(0));

                if (components.size() > 0)
                    currentMenu = (JMenu) currentMenu.add(new JMenu("More..."));
            }

            if (components.size() > 0)
                System.err.println(components.size() + " are remaining !!");
        }

        // do this recursively for sub-menus
        for (Component component : menu.getPopupMenu().getComponents())
        {
            if (component instanceof JMenu)
                split((JMenu) component, maxItemsPerMenu);
        }
    }

    public static TreePath buildTreePath(TreeNode node)
    {
        final ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();

        nodes.add(node);

        TreeNode n = node;
        while (n.getParent() != null)
        {
            n = n.getParent();
            nodes.add(n);
        }

        Collections.reverse(nodes);

        return new TreePath(nodes.toArray());
    }

    public static void expandAllTree(JTree tree)
    {
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);
    }

    public static HyperlinkListener getDefaultHyperlinkListener()
    {
        return new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                    NetworkUtil.openURL(e.getURL());
            }
        };
    }

    public static boolean isMaximized(Frame f)
    {
        return (f.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
    }

    public static void setMaximized(Frame f, boolean b)
    {
        // only relevant if state changed
        if (isMaximized(f) ^ b)
        {
            if (b)
                f.setExtendedState(Frame.MAXIMIZED_BOTH);
            else
                f.setExtendedState(Frame.NORMAL);
        }
    }

    public static boolean isMinimized(Frame f)
    {
        return (f.getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED;
    }

    public static void setMinimized(Frame f, boolean b)
    {
        // only relevant if state changed
        if (isMinimized(f) ^ b)
        {
            if (b)
                f.setExtendedState(Frame.ICONIFIED);
            else
                f.setExtendedState(Frame.NORMAL);
        }
    }
}

/**
 * 
 */
package icy.gui.util;

import icy.gui.frame.IcyFrame;
import icy.network.NetworkUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
    public static void center(Window w)
    {
        w.setLocationRelativeTo(w.getParent());
    }

    /**
     * Center specified JInternalFrame
     */
    public static void center(JInternalFrame f)
    {
        center((Component) f);
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
        final Point result = new Point(p);

        SwingUtilities.convertPoint(src, result, dst);

        return result;
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

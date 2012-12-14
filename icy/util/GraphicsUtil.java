/**
 * 
 */
package icy.util;

import icy.util.ShapeUtil.ShapeConsumer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Graphics utilities class.
 * 
 * @author Stephane
 */
public class GraphicsUtil
{
    /**
     * Draw ICY style background on the specified Graphics object with specified dimension.
     */
    public static void paintIcyBackGround(int width, int height, Graphics g)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        final float ray = Math.max(width, height) * 0.05f;
        final RoundRectangle2D roundRect = new RoundRectangle2D.Double(0, 0, width, height, Math.min(ray * 2, 20),
                Math.min(ray * 2, 20));

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, Color.white.darker(), 0, height / 1.5f, Color.black));
        g2.fill(roundRect);

        g2.setPaint(Color.black);
        g2.setColor(Color.black);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.fillOval(-width + (width / 2), height / 2, width * 2, height);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setStroke(new BasicStroke(Math.max(1f, Math.min(5f, ray))));
        g2.draw(roundRect);

        g2.dispose();
    }

    /**
     * Draw ICY style background on the specified Graphics object with specified component
     * dimension.
     */
    public static void paintIcyBackGround(Component component, Graphics g)
    {
        paintIcyBackGround(component.getWidth(), component.getHeight(), g);
    }

    /**
     * Draw ICY style background in the specified Image
     */
    public static void paintIcyBackGround(Image image)
    {
        final Graphics g = image.getGraphics();
        // draw background in image
        paintIcyBackGround(image.getWidth(null), image.getHeight(null), g);
        g.dispose();
    }

    /**
     * Returns bounds to draw specified string in the specified Graphics context
     * with specified font.<br>
     * This function handle multi lines string ('\n' character used a line separator).
     */
    public static Rectangle2D getStringBounds(Graphics g, Font f, String text)
    {
        Rectangle2D result = new Rectangle2D.Double();

        if (g != null)
        {
            final FontMetrics fm;

            if (f == null)
                fm = g.getFontMetrics();
            else
                fm = g.getFontMetrics(f);

            for (String s : text.split("\n"))
            {
                final Rectangle2D r = fm.getStringBounds(s, g);

                if (result.isEmpty())
                    result = r;
                else
                    result.setRect(result.getX(), result.getY(), Math.max(result.getWidth(), r.getWidth()),
                            result.getHeight() + r.getHeight());
            }
        }

        return result;
    }

    /**
     * Limit the single line string so it fits in the specified component width.
     */
    public static String limitStringFor(Component c, String text, int width)
    {
        if (width <= 0)
            return "";

        final int w = width - 20;

        if (w <= 0)
            return "..";

        String str = text;
        int textWidth = (int) getStringBounds(c, str).getWidth();
        boolean changed = false;

        while (textWidth > w)
        {
            str = str.substring(0, (str.length() * w) / textWidth);
            textWidth = (int) getStringBounds(c, str).getWidth();
            changed = true;
        }

        if (changed)
            return str.trim() + "...";

        return text;
    }

    /**
     * Return bounds to draw specified string in the specified component.<br>
     * This function handle multi lines string ('\n' character used a line separator).
     */
    public static Rectangle2D getStringBounds(Component c, String text)
    {
        return getStringBounds(c.getGraphics(), text);
    }

    /**
     * Return bounds to draw specified string in the specified Graphics context
     * with current font.<br>
     * This function handle multi lines string ('\n' character used a line separator).
     */
    public static Rectangle2D getStringBounds(Graphics g, String text)
    {
        return getStringBounds(g, null, text);
    }

    /**
     * Draw a text in the specified Graphics context and at the specified position.<br>
     * This function handles multi lines string ('\n' character used a line separator).
     */
    public static void drawString(Graphics g, String text, int x, int y, boolean shadow)
    {
        if (StringUtil.isEmpty(text))
            return;

        final Color color = g.getColor();
        final Color shadowColor;
        if (ColorUtil.getLuminance(color) > 128)
            shadowColor = ColorUtil.sub(color, Color.gray);
        else
            shadowColor = ColorUtil.add(color, Color.gray);
        final Rectangle2D textRect = getStringBounds(g, "M");

        // get height for a single line of text
        final double lineH = textRect.getHeight();
        final int curX = (int) (x - textRect.getX());
        double curY = y - textRect.getY();

        for (String s : text.split("\n"))
        {
            if (shadow)
            {
                g.setColor(shadowColor);
                g.drawString(s, curX + 1, (int) (curY + 1));
                g.setColor(color);
            }
            g.drawString(s, curX, (int) curY);
            curY += lineH;
        }
    }

    /**
     * Draw a horizontal centered text on specified position.
     * This function handle multi lines string ('\n' character used a line separator).
     */
    public static void drawHCenteredString(Graphics g, String text, int x, int y, boolean shadow)
    {
        if (StringUtil.isEmpty(text))
            return;

        final Color color = g.getColor();
        final Color shadowColor = ColorUtil.mix(color, Color.black);
        final Rectangle2D textRect = getStringBounds(g, "M");

        final double offX = textRect.getX();
        double curY = y - textRect.getY();

        for (String s : text.split("\n"))
        {
            final Rectangle2D r = getStringBounds(g, s);
            final int curX = (int) (x - (offX + (r.getWidth() / 2)));

            if (shadow)
            {
                g.setColor(shadowColor);
                g.drawString(s, curX + 1, (int) (curY + 1));
                g.setColor(color);
            }
            g.drawString(s, curX, (int) curY);
            curY += r.getHeight();
        }
    }

    /**
     * Draw a horizontal and vertical centered text on specified position.
     * This function handle multi lines string ('\n' character used a line separator).
     */
    public static void drawCenteredString(Graphics g, String text, int x, int y, boolean shadow)
    {
        if (StringUtil.isEmpty(text))
            return;

        final Color color = g.getColor();
        final Color shadowColor = ColorUtil.mix(color, Color.black);
        final Rectangle2D textRect = getStringBounds(g, text);

        final double offX = textRect.getX();
        double curY = y - (textRect.getY() + (textRect.getHeight() / 2));

        for (String s : text.split("\n"))
        {
            final Rectangle2D r = getStringBounds(g, s);
            final int curX = (int) (x - (offX + (r.getWidth() / 2)));

            if (shadow)
            {
                g.setColor(shadowColor);
                g.drawString(s, curX + 1, (int) (curY + 1));
                g.setColor(color);
            }
            g.drawString(s, curX, (int) curY);
            curY += r.getHeight();
        }
    }

    /**
     * Returns the size to draw a Hint type text in the specified Graphics context.
     */
    public static Dimension getHintSize(Graphics2D g, String text)
    {
        final Rectangle2D stringRect = getStringBounds(g, text);
        return new Dimension((int) Math.ceil(stringRect.getWidth() + 10d), (int) Math.ceil(stringRect.getHeight() + 8d));
    }

    /**
     * Returns the bounds to draw a Hint type text in the specified Graphics context<br>
     * at the specified location.
     */
    public static Rectangle getHintBounds(Graphics2D g, String text, int x, int y)
    {
        final Dimension dim = getHintSize(g, text);
        return new Rectangle(x, y, dim.width, dim.height);
    }

    /**
     * Draw multi line Hint type text in the specified Graphics context<br>
     * at the specified location.
     */
    public static void drawHint(Graphics2D g, String text, int x, int y, Color bgColor, Color textColor)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        final Rectangle2D stringRect = getStringBounds(g, text);
        // calculate hint rect
        final RoundRectangle2D backgroundRect = new RoundRectangle2D.Double(x, y, (int) (stringRect.getWidth() + 10),
                (int) (stringRect.getHeight() + 8), 8, 8);

        g2.setStroke(new BasicStroke(1.3f));
        // draw translucent background
        g2.setColor(bgColor);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2.fill(backgroundRect);
        // draw background border
        g2.setColor(ColorUtil.mix(bgColor, Color.black));
        g2.setComposite(AlphaComposite.Src);
        g2.draw(backgroundRect);
        // draw text
        g2.setColor(textColor);
        drawString(g2, text, x + 5, y + 4, false);

        g2.dispose();
    }

    /**
     * Returns the bounds to draw specified box dimension at specified position.<br>
     * By default the draw process is done from specified position to right/bottom.
     * 
     * @param origin
     *        the initial desired position we want to draw the box.
     * @param dim
     *        box dimension
     * @param xSpace
     *        horizontal space between the position and box
     * @param ySpace
     *        vertical space between the position and box
     * @param top
     *        box is at top of position
     * @param left
     *        box is at left of position
     */
    public static Rectangle getBounds(Point origin, Dimension dim, int xSpace, int ySpace, boolean top, boolean left)
    {
        int x = origin.x;
        int y = origin.y;

        if (left)
            x -= dim.width + xSpace;
        else
            x += xSpace;
        if (top)
            y -= dim.height + ySpace;
        else
            y += ySpace;

        return new Rectangle(x, y, dim.width, dim.height);
    }

    /**
     * Returns the bounds to draw specified box dimension at specified position.
     * By default the draw process is done from specified position to right/bottom.
     * 
     * @param origin
     *        the initial desired position we want to draw the box.
     * @param dim
     *        box dimension
     * @param top
     *        box is at top of position
     * @param left
     *        box is at left of position
     */
    public static Rectangle getBounds(Point origin, Dimension dim, boolean top, boolean left)
    {
        return getBounds(origin, dim, 0, 0, top, left);
    }

    /**
     * Returns the best position to draw specified box in specified region with initial desired
     * position.
     * 
     * @param origin
     *        the initial desired position we want to draw the box.
     * @param dim
     *        box dimension
     * @param region
     *        the rectangle defining the region where we want to draw
     * @param xSpace
     *        horizontal space between the position and box
     * @param ySpace
     *        vertical space between the position and box
     * @param top
     *        by default box is at top of position
     * @param left
     *        by default box is at left of position
     */
    public static Point getBestPosition(Point origin, Dimension dim, Rectangle region, int xSpace, int ySpace,
            boolean top, boolean left)
    {
        final Rectangle bounds1 = getBounds(origin, dim, xSpace, ySpace, top, left);
        if (region.contains(bounds1))
            return new Point(bounds1.x, bounds1.y);

        final Rectangle bounds2 = getBounds(origin, dim, xSpace, ySpace, top, !left);
        if (region.contains(bounds2))
            return new Point(bounds2.x, bounds2.y);

        final Rectangle bounds3 = getBounds(origin, dim, xSpace, ySpace, !top, left);
        if (region.contains(bounds3))
            return new Point(bounds3.x, bounds3.y);

        final Rectangle bounds4 = getBounds(origin, dim, xSpace, ySpace, !top, !left);
        if (region.contains(bounds4))
            return new Point(bounds4.x, bounds4.y);

        Rectangle r;

        r = region.intersection(bounds1);
        final long size1 = r.width * r.height;
        r = region.intersection(bounds2);
        final long size2 = r.width * r.height;
        r = region.intersection(bounds3);
        final long size3 = r.width * r.height;
        r = region.intersection(bounds4);
        final long size4 = r.width * r.height;

        long maxSize = Math.max(size1, Math.max(size2, Math.max(size3, size4)));

        if (maxSize == size1)
            return new Point(bounds1.x, bounds1.y);
        if (maxSize == size2)
            return new Point(bounds2.x, bounds2.y);
        if (maxSize == size3)
            return new Point(bounds3.x, bounds3.y);

        return new Point(bounds4.x, bounds4.y);
    }

    /**
     * Returns the best position to draw specified box in specified region with initial desired
     * position.
     * 
     * @param origin
     *        the initial desired position we want to draw the box.
     * @param dim
     *        box dimension
     * @param region
     *        the rectangle defining the region where we want to draw
     * @param xSpace
     *        horizontal space between the position and box
     * @param ySpace
     *        vertical space between the position and box
     */
    public static Point getBestPosition(Point origin, Dimension dim, Rectangle region, int xSpace, int ySpace)
    {
        return getBestPosition(origin, dim, region, xSpace, ySpace, false, false);
    }

    /**
     * Returns the best position to draw specified box in specified region with initial desired
     * position.
     * 
     * @param origin
     *        the initial desired position we want to draw the box.
     * @param dim
     *        box dimension
     * @param region
     *        the rectangle defining the region where we want to draw
     */
    public static Point getBestPosition(Point origin, Dimension dim, Rectangle region)
    {
        return getBestPosition(origin, dim, region, 0, 0, false, false);
    }

    /**
     * Draw the specified PathIterator in the specified Graphics2D context
     */
    public static void drawPathIterator(PathIterator path, final Graphics2D g)
    {
        ShapeUtil.consumeShapeFromPath(path, new ShapeConsumer()
        {
            @Override
            public boolean consume(Shape shape)
            {
                // draw shape
                g.draw(shape);
                return true;
            }
        });
    }

}

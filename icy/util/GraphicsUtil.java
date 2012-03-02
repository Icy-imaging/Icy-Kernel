/**
 * 
 */
package icy.util;

import icy.util.ShapeUtil.ShapeConsumer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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

        return result;
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
        final Color shadowColor = ColorUtil.mix(color, Color.black);
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
    public static void drawHCenteredText(Graphics g, String text, int x, int y, boolean shadow)
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
    public static void drawCenteredText(Graphics g, String text, int x, int y, boolean shadow)
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
     * Draw multi line Hint type text in the specified Graphics context<br>
     * and at the specified location.
     */
    public static void drawHint(Graphics2D g, String text, int x, int y, Color bgColor, Color textColor)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        final Rectangle2D stringRect = getStringBounds(g, text);
        // calculate hint rect
        final RoundRectangle2D backgroundRect = new RoundRectangle2D.Double(x, y, (int) (stringRect.getWidth() + 10),
                (int) (stringRect.getHeight() + 8), 5, 5);

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

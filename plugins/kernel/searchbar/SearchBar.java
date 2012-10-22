package plugins.kernel.searchbar;

import icy.gui.component.IcyTextField;
import icy.resource.ResourceUtil;
import icy.util.EventUtil;
import icy.util.StringUtil;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import org.jdesktop.swingx.painter.BusyPainter;

import plugins.kernel.searchbar.gui.SBDisplay;

/**
 * 
 * @author Thomas Provoost
 *
 */
public class SearchBar extends IcyTextField implements KeyListener
{
    private static final long serialVersionUID = 1L;
    private static final int DELAY = 200;
    private static final int BUSY_PAINTER_SIZE = 20;
    private SBDisplay displayResults;
    private boolean isLoading = false;

    private Image img = ResourceUtil.getAlphaIconAsImage("zoom");
    private BusyPainter busyPainter = new BusyPainter(BUSY_PAINTER_SIZE);
    private Timer busy;
    BufferedImage imgTmp;

    public SearchBar()
    {
        displayResults = new SBDisplay(this);
        addKeyListener(this);
        getMargin().right += 26;
        getMargin().left += 2;
        getMargin().bottom -= 2;
        getMargin().top -= 2;

        // focusable only when hit Ctrl + F or clicked at the beginning
        setFocusable(false);

        // SET THE BUSY PAINTER
        busyPainter.setFrame(0);
        busyPainter.setPoints(9);
        busyPainter.setTrailLength(6);
        busyPainter.setPointShape(new Rectangle2D.Float(0, 0, 8, 2));

        busy = new Timer(DELAY, new ActionListener()
        {
            int frame = busyPainter.getPoints();

            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame = (frame + 1) % busyPainter.getPoints();
                busyPainter.setFrame(frame);
                frameChanged();
            }
        });

        // ADD LISTENERS
        addCaretListener(displayResults);
        addFocusListener(new FocusListener()
        {

            @Override
            public void focusLost(FocusEvent e)
            {
            }

            @Override
            public void focusGained(FocusEvent e)
            {
                if (displayResults == null)
                    return;
                String text = getText();
                if (!StringUtil.isEmpty(text) && !displayResults.isShowing())
                    displayResults.update(text);
            }
        });
        addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!isFocusable())
                {
                    // 1st time not focusable, have to put it focusable
                    setFocusable(true);
                    requestFocus();
                }
                else
                {
                    if (displayResults == null)
                        return;
                    Point p = e.getPoint();
                    int w = getWidth();
                    int imgW = img.getWidth(null);
                    if (!displayResults.isShowing() || p.x >= w - imgW && p.x < w)
                        displayResults.update(getText());
                }
            }

        });

        getToolkit().addAWTEventListener(new AWTEventListener()
        {

            @Override
            public void eventDispatched(AWTEvent event)
            {

                if (event instanceof KeyEvent)
                {
                    KeyEvent key = (KeyEvent) event;
                    if (key.getID() == KeyEvent.KEY_PRESSED)
                    { // Handle key
                      // presses
                        switch (key.getKeyCode())
                        {
                            case KeyEvent.VK_F:
                                if (EventUtil.isControlDown(key))
                                {
                                    if (!isFocusable())
                                        setFocusable(true);
                                    requestFocus();
                                    key.consume();
                                }
                                break;
                        }
                    }
                }

            }
        }, AWTEvent.KEY_EVENT_MASK);

    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // set rendering presets
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        Insets insets = getMargin();
        if (isLoading)
        {
            // draw loading icon
            g2.translate(w - h, 2);
            imgTmp = new BufferedImage(BUSY_PAINTER_SIZE, BUSY_PAINTER_SIZE, BufferedImage.TYPE_INT_ARGB);
            busyPainter.paint((Graphics2D) imgTmp.getGraphics(), this, BUSY_PAINTER_SIZE, BUSY_PAINTER_SIZE);
            g2.drawImage(imgTmp, 0, 0, 16, 16, null);
        }
        else
        {
            // draw classic icon
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2.drawImage(img, w - h, 2, 16, 16, null);

        }
        if (StringUtil.isEmpty(getText()) && !hasFocus())
        {
            // draw "Search" if no focus
            Color fg = getForeground();
            g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 100));
            g2.drawString("Search", insets.left + 2, h - g2.getFontMetrics().getHeight() / 2 + 2);
        }
        g2.dispose();
    }

    /**
     * Synchronized method used for the loading. Synchronized should not be
     * necessary at this step, but may be in the future.
     * 
     * @param b
     */
    public synchronized void setLoading(boolean b)
    {
        if (b)
            busy.start();
        else
            busy.stop();
        isLoading = b;
        repaint();
    }

    /**
     * Busy Painter has changed, repaint the icon.
     */
    private void frameChanged()
    {
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (displayResults == null)
            return;
        int code = e.getKeyCode();
        switch (code)
        {
            case KeyEvent.VK_DOWN:
                displayResults.moveSelection(1);
                break;
            case KeyEvent.VK_UP:
                displayResults.moveSelection(-1);
                break;
            case KeyEvent.VK_ENTER:
                displayResults.enterPressed();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
}

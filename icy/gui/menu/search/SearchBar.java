package icy.gui.menu.search;

import icy.common.IcyAbstractAction;
import icy.gui.component.IcyTextField;
import icy.gui.main.MainFrame;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.search.SearchEngine;
import icy.search.SearchEngine.SearchEngineListener;
import icy.search.SearchResult;
import icy.util.StringUtil;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.painter.BusyPainter;

/**
 * @author Thomas Provoost & Stephane.
 */
public class SearchBar extends IcyTextField implements SearchEngineListener
{
    public static class SearchAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7457421618693984393L;

        public static final String NAME = "Search";

        public SearchAction()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_SEARCH), "Application search tool");
        }

        @Override
        public void doAction(ActionEvent e)
        {
            final MainFrame mf = Icy.getMainInterface().getMainFrame();

            if (mf != null)
            {
                final SearchBar sb = mf.getSearchBar();

                if (sb != null)
                    sb.setFocus();
            }
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = -931313822004038942L;

    private static final int DELAY = 100;

    private static final int BUSY_PAINTER_SIZE = 15;
    private static final int BUSY_PAINTER_POINTS = 7;
    private static final int BUSY_PAINTER_TRAIL = 4;

    /** Internal search engine */
    final SearchEngine searchEngine;

    /**
     * GUI
     */
    final SearchResultPanel resultsPanel;
    private final IcyIcon searchIcon;

    /**
     * Internals
     */
    private final Timer busyPainterTimer;
    final BusyPainter busyPainter;
    int frame;
    boolean lastSearchingState;
    boolean initialized;

    public SearchBar()
    {
        super();

        initialized = false;

        searchEngine = new SearchEngine();
        searchEngine.addListener(this);

        resultsPanel = new SearchResultPanel(this);
        searchIcon = new IcyIcon(ResourceUtil.ICON_SEARCH, 16);

        // modify margin so we have space for icon
        final Insets margin = getMargin();

        margin.right += 26;
        margin.left += 2;
        margin.bottom -= 2;
        margin.top -= 2;

        // focusable only when hit Ctrl + F or clicked at the beginning
        setFocusable(false);

        // SET THE BUSY PAINTER
        busyPainter = new BusyPainter(BUSY_PAINTER_SIZE);
        busyPainter.setFrame(0);
        busyPainter.setPoints(BUSY_PAINTER_POINTS);
        busyPainter.setTrailLength(BUSY_PAINTER_TRAIL);
        busyPainter.setPointShape(new Rectangle2D.Float(0, 0, 4, 2));
        frame = 0;

        lastSearchingState = false;
        busyPainterTimer = new Timer("Search animation timer");
        busyPainterTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                frame = (frame + 1) % BUSY_PAINTER_POINTS;
                busyPainter.setFrame(frame);

                final boolean searching = searchEngine.isSearching();

                // this permit to get rid of the small delay between the searchCompleted
                // event and when isSearching() actually returns false
                if (searching || (searching != lastSearchingState))
                    repaint();

                lastSearchingState = searching;
            }
        }, DELAY, DELAY);

        // ADD LISTENERS
        addTextChangeListener(new TextChangeListener()
        {
            @Override
            public void textChanged(IcyTextField source, boolean validate)
            {
                search(getText());
            }
        });
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setFocus();
            }
        });
        // addKeyListener(new KeyAdapter()
        // {
        // @Override
        // public void keyPressed(KeyEvent e)
        // {
        // if (initialized)
        // {
        // switch (e.getKeyCode())
        // {
        // case KeyEvent.VK_ESCAPE:
        // cancelSearch();
        // break;
        //
        // case KeyEvent.VK_DOWN:
        // resultsPanel.moveSelection(1);
        // break;
        //
        // case KeyEvent.VK_UP:
        // resultsPanel.moveSelection(-1);
        // break;
        //
        // case KeyEvent.VK_ENTER:
        // // result displayed --> launch selected result
        // if (resultsPanel.isShowing())
        // resultsPanel.executeSelected();
        // else
        // search(getText());
        // break;
        // }
        // }
        // }
        // });
        addFocusListener(new FocusListener()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                removeFocus();
            }

            @Override
            public void focusGained(FocusEvent e)
            {
                search(getText());
            }
        });

        // global key listener to catch Ctrl+F in every case (not elegant)
        // getToolkit().addAWTEventListener(new AWTEventListener()
        // {
        // @Override
        // public void eventDispatched(AWTEvent event)
        // {
        // if (event instanceof KeyEvent)
        // {
        // final KeyEvent key = (KeyEvent) event;
        //
        // if (key.getID() == KeyEvent.KEY_PRESSED)
        // {
        // // Handle key presses
        // switch (key.getKeyCode())
        // {
        // case KeyEvent.VK_F:
        // if (EventUtil.isControlDown(key))
        // {
        // setFocus();
        // key.consume();
        // }
        // break;
        // }
        // }
        // }
        // }
        // }, AWTEvent.KEY_EVENT_MASK);

        // global mouse listener to simulate focus lost (not elegant)
        getToolkit().addAWTEventListener(new AWTEventListener()
        {
            @Override
            public void eventDispatched(AWTEvent event)
            {
                if (!initialized || !hasFocus())
                    return;

                if (event instanceof MouseEvent)
                {
                    final MouseEvent evt = (MouseEvent) event;

                    if (evt.getID() == MouseEvent.MOUSE_PRESSED)
                    {
                        final Point pt = evt.getLocationOnScreen();

                        // user clicked outside search panel --> close it
                        if (!isInsideSearchComponents(pt))
                            removeFocus();
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        buildActionMap();

        initialized = true;
    }

    void buildActionMap()
    {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), SearchAction.NAME);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "MoveDown");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "MoveUp");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Execute");

        getActionMap().put(SearchAction.NAME, new SearchAction());
        getActionMap().put("Cancel", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 6690317671269902666L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (initialized)
                    cancelSearch();
            }
        });
        getActionMap().put("MoveDown", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 8864361043092897904L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (initialized)
                    moveDown();
            }
        });
        getActionMap().put("MoveUp", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 6258168037713535447L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (initialized)
                    moveUp();
            }
        });
        getActionMap().put("Execute", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 5363650211730888168L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (initialized)
                    execute();
            }
        });
    }

    public SearchEngine getSearchEngine()
    {
        return searchEngine;
    }

    protected boolean isInsideSearchComponents(Point pt)
    {
        final Rectangle bounds = new Rectangle();

        bounds.setLocation(getLocationOnScreen());
        bounds.setSize(getSize());

        if (bounds.contains(pt))
            return true;

        if (initialized)
        {
            if (resultsPanel.isVisible())
            {
                bounds.setLocation(resultsPanel.getLocationOnScreen());
                bounds.setSize(resultsPanel.getSize());

                return bounds.contains(pt);
            }
        }

        return false;
    }

    public void setFocus()
    {
        if (!hasFocus())
        {
            setFocusable(true);
            requestFocus();
        }
    }

    public void removeFocus()
    {
        if (initialized)
        {
            resultsPanel.close(true);
            setFocusable(false);
        }
    }

    public void cancelSearch()
    {
        setText("");
    }

    public void search(String text)
    {
        final String filter = text.trim();

        if (StringUtil.isEmpty(filter))
            searchEngine.cancelSearch();
        else
            searchEngine.search(filter);
    }

    protected void execute()
    {
        // result displayed --> launch selected result
        if (resultsPanel.isShowing())
            resultsPanel.executeSelected();
        else
            search(getText());
    }

    protected void moveDown()
    {
        resultsPanel.moveSelection(1);
    }

    protected void moveUp()
    {
        resultsPanel.moveSelection(-1);
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
        if (searchEngine.isSearching())
        {
            // draw loading icon
            g2.translate(w - (BUSY_PAINTER_SIZE + 5), 3);
            busyPainter.paint(g2, this, BUSY_PAINTER_SIZE, BUSY_PAINTER_SIZE);
        }
        else
        {
            // draw search icon
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            searchIcon.paintIcon(this, g2, w - h, 2);
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

    @Override
    public void resultChanged(SearchEngine source, SearchResult result)
    {
        if (initialized)
            resultsPanel.resultChanged(result);
    }

    @Override
    public void resultsChanged(SearchEngine source)
    {
        if (initialized)
            resultsPanel.resultsChanged();
    }

    @Override
    public void searchStarted(SearchEngine source)
    {
        if (initialized)
            resultsPanel.searchStarted();
        // for the busy loop animation
        repaint();
    }

    @Override
    public void searchCompleted(SearchEngine source)
    {
        // for the busy loop animation
        repaint();
    }

}

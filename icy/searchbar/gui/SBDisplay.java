package icy.searchbar.gui;

import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginRepositoryLoader;
import icy.searchbar.SearchBar;
import icy.searchbar.interfaces.SBLink;
import icy.searchbar.interfaces.SBProvider;
import icy.searchbar.provider.ProviderListener;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.util.EventUtil;
import icy.util.XMLUtil;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.pushingpixels.flamingo.internal.ui.common.JRichTooltipPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is the most important part of this plugin: it will handle and
 * display all local and online requests when characters are being typed in the {@link SearchBar}.
 * 
 * @author Thomas Provoost
 */
public class SBDisplay extends JWindow implements CaretListener, ProviderListener
{
    // CONSTANTS
    public static final boolean DEBUG = false;
    private static final long serialVersionUID = 1L;
    private static final int MAXIMUM_TO_DISPLAY = 15;
    private static final int RESIZE_SMOOTHING = 5;
    private static final int DEFAULT_ROW_HEIGHT = 48;
    protected static final boolean WINDOW_ANIMATION = false;

    /** This list contains all classes representing a provider. */
    private ArrayList<Class<? extends SBProvider>> providerClasses = new ArrayList<Class<? extends SBProvider>>();

    /** Reference to Search Bar */
    SearchBar sb;

    /** list of all providers */
    ArrayList<SBProvider> providers = new ArrayList<SBProvider>();

    // /** Animation Timer */
    // private Timer animationTimer;

    /** PopupMenu */
    // JWindow popupMenu;
    JRichTooltipPanel tooltipPanel;
    Popup tooltip;

    // GUI
    DTableModel tableModel;
    JTable table;

    /** Counter of processes left to fully run. */
    int toLoad = 0;

    /**
     * This string contains the text written before the caret update. Used to
     * test if there is any change in the text to avoid unecessary requests.
     */
    String oldText = "";

    OnlineRequest onlineRequest;

    /** Time (in ms) took to the previous request to have a response. */
    int previousTimeRequest = 200;

    /** Contains the current row selected to display the tooltip. */
    protected int currentRowToolTip = -1;

    /** True when request has been canceled. */
    boolean cancelRequest;
    boolean waitingToDisplay;
    JPanel panelMain;
    JPanel panelMore;
    private boolean addedListener;

    public SBDisplay(final SearchBar sb)
    {
        super(Icy.getMainInterface().getMainFrame());
        this.sb = sb;
        providers = new ArrayList<SBProvider>();

        // build table
        tableModel = new DTableModel();
        table = new JTable(tableModel);

        // sets the different column values and renderers
        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // provider column
        col = colModel.getColumn(0);
        col.setCellRenderer(new DLabelRenderer());

        // image/icon column
        col = colModel.getColumn(1);
        col.setPreferredWidth(DEFAULT_ROW_HEIGHT);
        col.setMinWidth(DEFAULT_ROW_HEIGHT);
        col.setMaxWidth(DEFAULT_ROW_HEIGHT);
        col.setCellRenderer(new DImageRenderer());

        // filtered data column
        col = colModel.getColumn(2);
        col.setCellRenderer(new DLabelFilteredRenderer(sb));

        // sets the table properties
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(DEFAULT_ROW_HEIGHT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                // left click launches the execute() method of the FLink.
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e))
                {
                    setVisible(false);
                    if (tooltip != null)
                        tooltip.hide();
                    if (table.columnAtPoint(e.getPoint()) == 3)
                        return;
                    abortRequest();
                    toLoad = 0;
                    sb.setLoading(false);
                    if (SwingUtilities.isLeftMouseButton(e))
                        tableModel.fireLeftClickEvent(table.rowAtPoint(e.getPoint()));
                    else
                        tableModel.fireRightClickEvent(table.rowAtPoint(e.getPoint()));
                    oldText = "";
                    e.consume();
                    table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                // auto select the necessary row when entered
                int row;
                row = table.rowAtPoint(e.getPoint());
                if (row != currentRowToolTip)
                {
                    table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
                    currentRowToolTip = row;
                    toSize(getWidth(), calcBestHeight(), WINDOW_ANIMATION);
                    updatePopupMenu();
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
            }
        });

        table.addMouseMotionListener(new MouseAdapter()
        {

            @Override
            public void mouseMoved(MouseEvent e)
            {
                // unselect previous row and select current one
                int row;
                row = table.rowAtPoint(e.getPoint());
                if (row != currentRowToolTip)
                {
                    table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
                    currentRowToolTip = row;
                    toSize(getWidth(), calcBestHeight(), WINDOW_ANIMATION);
                    updatePopupMenu();
                }
            }
        });

        // send the mouse click event to tryFocusLost method.
        getToolkit().addAWTEventListener(new AWTEventListener()
        {

            @Override
            public void eventDispatched(AWTEvent event)
            {

                if (event instanceof MouseEvent)
                {
                    MouseEvent mouseEvent = (MouseEvent) event;
                    if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                    {
                        if (EventUtil.isLeftMouseButton(mouseEvent))
                        {
                            tryFocusLost(mouseEvent);
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        // Sets the main panels
        panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout());
        panelMain.add(table, BorderLayout.CENTER);
        panelMain.setBorder(BorderFactory.createEtchedBorder());
        panelMore = new JPanel(new BorderLayout());
        add(panelMain);

        setFocusable(false);
        setFocusableWindowState(false);

        // last necessary focus
        addWindowFocusListener(new WindowFocusListener()
        {
            @Override
            public void windowLostFocus(WindowEvent e)
            {
                if (isShowing())
                    setVisible(false);
                if (tooltip != null)
                    tooltip.hide();
            }

            @Override
            public void windowGainedFocus(WindowEvent e)
            {
            }
        });
    }

    /**
     * Set the list of provider classes.
     * 
     * @param providers
     *        : list of provider.
     */
    public void setProvider(List<Class<? extends SBProvider>> providers)
    {
        synchronized (providerClasses)
        {
            providerClasses.clear();
            providerClasses.addAll(providers);
        }
    }

    /**
     * This method will register the provider class into the list of provider
     * classes. The {@link SBProvider} object will not be used except for its
     * class.
     * 
     * @param providerClass
     *        : provider used to get the Class<?> from.
     */
    public void registerProvider(Class<? extends SBProvider> providerClass)
    {
        if (!providerClasses.contains(providerClass))
            providerClasses.add(providerClass);
    }

    /**
     * This method will unregister the provider class from the list of provider
     * class.
     * 
     * @param providerClass
     *        : provider used to get the Class<?> from.
     */
    public void unregisterProvider(Class<? extends SBProvider> providerClass)
    {
        providerClasses.remove(providerClass);
    }

    /**
     * Updates the popup menu: asks the tablemodel for the right popupmenu and
     * displays it.
     */
    void updatePopupMenu()
    {
        if (tooltip != null)
            tooltip.hide();

        if (currentRowToolTip != -1)
        {
            tooltipPanel = tableModel.openPopupMenu(currentRowToolTip);

            if (tooltipPanel != null)
            {
                table.setRowSelectionInterval(currentRowToolTip, currentRowToolTip);

                if (isShowing())
                {
                    int x = getX() + table.getWidth();
                    int y = getY() + table.getRowHeight() * currentRowToolTip;
                    int w = tooltipPanel.getWidth();
                    int h = tooltipPanel.getHeight();
                    Rectangle screenSize = SystemUtil.getDesktopBounds();
                    if (x + w > screenSize.width)
                    {
                        x = x - (x + w - screenSize.width);
                    }
                    if (y + h > screenSize.height)
                    {
                        y = y - (y + h - screenSize.height);
                    }
                    tooltipPanel.setLocation(x, y);

                    tooltip = PopupFactory.getSharedInstance().getPopup(Icy.getMainInterface().getDesktopPane(),
                            tooltipPanel, x, y);
                    tooltip.show();

                    // tooltipPanel.setVisible(true);
                }
            }
        }
    }

    /**
     * Do an update with the giving filter. An update contains the following
     * steps:
     * <ul>
     * <li>abort previous Request</li>
     * <li>start (or not) new one</li>
     * </ul>
     * 
     * @param filter
     */
    public void update(final String filter)
    {
        // tableModel.elements.clear();
        if (tooltip != null)
            tooltip.hide();

        if (onlineRequest != null)
            onlineRequest.stop();

        abortRequest();

        ThreadUtil.bgRun(new Runnable()
        {

            @Override
            public void run()
            {

                if (filter.length() > 0)
                {
                    cancelRequest = false;
                    sb.setLoading(true);
                    doRequest(filter);
                    onlineRequest = new OnlineRequest(filter);
                    ThreadUtil.bgRun(onlineRequest);
                }
                else
                {
                    if (DEBUG)
                        System.out.println("No char");
                }
            }
        });
    }

    /**
     * Forwards abort signal to providers.
     */
    void abortRequest()
    {
        if (DEBUG)
            System.out.println("- abort request - ");
        cancelRequest = true;
        // Stop eventual previous requests
        synchronized (providers)
        {
            for (final SBProvider provider : providers)
            {
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        provider.cancelRequest();
                    }
                });
            }
        }
        sb.setLoading(false);
        setVisible(false);
        toLoad = 0;
        if (DEBUG)
            System.out.println("- abort requests done - " + Calendar.getInstance().get(Calendar.SECOND) + ":"
                    + Calendar.getInstance().get(Calendar.MILLISECOND));
    }

    @Override
    public void caretUpdate(CaretEvent e)
    {
        String filter = sb.getText();
        if (!filter.contentEquals("") && oldText.contentEquals(sb.getText()))
        {
            if (DEBUG)
                System.out.println("same text: " + filter);
            return;
        }
        if (DEBUG)
            System.out.println("written: " + filter);

        // SETUP REQUEST
        oldText = filter;
        update(filter);
    }

    /**
     * Updates this component. Only one request at a time.
     */
    @Override
    public synchronized void updateDisplay()
    {
        if (!addedListener)
        {
            Icy.getMainInterface().getMainFrame().addWindowFocusListener(new WindowFocusListener()
            {

                @Override
                public void windowLostFocus(WindowEvent e)
                {
                    if (isShowing())
                        setVisible(false);
                    if (tooltip != null)
                        tooltip.hide();
                }

                @Override
                public void windowGainedFocus(WindowEvent e)
                {
                }                        
            });
            Icy.getMainInterface().getMainFrame().addComponentListener(new ComponentListener()
            {

                @Override
                public void componentShown(ComponentEvent e)
                {
                }

                @Override
                public void componentResized(ComponentEvent e)
                {
                }

                @Override
                public void componentMoved(ComponentEvent e)
                {
                    if (isShowing())
                        setVisible(false);
                    if (tooltip != null)
                        tooltip.hide();
                }

                @Override
                public void componentHidden(ComponentEvent e)
                {
                }
            });
            addedListener = true;
        }

        if (waitingToDisplay)
            return;

        waitingToDisplay = true;
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                long time = System.nanoTime();
                waitingToDisplay = false;
                if (!sb.isShowing() || cancelRequest)
                    return;
                if (providers != null && providers.size() > 0)
                {

                    tableModel.elements.clear();

                    // Calculates the best size for 1st column display
                    Font tableFont = table.getFont();
                    FontMetrics fm = table.getFontMetrics(tableFont);
                    int maxProviderWidth = 0;
                    int maxDescriptionWidth = 0;

                    // calculates how many items per provider
                    int nbElements = 0;
                    int[] distributionTable = new int[providers.size()];
                    for (int i = 0; i < providers.size(); ++i)
                    {
                        distributionTable[i] = providers.get(i).getElements().size();
                        nbElements += distributionTable[i];
                    }
                    if (nbElements > MAXIMUM_TO_DISPLAY)
                        determineDistribution(distributionTable);

                    // For Each Provider
                    for (int idxProvider = 0; idxProvider < providers.size(); ++idxProvider)
                    {
                        SBProvider provider = providers.get(idxProvider);
                        if (provider.getElements().size() <= 0) // no element
                            continue;
                        String providerName = provider.getName();
                        int size = fm.charsWidth(providerName.toCharArray(), 0, providerName.length());
                        if (size + 15 > maxProviderWidth)
                            maxProviderWidth = size + 15;

                        // add everything into the table
                        ArrayList<SBLink> elements = provider.getElements();
                        for (int i = 0; i < elements.size() && i < distributionTable[idxProvider]; ++i)
                        {
                            SBLink element = elements.get(i);
                            String elementLabel = element.getLabel();
                            JLabel label = new JLabel();

                            int idx = elementLabel.toLowerCase().indexOf(sb.getText().toLowerCase());
                            if (idx == -1 || elementLabel.contains("<br/>"))
                            {
                                label.setText("<html>" + elementLabel + "</html>");
                            }
                            else
                            {
                                String text1 = elementLabel.substring(0, idx);
                                String text2 = elementLabel.substring(idx, idx + sb.getText().length());
                                String text3 = elementLabel.substring(idx + sb.getText().length(),
                                        elementLabel.length());
                                label.setText("<html>" + text1 + "<b>" + text2 + "</b>" + text3 + "</html>");
                            }

                            // created a dialog just to determine the text size.
                            // should be less heavy in the future
                            JPanel panel = new JPanel(new BorderLayout());
                            panel.add(label, BorderLayout.CENTER);
                            JDialog dlg = new JDialog();
                            dlg.add(panel);
                            dlg.pack();

                            int elemWidth = label.getWidth();

                            if (elemWidth > maxDescriptionWidth)
                                maxDescriptionWidth = elemWidth;
                            tableModel.elements.add(element);
                        }
                    }
                    if (tableModel.elements.size() == 0)
                    {
                        setVisible(false);
                        if (tooltip != null)
                            tooltip.hide();
                        return;
                    }
                    else if (tableModel.elements.size() < nbElements)
                    {
                        // creates the panel under the table, showing how many
                        // results were found
                        JLabel lbl = new JLabel(tableModel.elements.size() + " / " + nbElements + " results shown");
                        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                        lbl.setHorizontalTextPosition(SwingConstants.RIGHT);
                        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                        panelMore.removeAll();
                        panelMore.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
                        panelMore.setLayout(new BorderLayout());
                        panelMore.add(lbl, BorderLayout.CENTER);
                        panelMain.add(panelMore, BorderLayout.SOUTH);
                        panelMain.validate();
                    }
                    else
                    {
                        panelMain.remove(panelMore);
                        panelMain.validate();
                    }
                    int columnWith = maxProviderWidth == 0 ? 100 : maxProviderWidth;

                    final TableColumnModel colModel = table.getColumnModel();
                    TableColumn col = colModel.getColumn(0);
                    col.setPreferredWidth(columnWith);
                    col.setMinWidth(columnWith);
                    col.setMaxWidth(columnWith);

                    // Set location and visibility
                    if (sb == null || !sb.isShowing())
                    {
                        abortRequest();
                        setVisible(false);
                        return;
                    }
                    Point p = sb.getLocationOnScreen();
                    setLocation(p.x, p.y + sb.getHeight());
                    maxDescriptionWidth += 20;
                    int wantedW = maxProviderWidth + DEFAULT_ROW_HEIGHT + maxDescriptionWidth + 32;
                    int wantedH = calcBestHeight();

                    if (!isShowing() && !cancelRequest)
                    {
                        if (DEBUG)
                            System.out.println("first display " + Calendar.getInstance().get(Calendar.SECOND) + ":"
                                    + Calendar.getInstance().get(Calendar.MILLISECOND));
                        setLocationByPlatform(false);
                        setVisible(true);
                    }
                    toSize(wantedW, wantedH, WINDOW_ANIMATION);
                    repaint();
                }
                else
                {
                    sb.setLoading(false);
                    setVisible(false);
                    if (tooltip != null)
                        tooltip.hide();
                }
                if (DEBUG)
                    System.out.println("<time to display: " + (System.nanoTime() - time) / 1000000L + " ms> "
                            + Calendar.getInstance().get(Calendar.SECOND) + ":"
                            + Calendar.getInstance().get(Calendar.MILLISECOND));
            }
        });

    }

    /**
     * Gives a better distribution of items.
     * 
     * @param neededSizes
     */
    void determineDistribution(int[] neededSizes)
    {
        int threshold = MAXIMUM_TO_DISPLAY / neededSizes.length;
        int toDistribute = 0;
        int nbLeft = neededSizes.length;
        for (int i = 0; i < neededSizes.length; ++i)
        {
            if (neededSizes[i] <= threshold)
            {
                toDistribute += threshold - neededSizes[i];
                --nbLeft;
            }
            else
            {
                neededSizes[i] = threshold;
            }
        }
        for (int i = 0; i < neededSizes.length; ++i)
        {
            if (neededSizes[i] > threshold)
            {
                neededSizes[i] += toDistribute / nbLeft;
            }
        }
    }

    /**
     * @param filter
     */
    void doRequest(String filter)
    {
        filter = filter.toLowerCase();

        // remove listeners to avoid memory leak
        for (SBProvider provider : providers)
            provider.removeListener(SBDisplay.this);

        providers.clear();
        for (Class<?> providerClass : providerClasses)
        {
            try
            {
                providers.add((SBProvider) providerClass.newInstance());
            }
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        while (filter.startsWith(" "))
            filter = filter.substring(1);
        final String filterLC = filter;
        if (filter.contentEquals(""))
            return;
        toLoad = providers.size() * 2; // twice because local + online

        for (SBProvider provider : providers)
            provider.addListener(SBDisplay.this);

        for (int i = 0; i < providers.size(); ++i)
        { // not for each to avoid
          // concurrent thread
          // issues
            final SBProvider provider = providers.get(i);
            if (DEBUG)
                System.out.println("start " + provider.getName() + " " + provider.getId() + " "
                        + Calendar.getInstance().get(Calendar.SECOND) + ":"
                        + Calendar.getInstance().get(Calendar.MILLISECOND));
            provider.clear();

            // the local request can be threaded
            ThreadUtil.bgRun(new Runnable()
            {

                @Override
                public void run()
                {
                    provider.performLocalRequest(filterLC);
                }
            });
        }
    }

    @Override
    public synchronized void loadedProvider(SBProvider provider)
    {
        if (DEBUG)
            System.out.println("-" + provider.getName() + " " + provider.getId() + " is loaded. "
                    + +Calendar.getInstance().get(Calendar.SECOND) + ":"
                    + Calendar.getInstance().get(Calendar.MILLISECOND));
        if (providers.contains(provider) && !provider.isRequestCancelled())
        {
            --toLoad;
            if (DEBUG)
                System.out.println("- processes running: " + toLoad);
            if (toLoad <= 0)
                sb.setLoading(false);
            updateDisplay();
        }
    }

    @Override
    public void providerItemChanged()
    {
        if (DEBUG)
            System.out.println("Provider Item Changed");
        table.repaint();
    }

    /**
     * Automatically calculates the best height to display all the data.
     * 
     * @return
     */
    int calcBestHeight()
    {
        int toReturn;
        Insets insets = panelMain.getBorder().getBorderInsets(table);
        toReturn = table.getRowCount() * table.getRowHeight(); // rows
        toReturn += insets.top + insets.bottom; // borders
        if (panelMore.isVisible())
            toReturn += panelMore.getHeight(); // panelMore
        return toReturn;
    }

    /**
     * Animation of the {@link SBDisplay}.
     * 
     * @param wantedW
     *        : wanted width to go to.
     * @param wantedH
     *        : wanted height to go to.
     * @param animate
     *        : true if animation should be running, false if not.
     */
    synchronized void toSize(final int wantedW, final int wantedH, boolean animate)
    {

        final int w = getWidth();
        final int h = getHeight();

        // same window size, do nothing
        if ((wantedW == w && wantedH == h) || wantedW == 0 || wantedH == 0)
        {
            return;
        }

        // No animation, just do a single setSize(int, int)
        if (!animate)
        {
            setSize(wantedW, wantedH);
            return;
        }

        // Otherwise, start animation
        final int width = wantedW - w;
        final int height = wantedH - h;
        final int[] posX = new int[RESIZE_SMOOTHING];
        final int[] posY = new int[RESIZE_SMOOTHING];

        for (int x = 0; x < posX.length; ++x)
            posX[x] = wantedW - width / (x + 2);
        for (int y = 0; y < posY.length; ++y)
            posY[y] = wantedH - height / (y + 2);
    }

    /**
     * This class handles all online requests.
     * 
     * @author Thomas Provoost
     */
    private class OnlineRequest implements Runnable
    {
        boolean stop = false;
        String filter;

        public OnlineRequest(String filter)
        {
            this.filter = filter;
        }

        @Override
        public void run()
        {
            // PluginRepository loader may have some issues if called before
            // Network is set. Asks for a reload. If still fails, calls
            // errorOnline();
            if (PluginRepositoryLoader.failed())
            {
                if (NetworkUtil.hasInternetAccess())
                    PluginRepositoryLoader.reload();
                if (DEBUG)
                    System.out.println("No internet connection.");
                if (PluginRepositoryLoader.failed())
                {
                    errorOnline();
                    return;
                }
            }
            ThreadUtil.sleep(previousTimeRequest);
            if (DEBUG)
                System.out.println("waiting over");
            if (stop)
                return;
            try
            {
                // Request
                long before = System.nanoTime();
                String request = "http://bioimageanalysis.org/icy/search/search.php?search="
                        + sb.getText().replace(" ", "%20");
                request = request.replace("+", "%2B").replace("&", "%26").replace("@", "%40").replace("<", "%3C")
                        .replace(">", "%3E");
                if (DEBUG)
                    System.out.println("Request: " + request + " " + +Calendar.getInstance().get(Calendar.SECOND) + ":"
                            + Calendar.getInstance().get(Calendar.MILLISECOND));
                Document res = XMLUtil.loadDocument(new URL(request), false);
                if (stop)
                    return;
                // Request issue
                if (res == null)
                {
                    errorOnline();
                    return;
                }
                previousTimeRequest = (int) ((System.nanoTime() - before) / 1000000L);
                if (DEBUG)
                    System.out.println("request done in " + previousTimeRequest + " ms.");

                previousTimeRequest = previousTimeRequest <= 200 ? 200 : previousTimeRequest;

                Element result = res.getDocumentElement();
                result = XMLUtil.getElement(result, "searchresult");

                // Pass the request to all providers
                for (int i = 0; i < providers.size(); ++i)
                {
                    if (stop)
                        return;
                    final SBProvider provider = providers.get(i);
                    if (DEBUG)
                        System.out.println("start " + provider.getName() + " online " + provider.getId() + " "
                                + Calendar.getInstance().get(Calendar.SECOND) + ":"
                                + Calendar.getInstance().get(Calendar.MILLISECOND));
                    provider.processOnlineResult(filter, result);
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
        }

        public synchronized void stop()
        {
            stop = true;
        }
    }

    /**
     * Error with the online request, loading is not necessary anymore.
     */
    synchronized void errorOnline()
    {
        toLoad = 0;
        sb.setLoading(false);
    }

    /**
     * Selection movement in the table: up or down.
     * 
     * @param direction
     *        : should be 1 or -1.
     */
    public void moveSelection(int direction)
    {
        if (!isShowing())
            return;
        int nbRows = table.getRowCount();
        if (nbRows == 0)
            return;
        table.setRowHeight(DEFAULT_ROW_HEIGHT);
        int nextValue = currentRowToolTip + direction;

        if (currentRowToolTip != -1)
        {
            currentRowToolTip = (nbRows + nextValue) % nbRows;
        }
        else
        {
            currentRowToolTip = direction == 1 ? 0 : nbRows - 1;
        }
        if (currentRowToolTip >= 0 && currentRowToolTip < nbRows)
            table.setRowSelectionInterval(currentRowToolTip, currentRowToolTip);
        table.repaint();
        updatePopupMenu();
    }

    /**
     * Enter key has been pressed, execute the current item selected.
     */
    public void enterPressed()
    {
        if (DEBUG)
            System.out.println("run selected");
        if (!isShowing())
        {
            update(sb.getText());
        }
        else if (currentRowToolTip >= 0 && currentRowToolTip < tableModel.elements.size() && isShowing())
        {
            tableModel.elements.get(currentRowToolTip).execute();
            abortRequest();
            toLoad = 0;
            sb.setLoading(false);
            oldText = "";
            table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
            setVisible(false);
            if (tooltip != null)
                tooltip.hide();
        }
    }

    /**
     * This method will try to set the {@link SBDisplay} visible or not,
     * depending on the {@link MouseEvent}.
     * 
     * @param e
     *        : {@link MouseEvent} to be used.
     */
    public boolean tryFocusLost(MouseEvent e)
    {
        Point p = e.getLocationOnScreen();
        boolean insideDisplay = isShowing() && getBounds().contains(p);
        boolean insideSearchBar = sb != null && sb.isShowing()
                && new Rectangle(sb.getLocationOnScreen(), sb.getSize()).contains(p);
        boolean insidePopupMenu = tooltipPanel != null && tooltipPanel.isShowing()
                && tooltipPanel.getBounds().contains(p);
        if (!insideDisplay && !insideSearchBar && !insidePopupMenu)
        {
            if (DEBUG)
                System.out.println("outside: focus lost");
            setVisible(false);
            if (tooltip != null)
                tooltip.hide();
            abortRequest();
            return true;
        }
        return false;
    }
}

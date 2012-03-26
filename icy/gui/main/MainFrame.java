/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.main;

import icy.file.Loader;
import icy.gui.component.ComponentUtil;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.ExternalizablePanel.StateListener;
import icy.gui.inspector.ChatPanel;
import icy.gui.inspector.InspectorPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.MainRibbon;
import icy.gui.util.WindowPositionSaver;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyApplicationIcon;
import icy.system.SystemUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;

import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

/**
 * @author fab & Stephane
 */
public class MainFrame extends JRibbonFrame
{
    /**
     * Used to perform the drag and drop of file.<br>
     * Support Linux KDE/Gnome specs.
     * 
     * @author fab
     */
    private static class FileAndTextTransferHandler extends TransferHandler
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3575134952432441705L;

        private static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String";

        private final DataFlavor fileFlavor;
        private final DataFlavor stringFlavor;
        private DataFlavor uriListFlavor;

        public FileAndTextTransferHandler()
        {
            fileFlavor = DataFlavor.javaFileListFlavor;
            stringFlavor = DataFlavor.stringFlavor;

            try
            {
                uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
            }
            catch (ClassNotFoundException e)
            {
                uriListFlavor = null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(JComponent c, Transferable t)
        {
            if (!canImport(c, t.getTransferDataFlavors()))
                return false;

            try
            {
                if (hasFileFlavor(t.getTransferDataFlavors()))
                {
                    // Windows
                    Loader.load((List<File>) t.getTransferData(fileFlavor));
                    return true;
                }
                else if (hasURIListFlavor(t.getTransferDataFlavors()))
                {
                    // Linux
                    final ArrayList<File> files = textURIListToFileList((String) t.getTransferData(uriListFlavor));

                    if (files.size() > 0)
                        Loader.load((List<File>) t.getTransferData(fileFlavor));

                    return true;
                }
                else if (hasStringFlavor(t.getTransferDataFlavors()))
                {
                    String str = ((String) t.getTransferData(stringFlavor));

                    System.out.println(str);

                    return true;
                }
            }
            catch (UnsupportedFlavorException ufe)
            {
                System.err.println("importData: unsupported data flavor");
            }
            catch (IOException ieo)
            {
                System.err.println("importData: I/O exception");
            }

            return false;
        }

        @Override
        public int getSourceActions(JComponent c)
        {
            return COPY;
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors)
        {
            if (hasFileFlavor(flavors))
                return true;
            if (hasStringFlavor(flavors))
                return true;

            return false;
        }

        private boolean hasFileFlavor(DataFlavor[] flavors)
        {
            for (DataFlavor flavor : flavors)
                if (fileFlavor.equals(flavor))
                    return true;

            return false;
        }

        private boolean hasStringFlavor(DataFlavor[] flavors)
        {
            for (DataFlavor flavor : flavors)
                if (stringFlavor.equals(flavor))
                    return true;

            return false;
        }

        private boolean hasURIListFlavor(DataFlavor[] flavors)
        {
            if (uriListFlavor != null)
            {
                for (DataFlavor flavor : flavors)
                    if (uriListFlavor.equals(flavor))
                        return true;
            }

            return false;
        }

        /** Your helpful function */
        private static ArrayList<File> textURIListToFileList(String data)
        {
            final ArrayList<File> list = new ArrayList<File>(1);

            for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();)
            {
                final String s = st.nextToken();

                // the line is a comment (as per the RFC 2483)
                if (s.startsWith("#"))
                    continue;

                try
                {
                    final URI uri = new URI(s);
                    final File file = new File(uri);
                    list.add(file);
                }
                catch (URISyntaxException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
            }

            return list;
        }
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1113003570969611614L;

    public static final String TITLE = "Icy";

    public static final String PROPERTY_DETACHEDMODE = "detachedMode";

    public static final int TILE_HORIZONTAL = 0;
    public static final int TILE_VERTICAL = 1;
    public static final int TILE_GRID = 2;

    public static final String ID_PREVIOUS_STATE = "previousState";

    private final MainRibbon mainRibbon;
    JSplitPane mainPane;
    private final JPanel centerPanel;
    private final IcyDesktopPane desktopPane;
    InspectorPanel inspector;
    private final FileAndTextTransferHandler fileAndTextTransferHandler;
    private boolean detachedMode;
    int lastInspectorWidth;
    boolean inspectorWidthSet;

    // state save for detached mode
    private int previousHeight;
    private boolean previousMaximized;
    private boolean previousInspectorInternalized;

    // we need to keep reference on it as the object only use weak reference
    final WindowPositionSaver positionSaver;

    /**
     * @throws HeadlessException
     */
    public MainFrame() throws HeadlessException
    {
        super(TITLE);

        // RibbonFrame force these properties to false
        // but this add a problem on external JComboBox with mac osx
        // FIXME : problem with JComboBox and modal Dialog (OSX)
        JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);

        // FIXME : remove this when Ribbon with have fixed KeyTipLayer component
        getRootPane().getLayeredPane().getComponent(0).setVisible(false);

        positionSaver = new WindowPositionSaver(this, "frame/main", new Point(50, 50), new Dimension(800, 600));
        previousInspectorInternalized = positionSaver.getPreferences().getBoolean(ID_PREVIOUS_STATE, true);

        // transfert handler
        fileAndTextTransferHandler = new FileAndTextTransferHandler();

        // set "always on top" state
        setAlwaysOnTop(GeneralPreferences.getAlwaysOnTop());
        // default close operation
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // build ribbon
        mainRibbon = new MainRibbon(getRibbon());

        // set application icons
        setIconImages(ResourceUtil.getIcyIconImages());
        setApplicationIcon(new IcyApplicationIcon());

        // main center pane (contains desktop pane)
        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        // desktop pane
        desktopPane = new IcyDesktopPane();
        desktopPane.setTransferHandler(fileAndTextTransferHandler);
        desktopPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    if (isInpectorInternalized())
                        externalizeInspector();
                    else
                        internalizeInspector();
                }
            }
        });

        // set the desktop pane in center pane
        centerPanel.add(desktopPane, BorderLayout.CENTER);
    }

    /**
     * Process init.<br>
     * Inspector is an ExternalizablePanel and requires MainFrame to be created.
     */
    public void init()
    {
        // inspector
        inspector = new InspectorPanel();
        inspectorWidthSet = false;

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                // only need to do it at first display
                if (!inspectorWidthSet)
                {
                    // main frame resized --> adjust divider location so inspector keep its size.
                    // we need to use this method as getWidth() do not return immediate correct
                    // value on OSX when initial state is maximized.
                    if (inspector.isInternalized())
                        mainPane.setDividerLocation(getWidth() - lastInspectorWidth);

                    inspectorWidthSet = true;
                }

                // keep height to minimum height when we are in detached mode
                if (isDetachedMode())
                    setSize(getWidth(), getMinimumSize().height);
            }
        });

        // main pane
        mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel, null);
        mainPane.setContinuousLayout(true);

        // take in account the divider and border size
        lastInspectorWidth = inspector.getPreferredSize().width + 6 + 8;
        if (inspector.isInternalized())
        {
            mainPane.setRightComponent(inspector);
            mainPane.setDividerSize(6);
        }
        else
        {
            mainPane.setDividerSize(0);
            inspector.setParent(mainPane);
        }
        mainPane.setResizeWeight(1);

        inspector.addStateListener(new StateListener()
        {
            @Override
            public void stateChanged(ExternalizablePanel source, boolean externalized)
            {
                if (externalized)
                    mainPane.setDividerSize(0);
                else
                {
                    mainPane.setDividerSize(6);
                    // restore previous location
                    mainPane.setDividerLocation(getWidth() - lastInspectorWidth);
                }
            }
        });

        previousHeight = getHeight();
        previousMaximized = ComponentUtil.isMaximized(this);
        detachedMode = GeneralPreferences.getMultiWindowMode();

        // detached mode
        if (detachedMode)
        {
            // resize window to ribbon dimension
            if (previousMaximized)
                ComponentUtil.setMaximized(this, false);
            setSize(getWidth(), getMinimumSize().height);
        }
        else
            add(mainPane, BorderLayout.CENTER);

        validate();

        setVisible(true);
    }

    public ApplicationMenu getApplicationMenu()
    {
        return (ApplicationMenu) getRibbon().getApplicationMenu();
    }

    /**
     * Return the center pane, this pane contains the desktop pane.<br>
     * Feel free to add temporary top/left/right or bottom pane to it.
     */
    public JPanel getCenterPanel()
    {
        return centerPanel;
    }

    /**
     * Returns the desktopPane which contains InternalFrame.
     */
    public IcyDesktopPane getDesktopPane()
    {
        return desktopPane;
    }

    /**
     * Return all internal frames
     */
    public ArrayList<JInternalFrame> getInternalFrames()
    {
        if (desktopPane != null)
            return (ArrayList<JInternalFrame>) Arrays.asList(desktopPane.getAllFrames());

        return new ArrayList<JInternalFrame>();
    }

    /**
     * @return the inspector
     */
    public InspectorPanel getInspector()
    {
        return inspector;
    }

    /**
     * @return the mainRibbon
     */
    public MainRibbon getMainRibbon()
    {
        return mainRibbon;
    }

    /**
     * @return the chat component
     */
    public ChatPanel getChat()
    {
        return inspector.getChatPanel();
    }

    /**
     * Return true if the main frame is in "detached" mode
     */
    public boolean isDetachedMode()
    {
        return detachedMode;
    }

    /**
     * Return content pane dimension (available area in main frame).<br>
     * If the main frame is in "detached" mode this actually return the desktop dimension.
     */
    public Dimension getDesktopSize()
    {
        if (detachedMode)
            return SystemUtil.getMaximumWindowBounds().getSize();

        return desktopPane.getSize();
    }

    /**
     * Return content pane width
     */
    public int getDesktopWidth()
    {
        return getDesktopSize().width;
    }

    /**
     * Return content pane height
     */
    public int getDesktopHeight()
    {
        return getDesktopSize().height;
    }

    public int getPreviousHeight()
    {
        return previousHeight;
    }

    public boolean getPreviousMaximized()
    {
        return previousMaximized;
    }

    /**
     * Returns true if the inspector is internalized in main container.<br>
     * Always returns false in detached mode.
     */
    public boolean isInpectorInternalized()
    {
        return inspector.isInternalized();
    }

    /**
     * Internalize the inspector in main container.<br>
     * The method fails and returns false in detached mode.
     */
    public boolean internalizeInspector()
    {
        if (inspector.isExternalized() && inspector.isInternalizationAutorized())
        {
            inspector.internalize();
            return true;
        }

        return false;
    }

    /**
     * Externalize the inspector in main container.<br>
     * Returns false if the methods failed.
     */
    public boolean externalizeInspector()
    {
        if (inspector.isInternalized() && inspector.isExternalizationAutorized())
        {
            // save diviser location
            lastInspectorWidth = getWidth() - mainPane.getDividerLocation();
            inspector.externalize();
            return true;
        }

        return false;
    }

    /**
     * Organize all frames in cascade
     */
    public void organizeCascade()
    {
        // all screen devices
        final GraphicsDevice screenDevices[] = SystemUtil.getLocalGraphicsEnvironment().getScreenDevices();
        // screen devices to process
        final ArrayList<GraphicsDevice> devices = new ArrayList<GraphicsDevice>();

        // detached mode ?
        if (isDetachedMode())
        {
            // process all available screen for cascade organization
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    devices.add(dev);
        }
        else
        {
            // process desktop pane cascade organization
            desktopPane.organizeCascade();

            // we process screen where the mainFrame is not visible
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    if (!dev.getDefaultConfiguration().getBounds().contains(getLocation()))
                        devices.add(dev);
        }

        // organize frames on different screen
        for (GraphicsDevice dev : devices)
            organizeCascade(dev);
    }

    /**
     * Organize frames in cascade on the specified graphics device.
     */
    protected void organizeCascade(GraphicsDevice graphicsDevice)
    {
        final GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        final Rectangle bounds = graphicsConfiguration.getBounds();
        final Insets inset = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        // adjust bounds of current screen
        bounds.x += inset.left;
        bounds.y += inset.top;
        bounds.width -= inset.left + inset.right;
        bounds.height -= inset.top + inset.bottom;

        // prepare frames to process
        final ArrayList<Frame> frames = new ArrayList<Frame>();

        for (Frame f : Frame.getFrames())
            // add visible and resizable frame contained in this screen
            if ((f != this) && !ComponentUtil.isMinimized(f) && f.isResizable() && f.isVisible()
                    && bounds.contains(f.getLocation()))
                frames.add(f);

        // this screen contains the main frame ?
        if (bounds.contains(getLocation()))
        {
            // move main frame at top
            setLocation(bounds.x, bounds.y);

            final int mainFrameW = getWidth();
            final int mainFrameH = getHeight();

            // adjust available bounds of current screen
            if (mainFrameW > mainFrameH)
            {
                bounds.y += mainFrameH;
                bounds.height -= mainFrameH;
            }
            else
            {
                bounds.x += mainFrameW;
                bounds.width -= mainFrameW;
            }
        }

        // available space
        final int w = bounds.width;
        final int h = bounds.height;

        final int xMax = bounds.x + w;
        final int yMax = bounds.y + h;

        final int fw = (int) (w * 0.6f);
        final int fh = (int) (h * 0.6f);

        int x = bounds.x + 32;
        int y = bounds.y + 32;

        for (Frame f : frames)
        {
            f.setBounds(x, y, fw, fh);
            x += 30;
            y += 20;
            if ((x + fw) > xMax)
                x = bounds.x + 32;
            if ((y + fh) > yMax)
                y = bounds.y + 32;
        }
    }

    /**
     * Organize all frames in tile.<br>
     * 
     * @param type
     *        tile type.<br>
     *        TILE_HORIZONTAL, TILE_VERTICAL or TILE_GRID.
     */
    public void organizeTile(int type)
    {
        // all screen devices
        final GraphicsDevice screenDevices[] = SystemUtil.getLocalGraphicsEnvironment().getScreenDevices();
        // screen devices to process
        final ArrayList<GraphicsDevice> devices = new ArrayList<GraphicsDevice>();

        // detached mode ?
        if (isDetachedMode())
        {
            // process all available screen for cascade organization
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    devices.add(dev);
        }
        else
        {
            // process desktop pane cascade organization
            desktopPane.organizeTile(type);

            // we process screen where the mainFrame is not visible
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    if (!dev.getDefaultConfiguration().getBounds().contains(getLocation()))
                        devices.add(dev);
        }

        // organize frames on different screen
        for (GraphicsDevice dev : devices)
            organizeTile(dev, type);
    }

    /**
     * Organize frames in tile on the specified graphics device.
     */
    protected void organizeTile(GraphicsDevice graphicsDevice, int type)
    {
        final GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        final Rectangle bounds = graphicsConfiguration.getBounds();
        final Insets inset = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        // adjust bounds of current screen
        bounds.x += inset.left;
        bounds.y += inset.top;
        bounds.width -= inset.left + inset.right;
        bounds.height -= inset.top + inset.bottom;

        // prepare frames to process
        final ArrayList<Frame> frames = new ArrayList<Frame>();

        for (Frame f : Frame.getFrames())
            // add visible and resizable frame contained in this screen
            if ((f != this) && !ComponentUtil.isMinimized(f) && f.isResizable() && f.isVisible()
                    && bounds.contains(f.getLocation()))
                frames.add(f);

        // this screen contains the main frame ?
        if (bounds.contains(getLocation()))
        {
            // move main frame at top
            setLocation(bounds.x, bounds.y);

            final int mainFrameW = getWidth();
            final int mainFrameH = getHeight();

            // adjust available bounds of current screen
            if (mainFrameW > mainFrameH)
            {
                bounds.y += mainFrameH;
                bounds.height -= mainFrameH;
            }
            else
            {
                bounds.x += mainFrameW;
                bounds.width -= mainFrameW;
            }
        }

        final int numFrames = frames.size();

        // nothing to do
        if (numFrames == 0)
            return;

        // available space
        final int w = bounds.width;
        final int h = bounds.height;
        final int x = bounds.x;
        final int y = bounds.y;

        int numCol;
        int numLine;

        switch (type)
        {
            case MainFrame.TILE_HORIZONTAL:
                numCol = 1;
                numLine = numFrames;
                break;

            case MainFrame.TILE_VERTICAL:
                numCol = numFrames;
                numLine = 1;
                break;

            default:
                numCol = (int) Math.sqrt(numFrames);
                if (numFrames != (numCol * numCol))
                    numCol++;
                numLine = numFrames / numCol;
                if (numFrames > (numCol * numLine))
                    numLine++;
                break;
        }

        final int dx = w / numCol;
        final int dy = h / numLine;

        int k = 0;
        for (int i = 0; i < numLine; ++i)
            for (int j = 0; j < numCol && k < numFrames; ++j, ++k)
                frames.get(i * numCol + j).setBounds(x + (j * dx), y + (i * dy), dx, dy);
    }

    /**
     * Set detached window mode.<br>
     */
    public void setDetachedMode(boolean value)
    {
        if (detachedMode != value)
        {
            // detached mode
            if (value)
            {
                // save inspector state
                previousInspectorInternalized = inspector.isInternalized();
                // save it in preferences...
                positionSaver.getPreferences().putBoolean(ID_PREVIOUS_STATE, previousInspectorInternalized);

                // externalize inspector
                externalizeInspector();
                // no more internalization possible
                inspector.setInternalizationAutorized(false);

                // save the current height & state
                previousHeight = getHeight();
                previousMaximized = ComponentUtil.isMaximized(this);

                // hide main pane & resize window to ribbon dimension
                remove(mainPane);
                ComponentUtil.setMaximized(this, false);
                // recompute layout
                validate();
                // force resize to ribbon size
                setSize(getWidth(), getMinimumSize().height);
            }
            // single window mode
            else
            {
                // show main pane & resize window back to original dimension
                add(mainPane, BorderLayout.CENTER);
                setSize(getWidth(), previousHeight);
                if (previousMaximized)
                    ComponentUtil.setMaximized(this, true);
                // recompute layout
                validate();

                // internalization possible
                inspector.setInternalizationAutorized(true);
                // restore inspector internalization
                if (previousInspectorInternalized)
                    internalizeInspector();
            }

            detachedMode = value;

            // notify mode change
            firePropertyChange(PROPERTY_DETACHEDMODE, !value, value);
        }
    }
}

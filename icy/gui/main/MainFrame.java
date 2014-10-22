/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.gui.main;

import icy.action.FileActions;
import icy.action.GeneralActions;
import icy.action.SequenceOperationActions;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.ExternalizablePanel.StateListener;
import icy.gui.frame.IcyExternalFrame;
import icy.gui.inspector.ChatPanel;
import icy.gui.inspector.InspectorPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.MainRibbon;
import icy.gui.menu.search.SearchBar;
import icy.gui.util.ComponentUtil;
import icy.gui.util.WindowPositionSaver;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.math.HungarianAlgorithm;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyApplicationIcon;
import icy.system.FileDrop;
import icy.system.FileDrop.FileDropListener;
import icy.system.SystemUtil;
import icy.type.collection.CollectionUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

/**
 * @author fab & Stephane
 */
public class MainFrame extends JRibbonFrame
{
    private static Rectangle getDefaultBounds()
    {
        Rectangle r = SystemUtil.getMaximumWindowBounds();

        r.width -= 100;
        r.height -= 100;
        r.x += 50;
        r.y += 50;

        return r;
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
    boolean detachedMode;
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
        // but this might add problems with mac OSX
        // JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        // ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);

        // FIXME : remove this when Ribbon with have fixed KeyTipLayer component
        getRootPane().getLayeredPane().getComponent(0).setVisible(false);

        // SubstanceRibbonFrameTitlePane titlePane = (SubstanceRibbonFrameTitlePane)
        // LookAndFeelUtil.getTitlePane(this);
        // JCheckBox comp = new JCheckBox("test")
        // comp.setP
        // titlePane.add();
        //
        // "substancelaf.internal.titlePane.extraComponentKind"
        // titlePane.m

        final Rectangle defaultBounds = getDefaultBounds();

        positionSaver = new WindowPositionSaver(this, "frame/main", defaultBounds.getLocation(),
                defaultBounds.getSize());
        previousInspectorInternalized = positionSaver.getPreferences().getBoolean(ID_PREVIOUS_STATE, true);

        // set "always on top" state
        setAlwaysOnTop(GeneralPreferences.getAlwaysOnTop());
        // default close operation
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // build ribbon
        mainRibbon = new MainRibbon(getRibbon());

        // set application icons
        setIconImages(ResourceUtil.getIcyIconImages());
        setApplicationIcon(new IcyApplicationIcon());

        // set minimized state
        getRibbon().setMinimized(GeneralPreferences.getRibbonMinimized());

        // main center pane (contains desktop pane)
        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        // desktop pane
        desktopPane = new IcyDesktopPane();
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

        // action on file drop
        final FileDropListener fileDropListener = new FileDropListener()
        {
            @Override
            public void filesDropped(File[] files)
            {
                Loader.load(CollectionUtil.asList(FileUtil.toPaths(files)), false, true, true);
            }
        };

        // handle file drop in desktop pane and in ribbon pane
        new FileDrop(desktopPane, BorderFactory.createLineBorder(Color.blue.brighter(), 2), false, fileDropListener);
        new FileDrop(getRibbon(), BorderFactory.createLineBorder(Color.blue.brighter(), 1), false, fileDropListener);

        // listen ribbon minimization event
        getRibbon().addPropertyChangeListener(JRibbon.PROPERTY_MINIMIZED, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                final boolean value = ((Boolean) evt.getNewValue()).booleanValue();

                // pack the frame in detached mode
                if (detachedMode)
                    pack();

                // save state in preferene
                GeneralPreferences.setRibbonMinimized(value);
            }
        });
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

                if (detachedMode)
                {
                    // fix height
                    final int prefH = getPreferredSize().height;

                    if (getHeight() > prefH)
                        setSize(getWidth(), prefH);
                }
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

        // initialize now some stuff that need main frame to be initialized
        mainRibbon.init();

        setVisible(true);

        // can be done after setVisible
        buildActionMap();
    }

    void buildActionMap()
    {
        // global input map
        buildActionMap(mainPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), mainPane.getActionMap());
    }

    private void buildActionMap(InputMap imap, ActionMap amap)
    {
        imap.put(GeneralActions.searchAction.getKeyStroke(), GeneralActions.searchAction.getName());
        imap.put(FileActions.openSequenceAction.getKeyStroke(), FileActions.openSequenceAction.getName());
        imap.put(FileActions.saveAsSequenceAction.getKeyStroke(), FileActions.saveAsSequenceAction.getName());
        imap.put(GeneralActions.onlineHelpAction.getKeyStroke(), GeneralActions.onlineHelpAction.getName());
        imap.put(SequenceOperationActions.undoAction.getKeyStroke(), SequenceOperationActions.undoAction.getName());
        imap.put(SequenceOperationActions.redoAction.getKeyStroke(), SequenceOperationActions.redoAction.getName());

        amap.put(GeneralActions.searchAction.getName(), GeneralActions.searchAction);
        amap.put(FileActions.openSequenceAction.getName(), FileActions.openSequenceAction);
        amap.put(FileActions.saveAsSequenceAction.getName(), FileActions.saveAsSequenceAction);
        amap.put(GeneralActions.onlineHelpAction.getName(), GeneralActions.onlineHelpAction);
        amap.put(SequenceOperationActions.undoAction.getName(), SequenceOperationActions.undoAction);
        amap.put(SequenceOperationActions.redoAction.getName(), SequenceOperationActions.redoAction);
    }

    public ApplicationMenu getApplicationMenu()
    {
        return (ApplicationMenu) getRibbon().getApplicationMenu();
    }

    /**
     * Returns the center pane, this pane contains the desktop pane.<br>
     * Feel free to add temporary top/left/right or bottom pane to it.
     */
    public JPanel getCenterPanel()
    {
        return centerPanel;
    }

    /**
     * Returns the {@link SearchBar} component.
     */
    public SearchBar getSearchBar()
    {
        if (mainRibbon != null)
            return mainRibbon.getSearchBar();

        return null;
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
            return CollectionUtil.asArrayList(desktopPane.getAllFrames());

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
     * If the main frame is in "detached" mode this actually return the system desktop dimension.
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
     * Returns the list of internal viewers.
     * 
     * @param bounds
     *        If not null only viewers visible in the specified bounds are returned.
     * @param wantNotVisible
     *        Also return not visible viewers
     * @param wantIconized
     *        Also return iconized viewers
     */
    public Viewer[] getExternalViewers(Rectangle bounds, boolean wantNotVisible, boolean wantIconized)
    {
        final List<Viewer> result = new ArrayList<Viewer>();

        for (Viewer viewer : Icy.getMainInterface().getViewers())
        {
            if (viewer.isExternalized())
            {
                final IcyExternalFrame externalFrame = viewer.getIcyExternalFrame();

                if ((wantNotVisible || externalFrame.isVisible())
                        && (wantIconized || !ComponentUtil.isMinimized(externalFrame))
                        && ((bounds == null) || bounds.contains(ComponentUtil.getCenter(externalFrame))))
                    result.add(viewer);
            }
        }

        return result.toArray(new Viewer[result.size()]);
    }

    /**
     * Returns the list of internal viewers.
     * 
     * @param wantNotVisible
     *        Also return not visible viewers
     * @param wantIconized
     *        Also return iconized viewers
     */
    public Viewer[] getExternalViewers(boolean wantNotVisible, boolean wantIconized)
    {
        return getExternalViewers(null, wantNotVisible, wantIconized);
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
        final Insets inset = getToolkit().getScreenInsets(graphicsConfiguration);

        // adjust bounds of current screen
        bounds.x += inset.left;
        bounds.y += inset.top;
        bounds.width -= inset.left + inset.right;
        bounds.height -= inset.top + inset.bottom;

        // prepare viewers to process
        final Viewer[] viewers = getExternalViewers(bounds, false, false);

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

        for (Viewer v : viewers)
        {
            final IcyExternalFrame externalFrame = v.getIcyExternalFrame();

            if (externalFrame.isMaximized())
                externalFrame.setMaximized(false);
            externalFrame.setBounds(x, y, fw, fh);
            externalFrame.toFront();

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
            // process desktop pane tile organization
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

        // prepare viewers to process
        final Viewer[] viewers = getExternalViewers(bounds, false, false);

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

        final int numFrames = viewers.length;

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

        final double[][] framesDistances = new double[numCol * numLine][numFrames];

        final int dx = w / numCol;
        final int dy = h / numLine;
        int k = 0;

        for (int i = 0; i < numLine; i++)
        {
            for (int j = 0; j < numCol; j++, k++)
            {
                final double[] distances = framesDistances[k];
                final double fx = x + (j * dx) + (dx / 2d);
                final double fy = y + (i * dy) + (dy / 2d);

                for (int f = 0; f < numFrames; f++)
                {
                    final Point2D.Double center = ComponentUtil.getCenter(viewers[f].getExternalFrame());
                    distances[f] = Point2D.distanceSq(center.x, center.y, fx, fy);
                }
            }
        }

        final int[] framePos = new HungarianAlgorithm(framesDistances).resolve();

        k = 0;
        for (int i = 0; i < numLine; i++)
        {
            for (int j = 0; j < numCol; j++, k++)
            {
                final int f = framePos[k];

                if (f < numFrames)
                {
                    final IcyExternalFrame externalFrame = viewers[f].getIcyExternalFrame();

                    if (externalFrame.isMaximized())
                        externalFrame.setMaximized(false);
                    externalFrame.setBounds(x + (j * dx), y + (i * dy), dx, dy);
                    externalFrame.toFront();
                }
            }
        }
    }

    /**
     * Set detached window mode.
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

                // hide main pane and remove maximized state
                remove(mainPane);
                ComponentUtil.setMaximized(this, false);
                // and pack the frame
                pack();
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

    /**
     * Refresh connected username informations
     */
    public void refreshUserInfos()
    {
        final String login = GeneralPreferences.getUserLogin();
        final String userName = GeneralPreferences.getUserName();

        if (!StringUtil.isEmpty(userName))
            setTitle(TITLE + " - " + userName);
        else if (!StringUtil.isEmpty(login))
            setTitle(TITLE + " - " + login);
        else
            setTitle(TITLE);
    }

    @Override
    public void paint(Graphics g)
    {
        // new size arrives sometime in paint before resize event
        if (detachedMode)
        {
            // fix height
            final int prefH = getPreferredSize().height;

            if (getHeight() > prefH)
            {
                setSize(getWidth(), prefH);
                return;
            }
        }

        super.paint(g);
    }

}

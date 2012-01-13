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
import icy.gui.frame.IcyFrame;
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
import java.awt.HeadlessException;
import java.awt.Point;
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

    private final MainRibbon mainRibbon;
    final JSplitPane mainPane;
    private final IcyDesktopPane desktopPane;
    final InspectorPanel inspector;
    private final FileAndTextTransferHandler fileAndTextTransferHandler;
    private boolean multiWindowMode;
    int lastInspectorWidth;

    // state save for multi window mode
    private int previousHeight;
    private boolean previousMaximized;
    private boolean previousInspectorInternalized;

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

        new WindowPositionSaver(this, "frame/main", new Point(50, 50), new Dimension(800, 600));

        // transfert handler
        fileAndTextTransferHandler = new FileAndTextTransferHandler();

        // set "always on top" state
        setAlwaysOnTop(GeneralPreferences.getAlwaysOnTop());
        // default close operation
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                // keep height to minimum height when we are in multi window mode
                if (isMultiWindowMode())
                    setSize(getWidth(), getMinimumSize().height);
            }
        });

        // build ribbon
        mainRibbon = new MainRibbon(getRibbon());

        // set application icons
        setIconImages(ResourceUtil.getIcyIconImages());
        setApplicationIcon(new IcyApplicationIcon());

        // desktop pane
        desktopPane = new IcyDesktopPane();
        desktopPane.setTransferHandler(fileAndTextTransferHandler);

        // inspector
        inspector = new InspectorPanel();

        mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, desktopPane, null);
        // take in account the divider and border size
        lastInspectorWidth = getWidth() - (inspector.getPreferredSize().width + 6 + 8);
        previousInspectorInternalized = inspector.isInternalized();
        if (previousInspectorInternalized)
        {
            mainPane.setRightComponent(inspector);
            mainPane.setDividerSize(6);
            mainPane.setDividerLocation(lastInspectorWidth);
        }
        else
        {
            mainPane.setDividerSize(0);
            inspector.setParent(mainPane);
        }
        mainPane.setResizeWeight(1);
        mainPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() > 1)
                {
                    if (isInpectorInternalized())
                        externalizeInspector();
                    else
                        internalizeInspector();
                }
            }
        });

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
        multiWindowMode = GeneralPreferences.getMultiWindowMode();

        // multi window mode
        if (multiWindowMode)
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
     * @return the desktopPane
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
     * Return true if the main frame is in "multi window" mode
     */
    public boolean isMultiWindowMode()
    {
        return multiWindowMode;
    }

    /**
     * Return content pane dimension (available area in main frame).<br>
     * If the main frame is in "multi window" mode this actually return the desktop dimension.
     */
    public Dimension getDesktopSize()
    {
        if (multiWindowMode)
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
     * Always returns false in multi window mode.
     */
    public boolean isInpectorInternalized()
    {
        return inspector.isInternalized();
    }

    /**
     * Internalize the inspector in main container.<br>
     * The method fails and returns false in multi window mode.
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

    public void setMultiWindowMode(boolean value)
    {
        if (multiWindowMode != value)
        {
            // multi window mode
            if (value)
            {
                // save inspector state
                previousInspectorInternalized = inspector.isInternalized();

                // externalize inspector
                externalizeInspector();
                // no more internalization possible
                inspector.setInternalizationAutorized(false);

                // externalize all IcyFrame
                for (IcyFrame frame : IcyFrame.getAllFrames())
                    frame.externalize();

                // save the current height & state
                previousHeight = getHeight();
                previousMaximized = ComponentUtil.isMaximized(this);

                // hide main pane & resize window to ribbon dimension
                remove(mainPane);
                ComponentUtil.setMaximized(this, false);
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

                // internalization possible
                inspector.setInternalizationAutorized(true);
                // restore inspector internalization
                if (previousInspectorInternalized)
                    internalizeInspector();
            }

            multiWindowMode = value;
        }
    }
}

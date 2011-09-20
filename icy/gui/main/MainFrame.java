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
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.ExternalizablePanel.StateListener;
import icy.gui.inspector.InspectorPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.MainRibbon;
import icy.gui.util.WindowPositionSaver;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyApplicationIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;

import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

/**
 * @author fab
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

        /** Your helpfull function */
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
    private final IcyDesktopPane desktopPane;
    // private final BottomPanel bottomPanel;
    final InspectorPanel inspector;
    private final FileAndTextTransferHandler fileAndTextTransferHandler;
    int lastInspectorWidth;

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

        // desktop pane
        desktopPane = new IcyDesktopPane();
        desktopPane.setTransferHandler(fileAndTextTransferHandler);

        // inspector
        inspector = new InspectorPanel();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, desktopPane, null);
        // take in account the divider size
        lastInspectorWidth = getContentPane().getWidth() - (inspector.getPreferredSize().width + 6);
        if (inspector.isInternalized())
        {
            splitPane.setRightComponent(inspector);
            splitPane.setDividerSize(6);
            splitPane.setDividerLocation(lastInspectorWidth);
        }
        else
        {
            splitPane.setDividerSize(0);
            inspector.setParent(splitPane);
        }
        splitPane.setResizeWeight(1);
        splitPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() > 1)
                {
                    // save diviser location
                    if (inspector.isInternalized())
                        lastInspectorWidth = getWidth() - splitPane.getDividerLocation();

                    inspector.switchState();
                }
            }
        });

        inspector.addStateListener(new StateListener()
        {
            @Override
            public void stateChanged(ExternalizablePanel source, boolean externalized)
            {
                if (externalized)
                    splitPane.setDividerSize(0);
                else
                {
                    splitPane.setDividerSize(6);
                    // restore previous location
                    splitPane.setDividerLocation(getWidth() - lastInspectorWidth);
                }
            }
        });

        add(splitPane, BorderLayout.CENTER);

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

    // /**
    // * @return the bottomPanel
    // */
    // public BottomPanel getBottomPanel()
    // {
    // return bottomPanel;
    // }

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
     * Return content pane dimension (available area in main frame)
     */
    public Dimension getContentSize()
    {
        return desktopPane.getSize();
    }

    /**
     * Return content pane width
     */
    public int getContentWidth()
    {
        return desktopPane.getWidth();
    }

    /**
     * Return content pane height
     */
    public int getContentHeight()
    {
        return desktopPane.getHeight();
    }
}

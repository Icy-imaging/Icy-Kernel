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
package icy.gui.util;

import icy.common.listener.weak.WeakComponentListener;
import icy.common.listener.weak.WeakIcyFrameListener;
import icy.common.listener.weak.WeakWindowListener;
import icy.gui.component.ComponentUtil;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.ExternalizablePanel.StateListener;
import icy.gui.component.ExternalizablePanel.WeakStateListener;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.main.MainFrame;
import icy.preferences.IcyPreferences;
import icy.preferences.XMLPreferences;
import icy.system.SystemUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Load and Save you window position in the registry
 * 
 * @author fab & stephane
 */
public class WindowPositionSaver
{
    final private static int DESKTOP_MARGIN = 100;

    final private static String ID_EXTERNALIZED = "Externalized";
    final private static String ID_PANELIZED = "Panelized";

    final private static String ID_XC = "XC";
    final private static String ID_YC = "YC";
    final private static String ID_WC = "WidthC";
    final private static String ID_HC = "HeightC";

    final private static String ID_XI = "XI";
    final private static String ID_YI = "YI";
    final private static String ID_WI = "WidthI";
    final private static String ID_HI = "HeightI";
    final private static String ID_MAXIMIZEDI = "MaximizedI";

    final private static String ID_XE = "XE";
    final private static String ID_YE = "YE";
    final private static String ID_WE = "WidthE";
    final private static String ID_HE = "HeightE";
    final private static String ID_MAXIMIZEDE = "MaximizedE";

    final private MainFrame mainFrame;
    final private IcyFrame icyFrame;
    final private ExternalizablePanel extPanel;
    final private JFrame jFrame;
    final private JComponent component;

    final WindowAdapter windowAdapter;
    final IcyFrameAdapter icyFrameAdapter;
    final ComponentAdapter componentAdapter;
    final StateListener stateListener;

    private WindowPositionSaver(final MainFrame mainFrame, final IcyFrame icyFrame, final ExternalizablePanel extPanel,
            final JFrame jFrame, final JComponent component, final String key, final Point defLoc,
            final Dimension defDim)
    {
        final XMLPreferences preferences = IcyPreferences.root().node(key);

        this.mainFrame = mainFrame;
        this.icyFrame = icyFrame;
        this.extPanel = extPanel;
        this.jFrame = jFrame;
        this.component = component;

        // directly load location and dimension
        if (defLoc != null)
            loadLocation(preferences, defLoc);
        if (defDim != null)
            loadDimension(preferences, defDim);
        loadState(preferences);

        checkPosition();

        // code for save frame informations
        final Runnable saver = new Runnable()
        {
            @Override
            public void run()
            {
                final XMLPreferences preferences = IcyPreferences.root().node(key);

                if (defLoc != null)
                    saveLocation(preferences);
                if (defDim != null)
                    saveDimension(preferences);
                saveState(preferences);
            }
        };

        // keep hard reference on it
        componentAdapter = new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                if (extPanel != null)
                {
                    if (e.getSource() == extPanel)
                        // event comes from panel itself
                        saver.run();
                    else
                        // event comes from extern frame
                        checkPosition();
                }
                else
                {
                    checkPosition();
                    saver.run();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                if (extPanel != null)
                {
                    // event comes from extern frame
                    if (e.getSource() != extPanel)
                    {
                        saver.run();
                        checkPosition();
                    }
                }
                else
                {
                    checkPosition();
                    saver.run();
                }
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
                // correctly save internalized / externalized state
                saver.run();
            }

            @Override
            public void componentHidden(ComponentEvent e)
            {
                // correctly save internalized / externalized state
                saver.run();
            }
        };

        // keep hard reference on it
        windowAdapter = new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                saver.run();
            }
        };

        // keep hard reference on it
        icyFrameAdapter = new IcyFrameAdapter()
        {
            @Override
            public void icyFrameClosing(IcyFrameEvent e)
            {
                saver.run();
            }
        };

        // keep hard reference on it
        stateListener = new StateListener()
        {
            @Override
            public void stateChanged(ExternalizablePanel source, boolean externalized)
            {
                saver.run();
            }
        };

        if (mainFrame != null)
        {
            mainFrame.addWindowListener(new WeakWindowListener(windowAdapter));
            mainFrame.addComponentListener(new WeakComponentListener(componentAdapter));
        }
        else if (icyFrame != null)
        {
            icyFrame.addFrameListener(new WeakIcyFrameListener(icyFrameAdapter));
            icyFrame.addComponentListener(new WeakComponentListener(componentAdapter));
        }
        else if (extPanel != null)
        {
            extPanel.addComponentListener(new WeakComponentListener(componentAdapter));
            extPanel.addStateListener(new WeakStateListener(stateListener));
            extPanel.getFrame().addFrameListener(new WeakIcyFrameListener(icyFrameAdapter));
            extPanel.getFrame().addComponentListener(new WeakComponentListener(componentAdapter));
        }
        else if (jFrame != null)
        {
            jFrame.addWindowListener(new WeakWindowListener(windowAdapter));
            jFrame.addComponentListener(new WeakComponentListener(componentAdapter));
        }
        else if (component != null)
        {
            component.addComponentListener(new WeakComponentListener(componentAdapter));
        }
    }

    public WindowPositionSaver(MainFrame frame, String key, Point defLoc, Dimension defDim)
    {
        this(frame, null, null, null, null, key, defLoc, defDim);
    }

    public WindowPositionSaver(MainFrame frame, String key, Point defLoc)
    {
        this(frame, null, null, null, null, key, defLoc, null);
    }

    public WindowPositionSaver(MainFrame frame, String key, Dimension defDim)
    {
        this(frame, null, null, null, null, key, null, defDim);
    }

    public WindowPositionSaver(IcyFrame frame, String key, Point defLoc, Dimension defDim)
    {
        this(null, frame, null, null, null, key, defLoc, defDim);
    }

    public WindowPositionSaver(IcyFrame frame, String key, Point defLoc)
    {
        this(null, frame, null, null, null, key, defLoc, null);
    }

    public WindowPositionSaver(IcyFrame frame, String key, Dimension defDim)
    {
        this(null, frame, null, null, null, key, null, defDim);
    }

    public WindowPositionSaver(ExternalizablePanel extPanel, String key, Point defLoc, Dimension defDim)
    {
        this(null, null, extPanel, null, null, key, defLoc, defDim);
    }

    public WindowPositionSaver(ExternalizablePanel extPanel, String key, Point defLoc)
    {
        this(null, null, extPanel, null, null, key, defLoc, null);
    }

    public WindowPositionSaver(ExternalizablePanel extPanel, String key, Dimension defDim)
    {
        this(null, null, extPanel, null, null, key, null, defDim);
    }

    public WindowPositionSaver(JFrame frame, String key, Point defLoc, Dimension defDim)
    {
        this(null, null, null, frame, null, key, defLoc, defDim);
    }

    public WindowPositionSaver(JFrame frame, String key, Point defLoc)
    {
        this(null, null, null, frame, null, key, defLoc, null);
    }

    public WindowPositionSaver(JFrame frame, String key, Dimension defDim)
    {
        this(null, null, null, frame, null, key, null, defDim);
    }

    public WindowPositionSaver(JComponent component, String key, Point defLoc, Dimension defDim)
    {
        this(null, null, null, null, component, key, defLoc, defDim);
    }

    public WindowPositionSaver(JComponent component, String key, Point defLoc)
    {
        this(null, null, null, null, component, key, defLoc, null);
    }

    public WindowPositionSaver(JComponent component, String key, Dimension defDim)
    {
        this(null, null, null, null, component, key, null, defDim);
    }

    void checkPosition()
    {
        final Rectangle rect;

        // only for frame
        if (mainFrame != null)
            rect = mainFrame.getBounds();
        else if (icyFrame != null)
            rect = icyFrame.getBounds();
        // check position only for frame
        else if ((extPanel != null) && extPanel.isExternalized())
            rect = extPanel.getFrame().getBounds();
        else if (jFrame != null)
            rect = jFrame.getBounds();
        else
            return;

        if (fixPosition(rect))
        {
            if (mainFrame != null)
                mainFrame.setBounds(rect);
            else if (icyFrame != null)
                icyFrame.setBounds(rect);
            // check position only for frame
            else if ((extPanel != null) && extPanel.isExternalized())
                extPanel.getFrame().setBounds(rect);
            else if (jFrame != null)
                jFrame.setBounds(rect);
        }
    }

    boolean fixPosition(Rectangle rect)
    {
        Rectangle desktopRect = SystemUtil.getDesktopBounds();
        boolean result = false;
        int limit;

        limit = (int) (desktopRect.getMaxX() - DESKTOP_MARGIN);
        if (rect.x >= limit)
        {
            rect.x = limit;
            result = true;
        }
        else
        {
            limit = (int) desktopRect.getMinX();
            if (((rect.x + rect.width) - DESKTOP_MARGIN) < limit)
            {
                rect.x = DESKTOP_MARGIN - rect.width;
                result = true;
            }
        }
        limit = (int) (desktopRect.getMaxY() - DESKTOP_MARGIN);
        if (rect.y >= limit)
        {
            rect.y = limit;
            result = true;
        }
        else
        {
            limit = (int) desktopRect.getMinY();
            if (rect.y < limit)
            {
                rect.y = limit;
                result = true;
            }
        }

        return result;
    }

    public void loadLocation(XMLPreferences preferences, Point defLoc)
    {
        final int defX, defY;

        if (defLoc == null)
        {
            defX = 0;
            defY = 0;
        }
        else
        {
            defX = defLoc.x;
            defY = defLoc.y;
        }

        final Point positionC = new Point(preferences.getInt(ID_XC, defX), preferences.getInt(ID_YC, defY));
        final Point positionI = new Point(preferences.getInt(ID_XI, defX), preferences.getInt(ID_YI, defY));
        final Point positionE = new Point(preferences.getInt(ID_XE, defX), preferences.getInt(ID_YE, defY));

        if (mainFrame != null)
        {
            mainFrame.setLocation(positionE);
        }
        else if (icyFrame != null)
        {
            icyFrame.setLocationInternal(positionI);
            icyFrame.setLocationExternal(positionE);
        }
        else if (extPanel != null)
        {
            extPanel.setLocation(positionC);

            // get the panel frame
            final IcyFrame f = extPanel.getFrame();

            f.setLocationInternal(positionI);
            f.setLocationExternal(positionE);
        }
        else if (jFrame != null)
        {
            jFrame.setLocation(positionE);
        }
        else if (component != null)
        {
            component.setLocation(positionC);
        }
    }

    public void loadDimension(XMLPreferences preferences, Dimension defDim)
    {
        final int defW, defH;

        if (defDim == null)
        {
            defW = 300;
            defH = 300;
        }
        else
        {
            defW = defDim.width;
            defH = defDim.height;
        }

        // minimum size is 10 pixels
        int widthC = Math.max(preferences.getInt(ID_WC, defW), 10);
        int heightC = Math.max(preferences.getInt(ID_HC, defH), 10);
        int widthI = Math.max(preferences.getInt(ID_WI, defW), 10);
        int heightI = Math.max(preferences.getInt(ID_HI, defH), 10);
        int widthE = Math.max(preferences.getInt(ID_WE, defW), 10);
        int heightE = Math.max(preferences.getInt(ID_HE, defH), 10);

        final Dimension dimC = new Dimension(widthC, heightC);
        final Dimension dimI = new Dimension(widthI, heightI);
        final Dimension dimE = new Dimension(widthE, heightE);

        if (mainFrame != null)
        {
            mainFrame.setPreferredSize(dimE);
            mainFrame.setSize(dimE);
        }
        else if (icyFrame != null)
        {
            icyFrame.setPreferredSizeExternal(dimE);
            icyFrame.setSizeExternal(dimE);
            icyFrame.setPreferredSizeInternal(dimI);
            icyFrame.setSizeInternal(dimI);
        }
        else if (extPanel != null)
        {
            extPanel.setPreferredSize(dimC);

            // get the panel frame
            final IcyFrame f = extPanel.getFrame();

            f.setPreferredSizeExternal(dimE);
            f.setSizeExternal(dimE);
            f.setPreferredSizeInternal(dimI);
            f.setSizeInternal(dimI);
        }
        else if (jFrame != null)
        {
            jFrame.setPreferredSize(dimE);
            jFrame.setSize(dimE);
        }
        else if (component != null)
        {
            component.setPreferredSize(dimC);
        }
    }

    public void loadState(XMLPreferences preferences)
    {
        // default is internalized and panelized
        final boolean externalized = preferences.getBoolean(ID_EXTERNALIZED, false);
        final boolean panelized = preferences.getBoolean(ID_PANELIZED, true);
        final boolean maximizedE = preferences.getBoolean(ID_MAXIMIZEDE, false);
        final boolean maximizedI = preferences.getBoolean(ID_MAXIMIZEDI, false);

        if (mainFrame != null)
        {
            ComponentUtil.setMaximized(mainFrame, maximizedE);
        }
        else if (icyFrame != null)
        {
            if (externalized)
                icyFrame.externalize();
            else
                icyFrame.internalize();
            icyFrame.setMaximizedExternal(maximizedE);
            icyFrame.setMaximizedInternal(maximizedI);
        }
        else if (extPanel != null)
        {
            // get the panel frame
            final IcyFrame f = extPanel.getFrame();

            if (externalized)
                f.externalize();
            else
                f.internalize();
            f.setMaximizedExternal(maximizedE);
            f.setMaximizedInternal(maximizedI);

            if (panelized)
                extPanel.internalize();
            else
                extPanel.externalize();
        }
        else if (jFrame != null)
        {
            ComponentUtil.setMaximized(jFrame, maximizedE);
        }
    }

    public void saveLocation(XMLPreferences preferences)
    {
        if (mainFrame != null)
        {
            if (!(ComponentUtil.isMaximized(mainFrame) || ComponentUtil.isMinimized(mainFrame)))
            {
                preferences.putInt(ID_XE, mainFrame.getX());
                preferences.putInt(ID_YE, mainFrame.getY());
            }
        }
        else if (icyFrame != null)
        {
            if (!(icyFrame.isMaximizedExternal() || icyFrame.isMinimizedExternal()))
            {
                preferences.putInt(ID_XE, icyFrame.getXExternal());
                preferences.putInt(ID_YE, icyFrame.getYExternal());
            }
            if (!(icyFrame.isMaximizedInternal() || icyFrame.isMinimizedInternal()))
            {
                preferences.putInt(ID_XI, icyFrame.getXInternal());
                preferences.putInt(ID_YI, icyFrame.getYInternal());
            }
        }
        else if (extPanel != null)
        {
            preferences.putInt(ID_XC, extPanel.getX());
            preferences.putInt(ID_YC, extPanel.getY());

            // get the panel frame
            final IcyFrame f = extPanel.getFrame();

            if (!(f.isMaximizedExternal() || f.isMinimizedExternal()))
            {
                preferences.putInt(ID_XE, f.getXExternal());
                preferences.putInt(ID_YE, f.getYExternal());
            }
            if (!(f.isMaximizedInternal() || f.isMinimizedInternal()))
            {
                preferences.putInt(ID_XI, f.getXInternal());
                preferences.putInt(ID_YI, f.getYInternal());
            }
        }
        else if (jFrame != null)
        {
            if (!(ComponentUtil.isMaximized(jFrame) || ComponentUtil.isMinimized(jFrame)))
            {
                preferences.putInt(ID_XE, jFrame.getX());
                preferences.putInt(ID_YE, jFrame.getY());
            }
        }
        else if (component != null)
        {
            preferences.putInt(ID_XC, component.getX());
            preferences.putInt(ID_YC, component.getY());
        }
    }

    public void saveDimension(XMLPreferences preferences)
    {
        if (mainFrame != null)
        {
            if (!(ComponentUtil.isMaximized(mainFrame) || ComponentUtil.isMinimized(mainFrame)))
            {
                final Dimension dim;

                if (mainFrame.isDetachedMode())
                    dim = new Dimension(mainFrame.getWidth(), mainFrame.getPreviousHeight());
                else
                    dim = mainFrame.getSize();

                preferences.putInt(ID_WE, dim.width);
                preferences.putInt(ID_HE, dim.height);
            }
        }
        else if (icyFrame != null)
        {
            if (!(icyFrame.isMaximizedExternal() || icyFrame.isMinimizedExternal()))
            {
                preferences.putInt(ID_WE, icyFrame.getWidthExternal());
                preferences.putInt(ID_HE, icyFrame.getHeightExternal());
            }
            if (!(icyFrame.isMaximizedInternal() || icyFrame.isMinimizedInternal()))
            {
                preferences.putInt(ID_WI, icyFrame.getWidthInternal());
                preferences.putInt(ID_HI, icyFrame.getHeightInternal());
            }
        }
        else if (extPanel != null)
        {
            preferences.putInt(ID_WC, extPanel.getWidth());
            preferences.putInt(ID_HC, extPanel.getHeight());

            // get the panel frame
            final IcyFrame f = extPanel.getFrame();

            if (!(f.isMaximizedExternal() || f.isMinimizedExternal()))
            {
                preferences.putInt(ID_WE, f.getWidthExternal());
                preferences.putInt(ID_HE, f.getHeightExternal());
            }
            if (!(f.isMaximizedInternal() || f.isMinimizedInternal()))
            {
                preferences.putInt(ID_WI, f.getWidthInternal());
                preferences.putInt(ID_HI, f.getHeightInternal());
            }
        }
        else if (jFrame != null)
        {
            if (!(ComponentUtil.isMaximized(jFrame) || ComponentUtil.isMinimized(jFrame)))
            {
                preferences.putInt(ID_WE, jFrame.getWidth());
                preferences.putInt(ID_HE, jFrame.getHeight());
            }
        }
        else if (component != null)
        {
            preferences.putInt(ID_WC, component.getWidth());
            preferences.putInt(ID_HC, component.getHeight());
        }
    }

    public void saveState(XMLPreferences preferences)
    {
        if (mainFrame != null)
        {
            final boolean b;

            if (mainFrame.isDetachedMode())
                b = mainFrame.getPreviousMaximized();
            else
                b = ComponentUtil.isMaximized(mainFrame);

            preferences.putBoolean(ID_MAXIMIZEDE, b);
        }
        if (icyFrame != null)
        {
            preferences.putBoolean(ID_EXTERNALIZED, icyFrame.isExternalized());
            preferences.putBoolean(ID_MAXIMIZEDI, icyFrame.isMaximizedInternal());
            preferences.putBoolean(ID_MAXIMIZEDE, icyFrame.isMaximizedExternal());
        }
        else if (extPanel != null)
        {
            preferences.putBoolean(ID_PANELIZED, extPanel.isInternalized());

            // get the panel frame
            final IcyFrame f = extPanel.getFrame();

            preferences.putBoolean(ID_EXTERNALIZED, f.isExternalized());
            preferences.putBoolean(ID_MAXIMIZEDI, f.isMaximizedInternal());
            preferences.putBoolean(ID_MAXIMIZEDE, f.isMaximizedExternal());
        }
        else if (jFrame != null)
        {
            preferences.putBoolean(ID_MAXIMIZEDE, ComponentUtil.isMaximized(jFrame));
        }
    }
}

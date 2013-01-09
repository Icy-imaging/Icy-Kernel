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
package icy.gui.frame;

import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.main.MainFrame;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * This class behave either as a JFrame or a JInternalFrame.<br>
 * IcyFrame should be 100% AWT safe
 * 
 * @author Fabrice de Chaumont & Stephane Dallongeville
 */
public class IcyFrame implements InternalFrameListener, WindowListener, ImageObserver, PropertyChangeListener
{
    private class SwitchStateAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4433831471426743128L;

        final IcyIcon detachIcon;
        final IcyIcon attachIcon;

        public SwitchStateAction()
        {
            super("");

            detachIcon = new IcyIcon(ResourceUtil.ICON_EXPAND, 20);
            attachIcon = new IcyIcon(ResourceUtil.ICON_COLLAPSE, 20);
            setAccelerator(KeyEvent.VK_F4);

            refreshState();
        }

        @Override
        public void doAction(ActionEvent e)
        {
            switchState();
        }

        void refreshState()
        {
            if (isInternalized())
            {
                setName("Detach");
                setIcon(detachIcon);
                setDescription("Externalize the window");
            }
            else
            {
                setName("Attach");
                setIcon(attachIcon);
                setDescription("Internalize the window");
            }
        }
    }

    /**
     * list containing all active frames
     */
    static ArrayList<IcyFrame> frames = new ArrayList<IcyFrame>();

    /**
     * Return all active (not closed) IcyFrame
     */
    public static ArrayList<IcyFrame> getAllFrames()
    {
        synchronized (frames)
        {
            return new ArrayList<IcyFrame>(frames);
        }
    }

    /**
     * Return all active IcyFrame which derive from the specified class
     */
    public static ArrayList<IcyFrame> getAllFrames(Class<?> frameClass)
    {
        final ArrayList<IcyFrame> result = new ArrayList<IcyFrame>();

        if (frameClass != null)
        {
            synchronized (frames)
            {
                for (IcyFrame frame : frames)
                    if (frameClass.isAssignableFrom(frame.getClass()))
                        result.add(frame);

            }
        }

        return result;
    }

    /**
     * Find IcyFrame corresponding to the specified JInternalFrame
     */
    public static IcyFrame findIcyFrame(JInternalFrame frame)
    {
        synchronized (frames)
        {
            for (IcyFrame f : frames)
                if (f.getInternalFrame() == frame)
                    return f;

            return null;
        }
    }

    public enum IcyFrameState
    {
        INTERNALIZED, EXTERNALIZED
    }

    IcyExternalFrame externalFrame;
    IcyInternalFrame internalFrame;

    /**
     * frame state (internal / external)
     */
    IcyFrameState state;

    /**
     * sync flag for AWT thread process
     */
    boolean syncProcess;

    /**
     * listeners
     */
    EventListenerList frameEventListeners;

    /**
     * internals
     */
    final MenuCallback defaultSystemMenuCallback;
    SwitchStateAction switchStateAction;
    boolean switchStateItemVisible;
    IcyFrameState previousState;

    public IcyFrame()
    {
        this("", false, true, false, false, false);
    }

    public IcyFrame(String title)
    {
        this(title, false, true, false, false, false);
    }

    public IcyFrame(String title, boolean resizable)
    {
        this(title, resizable, true, false, false, false);
    }

    public IcyFrame(String title, boolean resizable, boolean closable)
    {
        this(title, resizable, closable, false, false, false);
    }

    public IcyFrame(String title, boolean resizable, boolean closable, boolean maximizable)
    {
        this(title, resizable, closable, maximizable, false, false);
    }

    public IcyFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable,
            final boolean iconifiable)
    {
        this(title, resizable, closable, maximizable, iconifiable, false);
    }

    public IcyFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable,
            final boolean iconifiable, final boolean waitCreate)
    {
        super();

        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        // listen main frame mode change
        if (mainFrame != null)
            mainFrame.addPropertyChangeListener(MainFrame.PROPERTY_DETACHEDMODE, this);

        frameEventListeners = new EventListenerList();
        defaultSystemMenuCallback = new MenuCallback()
        {
            @Override
            public JMenu getMenu()
            {
                return getDefaultSystemMenu();
            }
        };

        syncProcess = false;

        // set default state
        if (canBeInternalized())
            state = IcyFrameState.INTERNALIZED;
        else
            state = IcyFrameState.EXTERNALIZED;

        switchStateItemVisible = true;
        // wanted default state
        previousState = IcyFrameState.INTERNALIZED;

        // create action after state has been set
        switchStateAction = new SwitchStateAction();
        switchStateAction.setEnabled(canBeInternalized());

        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame = createExternalFrame(title);
                // redirect frame / window events
                externalFrame.addWindowListener(IcyFrame.this);
                externalFrame.setLocationRelativeTo(null);
                externalFrame.setResizable(resizable);
                externalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                // default size
                externalFrame.setSize(480, 400);

                internalFrame = createInternalFrame(title, resizable, closable, maximizable, iconifiable);
                // redirect frame / window events
                internalFrame.addInternalFrameListener(IcyFrame.this);
                // default size
                internalFrame.setSize(480, 400);

                // default system menu callback
                externalFrame.setSystemMenuCallback(defaultSystemMenuCallback);
                internalFrame.setSystemMenuCallback(defaultSystemMenuCallback);

                // register to the list
                synchronized (frames)
                {
                    frames.add(IcyFrame.this);
                }
            }
        }, waitCreate);
    }

    /**
     * Permit IcyExternalFrame overriding
     */
    protected IcyExternalFrame createExternalFrame(String title)
    {
        return new IcyExternalFrame(title);
    }

    /**
     * Permit IcyInternalFrame overriding
     */
    protected IcyInternalFrame createInternalFrame(String title, boolean resizable, boolean closable,
            boolean maximizable, boolean iconifiable)
    {
        return new IcyInternalFrame(title, resizable, closable, maximizable, iconifiable);
    }

    /**
     * Return true if the frame can be internalized
     */
    protected boolean canBeInternalized()
    {
        final MainFrame frame = Icy.getMainInterface().getMainFrame();

        // internalization possible only in single window mode
        if (frame != null)
            return !frame.isDetachedMode();

        return false;
    }

    /**
     * Refresh system menu
     */
    public void updateSystemMenu()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.updateSystemMenu();
                else
                    externalFrame.updateSystemMenu();
            }
        }, syncProcess);
    }

    /**
     * Close frame (send closing event)
     */
    public void close()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.close(true);
                externalFrame.close();
            }
        }, syncProcess);
    }

    /**
     * Dispose frame (send closed event)
     */
    public void dispose()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.dispose();
                else
                    externalFrame.dispose();
            }
        }, syncProcess);
    }

    /** go from detached to attached and opposite */
    public void switchState()
    {
        if (isInternalized())
            detach();
        else
            attach();
    }

    /** set the frame to be an inner frame on the desktop pane */
    public void internalize()
    {
        if (isExternalized())
            attach();
    }

    /** the frame becomes detached in an independent frame */
    public void externalize()
    {
        if (isInternalized())
            detach();
    }

    /** Set the frame to be an inner frame on the desktop pane */
    public void attach()
    {
        if (isInternalized() || !canBeInternalized())
            return;

        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                // save current visible state
                final boolean visible = externalFrame.isVisible();

                // hide external frame
                if (visible)
                    externalFrame.setVisible(false);

                final JMenuBar menuBar = externalFrame.getJMenuBar();
                final Container content = externalFrame.getContentPane();

                // remove components from external frame
                externalFrame.setJMenuBar(null);
                externalFrame.setContentPane(new JPanel());
                externalFrame.validate();

                internalFrame.setJMenuBar(menuBar);
                internalFrame.setContentPane(content);
                internalFrame.validate();

                // show internal frame
                if (visible)
                {
                    internalFrame.setVisible(true);
                    try
                    {
                        internalFrame.setSelected(true);
                    }
                    catch (PropertyVetoException e)
                    {
                        // ignore
                    }
                }

                state = IcyFrameState.INTERNALIZED;

                // notify state change
                stateChanged();
            }
        }, syncProcess);
    }

    /** Set the frame to be detached in an independent frame */
    public void detach()
    {
        if (isExternalized())
            return;

        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                // save current visible state
                final boolean visible = internalFrame.isVisible();

                // hide internal frame
                if (visible)
                    internalFrame.setVisible(false);

                final JMenuBar menuBar = internalFrame.getJMenuBar();
                final Container content = internalFrame.getContentPane();

                // remove components from internal frame
                internalFrame.setJMenuBar(null);
                internalFrame.setContentPane(new JPanel());
                internalFrame.validate();

                externalFrame.setJMenuBar(menuBar);
                externalFrame.setContentPane(content);
                externalFrame.validate();

                // show external frame
                if (visible)
                {
                    externalFrame.setVisible(true);
                    externalFrame.requestFocus();
                }

                // TODO : we have to force a refresh with resizing or we get a refresh bug on
                // scrollbar (OSX only ?)
                // externalFrame.setSize(externalFrame.getWidth(), externalFrame.getHeight() - 1);
                // externalFrame.setSize(externalFrame.getWidth(), externalFrame.getHeight() + 1);

                state = IcyFrameState.EXTERNALIZED;

                // notify state change
                stateChanged();
            }
        }, syncProcess);
    }

    /**
     * Called on state (internalized / externalized) change
     */
    public void stateChanged()
    {
        // refresh switch action state
        switchStateAction.refreshState();

        // refresh system menu
        updateSystemMenu();

        // fire event
        if (isInternalized())
            fireFrameInternalized(new IcyFrameEvent(IcyFrame.this, null, null));
        else
            fireFrameExternalized(new IcyFrameEvent(IcyFrame.this, null, null));
    }

    /** Center frame on the desktop */
    public void center()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    ComponentUtil.center(internalFrame);
                else
                    ComponentUtil.center(externalFrame);

            }
        }, syncProcess);
    }

    /**
     * Add to the container c
     */
    public void addTo(final Container c)
    {
        if (isInternalized())
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    c.add(internalFrame);
                }
            }, syncProcess);
        }
    }

    /**
     * Add to the container c
     */
    public void addTo(final Container c, final int index)
    {
        if (isInternalized())
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    c.add(internalFrame, index);
                }
            }, syncProcess);
        }
    }

    /**
     * Add to the container c
     */
    public void addTo(final Container c, final Object constraints)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                c.add(internalFrame, constraints);
            }
        }, syncProcess);
    }

    /**
     * Add the frame to the main pane of ICY
     */
    public void addToMainDesktopPane()
    {
        final JDesktopPane desktop = Icy.getMainInterface().getDesktopPane();

        if (desktop != null)
            addTo(desktop, JLayeredPane.DEFAULT_LAYER);
    }

    /**
     * Implement add method
     */
    public void add(final Component comp)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.add(comp);
                else
                    externalFrame.add(comp);
            }
        }, syncProcess);
    }

    /**
     * Implement add method
     */
    public void add(final Component comp, final Object constraints)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.add(comp, constraints);
                else
                    externalFrame.add(comp, constraints);
            }
        }, syncProcess);
    }

    /**
     * Implement add method
     */
    public void add(final String name, final Component comp)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.add(name, comp);
                else
                    externalFrame.add(name, comp);
            }
        }, syncProcess);
    }

    /**
     * Remove from the container
     */
    public void removeFrom(final Container c)
    {
        if (isInternalized())
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    c.remove(internalFrame);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement removeAll method
     */
    public void removeAll()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.removeAll();
                else
                    externalFrame.removeAll();
            }
        }, syncProcess);
    }

    /**
     * Implement remove method
     */
    public void remove(final Component comp)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.remove(comp);
                else
                    externalFrame.remove(comp);
            }
        }, syncProcess);
    }

    /**
     * Remove the frame from the main pane of ICY
     */
    public void removeFromMainDesktopPane()
    {
        removeFrom(Icy.getMainInterface().getDesktopPane());
    }

    /**
     * Implement toFront method
     */
    public void toFront()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.toFront();
                else
                    externalFrame.toFront();
            }
        }, syncProcess);
    }

    /**
     * Implement toBack method
     */
    public void toBack()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.toBack();
                else
                    externalFrame.toBack();
            }
        }, syncProcess);
    }

    /**
     * Implement pack method
     */
    public void pack()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.pack();
                else
                    externalFrame.pack();
            }
        }, syncProcess);
    }

    public Container getFrame()
    {
        if (isInternalized())
            return internalFrame;

        return externalFrame;
    }

    public JInternalFrame getInternalFrame()
    {
        return internalFrame;
    }

    public JFrame getExternalFrame()
    {
        return externalFrame;
    }

    /**
     * Indicate if system menu show display item to switch frame state (internal / external)
     */
    public void setSwitchStateItemVisible(boolean value)
    {
        if (switchStateItemVisible != value)
        {
            switchStateItemVisible = value;
            switchStateAction.setEnabled(value);
            updateSystemMenu();
        }
    }

    /**
     * @return the systemMenuCallback
     */
    public MenuCallback getSystemMenuCallback()
    {
        // always have same callback on each frame
        final MenuCallback result = internalFrame.getSystemMenuCallback();

        // default callback ? this means we set it to null
        if (result == defaultSystemMenuCallback)
            return null;

        return result;
    }

    /**
     * Set the system menu callback (this allow modification of system menu)
     * 
     * @param value
     *        the systemMenuCallback to set
     */
    public void setSystemMenuCallback(MenuCallback value)
    {
        if (value != null)
        {
            internalFrame.setSystemMenuCallback(value);
            externalFrame.setSystemMenuCallback(value);
        }
        else
        {
            internalFrame.setSystemMenuCallback(defaultSystemMenuCallback);
            externalFrame.setSystemMenuCallback(defaultSystemMenuCallback);
        }
    }

    /**
     * Return the default system menu
     */
    public JMenu getDefaultSystemMenu()
    {
        final JMenu result;

        if (isInternalized())
            result = internalFrame.getDefaultSystemMenu();
        else
            result = externalFrame.getDefaultSystemMenu();

        if (switchStateItemVisible)
        {
            result.insert(switchStateAction, 0);
            if (result.getMenuComponentCount() > 1)
                result.insertSeparator(1);
        }

        return result;
    }

    /**
     * Implement getParent
     */
    public Container getParent()
    {
        if (isInternalized())
            return internalFrame.getParent();

        return externalFrame.getParent();
    }

    /**
     * Implement getContentPane method
     */
    public Container getContentPane()
    {
        if (isInternalized())
            return internalFrame.getContentPane();

        return externalFrame.getContentPane();
    }

    /**
     * Implement getRootPane method
     */
    public JRootPane getRootPane()
    {
        if (isInternalized())
            return internalFrame.getRootPane();

        return externalFrame.getRootPane();
    }

    /**
     * @return the switchStateAction
     */
    public SwitchStateAction getSwitchStateAction()
    {
        return switchStateAction;
    }

    /**
     * Implement getMinimumSize method
     */
    public Dimension getMinimumSize()
    {
        if (isInternalized())
            return internalFrame.getMinimumSize();

        return externalFrame.getMinimumSize();
    }

    /**
     * Implement getMinimumSize method for internal frame only
     */
    public Dimension getMinimumSizeInternal()
    {
        return internalFrame.getMinimumSize();
    }

    /**
     * Implement getMinimumSize method for external frame only
     */
    public Dimension getMinimumSizeExternal()
    {
        return externalFrame.getMinimumSize();
    }

    /**
     * Implement getMaximumSize method
     */
    public Dimension getMaximumSize()
    {
        if (isInternalized())
            return internalFrame.getMaximumSize();

        return externalFrame.getMaximumSize();
    }

    /**
     * Implement getMaximumSize method for internal frame only
     */
    public Dimension getMaximumSizeInternal()
    {
        return internalFrame.getMaximumSize();
    }

    /**
     * Implement getMaximumSize method for external frame only
     */
    public Dimension getMaximumSizeExternal()
    {
        return externalFrame.getMaximumSize();
    }

    /**
     * Implement getPreferredSize method
     */
    public Dimension getPreferredSize()
    {
        if (isInternalized())
            return internalFrame.getPreferredSize();

        return externalFrame.getPreferredSize();
    }

    /**
     * Implement getPreferredSize method for internal frame only
     */
    public Dimension getPreferredSizeInternal()
    {
        return internalFrame.getPreferredSize();
    }

    /**
     * Implement getPreferredSize method for external frame only
     */
    public Dimension getPreferredSizeExternal()
    {
        return externalFrame.getPreferredSize();
    }

    /**
     * Implement getSize method
     */
    public Dimension getSize()
    {
        if (isInternalized())
            return internalFrame.getSize();

        return externalFrame.getSize();
    }

    /**
     * Implement getSize method for internal frame only
     */
    public Dimension getSizeInternal()
    {
        return internalFrame.getSize();
    }

    /**
     * Implement getSize method for external frame only
     */
    public Dimension getSizeExternal()
    {
        return externalFrame.getSize();
    }

    /**
     * Implement getHeight method
     */
    public int getHeight()
    {
        if (isInternalized())
            return internalFrame.getHeight();

        return externalFrame.getHeight();
    }

    /**
     * Implement getHeight method for internal frame only
     */
    public int getHeightInternal()
    {
        return internalFrame.getHeight();
    }

    /**
     * Implement getHeight method for external frame only
     */
    public int getHeightExternal()
    {
        return externalFrame.getHeight();
    }

    /**
     * Implement getWidth method
     */
    public int getWidth()
    {
        if (isInternalized())
            return internalFrame.getWidth();

        return externalFrame.getWidth();
    }

    /**
     * Implement getWidth method for internal frame only
     */
    public int getWidthInternal()
    {
        return internalFrame.getWidth();
    }

    /**
     * Implement getWidth method for external frame only
     */
    public int getWidthExternal()
    {
        return externalFrame.getWidth();
    }

    /**
     * Implement getX method
     */
    public int getX()
    {
        if (isInternalized())
            return internalFrame.getX();

        return externalFrame.getX();
    }

    /**
     * Implement getX method for internal frame only
     */
    public int getXInternal()
    {
        return internalFrame.getX();
    }

    /**
     * Implement getX method for external frame only
     */
    public int getXExternal()
    {
        return externalFrame.getX();
    }

    /**
     * Implement getY method
     */
    public int getY()
    {
        if (isInternalized())
            return internalFrame.getY();

        return externalFrame.getY();
    }

    /**
     * Implement getY method for internal frame only
     */
    public int getYInternal()
    {
        return internalFrame.getY();
    }

    /**
     * Implement getY method for external frame only
     */
    public int getYExternal()
    {
        return externalFrame.getY();
    }

    /**
     * Implement getLocation method
     */
    public Point getLocation()
    {
        if (isInternalized())
            return internalFrame.getLocation();

        return externalFrame.getLocation();
    }

    /**
     * Implement getLocation method
     */
    public Point getLocationInternal()
    {
        return internalFrame.getLocation();
    }

    /**
     * Implement getLocation method for external frame only
     */
    public Point getLocationExternal()
    {
        return externalFrame.getLocation();
    }

    /**
     * Implement getBounds method
     */
    public Rectangle getBounds()
    {
        if (isInternalized())
            return internalFrame.getBounds();

        return externalFrame.getBounds();
    }

    /**
     * Implement getBounds method for internal frame only
     */
    public Rectangle getBoundsInternal()
    {
        return internalFrame.getBounds();
    }

    /**
     * Implement getBounds method for external frame only
     */
    public Rectangle getBoundsExternal()
    {
        return externalFrame.getBounds();
    }

    /**
     * Implement getBounds method for external frame only
     */
    public Rectangle getVisibleRect()
    {
        if (isInternalized())
            return internalFrame.getVisibleRect();

        // not supported on external frame
        if (externalFrame.isVisible())
            return externalFrame.getBounds();

        return new Rectangle();
    }

    /**
     * Implement getJMenuBar method
     */
    public JMenuBar getJMenuBar()
    {
        if (isInternalized())
            return internalFrame.getJMenuBar();

        return externalFrame.getJMenuBar();
    }

    /**
     * Implement getToolkit method
     */
    public Toolkit getToolkit()
    {
        if (isInternalized())
            return internalFrame.getToolkit();

        return externalFrame.getToolkit();
    }

    /**
     * Implement setTitle method
     */
    public String getTitle()
    {
        if (isInternalized())
            return internalFrame.getTitle();

        return externalFrame.getTitle();
    }

    /**
     * Return true if title bar is visible
     */
    public boolean getTitleBarVisible()
    {
        if (isInternalized())
            return internalFrame.isTitleBarVisible();

        return externalFrame.isTitleBarVisible();
    }

    /**
     * @return the displaySwitchStateItem
     */
    public boolean isSwitchStateItemVisible()
    {
        return switchStateItemVisible;
    }

    /**
     * Implement getMousePosition method
     */
    public Point getMousePosition()
    {
        if (isInternalized())
            return internalFrame.getMousePosition();

        return externalFrame.getMousePosition();
    }

    /**
     * Implement isMinimized method
     */
    public boolean isMinimized()
    {
        if (isInternalized())
            return internalFrame.isIcon();

        return ComponentUtil.isMinimized(externalFrame);
    }

    /**
     * Implement isMinimized method for internal frame only
     */
    public boolean isMinimizedInternal()
    {
        return internalFrame.isIcon();
    }

    /**
     * Implement isMinimized method for external frame only
     */
    public boolean isMinimizedExternal()
    {
        return ComponentUtil.isMinimized(externalFrame);
    }

    /**
     * Implement isMaximized method
     */
    public boolean isMaximized()
    {
        if (isInternalized())
            return internalFrame.isMaximum();

        return ComponentUtil.isMaximized(externalFrame);
    }

    /**
     * Implement isMaximized method for internal frame only
     */
    public boolean isMaximizedInternal()
    {
        return internalFrame.isMaximum();
    }

    /**
     * Implement isMaximized method for external frame only
     */
    public boolean isMaximizedExternal()
    {
        return ComponentUtil.isMaximized(externalFrame);
    }

    /**
     * Implement isVisible method
     */
    public boolean isVisible()
    {
        if (isInternalized())
            return internalFrame.isVisible();

        return externalFrame.isVisible();
    }

    /**
     * Implement isResizable method
     */
    public boolean isResizable()
    {
        if (isInternalized())
            return internalFrame.isResizable();

        return externalFrame.isResizable();
    }

    /**
     * Implement isClosable method
     */
    public boolean isClosable()
    {
        if (isInternalized())
            return internalFrame.isClosable();

        // external frame is always closable
        return true;
    }

    /**
     * return true if frame is in internalized state
     */
    public boolean isInternalized()
    {
        return (state == IcyFrameState.INTERNALIZED);
    }

    /**
     * return true if frame is in externalized state
     */
    public boolean isExternalized()
    {
        return (state == IcyFrameState.EXTERNALIZED);
    }

    /**
     * return true if frame is active
     */
    public boolean isActive()
    {
        if (isInternalized())
            return internalFrame.isSelected();

        return externalFrame.isActive();
    }

    /**
     * Implement isAlwaysOnTop method (only for externalized frame)
     */
    public boolean isAlwaysOnTop()
    {
        return externalFrame.isAlwaysOnTop();
    }

    /**
     * Implement hasFocus method
     */
    public boolean hasFocus()
    {
        if (isInternalized())
            return internalFrame.hasFocus();

        return externalFrame.hasFocus();
    }

    /**
     * Implement setTitle method
     */
    public void setTitle(final String title)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setTitle(title);
                externalFrame.setTitle(title);
            }
        }, syncProcess);
    }

    /**
     * Implement setToolTipText method (only for internalized frame)
     */
    public void setToolTipText(final String text)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                // only internal frame support it
                internalFrame.setToolTipText(text);
                // externalFrame.setToolTipText(text);
            }
        }, syncProcess);
    }

    /**
     * Implement setBackground method
     */
    public void setBackground(final Color value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setBackground(value);
                externalFrame.setBackground(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setForeground method
     */
    public void setForeground(final Color value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setForeground(value);
                externalFrame.setForeground(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setResizable method
     */
    public void setResizable(final boolean value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setResizable(value);
                externalFrame.setResizable(value);
            }
        }, syncProcess);

    }

    /**
     * Implement setLocation method
     */
    public void setLocation(final Point p)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setLocation(p);
                else
                    externalFrame.setLocation(p);
            }
        }, syncProcess);
    }

    /**
     * Implement setLocation method
     */
    public void setLocation(final int x, final int y)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setLocation(x, y);
                else
                    externalFrame.setLocation(x, y);
            }
        }, syncProcess);
    }

    /**
     * Implement setLocation method for internal frame only
     */
    public void setLocationInternal(final Point p)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setLocation(p);
            }
        }, syncProcess);
    }

    /**
     * Implement setLocation method for internal frame only
     */
    public void setLocationInternal(final int x, final int y)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setLocation(x, y);
            }
        }, syncProcess);
    }

    /**
     * Implement setLocation method for external frame only
     */
    public void setLocationExternal(final Point p)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setLocation(p);
            }
        }, syncProcess);
    }

    /**
     * Implement setLocation method for external frame only
     */
    public void setLocationExternal(final int x, final int y)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setLocation(x, y);
            }
        }, syncProcess);
    }

    /**
     * Implement setSize method
     */
    public void setSize(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setSize(d);
                else
                    externalFrame.setSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setSize method
     */
    public void setSize(final int width, final int height)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setSize(width, height);
                else
                    externalFrame.setSize(width, height);
            }
        }, syncProcess);
    }

    /**
     * Implement setSize method for internal frame only
     */
    public void setSizeInternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setSize method for internal frame only
     */
    public void setSizeInternal(final int width, final int height)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setSize(width, height);
            }
        }, syncProcess);
    }

    /**
     * Implement setSize method for external frame only
     */
    public void setSizeExternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setSize method for external frame only
     */
    public void setSizeExternal(final int width, final int height)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setSize(width, height);
            }
        }, syncProcess);
    }

    /**
     * Implement setPreferredSize method
     */
    public void setPreferredSize(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setPreferredSize(d);
                else
                    externalFrame.setPreferredSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setPreferredSize method for internal frame only
     */
    public void setPreferredSizeInternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setPreferredSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setPreferredSize method for external frame only
     */
    public void setPreferredSizeExternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setPreferredSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setMinimumSize method
     */
    public void setMinimumSize(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setMinimumSize(d);
                else
                    externalFrame.setMinimumSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setMaximumSize method
     */
    public void setMaximumSize(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setMaximumSize(d);
                else
                    externalFrame.setMaximumSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setMinimumSize method for internal frame only
     */
    public void setMinimumSizeInternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setMinimumSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setMaximumSize method for internal frame only
     */
    public void setMaximumSizeInternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setMaximumSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setMinimumSize method for external frame only
     */
    public void setMinimumSizeExternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setMinimumSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setMaximumSize method for external frame only
     */
    public void setMaximumSizeExternal(final Dimension d)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setMaximumSize(d);
            }
        }, syncProcess);
    }

    /**
     * Implement setBounds method
     */
    public void setBounds(final Rectangle r)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setBounds(r);
                else
                    externalFrame.setBounds(r);
            }
        }, syncProcess);

    }

    /**
     * Implement setMaximisable method
     */
    public void setMaximisable(final boolean value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                // only for internal frame
                internalFrame.setMaximizable(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setMinimized method
     */
    public void setMinimized(final boolean value)
    {
        // only relevant if state changed
        if (isMinimized() ^ value)
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    if (isInternalized())
                        internalFrame.setMinimized(value);
                    else
                        externalFrame.setMinimized(value);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement setMinimized method for internal frame only
     */
    public void setMinimizedInternal(final boolean value)
    {
        // only relevant if state changed
        if (internalFrame.isMinimized() ^ value)
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    internalFrame.setMinimized(value);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement setMinimized method for external frame only
     */
    public void setMinimizedExternal(final boolean value)
    {
        // only relevant if state changed
        if (externalFrame.isMinimized() ^ value)
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    externalFrame.setMinimized(value);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement setMaximized method
     */
    public void setMaximized(final boolean value)
    {
        // only relevant if state changed
        if (isMaximized() ^ value)
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    if (isInternalized())
                        internalFrame.setMaximized(value);
                    else
                        externalFrame.setMaximized(value);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement setMaximized method for internal frame only
     */
    public void setMaximizedInternal(final boolean value)
    {
        // only relevant if state changed
        if (internalFrame.isMaximized() ^ value)
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    internalFrame.setMaximized(value);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement setMaximized method for external frame only
     */
    public void setMaximizedExternal(final boolean value)
    {
        // only relevant if state changed
        if (externalFrame.isMaximized() ^ value)
        {
            // AWT safe
            ThreadUtil.invoke(new Runnable()
            {
                @Override
                public void run()
                {
                    externalFrame.setMaximized(value);
                }
            }, syncProcess);
        }
    }

    /**
     * Implement setClosable method
     */
    public void setClosable(final boolean value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setClosable(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setDefaultCloseOperation method
     */
    public void setDefaultCloseOperation(final int operation)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setDefaultCloseOperation(operation);
                externalFrame.setDefaultCloseOperation(operation);
            }
        }, syncProcess);
    }

    /**
     * Implement setFocusable method
     */
    public void setFocusable(final boolean value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setFocusable(value);
                externalFrame.setFocusable(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setVisible method
     */
    public void setVisible(final boolean value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setVisible(value);
                else
                    externalFrame.setVisible(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setAlwaysOnTop method (only for externalized frame)
     */
    public void setAlwaysOnTop(final boolean alwaysOnTop)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                externalFrame.setAlwaysOnTop(alwaysOnTop);
            }
        }, syncProcess);
    }

    /**
     * Implement setJMenuBar method
     */
    public void setJMenuBar(final JMenuBar m)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setJMenuBar(m);
                else
                    externalFrame.setJMenuBar(m);
            }
        }, syncProcess);
    }

    /**
     * Hide or show the title bar (frame should not be displayable when you set this property)
     */
    public void setTitleBarVisible(final boolean value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setTitleBarVisible(value);
                externalFrame.setTitleBarVisible(value);
            }
        }, syncProcess);
    }

    /**
     * Implement setLayout method
     */
    public void setLayout(final LayoutManager layout)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setLayout(layout);
                else
                    externalFrame.setLayout(layout);
            }
        }, syncProcess);
    }

    /**
     * Implement setBorder method (only for internal frame)
     */
    public void setBorder(final Border border)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.setBorder(border);
            }
        }, syncProcess);
    }

    /**
     * Implement setContentPane method
     */
    public void setContentPane(final Container value)
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.setContentPane(value);
                else
                    externalFrame.setContentPane(value);
            }
        }, syncProcess);
    }

    /**
     * @return the syncProcess
     */
    public boolean isSyncProcess()
    {
        return syncProcess;
    }

    /**
     * By default IcyFrame does asych processing, you can force sync processing<br>
     * with this property
     * 
     * @param syncProcess
     *        the syncProcess to set
     */
    public void setSyncProcess(boolean syncProcess)
    {
        this.syncProcess = syncProcess;
    }

    /**
     * Frame becomes the active/focused frame
     */
    public void requestFocus()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                {
                    try
                    {
                        internalFrame.setSelected(true);
                    }
                    catch (PropertyVetoException e)
                    {
                        // ignore
                    }
                }
                else
                    externalFrame.requestFocus();
            }
        }, syncProcess);
    }

    /**
     * Implement validate
     */
    public void validate()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.validate();
                else
                    externalFrame.validate();
            }
        }, syncProcess);
    }

    /**
     * Implement revalidate
     */
    public void revalidate()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.revalidate();
                else
                {
                    externalFrame.invalidate();
                    externalFrame.repaint();
                }
            }
        }, syncProcess);
    }

    /**
     * Implement repaint
     */
    public void repaint()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                if (isInternalized())
                    internalFrame.repaint();
                else
                    externalFrame.repaint();
            }
        }, syncProcess);
    }

    /**
     * Implement updateUI
     */
    public void updateUI()
    {
        // AWT safe
        ThreadUtil.invoke(new Runnable()
        {
            @Override
            public void run()
            {
                internalFrame.updateUI();
            }
        }, syncProcess);
    }

    /**
     * Fire frame activated event
     */
    private void fireFrameActivated(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameActivated(e);
    }

    /**
     * Fire frame deactivated event
     */
    private void fireFrameDeactivated(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameDeactivated(e);
    }

    /**
     * Fire frame closing event
     */
    private void fireFrameClosing(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameClosing(e);
    }

    /**
     * Fire frame closed event
     */
    private void fireFrameClosed(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameClosed(e);
    }

    /**
     * Fire frame iconified event
     */
    private void fireFrameIconified(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameIconified(e);
    }

    /**
     * Fire frame deiconified event
     */
    private void fireFrameDeiconified(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameDeiconified(e);
    }

    /**
     * Fire frame opened event
     */
    private void fireFrameOpened(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameOpened(e);
    }

    /**
     * Fire frame internalized event
     */
    void fireFrameInternalized(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameInternalized(e);
    }

    /**
     * Fire frame externalized event
     */
    void fireFrameExternalized(IcyFrameEvent e)
    {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameExternalized(e);
    }

    /**
     * Implement addFrameListener method
     */
    public void addFrameListener(IcyFrameListener l)
    {
        frameEventListeners.add(IcyFrameListener.class, l);
    }

    /**
     * Implement removeFrameListener method
     */
    public void removeFrameListener(IcyFrameListener l)
    {
        frameEventListeners.remove(IcyFrameListener.class, l);
    }

    /**
     * Implement addComponentListener method
     */
    public void addComponentListener(ComponentListener l)
    {
        internalFrame.addComponentListener(l);
        externalFrame.addComponentListener(l);
    }

    /**
     * Implement removeComponentListener method
     */
    public void removeComponentListener(ComponentListener l)
    {
        internalFrame.removeComponentListener(l);
        externalFrame.removeComponentListener(l);
    }

    /**
     * Implement addKeyListener method
     */
    public void addKeyListener(KeyListener l)
    {
        internalFrame.addKeyListener(l);
        externalFrame.addKeyListener(l);
    }

    /**
     * Implement addKeyListener method
     */
    public void removeKeyListener(KeyListener l)
    {
        internalFrame.removeKeyListener(l);
        externalFrame.removeKeyListener(l);
    }

    /**
     * internal close stuff
     */
    public void frameClosed(AWTEvent e)
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        // remove listener on main frame mode change
        if (mainFrame != null)
            mainFrame.removePropertyChangeListener(MainFrame.PROPERTY_DETACHEDMODE, this);
        // remove others listeners
        externalFrame.removeWindowListener(IcyFrame.this);
        internalFrame.removeInternalFrameListener(IcyFrame.this);

        if (e instanceof InternalFrameEvent)
        {
            fireFrameClosed(new IcyFrameEvent(this, (InternalFrameEvent) e, null));
            // don't forget to close external frame
            externalFrame.dispose();
        }
        else if (e instanceof WindowEvent)
        {
            fireFrameClosed(new IcyFrameEvent(this, null, (WindowEvent) e));
            // don't forget to close internal frame
            internalFrame.dispose();
        }

        // easy onClosed handling
        onClosed();
    }

    /**
     * easy onClosed job
     */
    public void onClosed()
    {
        // unregister from list
        synchronized (frames)
        {
            frames.remove(this);
        }

        // release some stuff else we have cycling reference
        externalFrame.systemMenuCallback = null;
        internalFrame.systemMenuCallback = null;
        switchStateAction = null;
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e)
    {
        fireFrameActivated(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e)
    {
        frameClosed(e);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e)
    {
        fireFrameClosing(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e)
    {
        fireFrameDeactivated(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e)
    {
        fireFrameDeiconified(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e)
    {
        fireFrameIconified(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e)
    {
        fireFrameOpened(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
        fireFrameActivated(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
        frameClosed(e);
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        fireFrameClosing(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
        fireFrameDeactivated(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
        fireFrameDeiconified(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
        fireFrameIconified(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
        fireFrameOpened(new IcyFrameEvent(this, null, e));
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
    {
        if (isInternalized())
            return internalFrame.imageUpdate(img, infoflags, x, y, width, height);

        return externalFrame.imageUpdate(img, infoflags, x, y, width, height);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (StringUtil.equals(evt.getPropertyName(), MainFrame.PROPERTY_DETACHEDMODE))
        {
            // window mode has been changed
            final boolean detachedMode = ((Boolean) evt.getNewValue()).booleanValue();

            // detached mode set --> externalize
            if (detachedMode)
            {
                // save previous state
                previousState = state;
                externalize();
                // disable switch state item
                if (switchStateAction != null)
                    switchStateAction.setEnabled(false);
            }
            else
            {
                // restore previous state
                if (previousState == IcyFrameState.INTERNALIZED)
                    internalize();
                // enable switch state item
                if (switchStateAction != null)
                    switchStateAction.setEnabled(true);
            }
        }
    }
}

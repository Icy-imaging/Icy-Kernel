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
package icy.gui.component;

import icy.common.listener.weak.WeakListener;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.util.WindowPositionSaver;
import icy.main.Icy;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.util.EventListener;

import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Externalizable panel component.<br>
 * Basically this is a JPanel you can externalize and display in an IcyFrame.<br>
 * 
 * @author Stephane
 */
public class ExternalizablePanel extends JPanel
{
    public static class WeakStateListener extends WeakListener<StateListener> implements StateListener
    {
        public WeakStateListener(StateListener listener)
        {
            super(listener);
        }

        @Override
        public void removeListener(Object source)
        {
            if (source != null)
                ((ExternalizablePanel) source).removeStateListener(this);
        }

        @Override
        public void stateChanged(ExternalizablePanel source, boolean externalized)
        {
            final StateListener listener = getListener(source);

            if (listener != null)
                listener.stateChanged(source, externalized);
        }
    }

    public static interface StateListener extends EventListener
    {
        public void stateChanged(ExternalizablePanel source, boolean externalized);
    }

    public class Frame extends IcyFrame
    {
        public Frame(String title) throws HeadlessException
        {
            super(title, true, true, true, true);

            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            addFrameListener(new IcyFrameAdapter()
            {
                @Override
                public void icyFrameClosing(IcyFrameEvent e)
                {
                    super.icyFrameClosing(e);

                    // ignore the event when frame is manually closed or application is exiting
                    if (!(closed || Icy.isExiting()))
                    {
                        if (ExternalizablePanel.this.isExternalized())
                            ExternalizablePanel.this.internalizeInternal();
                    }
                }
            });

            setLayout(new BorderLayout());
            setSize(400, 400);
            addToMainDesktopPane();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -7690543443681714719L;

    /**
     * extern frame
     */
    protected final Frame frame;
    /**
     * parent component
     */
    private Container parent;

    /**
     * internals
     */
    private boolean internalizationAutorized;
    private boolean externalizationAutorized;
    boolean closed;

    // we need to keep reference on it as the object only use weak reference
    final WindowPositionSaver positionSaver;

    /**
     * Create a new externalizable panel.
     * 
     * @param title
     *        title for the associated frame.
     * @param key
     *        save key, used for WindowPositionSaver.<br>
     *        Set to null or empty string disable parameter saving.
     * @param defLoc
     *        the default location for the frame (externalized state)
     * @param defDim
     *        the default dimension for the frame (externalized state)
     */
    public ExternalizablePanel(String title, String key, Point defLoc, Dimension defDim)
    {
        super();

        frame = new Frame(title);
        parent = null;
        internalizationAutorized = true;
        externalizationAutorized = true;
        closed = false;

        // use window position saver with default parameters
        if (!StringUtil.isEmpty(key))
            positionSaver = new WindowPositionSaver(this, "frame/" + key, defLoc, defDim);
        else
            positionSaver = null;
    }

    /**
     * Create a new externalizable panel.
     * 
     * @param title
     *        title for the associated frame.
     * @param key
     *        save key, used for WindowPositionSaver.<br>
     *        Set to null or empty string disable parameter saving.
     */
    public ExternalizablePanel(String title, String key)
    {
        // default location and dimension for extern frame
        this(title, key, new Point(200, 200), new Dimension(400, 300));
    }

    public ExternalizablePanel(String title)
    {
        this(title, null);
    }

    public ExternalizablePanel()
    {
        this("", null);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        // set parent on first attachment
        if (parent == null)
        {
            final Container p = getParent();

            if ((p != frame.getInternalFrame().getContentPane()) && (p != frame.getExternalFrame().getContentPane()))
                parent = p;
        }
    }

    /**
     * Close the panel (close and release associated frames and resources).
     */
    public void close()
    {
        closed = true;
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.close();
    }

    /**
     * Manual parent set
     */
    public void setParent(Container value)
    {
        parent = value;
    }

    /**
     * @return the internalizationAutorized
     */
    public boolean isInternalizationAutorized()
    {
        return internalizationAutorized;
    }

    /**
     * @param internalizationAutorized
     *        the internalizationAutorized to set
     */
    public void setInternalizationAutorized(boolean internalizationAutorized)
    {
        this.internalizationAutorized = internalizationAutorized;
    }

    /**
     * @return the externalizationAutorized
     */
    public boolean isExternalizationAutorized()
    {
        return externalizationAutorized;
    }

    /**
     * @param externalizationAutorized
     *        the externalizationAutorized to set
     */
    public void setExternalizationAutorized(boolean externalizationAutorized)
    {
        this.externalizationAutorized = externalizationAutorized;
    }

    /**
     * Externalize panel in an independent frame
     */
    public void externalize()
    {
        if (isInternalized())
            externalizeInternal();
    }

    /**
     * Internalize panel (remove from independent frame)
     */
    public void internalize()
    {
        if (isExternalized())
            internalizeInternal();
    }

    /**
     * Externalize panel (internal method)
     */
    void externalizeInternal()
    {
        if (!externalizationAutorized)
            return;

        // externalize
        if (parent != null)
        {
            parent.remove(this);
            parent.validate();
        }

        frame.add(this, BorderLayout.CENTER);
        frame.validate();
        frame.setVisible(true);

        // notify
        fireStateChange(true);
    }

    /**
     * Internalize panel (internal method)
     */
    void internalizeInternal()
    {
        if (!internalizationAutorized)
            return;

        // internalize
        frame.setVisible(false);
        frame.remove(this);
        frame.validate();

        if (parent != null)
        {
            parent.add(this);
            parent.validate();
        }

        // notify
        fireStateChange(false);
    }

    /**
     * Switch from internalized <--> externalized state and vice versa
     */
    public void switchState()
    {
        if (isExternalized())
            internalizeInternal();
        else
            externalizeInternal();
    }

    public boolean isInternalized()
    {
        return !frame.isVisible();
    }

    public boolean isExternalized()
    {
        return frame.isVisible();
    }

    /**
     * @return the frame
     */
    public IcyFrame getFrame()
    {
        return frame;
    }

    // /**
    // * Implement getMinimumSize method for external frame only
    // */
    // public Dimension getMinimumSizeExternal()
    // {
    // return frame.getMinimumSize();
    // }
    //
    // /**
    // * Implement getMaximumSize method for external frame only
    // */
    // public Dimension getMaximumSizeExternal()
    // {
    // return frame.getMaximumSize();
    // }
    //
    // /**
    // * Implement getPreferredSize method for external frame only
    // */
    // public Dimension getPreferredSizeExternal()
    // {
    // return frame.getPreferredSize();
    // }
    //
    // /**
    // * Implement getSize method for external frame only
    // */
    // public Dimension getSizeExternal()
    // {
    // return frame.getSize();
    // }
    //
    // /**
    // * Implement getHeight method for external frame only
    // */
    // public int getHeightExternal()
    // {
    // return frame.getHeight();
    // }
    //
    // /**
    // * Implement getWidth method for external frame only
    // */
    // public int getWidthExternal()
    // {
    // return frame.getWidth();
    // }
    //
    // /**
    // * Implement getLocation method for external frame only
    // */
    // public Point getLocationExternal()
    // {
    // return frame.getLocation();
    // }
    //
    // /**
    // * Implement getBounds method for external frame only
    // */
    // public Rectangle getBoundsExternal()
    // {
    // return frame.getBounds();
    // }
    //
    // /**
    // * Implement setLocation method for external frame only
    // */
    // public void setLocationExternal(final Point p)
    // {
    // frame.setLocation(p);
    // }
    //
    // /**
    // * Implement setLocation method for external frame only
    // */
    // public void setLocationExternal(final int x, final int y)
    // {
    // frame.setLocation(x, y);
    // }
    //
    // /**
    // * Implement setSize method for external frame only
    // */
    // public void setSizeExternal(final Dimension d)
    // {
    // frame.setSize(d);
    // }
    //
    // /**
    // * Implement setSize method for external frame only
    // */
    // public void setSizeExternal(final int width, final int height)
    // {
    // frame.setSize(width, height);
    // }
    //
    // /**
    // * Implement setPreferredSize method for external frame only
    // */
    // public void setPreferredSizeExternal(final Dimension d)
    // {
    // frame.setPreferredSize(d);
    // }
    //
    // /**
    // * Implement setMinimumSize method for external frame only
    // */
    // public void setMinimumSizeExternal(final Dimension d)
    // {
    // frame.setMinimumSize(d);
    // }
    //
    // /**
    // * Implement setMaximumSize method for external frame only
    // */
    // public void setMaximumSizeExternal(final Dimension d)
    // {
    // frame.setMaximumSize(d);
    // }

    /**
     * Fire state change event
     */
    private void fireStateChange(boolean externalized)
    {
        for (StateListener l : listenerList.getListeners(StateListener.class))
            l.stateChanged(this, externalized);
    }

    /**
     * Implement addFrameListener method
     */
    public void addStateListener(StateListener l)
    {
        listenerList.add(StateListener.class, l);
    }

    /**
     * Implement removeFrameListener method
     */
    public void removeStateListener(StateListener l)
    {
        listenerList.remove(StateListener.class, l);
    }
}

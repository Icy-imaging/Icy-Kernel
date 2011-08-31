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

import icy.gui.frame.IcyExternalFrame;
import icy.main.Icy;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventListener;

import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class ExternalizablePanel extends JPanel
{
    public interface StateListener extends EventListener
    {
        public void stateChanged(ExternalizablePanel source, boolean externalized);
    }

    private class ExternalFrame extends IcyExternalFrame
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7219003649181966956L;

        public ExternalFrame(String title) throws HeadlessException
        {
            super(title);

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    super.windowClosing(e);

                    if (isExternalized())
                        switchState();
                }
            });

            setSize(400, 400);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -7690543443681714719L;

    private final ExternalFrame frame;
    private Container parent;

    public ExternalizablePanel(String title)
    {
        super();

        frame = new ExternalFrame(title);
        frame.setVisible(false);
        parent = null;
    }

    public ExternalizablePanel()
    {
        this("");
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        if (parent == null)
        {
            final Container p = getParent();

            if (p != frame.getContentPane())
                parent = p;
        }

        Icy.getMainInterface().registerExternalFrame(frame);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();

        Icy.getMainInterface().unRegisterExternalFrame(frame);
    }

    public void setParent(Container value)
    {
        parent = value;
    }

    /**
     * Externalize panel in an independent frame
     */
    public void externalize()
    {
        if (isInternalized())
            externalize_internal();
    }

    /**
     * Internalize panel (remove from independent frame)
     */
    public void internalize()
    {
        if (isExternalized())
            internalize_internal();
    }

    /**
     * Externalize panel (internal method)
     */
    private void externalize_internal()
    {
        // externalize
        if (parent != null)
        {
            parent.remove(this);
            parent.validate();
        }

        frame.add(this);
        frame.validate();
        frame.setVisible(true);

        // notify
        fireStateChange(true);
    }

    /**
     * Internalize panel (internal method)
     */
    private void internalize_internal()
    {
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
            internalize_internal();
        else
            externalize_internal();
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
    public IcyExternalFrame getFrame()
    {
        return frame;
    }

    /**
     * Implement getMinimumSize method for external frame only
     */
    public Dimension getMinimumSizeExternal()
    {
        return frame.getMinimumSize();
    }

    /**
     * Implement getMaximumSize method for external frame only
     */
    public Dimension getMaximumSizeExternal()
    {
        return frame.getMaximumSize();
    }

    /**
     * Implement getPreferredSize method for external frame only
     */
    public Dimension getPreferredSizeExternal()
    {
        return frame.getPreferredSize();
    }

    /**
     * Implement getSize method for external frame only
     */
    public Dimension getSizeExternal()
    {
        return frame.getSize();
    }

    /**
     * Implement getHeight method for external frame only
     */
    public int getHeightExternal()
    {
        return frame.getHeight();
    }

    /**
     * Implement getWidth method for external frame only
     */
    public int getWidthExternal()
    {
        return frame.getWidth();
    }

    /**
     * Implement getLocation method for external frame only
     */
    public Point getLocationExternal()
    {
        return frame.getLocation();
    }

    /**
     * Implement getBounds method for external frame only
     */
    public Rectangle getBoundsExternal()
    {
        return frame.getBounds();
    }

    /**
     * Implement setLocation method for external frame only
     */
    public void setLocationExternal(final Point p)
    {
        frame.setLocation(p);
    }

    /**
     * Implement setLocation method for external frame only
     */
    public void setLocationExternal(final int x, final int y)
    {
        frame.setLocation(x, y);
    }

    /**
     * Implement setSize method for external frame only
     */
    public void setSizeExternal(final Dimension d)
    {
        frame.setSize(d);
    }

    /**
     * Implement setSize method for external frame only
     */
    public void setSizeExternal(final int width, final int height)
    {
        frame.setSize(width, height);
    }

    /**
     * Implement setPreferredSize method for external frame only
     */
    public void setPreferredSizeExternal(final Dimension d)
    {
        frame.setPreferredSize(d);
    }

    /**
     * Implement setMinimumSize method for external frame only
     */
    public void setMinimumSizeExternal(final Dimension d)
    {
        frame.setMinimumSize(d);
    }

    /**
     * Implement setMaximumSize method for external frame only
     */
    public void setMaximumSizeExternal(final Dimension d)
    {
        frame.setMaximumSize(d);
    }

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

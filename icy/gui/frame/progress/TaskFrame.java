/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.gui.frame.progress;

import icy.gui.frame.IcyExternalFrame;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyInternalFrame;
import icy.gui.main.MainFrame;
import icy.gui.main.TaskFrameManager;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Use it to create a Task Window on the border like the loader (Thread Safe)<br>
 * 
 * @author fab & Stephane
 */
public abstract class TaskFrame extends IcyFrame
{
    protected JPanel mainPanel;
    private boolean remove;

    /**
     * 
     */
    public TaskFrame()
    {
        this("Task in progress", false, false, false, false);
    }

    /**
     * @param title
     */
    public TaskFrame(String title)
    {
        this(title, false, false, false, false);
    }

    /**
     * @param title
     * @param resizable
     */
    public TaskFrame(String title, boolean resizable)
    {
        this(title, resizable, false, false, false);
    }

    /**
     * @param title
     * @param resizable
     * @param closable
     */
    public TaskFrame(String title, boolean resizable, boolean closable)
    {
        this(title, resizable, closable, false, false);
    }

    /**
     * @param title
     * @param resizable
     * @param closable
     * @param maximizable
     */
    public TaskFrame(String title, boolean resizable, boolean closable, boolean maximizable)
    {
        this(title, resizable, closable, maximizable, false);
    }

    /**
     * @param title
     * @param resizable
     * @param closable
     * @param maximizable
     * @param iconifiable
     */
    public TaskFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
    {
        super(title, resizable, closable, maximizable, iconifiable, true);

        remove = false;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final IcyInternalFrame iFrame = getIcyInternalFrame();
                final IcyExternalFrame eFrame = getIcyExternalFrame();

                mainPanel = new JPanel();
                mainPanel.setBorder(BorderFactory.createTitledBorder(""));

                // no border on frame
                iFrame.setBorder(BorderFactory.createEmptyBorder());
                // no focusable
                iFrame.setFocusable(false);
                eFrame.setFocusable(false);
                eFrame.setFocusableWindowState(false);
                // no title bar
                iFrame.setTitleBarVisible(false);
                eFrame.setTitleBarVisible(false);

                // set maximum size of task frame
                Dimension maxDim = new Dimension(800, 600);

                final MainFrame mf = Icy.getMainInterface().getMainFrame();
                if (mf != null)
                {
                    final Dimension desktopSize = mf.getDesktopSize();
                    if (desktopSize != null)
                        maxDim = desktopSize;
                }
                iFrame.setMaximumSize(maxDim);
                eFrame.setMaximumSize(maxDim);

                if (isInternalized())
                {
                    iFrame.setLayout(new BorderLayout());
                    iFrame.add(mainPanel, BorderLayout.CENTER);
                }
                else
                {
                    eFrame.setLayout(new BorderLayout());
                    eFrame.add(mainPanel, BorderLayout.CENTER);
                }

                // add to the task manager if a GUI is present
                final TaskFrameManager tfm = Icy.getMainInterface().getTaskWindowManager();

                if (tfm != null)
                    tfm.addTaskWindow(TaskFrame.this);
            }
        });
    }

    @Override
    public void stateChanged()
    {
        super.stateChanged();

        // re pack the frame
        pack();
    }

    @Override
    public void close()
    {
        // prevent direct close here
        remove = true;
    }

    /**
     * Used by the TaskWindowManager to close the TaskFrame.<br>
     * (internal use only)
     */
    public void internalClose()
    {
        super.close();
    }

    /**
     * Used by the TaskWindowManager to close the TaskFrame.<br>
     * (internal use only)
     */
    public boolean canRemove()
    {
        return remove;
    }

    /**
     * @deprecated Use {@link #canRemove()} instead
     */
    @Deprecated
    public boolean isCanBeRemoved()
    {
        return canRemove();
    }
}

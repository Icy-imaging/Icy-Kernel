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
package icy.gui.frame.progress;

import icy.gui.frame.IcyFrame;
import icy.gui.main.TaskFrameManager;

/**
 * Use it to create a Task Window on the border like the loader (Thread Safe)<br>
 * 
 * @author fab & Stephane
 */
public abstract class TaskFrame extends IcyFrame
{
    private boolean canBeRemoved = false;

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
        super(title, resizable, closable, maximizable, iconifiable);

        // add to the task manager if a GUI is present
        final TaskFrameManager tfm = icy.main.Icy.getMainInterface().getTaskWindowManager();

        if (tfm != null)
            tfm.addTaskWindow(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.frame.IcyFrame#close()
     */
    @Override
    public void close()
    {
        // prevent direct close here
        canBeRemoved = true;
    }

    /**
     * used by the TaskWindowManager to close the taskframe
     */
    public void internalClose()
    {
        super.close();
    }

    public boolean isCanBeRemoved()
    {
        return canBeRemoved;
    }
}

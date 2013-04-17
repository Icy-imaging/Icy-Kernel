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
package icy.system.thread;

import icy.main.Icy;

/**
 * @author Stephane
 */
public class InstanceProcessor extends Processor
{
    /**
     * Create an InstanceProcessor
     * 
     * @deprecated uses default constructor instead
     */
    @Deprecated
    public InstanceProcessor(int maxProcess, int maxProcessPerInstance)
    {
        this();
    }

    /**
     * Create an InstanceProcessor
     */
    public InstanceProcessor(int priority)
    {
        super(Processor.DEFAULT_MAX_WAITING, 1, priority);

        setDefaultThreadName("InstanceProcessor");
    }

    /**
     * Create an InstanceProcessor
     */
    public InstanceProcessor()
    {
        this(Processor.NORM_PRIORITY);
    }

    /**
     * Add a task to the processor.<br>
     */
    @Override
    public synchronized boolean addTask(Runnable task, boolean onAWTEventThread, int id)
    {
        if (task == null)
            return false;

        // we remove pending task if any
        removeFirstWaitingTask(task);

        if (!super.addTask(task, onAWTEventThread, id))
        {
            if (!Icy.isExiting())
            {
                // error while adding task
                System.err.println("Cannot add task, ignore execution : " + task);
                return false;
            }
        }

        return true;
    }

}

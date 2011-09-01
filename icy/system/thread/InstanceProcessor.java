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
package icy.system.thread;

/**
 * @author Stephane
 */
public class InstanceProcessor extends Processor
{
    private final int maxProcessPerInstance;

    /**
     * 
     */
    public InstanceProcessor(int maxProcess, int maxProcessPerInstance)
    {
        // no more than 512 waiting tasks
        super(512, maxProcess);

        this.maxProcessPerInstance = maxProcessPerInstance;
    }

    /**
     * Request a process
     */
    @Override
    public synchronized boolean addTask(Runnable task, boolean onAWTEventThread)
    {
        if (task == null)
            return false;

        // we remove some tasks in queue if needed
        while (getWaitingTasksCount(task) >= maxProcessPerInstance)
            removeFirstWaitingTask(task);

        if (!super.addTask(task, onAWTEventThread))
        {
            // error while adding task
            System.err.println("Queue overhead, ignore execution : " + task);
            return false;
        }

        return true;
    }
}

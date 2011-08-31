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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author stephane
 */
public class SingleProcessor extends Processor
{
    class SingleProcessorRejectedExecutionHandler implements RejectedExecutionHandler
    {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
        {
            // just ignore
            // System.out.println("Ignore execution :" + r);
        }
    }

    private final boolean queue;

    /**
     * 
     */
    public SingleProcessor(boolean enableQueue)
    {
        super(1, 1);

        queue = enableQueue;
        setRejectedExecutionHandler(new SingleProcessorRejectedExecutionHandler());
    }

    /**
     * Request a process
     */
    public synchronized boolean requestProcess(Runnable task)
    {
        return requestProcess(task, false);
    }

    /**
     * Request a process
     */
    public synchronized boolean requestProcess(Runnable task, boolean onAWTEventThread)
    {
        if (queue || (!isProcessing()))
        {
            // remove current task if any
            removeAllWaitingTasks();
            // add task
            return addTask(task, onAWTEventThread);
        }

        return false;
    }

}

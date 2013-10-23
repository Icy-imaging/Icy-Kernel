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

import java.util.concurrent.Future;

/**
 * @author Stephane
 */
public class InstanceProcessor extends Processor
{
    /**
     * Create an InstanceProcessor
     */
    public InstanceProcessor(int maxWaiting, int priority)
    {
        super(maxWaiting, 1, priority);

        setDefaultThreadName("InstanceProcessor");
    }

    /**
     * Create an InstanceProcessor
     */
    public InstanceProcessor(int priority)
    {
        this(Processor.DEFAULT_MAX_WAITING, priority);
    }

    /**
     * Create an InstanceProcessor
     */
    public InstanceProcessor()
    {
        this(Processor.NORM_PRIORITY);
    }

    @Override
    protected synchronized <T> Future<T> submit(FutureTaskAdapter<T> task)
    {
        // we remove pending task if any
        removeFirstWaitingTask(task);

        return super.submit(task);
    }
}

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
package icy.common.listener;

/**
 * Progress notification listener.
 * 
 * @author Stephane
 */
public interface ProgressListener
{
    /**
     * Notify progression of the current task
     * 
     * @param position
     *        current position of completion
     * @param length
     *        final position of completion
     * @return <code>false</code> if the cancellation of the task has been requested, <code>true</code> otherwise
     */
    public boolean notifyProgress(double position, double length);
}

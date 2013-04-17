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
package icy.common.listener;

import java.util.EventListener;

/**
 * Basic accept callback.<br>
 * The listener can refuse action from source by returning false.<br>
 * <br>
 * For instance it can be used to prevent main frame from being closed :
 * 
 * <pre>
 * public boolean accept(Object source)
 * {
 *     if (active)
 *         return ConfirmDialog.confirm(&quot;Do you want to interrupt the process ?&quot;);
 * 
 *     return true;
 * }
 * </pre>
 * 
 * @author Stephane
 */
public interface AcceptListener extends EventListener
{
    public boolean accept(Object source);
}

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
package icy.plugin.interface_;

/**
 * Plugin Threaded interface.<br>
 * <br>
 * By default a plugin is launched on the AWT Event Dispatch Thread so GUI creation
 * can be done directly without <code>invokeLater</code> calls.<br>
 * A common problem is that long process will actually lock the EDT and make GUI not responding.<br>
 * <br>
 * A plugin implementing this interface will have both its <code>run()</code> methods called
 * in a separate thread but developer has to use <code>invokeLater</code> method
 * for GUI creation / modification. Also there is no more guarantee that plugin constructor is called
 * in the EDT (which is the case for default plugin).
 * 
 * @author Stephane
 */
public interface PluginThreaded extends Runnable, PluginStartAsThread
{

}

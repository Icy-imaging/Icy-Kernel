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
package icy.plugin.interface_;

/**
 * @author Fabrice de Chaumont
 *         If a Plugin implements this interface, the PluginLauncher will start the plugin as a
 *         thread.
 *         The good point is that the plugin will not lock the interface for his compute.
 *         The bad point is that you have to be cautious, you are not in the awt thread, so you must
 *         consider InvokeLater methods to access all GUI components.
 *         Programming with threads is not as easy as programming in the awt.
 */
public interface PluginStartAsThread
{
}

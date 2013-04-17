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
package icy.plugin.interface_;

import icy.plugin.PluginLoader;

/**
 * Interface for inner bundled plugin.
 * This interface should be used for plugin which are packaged inside others plugins<br>
 * (in a single JAR plugin file).<br>
 * Generally you should avoid that as only one plugin can be correctly identified in a JAR<br>
 * and a descriptor but in some case it can be usefull.<br>
 * You have to implement the {@link #getMainPluginClassName()} method to return<br>
 * the main plugin class name so your plugin can be identified.
 * This class will also hide your plugin from the plugin list in {@link PluginLoader}.
 * 
 * @author Stephane
 */
public interface PluginBundled
{
    public String getMainPluginClassName();
}

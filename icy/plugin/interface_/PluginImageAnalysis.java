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

import icy.plugin.abstract_.PluginActionable;

/**
 * The old declaration :<br>
 * <code>class MyPlugin extends Plugin implements PluginImageAnalysis</code><br>
 * becomes :<br>
 * <code>class MyPlugin extends PluginActionable</code>
 * 
 * @deprecated Uses {@link PluginActionable} instead.
 */
@Deprecated
public interface PluginImageAnalysis
{
    /**
     * Main compute method for PluginImageAnalysis interface
     */
    abstract public void compute();
}

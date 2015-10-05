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
package icy.plugin.abstract_;

import icy.plugin.interface_.PluginNoEDTConstructor;
import icy.sequence.SequenceExporter;

/**
 * Plugin specialized for Sequence export operation (see the {@link SequenceExporter} interface)
 * 
 * @see PluginSequenceFileExporter
 * @author Stephane
 */
public abstract class PluginSequenceExporter extends Plugin implements SequenceExporter, PluginNoEDTConstructor
{

}

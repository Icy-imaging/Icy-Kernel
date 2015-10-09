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
package icy.sequence;

import icy.file.SequenceFileExporter;

/**
 * Sequence exporter interface.<br>
 * Used to define a specific exporter (appears in the <b>Export</b> section).<br>
 * Note that you have the {@link SequenceFileExporter} interface* which allow to export
 * {@link Sequence} to file.
 * 
 * @author Stephane
 */
public interface SequenceExporter
{
    /**
     * Launch the exporter and save the specified Sequence.<br>
     * The exporter is responsible to handle its own UI and save the wanted resource.
     * 
     * @param sequence
     *        sequence to save
     * @return <code>true</code> if the operation succeed
     */
    public boolean save(Sequence sequence);
}

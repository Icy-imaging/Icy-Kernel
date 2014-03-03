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
package icy.file;

import icy.sequence.Sequence;

/**
 * Importer interface.<br>
 * Used to define a specific importer visible in the <b>Import</b> section.<br>
 * Note that you have {@link FileImporter} and {@link SequenceFileImporter} interfaces
 * which allow to import resources from file or {@link Sequence} from file.
 * 
 * @author Stephane
 */
public interface Importer
{
    /**
     * Launch the importer.<br>
     * The importer is responsible to handle its own UI and load the wanted resource.
     * 
     * @return <code>true</code> if the operation succeed
     */
    public boolean load() throws Exception;
}

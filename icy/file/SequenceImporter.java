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

import icy.image.ImageProvider;
import icy.sequence.Sequence;

/**
 * Sequence importer interface.<br>
 * Used to define a specific {@link Sequence} importer visible in the <b>Import</b> section.<br>
 * Can take any resource type as input and return Sequence array as result.
 * Note that you have {@link SequenceFileImporter} interface which allow to import {@link Sequence}
 * from file(s).
 * 
 * @author Stephane
 */
public interface SequenceImporter extends ImageProvider
{
    /**
     * Return the description given on the importer menu
     */
    public String getDescription();
}

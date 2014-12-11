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

import icy.common.exception.UnsupportedFormatException;
import icy.image.AbstractImageProvider;
import icy.image.ImageProvider;
import icy.sequence.SequenceIdImporter;

import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * Sequence file importer interface.<br>
 * The importer is directly integrated in the classic <i>Open</i> command menu and in the
 * {@link Loader} class to open Sequence.<br>
 * Can take any resource type identified by a file and should be able to give multiple level access
 * to the image data.<br>
 * See details about the image data access implementation with the {@link ImageProvider} interface
 * and {@link AbstractImageProvider} abstract class helper.
 * 
 * @see SequenceIdImporter
 * @author Stephane
 */
public interface SequenceFileImporter extends SequenceIdImporter
{
    /**
     * @return The path of the image file of the image currently opened or <code>null</code>
     *         otherwise.<br>
     *         Note that path is always returned in generic java path style (see
     *         {@link FileUtil#getGenericPath(String)} method).
     * @see #open(String, int)
     * @see #close()
     */
    @Override
    public String getOpened();

    /**
     * Open the image designed by the specified file <code>path</code> to allow image data /
     * metadata access.<br>
     * Calling this method will automatically close the previous opened image.<br>
     * Don't forget to call {@link #close()} to close the image when you're done.<br>
     * 
     * @param path
     *        Path of the image file to open.
     * @param flags
     *        operation flag (not used yet, keep it to 0)
     * @return <code>true</code> if the operation has succeeded and <code>false</code> otherwise.
     */
    @Override
    public boolean open(String path, int flags) throws UnsupportedFormatException, IOException;

    /**
     * Return <code>true</code> if the specified file can be opened by the importer.
     */
    public boolean acceptFile(String path);

    /**
     * Return the supported FileFilter for this importer.
     */
    public List<FileFilter> getFileFilters();
}

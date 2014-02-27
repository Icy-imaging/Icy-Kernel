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
import icy.gui.frame.progress.FileFrame;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * File importer interface.<br>
 * The importer is directly integrated in the classic <b>Open</b> command menu and can open any file
 * type except image (use the {@link SequenceFileImporter} interface for image file).<br>
 * It is from his responsibility to make the opened file available in the application.
 * 
 * @author Stephane
 */
public interface FileImporter
{
    /**
     * Return <code>true</code> if the specified file can be opened by the importer.
     */
    public boolean acceptFile(File file);

    /**
     * Return the supported FileFilter for this importer.
     */
    public List<FileFilter> getFileFilters();

    /**
     * Load the specified file and returns true if the operation succeed.<br>
     * The method is free to handle the way it makes the opened file available in the application.
     * 
     * @param file
     *        File to load.
     * @param loadingFrame
     *        Frame where to display the loading progression (can be <code>null</code> if no
     *        progression wanted)
     * @return <code>true</code> if the operation succeed
     */
    public boolean load(File file, FileFrame loadingFrame) throws UnsupportedFormatException, IOException;

    // /**
    // * Load the specified files and returns true if the operation succeed.<br>
    // * The method is free to handle the way it makes the opened file available in the application.
    // *
    // * @param files
    // * List of image file to load.
    // * @param loadingFrame
    // * Frame where to display the loading progression (can be <code>null</code> if no
    // * progression wanted)
    // * @return <code>true</code> if the operation succeed
    // */
    // public boolean load(File[] files, FileFrame loadingFrame) throws UnsupportedFormatException,
    // IOException;
}

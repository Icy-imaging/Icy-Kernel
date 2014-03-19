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

import icy.gui.frame.progress.FileFrame;
import icy.sequence.Sequence;

import java.io.File;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * Sequence file exporter interface.<br>
 * The exporter is directly integrated in the classic <b>Save</b> command menu and in the
 * {@link Saver} class to open Sequence.<br>
 * It takes a {@link Sequence} and saves it in {@link File}.
 * 
 * @author Stephane
 */
public interface SequenceFileExporter
{
    /**
     * Return the supported FileFilter for this exporter.
     */
    public List<FileFilter> getFileFilters();

    /**
     * Save the specified sequence in the specified file.<br>
     * 
     * @param sequence
     *        sequence to save
     * @param path
     *        file where we want to save sequence
     * @param loadingFrame
     *        progress bar (if available)
     * @return <code>true</code> if the operation succeed
     */
    public boolean save(Sequence sequence, String path, FileFrame loadingFrame);
}

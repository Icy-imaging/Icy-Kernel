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

import icy.common.exception.UnsupportedFormatException;
import icy.file.SequenceFileImporter;
import icy.image.AbstractImageProvider;
import icy.image.ImageProvider;

import java.io.Closeable;
import java.io.IOException;

/**
 * Sequence importer interface.<br>
 * Used to define a specific {@link Sequence} importer.<br>
 * Can take any resource type identified by an ID and should be able to give multiple level access
 * to the image data.<br>
 * See details about the image data access implementation with the {@link ImageProvider} interface
 * and {@link AbstractImageProvider} abstract class helper.
 * Note that you have {@link SequenceFileImporter} interface which allow to import {@link Sequence}
 * from file(s).
 * 
 * @author Stephane
 */
public interface SequenceIdImporter extends ImageProvider, Closeable
{
    /**
     * @return The <code>id</code> of the image currently opened or <code>null</code> otherwise.
     * @see #open(String, int)
     * @see #close()
     */
    public String getOpened();

    /**
     * Open the image designed by the specified <code>id</code> to allow image data / metadata
     * access.<br>
     * Calling this method will automatically close the previous opened image.<br>
     * Don't forget to call {@link #close()} to close the image when you're done.<br>
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @param flags
     *        operation flag (not used yet, keep it to 0)
     * @return <code>true</code> if the operation has succeeded and <code>false</code> otherwise.
     */
    public boolean open(String id, int flags) throws UnsupportedFormatException, IOException;

    /**
     * Close the image which has been previously opened with {@link #open(String, int)} method.<br>
     */
    @Override
    public void close() throws IOException;
}

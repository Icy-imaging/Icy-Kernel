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

import java.awt.Image;
import java.util.EventListener;

/**
 * Sequence Model.<br>
 * <br>
 * Basic sequence model (5D image data structure).
 * 
 * @author Stephane
 */
public interface SequenceModel
{
    public static interface SequenceModelListener extends EventListener
    {
        /**
         * Sequence model image changed.
         */
        public void imageChanged();

        /**
         * Sequence dimension image changed.
         */
        public void dimensionChanged();
    }

    /**
     * Get dimension X size
     */
    public int getSizeX();

    /**
     * Get dimension Y size
     */
    public int getSizeY();

    /**
     * Get dimension Z size
     */
    public int getSizeZ();

    /**
     * Get dimension T size
     */
    public int getSizeT();

    /**
     * Get dimension C size
     */
    public int getSizeC();

    /**
     * Get image at position [T, Z]
     */
    public Image getImage(int t, int z);

    /**
     * Get image at position [T, Z, C]
     */
    public Image getImage(int t, int z, int c);

    /**
     * fire model image changed event
     */
    public void fireModelImageChangedEvent();

    /**
     * fire model dimension changed event
     */
    public void fireModelDimensionChangedEvent();

    public void addSequenceModelListener(SequenceModelListener listener);

    public void removeSequenceModelListener(SequenceModelListener listener);
}

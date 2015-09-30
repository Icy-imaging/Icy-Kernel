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
package icy.gui.dialog;

import icy.sequence.Sequence;

/**
 * @deprecated Use {@link SaveDialog} instead.
 */
@Deprecated
public class ImageSaverDialog extends SaverDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -5478292512178275546L;

    /**
     * @deprecated Use {@link LoaderDialog.AllImagesFileFilter} instead.
     */
    @Deprecated
    public ImageSaverDialog(Sequence sequence, int defZ, int defT, boolean autoSave)
    {
        super(sequence, defZ, defT, autoSave);
    }

    /**
     * @deprecated Use {@link LoaderDialog.AllImagesFileFilter} instead.
     */
    @Deprecated
    public ImageSaverDialog(Sequence sequence, int defZ, int defT)
    {
        super(sequence, defZ, defT);
    }

    /**
     * @deprecated Use {@link LoaderDialog.AllImagesFileFilter} instead.
     */
    @Deprecated
    public ImageSaverDialog(Sequence sequence, boolean autoSave)
    {
        super(sequence, autoSave);
    }

    /**
     * @deprecated Use {@link LoaderDialog.AllImagesFileFilter} instead.
     */
    @Deprecated
    public ImageSaverDialog(Sequence sequence)
    {
        super(sequence);
    }
}

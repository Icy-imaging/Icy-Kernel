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

/**
 * @deprecated Use {@link LoaderDialog} instead.
 */
@Deprecated
public class ImageLoaderDialog extends LoaderDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 3459047305037333327L;

    /**
     * @deprecated Use {@link LoaderDialog.AllImagesFileFilter} instead.
     */
    @Deprecated
    public static class AllImagesFileFilter extends LoaderDialog.AllImagesFileFilter
    {

    }

    /**
     * @deprecated
     */
    @Deprecated
    public static class AllImageFileFilter extends AllImagesFileFilter
    {

    }

    public ImageLoaderDialog(boolean autoLoad)
    {
        super(autoLoad);
    }

    public ImageLoaderDialog()
    {
        super();
    }
}

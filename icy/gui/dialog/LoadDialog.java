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
 * @deprecated Use {@link OpenDialog} instead.
 * @author Stephane
 */
@Deprecated
public class LoadDialog
{
    /**
     * @deprecated Use {@link OpenDialog#chooseFile(String, String, String, String)} instead
     */
    @Deprecated
    public static String chooseFile(String title, String defaultDir, String defaultName, String extension)
    {
        return OpenDialog.chooseFile(title, defaultDir, defaultName, extension);
    }

    /**
     * @deprecated Use {@link OpenDialog#chooseFile(String, String, String)} instead
     */
    @Deprecated
    public static String chooseFile(String title, String defaultDir, String defaultName)
    {
        return OpenDialog.chooseFile(title, defaultDir, defaultName);
    }

    /**
     * @deprecated Use {@link OpenDialog#chooseFile(String, String)} instead
     */
    @Deprecated
    public static String chooseFile(String defaultDir, String defaultName)
    {
        return OpenDialog.chooseFile(defaultDir, defaultName);
    }

    /**
     * @deprecated Use {@link OpenDialog#chooseFile()} instead
     */
    @Deprecated
    public static String chooseFile()
    {
        return OpenDialog.chooseFile();
    }

}

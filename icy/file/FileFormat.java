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

import loci.formats.gui.ExtensionFileFilter;

/**
 * @author Stephane
 */
public enum FileFormat
{
    TIFF
    {
        @Override
        public String[] getExtensions()
        {
            return new String[] {"tif", "tiff"};
        }

        @Override
        public String getDescription()
        {
            return "TIFF images";
        }
    },
    PNG
    {
        @Override
        public String[] getExtensions()
        {
            return new String[] {"png"};
        }

        @Override
        public String getDescription()
        {
            return "PNG images";
        }
    },
    LSM
    {
        @Override
        public String[] getExtensions()
        {
            return new String[] {"lsm"};
        }

        @Override
        public String getDescription()
        {
            return "LSM images";
        }
    },
    JPG
    {
        @Override
        public String[] getExtensions()
        {
            return new String[] {"jpg", "jpeg"};
        }

        @Override
        public String getDescription()
        {
            return "JPG images";
        }
    },
    AVI
    {
        @Override
        public String[] getExtensions()
        {
            return new String[] {"avi"};
        }

        @Override
        public String getDescription()
        {
            return "AVI sequences";
        }
    };

    public ExtensionFileFilter getExtensionFileFilter()
    {
        return new ExtensionFileFilter(getExtensions(), getDescription());
    }

    /**
     * Get file format description
     */
    public String getDescription()
    {
        return "unknow";
    }

    /**
     * Get file format extensions
     */
    public String[] getExtensions()
    {
        return new String[] {""};
    }

    /**
     * Return true if the specified extension matches this format.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public boolean matches(String ext)
    {
        for (String e : getExtensions())
            if (e.equals(ext))
                return true;

        return false;
    }

    /**
     * Return the FileFormat corresponding to specified extension.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static FileFormat getFileFormat(String ext, FileFormat defaultValue)
    {
        for (FileFormat ff : values())
            if (ff.matches(ext))
                return ff;

        return defaultValue;
    }

    /**
     * Return the FileFormat corresponding to specified filename extension.<br>
     * <code>null</code> is returned if no matching format is found.
     */
    public static FileFormat getFileFormat(String ext)
    {
        return getFileFormat(ext, null);
    }
}

/**
 * 
 */
package icy.file;

import loci.formats.gui.ExtensionFileFilter;

/**
 * Define some default image file format for Icy.
 * 
 * @author Stephane
 */
public enum ImageFileFormat
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

        @Override
        public boolean canRead()
        {
            return true;
        }

        @Override
        public boolean canWrite()
        {
            return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public FileFormat toFileFormat()
        {
            return FileFormat.TIFF;
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

        @Override
        public boolean canRead()
        {
            return true;
        }

        @Override
        public boolean canWrite()
        {
            return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public FileFormat toFileFormat()
        {
            return FileFormat.PNG;
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

        @Override
        public boolean canRead()
        {
            return true;
        }

        @Override
        public boolean canWrite()
        {
            return false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public FileFormat toFileFormat()
        {
            return FileFormat.LSM;
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

        @Override
        public boolean canRead()
        {
            return true;
        }

        @Override
        public boolean canWrite()
        {
            return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public FileFormat toFileFormat()
        {
            return FileFormat.JPG;
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

        @Override
        public boolean canRead()
        {
            return true;
        }

        @Override
        public boolean canWrite()
        {
            return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public FileFormat toFileFormat()
        {
            return FileFormat.AVI;
        }
    };

    /**
     * Returns true if the image file format supports read operation.
     */
    public abstract boolean canRead();

    /**
     * Returns true if the image file format supports write operation.
     */
    public abstract boolean canWrite();

    /**
     * Returns the image file format description.
     */
    public String getDescription()
    {
        return "unknow";
    }

    /**
     * Returns the image file format extensions.
     */
    public String[] getExtensions()
    {
        return new String[] {""};
    }

    /**
     * For backward compatibility with {@link FileFormat}.
     */
    @SuppressWarnings({"deprecation", "javadoc"})
    public abstract FileFormat toFileFormat();

    /**
     * Returns the associated {@link ExtensionFileFilter}
     */
    public ExtensionFileFilter getExtensionFileFilter()
    {
        return new ExtensionFileFilter(getExtensions(), getDescription());
    }

    /**
     * Return true if the specified extension matches this format.<br>
     * <code>defaultValue</code> is returned if no matching format is found (it can be null).
     */
    public boolean matches(String ext)
    {
        if (ext == null)
            return false;

        // always consider lower case extension
        final String extLC = ext.toLowerCase();

        for (String e : getExtensions())
            if (e.equals(extLC))
                return true;

        return false;
    }

    /**
     * Returns the FileFormat corresponding to specified extension.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getFormat(String ext, ImageFileFormat defaultValue)
    {
        for (ImageFileFormat iff : values())
            if (iff.matches(ext))
                return iff;

        return defaultValue;
    }

    /**
     * Returns the {@link ImageFileFormat} corresponding to the specified extension and which
     * support read operation.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getReadFormat(String ext, ImageFileFormat defaultValue)
    {
        for (ImageFileFormat iff : values())
            if (iff.canRead() && iff.matches(ext))
                return iff;

        return defaultValue;
    }

    /**
     * Returns the {@link ImageFileFormat} corresponding to the specified extension and which
     * support write operation.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getWriteFormat(String ext, ImageFileFormat defaultValue)
    {
        for (ImageFileFormat iff : values())
            if (iff.canWrite() && iff.matches(ext))
                return iff;

        return defaultValue;
    }

    /**
     * For backward compatibility with {@link FileFormat}.
     */
    @SuppressWarnings({"deprecation", "javadoc"})
    public static ImageFileFormat getFormat(FileFormat format)
    {
        switch (format)
        {
            case AVI:
                return AVI;
            case JPG:
                return JPG;
            case LSM:
                return LSM;
            case PNG:
                return PNG;
            default:
                return TIFF;
        }
    }
}

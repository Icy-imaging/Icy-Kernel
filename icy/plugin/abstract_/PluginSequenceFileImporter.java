/**
 * 
 */
package icy.plugin.abstract_;

import icy.common.exception.UnsupportedFormatException;
import icy.file.SequenceFileImporter;
import icy.image.AbstractImageProvider;
import icy.image.IcyBufferedImage;

import java.awt.Rectangle;
import java.io.IOException;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Plugin specialized for Sequence file import operation (see the {@link SequenceFileImporter}
 * interface)
 * 
 * @author Stephane
 */
public abstract class PluginSequenceFileImporter extends Plugin implements SequenceFileImporter
{
    // default helper
    protected class InternalImageProviderHelper extends AbstractImageProvider
    {
        @Override
        public OMEXMLMetadataImpl getMetaData(String path) throws UnsupportedFormatException, IOException
        {
            return PluginSequenceFileImporter.this.getMetaData(path);
        }

        @Override
        public IcyBufferedImage getImage(String path, int serie, int resolution, Rectangle rectangle, int z, int t,
                int c) throws UnsupportedFormatException, IOException
        {
            return PluginSequenceFileImporter.this.getImage(path, serie, resolution, rectangle, z, t, c);
        }
    }

    protected final InternalImageProviderHelper interfaceHelper;

    public PluginSequenceFileImporter()
    {
        super();

        interfaceHelper = new InternalImageProviderHelper();
    }

    @Override
    public String getOpened()
    {
        return interfaceHelper.getOpened();
    }

    // default implementation, override it to provide better consecutive access performance
    @Override
    public boolean open(String path) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.open(path);
    }

    // default implementation, override it to provide better consecutive access performance
    @Override
    public boolean close() throws IOException
    {
        return interfaceHelper.close();
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(String path, int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileWidth(path, serie);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(String path, int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileHeight(path, serie);
    }

    // default implementation
    @Override
    public IcyBufferedImage getThumbnail(String path, int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getThumbnail(path, serie);
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(String path, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getPixels(path, serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(path, serie, resolution, rectangle, z, t);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(path, serie, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(path, serie, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int z, int t) throws UnsupportedFormatException,
            IOException
    {
        return interfaceHelper.getImage(path, serie, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String path, int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(path, z, t);
    }

    /**
     * See {@link AbstractImageProvider#getResolutionFactor(String, int, int)}
     */
    public int getResolutionFactor(String path, int serie, int wantedSize) throws UnsupportedFormatException,
            IOException
    {
        return interfaceHelper.getResolutionFactor(path, serie, wantedSize);
    }
}

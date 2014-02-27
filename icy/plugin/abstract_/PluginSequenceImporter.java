/**
 * 
 */
package icy.plugin.abstract_;

import icy.common.exception.UnsupportedFormatException;
import icy.file.SequenceImporter;
import icy.image.AbstractImageProvider;
import icy.image.IcyBufferedImage;

import java.awt.Rectangle;
import java.io.IOException;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Plugin specialized for Sequence import operation (see the {@link SequenceImporter} interface)
 * 
 * @author Stephane
 */
public abstract class PluginSequenceImporter extends Plugin implements SequenceImporter
{
    // default helper
    protected class InternalImageProviderHelper extends AbstractImageProvider
    {
        @Override
        public OMEXMLMetadataImpl getMetaData(String id) throws UnsupportedFormatException, IOException
        {
            return PluginSequenceImporter.this.getMetaData(id);
        }

        @Override
        public IcyBufferedImage getImage(String id, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
                throws UnsupportedFormatException, IOException
        {
            return PluginSequenceImporter.this.getImage(id, serie, resolution, rectangle, z, t, c);
        }
    }

    protected final InternalImageProviderHelper interfaceHelper;

    public PluginSequenceImporter()
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
    public boolean open(String id) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.open(id);
    }

    // default implementation, override it to provide better consecutive access performance
    @Override
    public boolean close() throws IOException
    {
        return interfaceHelper.close();
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(String id, int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileWidth(id, serie);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(String id, int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileHeight(id, serie);
    }

    // default implementation
    @Override
    public IcyBufferedImage getThumbnail(String id, int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getThumbnail(id, serie);
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(String id, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getPixels(id, serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(id, serie, resolution, rectangle, z, t);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(id, serie, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(id, serie, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(id, serie, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String id, int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(id, z, t);
    }

    /**
     * See {@link AbstractImageProvider#getResolutionFactor(String, int, int)}
     */
    public int getResolutionFactor(String id, int serie, int wantedSize) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getResolutionFactor(id, serie, wantedSize);
    }
}

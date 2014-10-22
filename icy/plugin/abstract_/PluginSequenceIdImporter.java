/**
 * 
 */
package icy.plugin.abstract_;

import icy.common.exception.UnsupportedFormatException;
import icy.image.AbstractImageProvider;
import icy.image.IcyBufferedImage;
import icy.plugin.interface_.PluginNoEDTConstructor;
import icy.plugin.interface_.PluginThreaded;
import icy.sequence.SequenceIdImporter;

import java.awt.Rectangle;
import java.io.IOException;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Plugin specialized for Sequence id import operation (see the {@link SequenceIdImporter}
 * interface)
 * 
 * @see PluginImporter
 * @see PluginFileImporter
 * @see PluginSequenceFileImporter
 * @see PluginSequenceImporter
 * @author Stephane
 */
public abstract class PluginSequenceIdImporter extends Plugin implements SequenceIdImporter, PluginNoEDTConstructor
{
    // default helper
    protected class InternalImageProviderHelper extends AbstractImageProvider
    {
        @Override
        public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException
        {
            return PluginSequenceIdImporter.this.getMetaData();
        }

        @Override
        public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
                throws UnsupportedFormatException, IOException
        {
            return PluginSequenceIdImporter.this.getImage(serie, resolution, rectangle, z, t, c);
        }
    }

    protected final InternalImageProviderHelper interfaceHelper;

    public PluginSequenceIdImporter()
    {
        super();

        interfaceHelper = new InternalImageProviderHelper();
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileWidth(serie);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileHeight(serie);
    }

    // default implementation
    @Override
    public IcyBufferedImage getThumbnail(int serie) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getThumbnail(serie);
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getPixels(serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(serie, resolution, rectangle, z, t);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t, int c) throws UnsupportedFormatException,
            IOException
    {
        return interfaceHelper.getImage(serie, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t) throws UnsupportedFormatException,
            IOException
    {
        return interfaceHelper.getImage(serie, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(serie, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(z, t);
    }

    /**
     * See {@link AbstractImageProvider#getResolutionFactor(int, int)}
     */
    public int getResolutionFactor(int serie, int wantedSize) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getResolutionFactor(serie, wantedSize);
    }
}

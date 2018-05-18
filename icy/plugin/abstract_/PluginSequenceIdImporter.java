/**
 * 
 */
package icy.plugin.abstract_;

import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ProgressListener;
import icy.image.AbstractImageProvider;
import icy.image.IcyBufferedImage;
import icy.plugin.interface_.PluginNoEDTConstructor;
import icy.sequence.SequenceIdImporter;

import java.awt.Rectangle;
import java.io.IOException;

import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;

/**
 * Plugin specialized for Sequence id import operation (see the {@link SequenceIdImporter} interface)
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
    protected class InternalSequenceIdImporterHelper extends AbstractImageProvider implements SequenceIdImporter
    {
        @Override
        public String getOpened()
        {
            return PluginSequenceIdImporter.this.getOpened();
        }

        @Override
        public boolean open(String id, int flags) throws UnsupportedFormatException, IOException
        {
            return PluginSequenceIdImporter.this.open(id, flags);
        }

        @Override
        public void close() throws IOException
        {
            PluginSequenceIdImporter.this.close();
        }

        @Deprecated
        @Override
        public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException
        {
            return PluginSequenceIdImporter.this.getMetaData();
        }

        @Override
        public IcyBufferedImage getImage(int series, int resolution, Rectangle rectangle, int z, int t, int c)
                throws UnsupportedFormatException, IOException
        {
            return PluginSequenceIdImporter.this.getImage(series, resolution, rectangle, z, t, c);
        }
    }

    protected final InternalSequenceIdImporterHelper interfaceHelper;

    public PluginSequenceIdImporter()
    {
        super();

        interfaceHelper = new InternalSequenceIdImporterHelper();
    }

    // default implementation as ImageProvider interface changed
    @Override
    public OMEXMLMetadata getOMEXMLMetaData() throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getOMEXMLMetaData();
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(int series) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileWidth(series);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(int series) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getTileHeight(series);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public boolean isResolutionAvailable(int series, int resolution) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.isResolutionAvailable(series, resolution);
    }

    // default implementation
    @Override
    public IcyBufferedImage getThumbnail(int series) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getThumbnail(series);
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(int series, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getPixels(series, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int series, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(series, resolution, rectangle, z, t);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(int series, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(series, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int series, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(series, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int series, int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(series, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getImage(z, t);
    }

    /**
     * See {@link AbstractImageProvider#getPixelsByTile(int, int, Rectangle, int, int, int, int, int,ProgressListener)}
     */
    public Object getPixelsByTile(int series, int resolution, Rectangle region, int z, int t, int c, int tileW,
            int tileH, ProgressListener listener) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getPixelsByTile(series, resolution, region, z, t, c, tileW, tileH, listener);
    }

    /**
     * See {@link AbstractImageProvider#getResolutionFactor(int, int)}
     */
    public int getResolutionFactor(int series, int wantedSize) throws UnsupportedFormatException, IOException
    {
        return interfaceHelper.getResolutionFactor(series, wantedSize);
    }
}

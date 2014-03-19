/**
 * 
 */
package plugins.kernel.importer;

import icy.common.exception.UnsupportedFormatException;
import icy.image.IcyBufferedImage;
import icy.plugin.abstract_.PluginSequenceFileImporter;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * @author Stephane
 */
public class LociImporterPlugin extends PluginSequenceFileImporter
{
    final LociImporter importer;

    public LociImporterPlugin()
    {
        super();

        importer = new LociImporter();
    }

    @Override
    public boolean acceptFile(String path)
    {
        return importer.acceptFile(path);
    }

    @Override
    public List<FileFilter> getFileFilters()
    {
        return importer.getFileFilters();
    }

    @Override
    public String getOpened()
    {
        return importer.getOpened();
    }

    @Override
    public boolean open(String path, int flags) throws UnsupportedFormatException, IOException
    {
        return importer.open(path, flags);
    }

    @Override
    public boolean close() throws IOException
    {
        return importer.close();
    }

    @Override
    public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException
    {
        return importer.getMetaData();
    }

    @Override
    public int getTileWidth(int serie) throws UnsupportedFormatException, IOException
    {
        return importer.getTileWidth(serie);
    }

    @Override
    public int getTileHeight(int serie) throws UnsupportedFormatException, IOException
    {
        return importer.getTileHeight(serie);
    }

    @Override
    public IcyBufferedImage getThumbnail(int serie) throws UnsupportedFormatException, IOException
    {
        return importer.getThumbnail(serie);
    }

    @Override
    public Object getPixels(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return importer.getPixels(serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(serie, resolution, rectangle, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(serie, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(serie, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int z, int t) throws UnsupportedFormatException, IOException
    {
        return importer.getImage(serie, z, t);
    }

    @Override
    public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException
    {
        return importer.getImage(z, t);
    }

}

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
    public boolean open(String id) throws UnsupportedFormatException, IOException
    {
        return importer.open(id);
    }

    @Override
    public boolean close() throws IOException
    {
        return importer.close();
    }

    @Override
    public OMEXMLMetadataImpl getMetaData(String id) throws UnsupportedFormatException, IOException
    {
        return importer.getMetaData(id);
    }

    @Override
    public int getTileWidth(String id, int serie) throws UnsupportedFormatException, IOException
    {
        return importer.getTileWidth(id, serie);
    }

    @Override
    public int getTileHeight(String id, int serie) throws UnsupportedFormatException, IOException
    {
        return importer.getTileHeight(id, serie);
    }

    @Override
    public IcyBufferedImage getThumbnail(String id, int serie) throws UnsupportedFormatException, IOException
    {
        return importer.getThumbnail(id, serie);
    }

    @Override
    public Object getPixels(String id, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return importer.getPixels(id, serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(id, serie, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(id, serie, resolution, rectangle, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(id, serie, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return importer.getImage(id, serie, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String id, int serie, int z, int t) throws UnsupportedFormatException, IOException
    {
        return importer.getImage(id, serie, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String id, int z, int t) throws UnsupportedFormatException, IOException
    {
        return importer.getImage(id, z, t);
    }

}

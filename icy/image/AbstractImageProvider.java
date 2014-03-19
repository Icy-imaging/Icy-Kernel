/**
 * 
 */
package icy.image;

import icy.common.exception.UnsupportedFormatException;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.sequence.MetaDataUtil;

import java.awt.Rectangle;
import java.io.IOException;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Abstract implementation of the {@link ImageProvider} interface.<br>
 * It provide methods wrapper so you only need implement one method the get your importer working.<br>
 * But free feel to override more methods to provide better support and/or better performance.
 * 
 * @author Stephane
 */
public abstract class AbstractImageProvider implements ImageProvider
{
    public static final int DEFAULT_THUMBNAIL_SIZE = 160;

    // // default implementation, override it to provide better consecutive access performance
    // @Override
    // public String getOpened()
    // {
    // // just assume not opened
    // return null;
    // }
    //
    // // default implementation, override it to provide better consecutive access performance
    // @Override
    // public boolean open(String id) throws UnsupportedFormatException, IOException
    // {
    // // just assume ok
    // return true;
    // }
    //
    // // default implementation, override it to provide better consecutive access performance
    // @Override
    // public boolean close() throws IOException
    // {
    // // just assume ok
    // return true;
    // }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(int serie) throws UnsupportedFormatException, IOException
    {
        return MetaDataUtil.getSizeX(getMetaData(), serie);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(int serie) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadataImpl meta = getMetaData();
        final int sx = MetaDataUtil.getSizeX(meta, serie);

        if (sx == 0)
            return 0;

        // default implementation
        final int maxHeight = (1024 * 1024) / sx;
        final int sy = MetaDataUtil.getSizeY(meta, serie);

        return Math.min(maxHeight, sy);
    }

    // default implementation which use the getImage(..) method, override it for better support /
    // performance.
    @Override
    public IcyBufferedImage getThumbnail(int serie) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadataImpl meta = getMetaData();
        final int sx = MetaDataUtil.getSizeX(meta, serie);
        final int sy = MetaDataUtil.getSizeY(meta, serie);
        final int sz = MetaDataUtil.getSizeZ(meta, serie);
        final int st = MetaDataUtil.getSizeT(meta, serie);

        // empty size --> return null
        if ((sx == 0) || (sy == 0) || (sz == 0) || (st == 0))
            return null;

        final double ratio = Math.min((double) DEFAULT_THUMBNAIL_SIZE / (double) sx, (double) DEFAULT_THUMBNAIL_SIZE
                / (double) sy);

        // final thumbnail size
        final int tnx = (int) Math.round(sx * ratio);
        final int tny = (int) Math.round(sy * ratio);
        final int resolution = getResolutionFactor(sx, sy, DEFAULT_THUMBNAIL_SIZE);

        // take middle image for thumbnail
        IcyBufferedImage result = getImage(serie, resolution, sz / 2, st / 2);
        // scale it to desired dimension
        return IcyBufferedImageUtil.scale(result, tnx, tny, FilterType.BILINEAR);
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return getImage(serie, resolution, rectangle, z, t, c).getDataXY(0);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return getImage(serie, resolution, rectangle, z, t, -1);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t, int c) throws UnsupportedFormatException,
            IOException
    {
        return getImage(serie, resolution, null, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t) throws UnsupportedFormatException,
            IOException
    {
        return getImage(serie, resolution, null, z, t, -1);
    }

    @Override
    public IcyBufferedImage getImage(int serie, int z, int t) throws UnsupportedFormatException, IOException
    {
        return getImage(serie, 0, null, z, t, -1);
    }

    @Override
    public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException
    {
        return getImage(0, 0, null, z, t, -1);
    }

    /**
     * Returns the image resolution that best suit to the size resolution.
     * 
     * @param sizeX
     *        original image width
     * @param sizeY
     *        original image height
     * @param wantedSize
     *        wanted size (for the maximum dimension)
     * @return resolution ratio<br>
     *         0 = original resolution<br>
     *         1 = (original resolution / 2)<br>
     *         2 = (original resolution / 4)
     */
    public static int getResolutionFactor(int sizeX, int sizeY, int wantedSize)
    {
        int sx = sizeX / 2;
        int sy = sizeY / 2;
        int result = 0;

        while ((sx > wantedSize) || (sy > wantedSize))
        {
            sx /= 2;
            sy /= 2;
            result++;
        }

        return result;
    }

    /**
     * Returns the image resolution that best suit to the size resolution.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param wantedSize
     *        wanted size (for the maximum dimension)
     * @return resolution ratio<br>
     *         0 = original resolution<br>
     *         1 = (original resolution / 2)<br>
     *         2 = (original resolution / 4)
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public int getResolutionFactor(int serie, int wantedSize) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadataImpl meta = getMetaData();
        return getResolutionFactor(MetaDataUtil.getSizeX(meta, serie), MetaDataUtil.getSizeY(meta, serie), wantedSize);
    }
}

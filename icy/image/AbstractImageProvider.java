/**
 * 
 */
package icy.image;

import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ProgressListener;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.sequence.MetaDataUtil;
import icy.system.SystemUtil;
import icy.system.thread.Processor;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    /**
     * Used for multi thread tile image reading.
     * 
     * @author Stephane
     */
    class TileImageReader implements Runnable
    {
        final int serie;
        final int resolution;
        final Rectangle region;
        final int z;
        final int t;
        final int c;
        final IcyBufferedImage result;
        boolean done;
        boolean failed;

        public TileImageReader(int serie, int resolution, Rectangle region, int z, int t, int c, IcyBufferedImage result)
        {
            super();

            this.serie = serie;
            this.resolution = resolution;
            this.region = region;
            this.z = z;
            this.t = t;
            this.c = c;
            this.result = result;
            done = false;
            failed = false;
        }

        public TileImageReader(int serie, int resolution, Rectangle region, int z, int t, IcyBufferedImage result)
        {
            this(serie, resolution, region, z, t, -1, result);
        }

        @Override
        public void run()
        {
            if (Thread.interrupted())
            {
                failed = true;
                return;
            }

            try
            {
                // get image tile
                final IcyBufferedImage img = getImage(serie, resolution, region, z, t, c);
                // compute resolution divider
                final int divider = (int) Math.pow(2, resolution);
                // copy tile to image result
                result.copyData(img, null, new Point(region.x / divider, region.y / divider));
            }
            catch (Exception e)
            {
                failed = true;
            }

            done = true;
        }
    }

    public static final int DEFAULT_THUMBNAIL_SIZE = 160;

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

    // default implementation which use the getImage(..) method, override it for better support / performance
    @Override
    public IcyBufferedImage getThumbnail(int serie) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadataImpl meta = getMetaData();
        int sx = MetaDataUtil.getSizeX(meta, serie);
        int sy = MetaDataUtil.getSizeY(meta, serie);
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
        int resolution = getResolutionFactor(sx, sy, DEFAULT_THUMBNAIL_SIZE);

        // take middle image for thumbnail
        IcyBufferedImage result = getImage(serie, resolution, sz / 2, st / 2);
        
        sx = result.getSizeX();
        sy = result.getSizeY();
        // wanted sub resolution of the image (
        resolution = getResolutionFactor(sx, sy, DEFAULT_THUMBNAIL_SIZE);

        // scale it to desired dimension (fast enough as here we have a small image)
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
     * Returns the image located at specified position using tile by tile reading (if supported by the importer).<br>
     * This method is useful to read a sub resolution of a very large image which cannot fit in memory and also to take
     * advantage of multi threading.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @param c
     *        C position of the image (channel) we want retrieve.<br>
     *        -1 is a special value meaning we want all channel.
     * @param tileW
     *        width of the tile (better to use a multiple of 2)
     * @param tileH
     *        height of the tile (better to use a multiple of 2)
     * @param listener
     *        Progression listener
     */
    public IcyBufferedImage getImageByTile(int serie, int resolution, int z, int t, int c, int tileW, int tileH,
            ProgressListener listener) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadataImpl meta = getMetaData();
        final int sizeX = MetaDataUtil.getSizeX(meta, serie);
        final int sizeY = MetaDataUtil.getSizeY(meta, serie);

        // resolution divider
        final int divider = (int) Math.pow(2, resolution);
        // allocate result
        final IcyBufferedImage result = new IcyBufferedImage(sizeX / divider, sizeY / divider, MetaDataUtil.getSizeC(
                meta, serie), MetaDataUtil.getDataType(meta, serie));
        // create processor
        final Processor readerProcessor = new Processor(Math.max(1, SystemUtil.getNumberOfCPUs() - 1));

        readerProcessor.setThreadName("Image tile reader");
        result.beginUpdate();

        try
        {
            final List<Rectangle> tiles = getTileList(sizeX, sizeY, tileW, tileH);

            // submit all tasks
            for (Rectangle tile : tiles)
            {
                // wait a bit if the process queue is full
                while (readerProcessor.isFull())
                {
                    try
                    {
                        Thread.sleep(0);
                    }
                    catch (InterruptedException e)
                    {
                        // interrupt all processes
                        readerProcessor.shutdownNow();
                        break;
                    }
                }

                // submit next task
                readerProcessor.submit(new TileImageReader(serie, resolution, tile, z, t, c, result));

                // display progression
                if (listener != null)
                {
                    // process cancel requested ?
                    if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size()))
                    {
                        // interrupt processes
                        readerProcessor.shutdownNow();
                        break;
                    }
                }
            }

            // wait for completion
            while (readerProcessor.isProcessing())
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    // interrupt all processes
                    readerProcessor.shutdownNow();
                    break;
                }

                // display progression
                if (listener != null)
                {
                    // process cancel requested ?
                    if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size()))
                    {
                        // interrupt processes
                        readerProcessor.shutdownNow();
                        break;
                    }
                }
            }

            // last wait for completion just in case we were interrupted
            readerProcessor.waitAll();
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Get the list of tiles to fill the given XY plan size.
     * 
     * @param sizeX
     *        plan sizeX
     * @param sizeY
     *        plan sizeY
     * @param tileW
     *        tile width
     * @param tileH
     *        tile height
     */
    public static List<Rectangle> getTileList(int sizeX, int sizeY, int tileW, int tileH)
    {
        final List<Rectangle> result = new ArrayList<Rectangle>();
        int x, y;

        for (y = 0; y < (sizeY - tileH); y += tileH)
        {
            for (x = 0; x < (sizeX - tileW); x += tileW)
                result.add(new Rectangle(x, y, tileW, tileH));
            // last tile column
            result.add(new Rectangle(x, y, sizeX - x, tileH));
        }

        // last tiles row
        for (x = 0; x < (sizeX - tileW); x += tileW)
            result.add(new Rectangle(x, y, tileW, sizeY - y));
        // last column/row tile
        result.add(new Rectangle(x, y, sizeX - x, sizeY - y));

        return result;
    }

    /**
     * Returns the sub image resolution which best suit to the desired size.
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

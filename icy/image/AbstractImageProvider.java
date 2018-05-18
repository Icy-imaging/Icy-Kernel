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
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import ome.xml.meta.OMEXMLMetadata;

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
    class TilePixelsReader implements Runnable
    {
        final int series;
        final int resolution;
        final Rectangle region;
        final int z;
        final int t;
        final int c;
        final Object result;
        final Dimension resDim;
        final boolean signed;

        boolean done;
        boolean failed;

        public TilePixelsReader(int series, int resolution, Rectangle region, int z, int t, int c, Object result,
                Dimension resDim, boolean signed)
        {
            super();

            this.series = series;
            this.resolution = resolution;
            this.region = region;
            this.z = z;
            this.t = t;
            this.c = c;
            this.result = result;
            this.resDim = resDim;
            this.signed = signed;
            done = false;
            failed = false;
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
                final Object obj = getPixels(series, resolution, region, z, t, c);
                // compute resolution divider
                final int divider = (int) Math.pow(2, resolution);
                // copy tile to image result
                Array1DUtil.copyRect(obj, region.getSize(), null, result, resDim,
                        new Point(region.x / divider, region.y / divider), signed);
            }
            catch (Exception e)
            {
                failed = true;
            }

            done = true;
        }
    }

    public static final int DEFAULT_THUMBNAIL_SIZE = 160;

    // default implementation as ImageProvider interface changed
    @SuppressWarnings("deprecation")
    @Override
    public OMEXMLMetadata getOMEXMLMetaData() throws UnsupportedFormatException, IOException
    {
        return getMetaData();
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(int series) throws UnsupportedFormatException, IOException
    {
        return MetaDataUtil.getSizeX(getOMEXMLMetaData(), series);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(int series) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadata meta = getOMEXMLMetaData();
        final int sx = MetaDataUtil.getSizeX(meta, series);

        if (sx == 0)
            return 0;

        // default implementation
        final int maxHeight = (1024 * 1024) / sx;
        final int sy = MetaDataUtil.getSizeY(meta, series);

        return Math.min(maxHeight, sy);
    }

    // default implementation, override it if sub resolution is supported
    @Override
    public boolean isResolutionAvailable(int series, int resolution) throws UnsupportedFormatException, IOException
    {
        // by default we have only the original resolution
        return resolution == 0;
    }

    // default implementation which use the getImage(..) method, override it for better support / performance
    @Override
    public IcyBufferedImage getThumbnail(int series) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadata meta = getOMEXMLMetaData();
        int sx = MetaDataUtil.getSizeX(meta, series);
        int sy = MetaDataUtil.getSizeY(meta, series);
        final int sz = MetaDataUtil.getSizeZ(meta, series);
        final int st = MetaDataUtil.getSizeT(meta, series);

        // empty size --> return null
        if ((sx == 0) || (sy == 0) || (sz == 0) || (st == 0))
            return null;

        final double ratio = Math.min((double) DEFAULT_THUMBNAIL_SIZE / (double) sx,
                (double) DEFAULT_THUMBNAIL_SIZE / (double) sy);

        // final thumbnail size
        final int tnx = (int) Math.round(sx * ratio);
        final int tny = (int) Math.round(sy * ratio);
        final int resolution = getResolutionFactor(sx, sy, DEFAULT_THUMBNAIL_SIZE);

        // take middle image for thumbnail
        final IcyBufferedImage image = getImage(series, resolution, sz / 2, st / 2);

        // sx = result.getSizeX();
        // sy = result.getSizeY();
        // // wanted sub resolution of the image
        // resolution = getResolutionFactor(sx, sy, DEFAULT_THUMBNAIL_SIZE);

        // scale it to desired dimension (fast enough as here we have a small image)
        final IcyBufferedImage thumbnail = IcyBufferedImageUtil.scale(image, tnx, tny, FilterType.BILINEAR);

        // preserve colormaps
        thumbnail.setColorMaps(image);

        return thumbnail;
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(int series, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return getImage(series, resolution, rectangle, z, t, c).getDataXY(0);
    }

    @Override
    public IcyBufferedImage getImage(int series, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return getImage(series, resolution, rectangle, z, t, -1);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(int series, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        return getImage(series, resolution, null, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(int series, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return getImage(series, resolution, null, z, t, -1);
    }

    @Override
    public IcyBufferedImage getImage(int series, int z, int t) throws UnsupportedFormatException, IOException
    {
        return getImage(series, 0, null, z, t, -1);
    }

    @Override
    public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException
    {
        return getImage(0, 0, null, z, t, -1);
    }

    /**
     * Returns the pixels located at specified position using tile by tile reading (if supported by the importer).<br>
     * This method is useful to read a sub resolution of a very large image which cannot fit in memory and also to take
     * advantage of multi threading.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region we want to retrieve (considering the original image resolution).<br>
     *        If set to <code>null</code> then the whole image is returned.
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @param c
     *        C position of the image (channel) we want retrieve (-1 not accepted here).<br>
     * @param tileW
     *        width of the tile (better to use a multiple of 2).<br>
     *        If <= 0 then tile width is automatically determined
     * @param tileH
     *        height of the tile (better to use a multiple of 2).<br>
     *        If <= 0 then tile height is automatically determined
     * @param listener
     *        Progression listener
     * @see #getPixels(int, int, Rectangle, int, int, int)
     */
    public Object getPixelsByTile(int series, int resolution, Rectangle region, int z, int t, int c, int tileW,
            int tileH, ProgressListener listener) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadata meta = getOMEXMLMetaData();
        final int sizeX = MetaDataUtil.getSizeX(meta, series);
        final int sizeY = MetaDataUtil.getSizeY(meta, series);
        final DataType type = MetaDataUtil.getDataType(meta, series);
        final boolean signed = type.isSigned();

        // define XY region to load
        Rectangle adjRegion = new Rectangle(sizeX, sizeY);
        if (region != null)
            adjRegion = adjRegion.intersection(region);

        // allocate result
        final Dimension resDim = new Dimension(adjRegion.width >> resolution, adjRegion.height >> resolution);
        final Object result = Array1DUtil.createArray(type, resDim.width * resDim.height);

        // create processor
        final Processor readerProcessor = new Processor(Math.max(1, SystemUtil.getNumberOfCPUs() - 1));
        readerProcessor.setThreadName("Image tile reader");

        int tw = tileW;
        int th = tileH;

        // adjust tile size if needed
        if (tw <= 0)
            tw = getTileWidth(series);
        if (tw <= 0)
            tw = 512;
        if (th <= 0)
            th = getTileHeight(series);
        if (th <= 0)
            th = 512;

        final List<Rectangle> tiles = ImageUtil.getTileList(adjRegion, tw, th);

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
            readerProcessor.submit(new TilePixelsReader(series, resolution, tile.intersection(adjRegion), z, t, c,
                    result, resDim, signed));

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

        return result;
    }

    // /**
    // * Returns the image located at specified position using tile by tile reading (if supported by the importer).<br>
    // * This method is useful to read a sub resolution of a very large image which cannot fit in memory and also to take
    // * advantage of multi threading.
    // *
    // * @param series
    // * Series index for multi series image (use 0 if unsure).
    // * @param resolution
    // * Wanted resolution level for the image (use 0 if unsure).<br>
    // * The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
    // * So for instance level 0 is the default image resolution while level 1 is base image
    // * resolution / 2 and so on...
    // * @param region
    // * The 2D region we want to retrieve (considering the original image resolution).<br>
    // * If set to <code>null</code> then the whole image is returned.
    // * @param z
    // * Z position of the image (slice) we want retrieve
    // * @param t
    // * T position of the image (frame) we want retrieve
    // * @param c
    // * C position of the image (channel) we want retrieve.<br>
    // * -1 is a special value meaning we want all channel.
    // * @param tileW
    // * width of the tile (better to use a multiple of 2)
    // * @param tileH
    // * height of the tile (better to use a multiple of 2)
    // * @param listener
    // * Progression listener
    // * @see #getImage(int, int, Rectangle, int, int, int)
    // */
    // public IcyBufferedImage getImageByTile(int series, int resolution, Rectangle region, int z, int t, int c, int tileW,
    // int tileH, ProgressListener listener) throws UnsupportedFormatException, IOException
    // {
    // final OMEXMLMetadata meta = getOMEXMLMetaData();
    // final int sizeX = MetaDataUtil.getSizeX(meta, series);
    // final int sizeY = MetaDataUtil.getSizeY(meta, series);
    //
    // // TODO: handle rect
    //
    // // resolution divider
    // final int divider = (int) Math.pow(2, resolution);
    // // allocate result
    // final IcyBufferedImage result = new IcyBufferedImage(sizeX / divider, sizeY / divider,
    // MetaDataUtil.getSizeC(meta, series), MetaDataUtil.getDataType(meta, series));
    // // create processor
    // final Processor readerProcessor = new Processor(Math.max(1, SystemUtil.getNumberOfCPUs() - 1));
    //
    // readerProcessor.setThreadName("Image tile reader");
    // result.beginUpdate();
    //
    // try
    // {
    // final List<Rectangle> tiles = ImageUtil.getTileList(sizeX, sizeY, tileW, tileH);
    //
    // // submit all tasks
    // for (Rectangle tile : tiles)
    // {
    // // wait a bit if the process queue is full
    // while (readerProcessor.isFull())
    // {
    // try
    // {
    // Thread.sleep(0);
    // }
    // catch (InterruptedException e)
    // {
    // // interrupt all processes
    // readerProcessor.shutdownNow();
    // break;
    // }
    // }
    //
    // // submit next task
    // readerProcessor.submit(new TileImageReader(series, resolution, tile, z, t, c, result));
    //
    // // display progression
    // if (listener != null)
    // {
    // // process cancel requested ?
    // if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size()))
    // {
    // // interrupt processes
    // readerProcessor.shutdownNow();
    // break;
    // }
    // }
    // }
    //
    // // wait for completion
    // while (readerProcessor.isProcessing())
    // {
    // try
    // {
    // Thread.sleep(1);
    // }
    // catch (InterruptedException e)
    // {
    // // interrupt all processes
    // readerProcessor.shutdownNow();
    // break;
    // }
    //
    // // display progression
    // if (listener != null)
    // {
    // // process cancel requested ?
    // if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size()))
    // {
    // // interrupt processes
    // readerProcessor.shutdownNow();
    // break;
    // }
    // }
    // }
    //
    // // last wait for completion just in case we were interrupted
    // readerProcessor.waitAll();
    // }
    // finally
    // {
    // result.endUpdate();
    // }
    //
    // return result;
    // }

    // /**
    // * @deprecated Use {@link #getImageByTile(int, int, Rectangle, int, int, int, int, int, ProgressListener)} instead.
    // */
    // @Deprecated
    // public IcyBufferedImage getImageByTile(int series, int resolution, int z, int t, int c, int tileW, int tileH,
    // ProgressListener listener) throws UnsupportedFormatException, IOException
    // {
    // return getImageByTile(series, resolution, null, z, t, c, tileW, tileH, listener);
    // }
    //
    // /**
    // * @deprecated USe {@link ImageUtil#getTileList(int, int, int, int)} instead
    // */
    // @Deprecated
    // public static List<Rectangle> getTileList(int sizeX, int sizeY, int tileW, int tileH)
    // {
    // return ImageUtil.getTileList(sizeX, sizeY, tileW, tileH);
    // }

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
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param wantedSize
     *        wanted size (for the maximum dimension)
     * @return resolution ratio<br>
     *         0 = original resolution<br>
     *         1 = (original resolution / 2)<br>
     *         2 = (original resolution / 4)
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public int getResolutionFactor(int series, int wantedSize) throws UnsupportedFormatException, IOException
    {
        final OMEXMLMetadata meta = getOMEXMLMetaData();
        return getResolutionFactor(MetaDataUtil.getSizeX(meta, series), MetaDataUtil.getSizeY(meta, series),
                wantedSize);
    }
}

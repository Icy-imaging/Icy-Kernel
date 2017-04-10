/**
 * 
 */
package plugins.kernel.importer;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.filechooser.FileFilter;

import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ProgressListener;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.dialog.LoaderDialog.AllImagesFileFilter;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.LinearColorMap;
import icy.plugin.abstract_.PluginSequenceFileImporter;
import icy.sequence.MetaDataUtil;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ByteArrayConvert;
import icy.util.ColorUtil;
import icy.util.StringUtil;
import jxl.biff.drawing.PNGReader;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MissingLibraryException;
import loci.formats.UnknownFormatException;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.ExtensionFileFilter;
import loci.formats.in.APNGReader;
import loci.formats.in.JPEG2000Reader;
import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * LOCI Bio-Formats library importer class.
 * 
 * @author Stephane
 */
public class LociImporterPlugin extends PluginSequenceFileImporter
{
    protected class LociAllFileFilter extends AllImagesFileFilter
    {
        @Override
        public String getDescription()
        {
            return "All image files / Bio-Formats";
        }
    };

    /**
     * Used for multi thread tile image reading.
     * 
     * @author Stephane
     */
    class LociTileImageReader
    {
        class WorkBuffer
        {
            final byte[] rawBuffer;
            final byte[] channelBuffer;
            final Object[] pixelBuffer;

            public WorkBuffer(int sizeX, int sizeY, int sizeC, int rgbChannel, DataType dataType)
            {
                super();

                // allocate arrays
                rawBuffer = new byte[sizeX * sizeY * rgbChannel * dataType.getSize()];
                channelBuffer = new byte[sizeX * sizeY * dataType.getSize()];
                pixelBuffer = Array2DUtil.createArray(dataType, sizeC);
                for (int i = 0; i < sizeC; i++)
                    pixelBuffer[i] = Array1DUtil.createArray(dataType, sizeX * sizeY);
            }
        }

        class TileReaderWorker implements Runnable
        {
            final Rectangle region;
            boolean done;
            boolean failed;

            public TileReaderWorker(Rectangle region)
            {
                super();

                this.region = region;
                done = false;
                failed = false;
            }

            @SuppressWarnings("resource")
            @Override
            public void run()
            {
                IcyBufferedImage img;

                try
                {
                    // get reader and working buffers
                    final IFormatReader r = getReader();
                    final WorkBuffer buf = buffers.pop();

                    try
                    {
                        try
                        {
                            // get image tile
                            if (c == -1)
                            {
                                img = getImageInternal(r, region, z, t, false, buf.rawBuffer, buf.channelBuffer,
                                        buf.pixelBuffer);
                            }
                            else
                            {
                                img = getImageInternal(r, region, z, t, c, false, buf.rawBuffer, buf.channelBuffer,
                                        buf.pixelBuffer);
                            }
                        }
                        finally
                        {
                            // release reader
                            releaseReader(r);
                        }

                        // downscale image if needed
                        img = downScale(img, downScaleLevel);
                        // copy tile to image result
                        result.copyData(img, null, new Point(region.x / resDivider, region.y / resDivider));
                    }
                    finally
                    {
                        // release working buffer
                        buffers.push(buf);
                    }
                }
                catch (Exception e)
                {
                    failed = true;
                }

                done = true;
            }
        }

        // required image down scaling
        final int downScaleLevel;
        // resolution divider
        final int resDivider;
        final int z;
        final int t;
        final int c;
        final IcyBufferedImage result;
        final Stack<WorkBuffer> buffers;

        public LociTileImageReader(int serie, int resolution, int z, int t, int c, int tileW, int tileH,
                ProgressListener listener) throws IOException, UnsupportedFormatException
        {
            super();

            this.z = z;
            this.t = t;
            this.c = c;

            final OMEXMLMetadataImpl meta = getMetaData();
            final int sizeX = MetaDataUtil.getSizeX(meta, serie);
            final int sizeY = MetaDataUtil.getSizeY(meta, serie);
            final int numThread = Math.max(1, SystemUtil.getNumberOfCPUs() - 1);

            // prepare main reader and get needed downScale
            downScaleLevel = prepareReader(serie, resolution);
            // resolution divider
            resDivider = (int) Math.pow(2, resolution);
            // allocate result
            result = new IcyBufferedImage(sizeX / resDivider, sizeY / resDivider, MetaDataUtil.getSizeC(meta, serie),
                    MetaDataUtil.getDataType(meta, serie));

            // allocate working buffers
            final int sizeC = MetaDataUtil.getSizeC(meta, serie);
            final int rgbChannelCount = reader.getRGBChannelCount();
            final DataType dataType = MetaDataUtil.getDataType(meta, serie);

            buffers = new Stack<WorkBuffer>();
            for (int i = 0; i < numThread; i++)
                buffers.push(new WorkBuffer(tileW, tileH, sizeC, rgbChannelCount, dataType));

            // create processor
            final Processor readerProcessor = new Processor(numThread);

            readerProcessor.setThreadName("Image tile reader");
            // to avoid multiple update
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
                    readerProcessor.submit(new TileReaderWorker(tile));

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

            // faster memory release
            buffers.clear();
        }
    }

    /**
     * Main image reader used to retrieve a specific format reader
     */
    protected final ImageReader mainReader;
    /**
     * Current format reader
     */
    protected IFormatReader reader;

    /**
     * Shared readers for multi threading
     */
    protected final List<IFormatReader> readersPool;

    /**
     * Advanced settings
     */
    protected boolean originalMetadata;
    protected boolean groupFiles;

    public LociImporterPlugin()
    {
        super();

        mainReader = new ImageReader();
        // just to be sure
        mainReader.setAllowOpenFiles(true);

        reader = null;
        readersPool = new ArrayList<IFormatReader>();

        originalMetadata = false;
        groupFiles = false;
    }

    protected void setReader(String path) throws FormatException, IOException
    {
        // no reader defined so just get the good one
        if (reader == null)
            reader = mainReader.getReader(path);
        else
        {
            // don't check if the file is currently opened
            if (!isOpen(path))
            {
                // try to check with extension only first then open it if needed
                if (!reader.isThisType(path, false) && !reader.isThisType(path, true))
                    reader = mainReader.getReader(path);
            }
        }
    }

    protected void reportError(final String title, final String message, final String filename)
    {
        // TODO: enable that when LOCI will be ready
        // ThreadUtil.invokeLater(new Runnable()
        // {
        // @Override
        // public void run()
        // {
        // final ErrorReportFrame errorFrame = new ErrorReportFrame(null, title, message);
        //
        // errorFrame.setReportAction(new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // try
        // {
        // OMEUtil.reportLociError(filename, errorFrame.getReportMessage());
        // }
        // catch (BadLocationException e1)
        // {
        // System.err.println("Error while sending report:");
        // IcyExceptionHandler.showErrorMessage(e1, false, true);
        // }
        // }
        // });
        // }
        // });
    }

    /**
     * When set to <code>true</code> the importer will also read original metadata (as
     * annotations)
     * 
     * @return the readAllMetadata state<br>
     * @see #setReadOriginalMetadata(boolean)
     */
    public boolean getReadOriginalMetadata()
    {
        return originalMetadata;
    }

    /**
     * When set to <code>true</code> the importer will also read original metadata (as
     * annotations)
     */
    public void setReadOriginalMetadata(boolean value)
    {
        originalMetadata = value;
    }

    /**
     * When set to <code>true</code> the importer will try to group files required for the whole
     * dataset.
     * 
     * @return the groupFiles
     */
    public boolean isGroupFiles()
    {
        return groupFiles;
    }

    /**
     * When set to <code>true</code> the importer will try to group files required for the whole
     * dataset.
     */
    public void setGroupFiles(boolean value)
    {
        groupFiles = value;
    }

    @Override
    public List<FileFilter> getFileFilters()
    {
        final List<FileFilter> result = new ArrayList<FileFilter>();

        result.add(new LociAllFileFilter());
        result.add(new ExtensionFileFilter(new String[] {"tif", "tiff"}, "TIFF images / Bio-Formats"));
        result.add(new ExtensionFileFilter(new String[] {"png"}, "PNG images / Bio-Formats"));
        result.add(new ExtensionFileFilter(new String[] {"jpg", "jpeg"}, "JPEG images / Bio-Formats"));
        result.add(new ExtensionFileFilter(new String[] {"avi"}, "AVI videos / Bio-Formats"));

        // final IFormatReader[] readers = mainReader.getReaders();

        // for (IFormatReader reader : readers)
        // result.add(new FormatFileFilter(reader, true));

        return result;
    }

    @Override
    public boolean acceptFile(String path)
    {
        // easy discard
        if (Loader.canDiscardImageFile(path))
            return false;

        try
        {
            // better for Bio-Formats to have system path format (bug with Bio-Format?)
            final String adjPath = new File(path).getAbsolutePath();

            // this method should not modify the current reader !

            // no reader defined or not the same type --> try to obtain the reader for this file
            if ((reader == null) || (!reader.isThisType(adjPath, false) && !reader.isThisType(adjPath, true)))
                mainReader.getReader(adjPath);

            return true;
        }
        catch (Exception e)
        {
            // assume false on exception (FormatException or IOException)
            return false;
        }
    }

    public boolean isOpen(String path)
    {
        return StringUtil.equals(getOpened(), FileUtil.getGenericPath(path));
    }

    @Override
    public String getOpened()
    {
        if (reader != null)
            return FileUtil.getGenericPath(reader.getCurrentFile());

        return null;
    }

    @Override
    public boolean open(String path, int flags) throws UnsupportedFormatException, IOException
    {
        // already opened ?
        if (isOpen(path))
            return true;

        // close first
        close();

        try
        {
            // better for Bio-Formats to have system path format
            final String adjPath = new File(path).getAbsolutePath();

            // ensure we have the correct reader
            setReader(adjPath);

            // disable file grouping
            reader.setGroupFiles(groupFiles);
            // we want all metadata
            reader.setOriginalMetadataPopulated(originalMetadata);
            // prepare meta data store structure
            reader.setMetadataStore(new OMEXMLMetadataImpl());
            // load file with LOCI library
            reader.setId(adjPath);

            // set reader in reader pool
            synchronized (readersPool)
            {
                readersPool.add(reader);
            }

            return true;
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
    }

    @Override
    public void close() throws IOException
    {
        // something to close ?
        if (getOpened() != null)
        {
            synchronized (readersPool)
            {
                // close all readers
                for (IFormatReader r : readersPool)
                    r.close();

                readersPool.clear();
            }
        }
    }

    /**
     * Clone the current used reader conserving its properties and current path
     */
    protected IFormatReader cloneReader()
            throws FormatException, IOException, InstantiationException, IllegalAccessException
    {
        if (reader == null)
            return null;

        // create the new reader instance
        final IFormatReader result = reader.getClass().newInstance();

        // get opened file
        final String path = getOpened();

        if (path != null)
        {
            // better for Bio-Formats to have system path format
            final String adjPath = new File(path).getAbsolutePath();

            // disable file grouping
            result.setGroupFiles(groupFiles);
            // we want all metadata
            result.setOriginalMetadataPopulated(originalMetadata);
            // prepare meta data store structure
            result.setMetadataStore(new OMEXMLMetadataImpl());
            // load file with LOCI library
            result.setId(adjPath);

            // preserve serie and resolution info
            result.setSeries(reader.getSeries());
            result.setResolution(reader.getResolution());
        }

        return result;
    }

    /**
     * Returns a reader to use for the current thread (allocate it if needed).<br>
     * Any obtained reader should be released using {@link #releaseReader(IFormatReader)}
     * 
     * @see #releaseReader(IFormatReader)
     */
    public IFormatReader getReader() throws FormatException, IOException
    {
        try
        {
            synchronized (readersPool)
            {
                if (readersPool.isEmpty())
                    readersPool.add(cloneReader());
                // allocate last reader (faster)
                return readersPool.remove(readersPool.size() - 1);
            }
        }
        catch (InstantiationException e)
        {
            // better to rethrow as RuntimeException
            throw new RuntimeException(e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            // better to rethrow as RuntimeException
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Release the reader obtained through {@link #getReader()} to the reader pool.
     * 
     * @see #getReader()
     */
    public void releaseReader(IFormatReader r)
    {
        synchronized (readersPool)
        {
            readersPool.add(r);
        }
    }

    /**
     * Prepare the reader to read data from specified serie and at specified resolution.<br>
     * 
     * @return the image divisor factor to match the wanted resolution if needed
     */
    protected int prepareReader(int serie, int resolution)
    {
        final int resCount;
        final int res;

        // set wanted serie
        reader.setSeries(serie);

        // set wanted resolution
        resCount = reader.getResolutionCount();
        if (resolution >= resCount)
            res = resCount - 1;
        else
            res = resolution;
        reader.setResolution(res);

        return resolution - res;
    }

    @Override
    public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException
    {
        // no image currently opened
        if (getOpened() == null)
            return null;

        // don't need thread safe reader for this
        return (OMEXMLMetadataImpl) reader.getMetadataStore();
    }

    @Override
    public int getTileWidth(int serie) throws UnsupportedFormatException, IOException
    {
        // no image currently opened
        if (getOpened() == null)
            return 0;

        // prepare reader
        prepareReader(serie, 0);

        // don't need thread safe reader for this
        return reader.getOptimalTileWidth();
    }

    @Override
    public int getTileHeight(int serie) throws UnsupportedFormatException, IOException
    {
        // no image currently opened
        if (getOpened() == null)
            return 0;

        // prepare reader
        prepareReader(serie, 0);

        // don't need thread safe reader for this
        return reader.getOptimalTileHeight();
    }

    @SuppressWarnings("resource")
    @Override
    public IcyBufferedImage getThumbnail(int serie) throws UnsupportedFormatException, IOException
    {
        // no image currently opened
        if (getOpened() == null)
            return null;

        try
        {
            // prepare reader (no down scaling here)
            prepareReader(serie, 0);

            final IFormatReader r = getReader();
            try
            {
                // get image
                return getThumbnail(reader, reader.getSizeZ() / 2, reader.getSizeT() / 2);
            }
            finally
            {
                releaseReader(r);
            }
        }
        catch (FormatException e)
        {
            throw translateException(getOpened(), e);
        }
        catch (Throwable t)
        {
            // can happen if we don't have enough memory --> try default implementation
            return super.getThumbnail(serie);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public Object getPixels(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        // no image currently opened
        if (getOpened() == null)
            return null;

        try
        {
            // prepare reader and get down scale factor
            final int downScaleLevel = prepareReader(serie, resolution);

            // no need to rescale ? --> directly return the pixels
            if (downScaleLevel == 0)
            {
                final Object result;
                final IFormatReader r = getReader();

                try
                {
                    // get pixels
                    result = getPixelsInternal(reader, rectangle, z, t, c, false);
                }
                finally
                {
                    releaseReader(r);
                }

                return result;
            }

            // use classic getImage method when we need rescaling
            return getImage(serie, resolution, rectangle, z, t, c).getDataXY(0);
        }
        catch (FormatException e)
        {
            throw translateException(getOpened(), e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        // no image currently opened
        if (getOpened() == null)
            return null;

        try
        {
            // prepare reader and get down scale factor if wanted resolution is not available
            final int downScaleLevel = prepareReader(serie, resolution);

            final IFormatReader r = getReader();
            try
            {
                // get image
                final IcyBufferedImage result = getImage(reader, rectangle, z, t, c);
                // return down scaled version if needed
                return downScale(result, downScaleLevel);
            }
            // not enough memory error ?
            catch (OutOfMemoryError e)
            {
                // need rescaling --> try tiling read
                if (downScaleLevel > 0)
                    return getImageByTile(serie, resolution, z, t, c, getTileWidth(serie), getTileHeight(serie), null);

                throw e;
            }
            // too large XY plan ?
            catch (UnsupportedOperationException e)
            {
                // need rescaling --> try tiling read
                if (downScaleLevel > 0)
                    return getImageByTile(serie, resolution, z, t, c, getTileWidth(serie), getTileHeight(serie), null);

                throw e;
            }
            catch (FormatException e)
            {
                // we can have here a "Image plane too large. Only 2GB of data can be extracted at
                // one time." error here --> so can try to use tile loading when we need rescaling
                if (downScaleLevel > 0)
                    return getImageByTile(serie, resolution, z, t, c, getTileWidth(serie), getTileHeight(serie), null);

                throw e;
            }
            catch (IOException e)
            {
                throw e;
            }
            finally
            {
                releaseReader(r);
            }
        }
        catch (FormatException e)
        {
            throw translateException(getOpened(), e);
        }
    }

    @Override
    public IcyBufferedImage getImageByTile(int serie, int resolution, int z, int t, int c, int tileW, int tileH,
            ProgressListener listener) throws UnsupportedFormatException, IOException
    {
        return new LociTileImageReader(serie, resolution, z, t, c, tileW, tileH, listener).result;
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and
     * returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnail(IFormatReader reader, int z, int t)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        return getThumbnail(reader, z, t, -1);
    }

    /**
     * Load a thumbnail version of the image located at (Z, T, C) position from the specified
     * {@link IFormatReader} and
     * returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the thumbnail to load
     * @param t
     *        T position of the thumbnail to load
     * @param c
     *        Channel index
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnail(IFormatReader reader, int z, int t, int c)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        try
        {
            // all channel ?
            if (c == -1)
                return getImageInternal(reader, null, z, t, true);

            return getImageInternal(reader, null, z, t, c, true);
        }
        catch (ClosedByInterruptException e)
        {
            // loading interrupted --> return null
            return null;
        }
        catch (Exception e)
        {
            // LOCI do not support thumbnail for all image, try compatible version
            return getThumbnailCompatible(reader, z, t, c);
        }
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and
     * returns it as an IcyBufferedImage.<br>
     * <i>Slow compatible version (load the original image and resize it)</i>
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnailCompatible(IFormatReader reader, int z, int t)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        return getThumbnailCompatible(reader, z, t, -1);
    }

    /**
     * Load a thumbnail version of the image located at (Z, T, C) position from the specified
     * {@link IFormatReader} and
     * returns it as an IcyBufferedImage.<br>
     * <i>Slow compatible version (load the original image and resize it)</i>
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the thumbnail to load
     * @param t
     *        T position of the thumbnail to load
     * @param c
     *        Channel index
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnailCompatible(IFormatReader reader, int z, int t, int c)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        return IcyBufferedImageUtil.scale(getImage(reader, null, z, t, c), reader.getThumbSizeX(),
                reader.getThumbSizeY());
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified
     * {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        Reader used to load the image
     * @param rect
     *        Region we want to retrieve data.<br>
     *        Set to <code>null</code> to retrieve the whole image.
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @param c
     *        Channel index to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImage(IFormatReader reader, Rectangle rect, int z, int t, int c)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        // we want all channel ? use method to retrieve whole image
        if (c == -1)
            return getImageInternal(reader, rect, z, t, false);

        return getImageInternal(reader, rect, z, t, c, false);
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param rect
     *        Region we want to retrieve data.<br>
     *        Set to <code>null</code> to retrieve the whole image.
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImage(IFormatReader reader, Rectangle rect, int z, int t)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        return getImageInternal(reader, rect, z, t, false);
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage (compatible and slower method).
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImageCompatible(IFormatReader reader, int z, int t)
            throws FormatException, IOException
    {
        final int sizeX = reader.getSizeX();
        final int sizeY = reader.getSizeY();
        final List<BufferedImage> imageList = new ArrayList<BufferedImage>();
        final int sizeC = reader.getEffectiveSizeC();

        for (int c = 0; c < sizeC; c++)
            imageList.add(AWTImageTools.openImage(reader.openBytes(reader.getIndex(z, c, t)), reader, sizeX, sizeY));

        // combine channels
        return IcyBufferedImage.createFrom(imageList);

    }

    /**
     * Load pixels of the specified region of image at (Z, T, C) position and returns them as an
     * array.
     * 
     * @param reader
     *        Reader used to load the pixels
     * @param dataType
     *        pixel data type
     * @param rect
     *        Define the image rectangular region we want to load.<br>
     *        Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param z
     *        Z position of the pixels to load
     * @param t
     *        T position of the pixels to load
     * @param c
     *        Channel index to load
     * @param thumbnail
     *        Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i>
     *        parameter should
     *        contains thumbnail size
     * @param rawBuffer
     *        pre allocated byte data buffer ([reader.getRGBChannelCount() * SizeX * SizeY *
     *        Datatype.size]) used to
     *        read the whole RGB raw data (can be <code>null</code>)
     * @param channelBuffer
     *        pre allocated byte data buffer ([SizeX * SizeY * Datatype.size]) used to read the
     *        channel raw data (can be
     *        <code>null</code>)
     * @param pixelBuffer
     *        pre allocated 1D array pixel data buffer ([SizeX * SizeY]) used to receive the pixel
     *        converted data and to
     *        build the result image (can be <code>null</code>)
     * @return 1D array containing pixels data.<br>
     *         The type of the array depends from the internal image data type
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    protected static Object getPixelsInternal(IFormatReader reader, Rectangle rect, int z, int t, int c,
            boolean thumbnail, byte[] rawBuffer, byte[] channelBuffer, Object pixelBuffer)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        // get pixel data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());

        // check we can open the image
        // Loader.checkOpening(reader.getResolution(), rect.width, rect.height, 1, 1, 1, dataType,
        // "");

        // prepare informations
        final int rgbChanCount = reader.getRGBChannelCount();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // allocate internal image data array if needed
        final Object result = Array1DUtil.allocIfNull(pixelBuffer, dataType, rect.width * rect.height);
        // compute channel offsets
        final int baseC = c / rgbChanCount;
        final int subC = c % rgbChanCount;

        // get image data (whole RGB data for RGB channel)
        byte[] rawData = getBytesInternal(reader, reader.getIndex(z, baseC, t), rect, thumbnail, rawBuffer);

        // current final component
        final int componentByteLen = rawData.length / rgbChanCount;

        // build data array
        if (interleaved)
        {
            // get channel interleaved data
            final byte[] channelData = Array1DUtil.getInterleavedData(rawData, subC, rgbChanCount, channelBuffer, 0,
                    componentByteLen);
            ByteArrayConvert.byteArrayTo(channelData, 0, result, 0, componentByteLen, little);
        }
        else
            ByteArrayConvert.byteArrayTo(rawData, subC * componentByteLen, result, 0, componentByteLen, little);

        // return raw pixels data
        return result;
    }

    /**
     * Load pixels of the specified region of image at (Z, T, C) position and returns them as an
     * array.
     * 
     * @param reader
     *        Reader used to load the pixels
     * @param rect
     *        Region we want to retrieve data.<br>
     *        Set to <code>null</code> to retrieve the whole image.
     * @param z
     *        Z position of the pixels to load
     * @param t
     *        T position of the pixels to load
     * @param c
     *        Channel index to load
     * @param thumbnail
     *        Set to <code>true</code> to request a thumbnail of the image (<code>rect</code>
     *        parameter is then ignored)
     * @return 1D array containing pixels data.<br>
     *         The type of the array depends from the internal image data type
     */
    protected static Object getPixelsInternal(IFormatReader reader, Rectangle rect, int z, int t, int c,
            boolean thumbnail) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        final Rectangle r;

        if (thumbnail)
            r = new Rectangle(0, 0, reader.getThumbSizeX(), reader.getThumbSizeY());
        else if (rect == null)
            r = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());
        else
            r = rect;

        return getPixelsInternal(reader, r, z, t, c, thumbnail, null, null, null);
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified
     * {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        Reader used to load the image
     * @param rect
     *        Define the image rectangular region we want to load.<br>
     *        Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @param c
     *        Channel index to load
     * @param thumbnail
     *        Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i>
     *        parameter should
     *        contains thumbnail size
     * @param rawBuffer
     *        pre allocated byte data buffer ([reader.getRGBChannelCount() * SizeX * SizeY *
     *        Datatype.size]) used to
     *        read the whole RGB raw data (can be <code>null</code>)
     * @param channelBuffer
     *        pre allocated byte data buffer ([SizeX * SizeY * Datatype.size]) used to read the
     *        channel raw data (can be
     *        <code>null</code>)
     * @param pixelBuffer
     *        pre allocated 1D array pixel data buffer ([SizeX * SizeY]) used to receive the pixel
     *        converted data and to
     *        build the result image (can be <code>null</code>)
     * @return {@link IcyBufferedImage}
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    protected static IcyBufferedImage getImageInternal(IFormatReader reader, Rectangle rect, int z, int t, int c,
            boolean thumbnail, byte[] rawBuffer, byte[] channelBuffer, Object pixelBuffer)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        // get pixel data
        final Object pixelData = getPixelsInternal(reader, rect, z, t, c, thumbnail, rawBuffer, channelBuffer,
                pixelBuffer);
        // get pixel data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // create the single channel result image from pixel data
        final IcyBufferedImage result = new IcyBufferedImage(rect.width, rect.height, pixelData, dataType.isSigned());

        // indexed color ?
        if (reader.isIndexed())
        {
            IcyColorMap map = null;

            // only 8 bits and 16 bits lookup table supported
            switch (dataType.getJavaType())
            {
                case BYTE:
                    final byte[][] bmap = reader.get8BitLookupTable();
                    if (bmap != null)
                        map = new IcyColorMap("Channel " + c, bmap);
                    break;

                case SHORT:
                    final short[][] smap = reader.get16BitLookupTable();
                    if (smap != null)
                        map = new IcyColorMap("Channel " + c, smap);
                    break;

                default:
                    break;
            }

            // colormap not set (or black) ? --> try to use metadata
            if ((map == null) || map.isBlack())
            {
                final OMEXMLMetadataImpl metaData = (OMEXMLMetadataImpl) reader.getMetadataStore();
                final Color color = MetaDataUtil.getChannelColor(metaData, reader.getSeries(), c);

                if ((color != null) && !ColorUtil.isBlack(color))
                    map = new LinearColorMap("Channel " + c, color);
                else
                    map = null;
            }

            // we were able to retrieve a colormap ? --> set it
            if (map != null)
                result.setColorMap(0, map, true);
        }

        return result;
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified
     * {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        Reader used to load the image
     * @param rect
     *        Region we want to retrieve data.<br>
     *        Set to <code>null</code> to retrieve the whole image.
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @param c
     *        Channel index to load
     * @param thumbnail
     *        Set to <code>true</code> to request a thumbnail of the image (<code>rect</code>
     *        parameter is then ignored)
     * @return {@link IcyBufferedImage}
     */
    protected static IcyBufferedImage getImageInternal(IFormatReader reader, Rectangle rect, int z, int t, int c,
            boolean thumbnail) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        final Rectangle r;

        if (thumbnail)
            r = new Rectangle(0, 0, reader.getThumbSizeX(), reader.getThumbSizeY());
        else if (rect == null)
            r = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());
        else
            r = rect;

        return getImageInternal(reader, r, z, t, c, thumbnail, null, null, null);
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader and return it as
     * an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param rect
     *        Define the image rectangular region we want to load.<br>
     *        Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @param thumbnail
     *        Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i>
     *        parameter should
     *        contains thumbnail size
     * @param rawBuffer
     *        pre allocated byte data buffer ([reader.getRGBChannelCount() * SizeX * SizeY *
     *        Datatype.size]) used to
     *        read the whole RGB raw data (can be <code>null</code>)
     * @param channelBuffer
     *        pre allocated byte data buffer ([SizeX * SizeY * Datatype.size]) used to read the
     *        channel raw data (can be
     *        <code>null</code>)
     * @param pixelBuffer
     *        pre allocated 2D array ([SizeC, SizeX*SizeY]) pixel data buffer used to receive the
     *        pixel converted data
     *        and to build the result image (can be <code>null</code>)
     * @return {@link IcyBufferedImage}
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    protected static IcyBufferedImage getImageInternal(IFormatReader reader, Rectangle rect, int z, int t,
            boolean thumbnail, byte[] rawBuffer, byte[] channelBuffer, Object[] pixelBuffer)
            throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException
    {
        // get pixel data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // get sizeC
        final int effSizeC = reader.getEffectiveSizeC();
        final int rgbChanCount = reader.getRGBChannelCount();
        final int sizeX = rect.width;
        final int sizeY = rect.height;
        final int sizeC = effSizeC * rgbChanCount;

        // check we can open the image
        // Loader.checkOpening(reader.getResolution(), sizeX, sizeY, sizeC, 1, 1, dataType, "");

        final int serie = reader.getSeries();
        // prepare informations
        final boolean indexed = reader.isIndexed();
        final boolean little = reader.isLittleEndian();
        final OMEXMLMetadataImpl metaData = (OMEXMLMetadataImpl) reader.getMetadataStore();

        // prepare internal image data array
        final Object[] pixelData;

        if (pixelBuffer == null)
        {
            // allocate array
            pixelData = Array2DUtil.createArray(dataType, sizeC);
            for (int i = 0; i < sizeC; i++)
                pixelData[i] = Array1DUtil.createArray(dataType, sizeX * sizeY);
        }
        else
            pixelData = pixelBuffer;

        // colormap allocation
        final IcyColorMap[] colormaps = new IcyColorMap[effSizeC];

        byte[] rawData = null;
        for (int effC = 0; effC < effSizeC; effC++)
        {
            // get data
            rawData = getBytesInternal(reader, reader.getIndex(z, effC, t), rect, thumbnail, rawBuffer);

            // current final component
            final int c = effC * rgbChanCount;
            final int componentByteLen = rawData.length / rgbChanCount;

            // build data array
            int inOffset = 0;
            if (reader.isInterleaved())
            {
                final byte[] channelData = (channelBuffer == null) ? new byte[componentByteLen] : channelBuffer;

                for (int sc = 0; sc < rgbChanCount; sc++)
                {
                    // get channel interleaved data
                    Array1DUtil.getInterleavedData(rawData, inOffset, rgbChanCount, channelData, 0, componentByteLen);
                    ByteArrayConvert.byteArrayTo(channelData, 0, pixelData[c + sc], 0, componentByteLen, little);
                    inOffset++;
                }
            }
            else
            {
                for (int sc = 0; sc < rgbChanCount; sc++)
                {
                    ByteArrayConvert.byteArrayTo(rawData, inOffset, pixelData[c + sc], 0, componentByteLen, little);
                    inOffset += componentByteLen;
                }
            }

            // indexed color ?
            if (indexed)
            {
                // only 8 bits and 16 bits lookup table supported
                switch (dataType.getJavaType())
                {
                    case BYTE:
                        final byte[][] bmap = reader.get8BitLookupTable();
                        if (bmap != null)
                            colormaps[effC] = new IcyColorMap("Channel " + effC, bmap);
                        break;

                    case SHORT:
                        final short[][] smap = reader.get16BitLookupTable();
                        if (smap != null)
                            colormaps[effC] = new IcyColorMap("Channel " + effC, smap);
                        break;

                    default:
                        colormaps[effC] = null;
                        break;
                }
            }

            // colormap not yet set (or black) ? --> try to use metadata
            if ((colormaps[effC] == null) || colormaps[effC].isBlack())
            {
                final Color color = MetaDataUtil.getChannelColor(metaData, serie, effC);

                if ((color != null) && !ColorUtil.isBlack(color))
                    colormaps[effC] = new LinearColorMap("Channel " + effC, color);
                else
                    colormaps[effC] = null;
            }
        }

        final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, pixelData, dataType.isSigned());

        // affect colormap
        result.beginUpdate();
        try
        {
            // set colormaps
            for (int comp = 0; comp < effSizeC; comp++)
            {
                // we were able to retrieve a colormap for that channel ? --> set it
                if (colormaps[comp] != null)
                    result.setColorMap(comp, colormaps[comp], true);
            }

            // special case of 4 channels image, try to restore alpha channel
            if ((sizeC == 4) && ((colormaps.length < 4) || (colormaps[3] == null)))
            {
                // assume real alpha channel depending from the reader we use
                final boolean alpha = (rgbChanCount == 4) || (reader instanceof PNGReader)
                        || (reader instanceof APNGReader) || (reader instanceof JPEG2000Reader);

                // restore alpha channel
                if (alpha)
                    result.setColorMap(3, LinearColorMap.alpha_, true);
            }
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param rect
     *        Region we want to retrieve data.<br>
     *        Set to <code>null</code> to retrieve the whole image.
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @param thumbnail
     *        Set to <code>true</code> to request a thumbnail of the image (<code>rect</code>
     *        parameter is then ignored)
     * @return {@link IcyBufferedImage}
     */
    protected static IcyBufferedImage getImageInternal(IFormatReader reader, Rectangle rect, int z, int t,
            boolean thumbnail) throws FormatException, IOException
    {
        final Rectangle r;

        if (thumbnail)
            r = new Rectangle(0, 0, reader.getThumbSizeX(), reader.getThumbSizeY());
        else if (rect == null)
            r = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());
        else
            r = rect;

        return getImageInternal(reader, r, z, t, thumbnail, null, null, null);
    }

    /**
     * low level byte read from LOCI reader (only used by internal methods)
     */
    protected static byte[] getBytesInternal(IFormatReader reader, int index, Rectangle rect, boolean thumbnail,
            byte[] buffer) throws FormatException, IOException
    {
        if (thumbnail)
            return reader.openThumbBytes(index);

        final Rectangle imgRect = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());

        // need to allocate
        if (buffer == null)
        {
            // return whole image
            if ((rect == null) || rect.equals(imgRect))
                return reader.openBytes(index);

            // return region
            return reader.openBytes(index, rect.x, rect.y, rect.width, rect.height);
        }

        // already allocated / whole image
        if ((rect == null) || rect.equals(imgRect))
            return reader.openBytes(index, buffer);

        // return region
        return reader.openBytes(index, buffer, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Down scale the specified image with the given down scale factor.<br>
     * If down scale factor equals <code>0</code> then the input image is directly returned.
     * 
     * @param source
     *        input image
     * @param scale
     *        scale factor
     * @return scaled image or source image is scale factor equals <code>0</code>
     */
    protected static IcyBufferedImage downScale(IcyBufferedImage source, int downScaleLevel)
    {
        IcyBufferedImage result = source;
        int it = downScaleLevel;

        // process fast down scaling
        while (it-- > 0)
            result = IcyBufferedImageUtil.downscaleBy2(result, true);

        return result;

        // final double scale = Math.pow(2, downScaleLevel);
        // if (scale > 1d)
        // {
        // final int sizeX = (int) (Math.round(source.getSizeX() / scale));
        // final int sizeY = (int) (Math.round(source.getSizeY() / scale));
        // // down scale
        // return IcyBufferedImageUtil.scale(source, sizeX, sizeY, FilterType.BILINEAR);
        // }
        //
        // return source;
    }

    protected static UnsupportedFormatException translateException(String path, FormatException exception)
    {
        if (exception instanceof UnknownFormatException)
            return new UnsupportedFormatException(path + ": Unknown image format.", exception);
        else if (exception instanceof MissingLibraryException)
            return new UnsupportedFormatException(path + ": Missing library to load the image.", exception);
        else
            return new UnsupportedFormatException(path + ": Unsupported image format.", exception);
    }
}

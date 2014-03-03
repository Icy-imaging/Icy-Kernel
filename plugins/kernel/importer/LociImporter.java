/**
 * 
 */
package plugins.kernel.importer;

import icy.common.exception.UnsupportedFormatException;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.gui.dialog.ImageLoaderDialog.AllImagesFileFilter;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.LinearColorMap;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ByteArrayConvert;
import icy.util.StringUtil;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

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
import loci.formats.in.TiffJAIReader;
import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * LOCI Bio-Formats library importer class.
 * 
 * @author Stephane
 */
public class LociImporter implements SequenceFileImporter
{
    protected class LociAllFileFilter extends AllImagesFileFilter
    {
        @Override
        public String getDescription()
        {
            return "All image files (Bio-Formats)";
        }
    };

    private final ImageReader mainReader;
    IFormatReader reader;

    public LociImporter()
    {
        super();

        mainReader = new ImageReader();
        reader = null;
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

    @Override
    public List<FileFilter> getFileFilters()
    {
        final List<FileFilter> result = new ArrayList<FileFilter>();

        result.add(new ExtensionFileFilter(new String[] {"tif", "tiff"}, "TIFF images (Bio-Formats)"));
        result.add(new ExtensionFileFilter(new String[] {"png"}, "PNG images (Bio-Formats)"));
        result.add(new ExtensionFileFilter(new String[] {"jpg", "jpeg"}, "JPEG images (Bio-Formats)"));
        result.add(new ExtensionFileFilter(new String[] {"avi"}, "AVI videos (Bio-Formats)"));

        // final IFormatReader[] readers = mainReader.getReaders();

        // for (IFormatReader reader : readers)
        // result.add(new FormatFileFilter(reader, true));

        result.add(new LociAllFileFilter());

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
            setReader(path);
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
        if (reader != null)
            return StringUtil.equals(reader.getCurrentFile(), path);

        return false;
    }

    @Override
    public String getOpened()
    {
        if (reader != null)
            return reader.getCurrentFile();

        return null;
    }

    @Override
    public boolean open(String path) throws UnsupportedFormatException, IOException
    {
        // already opened ?
        if (isOpen(path))
            return true;

        // close first
        close();

        try
        {
            // ensure we have the correct reader
            setReader(path);

            // disable file grouping
            reader.setGroupFiles(false);
            // we want all metadata
            reader.setOriginalMetadataPopulated(true);
            // prepare meta data store structure
            reader.setMetadataStore(new OMEXMLMetadataImpl());
            // load file with LOCI library
            reader.setId(path);

            return true;
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
    }

    @Override
    public boolean close() throws IOException
    {
        // something to close ?
        if (getOpened() != null)
        {
            reader.close();
            return true;
        }

        return false;
    }

    /**
     * Prepare the reader to read data from specified serie and at specified resolution.<br>
     * 
     * @return the image divisor factor to match the wanted resolution if needed.
     */
    protected double prepareReader(int serie, int resolution)
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
        return Math.pow(2d, resolution - res);
    }

    @Override
    public OMEXMLMetadataImpl getMetaData(String path) throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            return (OMEXMLMetadataImpl) reader.getMetadataStore();
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public int getTileWidth(String path, int serie) throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader
            prepareReader(serie, 0);

            return reader.getOptimalTileWidth();
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public int getTileHeight(String path, int serie) throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader
            prepareReader(serie, 0);

            return reader.getOptimalTileHeight();
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public IcyBufferedImage getThumbnail(String path, int serie) throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader and get down scale factor
            final double scale = prepareReader(serie, 0);
            // get image
            IcyBufferedImage result = getThumbnail(reader, reader.getSizeZ() / 2, reader.getSizeT() / 2);
            // return down scaled version if needed
            return downScale(result, scale);
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public Object getPixels(String path, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader and get down scale factor
            final double scale = prepareReader(serie, resolution);
            // no need to rescale ? --> directly return the pixels
            if (scale == 1d)
                return getPixels(reader, rectangle, z, t, c);

            // get the image
            IcyBufferedImage result = getImage(reader, rectangle, z, t, c);
            // down scale it
            result = downScale(result, scale);

            // and return internal data
            return result.getDataXY(0);
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws icy.common.exception.UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader and get down scale factor
            final double scale = prepareReader(serie, resolution);
            // get image
            IcyBufferedImage result = getImage(reader, rectangle, z, t, c);
            // return down scaled version if needed
            return downScale(result, scale);
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader and get down scale factor
            final double scale = prepareReader(serie, resolution);
            // get image
            IcyBufferedImage result = getImage(reader, rectangle, z, t);
            // return down scaled version if needed
            return downScale(result, scale);
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader and get down scale factor
            final double scale = prepareReader(serie, resolution);
            // get image
            IcyBufferedImage result = getImage(reader, null, z, t, c);
            // return down scaled version if needed
            return downScale(result, scale);
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        boolean close;

        if (!isOpen(path))
        {
            open(path);
            // keep info if we need to close the reader at end or not
            close = true;
        }
        else
            close = false;

        try
        {
            // prepare reader and get down scale factor
            final double scale = prepareReader(serie, resolution);
            // get image
            IcyBufferedImage result = getImage(reader, null, z, t);
            // return down scaled version if needed
            return downScale(result, scale);
        }
        catch (FormatException e)
        {
            throw translateException(path, e);
        }
        finally
        {
            if (close)
                reader.close();
        }
    }

    @Override
    public IcyBufferedImage getImage(String path, int serie, int z, int t) throws UnsupportedFormatException,
            IOException
    {
        return getImage(path, serie, 0, z, t);
    }

    @Override
    public IcyBufferedImage getImage(String path, int z, int t) throws UnsupportedFormatException, IOException
    {
        return getImage(path, 0, 0, z, t);
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.<br>
     * <i>Compatible version (load the original image and resize it)</i>
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnailCompatible(IFormatReader reader, int z, int t) throws FormatException,
            IOException
    {
        return IcyBufferedImageUtil.scale(getImage(reader, null, z, t), reader.getThumbSizeX(), reader.getThumbSizeY());
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnail(IFormatReader reader, int z, int t) throws FormatException, IOException
    {
        try
        {
            return getImage(reader, null, z, t, true);
        }
        catch (Exception e)
        {
            // LOCI do not support thumbnail for all image, try compatible version
            return getThumbnailCompatible(reader, z, t);
        }
    }

    /**
     * Load a thumbnail version of the image located at (Z, T, C) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.<br>
     * <i>Compatible version (load the original image and resize it)</i>
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
            throws FormatException, IOException
    {
        return IcyBufferedImageUtil.scale(getImage(reader, null, z, t, c), reader.getThumbSizeX(),
                reader.getThumbSizeY());
    }

    /**
     * Load a thumbnail version of the image located at (Z, T, C) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.
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
    public static IcyBufferedImage getThumbnail(IFormatReader reader, int z, int t, int c) throws FormatException,
            IOException
    {
        try
        {
            return getImage(reader, null, z, t, c, true);
        }
        catch (Exception e)
        {
            // LOCI do not support thumbnail for all image, try compatible version
            return getThumbnailCompatible(reader, z, t);
        }
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
            throws FormatException, IOException
    {
        return getImage(reader, rect, z, t, c, false);
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
    public static IcyBufferedImage getImage(IFormatReader reader, Rectangle rect, int z, int t) throws FormatException,
            IOException
    {
        return getImage(reader, rect, z, t, false);
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
    public static IcyBufferedImage getImageCompatible(IFormatReader reader, int z, int t) throws FormatException,
            IOException
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
     * @param rect
     *        Region we want to retrieve data.<br>
     *        Set to <code>null</code> to retrieve the whole image.
     * @param z
     *        Z position of the pixels to load
     * @param t
     *        T position of the pixels to load
     * @param c
     *        Channel index to load
     * @return 1D array containing pixels data.<br>
     *         The type of the array depends from the internal image data type
     */
    static Object getPixels(IFormatReader reader, Rectangle rect, int z, int t, int c) throws FormatException,
            IOException
    {
        final int sizeX;
        final int sizeY;

        if (rect == null)
        {
            sizeX = reader.getSizeX();
            sizeY = reader.getSizeY();
        }
        else
        {
            sizeX = rect.width;
            sizeY = rect.height;
        }

        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int rgbChanCount = reader.getRGBChannelCount();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // allocate internal image data array
        final Object result = Array1DUtil.createArray(dataType, sizeX * sizeY);

        final int baseC = c / rgbChanCount;
        final int subC = c % rgbChanCount;

        // get image data
        final byte[] byteData = getBytes(reader, reader.getIndex(z, baseC, t), rect, false, null);

        // current final component
        final int componentByteLen = byteData.length / rgbChanCount;

        // build data array
        if (interleaved)
            ByteArrayConvert.byteArrayTo(byteData, subC, rgbChanCount, result, 0, 1, componentByteLen, little);
        else
            ByteArrayConvert.byteArrayTo(byteData, subC * componentByteLen, 1, result, 0, 1, componentByteLen, little);

        // return raw pixels data
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
    static IcyBufferedImage getImage(IFormatReader reader, Rectangle rect, int z, int t, int c, boolean thumbnail)
            throws FormatException, IOException
    {
        final int sizeX;
        final int sizeY;

        if (thumbnail)
        {
            sizeX = reader.getThumbSizeX();
            sizeY = reader.getThumbSizeY();
        }
        else if (rect == null)
        {
            sizeX = reader.getSizeX();
            sizeY = reader.getSizeY();
        }
        else
        {
            sizeX = rect.width;
            sizeY = rect.height;
        }

        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int rgbChanCount = reader.getRGBChannelCount();
        final boolean indexed = reader.isIndexed();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // allocate internal image data array
        final Object data = Array1DUtil.createArray(dataType, sizeX * sizeY);

        final int baseC = c / rgbChanCount;
        final int subC = c % rgbChanCount;

        // get image data
        final byte[] byteData = getBytes(reader, reader.getIndex(z, baseC, t), rect, thumbnail, null);

        // current final component
        final int componentByteLen = byteData.length / rgbChanCount;

        // build data array
        if (interleaved)
            ByteArrayConvert.byteArrayTo(byteData, subC, rgbChanCount, data, 0, 1, componentByteLen, little);
        else
            ByteArrayConvert.byteArrayTo(byteData, subC * componentByteLen, 1, data, 0, 1, componentByteLen, little);

        final IcyBufferedImage result = new IcyBufferedImage(rect.width, rect.height, data, dataType.isSigned());

        // indexed color ?
        if (indexed)
        {
            IcyColorMap map;

            // only 8 bits and 16 bits lookup table supported
            switch (dataType.getJavaType())
            {
                case BYTE:
                    map = new IcyColorMap("Channel " + c, reader.get8BitLookupTable());
                    break;

                case SHORT:
                    map = new IcyColorMap("Channel " + c, reader.get16BitLookupTable());
                    break;

                default:
                    map = null;
            }

            // sometime loci return black colormap map and we want to avoid them...
            if ((map != null) && !map.isBlack())
                result.setColorMap(0, map, true);
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
    static IcyBufferedImage getImage(IFormatReader reader, Rectangle rect, int z, int t, boolean thumbnail)
            throws FormatException, IOException
    {
        final int sizeX;
        final int sizeY;

        if (thumbnail)
        {
            sizeX = reader.getThumbSizeX();
            sizeY = reader.getThumbSizeY();
        }
        else if (rect == null)
        {
            sizeX = reader.getSizeX();
            sizeY = reader.getSizeY();
        }
        else
        {
            sizeX = rect.width;
            sizeY = rect.height;
        }

        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int effSizeC = reader.getEffectiveSizeC();
        final int rgbChanCount = reader.getRGBChannelCount();
        final int sizeC = effSizeC * rgbChanCount;
        final boolean indexed = reader.isIndexed();
        final boolean little = reader.isLittleEndian();

        // prepare internal image data array
        final Object[] data = Array2DUtil.createArray(dataType, sizeC);
        final IcyColorMap[] colormaps = new IcyColorMap[effSizeC];

        // allocate array
        for (int i = 0; i < sizeC; i++)
            data[i] = Array1DUtil.createArray(dataType, sizeX * sizeY);

        byte[] byteData = null;
        for (int effC = 0; effC < effSizeC; effC++)
        {
            // get data
            byteData = getBytes(reader, reader.getIndex(z, effC, t), rect, thumbnail, byteData);

            // current final component
            final int c = effC * rgbChanCount;
            final int componentByteLen = byteData.length / rgbChanCount;

            // build data array
            int inOffset = 0;
            if (reader.isInterleaved())
            {
                for (int sc = 0; sc < rgbChanCount; sc++)
                {
                    ByteArrayConvert.byteArrayTo(byteData, inOffset, rgbChanCount, data[c + sc], 0, 1,
                            componentByteLen, little);
                    inOffset++;
                }
            }
            else
            {
                for (int sc = 0; sc < rgbChanCount; sc++)
                {
                    ByteArrayConvert.byteArrayTo(byteData, inOffset, 1, data[c + sc], 0, 1, componentByteLen, little);
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
                        colormaps[effC] = new IcyColorMap("Channel " + effC, reader.get8BitLookupTable());
                        break;

                    case SHORT:
                        colormaps[effC] = new IcyColorMap("Channel " + effC, reader.get16BitLookupTable());
                        break;

                    default:
                        colormaps[effC] = null;
                        break;
                }
            }
        }

        final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, data, dataType.isSigned());

        // affect colormap
        result.beginUpdate();
        try
        {
            if (indexed)
            {
                // error ! we should have same number of colormap than component
                if (colormaps.length != sizeC)
                {
                    System.err.println("Warning : " + colormaps.length + " colormap for " + sizeC + " components");
                    System.err.println("Colormap can not be restored");
                }
                else
                {
                    // set colormaps
                    for (int comp = 0; comp < sizeC; comp++)
                    {
                        // sometime loci return black colormap map and we want to avoid them...
                        if ((colormaps[comp] != null) && !colormaps[comp].isBlack())
                            result.setColorMap(comp, colormaps[comp], true);
                    }
                }
            }
            // special case of 4 channels image, try to set 4th channel colormap
            else if (sizeC == 4)
            {
                // assume real alpha channel depending from the reader we use
                final boolean alpha = (reader instanceof PNGReader) || (reader instanceof APNGReader)
                        || (reader instanceof TiffJAIReader) || (reader instanceof JPEG2000Reader);

                // replace alpha with Cyan color
                if (!alpha)
                    result.setColorMap(3, LinearColorMap.cyan_, true);
            }
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    static byte[] getBytes(IFormatReader reader, int index, Rectangle rect, boolean thumbnail, byte[] buffer)
            throws FormatException, IOException
    {
        // need to allocate
        if (buffer == null)
        {
            if (thumbnail)
                return reader.openThumbBytes(index);
            if (rect != null)
                return reader.openBytes(index, rect.x, rect.y, rect.width, rect.height);
            return reader.openBytes(index);
        }

        // already allocated
        if (thumbnail)
            return reader.openThumbBytes(index);
        if (rect != null)
            return reader.openBytes(index, buffer, rect.x, rect.y, rect.width, rect.height);
        return reader.openBytes(index, buffer);
    }

    static IcyBufferedImage downScale(IcyBufferedImage source, double scale)
    {
        // need down scale ?
        if (scale != 1d)
        {
            final int sizeX = (int) (Math.round(source.getSizeX() / scale));
            final int sizeY = (int) (Math.round(source.getSizeY() / scale));
            // down scale
            return IcyBufferedImageUtil.scale(source, sizeX, sizeY, FilterType.BILINEAR);
        }

        return source;
    }

    static UnsupportedFormatException translateException(String path, FormatException exception)
    {
        if (exception instanceof UnknownFormatException)
            return new UnsupportedFormatException(path + ": Unknown image format.", exception);
        else if (exception instanceof MissingLibraryException)
            return new UnsupportedFormatException(path + ": Missing library to load the image.", exception);
        else
            return new UnsupportedFormatException(path + ": Unsupported image format.", exception);
    }
}

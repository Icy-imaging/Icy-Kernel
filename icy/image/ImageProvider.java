/**
 * 
 */
package icy.image;

import java.awt.Rectangle;
import java.io.IOException;

import icy.common.exception.UnsupportedFormatException;
import icy.sequence.MetaDataUtil;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;

/**
 * Image provider interface.<br>
 * This interface is designed to any class capable of delivering image data at different level
 * access.<br>
 * Takes an uniquely identified resource as input and returns image data at different level access.<br>
 * <br>
 * Example of possible class implementing this interface:
 * <ul>
 * <li>LOCI (Bio-Formats) image reader class.</li>
 * <li>image database interface.</li>
 * </ul>
 * 
 * @author Stephane
 */
public interface ImageProvider
{
    /**
     * Returns the image metadata in OME format (metadata provides many informations about the image).<br>
     * <br>
     * Number of series (mandatory field) :<br>
     * {@link MetaDataUtil#getNumSeries(OMEXMLMetadata)}<br>
     * Dimension (mandatory fields) :<br>
     * {@link MetaDataUtil#getSizeX(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeY(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeZ(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeT(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeC(OMEXMLMetadata, int)}<br>
     * Internal data type (mandatory field) :<br>
     * {@link MetaDataUtil#getDataType(OMEXMLMetadata, int)}<br>
     * Physical and time position (mandatory fields) :<br>
     * {@link MetaDataUtil#getPositionX(OMEXMLMetadata, int, int, int, int, double)}<br>
     * {@link MetaDataUtil#getPositionY(OMEXMLMetadata, int, int, int, int, double)}<br>
     * {@link MetaDataUtil#getPositionZ(OMEXMLMetadata, int, int, int, int, double)}<br>
     * {@link MetaDataUtil#getPositionT(OMEXMLMetadata, int, long)}<br>
     * {@link MetaDataUtil#getPositionTOffset(OMEXMLMetadata, int, int, int, int, double)}<br>
     * <br>
     * and many others informations depending the available metadata in the image format.
     */
    public OMEXMLMetadata getOMEXMLMetaData() throws UnsupportedFormatException, IOException;

    /**
     * @deprecated Use {@link #getOMEXMLMetaData()} instead.
     */
    @Deprecated
    public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException;

    /**
     * Returns the (optimal) tile width for the specified series of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.<br>
     * This method should returns <code>0</code> if tile loading is not supported and <code>-1</code> if any tile size
     * can be used.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @return optimal tile width
     */
    public int getTileWidth(int series) throws UnsupportedFormatException, IOException;

    /**
     * Returns the (optimal) tile height for the specified series of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.<br>
     * This method should returns <code>0</code> if tile loading is not supported and <code>-1</code> if any tile size
     * can be used.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @return optimal tile height
     */
    public int getTileHeight(int series) throws UnsupportedFormatException, IOException;

    /**
     * Returns <code>true</code> if the given sub resolution is available from this series.<br>
     * Note that even if a sub resolution isn't available, the ImageProvider should provide it by generating it manually
     * (using the {@link IcyBufferedImageUtil#downscaleBy2(IcyBufferedImage, boolean, int)} method.<br>
     * <br>
     * Final image resolution can be calculated with: <code>image.originalResolution / (2^resolution)</code><br>
     * So <i>resolution 0</i> is the original resolution and it's always available (should always returns
     * <code>true</code>).<br>
     * <i>Resolution 1</i> is half of the original resolution<br>
     * <i>Resolution 2</i> is quarter of the original resolution<br>
     * <i>...</i>
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Resolution level
     */
    public boolean isResolutionAvailable(int series, int resolution) throws UnsupportedFormatException, IOException;

    /**
     * Returns the image thumbnail for the specified series of the image.<br>
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @return thumbnail image.
     */
    public IcyBufferedImage getThumbnail(int series)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the pixel data located for specified position of the image.<br>
     * Data is returned in form of a single dimension array, the type of this array depends from the image data type<br>
     * which can be retrieve from the metadata (see {@link OMEXMLMetadataImpl#getPixelsType(int)}
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region we want to retrieve (considering the original image resolution).<br>
     *        If set to <code>null</code> then the whole image is returned.
     * @param z
     *        Z position of the image (slice) we want retrieve data from
     * @param t
     *        T position of the image (frame) we want retrieve data from
     * @param c
     *        C position of the image (channel) we want retrieve (-1 is not accepted here).
     * @return native type array containing image pixel data.<br>
     * @see #isResolutionAvailable(int, int)
     */
    public Object getPixels(int series, int resolution, Rectangle region, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
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
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    public IcyBufferedImage getImage(int series, int resolution, Rectangle region, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region we want to retrieve (considering the original image resolution).<br>
     *        If set to <code>null</code> then the whole image is returned.
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    public IcyBufferedImage getImage(int series, int resolution, Rectangle region, int z, int t)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @param c
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    public IcyBufferedImage getImage(int series, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    public IcyBufferedImage getImage(int series, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param series
     *        Series index for multi series image (use 0 if unsure).
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(int series, int z, int t)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException;
}

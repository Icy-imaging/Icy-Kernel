/**
 * 
 */
package icy.image;

import icy.common.exception.UnsupportedFormatException;

import java.awt.Rectangle;
import java.io.IOException;

import loci.formats.ome.OMEXMLMetadataImpl;

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
     * Returns the image metadata in OME format.<br>
     * Metadata give many informations about the image.<br>
     * <br>
     * Number of serie (mandatory field) :<br>
     * {@link OMEXMLMetadataImpl#getImageCount()}<br>
     * Dimension (mandatory fields) :<br>
     * {@link OMEXMLMetadataImpl#getPixelsSizeX(int)}<br>
     * {@link OMEXMLMetadataImpl#getPixelsSizeY(int)}<br>
     * {@link OMEXMLMetadataImpl#getPixelsSizeZ(int)}<br>
     * {@link OMEXMLMetadataImpl#getPixelsSizeT(int)}<br>
     * {@link OMEXMLMetadataImpl#getPixelsSizeC(int)}<br>
     * {@link OMEXMLMetadataImpl#getPixelsSizeX(int)}<br>
     * Internal data type (mandatory field) :<br>
     * {@link OMEXMLMetadataImpl#getPixelsType(int)}<br>
     * <br>
     * and many others informations depending the available metadata in the image format.
     */
    public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException;

    /**
     * Returns the tile width for the specified serie of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @return tile width
     */
    public int getTileWidth(int serie) throws UnsupportedFormatException, IOException;

    /**
     * Returns the tile height for the specified serie of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @return tile height
     */
    public int getTileHeight(int serie) throws UnsupportedFormatException, IOException;

    /**
     * Returns the image thumbnail for the specified serie of the image.<br>
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @return thumbnail image.
     */
    public IcyBufferedImage getThumbnail(int serie) throws UnsupportedFormatException, IOException;

    /**
     * Returns the pixel data located for specified position of the image.<br>
     * Data is returned in form of an array, the type of this array depends from the image data type<br>
     * which can be retrieve from the metadata (see {@link OMEXMLMetadataImpl#getPixelsType(int)}
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param rectangle
     *        The 2D region we want to retrieve.<br>
     *        If set to <code>null</code> then the whole image is returned.
     * @param z
     *        Z position of the image (slice) we want retrieve data from
     * @param t
     *        T position of the image (frame) we want retrieve data from
     * @param c
     *        C position of the image (channel) we want retrieve.
     * @return native type array containing image pixel data.<br>
     */
    public Object getPixels(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param rectangle
     *        The 2D region we want to retrieve.<br>
     *        If set to <code>null</code> then the whole image is returned.
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @param c
     *        C position of the image (channel) we want retrieve.<br>
     *        -1 is a special value meaning we want all channels.
     * @return image
     */
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param rectangle
     *        The 2D region we want to retrieve.<br>
     *        If set to <code>null</code> then the whole image is returned.
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @param c
     *        C position of the image (channel) we want retrieve.<br>
     *        -1 is a special value meaning we want all channel.
     * @return image
     */
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t, int c) throws UnsupportedFormatException,
            IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure).<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default image resolution while level 1 is base image
     *        resolution / 2 and so on...
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(int serie, int resolution, int z, int t) throws UnsupportedFormatException,
            IOException;

    /**
     * Returns the image located at specified position.
     * 
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(int serie, int z, int t) throws UnsupportedFormatException, IOException;

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

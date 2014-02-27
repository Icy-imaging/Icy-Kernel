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
 * This interface is designed for any class capable of providing image to Icy.<br>
 * This can be image reader classes as LOCI (Bio-Formats) or image database interface for instance.
 * 
 * @author Stephane
 */
public interface ImageProvider
{
    /**
     * @return The <code>id</code> of the image currently opened or <code>null</code> otherwise.
     * @see #open(String)
     * @see #close()
     */
    public String getOpened();

    /**
     * Open the image designed by the specified <code>id</code>.<br>
     * This operation is optional but allow for better performance when doing severals consecutive
     * operations on the same image.<br>
     * Don't forget to call {@link #close()} to close the image when you're done.<br>
     * Calling this method will automatically close any previous opened image.     * 
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @return <code>true</code> if the operation has succeeded and <code>false</code> otherwise.
     */
    public boolean open(String id) throws UnsupportedFormatException, IOException;

    /**
     * Close the image which has been previously opened with {@link #open(String)} method.<br>
     * 
     * @return <code>true</code> if the operation has succeeded and <code>false</code> otherwise.
     */
    public boolean close() throws IOException;

    /**
     * Returns metadata in OME format for the given serie for specified image.<br>
     * Metadata give many information about the image.<br>
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
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     */
    public OMEXMLMetadataImpl getMetaData(String id) throws UnsupportedFormatException, IOException;

    /**
     * Returns the tile width for the specified serie of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @return tile width
     */
    public int getTileWidth(String id, int serie) throws UnsupportedFormatException, IOException;

    /**
     * Returns the tile height for the specified serie of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @return tile height
     */
    public int getTileHeight(String id, int serie) throws UnsupportedFormatException, IOException;

    /**
     * Returns the image thumbnail for the specified serie of the image.<br>
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @return thumbnail image.
     */
    public IcyBufferedImage getThumbnail(String id, int serie) throws UnsupportedFormatException, IOException;

    /**
     * Returns the pixel data located for specified position of the image designed by id.<br>
     * Data is returned in form of an array, the type of this array depends from the image data type<br>
     * which can be retrieve from the metadata (see {@link OMEXMLMetadataImpl#getPixelsType(int)}
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
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
    public Object getPixels(String id, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position and designed by id.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
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
    public IcyBufferedImage getImage(String id, int serie, int resolution, Rectangle rectangle, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position and designed by id.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
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
    public IcyBufferedImage getImage(String id, int serie, int resolution, Rectangle rectangle, int z, int t)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position and designed by id.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
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
    public IcyBufferedImage getImage(String id, int serie, int resolution, int z, int t, int c)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position and designed by id.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
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
    public IcyBufferedImage getImage(String id, int serie, int resolution, int z, int t)
            throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position and designed by id.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @param serie
     *        Serie index for multi serie image (use 0 if unsure).
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(String id, int serie, int z, int t) throws UnsupportedFormatException, IOException;

    /**
     * Returns the image located at specified position and designed by id.
     * 
     * @param id
     *        Image id, it can be a file path or URL or whatever depending the internal
     *        import method.
     * @param z
     *        Z position of the image (slice) we want retrieve
     * @param t
     *        T position of the image (frame) we want retrieve
     * @return image
     */
    public IcyBufferedImage getImage(String id, int z, int t) throws UnsupportedFormatException, IOException;
}

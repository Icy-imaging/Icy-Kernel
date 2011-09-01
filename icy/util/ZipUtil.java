/**
 * 
 */
package icy.util;

import icy.system.IcyExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Stephane
 */
public class ZipUtil
{
    /**
     * Compress the specified array of byte and return packed data
     */
    public static byte[] pack(byte[] rawData)
    {
        final Deflater compressor = new Deflater();

        // give data to compress
        compressor.setInput(rawData);
        compressor.finish();

        // create an expandable byte array to hold the compressed data.
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(rawData.length);
        final byte[] buf = new byte[1024];

        // pack data
        while (!compressor.finished())
        {
            final int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        try
        {
            bos.close();
        }
        catch (IOException e)
        {
        }

        // return packed data
        return bos.toByteArray();
    }

    /**
     * Uncompress the specified array of byte and return unpacked data
     */
    public static byte[] unpack(byte[] packedData)
    {
        final Inflater decompressor = new Inflater();

        // give the data to uncompress
        decompressor.setInput(packedData);

        // create an expandable byte array to hold the uncompressed data
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(packedData.length);
        final byte[] buf = new byte[1024];

        // unpack data
        while (!decompressor.finished())
        {
            try
            {
                final int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            catch (DataFormatException e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
                return null;
            }
        }
        try
        {
            bos.close();
        }
        catch (IOException e)
        {
        }

        // return unpacked data
        return bos.toByteArray();
    }
}

/**
 * 
 */
package icy.util;

import icy.file.FileUtil;
import icy.network.NetworkUtil;
import icy.system.IcyExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        final byte[] buf = new byte[4096];

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
        final byte[] buf = new byte[4096];

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

    /**
     * Extract the specified zip file to the specified destination directory.
     * 
     * @param zipFile
     *        input zip file name
     * @param outputDirectory
     *        output directory name
     * @return true if file was correctly extracted, false otherwise
     */
    public static boolean extract(String zipFile, String outputDirectory)
    {
        boolean ok = true;

        try
        {
            final ZipFile file = new ZipFile(zipFile);
            final Enumeration<? extends ZipEntry> entries = file.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory())
                {
                    if (!FileUtil.createDir(outputDirectory + FileUtil.separator + entry.getName()))
                    {
                        System.err.println("ZipUtil.unpack(" + zipFile + "," + outputDirectory + ") error :");
                        System.err.println("Can't create directory : '" + outputDirectory + FileUtil.separator
                                + entry.getName() + "'");
                        ok = false;
                        break;
                    }
                }
                else if (!FileUtil.save(outputDirectory + FileUtil.separator + entry.getName(),
                        NetworkUtil.download(file.getInputStream(entry)), true))
                {
                    System.err.println("ZipUtil.unpack(" + zipFile + "," + outputDirectory + ") failed.");
                    ok = false;
                    break;
                }
            }

            file.close();
        }
        catch (IOException ioe)
        {
            System.err.println("ZipUtil.unpack(" + zipFile + "," + outputDirectory + ") error :");
            IcyExceptionHandler.showErrorMessage(ioe, false);
            ok = false;
        }

        return ok;
    }

    /**
     * Extract the specified zip file in to default location.
     * 
     * @param zipFile
     *        input zip file name
     * @return true if file was correctly extracted, false otherwise
     */
    public static boolean extract(String zipFile)
    {
        return extract(zipFile, FileUtil.getDirectory(zipFile) + FileUtil.getFileName(zipFile, false));
    }
}

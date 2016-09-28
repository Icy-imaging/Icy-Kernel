/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
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
     * Compress the specified array of byte with given level of compression and return packed data.<br/>
     * 
     * @param packer
     *        the packer object, can be <code>null</code> in which case we create a new Deflater object
     * @param rawData
     *        raw data to compress
     * @param level
     *        level of compression where 0 is low and 9 is high (use -1 to keep current / use default level)
     */
    public static byte[] pack(Deflater packer, byte[] rawData, int level)
    {
        final Deflater compressor;

        if (packer != null)
        {
            compressor = packer;
            compressor.reset();
        }
        else
            compressor = new Deflater();

        if (level != -1)
            compressor.setLevel(level);

        // give data to compress
        compressor.setInput(rawData);
        compressor.finish();

        // create an expandable byte array to hold the compressed data.
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(rawData.length);
        final byte[] buf = new byte[65536];

        // pack data
        while (!compressor.finished())
        {
            final int count = compressor.deflate(buf);

            // nothing more to do ? --> end here
            if (count == 0)
                break;

            bos.write(buf, 0, count);
        }

        try
        {
            bos.close();
        }
        catch (IOException e)
        {
            // we can freely ignore this one
        }

        // return packed data
        return bos.toByteArray();
    }

    /**
     * Compress the specified array of byte and return packed data
     */
    public static byte[] pack(byte[] rawData)
    {
        return pack(null, rawData, -1);
    }

    /**
     * Uncompress the specified array of byte and return unpacked data
     * 
     * @param unpacker
     *        the unpacker object, can be <code>null</code> in which case we create a new Inflater object
     * @param packedData
     *        packed data to uncompress
     * @throws DataFormatException
     */
    public static byte[] unpack(Inflater unpacker, byte[] packedData) throws DataFormatException
    {
        final Inflater decompressor;

        if (unpacker != null)
        {
            decompressor = unpacker;
            decompressor.reset();
        }
        else
            decompressor = new Inflater();

        // give the data to uncompress
        decompressor.setInput(packedData);

        // create an expandable byte array to hold the uncompressed data
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(packedData.length);
        final byte[] buf = new byte[65536];

        // unpack data
        while (!decompressor.finished())
        {
            final int count = decompressor.inflate(buf);

            // nothing more to do ? --> end here
            if (count == 0)
                break;

            bos.write(buf, 0, count);
        }
        try
        {
            bos.close();
        }
        catch (IOException e)
        {
            // we can freely ignore this one
        }

        // return unpacked data
        return bos.toByteArray();
    }

    /**
     * Uncompress the specified array of byte and return unpacked data
     * 
     * @throws DataFormatException
     */
    public static byte[] unpack(byte[] packedData) throws DataFormatException
    {
        return unpack(null, packedData);
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
                        System.err.println("ZipUtil.extract(" + zipFile + "," + outputDirectory + ") error :");
                        System.err.println("Can't create directory : '" + outputDirectory + FileUtil.separator
                                + entry.getName() + "'");
                        ok = false;
                        break;
                    }
                }
                else if (!FileUtil.save(outputDirectory + FileUtil.separator + entry.getName(),
                        NetworkUtil.download(file.getInputStream(entry)), true))
                {
                    System.err.println("ZipUtil.extract(" + zipFile + "," + outputDirectory + ") failed.");
                    ok = false;
                    break;
                }
            }

            file.close();
        }
        catch (IOException ioe)
        {
            System.err.println("ZipUtil.extract(" + zipFile + "," + outputDirectory + ") error :");
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

    /**
     * Verify that specified file is a valid ZIP file
     * 
     * @param zipFile
     *        input zip file name
     * @throws IOException
     *         if the input file is not a valid zip file.
     */
    public static void isValid(String zipFile) throws IOException
    {
        final ZipFile file = new ZipFile(zipFile);
        try
        {
            final Enumeration<? extends ZipEntry> entries = file.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                entry.getName();
            }
        }
        finally
        {
            file.close();
        }
    }

    /**
     * Verify that specified file is a valid ZIP file
     * 
     * @param zipFile
     *        input zip file name
     * @param showError
     *        indicate if the method should show the error (in the output console) if the verify operation failed.
     * @return true if the specified file is a valid ZIP file
     */
    public static boolean isValid(String zipFile, boolean showError)
    {
        try
        {
            isValid(zipFile);
        }
        catch (IOException e)
        {
            if (showError)
            {
                System.err.println("ZipUtil.isValid(" + zipFile + ") error :");
                IcyExceptionHandler.showErrorMessage(e, false);
            }

            return false;
        }

        return true;
    }
}

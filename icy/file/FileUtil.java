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
package icy.file;

import icy.network.NetworkUtil;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stephane
 */
public class FileUtil
{
    public static final char separatorChar = '/';
    public static final String separator = "/";

    /**
     * Transform any system specific path in java generic path form.<br>
     * Ex: "C:\windows" --> "C:/windows"
     */
    public static String getGenericPath(String path)
    {
        if (path != null)
            return path.replace('\\', '/');

        return null;
    }

    /**
     * Returns default temporary directory.
     * ex:<br>
     * <code>c:/temp</code><br>
     * <code>/tmp</code><br>
     * Same as {@link SystemUtil#getTempDirectory()}
     */
    public static String getTempDirectory()
    {
        final String result = FileUtil.getGenericPath(SystemUtil.getProperty("java.io.tmpdir"));
        final int len = result.length();

        // remove last separator
        if ((len > 1) && (result.charAt(len - 1) == FileUtil.separatorChar))
            return result.substring(0, len - 1);

        return result;
    }

    /**
     * Change path extension.<br>
     * Ex : setExtension(path, ".dat")<br>
     * "c:\temp" --> "c:\temp.dat"
     * "c:\file.out" --> "c:\file.dat"
     * "" --> ""
     */
    public static String setExtension(String path, String extension)
    {
        final String finalPath = getGenericPath(path);

        if (StringUtil.isEmpty(finalPath))
            return "";

        final int len = finalPath.length();
        String result = finalPath;

        final int dotIndex = result.lastIndexOf(".");
        // ensure we are modifying an extension
        if (dotIndex >= 0 && (len - dotIndex) <= 5)
        {
            // we consider that an extension starting with a digit is not an extension
            if (((dotIndex + 1) == len) || !Character.isDigit(result.charAt(dotIndex + 1)))
                result = result.substring(0, dotIndex);
        }

        if (extension != null)
            result += extension;

        return result;
    }

    public static void ensureParentDirExist(String filename)
    {
        ensureParentDirExist(new File(getGenericPath(filename)));
    }

    public static boolean ensureParentDirExist(File file)
    {
        final String dir = file.getParent();

        if (dir != null)
            return createDir(dir);

        return true;
    }

    public static boolean createDir(String dirname)
    {
        return createDir(new File(getGenericPath(dirname)));
    }

    public static boolean createDir(File dir)
    {
        if (!dir.exists())
            return dir.mkdirs();

        return true;
    }

    public static File createFile(String filename)
    {
        return createFile(new File(getGenericPath(filename)));
    }

    public static File createFile(File file)
    {
        if (!file.exists())
        {
            // create parent directory if not exist
            ensureParentDirExist(file);

            try
            {
                file.createNewFile();
            }
            catch (Exception e)
            {
                IcyExceptionHandler.showErrorMessage(e, false);
                return null;
            }
        }

        return file;
    }

    /**
     * Transform the specified list of path to file.
     */
    public static File[] toFiles(String[] paths)
    {
        final File[] result = new File[paths.length];

        for (int i = 0; i < paths.length; i++)
            result[i] = new File(paths[i]);

        return result;
    }

    /**
     * Transform the specified list of path to file.
     */
    public static List<File> toFiles(List<String> paths)
    {
        final List<File> result = new ArrayList<File>(paths.size());

        for (String path : paths)
            result.add(new File(path));

        return result;
    }

    /**
     * Transform the specified list of file to path.
     */
    public static String[] toPaths(File[] files)
    {
        final String[] result = new String[files.length];

        for (int i = 0; i < files.length; i++)
            result[i] = getGenericPath(files[i].getAbsolutePath());

        return result;
    }

    /**
     * Transform the specified list of file to path.
     */
    public static List<String> toPaths(List<File> files)
    {
        final List<String> result = new ArrayList<String>(files.size());

        for (File file : files)
            result.add(getGenericPath(file.getAbsolutePath()));

        return result;
    }

    /**
     * Create a symbolic link file
     */
    public static boolean createLink(String path, String target)
    {
        final String finalPath = getGenericPath(path);

        ensureParentDirExist(finalPath);

        // use OS dependent command (FIXME : replace by java 7 API when available)
        if (SystemUtil.isLinkSupported())
        {
            final Process process = SystemUtil.exec("ln -s " + target + " " + finalPath);

            // error while executing command
            if (process == null)
                return false;

            try
            {
                return (process.waitFor() == 0);
            }
            catch (InterruptedException e)
            {
                System.err.println("FileUtil.createLink(" + path + ", " + target + ") error :");
                IcyExceptionHandler.showErrorMessage(e, false);
                return false;
            }
        }

        // use classic copy if link isn't supported by OS
        return copy(target, finalPath, true, false, false);
    }

    public static byte[] load(String path, boolean displayError)
    {
        return load(new File(getGenericPath(path)), displayError);
    }

    public static byte[] load(File file, boolean displayError)
    {
        return NetworkUtil.download(file, null, displayError);
    }

    public static boolean save(String path, byte[] data, boolean displayError)
    {
        return save(new File(getGenericPath(path)), data, displayError);
    }

    public static boolean save(File file, byte[] data, boolean displayError)
    {
        final File f = createFile(file);

        if (f != null)
        {
            try
            {
                final FileOutputStream out = new FileOutputStream(f);

                out.write(data, 0, data.length);
                out.close();
            }
            catch (Exception e)
            {
                if (displayError)
                    System.err.println(e.getMessage());
                // delete incorrect file
                f.delete();
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Returns the path where the application is located (current directory).<br>
     * Ex: "D:/Apps/Icy"
     */
    public static String getApplicationDirectory()
    {
        String result;

        try
        {
            // try to get from sources
            final File f = new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            // if already a folder, return it directly
            if (f.isDirectory())
                result = f.getAbsolutePath();
            else
                result = f.getParentFile().getAbsolutePath();
        }
        catch (Exception e1)
        {
            try
            {
                // try to get from resource (this sometime return incorrect folder on mac osx)
                result = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI()).getAbsolutePath();
            }
            catch (Exception e2)
            {
                // use launch directory (which may be different from application directory)
                result = new File(System.getProperty("user.dir")).getAbsolutePath();
            }
        }

        try
        {
            // so we replace any %20 sequence in space
            result = URLDecoder.decode(result, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // ignore
        }

        return getGenericPath(result);
    }

    /**
     * @deprecated Use {@link #getApplicationDirectory()} instead.
     */
    @Deprecated
    public static String getCurrentDirectory()
    {
        return getApplicationDirectory();
    }

    /**
     * Return directory information from specified path<br>
     * <br>
     * getDirectory("/file.txt") --> "/"<br>
     * getDirectory("D:/temp/file.txt") --> "D:/temp/"<br>
     * getDirectory("D:/temp/") --> "D:/temp/"<br>
     * getDirectory("D:/temp") --> "D:/"<br>
     * getDirectory("C:file.txt") --> "C:"<br>
     * getDirectory("file.txt") --> ""<br>
     * getDirectory("file") --> ""<br>
     * getDirectory(null) --> ""
     */
    public static String getDirectory(String path, boolean separator)
    {
        final String finalPath = getGenericPath(path);

        if (!StringUtil.isEmpty(finalPath))
        {
            int index = finalPath.lastIndexOf(FileUtil.separatorChar);
            if (index != -1)
                return finalPath.substring(0, index + (separator ? 1 : 0));

            index = finalPath.lastIndexOf(':');
            if (index != -1)
                return finalPath.substring(0, index + 1);
        }

        return "";
    }

    /**
     * Return directory information from specified path<br>
     * <br>
     * getDirectory("/file.txt") --> "/"<br>
     * getDirectory("D:/temp/file.txt") --> "D:/temp/"<br>
     * getDirectory("D:/temp/") --> "D:/temp/"<br>
     * getDirectory("D:/temp") --> "D:/"<br>
     * getDirectory("C:file.txt") --> "C:"<br>
     * getDirectory("file.txt") --> ""<br>
     * getDirectory("file") --> ""<br>
     * getDirectory(null) --> ""
     */
    public static String getDirectory(String path)
    {
        return getDirectory(path, true);
    }

    /**
     * Return filename information from specified path.<br>
     * <br>
     * getFileName("/file.txt") --> "file.txt"<br>
     * getFileName("D:/temp/file.txt") --> "file.txt"<br>
     * getFileName("C:file.txt") --> "file.txt"<br>
     * getFileName("file.txt") --> "file.txt"<br>
     * getFileName(null) --> ""
     */
    public static String getFileName(String path)
    {
        return getFileName(path, true);
    }

    /**
     * Return filename information from specified path.<br>
     * Filename's extension is returned depending the withExtension flag value<br>
     * <br>
     * getFileName("/file.txt") --> "file(.txt)"<br>
     * getFileName("D:/temp/file.txt") --> "file(.txt)"<br>
     * getFileName("C:file.txt") --> "file(.txt)"<br>
     * getFileName("file.txt") --> "file(.txt)"<br>
     * getFileName(null) --> ""
     */
    public static String getFileName(String path, boolean withExtension)
    {
        final String finalPath = getGenericPath(path);

        if (StringUtil.isEmpty(finalPath))
            return "";

        int index = finalPath.lastIndexOf(FileUtil.separatorChar);
        final String fileName;

        if (index != -1)
            fileName = finalPath.substring(index + 1);
        else
        {
            index = finalPath.lastIndexOf(':');

            if (index != -1)
                fileName = finalPath.substring(index + 1);
            else
                fileName = finalPath;
        }

        if (withExtension)
            return fileName;

        index = fileName.lastIndexOf('.');

        if (index == 0)
            return "";
        else if (index != -1)
            return fileName.substring(0, index);
        else
            return fileName;
    }

    /**
     * Return filename extension information from specified path<br>
     * Dot character is returned depending the withDot flag value<br>
     * <br>
     * getFileExtension("/file.txt") --> "(.)txt)"<br>
     * getFileExtension("D:/temp/file.txt.old") --> "(.)old"<br>
     * getFileExtension("C:/win/dir2/file") --> ""<br>
     * getFileExtension(".txt") --> "(.)txt)"<br>
     * getFileExtension(null) --> ""
     */
    public static String getFileExtension(String path, boolean withDot)
    {
        final String finalPath = getGenericPath(path);

        if (StringUtil.isEmpty(finalPath))
            return "";

        final int indexSep = finalPath.lastIndexOf(separatorChar);
        final int indexDot = finalPath.lastIndexOf('.');

        if (indexDot < indexSep)
            return "";

        if (withDot)
            return finalPath.substring(indexDot);

        return finalPath.substring(indexDot + 1);
    }

    /**
     * Rename the specified <code>src</code> file to <code>dst</code> file.
     * Return false if the method failed.
     * 
     * @param src
     *        the source filename we want to rename from.
     * @param dst
     *        the destination filename we want to rename to.
     * @param force
     *        If set to <code>true</code> the destination file is overwritten if it was already
     *        existing.
     * @see File#renameTo(File)
     */
    public static boolean rename(String src, String dst, boolean force)
    {
        return rename(new File(getGenericPath(src)), new File(getGenericPath(dst)), force);
    }

    /**
     * @deprecated Use {@link #rename(String, String, boolean)} instead
     */
    @Deprecated
    public static boolean rename(String src, String dst, boolean force, boolean wantHidden)
    {
        return rename(src, dst, force);
    }

    /**
     * Rename the specified 'src' file to 'dst' file.
     * Return false if the method failed.
     * 
     * @see File#renameTo(File)
     */
    public static boolean rename(File src, File dst, boolean force)
    {
        if (src.exists())
        {
            if (dst.exists())
            {
                if (force)
                {
                    if (!delete(dst, true))
                    {
                        System.err.println("Cannot rename '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath()
                                + "'");
                        System.err.println("Reason : destination cannot be overwritten.");
                        System.err.println("Make sure it is not locked by another program (e.g. Eclipse)");
                        System.err.println("Also check that you have the rights to do this operation.");
                        return false;
                    }
                }
                else
                {
                    System.err.println("Cannot rename '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath()
                            + "'");
                    System.err.println("The destination already exists.");
                    System.err.println("Use the 'force' flag to force the operation.");
                    return false;
                }
            }

            // create parent directory if not exist
            ensureParentDirExist(dst);

            // we can need that first to rename file
            if (!src.setWritable(true, false))
                src.setWritable(true, true);

            final long start = System.currentTimeMillis();

            // renameTo is not very reliable, better to do several try
            boolean done = src.renameTo(dst);

            while (!done && (System.currentTimeMillis() - start) < (10 * 1000))
            {
                // try to release objects which maintain lock
                System.gc();
                ThreadUtil.sleep(1000);

                // may help
                if (!src.setWritable(true, false))
                    src.setWritable(true, true);

                // retry
                done = src.renameTo(dst);
            }

            if (!done)
            {
                System.err.println("Cannot rename '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
                System.err.println("Check that the source file is not locked.");
                return false;
            }

            return true;
        }

        // missing input file
        System.err.println("Cannot rename '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
        System.err.println("Input file '" + src.getAbsolutePath() + "' not found !");

        return false;
    }

    /**
     * @deprecated Use {@link #rename(File, File, boolean)} instead
     */
    @Deprecated
    public static boolean rename(File src, File dst, boolean force, boolean wantHidden)
    {
        return rename(src, dst, force);
    }

    /**
     * @deprecated Use {@link #rename(String, String, boolean)} instead
     */
    @Deprecated
    public static boolean move(String src, String dst, boolean force)
    {
        return rename(src, dst, force);
    }

    /**
     * @deprecated Use {@link #move(String, String, boolean)} instead
     */
    @Deprecated
    public static boolean move(String src, String dst, boolean force, boolean wantHidden)
    {
        return move(src, dst, force);
    }

    /**
     * @deprecated Use {@link #rename(File, File, boolean)} instead
     */
    @Deprecated
    public static boolean move(File src, File dst, boolean force)
    {
        return rename(src, dst, force);
    }

    /**
     * @deprecated Use {@link #move(File, File, boolean)} instead
     */
    @Deprecated
    public static boolean move(File src, File dst, boolean force, boolean wantHidden)
    {
        return move(src, dst, force);
    }

    /**
     * Copy src to dst.<br>
     * Return true if file(s) successfully copied, false otherwise.
     * 
     * @param src
     *        source file or directory
     * @param dst
     *        destination file or directory
     * @param force
     *        force copy to previous existing file
     * @param recursive
     *        also copy sub directory
     * @return boolean
     */
    public static boolean copy(String src, String dst, boolean force, boolean recursive)
    {
        return copy(new File(getGenericPath(src)), new File(getGenericPath(dst)), force, recursive);
    }

    /**
     * @deprecated Use {@link #copy(String, String, boolean, boolean)} instead
     */
    @Deprecated
    public static boolean copy(String src, String dst, boolean force, boolean wantHidden, boolean recursive)
    {
        return copy(src, dst, force, recursive);
    }

    /**
     * Copy src to dst with the specified parameters.<br>
     * Return true if the operation succeed considering the specified parameters.<br>
     * That means if you try to copy a hidden file with wantHidden set to false then true is
     * returned<br>
     * even if the file is not copied.
     * 
     * @param src
     *        source file or directory
     * @param dst
     *        destination file or directory
     * @param force
     *        force copy to previous existing file
     * @param recursive
     *        also copy sub directory
     * @return boolean
     */
    public static boolean copy(File src, File dst, boolean force, boolean recursive)
    {
        return copy_(src, dst, force, recursive, false);
    }

    /**
     * @deprecated Use {@link #copy(File, File, boolean, boolean)} instead
     */
    @Deprecated
    public static boolean copy(File src, File dst, boolean force, boolean wantHidden, boolean recursive)
    {
        return copy(src, dst, force, recursive);
    }

    /**
     * internal copy
     */
    private static boolean copy_(File src, File dst, boolean force, boolean recursive, boolean inRecurse)
    {
        // directory copy ?
        if (src.isDirectory())
        {
            // no recursive copy --> end
            if (inRecurse && !recursive)
                return true;

            // so dst specify a directory too
            createDir(dst);

            boolean result = true;
            // get files list
            final String files[] = src.list();
            // recursive copy
            for (int i = 0; i < files.length; i++)
                result = result & copy_(new File(src, files[i]), new File(dst, files[i]), force, recursive, true);

            return result;
        }

        // single file copy
        if (src.exists())
        {
            // destination already exist ?
            if (dst.exists())
            {
                // copy only if force flag == true
                if (force)
                {
                    if (!delete(dst, true))
                    {
                        System.err.println("Cannot copy '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath()
                                + "'");
                        System.err.println("Reason : destination cannot be overwritten.");
                        System.err.println("Make sure it is not locked by another program (e.g. Eclipse)");
                        System.err.println("Also check that you have the rights to do this operation.");
                        return false;
                    }
                }
                else
                {
                    System.err
                            .println("Cannot copy '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
                    System.err.println("The destination already exists.");
                    System.err.println("Use the 'force' flag to force file copy.");
                    return false;
                }
            }

            boolean lnk;

            try
            {
                lnk = isLink(src);
            }
            catch (IOException e)
            {
                lnk = false;
            }

            // link file and link supported by OS ?
            if (lnk && SystemUtil.isLinkSupported())
            {
                // use OS dependent command (FIXME : replace by java 7 API when available)
                final Process process = SystemUtil.exec("cp -pRP " + src.getPath() + " " + dst.getPath());
                int res = 1;

                if (process != null)
                {
                    try
                    {
                        res = process.waitFor();
                    }
                    catch (InterruptedException e1)
                    {
                        // ignore;
                    }
                }

                // error while executing command
                if ((res != 0))
                {
                    System.err.println("FileUtil.copy(...) error while creating link '" + src.getPath() + "' to '"
                            + dst.getPath() + "'");

                    if (process != null)
                    {
                        // get error output and redirect it
                        final BufferedReader stderr = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()));

                        try
                        {
                            System.err.println(stderr.readLine());
                            if (stderr.ready())
                                System.err.println(stderr.readLine());
                        }
                        catch (IOException e)
                        {
                            // ignore
                        }
                    }
                    else if (res == 1)
                        System.err.println("Process interrupted.");

                    return false;
                }

                return true;
            }

            // get data to copy from src
            final byte[] data = load(src, true);
            // source data correctly loaded
            if (data != null)
            {
                // save in dst
                if (save(dst, data, true))
                {
                    // and set the last modified info.
                    dst.setLastModified(src.lastModified());
                    return true;
                }

                return false;
            }

            // cannot load input file
            System.err.println("Cannot copy '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
            System.err.println("Input file '" + src.getAbsolutePath() + "' data cannot be loaded !");

            return false;
        }

        // missing input file
        System.err.println("Cannot copy '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
        System.err.println("Input file '" + src.getAbsolutePath() + "' not found !");

        return false;
    }

    /**
     * Backup the specified file.<br>
     * Basically create a .bak version of the file. If the backup file already exist a postfix
     * number is automatically added.
     * 
     * @param filename
     *        file to backup
     * @return the backup filename is the operation success else return <code>null</code>
     */
    public static String backup(String filename)
    {
        int postfix = 0;
        String backupName = filename + ".bak";

        while (exists(backupName))
        {
            backupName = filename + "_" + StringUtil.toString(postfix, 3) + ".bak";
            postfix++;
        }

        if (FileUtil.copy(filename, backupName, true, false))
            return backupName;

        return null;
    }

    /**
     * Transform all directory entries by their sub files list
     */
    public static File[] explode(File[] files, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        final List<File> result = new ArrayList<File>();

        for (File file : files)
        {
            if (file.isDirectory())
                getFiles(file, filter, recursive, true, false, wantHidden, result);
            else
                result.add(file);
        }

        return result.toArray(new File[result.size()]);
    }

    /**
     * @deprecated Use {@link #explode(List, FileFilter, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<File> explode(List<File> files, boolean recursive, boolean wantHidden)
    {
        return explode(files, null, recursive, wantHidden);
    }

    /**
     * Transform all directory entries by their sub files list
     */
    public static List<File> explode(List<File> files, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        final List<File> result = new ArrayList<File>();

        for (File file : files)
        {
            if (file.isDirectory())
                getFiles(file, filter, recursive, true, false, wantHidden, result);
            else
                result.add(file);
        }

        return result;
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
    private static void getFiles(File f, FileFilter filter, boolean recursive, boolean wantFile, boolean wantDirectory,
            boolean wantHidden, List<File> list)
    {
        final File[] files = f.listFiles(filter);

        if (files != null)
        {
            for (File file : files)
            {
                if ((!file.isHidden()) || wantHidden)
                {
                    if (file.isDirectory())
                    {
                        if (wantDirectory)
                            list.add(file);
                        if (recursive)
                            getFiles(file, filter, recursive, wantFile, wantDirectory, wantHidden, list);
                    }
                    else if (wantFile)
                        list.add(file);
                }
            }
        }
    }

    /**
     * Get directory list from specified directory applying the specified parameters
     */
    public static File[] getDirectories(File file, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        final List<File> result = new ArrayList<File>();

        getFiles(file, filter, recursive, false, true, wantHidden, result);

        return result.toArray(new File[result.size()]);
    }

    /**
     * Returns an array of file denoting content of specified directory and parameters.
     * 
     * @param directory
     *        The directory we want to retrieve content.<br>
     * @param filter
     *        A file filter.<br>
     *        If the given <code>filter</code> is <code>null</code> then all pathnames are accepted.
     *        Otherwise, a pathname satisfies the filter if and only if the value <code>true</code> results when the
     *        <code>{@link FileFilter#accept(java.io.File)}</code> method of the
     *        filter is invoked on the pathname.
     * @param recursive
     *        If <code>true</code> then content from sub folder is also returned.
     * @param wantDirectory
     *        If <code>true</code> then directory entries are also returned.
     * @param wantHidden
     *        If <code>true</code> then file or directory with <code>hidden</code> attribute are
     *        also returned.
     * @see File#listFiles(FileFilter)
     */
    public static File[] getFiles(File directory, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        final List<File> result = new ArrayList<File>();

        getFiles(directory, filter, recursive, true, wantDirectory, wantHidden, result);

        return result.toArray(new File[result.size()]);
    }

    /**
     * Returns an array of file path denoting content of specified directory and parameters
     * (String format).
     * 
     * @param directory
     *        The directory we want to retrieve content.<br>
     * @param filter
     *        A file filter.<br>
     *        If the given <code>filter</code> is <code>null</code> then all files are accepted.
     *        Otherwise, a file satisfies the filter if and only if the value <code>true</code> results when the
     *        <code>{@link FileFilter#accept(java.io.File)}</code> method of the
     *        filter is invoked on the pathname.
     * @param recursive
     *        If <code>true</code> then content from sub folder is also returned.
     * @param wantDirectory
     *        If <code>true</code> then directory entries are also returned.
     * @param wantHidden
     *        If <code>true</code> then file or directory with <code>hidden</code> attribute are
     *        also returned.
     */
    public static String[] getFiles(String directory, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        final File[] files = getFiles(new File(getGenericPath(directory)), filter, recursive, wantDirectory, wantHidden);
        final String[] result = new String[files.length];

        for (int i = 0; i < files.length; i++)
            result[i] = files[i].getPath();

        return result;
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(String path, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        final ArrayList<File> result = new ArrayList<File>();

        getFiles(new File(getGenericPath(path)), filter, recursive, true, wantDirectory, wantHidden, result);

        return result;
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(File file, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        final ArrayList<File> result = new ArrayList<File>();

        getFiles(file, filter, recursive, true, wantDirectory, wantHidden, result);

        return result;
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(String path, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        return getFileList(path, filter, recursive, false, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(File file, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        return getFileList(file, filter, recursive, false, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(String path, boolean recursive, boolean wantDirectory, boolean wantHidden)
    {
        return getFileList(new File(getGenericPath(path)), null, recursive, wantDirectory, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(File file, boolean recursive, boolean wantDirectory, boolean wantHidden)
    {
        return getFileList(file, null, recursive, wantDirectory, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(File file, boolean recursive, boolean wantHidden)
    {
        return getFileList(file, recursive, false, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(File, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<File> getFileList(String path, boolean recursive, boolean wantHidden)
    {
        return getFileList(path, recursive, false, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(String, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<String> getFileListAsString(String path, FileFilter filter, boolean recursive,
            boolean wantDirectory, boolean wantHidden)
    {
        final ArrayList<File> files = getFileList(path, filter, recursive, wantDirectory, wantHidden);
        final ArrayList<String> result = new ArrayList<String>();

        for (File file : files)
            result.add(file.getPath());

        return result;
    }

    /**
     * @deprecated Use {@link #getFiles(String, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<String> getFileListAsString(String path, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        return getFileListAsString(path, null, recursive, wantDirectory, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(String, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<String> getFileListAsString(String path, FileFilter filter, boolean recursive,
            boolean wantHidden)
    {
        return getFileListAsString(path, filter, recursive, false, wantHidden);
    }

    /**
     * @deprecated Use {@link #getFiles(String, FileFilter, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static ArrayList<String> getFileListAsString(String path, boolean recursive, boolean wantHidden)
    {
        return getFileListAsString(path, recursive, false, wantHidden);
    }

    /**
     * Return true if the file described by specified path exists
     * 
     * @deprecated use {@link #exists(String)} instead
     */
    @Deprecated
    public static boolean exist(String path)
    {
        return exists(path);
    }

    /**
     * Return true if the file described by specified path exists
     */
    public static boolean exists(String path)
    {
        return new File(getGenericPath(path)).exists();
    }

    /**
     * Return true if the specified file is a directory
     */
    public static boolean isDirectory(String path)
    {
        return new File(getGenericPath(path)).isDirectory();
    }

    /**
     * Return true if the specified file is a link<br>
     * Be careful, it does work in almost case, but not all
     * 
     * @throws IOException
     */
    public static boolean isLink(String path) throws IOException
    {
        return isLink(new File(getGenericPath(path)));
    }

    /**
     * Return true if the specified file is a link<br>
     * Be careful, it does work in almost case, but not all
     * 
     * @throws IOException
     */
    public static boolean isLink(File file) throws IOException
    {
        if (file == null)
            return false;

        final File canon;

        if (file.getParent() == null)
            canon = file;
        else
            canon = new File(file.getParentFile().getCanonicalFile(), file.getName());

        // we want to ignore case whatever system does
        return !canon.getCanonicalFile().getAbsolutePath().equalsIgnoreCase(canon.getAbsolutePath());
    }

    public static boolean delete(String path, boolean recursive)
    {
        return delete(new File(getGenericPath(path)), recursive);
    }

    public static boolean delete(File f, boolean recursive)
    {
        boolean result = true;

        if (f.isDirectory())
        {
            final File[] files = f.listFiles();

            // delete files
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if (recursive)
                        result = result & delete(file, true);
                }
                else
                    result = result & file.delete();
            }

            // then delete empty directory
            result = result & f.delete();
        }
        else if (f.exists())
        {
            final long start = System.currentTimeMillis();

            // we can need that first to delete file
            if (!f.setWritable(true, false))
                f.setWritable(true, true);

            result = f.delete();

            // retry for locked file (we try for 15s max)
            while ((!result) && (System.currentTimeMillis() - start) < (10 * 1000))
            {
                // can help for file deletion...
                System.gc();
                ThreadUtil.sleep(1000);

                // may help
                if (!f.setWritable(true, false))
                    f.setWritable(true, true);

                result = f.delete();
            }
        }

        return result;
    }
}

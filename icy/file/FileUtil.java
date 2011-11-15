/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.file;

import icy.network.NetworkUtil;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.util.StringUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stephane
 */
public class FileUtil
{
    public static final char separatorChar = '/';
    public static final String separator = "/";

    public static String getGenericPath(String path)
    {
        if (path != null)
            return path.replace('\\', '/');

        return null;
    }

    public static String getTempDirectory()
    {
        return System.getProperty("java.io.tmpdir");
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

        final int dotIndex = finalPath.lastIndexOf(".");
        // ensure we are modifying an extension
        if (dotIndex >= 0 && (len - dotIndex) <= 5)
        {
            // we consider that an extension starting with a digit is not an extension
            if ((dotIndex + 1 < len) && Character.isDigit(finalPath.charAt(dotIndex + 1)))
                result += extension;
            else
                result = finalPath.substring(0, dotIndex) + extension;
        }
        else
            result += extension;

        return result;
    }

    private static void ensureParentDirExist(String filename)
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
     * Return directory information from specified path<br>
     * <br>
     * getDirectory("/file.txt") --> "/"<br>
     * getDirectory("D:/temp/file.txt") --> "D:/temp/"<br>
     * getDirectory("C:file.txt") --> "C:"<br>
     * getDirectory("file.txt") --> ""<br>
     * getDirectory("file") --> ""<br>
     * getDirectory(null) --> ""
     */
    public static String getDirectory(String path)
    {
        final String finalPath = getGenericPath(path);

        if (!StringUtil.isEmpty(finalPath))
        {
            int index = finalPath.lastIndexOf(FileUtil.separatorChar);
            if (index != -1)
                return finalPath.substring(0, index + 1);

            index = finalPath.lastIndexOf(':');
            if (index != -1)
                return finalPath.substring(0, index + 1);
        }

        return "";
    }

    public static String getFileName(String path)
    {
        return getFileName(path, true);
    }

    /**
     * Return filename information from specified path<br>
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
     * Rename the specified 'src' file to 'dst' file
     */
    public static boolean rename(String src, String dst, boolean force, boolean wantHidden)
    {
        return rename(new File(getGenericPath(src)), new File(getGenericPath(dst)), force, wantHidden);
    }

    /**
     * Rename the specified 'src' file to 'dst' file
     */
    public static boolean rename(File src, File dst, boolean force, boolean wantHidden)
    {
        // hidden file or directory and hidden flag == false
        if (src.isHidden() && !wantHidden)
            return false;

        if (src.exists())
        {
            if (dst.exists())
            {
                if (force)
                {
                    if (!delete(dst, true))
                    {
                        System.err.println("Can't move '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath()
                                + "'");
                        System.err.println("Reason : destination cannot be overwritten.");
                        System.err.println("Verify it is not locked by another program (as Eclipse)");
                        System.err.println("also check you've the rights to do this operation.");
                        return false;
                    }
                }
                else
                {
                    System.err.println("Can't move '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
                    System.err.println("The destination already exists.");
                    return false;
                }
            }

            // create parent directory if not exist
            ensureParentDirExist(dst);

            if (!src.renameTo(dst))
            {
                System.err.println("Can't move '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
                System.err.println("Reason : unknown");
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Move (same as rename) the specified 'src' file to 'dst' file
     */
    public static boolean move(String src, String dst, boolean force, boolean wantHidden)
    {
        return rename(src, dst, force, wantHidden);
    }

    /**
     * Move (same as rename) the specified 'src' file to 'dst' file
     */
    public static boolean move(File src, File dst, boolean force, boolean wantHidden)
    {
        return rename(src, dst, force, wantHidden);
    }

    /**
     * Copy src to dst<br>
     * Return true if file(s) successfully copied, false otherwise
     * 
     * @param src
     *        source file or directory
     * @param dst
     *        destination file or directory
     * @param force
     *        force copy to previous existing file
     * @param wantHidden
     *        also copy hidden file
     * @param recursive
     *        also copy sub directory
     * @return boolean
     */
    public static boolean copy(String src, String dst, boolean force, boolean wantHidden, boolean recursive)
    {
        return copy(new File(getGenericPath(src)), new File(getGenericPath(dst)), force, wantHidden, recursive);
    }

    /**
     * Copy src to dst<br>
     * Return true if file(s) successfully copied, false otherwise
     * 
     * @param src
     *        source file or directory
     * @param dst
     *        destination file or directory
     * @param force
     *        force copy to previous existing file
     * @param wantHidden
     *        also copy hidden file
     * @param recursive
     *        also copy sub directory
     * @return boolean
     */
    public static boolean copy(File src, File dst, boolean force, boolean wantHidden, boolean recursive)
    {
        return copy(src, dst, force, wantHidden, recursive, false);
    }

    public static boolean copy(File src, File dst, boolean force, boolean wantHidden, boolean recursive,
            boolean inRecurse)
    {
        // hidden file or directory and hidden flag == false
        if (src.isHidden() && !wantHidden)
            return false;

        // directory copy ?
        if (src.isDirectory())
        {
            // no recursive copy
            if (inRecurse && !recursive)
                return false;

            // so dst specify a directory too
            createDir(dst);

            boolean result = true;
            // get files list
            final String files[] = src.list();
            // recursive copy
            for (int i = 0; i < files.length; i++)
                result = result
                        & copy(new File(src, files[i]), new File(dst, files[i]), force, wantHidden, recursive, true);

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
                    if (!dst.delete())
                        return false;
                }
                else
                    return false;
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

                // error while executing command
                if (process == null)
                    return false;

                try
                {
                    return (process.waitFor() == 0);
                }
                catch (InterruptedException e)
                {
                    System.err.println("FileUtil.copy(...) on link file error :");
                    IcyExceptionHandler.showErrorMessage(e, false);
                    return false;
                }
            }

            // copy file
            final byte[] data = load(src, true);
            // and save in dst if correctly loaded
            if (data != null)
                return save(dst, data, true);
        }

        // source file does not exist
        return false;
    }

    /**
     * Transform all directory entries by their sub files list
     */
    public static ArrayList<File> explode(List<File> files, boolean recursive, boolean wantHidden)
    {
        return explode(files, null, recursive, wantHidden);
    }

    /**
     * Transform all directory entries by their sub files list
     */
    public static ArrayList<File> explode(List<File> files, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        final ArrayList<File> result = new ArrayList<File>();

        for (File file : files)
        {
            if (file.isDirectory())
                getFileList(file, filter, recursive, false, wantHidden, result);
            else
                result.add(file);
        }

        return result;
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
    public static ArrayList<String> getFileListAsString(String path, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        return getFileListAsString(path, null, recursive, wantDirectory, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
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
     * Get file list from specified directory applying the specified parameters
     */
    public static ArrayList<File> getFileList(String path, boolean recursive, boolean wantDirectory, boolean wantHidden)
    {
        return getFileList(new File(getGenericPath(path)), null, recursive, wantDirectory, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
    public static ArrayList<File> getFileList(String path, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        final ArrayList<File> result = new ArrayList<File>();

        getFileList(new File(getGenericPath(path)), filter, recursive, wantDirectory, wantHidden, result);

        return result;
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
    public static ArrayList<File> getFileList(File file, boolean recursive, boolean wantDirectory, boolean wantHidden)
    {
        return getFileList(file, null, recursive, wantDirectory, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
    public static ArrayList<File> getFileList(File file, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden)
    {
        final ArrayList<File> result = new ArrayList<File>();

        getFileList(file, filter, recursive, wantDirectory, wantHidden, result);

        return result;
    }

    /**
     * Get file list from specified directory applying the specified parameters
     */
    private static void getFileList(File f, FileFilter filter, boolean recursive, boolean wantDirectory,
            boolean wantHidden, ArrayList<File> list)
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
                            getFileList(file, filter, true, wantDirectory, wantHidden, list);
                    }
                    else
                        list.add(file);
                }
            }
        }
    }

    /**
     * Get file list from specified directory applying the specified parameters (doesn't return sub
     * directory)
     */
    public static ArrayList<String> getFileListAsString(String path, boolean recursive, boolean wantHidden)
    {
        return getFileListAsString(path, recursive, false, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters (doesn't return sub
     * directory)
     */
    public static ArrayList<String> getFileListAsString(String path, FileFilter filter, boolean recursive,
            boolean wantHidden)
    {
        return getFileListAsString(path, filter, recursive, false, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters (doesn't return sub
     * directory)
     */
    public static ArrayList<File> getFileList(String path, boolean recursive, boolean wantHidden)
    {
        return getFileList(path, recursive, false, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters (doesn't return sub
     * directory)
     */
    public static ArrayList<File> getFileList(String path, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        return getFileList(path, filter, recursive, false, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters (doesn't return sub
     * directory)
     */
    public static ArrayList<File> getFileList(File file, boolean recursive, boolean wantHidden)
    {
        return getFileList(file, recursive, false, wantHidden);
    }

    /**
     * Get file list from specified directory applying the specified parameters (doesn't return sub
     * directory)
     */
    public static ArrayList<File> getFileList(File file, FileFilter filter, boolean recursive, boolean wantHidden)
    {
        return getFileList(file, filter, recursive, false, wantHidden);
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
        else
            result = !f.exists() || f.delete();

        return result;
    }
}

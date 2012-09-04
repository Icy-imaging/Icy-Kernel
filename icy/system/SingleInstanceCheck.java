/**
 * 
 */
package icy.system;

import icy.file.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

/**
 * Class to verify we have a single instance of an object or whatever (use {@link FileLock}).
 * 
 * @author Stephane
 */
public class SingleInstanceCheck
{
    public static FileLock lock(String id)
    {
        FileLock result;
        final File f = new File(FileUtil.getGenericPath(FileUtil.getTempDirectory() + "/" + id + ".lock"));

        try
        {
            result = new FileOutputStream(f).getChannel().tryLock();
        }
        catch (Exception e)
        {
            result = null;
        }

        return result;
    }

    public static boolean release(FileLock lock)
    {
        if (lock != null)
        {
            try
            {
                lock.release();
                return true;
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        return false;
    }
}

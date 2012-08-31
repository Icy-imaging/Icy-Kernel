/**
 * 
 */
package icy.system;

import icy.file.FileUtil;
import icy.gui.dialog.ConfirmDialog;

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
    private FileLock lock;

    public SingleInstanceCheck(String id)
    {
        final File f = new File(FileUtil.getGenericPath(FileUtil.getTempDirectory() + "/" + id + ".lock"));

        try
        {
            lock = new FileOutputStream(f).getChannel().tryLock();
        }
        catch (Exception e)
        {
            lock = null;
        }

        if (lock == null)
        {
            if (!ConfirmDialog.confirm("ICY is already running on this computer. Start anyway ?"))
                System.exit(0);
        }
    }

    /**
     * Release lock
     */
    public void release()
    {
        if (lock != null)
        {
            try
            {
                lock.release();
            }
            catch (IOException e)
            {
                // ignore
                lock = null;
            }
        }
    }
}

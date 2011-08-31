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
package icy.main;

import icy.file.FileUtil;
import icy.gui.dialog.ConfirmDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

/**
 * @author fab & Stephane
 */
public class CheckUniqueTool
{
    private FileLock lock;

    /**
     * Stop the Unique socket.
     */
    public void releaseUnique()
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

    public CheckUniqueTool()
    {
        final File f = new File(FileUtil.getGenericPath(FileUtil.getTempDirectory() + "/icy.lock"));

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
}

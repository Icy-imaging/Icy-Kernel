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
        final File f = new File(FileUtil.getTempDirectory() + FileUtil.separator + id + ".lock");

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

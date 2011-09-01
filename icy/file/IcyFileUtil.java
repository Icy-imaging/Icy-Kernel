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

import icy.gui.frame.progress.ProgressFrame;
import icy.network.IcyNetworkUtil;

/**
 * @author stephane
 */
public class IcyFileUtil
{
    public static byte[] load(String path, boolean displayError)
    {
        return IcyNetworkUtil.download(path, displayError);
    }

    public static boolean save(String path, byte[] data, boolean displayError)
    {
        ProgressFrame taskFrame = new ProgressFrame("Saving file " + path + "...");
        try
        {
            return FileUtil.save(path, data, displayError);
        }
        finally
        {
            taskFrame.close();
        }
    }

}

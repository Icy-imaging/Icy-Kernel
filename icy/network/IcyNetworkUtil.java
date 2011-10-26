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
package icy.network;

import icy.gui.frame.progress.DownloadFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.system.thread.ThreadUtil;

import java.io.IOException;
import java.util.HashMap;

public class IcyNetworkUtil
{
    /**
     * Send report (asynchronous processing)<br>
     * 
     * @param values
     *        list of <key,value>
     */
    public static void report(final HashMap<String, String> values)
    {
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                postData(NetworkUtil.REPORT_URL, values, "sending report");
            }
        });
    }

    public static byte[] download(String path, boolean displayError)
    {
        final DownloadFrame frame = new DownloadFrame(path);

        try
        {
            return NetworkUtil.download(path, frame, displayError);
        }
        finally
        {
            frame.close();
        }
    }

    public static String postData(String target, String content, String message)
    {
        final ProgressFrame progressFrame = new ProgressFrame(message);

        try
        {
            try
            {
                return NetworkUtil.postData(target, content);
            }
            catch (IOException e)
            {
                System.err.println("postData(" + target + ", ...) error :");
                System.err.println(e.getMessage());
                return null;
            }
        }
        finally
        {
            progressFrame.close();
        }
    }

    public static String postData(String target, HashMap<String, String> values, String message)
    {
        final ProgressFrame progressFrame = new ProgressFrame(message);

        try
        {
            try
            {
                return NetworkUtil.postData(target, values);
            }
            catch (IOException e)
            {
                System.err.println("postData(" + target + ", ...) error :");
                System.err.println(e.getMessage());
                return null;
            }
        }
        finally
        {
            progressFrame.close();
        }
    }

    public static String postData(String target, HashMap<String, String> values, String login, String pass,
            String message)
    {
        final ProgressFrame progressFrame = new ProgressFrame(message);

        try
        {
            try
            {
                return NetworkUtil.postData(target, values, login, pass);
            }
            catch (IOException e)
            {
                System.err.println("postData(" + target + ", ...) error :");
                System.err.println(e.getMessage());
                return null;
            }
        }
        finally
        {
            progressFrame.close();
        }
    }

    public static String postData(String target, String content, String login, String pass, String message)
    {
        final ProgressFrame progressFrame = new ProgressFrame(message);

        try
        {
            try
            {
                return NetworkUtil.postData(target, content, login, pass);
            }
            catch (IOException e)
            {
                System.err.println("postData(" + target + ", ...) error :");
                System.err.println(e.getMessage());
                return null;
            }
        }
        finally
        {
            progressFrame.close();
        }
    }

}

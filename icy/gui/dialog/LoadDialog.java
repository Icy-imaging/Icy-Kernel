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
package icy.gui.dialog;

import icy.file.FileUtil;
import icy.system.thread.ThreadUtil;

import java.io.File;

import javax.swing.JFileChooser;

/**
 * @author Stephane
 */
public class LoadDialog
{
    private static class LoadDialogRunner implements Runnable
    {
        private final String title;
        private final String defaultDir;
        private final String defaultName;
        private final String extension;

        private JFileChooser dialog;
        String result;

        public LoadDialogRunner(String title, String defaultDir, String defaultName, String extension)
        {
            super();

            this.title = title;
            this.defaultDir = defaultDir;
            this.defaultName = defaultName;
            this.extension = extension;
        }

        @Override
        public void run()
        {
            result = null;

            final String defaultFileName;

            if ((defaultName != null) && (extension != null))
                defaultFileName = FileUtil.setExtension(defaultName, extension);
            else
                defaultFileName = defaultName;

            if (dialog == null)
                dialog = new JFileChooser();

            dialog.setDialogTitle(title);

            if (defaultDir != null)
                dialog.setCurrentDirectory(new File(defaultDir));

            if (defaultFileName != null)
                dialog.setSelectedFile(new File(defaultFileName));

            final int returnVal = dialog.showOpenDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION)
                return;

            final File f = dialog.getSelectedFile();
            if (!f.exists())
                return;

            result = f.getAbsolutePath();
        }
    }

    /**
     * Displays a file load dialog, using the specified default directory and file name and
     * extension
     */
    public static String chooseFile(String title, String defaultDir, String defaultName, String extension)
    {
        final LoadDialogRunner runner = new LoadDialogRunner(title, defaultDir, defaultName, extension);

        ThreadUtil.invokeNow(runner);

        return runner.result;
    }

    /**
     * Displays a file load dialog, using the specified default directory and file name
     */
    public static String chooseFile(String title, String defaultDir, String defaultName)
    {
        return chooseFile(title, defaultDir, defaultName, null);
    }

    /**
     * Displays a file load dialog, using the specified default directory and file name
     */
    public static String chooseFile(String defaultDir, String defaultName)
    {
        return chooseFile("Load file...", defaultDir, defaultName);
    }

    /**
     * Displays a file load dialog
     */
    public static String chooseFile()
    {
        return chooseFile(null, null);
    }

}

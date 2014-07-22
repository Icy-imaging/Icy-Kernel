/**
 * 
 */
package icy.gui.dialog;

import icy.file.FileUtil;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.io.File;

import javax.swing.JFileChooser;

/**
 * Simple dialog to let user select a file or a folder for open operation.
 * 
 * @author Stephane
 */
public class OpenDialog
{
    private static class OpenDialogRunner implements Runnable
    {
        private final String title;
        private final String defaultDir;
        private final String defaultName;
        private final String extension;

        private JFileChooser dialog;
        String result;

        public OpenDialogRunner(String title, String defaultDir, String defaultName, String extension)
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
        final OpenDialogRunner runner = new OpenDialogRunner(title, defaultDir, defaultName, extension);

        // no result in headless
        if (Icy.getMainInterface().isHeadLess())
            return null;

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

/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.action;

import icy.file.Saver;
import icy.gui.dialog.ImageLoaderDialog;
import icy.gui.dialog.ImageSaverDialog;
import icy.gui.menu.ApplicationMenu;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File actions (open / save / close...)
 * 
 * @author Stephane
 */
public class FileActions
{
    public static IcyAbstractAction clearRecentFilesAction = new IcyAbstractAction("Clear recent files", new IcyIcon(
            ResourceUtil.ICON_DOC_COPY), "Clear recent files", "Clear the list of last opened files")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4762494034660452392L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final ApplicationMenu appMenu = Icy.getMainInterface().getApplicationMenu();

            if (appMenu != null)
            {
                appMenu.getRecentFileList().clear();
                return true;
            }

            return false;
        }
    };

    public static IcyAbstractAction newSequenceAction = new IcyAbstractAction("New", new IcyIcon(
            ResourceUtil.ICON_DOC_NEW), "Create a new sequence")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4799299843248624925L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().addSequence(
                    new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1, DataType.UBYTE)));
            return true;
        }
    };

    public static IcyAbstractAction newGraySequenceAction = new IcyAbstractAction("Create gray sequence", new IcyIcon(
            ResourceUtil.ICON_DOC_NEW), "Create a new gray sequence",
            "Create a new single channel (gray level) sequence.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 797949281499261778L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().addSequence(
                    new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1, DataType.UBYTE)));
            return true;
        }
    };

    public static IcyAbstractAction newRGBSequenceAction = new IcyAbstractAction("Create RGB sequence", new IcyIcon(
            ResourceUtil.ICON_DOC_NEW), "Create a new RGB color sequence",
            "Create a 3 channels sequence (red, green, blue).")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5755927058175369657L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().addSequence(
                    new Sequence("RGB sequence", new IcyBufferedImage(512, 512, 3, DataType.UBYTE)));
            return true;
        }
    };

    public static IcyAbstractAction newARGBSequenceAction = new IcyAbstractAction("Create RGBA sequence", new IcyIcon(
            ResourceUtil.ICON_DOC_NEW), "Create a new RGBA color sequence",
            "Create a 4 channels sequence (red, green, blue, alpha).")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -142873334899977341L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().addSequence(
                    new Sequence("RGB sequence", new IcyBufferedImage(512, 512, 3, DataType.UBYTE)));
            return true;
        }
    };

    public static IcyAbstractAction openSequenceAction = new IcyAbstractAction("Open", new IcyIcon(
            ResourceUtil.ICON_OPEN), "Open a sequence from file(s)", KeyEvent.VK_O, SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = 7399973037052771669L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new ImageLoaderDialog();
            return true;
        }
    };

    public static IcyAbstractAction saveSequenceAction = new IcyAbstractAction("Save", new IcyIcon(
            ResourceUtil.ICON_SAVE), "Save active sequence")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8450533919443304021L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final Sequence seq = viewer.getSequence();

            if (seq != null)
            {
                final String filename = seq.getFilename();

                if (StringUtil.isEmpty(filename))
                    new ImageSaverDialog(seq, viewer.getPositionZ(), viewer.getPositionT());
                else
                {
                    // background process
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final File file = new File(filename);

                            Saver.save(seq, file, !file.exists() || file.isDirectory(), true);
                        }
                    });
                }

                return true;
            }

            return false;
        }
    };

    public static IcyAbstractAction saveAsSequenceAction = new IcyAbstractAction("Save...", new IcyIcon(
            ResourceUtil.ICON_SAVE), "Save active sequence", "Save the active sequence under selected file name",
            KeyEvent.VK_S, SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3556923605878121275L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence seq = viewer.getSequence();

                if (seq != null)
                {
                    new ImageSaverDialog(seq, viewer.getPositionZ(), viewer.getPositionT());
                    return true;
                }
            }

            return false;
        }
    };

    public static IcyAbstractAction closeSequenceAction = new IcyAbstractAction("Close", new IcyIcon(
            ResourceUtil.ICON_CLOSE), "Close active sequence")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9023064791162525318L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                viewer.close();
                return true;
            }

            return false;
        }
    };

    public static IcyAbstractAction closeCurrentSequenceAction = new IcyAbstractAction("Close sequence", new IcyIcon(
            ResourceUtil.ICON_CLOSE), "Close active sequence", "Close the current active sequence")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -1127914432836889905L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                viewer.close();
                return true;
            }

            return false;
        }
    };

    public static IcyAbstractAction closeOthersSequencesAction = new IcyAbstractAction("Close others", new IcyIcon(
            ResourceUtil.ICON_CLOSE), "Close others sequences", "Close all opened sequences except the active one.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8595244752658024122L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer focusedViewer = Icy.getMainInterface().getActiveViewer();

            for (Viewer viewer : Icy.getMainInterface().getViewers())
                if (viewer != focusedViewer)
                    viewer.close();

            return true;
        }
    };

    public static IcyAbstractAction closeAllSequencesAction = new IcyAbstractAction("Close all", new IcyIcon(
            ResourceUtil.ICON_CLOSE), "Close all sequences", "Close all opened sequences.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -1343557201445697749L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().closeAllViewers();
            return true;
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : FileActions.class.getFields())
        {
            final Class<?> type = field.getType();

            try
            {
                if (type.isAssignableFrom(IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (type.isAssignableFrom(IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return result;
    }
}

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
package icy.action;

import icy.file.Loader;
import icy.file.Saver;
import icy.gui.dialog.ImageSaverDialog;
import icy.gui.dialog.LoaderDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.SaverDialog;
import icy.gui.menu.ApplicationMenu;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.util.ClassUtil;
import icy.util.StringUtil;

import java.awt.Rectangle;
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
    public static class OpenSequenceRegionAction extends IcyAbstractAction
    {
        final int resolution;

        public OpenSequenceRegionAction(int resolution)
        {
            super("Open at resolution 1/" + ((int) Math.pow(2, resolution)), null, "Open region at resolution 1/"
                    + ((int) Math.pow(2, resolution)),
                    "Open the selected region from the original image at resolution 1/"
                            + ((int) Math.pow(2, resolution)), true, null);

            this.resolution = resolution;
        }

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final int originRes = sequence.getOriginResolution();
                final String path = sequence.getFilename();

                if (!StringUtil.isEmpty(path))
                {
                    List<ROI> rois = sequence.getROIs();
                    int size = rois.size();

                    if (size == 0)
                    {
                        MessageDialog
                                .showDialog(
                                        "There is no ROI in the current sequence.\nYou need a ROI to define the region to open.",
                                        MessageDialog.INFORMATION_MESSAGE);
                        return false;
                    }
                    else if (size > 1)
                    {
                        rois = sequence.getSelectedROIs();
                        size = rois.size();

                        if (size == 0)
                        {
                            MessageDialog.showDialog("You need to select a ROI to do this operation.",
                                    MessageDialog.INFORMATION_MESSAGE);
                            return false;
                        }
                        else if (size > 1)
                        {
                            MessageDialog.showDialog("You must have only one selected ROI to do this operation.",
                                    MessageDialog.INFORMATION_MESSAGE);
                            return false;
                        }
                    }

                    final ROI roi = rois.get(0);
                    final Rectangle bounds = SequenceUtil.getOriginRectangle(roi.getBounds5D().toRectangle2D().getBounds(),sequence);
                    
                    if (!bounds.isEmpty())
                    {
                        final Sequence regionSequence = Loader.loadSequence(null, path, sequence.getSerieIndex(),
                                resolution, bounds, -1, -1, -1, -1, -1, false, true);

                        if (regionSequence != null)
                        {
                            Icy.getMainInterface().addSequence(regionSequence);
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (!StringUtil.isEmpty(seq.getFilename()) && seq.hasROI());
        }
    }

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

    public static IcyAbstractAction newSequenceAction = new IcyAbstractAction("Create", new IcyIcon(
            ResourceUtil.ICON_DOC_NEW), "Create an empty sequence")
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
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    Icy.getMainInterface().addSequence(
                            new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1, DataType.UBYTE)));
                }
            });

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
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    Icy.getMainInterface().addSequence(
                            new Sequence("RGB sequence", new IcyBufferedImage(512, 512, 3, DataType.UBYTE)));
                }
            });

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
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    final IcyBufferedImage image = new IcyBufferedImage(512, 512, 4, DataType.UBYTE);
                    Arrays.fill(image.getDataXYAsByte(3), (byte) -1);
                    Icy.getMainInterface().addSequence(new Sequence("RGBA sequence", image));
                }
            });

            return true;
        }
    };

    public static IcyAbstractAction openSequenceAction = new IcyAbstractAction("Open", new IcyIcon(
            ResourceUtil.ICON_OPEN), "Open a sequence",
            "Display a file selection dialog and choose the sequence file to open", KeyEvent.VK_O,
            SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = 7399973037052771669L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new LoaderDialog();
            return true;
        }
    };

    public static IcyAbstractAction saveSequenceAction = new IcyAbstractAction("Save", new IcyIcon(
            ResourceUtil.ICON_SAVE), "Save active sequence", "Save the active sequence with its default filename")
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
                final String filename = seq.getOutputFilename(true);

                if (StringUtil.isEmpty(filename))
                    new SaverDialog(seq, true);
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

    public static IcyAbstractAction saveDefaultSequenceAction = new IcyAbstractAction("Save...", new IcyIcon(
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

    public static IcyAbstractAction saveAsSequenceAction = new IcyAbstractAction("Save as...", new IcyIcon(
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
                    new SaverDialog(seq);
                    return true;
                }
            }

            return false;
        }
    };

    public static IcyAbstractAction saveMetaDataAction = new IcyAbstractAction("Save metadata", new IcyIcon(
            ResourceUtil.ICON_SAVE), "Save active sequence metadata", "Save the metadata of the active sequence now",
            true, "Saving metadata...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3556923605878121275L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            // Sequence persistence enabled --> save XML metadata
            if (GeneralPreferences.getSequencePersistence())
            {
                final Sequence seq = Icy.getMainInterface().getActiveSequence();
                if (seq != null)
                    return seq.saveXMLData();
            }

            return false;
        }
    };

    public static IcyAbstractAction closeSequenceAction = new IcyAbstractAction("Close", new IcyIcon(
            ResourceUtil.ICON_CLOSE), "Close active sequence", "Close the current active sequence")
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
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
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

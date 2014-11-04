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

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.main.MainFrame;
import icy.gui.sequence.tools.SequenceCanvasResizeFrame;
import icy.gui.sequence.tools.SequenceDimensionAdjustFrame;
import icy.gui.sequence.tools.SequenceDimensionConvertFrame;
import icy.gui.sequence.tools.SequenceDimensionExtendFrame;
import icy.gui.sequence.tools.SequenceDimensionMergeFrame;
import icy.gui.sequence.tools.SequenceResizeFrame;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.SequenceUtil;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.undo.IcyUndoManager;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Actions for "Sequence Operation" tab.
 * 
 * @author Stephane
 */
public class SequenceOperationActions
{
    static class SequenceConvertAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 614601313456867774L;

        final DataType dataType;
        final boolean scaled;

        public SequenceConvertAction(DataType dataType, boolean scaled)
        {
            super(dataType.toString(true), new IcyIcon(ResourceUtil.ICON_BAND_RIGHT), "Convert to "
                    + dataType.toString(true), "Convert sequence data type to " + dataType.toString(true), true,
                    "Converting sequence to " + dataType.toString(false) + " ...");

            this.dataType = dataType;
            this.scaled = scaled;
        }

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null)
                {
                    final Sequence out = SequenceUtil.convertToType(Icy.getMainInterface().getActiveSequence(),
                            dataType, scaled);

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // get output viewer
                            final Viewer vout = new Viewer(out);
                            // copy colormap from input viewer
                            vout.getLut().copyFrom(viewer.getLut());
                        }
                    });

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    }

    static class SequenceColorAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3775617713982984867L;

        private static final Image images[] = {null, ResourceUtil.ICON_RGB_COLOR, ResourceUtil.ICON_ARGB_COLOR, null,
                null, null, null, null, null, null, ResourceUtil.ICON_GRAY_COLOR, null, null, null, null, null};
        private static final String names[] = {null, "RGB image", "ARGB image", null, null, null, null, null, null,
                null, "Gray image", null, null, null, null, null};
        private static final String titles[] = {null, "Build RGB image", "Build ARGB image", null, null, null, null,
                null, null, null, "Build gray image", null, null, null, null, null};
        private static final String tooltips[] = {
                null,
                "Create a RGB color rendered version of the current sequence.\nResulting sequence is 3 channels with unsigned byte (8 bits) data type.",
                "Create an ARGB color (support transparency) rendered version of the current sequence.\nResulting sequence is 4 channels with unsigned byte (8 bits) data type.",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Create a gray rendered version of the current sequence.\nResulting sequence is single channel with unsigned byte (8 bits) data type.",
                null, null, null, null, null};
        private static final String processMessages[] = {null, "Converting to RGB image...",
                "Converting to ARGB image...", null, null, null, null, null, null, null, "Converting to gray image...",
                null, null, null, null, null};

        final int imageType;

        public SequenceColorAction(int imageType)
        {
            super(names[imageType], new IcyIcon(images[imageType], false), titles[imageType], tooltips[imageType],
                    true, processMessages[imageType]);

            this.imageType = imageType;
        }

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null)
                {
                    // convert the sequence
                    final Sequence out = SequenceUtil.convertColor(sequence, imageType, viewer.getLut());
                    Icy.getMainInterface().addSequence(out);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    }

    public static class ExtractChannelAction extends IcyAbstractAction
    {
        private static final long serialVersionUID = -8722922231336771871L;

        final int channel;

        public ExtractChannelAction(int channel)
        {
            super((channel == -1) ? "all channels" : "channel " + channel, new IcyIcon(
                    ResourceUtil.ICON_INDENT_DECREASE), (channel == -1) ? "Extract all channels" : "Extract channel "
                    + channel, (channel == -1) ? "Separate all channels of active sequence"
                    : "Create a new single channel sequence from channel " + channel + " of active sequence", true,
                    (channel == -1) ? "Extracting channel(s)..." : "Extracting channel " + channel + "...");

            this.channel = channel;
        }

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                if (channel == -1)
                {
                    for (int c = 0; c < sequence.getSizeC(); c++)
                        Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(sequence, c));
                }
                else
                    Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(sequence, channel));

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (channel < seq.getSizeC());
        }
    }

    public static class RemoveChannelAction extends IcyAbstractAction
    {
        private static final long serialVersionUID = 66288944320765300L;

        final int channel;

        public RemoveChannelAction(int channel)
        {
            super("channel " + channel, new IcyIcon(ResourceUtil.ICON_INDENT_REMOVE), "Remove channel " + channel,
                    "Remove channel " + channel + " from active sequence", true, "Removing channel " + channel + "...");

            this.channel = channel;
        }

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                // create undo point
                final boolean canUndo = sequence.createUndoDataPoint("Channel " + channel + "removed");

                // cannot backup
                if (!canUndo)
                {
                    // ask confirmation to continue
                    if (!IdConfirmDialog.confirm("Not enough memory to undo the operation, do you want to continue ?",
                            "ChannelRemoveNoUndoConfirm"))
                        return false;
                }

                SequenceUtil.removeChannel(sequence, channel);

                // no undo, clear undo manager after modification
                if (!canUndo)
                    sequence.clearUndoManager();

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (channel < seq.getSizeC());
        }
    }

    public static class MergeDimensionAction extends IcyAbstractAction
    {
        private static final long serialVersionUID = -3859065456632266213L;

        private static final String titles[] = {null, null, null, "Merge channels", "Merge Z slices", "Merge T frames"};
        private static final String tooltips[] = {null, null, null,
                "Merge channels from severals input sequences to build a new sequence.",
                "Merge Z slices from severals input sequences to build a new sequence.",
                "Merge T frames from severals input sequences to build a new sequence."};

        final DimensionId dim;

        public MergeDimensionAction(DimensionId dim)
        {
            super("Merge...", new IcyIcon(ResourceUtil.ICON_INDENT_INCREASE), titles[dim.ordinal()], tooltips[dim
                    .ordinal()]);

            this.dim = dim;
        }

        @Override
        public boolean doAction(ActionEvent e)
        {
            new SequenceDimensionMergeFrame(dim);
            return true;
        }
    }

    public static IcyAbstractAction cloneSequenceAction = new IcyAbstractAction("Duplicate", new IcyIcon(
            ResourceUtil.ICON_DUPLICATE), "Duplicate sequence", "Create a fresh copy of the sequence", true,
            "Duplicating sequence...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6907103082567189377L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            if (viewer == null)
                return false;

            final Sequence seq = viewer.getSequence();
            if (seq == null)
                return false;

            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    // create output sequence
                    final Sequence out = SequenceUtil.getCopy(seq);

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // get output viewer
                            final Viewer vout = new Viewer(out);
                            // copy colormap from input viewer
                            vout.getLut().copyFrom(viewer.getLut());
                        }
                    });
                }
            });

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertUByteScaledSequenceAction = new SequenceConvertAction(DataType.UBYTE, true);
    public static IcyAbstractAction convertUByteSequenceAction = new SequenceConvertAction(DataType.UBYTE, false);
    public static IcyAbstractAction convertByteScaledSequenceAction = new SequenceConvertAction(DataType.BYTE, true);
    public static IcyAbstractAction convertByteSequenceAction = new SequenceConvertAction(DataType.BYTE, false);
    public static IcyAbstractAction convertUShortScaledSequenceAction = new SequenceConvertAction(DataType.USHORT, true);
    public static IcyAbstractAction convertUShortSequenceAction = new SequenceConvertAction(DataType.USHORT, false);
    public static IcyAbstractAction convertShortScaledSequenceAction = new SequenceConvertAction(DataType.SHORT, true);
    public static IcyAbstractAction convertShortSequenceAction = new SequenceConvertAction(DataType.SHORT, false);
    public static IcyAbstractAction convertUIntScaledSequenceAction = new SequenceConvertAction(DataType.UINT, true);
    public static IcyAbstractAction convertUIntSequenceAction = new SequenceConvertAction(DataType.UINT, false);
    public static IcyAbstractAction convertIntScaledSequenceAction = new SequenceConvertAction(DataType.INT, true);
    public static IcyAbstractAction convertIntSequenceAction = new SequenceConvertAction(DataType.INT, false);
    public static IcyAbstractAction convertFloatScaledSequenceAction = new SequenceConvertAction(DataType.FLOAT, true);
    public static IcyAbstractAction convertFloatSequenceAction = new SequenceConvertAction(DataType.FLOAT, false);
    public static IcyAbstractAction convertDoubleScaledSequenceAction = new SequenceConvertAction(DataType.DOUBLE, true);
    public static IcyAbstractAction convertDoubleSequenceAction = new SequenceConvertAction(DataType.DOUBLE, false);

    // color operations
    public static IcyAbstractAction argbSequenceAction = new SequenceColorAction(BufferedImage.TYPE_INT_ARGB);
    public static IcyAbstractAction rgbSequenceAction = new SequenceColorAction(BufferedImage.TYPE_INT_RGB);
    public static IcyAbstractAction graySequenceAction = new SequenceColorAction(BufferedImage.TYPE_BYTE_GRAY);

    // XY plan operations
    public static IcyAbstractAction cropSequenceAction = new IcyAbstractAction("Fast crop", new IcyIcon(
            ResourceUtil.ICON_CUT), "Fast crop image", "Crop an image from a ROI.")
    {
        private static final long serialVersionUID = 2928113834852115366L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            if (viewer == null)
                return false;

            final Sequence seq = viewer.getSequence();
            if (seq == null)
                return false;

            List<ROI> rois = seq.getROIs();
            int size = rois.size();

            if (size == 0)
            {
                MessageDialog.showDialog("There is no ROI in the current sequence.\nCrop operation need a ROI.",
                        MessageDialog.INFORMATION_MESSAGE);
                return false;
            }
            else if (size > 1)
            {
                rois = seq.getSelectedROIs();
                size = rois.size();

                if (size == 0)
                {
                    MessageDialog.showDialog("You need to select a ROI to do the crop operation.",
                            MessageDialog.INFORMATION_MESSAGE);
                    return false;
                }
                else if (size > 1)
                {
                    MessageDialog.showDialog("You must have only one selected ROI to do the crop operation.",
                            MessageDialog.INFORMATION_MESSAGE);
                    return false;
                }
            }

            final ROI roi = rois.get(0);

            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {

                    // create output sequence
                    final Sequence out = SequenceUtil.getSubSequence(seq, roi);

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // get output viewer
                            final Viewer vout = new Viewer(out);
                            // copy colormap from input viewer
                            vout.getLut().copyFrom(viewer.getLut());
                        }
                    });
                }
            });

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction canvasResizeAction = new IcyAbstractAction("Canvas size...", new IcyIcon(
            ResourceUtil.ICON_CROP), "Canvas resize", "Resize the canvas without changing image size.")
    {
        private static final long serialVersionUID = 9156831541828750627L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceCanvasResizeFrame(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction imageResizeAction = new IcyAbstractAction("Image size...", new IcyIcon(
            ResourceUtil.ICON_FIT_CANVAS), "Image resize", "Resize the image.")
    {
        private static final long serialVersionUID = -4731940627380446776L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceResizeFrame(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    // channel operations
    public static IcyAbstractAction extractAllChannelAction = new ExtractChannelAction(-1);
    public static IcyAbstractAction extractChannelActions[] = {new ExtractChannelAction(0),
            new ExtractChannelAction(1), new ExtractChannelAction(2), new ExtractChannelAction(3),
            new ExtractChannelAction(4), new ExtractChannelAction(5)};
    public static IcyAbstractAction removeChannelActions[] = {new RemoveChannelAction(0), new RemoveChannelAction(1),
            new RemoveChannelAction(2), new RemoveChannelAction(3), new RemoveChannelAction(4),
            new RemoveChannelAction(5)};
    public static IcyAbstractAction mergeChannelsAction = new MergeDimensionAction(DimensionId.C);

    // Z operations
    public static IcyAbstractAction reverseSlicesAction = new IcyAbstractAction("Reverse order", new IcyIcon(
            ResourceUtil.ICON_LAYER_REVERSE_V), "Reverse Z slices", "Reverse Z slices order", false,
            "Reversing slices...")
    {
        private static final long serialVersionUID = -4731940627380446776L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                SequenceUtil.reverseZ(sequence);
                return true;
            }

            return false;
        }
    };

    public static IcyAbstractAction extractSliceAction = new IcyAbstractAction("Extract slice", new IcyIcon(
            ResourceUtil.ICON_LAYER_EXTRACT_V), "Extract current Z slice",
            "Create a new sequence by extracting current Z slice of active sequence.", false, "Extracting slice...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3731161374656240419L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null)
                {
                    final int z = viewer.getPositionZ();

                    if (z != -1)
                    {
                        final Sequence out = SequenceUtil.extractSlice(sequence, z);

                        ThreadUtil.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // get output viewer
                                final Viewer vout = new Viewer(out);
                                // copy colormap from input viewer
                                vout.getLut().copyFrom(viewer.getLut());
                            }
                        });

                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int z = (viewer == null) ? -1 : viewer.getPositionZ();

            return super.isEnabled() && (z != -1);
        }
    };

    public static IcyAbstractAction removeSliceAction = new IcyAbstractAction("Remove slice", new IcyIcon(
            ResourceUtil.ICON_LAYER_REMOVE_V), "Remove current Z slice",
            "Remove the current Z slice of active sequence.", false, "Removing slice...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6588564641490390145L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int z = (viewer == null) ? -1 : viewer.getPositionZ();

            if (z != -1)
            {
                SequenceUtil.removeZAndShift(viewer.getSequence(), z);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int z = (viewer == null) ? -1 : viewer.getPositionZ();

            return super.isEnabled() && (z != -1);
        }
    };

    public static IcyAbstractAction addSlicesAction = new IcyAbstractAction("Add...", new IcyIcon(
            ResourceUtil.ICON_LAYER_ADD_V), "Add slice(s)",
            "Extends Z dimension by adding empty or duplicating slices.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -1967473595758834348L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceDimensionExtendFrame(Icy.getMainInterface().getActiveSequence(), DimensionId.Z);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static IcyAbstractAction mergeSlicesAction = new MergeDimensionAction(DimensionId.Z);

    public static IcyAbstractAction removeSlicesAction = new IcyAbstractAction("Remove...", new IcyIcon(
            ResourceUtil.ICON_LAYER_REMOVE_ADV_V), "Advanced slice remove", "Advanced Z slice remove operation.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -1899409406755437158L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceDimensionAdjustFrame(sequence, DimensionId.Z);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    // T operations
    public static IcyAbstractAction reverseFramesAction = new IcyAbstractAction("Reverse order", new IcyIcon(
            ResourceUtil.ICON_LAYER_REVERSE_H), "Reverse T frames", "Reverse T frames order", false,
            "Reversing frames...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2403122454093281595L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                SequenceUtil.reverseT(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction extractFrameAction = new IcyAbstractAction("Extract frame", new IcyIcon(
            ResourceUtil.ICON_LAYER_EXTRACT_H), "Extract current T frame",
            "Create a new sequence by extracting current T frame of active sequence.", false, "Extracting frame...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -5809053788547447661L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null)
                {
                    final int t = viewer.getPositionT();

                    if (t != -1)
                    {
                        final Sequence out = SequenceUtil.extractFrame(sequence, t);

                        ThreadUtil.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // get output viewer
                                final Viewer vout = new Viewer(out);
                                // copy colormap from input viewer
                                vout.getLut().copyFrom(viewer.getLut());
                            }
                        });

                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int t = (viewer == null) ? -1 : viewer.getPositionT();

            return super.isEnabled() && (t != -1);
        }
    };

    public static IcyAbstractAction removeFrameAction = new IcyAbstractAction("Remove frame", new IcyIcon(
            ResourceUtil.ICON_LAYER_REMOVE_H), "Remove current T frame",
            "Remove the current T frame of active sequence.", false, "Removing frame...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6113522706924858672L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int t = (viewer == null) ? -1 : viewer.getPositionT();

            if (t != -1)
            {
                SequenceUtil.removeTAndShift(viewer.getSequence(), t);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int t = (viewer == null) ? -1 : viewer.getPositionT();

            return super.isEnabled() && (t != -1);
        }
    };

    public static IcyAbstractAction addFramesAction = new IcyAbstractAction("Add...", new IcyIcon(
            ResourceUtil.ICON_LAYER_ADD_H), "Add frame(s)",
            "Extends T dimension by adding empty or duplicating frames.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6106326145960291510L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceDimensionExtendFrame(Icy.getMainInterface().getActiveSequence(), DimensionId.T);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static IcyAbstractAction mergeFramesAction = new MergeDimensionAction(DimensionId.T);

    public static IcyAbstractAction removeFramesAction = new IcyAbstractAction("Remove...", new IcyIcon(
            ResourceUtil.ICON_LAYER_REMOVE_ADV_H), "Advanced frame remove", "Advanced T frame remove operation.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7963804798009814712L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceDimensionAdjustFrame(sequence, DimensionId.T);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    // ZT conversion
    public static IcyAbstractAction convertToSlicesAction = new IcyAbstractAction("Convert to stack", new IcyIcon(
            ResourceUtil.ICON_LAYER_V1), "Convert to stack", "Set all images in Z dimension.", true,
            "Converting to stack...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5987495169612852524L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();
                final int t = viewer.getPositionT();

                if (sequence != null)
                {
                    SequenceUtil.convertToStack(sequence);
                    viewer.setPositionZ(t);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertToFramesAction = new IcyAbstractAction("Convert to time", new IcyIcon(
            ResourceUtil.ICON_LAYER_H1), "Convert to time sequence", "Set all images in T dimension.", true,
            "Converting to time...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6555855298812635009L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();
                final int z = viewer.getPositionZ();

                if (sequence != null)
                {
                    SequenceUtil.convertToTime(sequence);
                    viewer.setPositionT(z);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction advancedZTConvertAction = new IcyAbstractAction("Advanced...", new IcyIcon(
            ResourceUtil.ICON_COG), "Advanced dimension conversion", "Advanced dimension conversion operation.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 110261266295404071L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                new SequenceDimensionConvertFrame(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static IcyAbstractAction fillSequenceAction = new IcyAbstractAction("Fill", new IcyIcon(
            ResourceUtil.ICON_BRUSH), "Fill ROI content",
            "Fill the selected ROI content with specified value on whole sequence", true, "Filling content")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 110261266295404071L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null)
                {
                    final double value = mainFrame.getMainRibbon().getSequenceOperationTask().getFillValue();

                    for (ROI roi : sequence.getSelectedROIs())
                        DataIteratorUtil.set(new SequenceDataIterator(sequence, roi), value);

                    sequence.dataChanged();

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static IcyAbstractAction getConvertSequenceAction(DataType dataType, boolean scaled)
    {
        switch (dataType)
        {
            case UBYTE:
                if (scaled)
                    return convertUByteScaledSequenceAction;
                return convertUByteSequenceAction;

            case BYTE:
                if (scaled)
                    return convertByteScaledSequenceAction;
                return convertByteSequenceAction;

            case USHORT:
                if (scaled)
                    return convertUShortScaledSequenceAction;
                return convertUShortSequenceAction;

            case SHORT:
                if (scaled)
                    return convertShortScaledSequenceAction;
                return convertShortSequenceAction;

            case UINT:
                if (scaled)
                    return convertUIntScaledSequenceAction;
                return convertUIntSequenceAction;

            case INT:
                if (scaled)
                    return convertIntScaledSequenceAction;
                return convertIntSequenceAction;

            case FLOAT:
                if (scaled)
                    return convertFloatScaledSequenceAction;
                return convertFloatSequenceAction;

            case DOUBLE:
                if (scaled)
                    return convertDoubleScaledSequenceAction;
                return convertDoubleSequenceAction;

            default:
                // not supported
                return null;
        }
    }

    public static IcyAbstractAction undoAction = new IcyAbstractAction("Undo", new IcyIcon(ResourceUtil.ICON_UNDO),
            "Undo last operation", KeyEvent.VK_Z, SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5773755313377178022L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            return Icy.getMainInterface().undo();
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction redoAction = new IcyAbstractAction("Redo", new IcyIcon(ResourceUtil.ICON_REDO),
            "Redo last operation", KeyEvent.VK_Y, SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1288382252962040008L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            return Icy.getMainInterface().redo();
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction undoClearAction = new IcyAbstractAction("Clear history", new IcyIcon(
            ResourceUtil.ICON_DELETE), "Clear all history (can release some memory)")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1251314072585735122L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (undoManager != null)
            {
                undoManager.discardAllEdits();
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction undoClearOldsAction = new IcyAbstractAction("Clear olders", new IcyIcon(
            ResourceUtil.ICON_CLEAR_BEFORE), "Clear all history except the last operation (can release some memory)")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5773755313377178022L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (undoManager != null)
            {
                undoManager.discardOldEdits(1);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction undoClearFuturesAction = new IcyAbstractAction("Clear futures", new IcyIcon(
            ResourceUtil.ICON_CLEAR_AFTER), "Clear all future operations from history (can release some memory)")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -395525273305262280L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (undoManager != null)
            {
                undoManager.discardFutureEdits(0);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : SequenceOperationActions.class.getFields())
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

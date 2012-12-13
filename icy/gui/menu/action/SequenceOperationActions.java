/**
 * 
 */
package icy.gui.menu.action;

import icy.common.IcyAbstractAction;
import icy.gui.menu.tools.SequenceCropper;
import icy.gui.sequence.tools.SequenceCanvasResizeFrame;
import icy.gui.sequence.tools.SequenceDimensionAdjustFrame;
import icy.gui.sequence.tools.SequenceDimensionConvertFrame;
import icy.gui.sequence.tools.SequenceDimensionExtendFrame;
import icy.gui.sequence.tools.SequenceDimensionMergeFrame;
import icy.gui.sequence.tools.SequenceResizeFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImageUtil;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.type.DataType;
import icy.util.OMEUtil;

import java.awt.Image;
import java.awt.event.ActionEvent;
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
            super(dataType.toString(true), null, "Convert to " + dataType.toString(true),
                    "Convert sequence data type to " + dataType.toString(true), true, "Converting sequence to "
                            + dataType.toString(false) + " ...");

            this.dataType = dataType;
            this.scaled = scaled;
        }

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                Icy.getMainInterface().addSequence(
                        SequenceUtil.convertToType(Icy.getMainInterface().getFocusedSequence(), dataType, scaled));
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

            if (viewer != null)
            {
                final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                if (sequence != null)
                {
                    final LUT lut = viewer.getLut();
                    final Sequence out = new Sequence(OMEUtil.createOMEMetadata(sequence.getMetadata()));

                    // image receiver
                    final BufferedImage imgOut = new BufferedImage(sequence.getSizeX(), sequence.getSizeY(), imageType);

                    out.beginUpdate();
                    try
                    {
                        for (int t = 0; t < sequence.getSizeT(); t++)
                            for (int z = 0; z < sequence.getSizeZ(); z++)
                                // ARGB image is converted so we can safely always use
                                // the same input image
                                out.setImage(t, z,
                                        IcyBufferedImageUtil.toBufferedImage(sequence.getImage(t, z), imgOut, lut));
                    }
                    finally
                    {
                        out.endUpdate();
                    }

                    // rename channels
                    out.setChannelName(0, "red");
                    out.setChannelName(1, "green");
                    out.setChannelName(2, "blue");
                    out.setChannelName(3, "alpha");

                    // and set final name
                    switch (imageType)
                    {
                        default:
                        case BufferedImage.TYPE_INT_ARGB:
                            out.setName(sequence.getName() + " (ARGB rendering)");
                            break;

                        case BufferedImage.TYPE_INT_RGB:
                            out.setName(sequence.getName() + " (RGB rendering)");
                            break;

                        case BufferedImage.TYPE_BYTE_GRAY:
                            out.setName(sequence.getName() + " (gray rendering)");
                            break;
                    }

                    Icy.getMainInterface().addSequence(out);
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
            {
                if (channel == -1)
                {
                    for (int c = 0; c < sequence.getSizeC(); c++)
                        Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(sequence, c));
                }
                else
                    Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(sequence, channel));
            }
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence seq = Icy.getMainInterface().getFocusedSequence();

            return !processing && (seq != null) && (channel < seq.getSizeC());
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                SequenceUtil.removeChannel(sequence, channel);
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence seq = Icy.getMainInterface().getFocusedSequence();

            return !processing && (seq != null) && (channel < seq.getSizeC());
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
        public void doAction(ActionEvent e)
        {
            new SequenceDimensionMergeFrame(dim);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                Icy.getMainInterface().addSequence(SequenceUtil.getCopy(sequence));
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            new SequenceCropper();
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction canvasResizeAction = new IcyAbstractAction("Canvas size...", new IcyIcon(
            ResourceUtil.ICON_CROP), "Canvas resize", "Resize the canvas without changing image size.")
    {
        private static final long serialVersionUID = 9156831541828750627L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceCanvasResizeFrame(sequence);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction imageResizeAction = new IcyAbstractAction("Image size...", new IcyIcon(
            ResourceUtil.ICON_FIT_CANVAS), "Image resize", "Resize the image.")
    {
        private static final long serialVersionUID = -4731940627380446776L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceResizeFrame(sequence);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
            ResourceUtil.ICON_LAYER_REVERSE_V), "Reverse Z slices", "Reverse Z slices order", true,
            "Reversing slices...")
    {
        private static final long serialVersionUID = -4731940627380446776L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                SequenceUtil.reverseZ(sequence);
        }
    };

    public static IcyAbstractAction extractSliceAction = new IcyAbstractAction("Extract slice", new IcyIcon(
            ResourceUtil.ICON_LAYER_EXTRACT_V), "Extract current Z slice",
            "Create a new sequence by extracting current Z slice of active sequence.", true, "Extracting slice...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3731161374656240419L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int z = (viewer == null) ? -1 : viewer.getZ();

            if (z != -1)
                Icy.getMainInterface().addSequence(SequenceUtil.extractSlice(viewer.getSequence(), z));
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int z = (viewer == null) ? -1 : viewer.getZ();

            return !processing && (z != -1);
        }
    };

    public static IcyAbstractAction removeSliceAction = new IcyAbstractAction("Remove slice", new IcyIcon(
            ResourceUtil.ICON_LAYER_REMOVE_V), "Remove current Z slice",
            "Remove the current Z slice of active sequence.", true, "Removing slice...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6588564641490390145L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int z = (viewer == null) ? -1 : viewer.getZ();

            if (z != -1)
                SequenceUtil.removeZAndShift(viewer.getSequence(), z);
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int z = (viewer == null) ? -1 : viewer.getZ();

            return !processing && (z != -1);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceDimensionExtendFrame(Icy.getMainInterface().getFocusedSequence(), DimensionId.Z);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceDimensionAdjustFrame(sequence, DimensionId.Z);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    // T operations
    public static IcyAbstractAction reverseFramesAction = new IcyAbstractAction("Reverse order", new IcyIcon(
            ResourceUtil.ICON_LAYER_REVERSE_H), "Reverse T frames", "Reverse T frames order", true,
            "Reversing frames...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2403122454093281595L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                SequenceUtil.reverseT(sequence);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction extractFrameAction = new IcyAbstractAction("Extract frame", new IcyIcon(
            ResourceUtil.ICON_LAYER_EXTRACT_H), "Extract current T frame",
            "Create a new sequence by extracting current T frame of active sequence.", true, "Extracting frame...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -5809053788547447661L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int t = (viewer == null) ? -1 : viewer.getT();

            if (t != -1)
                Icy.getMainInterface().addSequence(SequenceUtil.extractFrame(viewer.getSequence(), t));
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int t = (viewer == null) ? -1 : viewer.getT();

            return !processing && (t != -1);
        }
    };

    public static IcyAbstractAction removeFrameAction = new IcyAbstractAction("Remove frame", new IcyIcon(
            ResourceUtil.ICON_LAYER_REMOVE_H), "Remove current T frame",
            "Remove the current T frame of active sequence.", true, "Removing frame...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6113522706924858672L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int t = (viewer == null) ? -1 : viewer.getT();

            if (t != -1)
                SequenceUtil.removeTAndShift(viewer.getSequence(), t);
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
            final int t = (viewer == null) ? -1 : viewer.getT();

            return !processing && (t != -1);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceDimensionExtendFrame(Icy.getMainInterface().getFocusedSequence(), DimensionId.T);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceDimensionAdjustFrame(sequence, DimensionId.T);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    // ZT conversion
    public static IcyAbstractAction convertToSlicesAction = new IcyAbstractAction("Convert to stack", new IcyIcon(
            ResourceUtil.ICON_LAYER_V1), "Convert to stack", "Put all images in Z dimension.", true,
            "Converting to stack...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5987495169612852524L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();
                final int t = viewer.getT();

                if (sequence != null)
                {
                    SequenceUtil.convertToVolume(sequence);
                    viewer.setZ(t);
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction convertToFramesAction = new IcyAbstractAction("Convert to frames", new IcyIcon(
            ResourceUtil.ICON_LAYER_H1), "Convert to frames", "Put all images in T dimension.", true,
            "Converting to frames...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6555855298812635009L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

            if (viewer != null)
            {
                final Sequence sequence = viewer.getSequence();
                final int z = viewer.getZ();

                if (sequence != null)
                {
                    SequenceUtil.convertToTime(sequence);
                    viewer.setT(z);
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                new SequenceDimensionConvertFrame(sequence);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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

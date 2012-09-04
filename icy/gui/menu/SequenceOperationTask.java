/**
 * 
 */
package icy.gui.menu;

import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandMenuButton;
import icy.gui.component.button.IcyCommandToggleMenuButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.menu.tools.SequenceCropper;
import icy.gui.sequence.tools.SequenceCanvasResizeFrame;
import icy.gui.sequence.tools.SequenceDimensionAdjustFrame;
import icy.gui.sequence.tools.SequenceDimensionConvertFrame;
import icy.gui.sequence.tools.SequenceDimensionExtendFrame;
import icy.gui.sequence.tools.SequenceDimensionMergeFrame;
import icy.gui.sequence.tools.SequenceResizeFrame;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.gui.util.RibbonUtil;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.util.OMEUtil;
import icy.util.StringUtil;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 * @author Stephane
 */
public class SequenceOperationTask extends RibbonTask
{
    public static class CopyConvertBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Copy / Convert";

        final IcyCommandButton cloneButton;
        final IcyCommandButton convertButton;
        // final IcyCommandButton advancedButton;

        final CommandToggleButtonGroup groupScaled;
        final IcyCommandToggleMenuButton scaledButton;

        public CopyConvertBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

            setToolTipText("Copy and data type conversion operation");

            // clone sequence
            cloneButton = new IcyCommandButton("Duplicate", new IcyIcon(ResourceUtil.ICON_DUPLICATE));
            cloneButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            cloneButton.setActionRichTooltip(new RichTooltip("Duplicate sequence",
                    "Create a fresh copy of the sequence."));
            cloneButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                            if (sequence != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Duplicating sequence...");
                                try
                                {
                                    Icy.getMainInterface().addSequence(SequenceUtil.getCopy(sequence));
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(cloneButton, RibbonElementPriority.MEDIUM);

            scaledButton = new IcyCommandToggleMenuButton("Scaled conversion");
            scaledButton.setActionRichTooltip(new RichTooltip("Scaled conversion",
                    "Scale values to the output data type."));

            groupScaled = new CommandToggleButtonGroup();
            groupScaled.add(scaledButton);
            groupScaled.setSelected(scaledButton, true);

            // data type conversion
            convertButton = new IcyCommandButton("Convert to", new IcyIcon(ResourceUtil.ICON_BAND_RIGHT));
            convertButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            convertButton.setPopupRichTooltip(new RichTooltip("Data type conversion",
                    "Convert the sequence to the selected data type."));
            convertButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();

                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
                    final boolean scaled = (groupScaled.getSelected() != null);

                    if (sequence != null)
                    {
                        final CommandToggleButtonGroup group = new CommandToggleButtonGroup();

                        result.addMenuButton(getConvertButton(sequence, DataType.UBYTE, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.BYTE, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.USHORT, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.SHORT, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.UINT, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.INT, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.FLOAT, scaled, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.DOUBLE, scaled, group));
                        result.addMenuSeparator();
                    }

                    result.addMenuButton(scaledButton);

                    return result;

                }
            });
            addCommandButton(convertButton, RibbonElementPriority.MEDIUM);

            // advanced conversion
            // advancedButton = new IcyCommandButton("Advanced...", new
            // IcyIcon(ResourceUtil.ICON_COG));
            // advancedButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            // advancedButton.setActionRichTooltip(new RichTooltip("Advanced conversion",
            // "Create a fresh copy of the sequence."));
            // advancedButton.addActionListener(new ActionListener()
            // {
            // @Override
            // public void actionPerformed(ActionEvent e)
            // {
            // // launch in background as it can take sometime
            // ThreadUtil.bgRun(new Runnable()
            // {
            // @Override
            // public void run()
            // {
            // final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            //
            // if (sequence != null)
            // {
            // final ProgressFrame pf = new ProgressFrame("Duplicating sequence...");
            // try
            // {
            // // not yet implemented
            // }
            // finally
            // {
            // pf.close();
            // }
            // }
            // }
            // });
            // }
            // });
            // addCommandButton(advancedButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        public IcyCommandToggleMenuButton getConvertButton(final Sequence sequence, final DataType dataType,
                final boolean scaled, CommandToggleButtonGroup group)
        {
            final String dataTypeStr = dataType.toString(true);
            final IcyCommandToggleMenuButton result = new IcyCommandToggleMenuButton(dataTypeStr);

            result.setActionRichTooltip(new RichTooltip("Convert to " + dataTypeStr, "Convert sequence data type to "
                    + dataTypeStr + "."));

            group.add(result);

            // sequence has same datatype ?
            if (sequence.getDataType_() == dataType)
            {
                // select and disable it
                group.setSelected(result, true);
                result.setEnabled(false);
            }
            else
            {
                group.setSelected(result, false);

                // action for type conversion
                result.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // background processing as it can take up sometime
                        ThreadUtil.bgRun(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final ProgressFrame pf = new ProgressFrame("Converting...");

                                try
                                {
                                    Icy.getMainInterface().addSequence(
                                            SequenceUtil.convertToType(sequence, dataType, scaled));
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        });
                    }
                });
            }

            return result;
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            cloneButton.setEnabled(enabled);
            convertButton.setEnabled(enabled);
            // advancedButton.setEnabled(enabled);
        }
    }

    public static class RenderingBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Rendering";

        final IcyCommandButton argbButton;
        final IcyCommandButton rgbButton;
        final IcyCommandButton grayButton;

        public RenderingBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

            setToolTipText("Color and gray rendering");

            // ARGB rendering
            argbButton = new IcyCommandButton("ARGB image", new IcyIcon(ResourceUtil.ICON_ARGB_COLOR, false));
            argbButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);

            final RichTooltip argbToolTip = new RichTooltip("Build ARGB image",
                    "Create an ARGB color (support transparency) rendered version of the current sequence.");
            argbToolTip
                    .addDescriptionSection("Resulting sequence is 4 channels with unsigned byte (8 bits) data type.");

            argbButton.setActionRichTooltip(argbToolTip);
            argbButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generateSequence(BufferedImage.TYPE_INT_ARGB);
                        }
                    });
                }
            });
            addCommandButton(argbButton, RibbonElementPriority.MEDIUM);

            // RGB rendering
            rgbButton = new IcyCommandButton("RGB image", new IcyIcon(ResourceUtil.ICON_RGB_COLOR, false));
            rgbButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);

            final RichTooltip rgbToolTip = new RichTooltip("Build RGB image",
                    "Create a RGB color rendered version of the current sequence.");
            rgbToolTip.addDescriptionSection("Resulting sequence is 3 channels with unsigned byte (8 bits) data type.");

            rgbButton.setActionRichTooltip(rgbToolTip);
            rgbButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generateSequence(BufferedImage.TYPE_INT_RGB);
                        }
                    });
                }
            });
            addCommandButton(rgbButton, RibbonElementPriority.MEDIUM);

            // Gray rendering
            grayButton = new IcyCommandButton("Gray image", new IcyIcon(ResourceUtil.ICON_GRAY_COLOR, false));
            grayButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);

            final RichTooltip grayToolTip = new RichTooltip("Build gray image",
                    "Create a gray rendered version of the current sequence.");
            grayToolTip
                    .addDescriptionSection("Resulting sequence is single channel with unsigned byte (8 bits) data type.");

            grayButton.setActionRichTooltip(grayToolTip);
            grayButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generateSequence(BufferedImage.TYPE_BYTE_GRAY);
                        }
                    });
                }
            });
            addCommandButton(grayButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void generateSequence(int imageType)
        {
            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

            if (viewer != null)
            {
                final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                if (sequence != null)
                {
                    final ProgressFrame pf = new ProgressFrame("Converting to ARGB image...");
                    try
                    {
                        final LUT lut = viewer.getLut();
                        final Sequence out = new Sequence(OMEUtil.createOMEMetadata(sequence.getMetadata()));

                        // image receiver
                        final BufferedImage imgOut = new BufferedImage(sequence.getSizeX(), sequence.getSizeY(),
                                imageType);

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
                    finally
                    {
                        pf.close();
                    }
                }
            }
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            argbButton.setEnabled(enabled);
            rgbButton.setEnabled(enabled);
            grayButton.setEnabled(enabled);
        }
    }

    public static class PlanarOperationBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7475753600896040618L;

        private static final String NAME = "Plan (XY)";

        final IcyCommandButton cropButton;
        final IcyCommandButton canvasResizeButton;
        final IcyCommandButton imageResizeButton;
        final IcyCommandButton mergeButton;

        public PlanarOperationBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_PICTURE));

            setToolTipText("XY (plan) operation");

            // fast crop operation
            cropButton = new IcyCommandButton("Fast crop", new IcyIcon(ResourceUtil.ICON_CUT));
            cropButton.setActionRichTooltip(new RichTooltip("Fast crop image", "Crop an image from a ROI."));
            cropButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceCropper();
                }
            });
            addCommandButton(cropButton, RibbonElementPriority.MEDIUM);

            // canvas resize operation
            canvasResizeButton = new IcyCommandButton("Canvas size...", new IcyIcon(ResourceUtil.ICON_CROP));
            canvasResizeButton.setActionRichTooltip(new RichTooltip("Canvas resize",
                    "Resize the canvas without changing image size."));
            canvasResizeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceCanvasResizeFrame(Icy.getMainInterface().getFocusedSequence());
                }
            });
            addCommandButton(canvasResizeButton, RibbonElementPriority.MEDIUM);

            // image resize operation
            imageResizeButton = new IcyCommandButton("Image size...", new IcyIcon(ResourceUtil.ICON_FIT_CANVAS));
            imageResizeButton.setActionRichTooltip(new RichTooltip("Image resize", "Resize the image."));
            imageResizeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceResizeFrame(Icy.getMainInterface().getFocusedSequence());
                }
            });
            addCommandButton(imageResizeButton, RibbonElementPriority.MEDIUM);

            // merge operation
            mergeButton = new IcyCommandButton("Merge...");
            mergeButton.setActionRichTooltip(new RichTooltip("Resize image",
                    "Resize an image using different policies."));
            mergeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                }
            });
            // not yet implemented
            // addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            cropButton.setEnabled(enabled);
            canvasResizeButton.setEnabled(enabled);
            imageResizeButton.setEnabled(enabled);
            mergeButton.setEnabled(enabled);
        }
    }

    public static class ChannelOperationBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Channel (C)";

        final IcyCommandButton extractButton;
        final IcyCommandButton removeButton;
        final IcyCommandButton mergeButton;

        public ChannelOperationBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

            setToolTipText("Channel operation");

            // single channel extraction
            extractButton = new IcyCommandButton("Extract", new IcyIcon(ResourceUtil.ICON_INDENT_DECREASE));
            extractButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            extractButton.setPopupRichTooltip(new RichTooltip("Single channel extraction",
                    "Create a new single channel sequence from selected channel of active sequence."));
            extractButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        for (int c = 0; c < sequence.getSizeC(); c++)
                        {
                            final int ch = c;
                            final IcyCommandMenuButton button = new IcyCommandMenuButton("channel " + c);

                            button.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    extractChannel(sequence, ch);
                                }
                            });

                            result.addMenuButton(button);
                        }

                        final IcyCommandMenuButton button = new IcyCommandMenuButton("all channels");

                        button.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                extractChannel(sequence, -1);
                            }
                        });

                        result.addMenuButton(button);
                    }

                    return result;
                }
            });
            addCommandButton(extractButton, RibbonElementPriority.MEDIUM);

            // single channel remove
            removeButton = new IcyCommandButton("Remove", new IcyIcon(ResourceUtil.ICON_INDENT_REMOVE));
            removeButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            removeButton.setPopupRichTooltip(new RichTooltip("Remove channel",
                    "Remove the selected channel from active sequence."));
            removeButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        for (int c = 0; c < sequence.getSizeC(); c++)
                        {
                            final int ch = c;
                            final IcyCommandMenuButton button = new IcyCommandMenuButton("channel " + c);

                            button.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    removeChannel(sequence, ch);
                                }
                            });

                            result.addMenuButton(button);
                        }
                    }

                    return result;
                }
            });
            addCommandButton(removeButton, RibbonElementPriority.MEDIUM);

            // advances band extraction
            mergeButton = new IcyCommandButton("Merge...", new IcyIcon(ResourceUtil.ICON_INDENT_INCREASE));
            mergeButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            mergeButton.setActionRichTooltip(new RichTooltip("Merge channels",
                    "Merge channels from severals input sequences to build a new sequence."));
            mergeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // new SequenceChannelMergeFrame();
                    new SequenceDimensionMergeFrame(DimensionId.C);
                }
            });
            addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void extractChannel(final Sequence seqIn, final int ch)
        {
            // nothing to do
            if (seqIn == null)
                return;

            // launch in background as it can take sometime
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    final ProgressFrame pf = new ProgressFrame("Extracting channel(s)...");
                    try
                    {
                        // process all channels ?
                        if (ch == -1)
                        {
                            for (int c = 0; c < seqIn.getSizeC(); c++)
                                Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(seqIn, c));
                        }
                        else
                            Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(seqIn, ch));
                    }
                    finally
                    {
                        pf.close();
                    }
                }
            });
        }

        void removeChannel(final Sequence seqIn, final int ch)
        {
            // nothing to do
            if (seqIn == null)
                return;

            // launch in background as it can take sometime
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    final ProgressFrame pf = new ProgressFrame("Removing channel...");
                    try
                    {
                        // process all channels ?
                        SequenceUtil.removeChannel(seqIn, ch);
                        udpateButtonsState();
                    }
                    finally
                    {
                        pf.close();
                    }
                }
            });
        }

        void udpateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getFocusedSequence();
            final boolean enabled = (seq != null);
            final boolean several = enabled && (seq.getSizeC() > 1);

            extractButton.setEnabled(several);
            removeButton.setEnabled(several);
            mergeButton.setEnabled(enabled);
        }
    }

    public static class ZOperationBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8301134961618666184L;

        public static final String NAME = "Stack (Z)";

        final IcyCommandButton reverseButton;
        final IcyCommandButton extractButton;
        final IcyCommandButton removeButton;
        final IcyCommandButton advancedRemoveButton;
        final IcyCommandButton addButton;
        final IcyCommandButton mergeButton;

        public ZOperationBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V1));

            setToolTipText("Z (stack) operation");

            // reverse stack
            reverseButton = new IcyCommandButton("Reverse order", new IcyIcon(ResourceUtil.ICON_LAYER_REVERSE_V));
            reverseButton.setActionRichTooltip(new RichTooltip("Reverse order", "Reverse Z slices order"));
            reverseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                            if (sequence != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Extracting slice...");
                                try
                                {
                                    SequenceUtil.reverseZ(sequence);
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(reverseButton, RibbonElementPriority.MEDIUM);

            // slice extraction
            extractButton = new IcyCommandButton("Extract current", new IcyIcon(ResourceUtil.ICON_LAYER_EXTRACT_V));
            extractButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            extractButton.setActionRichTooltip(new RichTooltip("Extract current Z slice",
                    "Create a new sequence by extracting current Z slice of active sequence."));
            extractButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                            final int z = (viewer == null) ? -1 : viewer.getZ();

                            if (z != -1)
                            {
                                final ProgressFrame pf = new ProgressFrame("Extracting slice...");
                                try
                                {
                                    Icy.getMainInterface().addSequence(
                                            SequenceUtil.extractSlice(viewer.getSequence(), z));
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(extractButton, RibbonElementPriority.MEDIUM);

            // slice remove
            removeButton = new IcyCommandButton("Remove current", new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_V));
            removeButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            removeButton.setActionRichTooltip(new RichTooltip("Remove current Z slice",
                    "Remove the current Z slice of active sequence."));
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

                            if (viewer != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Removing slice...");
                                try
                                {
                                    SequenceUtil.removeZ(viewer.getSequence(), viewer.getZ());
                                    ZOperationBand.this.udpateButtonsState();
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(removeButton, RibbonElementPriority.MEDIUM);

            // ADVANCED
            startGroup();

            // add slices
            addButton = new IcyCommandButton("Add...", new IcyIcon(ResourceUtil.ICON_LAYER_ADD_V));
            addButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            addButton.setActionRichTooltip(new RichTooltip("Add slice(s)",
                    "Extends Z dimension by adding empty or duplicating slices."));
            addButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceDimensionExtendFrame(Icy.getMainInterface().getFocusedSequence(), DimensionId.Z);
                }
            });
            addCommandButton(addButton, RibbonElementPriority.MEDIUM); // advanced Z slice remove

            // Z stack merge
            mergeButton = new IcyCommandButton("Merge...", new IcyIcon(ResourceUtil.ICON_LAYER_INSERT_V));
            mergeButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            mergeButton.setActionRichTooltip(new RichTooltip("Merge Z stacks",
                    "Merge Z stacks from severals input sequences to build a new sequence."));
            mergeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceDimensionMergeFrame(DimensionId.Z);
                }
            });
            addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            advancedRemoveButton = new IcyCommandButton("Remove...", new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_ADV_V));
            advancedRemoveButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            advancedRemoveButton.setActionRichTooltip(new RichTooltip("Advanced Z slice remove",
                    "Advanced Z slice remove operation."));
            advancedRemoveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        new SequenceDimensionAdjustFrame(sequence, DimensionId.Z);
                        ZOperationBand.this.udpateButtonsState();
                    }
                }
            });
            addCommandButton(advancedRemoveButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getFocusedSequence();
            final boolean enabled = (seq != null);
            final boolean several = enabled && (seq.getSizeZ() > 1);

            reverseButton.setEnabled(several);
            extractButton.setEnabled(several);
            removeButton.setEnabled(several);
            addButton.setEnabled(enabled);
            mergeButton.setEnabled(enabled);
            advancedRemoveButton.setEnabled(several);
        }
    }

    public static class TOperationBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3728386745443331069L;

        public static final String NAME = "Frame (T)";

        final IcyCommandButton reverseButton;
        final IcyCommandButton extractButton;
        final IcyCommandButton removeButton;
        final IcyCommandButton advancedRemoveButton;
        final IcyCommandButton addButton;
        final IcyCommandButton mergeButton;

        public TOperationBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_LAYER_H1));

            setToolTipText("T (frame) operation");

            // reverse frame
            reverseButton = new IcyCommandButton("Reverse order", new IcyIcon(ResourceUtil.ICON_LAYER_REVERSE_H));
            reverseButton.setActionRichTooltip(new RichTooltip("Reverse order", "Reverse T frames order"));
            reverseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                            if (sequence != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Extracting slice...");
                                try
                                {
                                    SequenceUtil.reverseT(sequence);
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(reverseButton, RibbonElementPriority.MEDIUM);

            // frame extraction
            extractButton = new IcyCommandButton("Extract current", new IcyIcon(ResourceUtil.ICON_LAYER_EXTRACT_H));
            extractButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            extractButton.setActionRichTooltip(new RichTooltip("Extract current T frame",
                    "Create a new sequence by extracting current T frame of active sequence."));
            extractButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

                            if (viewer != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Extracting frame...");
                                try
                                {
                                    Icy.getMainInterface().addSequence(
                                            SequenceUtil.extractFrame(viewer.getSequence(), viewer.getT()));
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(extractButton, RibbonElementPriority.MEDIUM);

            // frame remove
            removeButton = new IcyCommandButton("Remove current", new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_H));
            removeButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            removeButton.setActionRichTooltip(new RichTooltip("Remove current T frame",
                    "Remove the current T frame of active sequence."));
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // launch in background as it can take sometime
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

                            if (viewer != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Removing frame...");
                                try
                                {
                                    SequenceUtil.removeT(viewer.getSequence(), viewer.getT());
                                    TOperationBand.this.udpateButtonsState();
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        }
                    });
                }
            });
            addCommandButton(removeButton, RibbonElementPriority.MEDIUM);

            // ADVANCED
            startGroup();

            // add frames
            addButton = new IcyCommandButton("Add...", new IcyIcon(ResourceUtil.ICON_LAYER_ADD_H));
            addButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            addButton.setActionRichTooltip(new RichTooltip("Add frame(s)",
                    "Extends T dimension by adding empty or duplicating frames."));
            addButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceDimensionExtendFrame(Icy.getMainInterface().getFocusedSequence(), DimensionId.T);
                }
            });
            addCommandButton(addButton, RibbonElementPriority.MEDIUM);

            // T frames merge
            mergeButton = new IcyCommandButton("Merge...", new IcyIcon(ResourceUtil.ICON_LAYER_INSERT_H));
            mergeButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            mergeButton.setActionRichTooltip(new RichTooltip("Merge T frames",
                    "Merge T frames from severals input sequences to build a new sequence."));
            mergeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceDimensionMergeFrame(DimensionId.T);
                }
            });
            addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            // advanced T frame remove
            advancedRemoveButton = new IcyCommandButton("Remove...", new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_ADV_H));
            advancedRemoveButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            advancedRemoveButton.setActionRichTooltip(new RichTooltip("Advanced T frame remove",
                    "Advanced T frame remove operation."));
            advancedRemoveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        new SequenceDimensionAdjustFrame(sequence, DimensionId.T);
                        TOperationBand.this.udpateButtonsState();
                    }
                }
            });
            addCommandButton(advancedRemoveButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getFocusedSequence();
            final boolean enabled = (seq != null);
            final boolean several = enabled && (seq.getSizeT() > 1);

            reverseButton.setEnabled(several);
            extractButton.setEnabled(several);
            removeButton.setEnabled(several);
            addButton.setEnabled(enabled);
            mergeButton.setEnabled(enabled);
            advancedRemoveButton.setEnabled(several);
        }
    }

    public static class DimConversionBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8210688977085548878L;

        private static final String NAME = "Dimension operation";

        final IcyCommandButton convertToZButton;
        final IcyCommandButton convertToTButton;
        final IcyCommandButton advancedConvertButton;

        public DimConversionBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V2));

            setToolTipText("Dimension conversion");

            // convert to Z stack
            convertToZButton = new IcyCommandButton("Convert to stack", new IcyIcon(ResourceUtil.ICON_LAYER_V1));
            convertToZButton
                    .setActionRichTooltip(new RichTooltip("Convert to stack", "Set all images in Z dimension."));
            convertToZButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SequenceUtil.convertToVolume(Icy.getMainInterface().getFocusedSequence());
                        }
                    });
                }
            });
            addCommandButton(convertToZButton, RibbonElementPriority.MEDIUM);

            // convert to T stack
            convertToTButton = new IcyCommandButton("Convert to frames", new IcyIcon(ResourceUtil.ICON_LAYER_H1));
            convertToTButton
                    .setActionRichTooltip(new RichTooltip("Convert to frames", "Set all images in T dimension."));
            convertToTButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SequenceUtil.convertToTime(Icy.getMainInterface().getFocusedSequence());
                        }
                    });
                }
            });
            addCommandButton(convertToTButton, RibbonElementPriority.MEDIUM);

            // advanced conversion
            advancedConvertButton = new IcyCommandButton("Advanced...", new IcyIcon(ResourceUtil.ICON_COG));
            advancedConvertButton.setActionRichTooltip(new RichTooltip("Advanced dimension conversion",
                    "Advanced dimension conversion operation."));
            advancedConvertButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceDimensionConvertFrame();
                }
            });
            addCommandButton(advancedConvertButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            convertToTButton.setEnabled(enabled);
            convertToZButton.setEnabled(enabled);
            advancedConvertButton.setEnabled(enabled);
        }
    }

    public static class ModifyRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Fill operation";

        final JTextField fillValueField;
        final IcyButton fillImage;
        final IcyButton fillSequence;

        public ModifyRibbonBand()
        {
            super(NAME, new IcyIcon("document"));

            setToolTipText("Fill operation");

            fillValueField = new JTextField();
            ComponentUtil.setFixedWidth(fillValueField, 90);
            fillValueField.setToolTipText("Value used for filling");
            fillValueField.setText("0");

            fillImage = new IcyButton("Image", new IcyIcon("brush", 16));
            fillImage.setFlat(true);
            fillImage.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            ComponentUtil.setFixedWidth(fillImage, 90);
            fillImage.setHorizontalAlignment(SwingConstants.LEADING);
            fillImage.setToolTipText("Fill the content of selected ROI with specified value on current image.");
            fillImage.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
                    final IcyBufferedImage image = Icy.getMainInterface().getFocusedImage();

                    if ((sequence != null) && (image != null))
                        modifyImage(image, getBooleanMaskOfSelectedRoi(sequence), getFillValue(sequence));
                }
            });

            fillSequence = new IcyButton("Sequence", new IcyIcon("brush", 16));
            fillSequence.setFlat(true);
            fillSequence.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            ComponentUtil.setFixedWidth(fillSequence, 90);
            fillSequence.setHorizontalAlignment(SwingConstants.LEADING);
            fillSequence.setToolTipText("Fill the content of selected ROI with specified value on whole sequence.");
            fillSequence.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        final double value = getFillValue(sequence);
                        final BooleanMask2D mask = getBooleanMaskOfSelectedRoi(sequence);

                        sequence.beginUpdate();
                        try
                        {
                            for (IcyBufferedImage image : sequence.getAllImage())
                                if (image != null)
                                    modifyImage(image, mask, value);
                        }
                        finally
                        {
                            sequence.endUpdate();
                        }
                    }
                }
            });

            addRibbonComponent(new JRibbonComponent(fillImage));
            addRibbonComponent(new JRibbonComponent(fillSequence));
            addRibbonComponent(new JRibbonComponent(GuiUtil.createLineBoxPanel(fillValueField)));

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        double getFillValue(Sequence sequence)
        {
            double value = StringUtil.parseDouble(fillValueField.getText(), 0);

            if ((sequence != null) && (!sequence.isFloatDataType()))
            {
                final double bounds[] = sequence.getDataType_().getDefaultBounds();

                // limit value to data type bounds
                if (value < bounds[0])
                    value = bounds[0];
                if (value > bounds[1])
                    value = bounds[1];
            }

            // set value back if incorrect
            fillValueField.setText(Double.toString(value));

            return value;
        }

        BooleanMask2D getBooleanMaskOfSelectedRoi(Sequence sequence)
        {
            BooleanMask2D result = null;
            final Rectangle seqBounds = sequence.getBounds();

            // compute global boolean mask of all ROI2D selected in the sequence
            for (ROI2D roi : sequence.getROI2Ds())
            {
                if (roi.isSelected())
                {
                    // get intersection between image and roi bounds
                    final Rectangle intersect = roi.getBounds().intersection(seqBounds);
                    // get the boolean mask of roi (optimized from intersection bounds)
                    final boolean[] mask = roi.getAsBooleanMask(intersect);

                    // update global mask
                    if (result == null)
                        result = new BooleanMask2D(intersect, mask);
                    else
                        result.union(intersect, mask);
                }
            }

            return result;
        }

        void modifyImage(IcyBufferedImage image, BooleanMask2D booleanMask, double value)
        {
            final Rectangle imageBounds = image.getBounds();

            // process only if global mask is not empty
            if ((booleanMask != null) && (!booleanMask.bounds.isEmpty()))
            {
                final Rectangle bounds = booleanMask.bounds;
                final boolean[] mask = booleanMask.mask;

                for (int c = 0; c < image.getSizeC(); c++)
                {
                    final Object imgData = image.getDataXY(c);
                    // calculate offset
                    int offMsk = 0;
                    int offImg = (bounds.y * imageBounds.width) + bounds.x;

                    for (int y = 0; y < bounds.height; y++)
                    {
                        for (int x = 0; x < bounds.width; x++)
                            if (mask[offMsk + x])
                                Array1DUtil.setValue(imgData, offImg + x, value);

                        offMsk += bounds.width;
                        offImg += imageBounds.width;
                    }
                }

                image.dataChanged();
            }
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            fillValueField.setEnabled(enabled);
            fillImage.setEnabled(enabled);
            fillSequence.setEnabled(enabled);
        }
    }

    public static final String NAME = "Sequence operation";

    private final CopyConvertBand copyConvertBand;
    private final RenderingBand colorConvertBand;
    private final DimConversionBand stackConversionBand;
    private final PlanarOperationBand planarOperationBand;
    private final ChannelOperationBand channelOperationBand;
    private final ZOperationBand zStackOperationBand;
    private final TOperationBand tStackOperationBand;
    private final ModifyRibbonBand modifyBand;

    public SequenceOperationTask()
    {
        super(NAME, new CopyConvertBand(), new PlanarOperationBand(), new ChannelOperationBand(), new ZOperationBand(),
                new TOperationBand(), new DimConversionBand(), new RenderingBand(), new ModifyRibbonBand());

        copyConvertBand = (CopyConvertBand) getBand(0);
        planarOperationBand = (PlanarOperationBand) getBand(1);
        channelOperationBand = (ChannelOperationBand) getBand(2);
        zStackOperationBand = (ZOperationBand) getBand(3);
        tStackOperationBand = (TOperationBand) getBand(4);
        stackConversionBand = (DimConversionBand) getBand(5);
        colorConvertBand = (RenderingBand) getBand(6);
        modifyBand = (ModifyRibbonBand) getBand(7);
    }

    /**
     * call this method on sequence focus change
     */
    public void onSequenceFocusChange()
    {
        copyConvertBand.udpateButtonsState();
        colorConvertBand.udpateButtonsState();
        channelOperationBand.udpateButtonsState();
        planarOperationBand.udpateButtonsState();
        stackConversionBand.udpateButtonsState();
        zStackOperationBand.udpateButtonsState();
        tStackOperationBand.udpateButtonsState();
        modifyBand.udpateButtonsState();
    }
}

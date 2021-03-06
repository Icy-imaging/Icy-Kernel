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
package icy.gui.menu;

import icy.action.FileActions;
import icy.action.SequenceOperationActions;
import icy.action.SequenceOperationActions.ExtractChannelAction;
import icy.action.SequenceOperationActions.RemoveChannelAction;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandMenuButton;
import icy.gui.component.button.IcyCommandToggleMenuButton;
import icy.gui.util.RibbonUtil;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.util.StringUtil;

import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 * @author Stephane
 */
public class SequenceOperationTask extends RibbonTask
{
    public static class FileBand extends JRibbonBand
    {
        /**
        *
        */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String BAND_NAME = "File";

        final IcyCommandButton openButton;
        final IcyCommandButton openRegionButton;
        final IcyCommandButton saveButton;

        public FileBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_DOC));

            openButton = new IcyCommandButton(FileActions.openSequenceAction);
            openRegionButton = new IcyCommandButton(FileActions.openSequenceRegionAction);
            // openAreaButton = new IcyCommandButton("Open region", new IcyIcon(ResourceUtil.ICON_CROP));
            // openAreaButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            // openAreaButton.setPopupRichTooltip(new RichTooltip("Open selected region",
            // "Open the selected ROI region from the original image at a specific resolution level"));
            // openAreaButton.setPopupCallback(new PopupPanelCallback()
            // {
            // @Override
            // public JPopupPanel getPopupPanel(JCommandButton commandButton)
            // {
            // final JCommandPopupMenu result = new JCommandPopupMenu();
            //
            // for (int r = 0; r < 5; r++)
            // result.addMenuButton(new IcyCommandMenuButton(new OpenSequenceRegionAction(r)));
            //
            // return result;
            // }
            // });
            saveButton = new IcyCommandButton(FileActions.saveAsSequenceAction);

            addCommandButton(openButton, RibbonElementPriority.MEDIUM);
            addCommandButton(openRegionButton, RibbonElementPriority.MEDIUM);
            addCommandButton(saveButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            openRegionButton.setEnabled(
                    (sequence != null) && (!StringUtil.isEmpty(sequence.getFilename())) && sequence.hasSelectedROI());
            saveButton.setEnabled(Icy.getMainInterface().getActiveSequence() != null);
        }
    }

    public static class CopyConvertBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String BAND_NAME = "Copy / Convert";

        final IcyCommandButton cloneButton;
        final IcyCommandButton convertButton;
        final IcyCommandButton convertButtonRaw;

        public CopyConvertBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

            setToolTipText("Copy and data type conversion operation");

            // clone sequence
            cloneButton = new IcyCommandButton(SequenceOperationActions.cloneSequenceAction);
            addCommandButton(cloneButton, RibbonElementPriority.MEDIUM);

            // data type conversion
            convertButton = new IcyCommandButton("Conversion", new IcyIcon(ResourceUtil.ICON_BAND_RIGHT));
            convertButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            convertButton.setPopupRichTooltip(new RichTooltip("Data type conversion",
                    "Convert the sequence to the selected data type (values are scaled to fit the new type)"));
            convertButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        final CommandToggleButtonGroup group = new CommandToggleButtonGroup();

                        result.addMenuButton(getConvertButton(sequence, DataType.UBYTE, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.BYTE, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.USHORT, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.SHORT, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.UINT, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.INT, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.FLOAT, true, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.DOUBLE, true, group));
                    }

                    return result;
                }
            });
            addCommandButton(convertButton, RibbonElementPriority.MEDIUM);

            // data type conversion
            convertButtonRaw = new IcyCommandButton("Raw conversion", new IcyIcon(ResourceUtil.ICON_BAND_RIGHT));
            convertButtonRaw.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            convertButtonRaw.setPopupRichTooltip(new RichTooltip("Raw data type conversion",
                    "Convert the sequence to the selected data type (values remain unchanged or are clamped in case of overflow)"));
            convertButtonRaw.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        final CommandToggleButtonGroup group = new CommandToggleButtonGroup();

                        result.addMenuButton(getConvertButton(sequence, DataType.UBYTE, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.BYTE, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.USHORT, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.SHORT, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.UINT, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.INT, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.FLOAT, false, group));
                        result.addMenuButton(getConvertButton(sequence, DataType.DOUBLE, false, group));
                    }

                    return result;
                }
            });
            addCommandButton(convertButtonRaw, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        public static IcyCommandToggleMenuButton getConvertButton(final Sequence sequence, final DataType dataType,
                final boolean scaled, CommandToggleButtonGroup group)
        {
            final IcyCommandToggleMenuButton result = new IcyCommandToggleMenuButton(
                    SequenceOperationActions.getConvertSequenceAction(dataType, scaled));

            group.add(result);

            // sequence has same datatype ?
            if (sequence.getDataType_() == dataType)
            {
                // select and disable it
                group.setSelected(result, true);
                result.setEnabled(false);
            }
            else
                group.setSelected(result, false);

            return result;
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null);
            final boolean notEmpty = (seq != null) && !seq.isEmpty();

            cloneButton.setEnabled(enabled);
            convertButton.setEnabled(notEmpty);
            convertButtonRaw.setEnabled(notEmpty);
        }
    }

    public static class RenderingBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String BAND_NAME = "Rendering";

        final IcyCommandButton argbButton;
        final IcyCommandButton rgbButton;
        final IcyCommandButton grayButton;

        public RenderingBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

            setToolTipText("Color and gray rendering");

            // ARGB rendering
            argbButton = new IcyCommandButton(SequenceOperationActions.argbSequenceAction);
            addCommandButton(argbButton, RibbonElementPriority.MEDIUM);

            // RGB rendering
            rgbButton = new IcyCommandButton(SequenceOperationActions.rgbSequenceAction);
            addCommandButton(rgbButton, RibbonElementPriority.MEDIUM);

            // Gray rendering
            grayButton = new IcyCommandButton(SequenceOperationActions.graySequenceAction);
            addCommandButton(grayButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null) && !seq.isEmpty();

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

        private static final String BAND_NAME = "Plane (XY)";

        final IcyCommandButton cropButton;
        final IcyCommandButton canvasResizeButton;
        final IcyCommandButton imageResizeButton;

        // final IcyCommandButton mergeButton;

        public PlanarOperationBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_PICTURE));

            setToolTipText("XY (plane) operation");

            // fast crop operation
            cropButton = new IcyCommandButton(SequenceOperationActions.cropSequenceAction);
            addCommandButton(cropButton, RibbonElementPriority.MEDIUM);

            // canvas resize operation
            canvasResizeButton = new IcyCommandButton(SequenceOperationActions.canvasResizeAction);
            addCommandButton(canvasResizeButton, RibbonElementPriority.MEDIUM);

            // image resize operation
            imageResizeButton = new IcyCommandButton(SequenceOperationActions.imageResizeAction);
            addCommandButton(imageResizeButton, RibbonElementPriority.MEDIUM);

            // merge operation
            // mergeButton = new IcyCommandButton("Merge...");
            // mergeButton.setActionRichTooltip(new RichTooltip("Resize image",
            // "Resize an image using different policies."));
            // mergeButton.addActionListener(new ActionListener()
            // {
            // @Override
            // public void actionPerformed(ActionEvent e)
            // {
            // }
            // });
            // not yet implemented
            // addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null) && !seq.isEmpty();

            cropButton.setEnabled(enabled);
            canvasResizeButton.setEnabled(enabled);
            imageResizeButton.setEnabled(enabled);
            // mergeButton.setEnabled(enabled);
        }
    }

    public static class ChannelOperationBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String BAND_NAME = "Channel (C)";

        final IcyCommandButton extractButton;
        final IcyCommandButton removeButton;
        final IcyCommandButton mergeButton;

        public ChannelOperationBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

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
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        for (int c = 0; c < sequence.getSizeC(); c++)
                        {
                            final IcyCommandMenuButton button;

                            if (c < SequenceOperationActions.extractChannelActions.length)
                                button = new IcyCommandMenuButton(SequenceOperationActions.extractChannelActions[c]);
                            else
                                button = new IcyCommandMenuButton(new ExtractChannelAction(c));

                            result.addMenuButton(button);
                        }

                        result.addMenuButton(
                                new IcyCommandMenuButton(SequenceOperationActions.extractAllChannelAction));
                    }

                    return result;
                }
            });
            addCommandButton(extractButton, RibbonElementPriority.MEDIUM);

            // single channel remove
            removeButton = new IcyCommandButton("Remove", new IcyIcon(ResourceUtil.ICON_INDENT_REMOVE));
            removeButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            removeButton.setPopupRichTooltip(
                    new RichTooltip("Remove channel", "Remove the selected channel from active sequence."));
            removeButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        for (int c = 0; c < sequence.getSizeC(); c++)
                        {
                            final IcyCommandMenuButton button;

                            if (c < SequenceOperationActions.removeChannelActions.length)
                                button = new IcyCommandMenuButton(SequenceOperationActions.removeChannelActions[c]);
                            else
                                button = new IcyCommandMenuButton(new RemoveChannelAction(c));

                            result.addMenuButton(button);
                        }
                    }

                    return result;
                }
            });
            addCommandButton(removeButton, RibbonElementPriority.MEDIUM);

            // channel merge operation
            mergeButton = new IcyCommandButton(SequenceOperationActions.mergeChannelsAction);
            addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null);
            final boolean several = (seq != null) && (seq.getSizeC() > 1);

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

        public static final String BAND_NAME = "Stack (Z)";

        final IcyCommandButton reverseButton;
        final IcyCommandButton extractButton;
        final IcyCommandButton removeButton;
        final IcyCommandButton advancedRemoveButton;
        final IcyCommandButton addButton;
        final IcyCommandButton mergeButton;

        public ZOperationBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V1));

            setToolTipText("Z (stack) operation");

            // reverse slices
            reverseButton = new IcyCommandButton(SequenceOperationActions.reverseSlicesAction);
            addCommandButton(reverseButton, RibbonElementPriority.MEDIUM);

            // slice extraction
            extractButton = new IcyCommandButton(SequenceOperationActions.extractSliceAction);
            addCommandButton(extractButton, RibbonElementPriority.MEDIUM);

            // slice remove
            removeButton = new IcyCommandButton(SequenceOperationActions.removeSliceAction);
            addCommandButton(removeButton, RibbonElementPriority.MEDIUM);

            // ADVANCED
            startGroup();

            // add slices
            addButton = new IcyCommandButton(SequenceOperationActions.addSlicesAction);
            addCommandButton(addButton, RibbonElementPriority.MEDIUM); // advanced Z slice remove

            // slices merge
            mergeButton = new IcyCommandButton(SequenceOperationActions.mergeSlicesAction);
            addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            // advanced slice remove
            advancedRemoveButton = new IcyCommandButton(SequenceOperationActions.removeSlicesAction);
            addCommandButton(advancedRemoveButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null);
            final boolean notEmpty = (seq != null) && !seq.isEmpty();
            final boolean several = (seq != null) && (seq.getSizeZ() > 1);

            reverseButton.setEnabled(several);
            extractButton.setEnabled(several);
            removeButton.setEnabled(several);
            addButton.setEnabled(notEmpty);
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

        public static final String BAND_NAME = "Frame (T)";

        final IcyCommandButton reverseButton;
        final IcyCommandButton extractButton;
        final IcyCommandButton removeButton;
        final IcyCommandButton advancedRemoveButton;
        final IcyCommandButton addButton;
        final IcyCommandButton mergeButton;

        public TOperationBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_LAYER_H1));

            setToolTipText("T (frame) operation");

            // reverse frames
            reverseButton = new IcyCommandButton(SequenceOperationActions.reverseFramesAction);
            addCommandButton(reverseButton, RibbonElementPriority.MEDIUM);

            // frame extraction
            extractButton = new IcyCommandButton(SequenceOperationActions.extractFrameAction);
            addCommandButton(extractButton, RibbonElementPriority.MEDIUM);

            // frame remove
            removeButton = new IcyCommandButton(SequenceOperationActions.removeFrameAction);
            addCommandButton(removeButton, RibbonElementPriority.MEDIUM);

            // ADVANCED
            startGroup();

            // add frames
            addButton = new IcyCommandButton(SequenceOperationActions.addFramesAction);
            addCommandButton(addButton, RibbonElementPriority.MEDIUM);

            // frames merge
            mergeButton = new IcyCommandButton(SequenceOperationActions.mergeFramesAction);
            addCommandButton(mergeButton, RibbonElementPriority.MEDIUM);

            // advanced frame remove
            advancedRemoveButton = new IcyCommandButton(SequenceOperationActions.removeFramesAction);
            addCommandButton(advancedRemoveButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null);
            final boolean notEmpty = (seq != null) && !seq.isEmpty();
            final boolean several = (seq != null) && (seq.getSizeT() > 1);

            reverseButton.setEnabled(several);
            extractButton.setEnabled(several);
            removeButton.setEnabled(several);
            addButton.setEnabled(notEmpty);
            mergeButton.setEnabled(enabled);
            advancedRemoveButton.setEnabled(several);
        }
    }

    public static class ZTConversionBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8210688977085548878L;

        private static final String BAND_NAME = "Z / T conversion";

        final IcyCommandButton convertToZButton;
        final IcyCommandButton convertToTButton;
        final IcyCommandButton advancedConvertButton;

        public ZTConversionBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V2));

            setToolTipText("Z/T conversion");

            // convert to Z stack
            convertToZButton = new IcyCommandButton(SequenceOperationActions.convertToSlicesAction);
            addCommandButton(convertToZButton, RibbonElementPriority.MEDIUM);

            // convert to T stack
            convertToTButton = new IcyCommandButton(SequenceOperationActions.convertToFramesAction);
            addCommandButton(convertToTButton, RibbonElementPriority.MEDIUM);

            // advanced conversion
            advancedConvertButton = new IcyCommandButton(SequenceOperationActions.advancedZTConvertAction);
            addCommandButton(advancedConvertButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean severalZ = (seq != null) && (seq.getSizeZ() > 1);
            final boolean severalT = (seq != null) && (seq.getSizeT() > 1);

            convertToTButton.setEnabled(severalZ);
            convertToZButton.setEnabled(severalT);
            advancedConvertButton.setEnabled(severalZ || severalT);
        }
    }

    public static final String NAME = "Image / Sequence";

    final FileBand fileBand;
    final CopyConvertBand copyConvertBand;
    final RenderingBand colorConvertBand;
    final ZTConversionBand stackConversionBand;
    final PlanarOperationBand planarOperationBand;
    final ChannelOperationBand channelOperationBand;
    final ZOperationBand zStackOperationBand;
    final TOperationBand tStackOperationBand;
    final Runnable buttonUpdater;

    public SequenceOperationTask()
    {
        super(NAME, new FileBand(), new CopyConvertBand(), new PlanarOperationBand(), new ChannelOperationBand(),
                new ZOperationBand(), new TOperationBand(), new ZTConversionBand(), new RenderingBand());

        fileBand = (FileBand) getBand(0);
        copyConvertBand = (CopyConvertBand) getBand(1);
        planarOperationBand = (PlanarOperationBand) getBand(2);
        channelOperationBand = (ChannelOperationBand) getBand(3);
        zStackOperationBand = (ZOperationBand) getBand(4);
        tStackOperationBand = (TOperationBand) getBand(5);
        stackConversionBand = (ZTConversionBand) getBand(6);
        colorConvertBand = (RenderingBand) getBand(7);

        fileBand.updateButtonsState();
        copyConvertBand.updateButtonsState();
        colorConvertBand.updateButtonsState();
        channelOperationBand.updateButtonsState();
        planarOperationBand.updateButtonsState();
        stackConversionBand.updateButtonsState();
        zStackOperationBand.updateButtonsState();
        tStackOperationBand.updateButtonsState();

        buttonUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                // sleep a bit
                ThreadUtil.sleep(1);

                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        fileBand.updateButtonsState();
                        copyConvertBand.updateButtonsState();
                        colorConvertBand.updateButtonsState();
                        channelOperationBand.updateButtonsState();
                        planarOperationBand.updateButtonsState();
                        stackConversionBand.updateButtonsState();
                        zStackOperationBand.updateButtonsState();
                        tStackOperationBand.updateButtonsState();
                    }
                });
            }
        };
    }

    /**
     * @deprecated Use Too
     */
    @Deprecated
    public double getFillValue()
    {
        return Icy.getMainInterface().getROIRibbonTask().getFillValue();
    }

    /**
     * call this method on sequence change
     */
    public void onSequenceChange()
    {
        ThreadUtil.runSingle(buttonUpdater);
    }
}

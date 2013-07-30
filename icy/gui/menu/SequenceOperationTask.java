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
package icy.gui.menu;

import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandMenuButton;
import icy.gui.component.button.IcyCommandToggleMenuButton;
import icy.gui.menu.action.SequenceOperationActions;
import icy.gui.menu.action.SequenceOperationActions.ExtractChannelAction;
import icy.gui.menu.action.SequenceOperationActions.RemoveChannelAction;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.gui.util.RibbonUtil;
import icy.image.IcyBufferedImage;
import icy.image.ImageDataIterator;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.system.thread.ThreadUtil;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        final IcyCommandButton convertButtonRaw;

        public CopyConvertBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

            setToolTipText("Copy and data type conversion operation");

            // clone sequence
            cloneButton = new IcyCommandButton(SequenceOperationActions.cloneSequenceAction);
            addCommandButton(cloneButton, RibbonElementPriority.MEDIUM);

            // data type conversion
            convertButton = new IcyCommandButton("Conversion", new IcyIcon(ResourceUtil.ICON_BAND_RIGHT));
            convertButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            convertButton.setPopupRichTooltip(new RichTooltip("Data type conversion",
                    "Convert the sequence to the selected data type (values are scaled to fit the new type)."));
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
            convertButtonRaw
                    .setPopupRichTooltip(new RichTooltip(
                            "Raw data type conversion",
                            "Convert the sequence to the selected data type (values remains unchanged so you have to take care about type change overflow)."));
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

        public IcyCommandToggleMenuButton getConvertButton(final Sequence sequence, final DataType dataType,
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
            final boolean enabled = seq != null;
            final boolean notEmpty = enabled && !seq.isEmpty();

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

        public static final String NAME = "Rendering";

        final IcyCommandButton argbButton;
        final IcyCommandButton rgbButton;
        final IcyCommandButton grayButton;

        public RenderingBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_TOOLS));

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

        private static final String NAME = "Plan (XY)";

        final IcyCommandButton cropButton;
        final IcyCommandButton canvasResizeButton;
        final IcyCommandButton imageResizeButton;

        // final IcyCommandButton mergeButton;

        public PlanarOperationBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_PICTURE));

            setToolTipText("XY (plan) operation");

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

                        result.addMenuButton(new IcyCommandMenuButton(SequenceOperationActions.extractAllChannelAction));
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
            final boolean notEmpty = enabled && !seq.isEmpty();
            final boolean several = enabled && (seq.getSizeZ() > 1);

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
            final boolean notEmpty = enabled && !seq.isEmpty();
            final boolean several = enabled && (seq.getSizeT() > 1);

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

        private static final String NAME = "Z / T conversion";

        final IcyCommandButton convertToZButton;
        final IcyCommandButton convertToTButton;
        final IcyCommandButton advancedConvertButton;

        public ZTConversionBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V2));

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
            final boolean enabled = (seq != null);
            final boolean severalZ = enabled && (seq.getSizeZ() > 1);
            final boolean severalT = enabled && (seq.getSizeT() > 1);

            convertToTButton.setEnabled(severalZ);
            convertToZButton.setEnabled(severalT);
            advancedConvertButton.setEnabled(severalZ || severalT);
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
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();
                    final IcyBufferedImage image = Icy.getMainInterface().getActiveImage();

                    if ((sequence != null) && (image != null))
                    {
                        ThreadUtil.bgRun(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final double value = getFillValue(sequence);

                                for (ROI roi : sequence.getSelectedROIs())
                                    DataIteratorUtil.set(new ImageDataIterator(image, roi), value);

                                image.dataChanged();
                            }
                        });
                    }
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
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        ThreadUtil.bgRun(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final double value = getFillValue(sequence);

                                for (ROI roi : sequence.getSelectedROIs())
                                    DataIteratorUtil.set(new SequenceDataIterator(sequence, roi), value);

                                sequence.dataChanged();
                            }
                        });
                    }
                }
            });

            addRibbonComponent(new JRibbonComponent(fillImage));
            addRibbonComponent(new JRibbonComponent(fillSequence));
            addRibbonComponent(new JRibbonComponent(GuiUtil.createLineBoxPanel(fillValueField)));

            RibbonUtil.setRestrictiveResizePolicies(this);
            updateButtonsState();
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

        void updateButtonsState()
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();
            final boolean enabled = (seq != null) && !seq.isEmpty();

            fillValueField.setEnabled(enabled);
            fillImage.setEnabled(enabled);
            fillSequence.setEnabled(enabled);
        }
    }

    public static final String NAME = "Sequence operation";

    private final CopyConvertBand copyConvertBand;
    private final RenderingBand colorConvertBand;
    private final ZTConversionBand stackConversionBand;
    private final PlanarOperationBand planarOperationBand;
    private final ChannelOperationBand channelOperationBand;
    private final ZOperationBand zStackOperationBand;
    private final TOperationBand tStackOperationBand;
    private final ModifyRibbonBand modifyBand;

    public SequenceOperationTask()
    {
        super(NAME, new CopyConvertBand(), new PlanarOperationBand(), new ChannelOperationBand(), new ZOperationBand(),
                new TOperationBand(), new ZTConversionBand(), new RenderingBand(), new ModifyRibbonBand());

        copyConvertBand = (CopyConvertBand) getBand(0);
        planarOperationBand = (PlanarOperationBand) getBand(1);
        channelOperationBand = (ChannelOperationBand) getBand(2);
        zStackOperationBand = (ZOperationBand) getBand(3);
        tStackOperationBand = (TOperationBand) getBand(4);
        stackConversionBand = (ZTConversionBand) getBand(5);
        colorConvertBand = (RenderingBand) getBand(6);
        modifyBand = (ModifyRibbonBand) getBand(7);
    }

    /**
     * call this method on sequence change
     */
    public void onSequenceChange()
    {
        copyConvertBand.updateButtonsState();
        colorConvertBand.updateButtonsState();
        channelOperationBand.updateButtonsState();
        planarOperationBand.updateButtonsState();
        stackConversionBand.updateButtonsState();
        zStackOperationBand.updateButtonsState();
        tStackOperationBand.updateButtonsState();
        modifyBand.updateButtonsState();
    }
}

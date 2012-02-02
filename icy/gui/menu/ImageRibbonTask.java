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
package icy.gui.menu;

import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandMenuButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.menu.tools.SequenceChannelMergeFrame;
import icy.gui.menu.tools.SequenceCropper;
import icy.gui.menu.tools.Time2Volume;
import icy.gui.util.GuiUtil;
import icy.gui.util.RibbonUtil;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.util.StringUtil;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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

public class ImageRibbonTask extends RibbonTask
{
    public static class ChannelOperationRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Channel operation";

        final IcyCommandButton extractSingleChannelButton;
        final IcyCommandButton splitChannelButton;
        final IcyCommandButton mergeChannelButton;

        public ChannelOperationRibbonBand()
        {
            super(NAME, new IcyIcon("wrench_plus"));

            // single channel extraction
            extractSingleChannelButton = new IcyCommandButton("Extract channel");
            extractSingleChannelButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            extractSingleChannelButton.setPopupRichTooltip(new RichTooltip("Single channel extraction",
                    "Create a new single channel sequence from the selected channel of current sequence"));
            extractSingleChannelButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    final JCommandPopupMenu result = new JCommandPopupMenu();
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        for (int i = 0; i < sequence.getSizeC(); i++)
                        {
                            final int bandNumber = i;
                            final IcyCommandMenuButton button = new IcyCommandMenuButton("Extract channel " + i);

                            button.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    extractBand(sequence, bandNumber);
                                }
                            });

                            result.addMenuButton(button);
                        }
                    }

                    return result;
                }
            });
            addCommandButton(extractSingleChannelButton, RibbonElementPriority.MEDIUM);

            // all channel extraction
            splitChannelButton = new IcyCommandButton("Split channels");
            splitChannelButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            splitChannelButton.setActionRichTooltip(new RichTooltip("Split channels",
                    "Create several single channel sequence from all channels of current sequence"));
            splitChannelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    extractBand(Icy.getMainInterface().getFocusedSequence(), -1);
                }
            });
            addCommandButton(splitChannelButton, RibbonElementPriority.MEDIUM);

            // advances band extraction
            mergeChannelButton = new IcyCommandButton("Merge channels...");
            mergeChannelButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
            mergeChannelButton.setActionRichTooltip(new RichTooltip("Merge channels",
                    "Merge channels from 2 input sequences to build a new sequence"));
            mergeChannelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceChannelMergeFrame();
                }
            });
            addCommandButton(mergeChannelButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void extractBand(final Sequence seqIn, final int chNum)
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
                        if (chNum == -1)
                        {
                            for (int ch = 0; ch < seqIn.getSizeC(); ch++)
                            {
                                final Sequence seqOut = seqIn.extractChannel(ch);

                                seqOut.setName(seqIn.getName() + " - (" + seqIn.getChannelName(ch) + ")");

                                Icy.addSequence(seqOut);
                            }
                        }
                        else
                        {
                            final Sequence seqOut = seqIn.extractChannel(chNum);

                            seqOut.setName(seqIn.getName() + " - (" + seqIn.getChannelName(chNum) + ")");

                            Icy.addSequence(seqOut);
                        }
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
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            extractSingleChannelButton.setEnabled(enabled);
            splitChannelButton.setEnabled(enabled);
            mergeChannelButton.setEnabled(enabled);
        }
    }

    public static class ConvertRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Copy / Convert";

        final IcyButton cloneButton;
        final IcyButton typeButton;
        final JCheckBox scaledCheckBox;

        public ConvertRibbonBand()
        {
            super(NAME, new IcyIcon("wrench_plus"));

            JRibbonComponent comp;

            // clone sequence
            cloneButton = new IcyButton("Duplicate");
            cloneButton.setFlat(true);
            cloneButton.setToolTipText("Create a fresh copy of the sequence");
            // cloneButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            // ComponentUtil.setFixedWidth(cloneButton, 110);
            cloneButton.setHorizontalAlignment(SwingConstants.LEADING);
            cloneButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                        Icy.addSequence(sequence.getCopy());
                }
            });
            comp = new JRibbonComponent(cloneButton);
            addRibbonComponent(comp);

            // data type conversion
            typeButton = new IcyButton("Convert to...");
            typeButton.setFlat(true);
            typeButton.setToolTipText("Convert the sequence to the selected data type");
            // typeButton.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            // ComponentUtil.setFixedWidth(typeButton, 110);
            typeButton.setHorizontalAlignment(SwingConstants.LEADING);
            typeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

                    if (sequence != null)
                    {
                        final JPopupMenu result = new JPopupMenu();

                        result.add(getConvertItem(sequence, DataType.UBYTE));
                        result.add(getConvertItem(sequence, DataType.BYTE));
                        result.add(getConvertItem(sequence, DataType.USHORT));
                        result.add(getConvertItem(sequence, DataType.SHORT));
                        result.add(getConvertItem(sequence, DataType.UINT));
                        result.add(getConvertItem(sequence, DataType.INT));
                        result.add(getConvertItem(sequence, DataType.FLOAT));
                        result.add(getConvertItem(sequence, DataType.DOUBLE));

                        result.show(typeButton, 0, typeButton.getHeight());
                    }
                }
            });
            comp = new JRibbonComponent(typeButton);
            addRibbonComponent(comp);

            scaledCheckBox = new JCheckBox("Scale data", true);
            scaledCheckBox.setToolTipText("Scale the data during conversion to best fit with result data type");
            comp = new JRibbonComponent(scaledCheckBox);
            addRibbonComponent(comp);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        public JMenuItem getConvertItem(final Sequence sequence, final DataType dataType)
        {
            // build type text
            // final String dataTypeString = ;
            final JMenuItem result = new JMenuItem(dataType.toString(true));
            // result.setText(dataTypeString);

            // sequence has same datatype ?
            if (sequence.getDataType_() == dataType)
            {
                // select and disable it
                result.setSelected(true);
                result.setEnabled(false);
            }
            else
            {
                result.setSelected(false);

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
                                    final Sequence seqOut = sequence.convertToType(dataType,
                                            scaledCheckBox.isSelected());

                                    // set sequence name
                                    seqOut.setName(sequence.getName() + " [" + dataType.toString(true) + "]");

                                    Icy.addSequence(seqOut);
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
            typeButton.setEnabled(enabled);
            scaledCheckBox.setEnabled(enabled);
        }
    }

    public static class ZStackOperationRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8301134961618666184L;

        private static final String NAME = "Z Stack operation";

        final IcyCommandButton removeFirstButton;
        final IcyCommandButton removeLastButton;
        final IcyCommandButton removeCurrentButton;
        final IcyCommandButton moveCloserButton;
        final IcyCommandButton moveFartherButton;
        final IcyCommandButton reverseButton;
        final IcyCommandButton removeEachButton;
        final IcyCommandButton reverseEachButton;
        final IcyCommandButton advancedButton;

        public ZStackOperationRibbonBand()
        {
            super(NAME, new IcyIcon("layers_1"));

            // REMOVE
            startGroup();

            // Remove first slice
            removeFirstButton = new IcyCommandButton("Remove first", ResourceUtil.ICON_SQUARE_UP);
            removeFirstButton.setActionRichTooltip(new RichTooltip("Remove first Z slice",
                    "Remove the first Z slice from the sequence"));
            removeFirstButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SequenceUtil.removeZAndShift(Icy.getMainInterface().getFocusedSequence(), 0);
                        }
                    });
                }
            });
            addCommandButton(removeFirstButton, RibbonElementPriority.MEDIUM);

            // Remove last slice
            removeLastButton = new IcyCommandButton("Remove last", ResourceUtil.ICON_SQUARE_DOWN);
            removeLastButton.setActionRichTooltip(new RichTooltip("Remove last Z slice",
                    "Remove the last Z slice the sequence"));
            removeLastButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Sequence seq = Icy.getMainInterface().getFocusedSequence();

                            SequenceUtil.removeZ(seq, seq.getSizeZ() - 1);
                        }
                    });
                }
            });
            addCommandButton(removeLastButton, RibbonElementPriority.MEDIUM);

            // current slice
            removeCurrentButton = new IcyCommandButton("Remove current", ResourceUtil.ICON_SQUARE_NEXT);
            removeCurrentButton.setActionRichTooltip(new RichTooltip("Remove current Z slice",
                    "Remove the current displayed Z slice from the sequence"));
            removeCurrentButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SequenceUtil.removeZAndShift(Icy.getMainInterface().getFocusedSequence(), Icy
                                    .getMainInterface().getFocusedViewer().getZ());
                        }
                    });
                }
            });
            addCommandButton(removeCurrentButton, RibbonElementPriority.MEDIUM);

            // MOVE
            startGroup();

            // slice closer
            moveCloserButton = new IcyCommandButton("Closer", "arrow_top");
            moveCloserButton.setActionRichTooltip(new RichTooltip("Move to front",
                    "Move the current displayed Z slice to position Z - 1"));
            moveCloserButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                            final int currentZ = viewer.getZ();

                            if (currentZ > 0)
                            {
                                SequenceUtil.swapZ(Icy.getMainInterface().getFocusedSequence(), currentZ, currentZ - 1);
                                viewer.setZ(currentZ - 1);
                            }
                        }
                    });
                }
            });
            addCommandButton(moveCloserButton, RibbonElementPriority.MEDIUM);

            // slice farther
            moveFartherButton = new IcyCommandButton("Farther", "arrow_bottom");
            moveFartherButton.setActionRichTooltip(new RichTooltip("Move to back",
                    "Move the current displayed Z slice to position Z + 1"));
            moveFartherButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                            final int currentZ = viewer.getZ();

                            if (currentZ >= 0)
                            {
                                SequenceUtil.swapZ(Icy.getMainInterface().getFocusedSequence(), currentZ, currentZ + 1);
                                viewer.setZ(currentZ + 1);
                            }
                        }
                    });
                }
            });
            addCommandButton(moveFartherButton, RibbonElementPriority.MEDIUM);

            // Reverse slices
            reverseButton = new IcyCommandButton("Reverse order", "arrow_two_head_2");
            reverseButton.setActionRichTooltip(new RichTooltip("Reverse Z slices order",
                    "Reverse Z slices order of sequence"));
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
                            SequenceUtil.reverseZ(Icy.getMainInterface().getFocusedSequence());
                        }
                    });
                }
            });
            addCommandButton(reverseButton, RibbonElementPriority.MEDIUM);

            // MISC
            // TODO : uncomment when ready
            // startGroup();

            // Remove each ...
            removeEachButton = new IcyCommandButton("Remove each...", "align_right");
            removeEachButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            removeEachButton.setPopupRichTooltip(new RichTooltip("Advanced Z slice remove",
                    "Remove each other, each third slice..."));
            removeEachButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
            // TODO : uncomment when ready
            // addCommandButton(removeEachButton, RibbonElementPriority.MEDIUM);

            // Reverse each ...
            reverseEachButton = new IcyCommandButton("Reverse each...", "arrow_two_head_2");
            reverseEachButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            reverseEachButton.setPopupRichTooltip(new RichTooltip("Advanced Z slice reverse",
                    "Reverse each other, each third slice..."));
            reverseEachButton.setPopupCallback(new PopupPanelCallback()
            {
                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
            // TODO : uncomment when ready
            // addCommandButton(reverseEachButton, RibbonElementPriority.MEDIUM);

            // advanced
            advancedButton = new IcyCommandButton("Advanced...", new IcyIcon(ResourceUtil.ICON_COG));
            advancedButton.setActionRichTooltip(new RichTooltip("Advanced Z stack operation",
                    "Advanced Z stack remove, move and misc operations..."));
            advancedButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // advanced operation dialog

                        }
                    });
                }
            });
            // TODO : uncomment when ready
            // addCommandButton(advancedButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            removeFirstButton.setEnabled(enabled);
            removeLastButton.setEnabled(enabled);
            removeCurrentButton.setEnabled(enabled);
            moveCloserButton.setEnabled(enabled);
            moveFartherButton.setEnabled(enabled);
            reverseButton.setEnabled(enabled);
            // TODO: change when ready
            removeEachButton.setEnabled(false);
            reverseEachButton.setEnabled(false);
            advancedButton.setEnabled(false);
        }
    }

    public static class TStackOperationRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3728386745443331069L;

        private static final String NAME = "T Stack operation";

        final IcyCommandButton removeFirstButton;
        final IcyCommandButton removeLastButton;
        final IcyCommandButton removeCurrentButton;
        final IcyCommandButton moveCloserButton;
        final IcyCommandButton moveFartherButton;
        final IcyCommandButton reverseButton;
        final IcyCommandButton removeEachButton;
        final IcyCommandButton reverseEachButton;
        final IcyCommandButton advancedButton;

        public TStackOperationRibbonBand()
        {
            super(NAME, new IcyIcon("layers_1"));

            // REMOVE
            startGroup();

            // Remove first slice
            removeFirstButton = new IcyCommandButton("Remove first", ResourceUtil.ICON_SQUARE_UP);
            removeFirstButton.setActionRichTooltip(new RichTooltip("Remove first T slice",
                    "Remove the first T slice from the sequence"));
            removeFirstButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SequenceUtil.removeTAndShift(Icy.getMainInterface().getFocusedSequence(), 0);
                        }
                    });
                }
            });
            addCommandButton(removeFirstButton, RibbonElementPriority.MEDIUM);

            // Remove last slice
            removeLastButton = new IcyCommandButton("Remove last", ResourceUtil.ICON_SQUARE_DOWN);
            removeLastButton.setActionRichTooltip(new RichTooltip("Remove last T slice",
                    "Remove the last T slice the sequence"));
            removeLastButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Sequence seq = Icy.getMainInterface().getFocusedSequence();

                            SequenceUtil.removeT(seq, seq.getSizeT() - 1);
                        }
                    });
                }
            });
            addCommandButton(removeLastButton, RibbonElementPriority.MEDIUM);

            // current slice
            removeCurrentButton = new IcyCommandButton("Remove current", ResourceUtil.ICON_SQUARE_NEXT);
            removeCurrentButton.setActionRichTooltip(new RichTooltip("Remove current T slice",
                    "Remove the current displayed T slice from the sequence"));
            removeCurrentButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SequenceUtil.removeTAndShift(Icy.getMainInterface().getFocusedSequence(), Icy
                                    .getMainInterface().getFocusedViewer().getT());
                        }
                    });
                }
            });
            addCommandButton(removeCurrentButton, RibbonElementPriority.MEDIUM);

            // MOVE
            startGroup();

            // slice closer
            moveCloserButton = new IcyCommandButton("Closer", "arrow_top");
            moveCloserButton.setActionRichTooltip(new RichTooltip("Move to front",
                    "Move the current displayed T slice to position T - 1"));
            moveCloserButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                            final int currentT = viewer.getT();

                            if (currentT > 0)
                            {
                                SequenceUtil.swapT(Icy.getMainInterface().getFocusedSequence(), currentT, currentT - 1);
                                viewer.setT(currentT - 1);
                            }
                        }
                    });
                }
            });
            addCommandButton(moveCloserButton, RibbonElementPriority.MEDIUM);

            // slice farther
            moveFartherButton = new IcyCommandButton("Farther", "arrow_bottom");
            moveFartherButton.setActionRichTooltip(new RichTooltip("Move to back",
                    "Move the current displayed T slice to position T + 1"));
            moveFartherButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                            final int currentT = viewer.getT();

                            if (currentT >= 0)
                            {
                                SequenceUtil.swapT(Icy.getMainInterface().getFocusedSequence(), currentT, currentT + 1);
                                viewer.setT(currentT + 1);
                            }
                        }
                    });
                }
            });
            addCommandButton(moveFartherButton, RibbonElementPriority.MEDIUM);

            // Reverse slices
            reverseButton = new IcyCommandButton("Reverse order", "arrow_two_head_2");
            reverseButton.setActionRichTooltip(new RichTooltip("Reverse T slices order",
                    "Reverse T slices order of sequence"));
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
                            SequenceUtil.reverseT(Icy.getMainInterface().getFocusedSequence());
                        }
                    });
                }
            });
            addCommandButton(reverseButton, RibbonElementPriority.MEDIUM);

            // MISC
            // TODO : uncomment when ready
            // startGroup();

            // Remove each ...
            removeEachButton = new IcyCommandButton("Remove each...", "align_right");
            removeEachButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            removeEachButton.setPopupRichTooltip(new RichTooltip("Advanced T slice remove",
                    "Remove each other, each third slice..."));
            removeEachButton.setPopupCallback(new PopupPanelCallback()
            {

                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
            // TODO : uncomment when ready
            // addCommandButton(removeEachButton, RibbonElementPriority.MEDIUM);

            // Reverse each ...
            reverseEachButton = new IcyCommandButton("Reverse each...", "arrow_two_head_2");
            reverseEachButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
            reverseEachButton.setPopupRichTooltip(new RichTooltip("Advanced T slice reverse",
                    "Reverse each other, each third slice..."));
            reverseEachButton.setPopupCallback(new PopupPanelCallback()
            {

                @Override
                public JPopupPanel getPopupPanel(JCommandButton commandButton)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
            // TODO : uncomment when ready
            // addCommandButton(reverseEachButton, RibbonElementPriority.MEDIUM);

            // advanced
            advancedButton = new IcyCommandButton("Advanced...", new IcyIcon(ResourceUtil.ICON_COG));
            advancedButton.setActionRichTooltip(new RichTooltip("Advanced T stack operation",
                    "Advanced T stack remove, move and misc operations..."));
            advancedButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // advanced operation dialog

                        }
                    });
                }
            });
            // TODO : uncomment when ready
            // addCommandButton(advancedButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            removeFirstButton.setEnabled(enabled);
            removeLastButton.setEnabled(enabled);
            removeCurrentButton.setEnabled(enabled);
            moveCloserButton.setEnabled(enabled);
            moveFartherButton.setEnabled(enabled);
            reverseButton.setEnabled(enabled);
            // TODO: change when ready
            removeEachButton.setEnabled(false);
            reverseEachButton.setEnabled(false);
            advancedButton.setEnabled(false);
        }
    }

    public static class SizeOperationRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7475753600896040618L;

        private static final String NAME = "Size operation";

        final IcyCommandButton cropButton;

        public SizeOperationRibbonBand()
        {
            super(NAME, new IcyIcon("layers_1"));

            // SIZE
            startGroup();

            // crop operation
            cropButton = new IcyCommandButton("Crop image", "clipboard_cut");
            cropButton.setActionRichTooltip(new RichTooltip("Crop image", "Crop an image from ROI."));
            cropButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SequenceCropper();
                }
            });
            addCommandButton(cropButton, RibbonElementPriority.MEDIUM);

            RibbonUtil.setRestrictiveResizePolicies(this);
            udpateButtonsState();
        }

        void udpateButtonsState()
        {
            final boolean enabled = Icy.getMainInterface().getFocusedSequence() != null;

            cropButton.setEnabled(enabled);
        }
    }

    public static class StackConversionRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8210688977085548878L;

        private static final String NAME = "Stack conversion";

        final IcyCommandButton convertToZButton;
        final IcyCommandButton convertToTButton;
        final IcyCommandButton advancedConvertButton;

        public StackConversionRibbonBand()
        {
            super(NAME, new IcyIcon("layers_1"));

            // CONVERT
            startGroup();

            // convert to Z stack
            convertToZButton = new IcyCommandButton("Convert to stack", "pin_sq_top");
            convertToZButton.setActionRichTooltip(new RichTooltip("Convert to stack", "Set all images in Z dimension"));
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
            convertToTButton = new IcyCommandButton("Convert to time", "pin_sq_right");
            convertToTButton.setActionRichTooltip(new RichTooltip("Convert to time", "Set all images in T dimension"));
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
            advancedConvertButton.setActionRichTooltip(new RichTooltip("Advanced stack conversion",
                    "Advanced stack conversion operation"));
            advancedConvertButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new Time2Volume();
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

        public static final String NAME = "Fill";

        final JTextField fillValueField;
        final IcyButton fillImage;
        final IcyButton fillSequence;

        public ModifyRibbonBand()
        {
            super(NAME, new IcyIcon("document"));
            {

                // MODIFY
                startGroup();

                fillValueField = new JTextField();
                ComponentUtil.setFixedWidth(fillValueField, 90);
                fillValueField.setToolTipText("Value used for filling");
                fillValueField.setText("0");

                fillImage = new IcyButton("Image", "brush", 16);
                fillImage.setFlat(true);
                fillImage.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                ComponentUtil.setFixedWidth(fillImage, 90);
                fillImage.setHorizontalAlignment(SwingConstants.LEADING);
                fillImage.setToolTipText("Fill the content of selected ROI with specified value on current image");
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

                fillSequence = new IcyButton("Sequence", "brush", 16);
                fillSequence.setFlat(true);
                fillSequence.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                ComponentUtil.setFixedWidth(fillSequence, 90);
                fillSequence.setHorizontalAlignment(SwingConstants.LEADING);
                fillSequence.setToolTipText("Fill the content of selected ROI with specified value on whole sequence");
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

    public static final String NAME = "Sequence / Image operation";

    private final ConvertRibbonBand convertBand;
    private final ChannelOperationRibbonBand channelOperationBand;
    private final SizeOperationRibbonBand sizeOperationBand;
    private final StackConversionRibbonBand stackConversionBand;
    private final ZStackOperationRibbonBand zStackOperationBand;
    private final TStackOperationRibbonBand tStackOperationBand;
    private final ModifyRibbonBand modifyBand;

    public ImageRibbonTask()
    {
        super(NAME, new ConvertRibbonBand(), new ChannelOperationRibbonBand(), new SizeOperationRibbonBand(),
                new StackConversionRibbonBand(), new ZStackOperationRibbonBand(), new TStackOperationRibbonBand(),
                new ModifyRibbonBand());

        convertBand = (ConvertRibbonBand) getBand(0);
        channelOperationBand = (ChannelOperationRibbonBand) getBand(1);
        sizeOperationBand = (SizeOperationRibbonBand) getBand(2);
        stackConversionBand = (StackConversionRibbonBand) getBand(3);
        zStackOperationBand = (ZStackOperationRibbonBand) getBand(4);
        tStackOperationBand = (TStackOperationRibbonBand) getBand(5);
        modifyBand = (ModifyRibbonBand) getBand(6);
    }

    /**
     * call this method on sequence focus change
     */
    public void onSequenceFocusChange()
    {
        convertBand.udpateButtonsState();
        channelOperationBand.udpateButtonsState();
        sizeOperationBand.udpateButtonsState();
        stackConversionBand.udpateButtonsState();
        zStackOperationBand.udpateButtonsState();
        tStackOperationBand.udpateButtonsState();
        modifyBand.udpateButtonsState();
    }
}

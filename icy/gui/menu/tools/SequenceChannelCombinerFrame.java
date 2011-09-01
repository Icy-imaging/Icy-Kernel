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
package icy.gui.menu.tools;

import icy.gui.component.ComponentUtil;
import icy.gui.component.SequenceChooser;
import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImage.FilterType;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

/**
 * @author Stephane
 */
public class SequenceChannelCombinerFrame extends ActionFrame
{
    private class ChannelChooserPanel extends JPanel implements ActionListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3188565507126815456L;

        private Sequence sequence;

        private final ArrayList<Integer> selectedChannels;

        // GUI
        private final JPanel channelSelectPanel;

        /**
         * 
         */
        public ChannelChooserPanel(String title)
        {
            super();

            sequence = Icy.getMainInterface().getFocusedSequence();
            selectedChannels = new ArrayList<Integer>();

            // GUI
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setBorder(BorderFactory.createTitledBorder(title));
            // fix the height of panel
            ComponentUtil.setFixedHeight(this, 40);

            final JPanel sequenceSelectPanel = new JPanel();
            sequenceSelectPanel.setLayout(new BoxLayout(sequenceSelectPanel, BoxLayout.LINE_AXIS));
            // fix the height of sequence select panel
            ComponentUtil.setFixedHeight(sequenceSelectPanel, 24);

            // select sequence
            final SequenceChooser sequenceSelector = new SequenceChooser(48);
            sequenceSelector.setSelectedSequence(sequence);
            sequenceSelector.setMinimumSize(new Dimension(100, 24));
            sequenceSelector.addActionListener(this);

            sequenceSelectPanel.add(Box.createHorizontalStrut(4));
            sequenceSelectPanel.add(sequenceSelector);
            sequenceSelectPanel.add(Box.createHorizontalStrut(8));

            channelSelectPanel = new JPanel();
            channelSelectPanel.setLayout(new BoxLayout(channelSelectPanel, BoxLayout.LINE_AXIS));
            // fix the height of channel select panel
            ComponentUtil.setFixedHeight(channelSelectPanel, 24);

            add(sequenceSelectPanel);
            add(Box.createVerticalStrut(8));
            add(channelSelectPanel);

            updateChannelPanel();
        }

        /**
         * @return the sequence
         */
        public Sequence getSequence()
        {
            return sequence;
        }

        /**
         * @return the selectedBands
         */
        public ArrayList<Integer> getSelectedChannels()
        {
            return new ArrayList<Integer>(selectedChannels);
        }

        private void setSequence(Sequence value)
        {
            if (sequence != value)
            {
                sequence = value;

                // rebuild
                selectedChannels.clear();
                updateChannelPanel();
                updateEnable();
            }
        }

        void updateChannelPanel()
        {
            channelSelectPanel.removeAll();

            if (sequence != null)
            {
                channelSelectPanel.add(Box.createHorizontalStrut(4));
                channelSelectPanel.add(new JLabel("Select channel :"));
                channelSelectPanel.add(Box.createHorizontalStrut(8));

                for (int i = 0; i < sequence.getSizeC(); i++)
                {
                    final int channel = i;
                    final JCheckBox checkBox = new JCheckBox(" " + channel + "  ");

                    checkBox.setSelected(selectedChannels.contains(Integer.valueOf(channel)));
                    checkBox.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            if (checkBox.isSelected())
                                addChannel(channel);
                            else
                                removeChannel(channel);
                        }
                    });

                    channelSelectPanel.add(checkBox);
                }
            }
            else
                channelSelectPanel.add(new JLabel("no sequence selected"));

            channelSelectPanel.add(Box.createHorizontalGlue());

            channelSelectPanel.validate();
        }

        void addChannel(int channel)
        {
            final Integer value = Integer.valueOf(channel);

            if (!selectedChannels.contains(value))
            {
                selectedChannels.add(value);
                updateEnable();
            }
        }

        void removeChannel(int channel)
        {
            final Integer value = Integer.valueOf(channel);

            if (selectedChannels.contains(value))
            {
                selectedChannels.remove(value);
                updateEnable();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final String cmd = e.getActionCommand();

            if (SequenceChooser.SEQUENCE_SELECT_CMD.equals(cmd))
                setSequence(((SequenceChooser) e.getSource()).getSelectedSequence());
        }
    }

    // GUI
    final ChannelChooserPanel sequence1;
    final ChannelChooserPanel sequence2;
    final JCheckBox fitToMaxSize;
    final JCheckBox fillStackHole;

    public SequenceChannelCombinerFrame()
    {
        super("Channel combiner", true);

        // channel extraction on validation
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // so it won't change during process
                final Sequence seqIn1 = sequence1.getSequence();
                final ArrayList<Integer> channels1 = sequence1.getSelectedChannels();
                final Sequence seqIn2 = sequence2.getSequence();
                final ArrayList<Integer> channels2 = sequence2.getSelectedChannels();
                final Sequence seqOut = new Sequence();

                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Copying channel(s)...");
                        try
                        {
                            int sizeX = 0;
                            int sizeY = 0;
                            int sizeZ = 0;
                            int sizeT = 0;

                            if (seqIn1 != null)
                            {
                                sizeX = Math.max(seqIn1.getSizeX(), sizeX);
                                sizeY = Math.max(seqIn1.getSizeY(), sizeY);
                                sizeZ = Math.max(seqIn1.getSizeZ(), sizeZ);
                                sizeT = Math.max(seqIn1.getSizeT(), sizeT);
                            }

                            if (seqIn2 != null)
                            {
                                sizeX = Math.max(seqIn2.getSizeX(), sizeX);
                                sizeY = Math.max(seqIn2.getSizeY(), sizeY);
                                sizeZ = Math.max(seqIn2.getSizeZ(), sizeZ);
                                sizeT = Math.max(seqIn2.getSizeT(), sizeT);
                            }

                            final int dataType;
                            final boolean signedDataType;

                            // we use sequence1 if possible data type for output data type
                            if (seqIn1 != null)
                            {
                                dataType = seqIn1.getDataType();
                                signedDataType = seqIn1.isSignedDataType();
                            }
                            else
                            {
                                dataType = seqIn2.getDataType();
                                signedDataType = seqIn2.isSignedDataType();
                            }

                            final ArrayList<BufferedImage> imgList = new ArrayList<BufferedImage>();

                            seqOut.beginUpdate();
                            try
                            {
                                IcyBufferedImage srcChannelImg1 = null;
                                IcyBufferedImage srcChannelImg2 = null;

                                for (int t = 0; t < sizeT; t++)
                                {
                                    for (int z = 0; z < sizeZ; z++)
                                    {
                                        imgList.clear();

                                        if ((seqIn1 != null) && (channels1.size() > 0))
                                        {
                                            IcyBufferedImage srcImg1 = seqIn1.getImage(t, z);

                                            if (srcImg1 != null)
                                            {
                                                // resize image if needed
                                                if ((srcImg1.getSizeX() != sizeX) || (srcImg1.getSizeY() != sizeY))
                                                    srcImg1 = srcImg1.getScaledCopy(sizeX, sizeY,
                                                            fitToMaxSize.isSelected(), SwingConstants.CENTER,
                                                            SwingConstants.CENTER, FilterType.NEAREST);
                                                // get specified channels of srcImg1
                                                srcChannelImg1 = srcImg1.extractBands(channels1);
                                            }
                                            else if ((!fillStackHole.isSelected()) || (srcChannelImg1 == null))
                                                // get an empty image
                                                srcChannelImg1 = new IcyBufferedImage(sizeX, sizeY, channels1.size(),
                                                        dataType, signedDataType);

                                            imgList.add(srcChannelImg1);
                                        }

                                        if ((seqIn2 != null) && (channels2.size() > 0))
                                        {
                                            IcyBufferedImage srcImg2 = seqIn2.getImage(t, z);

                                            if (srcImg2 != null)
                                            {
                                                // convert image if needed
                                                if ((srcImg2.getDataType() != dataType)
                                                        || (srcImg2.isSignedDataType() != signedDataType))
                                                    srcImg2 = srcImg2.convertToType(dataType, signedDataType, true);
                                                // resize image if needed
                                                if ((srcImg2.getSizeX() != sizeX) || (srcImg2.getSizeY() != sizeY))
                                                    srcImg2 = srcImg2.getScaledCopy(sizeX, sizeY,
                                                            fitToMaxSize.isSelected(), SwingConstants.CENTER,
                                                            SwingConstants.CENTER, FilterType.NEAREST);
                                                // get specified channels of srcImg2
                                                srcChannelImg2 = srcImg2.extractBands(channels2);
                                            }
                                            else if ((!fillStackHole.isSelected()) || (srcChannelImg2 == null))
                                                // get an empty image
                                                srcChannelImg2 = new IcyBufferedImage(sizeX, sizeY, channels2.size(),
                                                        dataType, signedDataType);

                                            imgList.add(srcChannelImg2);
                                        }

                                        // add a new composed image
                                        seqOut.setImage(t, z, IcyBufferedImage.createFrom(imgList));
                                    }
                                }

                                // set sequence name
                                seqOut.setName("Channels combination");
                            }
                            finally
                            {
                                seqOut.endUpdate();
                            }

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

        // GUI
        sequence1 = new ChannelChooserPanel("Sequence 1");
        sequence2 = new ChannelChooserPanel("Sequence 2");
        fitToMaxSize = new JCheckBox("scale image to maximum dimension");
        fillStackHole = new JCheckBox("fill stack hole");
        fillStackHole.setToolTipText("duplicate last image found in stack to fill output stack");

        final JSplitPane spliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sequence1, sequence2);
        spliter.setResizeWeight(0.5);
        spliter.setDividerSize(5);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(spliter);
        mainPanel.add(fitToMaxSize);
        mainPanel.add(fillStackHole);

        // this rebuild panel from current focused sequence
        updateEnable();

        setPreferredSize(new Dimension(640, 300));
        pack();
        setVisible(true);
        addToMainDesktopPane();
        setLocation(50, 50);
        requestFocus();
    }

    void updateEnable()
    {
        final boolean selectedBandsOk = (sequence1.getSelectedChannels().size() > 0)
                || (sequence2.getSelectedChannels().size() > 0);
        final boolean sequenceOk = (sequence1.getSequence() != null) || (sequence2.getSequence() != null);

        // disable action if source is not defined
        getOkBtn().setEnabled(selectedBandsOk && sequenceOk);
    }

}

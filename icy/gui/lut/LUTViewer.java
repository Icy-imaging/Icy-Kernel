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
package icy.gui.lut;

import icy.gui.component.CheckTabbedPane;
import icy.gui.lut.abstract_.IcyLutViewer;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.lut.LUT;
import icy.image.lut.LUTBand;
import icy.math.Scaler;
import icy.sequence.Sequence;
import icy.sequence.SequenceAdapter;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.sequence.WeakSequenceListener;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LUTViewer extends IcyLutViewer
{
    private static final long serialVersionUID = 8385018166371243663L;

    /**
     * gui
     */
    final CheckTabbedPane bottomPane;

    final JCheckBox autoRefreshHistoCheckBox;
    final JCheckBox autoBoundsCheckBox;
    private final ButtonGroup scaleGroup;
    private final JRadioButton logButton;
    private final JRadioButton linearButton;

    /**
     * data
     */
    final ArrayList<LUTBandViewer> lutBandViewers;

    /**
     * internals
     */
    private final SequenceListener sequenceListener;
    private final WeakSequenceListener weakSequenceListener;

    public LUTViewer(final Viewer viewer, final LUT lut)
    {
        super(viewer, lut);

        final Sequence sequence = getSequence();

        lutBandViewers = new ArrayList<LUTBandViewer>();

        bottomPane = new CheckTabbedPane(SwingConstants.BOTTOM, true);
        bottomPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        bottomPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                for (int i = 0; i < lutBandViewers.size(); i++)
                {
                    boolean b;

                    try
                    {
                        // null pointer exception can sometime happen here, normal
                        b = bottomPane.isTabChecked(i);
                    }
                    catch (Exception exc)
                    {
                        b = true;
                    }

                    lutBandViewers.get(i).getColormapPanel().getColormapViewer().getColormap().setEnabled(b);
                }
            }
        });

        // GUI
        for (LUTBand lutBand : lut.getLutBands())
        {
            final LUTBandViewer lbv = new LUTBandViewer(viewer, lutBand);

            lutBandViewers.add(lbv);
            bottomPane.addTab("channel", lbv);
        }

        refreshChannelsName(sequence);

        autoRefreshHistoCheckBox = new JCheckBox("Refresh", true);
        autoRefreshHistoCheckBox.setToolTipText("Automatically refresh histogram when data is modified");
        autoRefreshHistoCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setHistoEnabled(autoRefreshHistoCheckBox.isSelected());
            }
        });

        autoBoundsCheckBox = new JCheckBox("Auto bounds", false);
        autoBoundsCheckBox.setToolTipText("Automatically ajdust bounds when data is modified");
        autoBoundsCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                refreshBounds(getSequence());
            }
        });

        scaleGroup = new ButtonGroup();
        logButton = new JRadioButton("log");
        logButton.setToolTipText("Display histogram in a logarithm form");
        logButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setLogScale(true);
            }
        });
        linearButton = new JRadioButton("linear");
        linearButton.setToolTipText("Display histogram in a linear form");
        linearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setLogScale(false);
            }
        });

        scaleGroup.add(logButton);
        scaleGroup.add(linearButton);

        // default
        setLogScale(true);
        logButton.setSelected(true);

        setLayout(new BorderLayout());

        add(GuiUtil.createLineBoxPanel(autoRefreshHistoCheckBox, autoBoundsCheckBox, Box.createHorizontalGlue(),
                Box.createHorizontalStrut(4), logButton, linearButton), BorderLayout.NORTH);
        add(bottomPane, BorderLayout.CENTER);

        validate();

        sequenceListener = new SequenceAdapter()
        {
            @Override
            public void sequenceChanged(SequenceEvent event)
            {
                final SequenceEvent e = event;

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        switch (e.getSourceType())
                        {
                            case SEQUENCE_META:
                                refreshChannelsName(e.getSequence());
                                break;

                            case SEQUENCE_COMPONENTBOUNDS:
                                if (autoBoundsCheckBox.isSelected())
                                    refreshBounds(e.getSequence());
                                break;
                        }
                    }
                });
            }
        };

        // weak reference --> released when LUTViewer is released
        weakSequenceListener = new WeakSequenceListener(sequenceListener);
        getSequence().addListener(weakSequenceListener);
    }

    void setHistoEnabled(boolean value)
    {
        for (int i = 0; i < lutBandViewers.size(); i++)
            lutBandViewers.get(i).getScalerPanel().getScalerViewer().setHistoEnabled(value);
    }

    void setLogScale(boolean value)
    {
        for (int i = 0; i < lutBandViewers.size(); i++)
            lutBandViewers.get(i).getScalerPanel().getScalerViewer().setLogScale(value);
    }

    void refreshBounds(Sequence sequence)
    {
        if (sequence != null)
        {
            double[][] typeBounds = sequence.getChannelsTypeBounds();
            double[][] bounds = sequence.getChannelsBounds();

            for (int i = 0; i < Math.min(lutBandViewers.size(), typeBounds.length); i++)
            {
                double[] tb = typeBounds[i];
                double[] b = bounds[i];

                final Scaler scaler = lutBandViewers.get(i).getScalerPanel().getScaler();

                scaler.setAbsLeftRightIn(tb[0], tb[1]);
                scaler.setLeftRightIn(b[0], b[1]);
            }
        }
    }

    void refreshChannelsName(Sequence sequence)
    {
        if (sequence != null)
        {
            for (int c = 0; c < Math.min(bottomPane.getTabCount(), sequence.getSizeC()); c++)
            {
                final String channelName = sequence.getChannelName(c);

                bottomPane.setTitleAt(c, StringUtil.limit(channelName, 10));
                if (sequence.getDefaultChannelName(c).equals(channelName))
                    bottomPane.setToolTipTextAt(c, "Channel " + c);
                else
                    bottomPane.setToolTipTextAt(c, channelName + " (channel " + c + ")");
            }
        }
    }
}

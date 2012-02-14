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
import icy.sequence.Sequence;
import icy.sequence.SequenceAdapter;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.sequence.WeakSequenceListener;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
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

    final JCheckBox autoCheckBox;
    private final ButtonGroup scaleGroup;
    private final JRadioButton logButton;
    private final JRadioButton linearButton;
    // private final JButton updateBoundsButton;

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

        lutBandViewers = new ArrayList<LUTBandViewer>();

        bottomPane = new CheckTabbedPane(SwingConstants.BOTTOM, true);
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
        if (lut != null)
        {
            for (LUTBand lutBand : lut.getLutBands())
            {
                final LUTBandViewer lbv = new LUTBandViewer(viewer, lutBand);
                final int ch = lutBand.getComponent();

                lutBandViewers.add(lbv);
                bottomPane.addTab("ch " + ch, lbv);
                bottomPane.setToolTipTextAt(ch, "Channel " + ch);
            }
        }

        refreshChannelsName();

        final boolean check;

        if (getSequence() != null)
            check = getSequence().isComponentUserBoundsAutoUpdate();
        else
            check = false;

        autoCheckBox = new JCheckBox("auto adjust", check);
        autoCheckBox.setToolTipText("Automatically ajdust bounds when data is modified");
        autoCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = getSequence();

                if (sequence != null)
                {
                    sequence.setComponentUserBoundsAutoUpdate(autoCheckBox.isSelected());
                    sequence.setComponentAbsBoundsAutoUpdate(autoCheckBox.isSelected());
                }
            }
        });

        final JLabel viewLabel = new JLabel("View");
        viewLabel.setToolTipText("Selection histogram display type");

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

        // updateBoundsButton = new JButton("Refresh");
        // updateBoundsButton.setToolTipText("Force histogram and bounds recalculation");
        // updateBoundsButton.addActionListener(new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // final Sequence sequence = getSequence();
        //
        // if ((sequence != null) && (lut != null))
        // {
        // sequence.updateComponentsBounds(true, true);
        // lut.copyScalers(sequence.createCompatibleLUT());
        // refreshHistogram();
        // }
        // }
        // });

        setLayout(new BorderLayout());

        add(GuiUtil.createLineBoxPanel(autoCheckBox, Box.createHorizontalGlue(), // viewLabel,
                Box.createHorizontalStrut(8), logButton, linearButton), BorderLayout.NORTH);
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
                                refreshChannelsName();
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

    void setLogScale(boolean value)
    {
        for (int i = 0; i < lutBandViewers.size(); i++)
            lutBandViewers.get(i).getScalerPanel().getScalerViewer().setLogScale(value);
    }

    void refreshHistogram()
    {
        for (int i = 0; i < lutBandViewers.size(); i++)
            lutBandViewers.get(i).getScalerPanel().refreshHistoData();
    }

    void refreshChannelsName()
    {
        final Sequence seq = getSequence();

        if (seq != null)
        {
            final int sizeC = seq.getSizeC();

            for (int c = 0; c < sizeC; c++)
                bottomPane.setTitleAt(c, seq.getChannelName(c));
        }
    }
}

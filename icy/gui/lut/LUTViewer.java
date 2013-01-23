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
import icy.image.lut.LUT.LUTChannel;
import icy.math.Scaler;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequenceAdapter;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.sequence.WeakSequenceListener;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
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
     * pref id
     */
    private static final String PREF_ID_HISTO = "gui.histo";

    private static final String ID_AUTO_REFRESH = "autoRefresh";
    private static final String ID_LOG_VIEW = "logView";

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
    final ArrayList<LUTChannelViewer> lutChannelViewers;

    /**
     * preferences
     */
    final XMLPreferences pref;

    /**
     * internals
     */
    private final SequenceListener sequenceListener;
    private final WeakSequenceListener weakSequenceListener;

    public LUTViewer(final Viewer viewer, final LUT lut)
    {
        super(viewer, lut);

        pref = ApplicationPreferences.getPreferences().node(PREF_ID_HISTO);

        lutChannelViewers = new ArrayList<LUTChannelViewer>();

        bottomPane = new CheckTabbedPane(SwingConstants.BOTTOM, true);
        bottomPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        bottomPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                for (int i = 0; i < lutChannelViewers.size(); i++)
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

                    lutChannelViewers.get(i).getLutChannel().getColorMap().setEnabled(b);
                }
            }
        });

        // GUI
        for (int c = 0; c < lut.getLutChannels().size(); c++)
        {
            final LUTChannel lutChannel = lut.getLutChannels().get(c);
            final LUTChannelViewer lbv = new LUTChannelViewer(viewer, lutChannel);

            lutChannelViewers.add(lbv);
            bottomPane.addTab("ch " + c, lbv);
        }

        refreshChannelsName();

        autoRefreshHistoCheckBox = new JCheckBox("Refresh", pref.getBoolean(ID_AUTO_REFRESH, true));
        autoRefreshHistoCheckBox.setToolTipText("Automatically refresh histogram when data is modified");
        autoRefreshHistoCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean value = autoRefreshHistoCheckBox.isSelected();
                autoRefreshHistogramInternal(value);
                pref.putBoolean(ID_AUTO_REFRESH, value);
            }
        });
        autoRefreshHistogramInternal(autoRefreshHistoCheckBox.isSelected());

        autoBoundsCheckBox = new JCheckBox("Auto bounds", getPreferredAutoBounds());
        autoBoundsCheckBox.setToolTipText("Automatically ajdust bounds when data is modified");
        autoBoundsCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean value = autoBoundsCheckBox.isSelected();

                if (value)
                {
                    refreshBounds();
                    autoRefreshHistogramInternal(true);
                    autoRefreshHistoCheckBox.setSelected(true);
                    autoRefreshHistoCheckBox.setEnabled(false);
                }
                else
                {
                    final boolean refreshValue = pref.getBoolean(ID_AUTO_REFRESH, true);
                    autoRefreshHistogramInternal(refreshValue);
                    autoRefreshHistoCheckBox.setSelected(refreshValue);
                    autoRefreshHistoCheckBox.setEnabled(true);
                }
            }
        });
        if (autoBoundsCheckBox.isSelected())
        {
            refreshBounds();
            autoRefreshHistogramInternal(true);
            autoRefreshHistoCheckBox.setSelected(true);
            autoRefreshHistoCheckBox.setEnabled(false);
        }

        scaleGroup = new ButtonGroup();
        logButton = new JRadioButton("log");
        logButton.setToolTipText("Display histogram in a logarithm form");
        logButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setLogScale(true);
                pref.putBoolean(ID_LOG_VIEW, true);
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
                pref.putBoolean(ID_LOG_VIEW, false);
            }
        });

        scaleGroup.add(logButton);
        scaleGroup.add(linearButton);

        // default
        final boolean b = pref.getBoolean(ID_LOG_VIEW, true);
        setLogScale(b);
        if (b)
            logButton.setSelected(true);
        else
            linearButton.setSelected(true);

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

                switch (e.getSourceType())
                {
                    case SEQUENCE_META:
                        refreshChannelsName();
                        break;

                    case SEQUENCE_COMPONENTBOUNDS:
                        if (autoBoundsCheckBox.isSelected())
                            refreshBounds();
                        break;
                }
            }
        };

        // weak reference --> released when LUTViewer is released
        weakSequenceListener = new WeakSequenceListener(sequenceListener);
        getSequence().addListener(weakSequenceListener);
    }

    private boolean getPreferredAutoBounds()
    {
        final Sequence sequence = getSequence();

        if ((sequence != null) && (sequence.getColorModel() != null))
        {
            final DataType dataType = sequence.getDataType_();
            final boolean byteRGBImage = (dataType == DataType.UBYTE)
                    && ((sequence.getSizeC() == 3) || (sequence.getSizeC() == 4));
            final boolean indexedImage = !sequence.getColorModel().hasLinearColormaps();

            // Do not use auto bounds on RGB or ARGB 8 bits image nor on indexed image.
            return !(byteRGBImage || indexedImage);
        }

        return true;
    }

    public boolean getAutoBounds()
    {
        return autoBoundsCheckBox.isSelected();
    }

    public void setAutoBound(boolean value)
    {
        autoBoundsCheckBox.setSelected(value);
    }

    public boolean getAutoRefreshHistogram()
    {
        return autoRefreshHistoCheckBox.isSelected();
    }

    public void setAutoRefreshHistogram(boolean value)
    {
        autoRefreshHistoCheckBox.setSelected(value);
    }

    void autoRefreshHistogramInternal(boolean value)
    {
        for (int i = 0; i < lutChannelViewers.size(); i++)
            lutChannelViewers.get(i).getScalerPanel().getScalerViewer().setAutoRefresh(value);
    }

    void setLogScale(boolean value)
    {
        for (int i = 0; i < lutChannelViewers.size(); i++)
            lutChannelViewers.get(i).getScalerPanel().getScalerViewer().setLogScale(value);
    }

    void refreshBounds()
    {
        final Sequence sequence = getSequence();

        if (sequence != null)
        {
            double[][] typeBounds = sequence.getChannelsTypeBounds();
            double[][] bounds = sequence.getChannelsBounds();

            for (int i = 0; i < Math.min(lutChannelViewers.size(), typeBounds.length); i++)
            {
                double[] tb = typeBounds[i];
                double[] b = bounds[i];

                final Scaler scaler = lutChannelViewers.get(i).getScalerPanel().getScaler();

                scaler.setAbsLeftRightIn(tb[0], tb[1]);
                scaler.setLeftRightIn(b[0], b[1]);
            }
        }
    }

    void refreshChannelsName()
    {
        final Sequence sequence = getSequence();

        if (sequence != null)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
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
            });
        }
    }
}

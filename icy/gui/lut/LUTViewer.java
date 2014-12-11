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
package icy.gui.lut;

import icy.gui.component.CheckTabbedPane;
import icy.gui.lut.abstract_.IcyLutViewer;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.IcyColorMapEvent;
import icy.image.colormap.IcyColorMapListener;
import icy.image.colormap.LinearColorMap;
import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;
import icy.math.Scaler;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LUTViewer extends IcyLutViewer implements IcyColorMapListener, SequenceListener
{
    private static final long serialVersionUID = 8385018166371243663L;

    /**
     * pref id
     */
    private static final String PREF_ID_HISTO = "gui.histo";

    private static final String ID_AUTO_REFRESH = "autoRefresh";
    private static final String ID_AUTO_BOUNDS = "autoBounds";
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
    final List<LUTChannelViewer> lutChannelViewers;

    /**
     * preferences
     */
    final XMLPreferences pref;

    final Runnable boundsUpdater;
    final Runnable channelNameUpdater;
    final Runnable channelEnableUpdater;
    final Runnable channelTabColorUpdater;

    public LUTViewer(Viewer viewer, LUT lut)
    {
        super(viewer, lut);

        pref = ApplicationPreferences.getPreferences().node(PREF_ID_HISTO);

        boundsUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                final Sequence sequence = getSequence();

                if (sequence != null)
                {
                    double[][] typeBounds = sequence.getChannelsTypeBounds();
                    double[][] bounds = sequence.getChannelsBounds();

                    for (int i = 0; i < Math.min(getLut().getNumChannel(), typeBounds.length); i++)
                    {
                        double[] tb = typeBounds[i];
                        double[] b = bounds[i];

                        final Scaler scaler = getLut().getLutChannel(i).getScaler();

                        scaler.setAbsLeftRightIn(tb[0], tb[1]);
                        scaler.setLeftRightIn(b[0], b[1]);
                    }
                }
            }
        };
        channelEnableUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                for (int c = 0; c < Math.min(getLut().getNumChannel(), bottomPane.getTabCount()); c++)
                    bottomPane.setTabChecked(c, getLut().getLutChannel(c).isEnabled());
            }
        };
        channelTabColorUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                for (int c = 0; c < Math.min(getLut().getNumChannel(), bottomPane.getTabCount()); c++)
                {
                    final IcyColorMap colormap = getLut().getLutChannel(c).getColorMap();
                    bottomPane.setBackgroundAt(c, colormap.getDominantColor());
                }
            }
        };
        channelNameUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                final Sequence sequence = getSequence();

                if (sequence != null)
                {
                    // need to be done on EDT
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (int c = 0; c < Math.min(sequence.getSizeC(), bottomPane.getTabCount()); c++)
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
        };

        lutChannelViewers = new ArrayList<LUTChannelViewer>();

        // build GUI
        bottomPane = new CheckTabbedPane(SwingConstants.BOTTOM, true);
        bottomPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // add tab for each channel
        for (int c = 0; c < lut.getNumChannel(); c++)
        {
            final LUTChannel lutChannel = lut.getLutChannel(c);
            final LUTChannelViewer lbv = new LUTChannelViewer(viewer, lutChannel);

            lutChannel.getColorMap().addListener(this);

            lutChannelViewers.add(lbv);
            bottomPane.addTab("ch " + c, lbv);
        }

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

                    lutChannelViewers.get(i).getLutChannel().setEnabled(b);
                }
            }
        });

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
                    ThreadUtil.runSingle(boundsUpdater);
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

                pref.putBoolean(ID_AUTO_BOUNDS, value);
            }
        });

        final Sequence seq = getSequence();
        final boolean userLut = (seq != null) ? seq.hasUserLUT() : false;

        if (!userLut && autoBoundsCheckBox.isSelected())
        {
            ThreadUtil.runSingle(boundsUpdater);
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

        // update channel name and color
        channelTabColorUpdater.run();
        channelNameUpdater.run();

        if (seq != null)
            seq.addListener(this);
    }

    private boolean getPreferredAutoBounds()
    {
        boolean result = pref.getBoolean(ID_AUTO_BOUNDS, true);

        if (!result)
            return false;

        final Sequence sequence = getSequence();

        if (sequence != null)
        {
            // byte data type ?
            if (sequence.getDataType_() == DataType.UBYTE)
            {
                final int numChannel = getLut().getNumChannel();

                // custom colormaps --> cannot use auto bounds
                for (int c = 0; c < numChannel; c++)
                    if (!getLut().getLutChannel(c).getColorMap().isLinear())
                        return false;

                if ((numChannel == 3) || (numChannel == 4))
                {
                    boolean rgb;

                    // check if we have classic RGB
                    rgb = getLut().getLutChannel(0).getColorMap().equals(LinearColorMap.red_)
                            && getLut().getLutChannel(1).getColorMap().equals(LinearColorMap.green_)
                            && getLut().getLutChannel(2).getColorMap().equals(LinearColorMap.blue_);

                    // ARGB
                    if (numChannel == 4)
                        rgb &= (getLut().getLutChannel(3).getColorMap().getType() == IcyColorMapType.ALPHA);

                    // do not use auto bounds for classic (A)RGB images
                    if (rgb)
                        return false;
                }
            }
        }

        return true;
    }

    @Override
    public Sequence getSequence()
    {
        return super.getSequence();
    }

    @Override
    public LUT getLut()
    {
        return super.getLut();
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

    @Override
    public void colorMapChanged(IcyColorMapEvent e)
    {
        switch (e.getType())
        {
            case ENABLED_CHANGED:
                ThreadUtil.runSingle(channelEnableUpdater);
                break;

            case MAP_CHANGED:
                ThreadUtil.runSingle(channelTabColorUpdater);
                break;

            case TYPE_CHANGED:
                break;
        }
    }

    public void dispose()
    {
        removeAll();

        Sequence seq = getSequence();
        if (seq != null)
            seq.removeListener(this);
    }

    @Override
    public void sequenceChanged(SequenceEvent sequenceEvent)
    {
        final SequenceEvent e = sequenceEvent;

        switch (e.getSourceType())
        {
            case SEQUENCE_META:
                ThreadUtil.runSingle(channelNameUpdater);
                break;

            case SEQUENCE_COMPONENTBOUNDS:
                if (autoBoundsCheckBox.isSelected())
                    ThreadUtil.runSingle(boundsUpdater);
                break;
        }
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {

    }
}

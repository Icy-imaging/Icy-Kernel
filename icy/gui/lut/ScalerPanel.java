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

import icy.gui.lut.abstract_.IcyScalerPanel;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.gui.viewer.ViewerListener;
import icy.image.lut.LUTBand;
import icy.image.lut.LUTBandEvent;
import icy.image.lut.LUTBandEvent.LUTBandEventType;
import icy.image.lut.LUTBandListener;
import icy.math.Scaler;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceListener;

import java.awt.BorderLayout;

/**
 * @author stephane
 */
public class ScalerPanel extends IcyScalerPanel implements SequenceListener, LUTBandListener, ViewerListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7681106081280637308L;

    /**
     * gui
     */
    final ScalerViewer scalerViewer;

    /**
     * 
     */
    public ScalerPanel(Viewer viewer, LUTBand lutBand)
    {
        super(viewer, lutBand);

        setLayout(new BorderLayout());

        scalerViewer = new ScalerViewer(viewer, lutBand);

        add(scalerViewer, BorderLayout.CENTER);

        validate();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        // add listeners
        final Sequence sequence = viewer.getSequence();

        if (sequence != null)
            sequence.addListener(this);
        viewer.addListener(this);
        lutBand.addListener(this);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();

        final Sequence sequence = viewer.getSequence();

        // remove listeners
        lutBand.removeListener(this);
        viewer.removeListener(this);
        if (sequence != null)
            sequence.removeListener(this);
    }

    /**
     * @return the scalerViewer
     */
    public ScalerViewer getScalerViewer()
    {
        return scalerViewer;
    }

    public void refreshHistoData()
    {
        // update histogram
        scalerViewer.refreshHistoData();
    }

    /**
     * process on sequence change
     */
    void onSequenceDataChanged()
    {
        // update histogram
        refreshHistoData();
    }

    /**
     * process on scaler change
     */
    private void onScalerChanged()
    {
        // process on scaler change

    }

    /**
     * process on position changed
     */
    private void onPositionChanged()
    {
        // update histogram
        refreshHistoData();
    }

    /**
     * 
     */
    public Scaler getScaler()
    {
        return lutBand.getScaler();
    }

    @Override
    public void sequenceChanged(SequenceEvent sequenceEvent)
    {
        if (sequenceEvent.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA)
            onSequenceDataChanged();
    }

    @Override
    public void lutBandChanged(LUTBandEvent event)
    {
        if (event.getType() == LUTBandEventType.SCALER_CHANGED)
            onScalerChanged();
    }

    @Override
    public void viewerChanged(ViewerEvent event)
    {
        if (event.getType() == ViewerEventType.POSITION_CHANGED)
            onPositionChanged();
    }

    @Override
    public void viewerClosed(Viewer viewer)
    {
        viewer.removeListener(this);
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        sequence.removeListener(this);
    }
}

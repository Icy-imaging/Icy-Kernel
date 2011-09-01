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
import icy.type.collection.array.Array1DUtil;

import java.awt.BorderLayout;

/**
 * @author stephane
 */
public class ScalerPanel extends IcyScalerPanel implements SequenceListener, LUTBandListener, SamplesProducer,
        ViewerListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7681106081280637308L;

    /**
     * cached
     */
    final Sequence sequence;
    private int component;

    /**
     * gui
     */
    final ScalerViewer scalerViewer;

    /**
     * internals
     */
    private Object sampleData;
    private int samplePosT;
    private int samplePosZ;
    private int samplePosXY;
    private int maxSamplePosT;
    private int maxSamplePosZ;
    private int maxSamplePosXY;
    private int sampleDataType;
    private boolean sampleSignedType;
    private boolean noMoreSample;

    /**
     * 
     */
    public ScalerPanel(Viewer viewer, LUTBand lutBand)
    {
        super(viewer, lutBand);

        sequence = viewer.getSequence();
        component = lutBand.getComponent();

        resetSamplePos();

        setLayout(new BorderLayout());

        scalerViewer = new ScalerViewer(this, lutBand);

        add(scalerViewer, BorderLayout.CENTER);

        validate();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        // add listeners
        sequence.addListener(this);
        viewer.addListener(this);
        lutBand.addListener(this);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();

        // remove listeners
        lutBand.removeListener(this);
        viewer.removeListener(this);
        sequence.removeListener(this);
    }

    /**
     * @return the scalerViewer
     */
    public ScalerViewer getScalerViewer()
    {
        return scalerViewer;
    }

    private void resetSamplePos()
    {
        if ((sequence == null) || (viewer == null))
        {
            noMoreSample = true;
            return;
        }

        final int posT = viewer.getT();
        final int posZ = viewer.getZ();

        if (posT != -1)
        {
            samplePosT = posT;
            maxSamplePosT = posT;
        }
        else
        {
            samplePosT = 0;
            maxSamplePosT = sequence.getSizeT() - 1;
        }

        if (posZ != -1)
        {
            samplePosZ = posZ;
            maxSamplePosZ = posZ;
        }
        else
        {
            samplePosZ = 0;
            maxSamplePosZ = sequence.getSizeZ() - 1;
        }

        samplePosXY = 0;
        maxSamplePosXY = (sequence.getSizeX() * sequence.getSizeY()) - 1;

        sampleDataType = sequence.getDataType();
        sampleSignedType = sequence.isSignedDataType();
        sampleData = sequence.getDataXY(samplePosT, samplePosZ, component);

        noMoreSample = (samplePosXY > maxSamplePosXY) || (samplePosZ > maxSamplePosZ) || (samplePosT > maxSamplePosT);
    }

    private void incSamplePos()
    {
        if (++samplePosXY > maxSamplePosXY)
        {
            samplePosXY = 0;
            if (++samplePosZ > maxSamplePosZ)
            {
                samplePosZ = 0;
                if (++samplePosT > maxSamplePosT)
                    noMoreSample = true;
            }

            // udpate sample data
            sampleData = sequence.getDataXY(samplePosT, samplePosZ, component);
        }
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
        scalerViewer.refreshHistoData();
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
    public void requestSamples()
    {
        resetSamplePos();
    }

    @Override
    public double nextSample()
    {
        final double result;

        if (sampleData != null)
            result = Array1DUtil.getValue(sampleData, samplePosXY, sampleDataType, sampleSignedType);
        else
            result = 0d;

        incSamplePos();

        return result;
    }

    @Override
    public boolean hasNextSample()
    {
        return !noMoreSample;
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

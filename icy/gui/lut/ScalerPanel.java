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

import icy.gui.viewer.Viewer;
import icy.image.lut.LUT.LUTChannel;
import icy.math.Scaler;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * @author stephane
 */
public class ScalerPanel extends JPanel
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
     * associated Viewer & LUTBand
     */
    protected final Viewer viewer;
    protected final LUTChannel lutChannel;

    /**
     * 
     */
    public ScalerPanel(Viewer viewer, LUTChannel lutChannel)
    {
        super();

        this.viewer = viewer;
        this.lutChannel = lutChannel;

        setLayout(new BorderLayout());

        scalerViewer = new ScalerViewer(viewer, lutChannel);

        add(scalerViewer, BorderLayout.CENTER);

        validate();
    }

    /**
     * @return the scalerViewer
     */
    public ScalerViewer getScalerViewer()
    {
        return scalerViewer;
    }

    /**
     * @deprecated Use {@link #refreshHistogram()} instead.
     */
    @Deprecated
    public void refreshHistoData()
    {
        refreshHistogram();
    }

    public void refreshHistogram()
    {
        // update histogram
        scalerViewer.requestHistoDataRefresh();
    }

    /**
     * 
     */
    public Scaler getScaler()
    {
        return lutChannel.getScaler();
    }

}

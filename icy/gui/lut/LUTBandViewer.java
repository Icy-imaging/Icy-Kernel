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

import icy.gui.lut.ColormapViewer.ColormapPositionListener;
import icy.gui.lut.ScalerViewer.ScalerPositionListener;
import icy.gui.lut.abstract_.IcyLutBandViewer;
import icy.gui.util.ComponentUtil;
import icy.gui.viewer.Viewer;
import icy.image.lut.LUTBand;
import icy.math.MathUtil;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class LUTBandViewer extends IcyLutBandViewer
{
    private static final long serialVersionUID = 8709589851405274816L;

    final JLabel indexLabel;
    final JLabel valueLabel;

    final ScalerPanel scalerPanel;
    final ColormapPanel colormapPanel;

    public LUTBandViewer(final Viewer viewer, final LUTBand lutBand)
    {
        super(viewer, lutBand);

        // scaler
        scalerPanel = new ScalerPanel(viewer, lutBand);
        // refresh info on scaler mouse position change
        scalerPanel.getScalerViewer().addScalerPositionListener(new ScalerPositionListener()
        {
            @Override
            public void positionChanged(double index, double value)
            {
                if (index != -1d)
                {
                    if (lutBand.getScaler().isIntegerData())
                        indexLabel.setText(Integer.toString((int) Math.round(index)));
                    else
                        indexLabel.setText(Double.toString(MathUtil.round(index, 2)));
                }
                else
                    indexLabel.setText("");

                if (value != -1d)
                    valueLabel.setText(Double.toString(MathUtil.round(value, 2)));
                else
                    valueLabel.setText("");
            }
        });

        // colormap
        colormapPanel = new ColormapPanel(viewer, lutBand);
        // refresh info on colormap mouse position change
        colormapPanel.getColormapViewer().addColormapPositionListener(new ColormapPositionListener()
        {
            @Override
            public void positionChanged(int index, int value)
            {
                if (index != -1)
                    indexLabel.setText(Integer.toString(index));
                else
                    indexLabel.setText("");

                if (value != -1)
                    valueLabel.setText(Integer.toString(value));
                else
                    valueLabel.setText("");
            }
        });

        // intensity / value
        final JPanel lutInfoPanel = new JPanel();
        lutInfoPanel.setLayout(new BoxLayout(lutInfoPanel, BoxLayout.LINE_AXIS));

        indexLabel = new JLabel("");
        indexLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        ComponentUtil.setFixedWidth(indexLabel, 80);
        valueLabel = new JLabel("");
        valueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        ComponentUtil.setFixedWidth(valueLabel, 32);

        lutInfoPanel.add(new JLabel("index :"));
        lutInfoPanel.add(indexLabel);
        lutInfoPanel.add(Box.createHorizontalStrut(10));
        lutInfoPanel.add(new JLabel("value :"));
        lutInfoPanel.add(valueLabel);
        lutInfoPanel.add(Box.createHorizontalGlue());
        lutInfoPanel.validate();

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(scalerPanel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(colormapPanel);

        setLayout(new BorderLayout());

        add(mainPanel, BorderLayout.CENTER);
        add(lutInfoPanel, BorderLayout.SOUTH);

        validate();
    }

    /**
     * @return the scalerPanel
     */
    public ScalerPanel getScalerPanel()
    {
        return scalerPanel;
    }

    /**
     * @return the colormapPanel
     */
    public ColormapPanel getColormapPanel()
    {
        return colormapPanel;
    }

}

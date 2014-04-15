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
package icy.gui.sequence;

import icy.gui.dialog.ActionDialog;
import icy.gui.dialog.MessageDialog;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.sequence.Sequence;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stephane
 */
public class SequencePropertiesDialog extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 5696186054980120411L;

    SequencePropertiesPanel panel;

    public SequencePropertiesDialog(final Sequence sequence)
    {
        super("Sequence Properties");

        initialize();

        panel.setSequence(sequence);
        // don't close automatically
        setCloseAfterAction(false);

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final double sx, sy, sz, st;

                sx = panel.getPixelSizeXFieldValue();
                sy = panel.getPixelSizeYFieldValue();
                sz = panel.getPixelSizeZFieldValue();
                st = panel.getTimeIntervalFieldValue();

                if ((sx <= 0d) || (sy <= 0d) || (sz <= 0d))
                {
                    MessageDialog.showDialog("Pixel size values should be > 0 !", MessageDialog.WARNING_MESSAGE);
                    return;
                }
                if (st <= 0d)
                {
                    MessageDialog.showDialog("Timer interval should be > 0 !", MessageDialog.WARNING_MESSAGE);
                    return;
                }

                sequence.setName(panel.getNameFieldValue());
                sequence.setPixelSizeX(UnitUtil.getValueInUnit(sx, panel.getPixelSizeXUnit(), UnitPrefix.MICRO));
                sequence.setPixelSizeY(UnitUtil.getValueInUnit(sy, panel.getPixelSizeYUnit(), UnitPrefix.MICRO));
                sequence.setPixelSizeZ(UnitUtil.getValueInUnit(sz, panel.getPixelSizeZUnit(), UnitPrefix.MICRO));

                double valueInSec = st;

                switch (panel.getTimeIntervalUnit())
                {
                    case 0:
                        valueInSec *= 60d;
                    case 1:
                        valueInSec *= 60d;
                        break;
                    case 3:
                        valueInSec /= 1000d;
                        break;
                }

                sequence.setTimeInterval(valueInSec);

                for (int c = 0; c < sequence.getSizeC(); c++)
                    sequence.setChannelName(c, panel.getChannelNameFieldValue(c));

                dispose();
            }
        });

        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        setVisible(true);
    }

    private void initialize()
    {
        panel = new SequencePropertiesPanel();

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.validate();
    }
}

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

    final SequencePropertiesPanel panel;

    public SequencePropertiesDialog(final Sequence sequence)
    {
        super("Sequence Properties");

        panel = new SequencePropertiesPanel();
        panel.setSequence(sequence);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.validate();

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sequence.setName(panel.getNameFieldValue());
                sequence.setPixelSizeX(UnitUtil.getValueInUnit(panel.getPixelSizeXFieldValue(),
                        panel.getPixelSizeXUnit(), UnitPrefix.MICRO));
                sequence.setPixelSizeY(UnitUtil.getValueInUnit(panel.getPixelSizeYFieldValue(),
                        panel.getPixelSizeYUnit(), UnitPrefix.MICRO));
                sequence.setPixelSizeZ(UnitUtil.getValueInUnit(panel.getPixelSizeZFieldValue(),
                        panel.getPixelSizeZUnit(), UnitPrefix.MICRO));

                double valueInSec = panel.getTimeIntervalFieldValue();

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
            }
        });

        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        pack();

        setVisible(true);
    }
}

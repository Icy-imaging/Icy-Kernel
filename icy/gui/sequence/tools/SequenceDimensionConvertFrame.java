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
package icy.gui.sequence.tools;

import icy.gui.dialog.ActionDialog;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.ComponentUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

/**
 * Advanced conversion of Z and T dimension.
 * 
 * @author Stephane
 */
public class SequenceDimensionConvertFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -6541431462734831647L;

    final SequenceDimensionConvertPanel convertPanel;

    public SequenceDimensionConvertFrame(Sequence sequence)
    {
        super("Z / T dimension conversion");

        convertPanel = new SequenceDimensionConvertPanel(sequence);
        convertPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(convertPanel, BorderLayout.CENTER);
        validate();

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Converting Z / T dimension...");

                        SequenceUtil.adjustZT(convertPanel.getSequence(), convertPanel.getNewSizeZ(),
                                convertPanel.getNewSizeT(), convertPanel.isOrderReversed());

                        pf.close();
                    }
                });
            }
        });

        setSize(340, 400);
        ComponentUtil.center(this);

        setVisible(true);
    }
}
/*
 * Copyright 2010-2015 Institut Pasteur.
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
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stephane
 */
public class SequenceCanvasResizeFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -430346980166539623L;

    final SequenceCanvasResizePanel resizePanel;

    public SequenceCanvasResizeFrame(Sequence sequence)
    {
        super("Canvas size");

        resizePanel = new SequenceCanvasResizePanel(sequence);
        getMainPanel().add(resizePanel, BorderLayout.CENTER);
        validate();

        // action
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Resizing sequence...");
                        try
                        {
                            Icy.getMainInterface().addSequence(
                                    SequenceUtil.scale(resizePanel.getSequence(), resizePanel.getNewWidth(),
                                            resizePanel.getNewHeight(), resizePanel.getResizeContent(),
                                            resizePanel.getXAlign(), resizePanel.getYAlign(),
                                            resizePanel.getFilterType()));
                        }
                        finally
                        {
                            pf.close();
                        }
                    }
                });
            }
        });

        setSize(420, 520);
        ComponentUtil.center(this);

        setVisible(true);
    }
}
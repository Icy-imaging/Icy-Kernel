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
import icy.sequence.AbstractSequenceModel;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

/**
 * @author Stephane
 */
public class SequenceDimensionAdjustFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -383226926743211242L;

    private class SequenceDimensionAdjustFrameModel extends AbstractSequenceModel
    {
        public SequenceDimensionAdjustFrameModel()
        {
            super();
        }

        @Override
        public int getSizeX()
        {
            if (sequence != null)
                return sequence.getSizeX();

            return 0;
        }

        @Override
        public int getSizeY()
        {
            if (sequence != null)
                return sequence.getSizeY();

            return 0;
        }

        @Override
        public int getSizeZ()
        {
            if (sequence != null)
                return sequence.getSizeZ();

            return 0;
        }

        @Override
        public int getSizeT()
        {
            if (sequence != null)
                return sequence.getSizeT();

            return 0;
        }

        @Override
        public int getSizeC()
        {
            if (sequence != null)
                return sequence.getSizeC();

            return 0;
        }

        @Override
        public Image getImage(int t, int z)
        {
            if (sequence != null)
                return sequence.getImage(t, z);

            return null;
        }

        @Override
        public Image getImage(int t, int z, int c)
        {
            if (sequence != null)
                return sequence.getImage(t, z, c);

            return null;
        }
    }

    final Sequence sequence;
    final SequenceDimensionAdjustPanel rangePanel;
    final DimensionId dim;

    public SequenceDimensionAdjustFrame(Sequence sequence, DimensionId dim)
    {
        super("Adjust " + dim.toString() + " dimension");

        this.sequence = sequence;
        this.dim = dim;

        rangePanel = new SequenceDimensionAdjustPanel(dim);
        rangePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(rangePanel, BorderLayout.CENTER);
        validate();

        rangePanel.setModel(new SequenceDimensionAdjustFrameModel());

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
                        final DimensionId dim = SequenceDimensionAdjustFrame.this.dim;
                        final ProgressFrame pf;

                        if (dim == DimensionId.Z)
                            pf = new ProgressFrame("Removing slices...");
                        else
                            pf = new ProgressFrame("Removing frames...");

                        final Sequence seq = SequenceDimensionAdjustFrame.this.sequence;

                        seq.beginUpdate();
                        try
                        {
                            int i;

                            if (dim == DimensionId.Z)
                            {
                                i = seq.getSizeZ() - 1;

                                for (; i > rangePanel.getRangeHigh(); i--)
                                    SequenceUtil.removeZAndShift(seq, i);
                                for (; i >= rangePanel.getRangeLow(); i--)
                                    if (!rangePanel.isIndexSelected(i))
                                        SequenceUtil.removeZAndShift(seq, i);
                                for (; i >= 0; i--)
                                    SequenceUtil.removeZAndShift(seq, i);
                            }
                            else
                            {
                                i = seq.getSizeT() - 1;

                                for (; i > rangePanel.getRangeHigh(); i--)
                                    SequenceUtil.removeTAndShift(seq, i);
                                for (; i >= rangePanel.getRangeLow(); i--)
                                    if (!rangePanel.isIndexSelected(i))
                                        SequenceUtil.removeTAndShift(seq, i);
                                for (; i >= 0; i--)
                                    SequenceUtil.removeTAndShift(seq, i);
                            }
                        }
                        finally
                        {
                            seq.endUpdate();
                            pf.close();
                        }
                    }
                });
            }
        });

        setSize(320, 360);
        ComponentUtil.center(this);

        setVisible(true);
    }

    /**
     * @wbp.parser.constructor
     */
    SequenceDimensionAdjustFrame()
    {
        this(new Sequence(), DimensionId.Z);
    }
}

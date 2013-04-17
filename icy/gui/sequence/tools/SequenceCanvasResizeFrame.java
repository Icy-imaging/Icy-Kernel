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
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

/**
 * @author Stephane
 */
public class SequenceCanvasResizeFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -430346980166539623L;

    private class SequenceCanvasResizePanel extends SequenceBaseResizePanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7929109041552115932L;

        private JLabel lblNewLabel_1;
        private PositionAlignmentPanel positionAlignmentPanel;

        public SequenceCanvasResizePanel(Sequence sequence)
        {
            super(sequence);

            keepRatioCheckBox.setSelected(false);

            positionAlignmentPanel.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    updatePreview();
                }
            });
        }

        @Override
        protected void initialize()
        {
            super.initialize();

            lblNewLabel_1 = new JLabel("Content alignment");
            GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
            gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
            gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
            gbc_lblNewLabel_1.gridx = 5;
            gbc_lblNewLabel_1.gridy = 2;
            settingPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

            positionAlignmentPanel = new PositionAlignmentPanel();
            GridBagConstraints gbc_positionAlignmentPanel = new GridBagConstraints();
            gbc_positionAlignmentPanel.gridheight = 4;
            gbc_positionAlignmentPanel.insets = new Insets(0, 0, 5, 5);
            gbc_positionAlignmentPanel.fill = GridBagConstraints.BOTH;
            gbc_positionAlignmentPanel.gridx = 5;
            gbc_positionAlignmentPanel.gridy = 3;
            settingPanel.add(positionAlignmentPanel, gbc_positionAlignmentPanel);
        }

        @Override
        public FilterType getFilterType()
        {
            return null;
        }

        @Override
        public boolean getResizeContent()
        {
            return false;
        }

        @Override
        public int getXAlign()
        {
            return positionAlignmentPanel.getXAlign();
        }

        @Override
        public int getYAlign()
        {
            return positionAlignmentPanel.getYAlign();
        }
    }

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
                            final Sequence seqIn = resizePanel.getSequence();
                            final double pixelSize = resizePanel.getResolution();

                            // apply chosen pixel size
                            seqIn.setPixelSizeX(pixelSize);
                            seqIn.setPixelSizeY(pixelSize);

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
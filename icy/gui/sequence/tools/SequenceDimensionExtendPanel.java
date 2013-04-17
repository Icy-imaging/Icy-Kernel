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

import icy.gui.component.sequence.SequencePreviewPanel;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class SequenceDimensionExtendPanel extends JPanel
{
    static class SequenceChannelEntry
    {
        final Sequence sequence;
        final int c;

        /**
         * @param sequence
         * @param c
         */
        public SequenceChannelEntry(Sequence sequence, int c)
        {
            super();

            this.sequence = sequence;
            this.c = c;
        }

        public SequenceChannelEntry(Sequence sequence)
        {
            this(sequence, -1);
        }

        @Override
        public String toString()
        {
            if (c == -1)
                return sequence.toString();

            return sequence.toString() + "    [channel " + c + "]";
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5908902915282090447L;
    protected SequencePreviewPanel sequencePreview;
    private JLabel lblInsertPosition;
    JSpinner insertPositionSpinner;
    private JLabel lblNewZSize;
    private JSpinner newSizeSpinner;
    private JLabel lblImages;
    JSpinner duplicateSpinner;
    JCheckBox duplicateCheckBox;

    // internals
    protected final DimensionId dim;

    /**
     * Create the panel.
     */
    public SequenceDimensionExtendPanel(DimensionId dim)
    {
        super();

        if ((dim != DimensionId.Z) && (dim != DimensionId.T))
            throw new IllegalArgumentException("Only Z or T dimension allowed");

        this.dim = dim;

        initialize();

        newSizeSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                previewDimensionChanged();
                fireChangedEvent();
            }
        });

        final ChangeListener changeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                previewImageChanged();
                fireChangedEvent();
            }
        };

        insertPositionSpinner.addChangeListener(changeListener);
        duplicateSpinner.addChangeListener(changeListener);

        duplicateCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                duplicateSpinner.setEnabled(duplicateCheckBox.isSelected());

                previewImageChanged();
                fireChangedEvent();
            }
        });

        refreshButtonsState();
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {20, 0, 20, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 174, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        lblNewZSize = new JLabel("New size");
        lblNewZSize.setToolTipText("New dimension size");
        lblNewZSize.setHorizontalAlignment(SwingConstants.TRAILING);
        GridBagConstraints gbc_lblNewZSize = new GridBagConstraints();
        gbc_lblNewZSize.fill = GridBagConstraints.BOTH;
        gbc_lblNewZSize.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewZSize.gridx = 1;
        gbc_lblNewZSize.gridy = 0;
        add(lblNewZSize, gbc_lblNewZSize);

        newSizeSpinner = new JSpinner();
        newSizeSpinner.setToolTipText("Enter new size");
        newSizeSpinner.setModel(new SpinnerNumberModel(0, 0, 99999, 1));
        GridBagConstraints gbc_newSizeSpinner = new GridBagConstraints();
        gbc_newSizeSpinner.fill = GridBagConstraints.BOTH;
        gbc_newSizeSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_newSizeSpinner.gridx = 3;
        gbc_newSizeSpinner.gridy = 0;
        add(newSizeSpinner, gbc_newSizeSpinner);

        lblInsertPosition = new JLabel("Insert position");
        lblInsertPosition.setToolTipText("Index where to add the new image(s)");
        lblInsertPosition.setHorizontalAlignment(SwingConstants.TRAILING);
        GridBagConstraints gbc_lblInsertPosition = new GridBagConstraints();
        gbc_lblInsertPosition.fill = GridBagConstraints.BOTH;
        gbc_lblInsertPosition.insets = new Insets(0, 0, 5, 5);
        gbc_lblInsertPosition.gridx = 1;
        gbc_lblInsertPosition.gridy = 1;
        add(lblInsertPosition, gbc_lblInsertPosition);

        insertPositionSpinner = new JSpinner();
        insertPositionSpinner.setToolTipText("Index where to add the new image(s)");
        insertPositionSpinner.setModel(new SpinnerNumberModel(0, 0, 99999, 1));
        GridBagConstraints gbc_insertPositionSpinner = new GridBagConstraints();
        gbc_insertPositionSpinner.fill = GridBagConstraints.BOTH;
        gbc_insertPositionSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_insertPositionSpinner.gridx = 3;
        gbc_insertPositionSpinner.gridy = 1;
        add(insertPositionSpinner, gbc_insertPositionSpinner);

        duplicateCheckBox = new JCheckBox("Duplicate last");
        duplicateCheckBox.setToolTipText("Duplicate last images in new images");
        GridBagConstraints gbc_duplicateCheckBox = new GridBagConstraints();
        gbc_duplicateCheckBox.fill = GridBagConstraints.BOTH;
        gbc_duplicateCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_duplicateCheckBox.gridx = 1;
        gbc_duplicateCheckBox.gridy = 2;
        add(duplicateCheckBox, gbc_duplicateCheckBox);

        duplicateSpinner = new JSpinner();
        duplicateSpinner.setToolTipText("Number of last images to duplicate");
        duplicateSpinner.setEnabled(false);
        duplicateSpinner.setModel(new SpinnerNumberModel(1, 1, 99999, 1));
        GridBagConstraints gbc_duplicateSpinner = new GridBagConstraints();
        gbc_duplicateSpinner.fill = GridBagConstraints.BOTH;
        gbc_duplicateSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_duplicateSpinner.gridx = 3;
        gbc_duplicateSpinner.gridy = 2;
        add(duplicateSpinner, gbc_duplicateSpinner);

        lblImages = new JLabel("image(s)");
        GridBagConstraints gbc_lblImages = new GridBagConstraints();
        gbc_lblImages.gridwidth = 2;
        gbc_lblImages.fill = GridBagConstraints.BOTH;
        gbc_lblImages.insets = new Insets(0, 0, 5, 0);
        gbc_lblImages.gridx = 4;
        gbc_lblImages.gridy = 2;
        add(lblImages, gbc_lblImages);

        sequencePreview = new SequencePreviewPanel();
        sequencePreview
                .setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_sequencePreview = new GridBagConstraints();
        gbc_sequencePreview.gridwidth = 6;
        gbc_sequencePreview.fill = GridBagConstraints.BOTH;
        gbc_sequencePreview.gridx = 0;
        gbc_sequencePreview.gridy = 3;
        add(sequencePreview, gbc_sequencePreview);
    }

    public DimensionId getDimensionId()
    {
        return dim;
    }

    void refreshButtonsState()
    {
    }

    /**
     * @return the image provider
     */
    public SequenceModel getModel()
    {
        return sequencePreview.getModel();
    }

    public void setModel(SequenceModel model)
    {
        sequencePreview.setModel(model);
    }

    public void previewDimensionChanged()
    {
        sequencePreview.dimensionChanged();
    }

    public void previewImageChanged()
    {
        sequencePreview.imageChanged();
    }

    public int getNewSize()
    {
        return ((Integer) newSizeSpinner.getValue()).intValue();
    }

    public int getInsertPosition()
    {
        return ((Integer) insertPositionSpinner.getValue()).intValue();
    }

    public int getDuplicateNumber()
    {
        if (!duplicateCheckBox.isSelected())
            return 0;

        return ((Integer) duplicateSpinner.getValue()).intValue();
    }

    public void setNewSize(int size)
    {
        newSizeSpinner.setModel(new SpinnerNumberModel(size, size, 99999, 1));
        insertPositionSpinner.setModel(new SpinnerNumberModel(size, 0, size, 1));
    }

    public void setInsertPosition(int position)
    {
        insertPositionSpinner.setValue(Integer.valueOf(position));
    }

    public void setMaxDuplicate(int max)
    {
        duplicateSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(max, 1), 1));
    }

    protected void fireChangedEvent()
    {
        final ChangeEvent event = new ChangeEvent(SequenceDimensionExtendPanel.this);

        for (ChangeListener listener : getListeners(ChangeListener.class))
            listener.stateChanged(event);
    }

    public void addChangeListener(ChangeListener listener)
    {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener)
    {
        listenerList.remove(ChangeListener.class, listener);
    }

}
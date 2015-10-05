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

import icy.gui.component.button.IcyButton;
import icy.gui.component.sequence.SequenceChooser;
import icy.gui.component.sequence.SequencePreviewPanel;
import icy.gui.dialog.MessageDialog;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Frame for dimension merge operation.
 * 
 * @author Stephane
 */
public class SequenceDimensionMergePanel extends JPanel
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

    // GUI
    protected IcyButton addButton;
    protected IcyButton removeButton;
    protected IcyButton upButton;
    protected IcyButton downButton;
    protected JList sequenceList;
    protected SequenceChooser sequenceChooser;
    protected SequencePreviewPanel sequencePreview;
    protected JCheckBox interlaceCheckBox;
    protected JCheckBox fillEmptyImageCheckBox;
    protected JCheckBox fitCheckbox;
    private JLabel bottomArrowLabel;
    private JLabel dimLabel;

    // internals
    protected DefaultListModel listModel;
    protected ListSelectionModel selectionModel;
    protected final DimensionId dim;

    /**
     * Create the panel.
     */
    public SequenceDimensionMergePanel(DimensionId dim)
    {
        super();

        this.dim = dim;

        listModel = new DefaultListModel();

        initialize();

        selectionModel = sequenceList.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                refreshButtonsState();
            }
        });

        interlaceCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fireChangedEvent();
                previewImageChanged();
            }
        });
        fillEmptyImageCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fireChangedEvent();
                previewImageChanged();
            }
        });
        fitCheckbox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fireChangedEvent();
                previewImageChanged();
            }
        });

        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence seq = sequenceChooser.getSelectedSequence();

                if (seq != null)
                {
                    if (checkSequenceIsCompatible(seq, true, true))
                    {
                        if (SequenceDimensionMergePanel.this.dim == DimensionId.C)
                        {
                            // add per channel
                            for (int c = 0; c < seq.getSizeC(); c++)
                                listModel.addElement(new SequenceChannelEntry(seq, c));
                        }
                        else
                            listModel.addElement(new SequenceChannelEntry(seq));

                        refreshButtonsState();
                        fireChangedEvent();
                        previewDimensionChanged();
                    }
                }
            }
        });
        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                listModel.remove(selectionModel.getMinSelectionIndex());

                refreshButtonsState();
                fireChangedEvent();
                previewDimensionChanged();
            }
        });
        upButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final int index = selectionModel.getMinSelectionIndex();

                // exchange index and (index - 1)
                final Object obj = listModel.getElementAt(index - 1);
                listModel.set(index - 1, listModel.getElementAt(index));
                listModel.set(index, obj);

                selectionModel.setSelectionInterval(index - 1, index - 1);

                refreshButtonsState();
                fireChangedEvent();
                previewImageChanged();
            }
        });
        downButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final int index = selectionModel.getMinSelectionIndex();

                // exchange index and (index + 1)
                final Object obj = listModel.getElementAt(index + 1);
                listModel.set(index + 1, listModel.getElementAt(index));
                listModel.set(index, obj);

                selectionModel.setSelectionInterval(index + 1, index + 1);

                refreshButtonsState();
                fireChangedEvent();
                previewImageChanged();
            }
        });

        dimLabel.setText(dim.toString());
        IcyIcon icon = new IcyIcon(ResourceUtil.ICON_ARROW_DOWN);
        icon.setDimension(new Dimension(20, 60));
        bottomArrowLabel.setIcon(icon);

        // interlace not available for channel merge operation
        interlaceCheckBox.setVisible(dim != DimensionId.C);
        fillEmptyImageCheckBox.setVisible(false);

        refreshButtonsState();
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {24, 160, 160, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 26, 0, 0, 0, 0, 0, 0, 174, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JLabel lblSelectSequenceTo = new JLabel("Add sequence to merge in the list :");
        GridBagConstraints gbc_lblSelectSequenceTo = new GridBagConstraints();
        gbc_lblSelectSequenceTo.fill = GridBagConstraints.BOTH;
        gbc_lblSelectSequenceTo.gridwidth = 3;
        gbc_lblSelectSequenceTo.insets = new Insets(0, 0, 5, 5);
        gbc_lblSelectSequenceTo.gridx = 0;
        gbc_lblSelectSequenceTo.gridy = 0;
        add(lblSelectSequenceTo, gbc_lblSelectSequenceTo);

        sequenceChooser = new SequenceChooser();
        GridBagConstraints gbc_sequenceChooser = new GridBagConstraints();
        gbc_sequenceChooser.gridwidth = 3;
        gbc_sequenceChooser.insets = new Insets(0, 0, 5, 5);
        gbc_sequenceChooser.fill = GridBagConstraints.BOTH;
        gbc_sequenceChooser.gridx = 0;
        gbc_sequenceChooser.gridy = 1;
        add(sequenceChooser, gbc_sequenceChooser);

        addButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROUND_PLUS));
        addButton.setToolTipText("Add selected sequence to the list.");
        addButton.setFlat(true);
        GridBagConstraints gbc_addButton = new GridBagConstraints();
        gbc_addButton.fill = GridBagConstraints.BOTH;
        gbc_addButton.insets = new Insets(0, 0, 5, 0);
        gbc_addButton.gridx = 3;
        gbc_addButton.gridy = 1;
        add(addButton, gbc_addButton);

        dimLabel = new JLabel("Z");
        dimLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dimLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        GridBagConstraints gbc_dimLabel = new GridBagConstraints();
        gbc_dimLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_dimLabel.anchor = GridBagConstraints.BASELINE;
        gbc_dimLabel.insets = new Insets(0, 0, 5, 5);
        gbc_dimLabel.gridx = 0;
        gbc_dimLabel.gridy = 2;
        add(dimLabel, gbc_dimLabel);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.anchor = GridBagConstraints.WEST;
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridheight = 4;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 2;
        add(scrollPane, gbc_scrollPane);

        sequenceList = new JList(listModel);
        scrollPane.setViewportView(sequenceList);
        sequenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        removeButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROUND_MINUS));
        removeButton.setToolTipText("Remove selected sequence from the list.");
        removeButton.setFlat(true);
        GridBagConstraints gbc_removeButton = new GridBagConstraints();
        gbc_removeButton.fill = GridBagConstraints.BOTH;
        gbc_removeButton.insets = new Insets(0, 0, 5, 0);
        gbc_removeButton.gridx = 3;
        gbc_removeButton.gridy = 2;
        add(removeButton, gbc_removeButton);

        bottomArrowLabel = new JLabel("");
        bottomArrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_bottomArrowLabel = new GridBagConstraints();
        gbc_bottomArrowLabel.gridheight = 3;
        gbc_bottomArrowLabel.insets = new Insets(0, 0, 5, 5);
        gbc_bottomArrowLabel.gridx = 0;
        gbc_bottomArrowLabel.gridy = 3;
        add(bottomArrowLabel, gbc_bottomArrowLabel);

        upButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROUND_ARROW_UP));
        upButton.setToolTipText("Move up selected sequence.");
        upButton.setFlat(true);
        GridBagConstraints gbc_upButton = new GridBagConstraints();
        gbc_upButton.fill = GridBagConstraints.BOTH;
        gbc_upButton.insets = new Insets(0, 0, 5, 0);
        gbc_upButton.gridx = 3;
        gbc_upButton.gridy = 3;
        add(upButton, gbc_upButton);

        downButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROUND_ARROW_DOWN));
        downButton.setToolTipText("Move down selected sequence.");
        downButton.setFlat(true);
        GridBagConstraints gbc_downButton = new GridBagConstraints();
        gbc_downButton.fill = GridBagConstraints.BOTH;
        gbc_downButton.insets = new Insets(0, 0, 5, 0);
        gbc_downButton.gridx = 3;
        gbc_downButton.gridy = 4;
        add(downButton, gbc_downButton);

        fitCheckbox = new JCheckBox("Scale image");
        fitCheckbox.setToolTipText("Scale all image to the largest one");
        GridBagConstraints gbc_fitCheckbox = new GridBagConstraints();
        gbc_fitCheckbox.fill = GridBagConstraints.HORIZONTAL;
        gbc_fitCheckbox.gridwidth = 2;
        gbc_fitCheckbox.insets = new Insets(0, 0, 5, 5);
        gbc_fitCheckbox.gridx = 0;
        gbc_fitCheckbox.gridy = 6;
        add(fitCheckbox, gbc_fitCheckbox);

        interlaceCheckBox = new JCheckBox("Interlace image");
        interlaceCheckBox.setToolTipText("Interlace sequence image");
        GridBagConstraints gbc_interlaceCheckBox = new GridBagConstraints();
        gbc_interlaceCheckBox.gridwidth = 2;
        gbc_interlaceCheckBox.fill = GridBagConstraints.BOTH;
        gbc_interlaceCheckBox.insets = new Insets(0, 0, 5, 0);
        gbc_interlaceCheckBox.gridx = 2;
        gbc_interlaceCheckBox.gridy = 6;
        add(interlaceCheckBox, gbc_interlaceCheckBox);

        fillEmptyImageCheckBox = new JCheckBox("Fill empty image");
        fillEmptyImageCheckBox.setToolTipText("Replace empty image by the previous non empty one");
        GridBagConstraints gbc_noEmptyImageCheckBox = new GridBagConstraints();
        gbc_noEmptyImageCheckBox.gridwidth = 2;
        gbc_noEmptyImageCheckBox.fill = GridBagConstraints.VERTICAL;
        gbc_noEmptyImageCheckBox.anchor = GridBagConstraints.WEST;
        gbc_noEmptyImageCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_noEmptyImageCheckBox.gridx = 0;
        gbc_noEmptyImageCheckBox.gridy = 7;
        add(fillEmptyImageCheckBox, gbc_noEmptyImageCheckBox);

        sequencePreview = new SequencePreviewPanel();
        sequencePreview
                .setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_sequencePreview = new GridBagConstraints();
        gbc_sequencePreview.gridwidth = 4;
        gbc_sequencePreview.insets = new Insets(0, 0, 0, 5);
        gbc_sequencePreview.fill = GridBagConstraints.BOTH;
        gbc_sequencePreview.gridx = 0;
        gbc_sequencePreview.gridy = 8;
        add(sequencePreview, gbc_sequencePreview);
    }

    public DimensionId getDimensionId()
    {
        return dim;
    }

    void refreshButtonsState()
    {
        final int index = selectionModel.getMinSelectionIndex();
        final boolean notEmpty = index != -1;
        final int size = listModel.getSize();

        removeButton.setEnabled(notEmpty);
        upButton.setEnabled(notEmpty && (index != 0));
        downButton.setEnabled(notEmpty && (index != (size - 1)));
    }

    public int[] getSelectedChannels()
    {
        final int[] result = new int[listModel.size()];

        for (int i = 0; i < listModel.getSize(); i++)
            result[i] = ((SequenceChannelEntry) listModel.get(i)).c;

        return result;
    }

    public Sequence[] getSequences()
    {
        final Sequence result[] = new Sequence[listModel.size()];

        for (int i = 0; i < listModel.getSize(); i++)
            result[i] = ((SequenceChannelEntry) listModel.get(i)).sequence;

        return result;
    }

    boolean checkSequenceIsCompatible(Sequence seq, boolean showMessage, boolean showWarning)
    {
        boolean warningXYDone = false;

        for (Sequence sequence : getSequences())
        {
            // first check for data type
            if (!seq.getDataType_().equals(sequence.getDataType_()))
            {
                if (showMessage)
                    MessageDialog.showDialog("You cannot merge sequences with different data type.");

                return false;
            }

            // then depending dimension merge check for dimension equality
            switch (getDimensionId())
            {
                case C:
                    if (seq.getSizeZ() != sequence.getSizeZ())
                    {
                        if (showMessage)
                            MessageDialog.showDialog("You cannot merge channels from sequences with different Z size.");

                        return false;
                    }
                    if (seq.getSizeT() != sequence.getSizeT())
                    {
                        if (showMessage)
                            MessageDialog.showDialog("You cannot merge channels from sequences with different T size.");

                        return false;
                    }
                    break;

                case Z:
                    if (seq.getSizeC() != sequence.getSizeC())
                    {
                        if (showMessage)
                            MessageDialog
                                    .showDialog("You cannot merge slices from sequences with different number of channel.");

                        return false;
                    }
                    if (seq.getSizeT() != sequence.getSizeT())
                    {
                        if (showMessage)
                            MessageDialog.showDialog("You cannot merge slices from sequences with different T size.");

                        return false;
                    }
                    break;

                case T:
                    if (seq.getSizeC() != sequence.getSizeC())
                    {
                        if (showMessage)
                            MessageDialog.showDialog(
                                    "You cannot merge frames from sequences with different number of channel.",
                                    MessageDialog.PLAIN_MESSAGE);

                        return false;
                    }
                    if (seq.getSizeZ() != sequence.getSizeZ())
                    {
                        if (showMessage)
                            MessageDialog.showDialog("You cannot merge frames from sequences with different Z size.");

                        return false;
                    }
                    break;
            }

            // also consider the XY size
            if (!isFitImagesEnabled())
            {
                if ((seq.getSizeX() != sequence.getSizeX()) || (seq.getSizeY() != sequence.getSizeY()))
                {
                    if (showWarning && !warningXYDone)
                    {
                        MessageDialog
                                .showDialog(
                                        "Sequences have different XY size !\nYou can enable the \"Scale image\" option to resize images if needed.",
                                        MessageDialog.WARNING_MESSAGE);
                        warningXYDone = true;
                    }
                }
            }
        }

        return true;
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

    public boolean isInterlaceEnabled()
    {
        return interlaceCheckBox.isVisible() && interlaceCheckBox.isSelected();
    }

    public boolean isFillEmptyImageEnabled()
    {
        return fillEmptyImageCheckBox.isVisible() && fillEmptyImageCheckBox.isSelected();
    }

    public boolean isFitImagesEnabled()
    {
        return fitCheckbox.isVisible() && fitCheckbox.isSelected();
    }

    public boolean isInterlaceVisible()
    {
        return interlaceCheckBox.isVisible();
    }

    public void setInterlaceVisible(boolean value)
    {
        interlaceCheckBox.setVisible(value);
    }

    protected void fireChangedEvent()
    {
        final ChangeEvent event = new ChangeEvent(SequenceDimensionMergePanel.this);

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

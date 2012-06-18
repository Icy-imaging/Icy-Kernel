package icy.gui.menu.tools;

import icy.gui.component.button.IcyButton;
import icy.gui.component.sequence.SequenceChooser;
import icy.gui.component.sequence.SequencePreviewPanel;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SequenceDimensionMergePanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -5908902915282090447L;

    protected DefaultListModel listModel;
    protected ListSelectionModel selectionModel;

    protected IcyButton addButton;
    protected IcyButton removeButton;
    protected IcyButton upButton;
    protected IcyButton downButton;
    protected JList sequenceList;
    protected SequenceChooser sequenceChooser;
    protected SequencePreviewPanel sequencePreview;
    protected JCheckBox interlaceCheckBox;
    protected JCheckBox noEmptyImageCheckBox;

    /**
     * Create the panel.
     */
    public SequenceDimensionMergePanel()
    {
        super();

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
            }
        });
        noEmptyImageCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fireChangedEvent();
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
                    listModel.addElement(seq);

                    refreshButtonsState();
                    fireChangedEvent();
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
            }
        });

        refreshButtonsState();
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {160, 160, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 26, 0, 0, 0, 0, 0, 174, 0};
        gridBagLayout.columnWeights = new double[] {1.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JLabel lblSelectSequenceTo = new JLabel("Add sequence to merge in the list :");
        GridBagConstraints gbc_lblSelectSequenceTo = new GridBagConstraints();
        gbc_lblSelectSequenceTo.fill = GridBagConstraints.VERTICAL;
        gbc_lblSelectSequenceTo.gridwidth = 2;
        gbc_lblSelectSequenceTo.anchor = GridBagConstraints.WEST;
        gbc_lblSelectSequenceTo.insets = new Insets(0, 0, 5, 5);
        gbc_lblSelectSequenceTo.gridx = 0;
        gbc_lblSelectSequenceTo.gridy = 0;
        add(lblSelectSequenceTo, gbc_lblSelectSequenceTo);

        sequenceChooser = new SequenceChooser();
        GridBagConstraints gbc_sequenceChooser = new GridBagConstraints();
        gbc_sequenceChooser.anchor = GridBagConstraints.WEST;
        gbc_sequenceChooser.gridwidth = 2;
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
        gbc_addButton.gridx = 2;
        gbc_addButton.gridy = 1;
        add(addButton, gbc_addButton);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.anchor = GridBagConstraints.WEST;
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridheight = 4;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.gridx = 0;
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
        gbc_removeButton.gridx = 2;
        gbc_removeButton.gridy = 2;
        add(removeButton, gbc_removeButton);

        upButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROUND_ARROW_UP));
        upButton.setToolTipText("Move up selected sequence.");
        upButton.setFlat(true);
        GridBagConstraints gbc_upButton = new GridBagConstraints();
        gbc_upButton.fill = GridBagConstraints.BOTH;
        gbc_upButton.insets = new Insets(0, 0, 5, 0);
        gbc_upButton.gridx = 2;
        gbc_upButton.gridy = 3;
        add(upButton, gbc_upButton);

        downButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROUND_ARROW_DOWN));
        downButton.setToolTipText("Move down selected sequence.");
        downButton.setFlat(true);
        GridBagConstraints gbc_downButton = new GridBagConstraints();
        gbc_downButton.fill = GridBagConstraints.BOTH;
        gbc_downButton.insets = new Insets(0, 0, 5, 0);
        gbc_downButton.gridx = 2;
        gbc_downButton.gridy = 4;
        add(downButton, gbc_downButton);

        interlaceCheckBox = new JCheckBox("Interlace images");
        GridBagConstraints gbc_interlaceCheckBox = new GridBagConstraints();
        gbc_interlaceCheckBox.fill = GridBagConstraints.VERTICAL;
        gbc_interlaceCheckBox.anchor = GridBagConstraints.WEST;
        gbc_interlaceCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_interlaceCheckBox.gridx = 0;
        gbc_interlaceCheckBox.gridy = 6;
        add(interlaceCheckBox, gbc_interlaceCheckBox);

        noEmptyImageCheckBox = new JCheckBox("Avoid empty image");
        noEmptyImageCheckBox.setToolTipText("Replace empty image by the previous non empty one");
        GridBagConstraints gbc_noEmptyImageCheckBox = new GridBagConstraints();
        gbc_noEmptyImageCheckBox.gridwidth = 2;
        gbc_noEmptyImageCheckBox.fill = GridBagConstraints.VERTICAL;
        gbc_noEmptyImageCheckBox.anchor = GridBagConstraints.WEST;
        gbc_noEmptyImageCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_noEmptyImageCheckBox.gridx = 1;
        gbc_noEmptyImageCheckBox.gridy = 6;
        add(noEmptyImageCheckBox, gbc_noEmptyImageCheckBox);

        sequencePreview = new SequencePreviewPanel();
        sequencePreview
                .setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_sequencePreview = new GridBagConstraints();
        gbc_sequencePreview.gridwidth = 2;
        gbc_sequencePreview.anchor = GridBagConstraints.WEST;
        gbc_sequencePreview.insets = new Insets(0, 0, 0, 5);
        gbc_sequencePreview.fill = GridBagConstraints.BOTH;
        gbc_sequencePreview.gridx = 0;
        gbc_sequencePreview.gridy = 7;
        add(sequencePreview, gbc_sequencePreview);
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

    public List<Sequence> getSequences()
    {
        final ArrayList<Sequence> result = new ArrayList<Sequence>();

        for (int i = 0; i < listModel.getSize(); i++)
            result.add((Sequence) listModel.get(i));

        return result;
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

    public boolean isNoEmptyImageEnabled()
    {
        return noEmptyImageCheckBox.isVisible() && noEmptyImageCheckBox.isSelected();
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

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
package icy.gui.component.sequence;

import icy.common.listener.AcceptListener;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.gui.main.MainListener;
import icy.gui.main.WeakMainListener;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * The sequence chooser is a component derived from JComboBox. <br>
 * The combo auto refresh its content regarding to the sequence opened in ICY.<br>
 * You can get it with getSequenceSelected()
 * 
 * @author Fabrice de Chaumont & Stephane<br>
 */

public class SequenceChooser extends JComboBox
{
    public interface SequenceChooserListener
    {
        /**
         * Called when the sequence chooser selection changed for specified sequence.
         */
        public void sequenceChanged(Sequence sequence);
    }

    private class SequenceComboModel extends DefaultComboBoxModel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -1402261337279171323L;

        /**
         * items list
         */
        final List<Sequence> sequences;

        /**
         * @param sequences
         */
        public SequenceComboModel(List<Sequence> sequences)
        {
            super();

            this.sequences = sequences;
        }

        @Override
        public Object getElementAt(int index)
        {
            return sequences.get(index);
        }

        @Override
        public int getSize()
        {
            return sequences.size();
        }
    }

    private static final long serialVersionUID = -6108163762809540675L;

    public static final String SEQUENCE_SELECT_CMD = "sequence_select";

    /**
     * var
     */
    private AcceptListener filter;
    private final boolean nullEntry;

    /**
     * listeners
     */
    private final List<SequenceChooserListener> listeners;
    private final MainListener mainListener;

    /**
     * internals
     */
    private Sequence previousSelectedSequence;

    public SequenceChooser(final int sequenceNameMaxLength, final boolean nullEntry, final boolean autoSelectIfNull,
            final String nullEntryName)
    {
        super();

        this.nullEntry = nullEntry;

        setRenderer(new ListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                final JLabel result = new JLabel();

                if (value instanceof Sequence)
                {
                    final String name = ((Sequence) value).getName();

                    result.setText(StringUtil.limit(name, sequenceNameMaxLength));
                    result.setToolTipText(name);
                }
                else if (value == null)
                    result.setText(nullEntryName);

                return result;
            }
        });

        mainListener = new MainAdapter()
        {
            @Override
            public void sequenceClosed(MainEvent event)
            {
                refreshSequenceList();
            }

            @Override
            public void sequenceFocused(MainEvent event)
            {
                // focus has changed
                if (event.getSource() != null)
                {
                    // if nothing was selected, pick the newly focused sequence
                    if ((getSelectedItem() == null) && autoSelectIfNull)
                        setSelectedItem(event.getSource());
                }
            }

            @Override
            public void sequenceOpened(MainEvent event)
            {
                refreshSequenceList();
            }
        };

        addActionListener(this);
        // add main listener (weak reference --> released when SequenceChooser is released)
        Icy.getMainInterface().addListener(new WeakMainListener(mainListener));

        // default
        listeners = new ArrayList<SequenceChooserListener>();
        setActionCommand(SEQUENCE_SELECT_CMD);
        previousSelectedSequence = null;
        setSelectedItem(null);

        // refresh list
        refreshSequenceList();

        // fix height
        ComponentUtil.setFixedHeight(this, 26);
    }

    public SequenceChooser(int maxLength, boolean nullEntry, boolean autoSelectIfNull)
    {
        this(maxLength, nullEntry, autoSelectIfNull, "no sequence");
    }

    public SequenceChooser(int maxLength, boolean nullEntry)
    {
        this(maxLength, nullEntry, true);
    }

    public SequenceChooser(int maxLength)
    {
        this(maxLength, true);
    }

    public SequenceChooser()
    {
        this(64);
    }

    /**
     * @return the filter
     */
    public AcceptListener getFilter()
    {
        return filter;
    }

    /**
     * @param filter
     *        the filter to set
     */
    public void setFilter(AcceptListener filter)
    {
        if (this.filter != filter)
        {
            this.filter = filter;
            refreshSequenceList();
        }
    }

    ArrayList<Sequence> getFilteredSequenceList()
    {
        final ArrayList<Sequence> allSeq = Icy.getMainInterface().getSequences();
        final ArrayList<Sequence> result = new ArrayList<Sequence>();

        // add null entry at first position
        if (nullEntry)
            result.add(null);

        // apply filter if any
        if (filter != null)
        {
            for (Sequence seq : allSeq)
                if (filter.accept(seq))
                    result.add(seq);
        }
        else
            result.addAll(allSeq);

        return result;
    }

    /**
     * @return current sequence selected in combo. null if no sequence is selected or if the
     *         sequence do not exists anymore.
     */
    public Sequence getSelectedSequence()
    {
        return (Sequence) getSelectedItem();
    }

    /**
     * @param sequence
     *        The sequence to select in the combo box
     */
    public void setSelectedSequence(Sequence sequence)
    {
        if (sequence != getSelectedSequence())
        {
            if (Icy.getMainInterface().getSequences().contains(sequence))
                setSelectedItem(sequence);
            else
                setSelectedItem(null); // set to "no sequence" selection
        }
    }

    /**
     * @deprecated
     *             use {@link #setSelectedSequence(Sequence)} instead
     */
    @Deprecated
    public void setSequenceSelected(Sequence sequence)
    {
        if (sequence != getSelectedSequence())
        {
            if (Icy.getMainInterface().getSequences().contains(sequence))
                setSelectedItem(sequence);
            else
                setSelectedItem(null); // set to "no sequence" selection
        }
    }

    void refreshSequenceList()
    {
        // save old selection
        final Sequence oldSelected = getSelectedSequence();
        // rebuild model
        setModel(new SequenceComboModel(getFilteredSequenceList()));
        // restore selection
        setSelectedSequence(oldSelected);
    }

    // called when sequence selection has changed
    private void sequenceChanged(Sequence sequence)
    {
        fireSequenceChanged(sequence);
    }

    private void fireSequenceChanged(Sequence sequence)
    {
        final ArrayList<SequenceChooserListener> listenersCopy = getListeners();

        for (SequenceChooserListener listener : listenersCopy)
            listener.sequenceChanged(sequence);
    }

    public ArrayList<SequenceChooserListener> getListeners()
    {
        return new ArrayList<SequenceChooserListener>(listeners);
    }

    public void addListener(SequenceChooserListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(SequenceChooserListener listener)
    {
        listeners.remove(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Sequence selected = getSelectedSequence();

        if (previousSelectedSequence != selected)
        {
            previousSelectedSequence = selected;
            // sequence changed
            sequenceChanged(selected);
        }
    }
}

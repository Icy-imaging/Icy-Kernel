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
import icy.gui.main.ActiveSequenceListener;
import icy.gui.main.GlobalSequenceListener;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
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

public class SequenceChooser extends JComboBox implements GlobalSequenceListener, ActiveSequenceListener
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
         * cached items list
         */
        final List<Sequence> cachedList;

        public SequenceComboModel()
        {
            super();

            cachedList = new ArrayList<Sequence>();
            updateList();
        }

        public void updateList()
        {
            final Object selected = getSelectedItem();
            final int oldSize = cachedList.size();

            cachedList.clear();

            // add null entry at first position
            if (nullEntry)
                cachedList.add(null);

            final List<Sequence> sequences = Icy.getMainInterface().getSequences();

            // apply filter if any
            if (filter != null)
            {
                for (Sequence seq : sequences)
                    if (filter.accept(seq))
                        cachedList.add(seq);
            }
            else
                cachedList.addAll(sequences);

            final int newSize = cachedList.size();

            // some elements has been removed
            if (newSize < oldSize)
                fireIntervalRemoved(this, newSize, oldSize - 1);
            // and some elements changed
            fireContentsChanged(this, 0, newSize - 1);

            setSelectedItem(selected);
        }

        @Override
        public Object getElementAt(int index)
        {
            return cachedList.get(index);
        }

        @Override
        public int getSize()
        {
            return cachedList.size();
        }
    }

    private static final long serialVersionUID = -6108163762809540675L;

    public static final String SEQUENCE_SELECT_CMD = "sequence_select";

    /**
     * var
     */
    AcceptListener filter;
    final boolean nullEntry;
    final boolean autoSelectIfNull;

    /**
     * listeners
     */
    private final List<SequenceChooserListener> listeners;

    /**
     * internals
     */
    private Sequence previousSelectedSequence;
    private final SequenceComboModel model;

    public SequenceChooser(final int sequenceNameMaxLength, final boolean nullEntry, final boolean autoSelectIfNull,
            final String nullEntryName)
    {
        super();

        this.nullEntry = nullEntry;
        this.autoSelectIfNull = autoSelectIfNull;

        model = new SequenceComboModel();
        setModel(model);
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

        addActionListener(this);

        // default
        listeners = new ArrayList<SequenceChooserListener>();
        setActionCommand(SEQUENCE_SELECT_CMD);
        previousSelectedSequence = null;
        setSelectedItem(null);

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

    @Override
    public void addNotify()
    {
        super.addNotify();

        Icy.getMainInterface().addGlobalSequenceListener(this);
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    @Override
    public void removeNotify()
    {
        Icy.getMainInterface().removeActiveSequenceListener(this);
        Icy.getMainInterface().removeGlobalSequenceListener(this);

        super.removeNotify();
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
            model.updateList();
        }
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
        setSelectedSequence(sequence);
    }

    // called when sequence selection has changed
    private void sequenceChanged(Sequence sequence)
    {
        fireSequenceChanged(sequence);
    }

    private void fireSequenceChanged(Sequence sequence)
    {
        for (SequenceChooserListener listener : getListeners())
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

    @Override
    public void sequenceActivated(Sequence sequence)
    {
        // active sequence changed
        if (sequence != null)
        {
            // if nothing was selected, pick the newly focused sequence
            if ((getSelectedItem() == null) && autoSelectIfNull)
                setSelectedItem(sequence);
        }
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        // nothing here
    }

    @Override
    public void sequenceOpened(Sequence sequence)
    {
        model.updateList();
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        model.updateList();
    }
}

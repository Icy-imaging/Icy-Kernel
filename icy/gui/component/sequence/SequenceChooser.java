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
import icy.gui.main.GlobalSequenceListener;
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

public class SequenceChooser extends JComboBox implements GlobalSequenceListener
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
            // save selected item
            final Object selected = getSelectedItem();

            final int oldSize = cachedList.size();

            cachedList.clear();

            // add null entry at first position
            if (nullEntryName != null)
                cachedList.add(null);

            final List<Sequence> sequences = Icy.getMainInterface().getSequences();

            // add active sequence entry at second position
            if ((sequences.size() > 0) && (activeSequence != null))
                cachedList.add(activeSequence);

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
            // some elements has been added
            else if (newSize > oldSize)
                fireIntervalAdded(this, oldSize, newSize - 1);

            // and some elements changed
            fireContentsChanged(this, 0, newSize - 1);

            // restore selected item
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
    final String nullEntryName;

    /**
     * listeners
     */
    protected final List<SequenceChooserListener> listeners;

    /**
     * internals
     */
    protected Sequence previousSelectedSequence;
    protected final SequenceComboModel model;
    protected final Sequence activeSequence;

    /**
     * Create a new Sequence chooser component (JComboBox for sequence selection).
     * 
     * @param activeSequenceEntry
     *        If true the combobox will display an <i>Active Sequence</i> entry so when we select it
     *        the {@link #getSelectedSequence()} method returns the current active sequence.
     * @param nullEntryName
     *        If this parameter is not <code>null</code> the combobox will display an extra entry
     *        with the given string to define <code>null</code> sequence selection so when this
     *        entry will be selected the {@link #getSelectedSequence()} will return
     *        <code>null</code>.
     * @param nameMaxLength
     *        Maximum authorized length for the sequence name display in the combobox (extra
     *        characters are truncated).<br>
     *        That prevent the combobox to be resized to very large width.
     */
    public SequenceChooser(final boolean activeSequenceEntry, final String nullEntryName, final int nameMaxLength)
    {
        super();

        this.nullEntryName = nullEntryName;

        if (activeSequenceEntry)
            activeSequence = new Sequence("active sequence");
        else
            activeSequence = null;

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

                    result.setText(StringUtil.limit(name, nameMaxLength));
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

    /**
     * @deprecated Use {@link #SequenceChooser(boolean, String, int)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public SequenceChooser(final int sequenceNameMaxLength, final boolean nullEntry, final boolean autoSelectIfNull,
            final String nullEntryName)
    {
        this(false, nullEntry ? nullEntryName : null, sequenceNameMaxLength);
    }

    /**
     * @deprecated Use {@link #SequenceChooser(boolean, String, int)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public SequenceChooser(int maxLength, boolean nullEntry, boolean autoSelectIfNull)
    {
        this(false, nullEntry ? "no sequence" : null, maxLength);
    }

    /**
     * @deprecated Use {@link #SequenceChooser(boolean, String, int)} instead.
     */
    @Deprecated
    public SequenceChooser(int maxLength, boolean nullEntry)
    {
        this(false, nullEntry ? "no sequence" : null, maxLength);
    }

    /**
     * Create a new Sequence chooser component (JComboBox for sequence selection).
     * 
     * @param activeSequenceEntry
     *        If true the combobox will display an <i>Active Sequence</i> entry so when we select it
     *        the {@link #getSelectedSequence()} method returns the current active sequence.
     * @param nullEntryName
     *        If this parameter is not <code>null</code> the combobox will display an extra entry
     *        with the given string to define <code>null</code> sequence selection so when this
     *        entry will be selected the {@link #getSelectedSequence()} will return
     *        <code>null</code>.
     */
    public SequenceChooser(boolean activeSequenceEntry, String nullEntryName)
    {
        this(activeSequenceEntry, nullEntryName, 64);
    }

    /**
     * @deprecated Use {@link #SequenceChooser(boolean, String, int)} instead.
     */
    @Deprecated
    public SequenceChooser(int nameMaxLength)
    {
        this(false, "no sequence", nameMaxLength);
    }

    /**
     * Create a new Sequence chooser component (JComboBox for sequence selection).
     */
    public SequenceChooser()
    {
        this(true, null, 64);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        Icy.getMainInterface().addGlobalSequenceListener(this);
    }

    @Override
    public void removeNotify()
    {
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
     * Set a filter for sequence display.<br>
     * Only Sequence accepted by the filter will appear in the combobox.
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
     * @return current selected sequence.
     */
    public Sequence getSelectedSequence()
    {
        final Sequence result = (Sequence) getSelectedItem();

        // special case for active sequence
        if (result == activeSequence)
            return Icy.getMainInterface().getActiveSequence();

        return result;
    }

    /**
     * Select the <i>Active sequence</i> entry if enable.
     */
    public void setActiveSequenceSelected()
    {
        if (activeSequence != null)
            setSelectedItem(activeSequence);
    }

    /**
     * @param sequence
     *        The sequence to select in the combo box
     */
    public void setSelectedSequence(Sequence sequence)
    {
        if (sequence != getSelectedSequence())
            setSelectedItem(sequence);
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

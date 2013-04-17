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
package icy.gui.frame.sequence;

import icy.gui.component.sequence.SequenceChooser;
import icy.gui.component.sequence.SequenceChooser.SequenceChooserListener;
import icy.gui.frame.ActionFrame;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.sequence.Sequence;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class SequenceActionFrame extends ActionFrame
{
    public interface SourceChangeListener
    {
        public void sequenceChanged(Sequence sequence);
    }

    /**
     * GUI
     */
    private final SequenceChooser sequenceSelector;

    /**
     * internals
     */
    private final ArrayList<SourceChangeListener> sourceChangeListeners;

    /**
     * @param title
     * @param resizable
     * @param iconifiable
     */
    public SequenceActionFrame(String title, boolean resizable, boolean iconifiable)
    {
        super(title, resizable, iconifiable);

        sourceChangeListeners = new ArrayList<SourceChangeListener>();

        // GUI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        final JPanel sourcePanel = new JPanel();
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Select the source"));
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.PAGE_AXIS));
        // fix the height of source panel
        ComponentUtil.setFixedHeight(sourcePanel, 58);

        final JPanel sequenceSelectPanel = new JPanel();
        sequenceSelectPanel.setLayout(new BoxLayout(sequenceSelectPanel, BoxLayout.LINE_AXIS));

        // select sequence
        final JLabel sequenceSelectLabel = new JLabel("Sequence  ");
        sequenceSelector = new SequenceChooser(48);
        sequenceSelector.setSelectedSequence(Icy.getMainInterface().getFocusedSequence());
        sequenceSelector.setMinimumSize(new Dimension(100, 24));
        sequenceSelector.addListener(new SequenceChooserListener()
        {
            @Override
            public void sequenceChanged(Sequence sequence)
            {
                fireSequenceChangeEvent();
            }
        });

        sequenceSelectPanel.add(Box.createHorizontalStrut(10));
        sequenceSelectPanel.add(sequenceSelectLabel);
        sequenceSelectPanel.add(sequenceSelector);
        sequenceSelectPanel.add(Box.createHorizontalStrut(10));

        sourcePanel.add(sequenceSelectPanel);

        mainPanel.add(sourcePanel);
    }

    /**
     * @param title
     * @param resizable
     */
    public SequenceActionFrame(String title, boolean resizable)
    {
        this(title, resizable, false);
    }

    /**
     * @param title
     */
    public SequenceActionFrame(String title)
    {
        this(title, false, false);
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence()
    {
        return sequenceSelector.getSelectedSequence();
    }

    public void addSourceChangeListener(SourceChangeListener listener)
    {
        if (!sourceChangeListeners.contains(listener))
            sourceChangeListeners.add(listener);
    }

    public void removeSourceChangeListener(SourceChangeListener listener)
    {
        sourceChangeListeners.remove(listener);
    }

    void fireSequenceChangeEvent()
    {
        final Sequence sequence = getSequence();

        for (SourceChangeListener listener : sourceChangeListeners)
            listener.sequenceChanged(sequence);
    }
}

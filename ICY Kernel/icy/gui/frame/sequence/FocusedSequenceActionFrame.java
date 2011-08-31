/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.frame.sequence;

import icy.gui.component.ComponentUtil;
import icy.gui.frame.ActionFrame;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;

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
public abstract class FocusedSequenceActionFrame extends ActionFrame implements SequenceListener
{
    public interface SourceChangeListener
    {
        public void sourceSequenceChanged(Sequence seq);
    }

    /**
     * input sequence
     */
    Sequence seqIn;

    /**
     * listeners and event handler
     */
    private final MainAdapter mainAdapter;
    private final ArrayList<SourceChangeListener> sourceChangeListeners;

    /**
     * gui
     */
    final JPanel sourcePanel;
    final JLabel sequenceLabel;

    /**
     * @param title
     * @param resizable
     * @param iconifiable
     */
    public FocusedSequenceActionFrame(String title, boolean resizable, boolean iconifiable)
    {
        super(title, resizable, iconifiable);

        sourceChangeListeners = new ArrayList<SourceChangeListener>();

        mainAdapter = new MainAdapter()
        {
            /*
             * (non-Javadoc)
             * 
             * @see icy.gui.main.MainListener#sequenceFocused(icy.gui.main.MainEvent)
             */
            @Override
            public void sequenceFocused(MainEvent event)
            {
                setSeqIn((Sequence) event.getSource());
            }
        };

        Icy.getMainInterface().addListener(mainAdapter);

        // init value
        seqIn = null;

        // GUI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        sourcePanel = new JPanel();
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Selected sequence"));
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.LINE_AXIS));

        // sequence label
        sequenceLabel = new JLabel();
        sequenceLabel.setMinimumSize(new Dimension(100, 24));

        sourcePanel.add(Box.createHorizontalStrut(10));
        sourcePanel.add(sequenceLabel);
        sourcePanel.add(Box.createHorizontalGlue());

        // fix the height of source panel
        ComponentUtil.setFixedHeight(sourcePanel, 54);

        mainPanel.add(sourcePanel);

        // set input sequence once GUI is built
        setSeqIn(Icy.getMainInterface().getFocusedSequence());
    }

    /**
     * @param title
     * @param resizable
     */
    public FocusedSequenceActionFrame(String title, boolean resizable)
    {
        this(title, resizable, false);
    }

    /**
     * @param title
     */
    public FocusedSequenceActionFrame(String title)
    {
        this(title, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.frame.IcyFrame#onClosed()
     */
    @Override
    public void onClosed()
    {
        Icy.getMainInterface().removeListener(mainAdapter);

        super.onClosed();
    }

    /**
     * @return the sourcePanel
     */
    public JPanel getSourcePanel()
    {
        return sourcePanel;
    }

    /**
     * @return the sequence
     */
    public Sequence getSeqIn()
    {
        return seqIn;
    }

    /**
     * @param value
     *        the sequence to set
     */
    void setSeqIn(Sequence value)
    {
        if (seqIn != value)
        {
            if (seqIn != null)
                seqIn.removeListener(this);

            seqIn = value;

            if (seqIn != null)
            {
                sequenceLabel.setText(seqIn.getName());
                seqIn.addListener(this);
            }
            else
                sequenceLabel.setText("no sequence");

            fireSequenceChangeEvent(seqIn);
        }
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

    private void fireSequenceChangeEvent(Sequence seq)
    {
        for (SourceChangeListener listener : sourceChangeListeners)
            listener.sourceSequenceChanged(seq);
    }

    @Override
    public void sequenceChanged(SequenceEvent event)
    {
        // just for overload
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        // just for overload
    }

}

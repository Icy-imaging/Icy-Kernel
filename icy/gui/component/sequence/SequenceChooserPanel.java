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
package icy.gui.component.sequence;

import icy.common.listener.AcceptListener;
import icy.gui.component.sequence.SequenceChooser.SequenceChooserListener;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SequenceChooserPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 486511549781132187L;

    protected JLabel titleLabel;
    protected SequenceChooser sequenceChooser;

    /**
     * Create the panel.
     */
    public SequenceChooserPanel(String title)
    {
        super();

        initialize(title);
    }

    private void initialize(String title)
    {
        setLayout(new BorderLayout(0, 0));

        sequenceChooser = new SequenceChooser();
        add(sequenceChooser, BorderLayout.CENTER);

        if (!StringUtil.isEmpty(title))
        {
            titleLabel = new JLabel(title);
            titleLabel.setBorder(new EmptyBorder(4, 0, 0, 8));
            add(titleLabel, BorderLayout.WEST);
        }
    }

    /**
     * @return the filter
     */
    public AcceptListener getFilter()
    {
        return sequenceChooser.getFilter();
    }

    /**
     * @param filter
     *        the filter to set
     */
    public void setFilter(AcceptListener filter)
    {
        sequenceChooser.setFilter(filter);
    }

    /**
     * @return current sequence selected in combo. null if no sequence is selected or if the
     *         sequence do not exists anymore.
     */
    public Sequence getSelectedSequence()
    {
        return sequenceChooser.getSelectedSequence();
    }

    /**
     * @param sequence
     *        The sequence to select in the combo box
     */
    public void setSelectedSequence(Sequence sequence)
    {
        sequenceChooser.setSelectedSequence(sequence);
    }

    public ArrayList<SequenceChooserListener> getListeners()
    {
        return sequenceChooser.getListeners();
    }

    public void addListener(SequenceChooserListener listener)
    {
        sequenceChooser.addListener(listener);
    }

    public void removeListener(SequenceChooserListener listener)
    {
        sequenceChooser.removeListener(listener);
    }

}

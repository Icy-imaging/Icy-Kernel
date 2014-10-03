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
package icy.sequence.edit;

import icy.sequence.Sequence;
import icy.undo.AbstractIcyUndoableEdit;

import java.awt.Image;

/**
 * Abstract sequence undoable edit.
 * 
 * @author Stephane
 */
public abstract class AbstractSequenceEdit extends AbstractIcyUndoableEdit
{
    public AbstractSequenceEdit(Sequence sequence, String name, Image icon)
    {
        super(sequence, name, icon);
    }

    public AbstractSequenceEdit(Sequence sequence, String name)
    {
        this(sequence, name, null);
    }

    public AbstractSequenceEdit(Sequence sequence, Image icon)
    {
        this(sequence, "Sequence changed", icon);
    }

    public AbstractSequenceEdit(Sequence sequence)
    {
        this(sequence, "Sequence changed", null);
    }

    public Sequence getSequence()
    {
        return (Sequence) getSource();
    }
}

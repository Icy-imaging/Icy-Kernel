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
package icy.gui.component;

/**
 * @deprecated Use {@link icy.gui.component.sequence.SequenceChooser} instead.
 */
@Deprecated
public class SequenceChooser extends icy.gui.component.sequence.SequenceChooser
{
    /**
     * 
     */
    private static final long serialVersionUID = 5958250080388193463L;

    public interface SequenceChooserListener extends icy.gui.component.sequence.SequenceChooser.SequenceChooserListener
    {

    }

    public SequenceChooser(final int sequenceNameMaxLength, final boolean nullEntry, final boolean autoSelectIfNull,
            final String nullEntryName)
    {
        super(sequenceNameMaxLength, nullEntry, autoSelectIfNull, nullEntryName);
    }

    public SequenceChooser(int maxLength, boolean nullEntry, boolean autoSelectIfNull)
    {
        super(maxLength, nullEntry, autoSelectIfNull);
    }

    public SequenceChooser(int maxLength, boolean nullEntry)
    {
        super(maxLength, nullEntry);
    }

    public SequenceChooser(int maxLength)
    {
        super(maxLength);
    }

    public SequenceChooser()
    {
        super();
    }
}

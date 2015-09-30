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
package icy.gui.frame;

import icy.gui.dialog.ConfirmDialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * ConfirmFrame class.<br>
 * Almost same as ConfirmDialog except it is not modal so you have to check<br>
 * for both <code>ready()</code> and <code>confirmed()</code> methods.
 * 
 * @author Stephane
 */
public class ConfirmFrame extends JOptionPane
{
    /**
     * 
     */
    private static final long serialVersionUID = 2833505262575458420L;

    public ConfirmFrame(final String title, final String message, final int optionType)
    {
        super(message, JOptionPane.QUESTION_MESSAGE, optionType);

        setInitialValue(initialValue);
        setComponentOrientation(getRootFrame().getComponentOrientation());
        JDialog dialog = createDialog(null, title);
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);

        selectInitialValue();
        dialog.show();
    }

    public ConfirmFrame(final String title, final String message)
    {
        this(title, message, YES_NO_OPTION);
    }

    public ConfirmFrame(final String message)
    {
        this("Confirmation", message, YES_NO_OPTION);
    }

    /**
     * Return true if user confirmed
     */
    public boolean confirmed()
    {
        final Object v = getValue();

        if (v instanceof Integer)
            return ConfirmDialog.getBooleanReturnValue(((Integer) v).intValue());

        return false;
    }

    /**
     * Return true if user made choice.
     */
    public boolean ready()
    {
        return (getValue() != JOptionPane.UNINITIALIZED_VALUE);
    }
}

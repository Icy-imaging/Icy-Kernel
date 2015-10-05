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
package icy.gui.dialog;

import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Confirmation dialog with a "do not display again" bottom checkbox.<br>
 * The id is used to store the "do not display again" state.<br>
 * If you call confirm(...) with "do not display again" already set to true then confirm dialog is
 * not displayed.
 * 
 * @author Stephane
 */
public class IdConfirmDialog
{
    /**
     * Keep it public in case we want custom IdConfirmDialog :)
     */
    public static class Confirmer implements Runnable
    {
        private final String title;
        private final String message;
        private final int optionType;
        private final String id;

        boolean result;
        JCheckBox doNotDisplayCheckbox;

        /**
         * @param title
         * @param message
         * @param optionType
         */
        public Confirmer(String title, String message, int optionType, String id)
        {
            super();

            this.id = id;
            this.title = title;
            this.message = message;
            this.optionType = optionType;
        }

        @Override
        public void run()
        {
            if (!StringUtil.isEmpty(id))
            {
                // Confirm dialog should not be displayed ?
                if (!GeneralPreferences.getPreferencesConfirms().getBoolean(id, true))
                {
                    // confirmed and exit
                    result = true;
                    return;
                }

                // display checkbox
                doNotDisplayCheckbox = new JCheckBox("Do not show this message again", false);
            }
            else
                doNotDisplayCheckbox = null;

            final JFrame parent = Icy.getMainInterface().getMainFrame();
            final JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, optionType, null, null,
                    null);

            pane.setInitialValue(null);
            if (parent != null)
                pane.setComponentOrientation(parent.getComponentOrientation());

            final JDialog dialog = pane.createDialog(parent, title);

            pane.selectInitialValue();
            if (doNotDisplayCheckbox != null)
            {
                dialog.getContentPane().add(
                        GuiUtil.createLineBoxPanel(doNotDisplayCheckbox, Box.createHorizontalGlue()),
                        BorderLayout.SOUTH);
                dialog.pack();
            }
            dialog.setVisible(true);
            dialog.dispose();

            final Object selectedValue = pane.getValue();

            // save checkbox state
            if ((doNotDisplayCheckbox != null) && doNotDisplayCheckbox.isSelected())
                GeneralPreferences.getPreferencesConfirms().putBoolean(id, false);

            if (selectedValue == null)
                result = false;
            else
            {
                if (selectedValue instanceof Integer)
                    result = getBooleanReturnValue(((Integer) selectedValue).intValue());
                else
                    result = false;
            }
        }

        public boolean getResult()
        {
            return result;
        }
    }

    public static final int DEFAULT_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;

    public static boolean getBooleanReturnValue(final int returnValue)
    {
        return (returnValue == JOptionPane.YES_OPTION) || (returnValue == JOptionPane.OK_OPTION);
    }

    public static boolean confirm(String title, String message, int optionType, String id)
    {
        final Confirmer confirmer = new Confirmer(title, message, optionType, id);

        // always confirm in headless mode
        if (Icy.getMainInterface().isHeadLess())
            return true;

        ThreadUtil.invokeNow(confirmer);

        return confirmer.result;
    }

    public static boolean confirm(final String title, final String message, String id)
    {
        return confirm(title, message, OK_CANCEL_OPTION, id);
    }

    public static boolean confirm(final String message, String id)
    {
        return confirm("Confirmation", message, OK_CANCEL_OPTION, id);
    }
}

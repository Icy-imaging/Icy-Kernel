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
package icy.gui.dialog;

import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author stephane
 */
public class ConfirmDialog
{
    private static class Confirmer implements Runnable
    {
        private final String title;
        private final String message;
        private final int optionType;

        boolean result;

        /**
         * @param title
         * @param message
         * @param optionType
         */
        public Confirmer(String title, String message, int optionType)
        {
            super();

            this.title = title;
            this.message = message;
            this.optionType = optionType;
        }

        @Override
        public void run()
        {
            final JFrame parent = Icy.getMainInterface().getFrame();

            result = getBooleanReturnValue(JOptionPane.showConfirmDialog(parent, message, title, optionType,
                    JOptionPane.QUESTION_MESSAGE));
        }
    }

    public static final int DEFAULT_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;

    static boolean getBooleanReturnValue(final int returnValue)
    {
        return (returnValue == JOptionPane.YES_OPTION) || (returnValue == JOptionPane.OK_OPTION);
    }

    public static boolean confirm(final String title, final String message, final int optionType)
    {
        final Confirmer confirmer = new Confirmer(title, message, optionType);

        ThreadUtil.invokeNow(confirmer);

        return confirmer.result;
    }

    public static boolean confirm(final String message)
    {
        return confirm("Confirmation", message, YES_NO_OPTION);
    }

    public static boolean confirm(final String title, final String message)
    {
        return confirm(title, message, YES_NO_OPTION);
    }

}

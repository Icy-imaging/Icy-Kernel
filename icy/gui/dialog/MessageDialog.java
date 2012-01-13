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
public class MessageDialog
{
    public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
    /** Used for information messages. */
    public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
    /** Used for warning messages. */
    public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
    /** Used for questions. */
    public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
    /** No icon is used. */
    public static final int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;

    public static void showDialog(final String message)
    {
        showDialog("Information", message, INFORMATION_MESSAGE);
    }

    public static void showDialog(final String message, final int messageType)
    {
        final String title;

        switch (messageType)
        {
            case INFORMATION_MESSAGE:
                title = "Information";
                break;

            case WARNING_MESSAGE:
                title = "Warning";
                break;

            case ERROR_MESSAGE:
                title = "Error";
                break;

            case QUESTION_MESSAGE:
                title = "Confirmation";
                break;

            default:
                title = "Message";
                break;
        }

        showDialog(title, message, messageType);
    }

    public static void showDialog(final String title, final String message)
    {
        showDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDialog(final String title, final String message, final int messageType)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final JFrame parent = Icy.getMainInterface().getMainFrame();

                JOptionPane.showMessageDialog(parent, message, title, messageType);
            }
        });
    }
}

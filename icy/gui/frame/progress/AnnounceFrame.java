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
package icy.gui.frame.progress;

import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Timer;

public class AnnounceFrame extends TaskFrame implements ActionListener
{
    JButton button;
    JLabel label;
    Timer timer;
    Runnable action;

    /**
     * Show an announcement with specified message.
     * 
     * @param message
     *        message to display in announcement
     */
    public AnnounceFrame(final String message)
    {
        this(message, "Ok", null, 0);
    }

    /**
     * Show an announcement with specified parameters.
     * 
     * @param message
     *        message to display in announcement
     * @param btnAction
     *        action on button click
     */
    public AnnounceFrame(final String message, final Runnable btnAction)
    {
        this(message, "Ok", btnAction, 0);
    }

    /**
     * Show an announcement with specified parameters
     * 
     * @param message
     *        message to display in announcement
     * @param liveTime
     *        life time in second (0 = infinite)
     */
    public AnnounceFrame(final String message, final int liveTime)
    {
        this(message, "Ok", null, liveTime);
    }

    /**
     * Show an announcement with specified parameters
     * 
     * @param message
     *        message to display in announcement
     * @param btnAction
     *        action on button click
     * @param liveTime
     *        life time in second (0 = infinite)
     */

    public AnnounceFrame(final String message, final Runnable btnAction, final int liveTime)
    {
        this(message, "Ok", btnAction, liveTime);
    }

    /**
     * Show an announcement with specified parameters
     * 
     * @param message
     *        message to display in announcement
     * @param buttonText
     *        button text
     * @param btnAction
     *        action on button click
     * @param liveTime
     *        life time in second (0 = infinite)
     */
    public AnnounceFrame(final String message, final String buttonText, final Runnable btnAction, final int liveTime)
    {
        super("");

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                button = new JButton();
                label = new JLabel();
                action = btnAction;
                if (liveTime != 0)
                {
                    timer = new Timer(liveTime * 1000, AnnounceFrame.this);
                    timer.setRepeats(false);
                    timer.start();
                }

                label.setText("   " + message + "   ");
                button.setText(buttonText);
                button.addActionListener(AnnounceFrame.this);

                setTitleBarVisible(false);

                setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

                add(label);
                add(button);

                pack();
            }
        });
    }

    /**
     * @return the action
     */
    public Runnable getAction()
    {
        return action;
    }

    /**
     * @param action
     *        the action to set
     */
    public void setAction(Runnable action)
    {
        this.action = action;
    }

    @Override
    public void internalClose()
    {
        // stop timer
        timer.stop();

        super.internalClose();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == button)
        {
            // execute action
            if (action != null)
                action.run();
        }

        // close frame on both action (timer or button)
        close();
    }

}

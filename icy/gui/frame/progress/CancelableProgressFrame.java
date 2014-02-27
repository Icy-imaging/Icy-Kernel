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
package icy.gui.frame.progress;

import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * @author stephane
 */
public class CancelableProgressFrame extends ProgressFrame implements ActionListener
{
    // GUI
    JButton cancelBtn;

    private boolean cancelRequested;

    /**
     * @param message
     */
    public CancelableProgressFrame(String message)
    {
        super(message);

        cancelRequested = false;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                cancelBtn = new JButton("Cancel");

                cancelBtn.addActionListener(CancelableProgressFrame.this);

                mainPanel.add(cancelBtn);
                pack();
            }
        });
    }

    @Override
    public void onClosed()
    {
        super.onClosed();

        // so we force cancel operation on application exit
        cancelRequested = true;
    }

    /**
     * @return the cancelRequested
     */
    public boolean isCancelRequested()
    {
        return cancelRequested;
    }

    public void setCancelEnabled(boolean enabled)
    {
        if (cancelBtn != null)
            cancelBtn.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // notify cancel requested
        cancelRequested = true;
        // close frame
        close();
    }

    @Override
    public boolean notifyProgress(double position, double length)
    {
        // cancel requested ?
        if (isCancelRequested())
            return false;

        return super.notifyProgress(position, length);
    }
}

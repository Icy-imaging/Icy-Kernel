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

import icy.common.ProgressListener;
import icy.system.thread.ThreadUtil;

import javax.swing.BoxLayout;
import javax.swing.JProgressBar;

/**
 * A progress TaskFrame (thread safe)
 * 
 * @author fab & stephane
 */
public class ProgressFrame extends TaskFrame implements ProgressListener
{
    private static final long serialVersionUID = -8374018918054001693L;

    /**
     * gui
     */
    JProgressBar progressBar;
    /**
     * length (in bytes) of download
     */
    protected double length;
    /**
     * current position (in bytes) of download
     */
    protected double position;

    public ProgressFrame(final String message)
    {
        super("");

        // default
        length = 100d;
        position = -1d;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar = new JProgressBar();
                progressBar.setString(buildMessage(message));
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(true);
                progressBar.setMinimum(0);
                // this is enough for a smooth progress
                progressBar.setMaximum(1000);

                setFocusable(false);
                // no title bar
                setTitleBarVisible(false);

                setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
                add(progressBar);
                pack();

                // already done with taskManager
                // setVisible(true);
            }
        });
    }

    protected String buildMessage(String message)
    {
        return "  " + message + "  ";
    }

    protected void updateDisplay()
    {
        // information on position
        if ((position != -1d) && (length > 0d))
            setProgress((int) (position * 1000d / length));
        else
            setProgress(-1);
    }

    public void setProgress(final int value)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (value == -1)
                    progressBar.setIndeterminate(true);
                else
                {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    public void setMessage(final String text)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setString(buildMessage(text));
                // so component is resized according to its string length
                progressBar.invalidate();
                pack();
            }
        });
    }

    // we want tooltip set on the progress component only
    @Override
    public void setToolTipText(final String text)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setToolTipText(text);
            }
        });
    }

    /**
     * @return the length
     */
    public double getLength()
    {
        return length;
    }

    /**
     * @param length
     *        the length to set
     */
    public void setLength(double length)
    {
        if (this.length != length)
        {
            this.length = length;

            // refresh
            updateDisplay();
        }
    }

    /**
     * @return the position
     */
    public double getPosition()
    {
        return position;
    }

    /**
     * increment progress position
     */
    public void incPosition()
    {
        setPosition(position + 1);
    }

    /**
     * @param position
     *        the position to set
     */
    public void setPosition(double position)
    {
        if (this.position != position)
        {
            this.position = position;

            // refresh
            updateDisplay();
        }
    }

    @Override
    public boolean notifyProgress(double position, double length)
    {
        setLength(length);
        setPosition(position);

        return true;
    }

}

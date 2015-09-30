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
package icy.gui.frame.progress;

import icy.common.listener.ProgressListener;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;

/**
 * A progress TaskFrame (thread safe)
 * 
 * @author fab & stephane
 */
public class ProgressFrame extends TaskFrame implements ProgressListener, Runnable
{
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

    /**
     * current message
     */
    protected String message;
    /**
     * current tooltip
     */
    protected String tooltip;

    /**
     * internals
     */
    // private final SingleProcessor processor;

    public ProgressFrame(final String message)
    {
        super("");

        // default
        length = 100d;
        position = -1d;
        // processor = new SingleProcessor(true);
        this.message = message;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar = new JProgressBar();
                progressBar.setString(buildMessage(message));
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(true);
                progressBar.setBorder(BorderFactory.createEmptyBorder());
                progressBar.setMinimum(0);
                // this is enough for a smooth progress
                progressBar.setMaximum(1000);

                mainPanel.setLayout(new BorderLayout());
                mainPanel.add(progressBar, BorderLayout.CENTER);

                pack();
            }
        });
    }

    protected String buildMessage(String message)
    {
        return "  " + message + "  ";
    }

    @Override
    public void run()
    {
        updateDisplay();
    }

    public void refresh()
    {
        // refresh (need single delayed execution)
        ThreadUtil.runSingle(this);
    }

    protected void updateDisplay()
    {
        // repacking need to be done on EDT
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {

                // position information
                if ((position != -1d) && (length > 0d))
                {
                    // remove indeterminate state
                    if (progressBar.isIndeterminate())
                        progressBar.setIndeterminate(false);

                    // set progress
                    final int value = (int) (position * 1000d / length);
                    if (progressBar.getValue() != value)
                        progressBar.setValue(value);
                }
                else
                {
                    // set indeterminate state
                    if (!progressBar.isIndeterminate())
                        progressBar.setIndeterminate(true);
                }

                final String text = buildMessage(message);

                // set progress message
                if (!StringUtil.equals(progressBar.getString(), text))
                {
                    progressBar.setString(text);
                    // so component is resized according to its string length
                    progressBar.invalidate();

                    // repack frame
                    pack();
                }

                // set tooltip
                if (!StringUtil.equals(progressBar.getToolTipText(), tooltip))
                    progressBar.setToolTipText(tooltip);
            }
        });
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String value)
    {
        if (message != value)
        {
            message = value;
            refresh();
        }
    }

    // we want tooltip set on the progress component only
    @Override
    public void setToolTipText(String value)
    {
        if (tooltip != value)
        {
            tooltip = value;
            refresh();
        }
    }

    /**
     * @return the length
     */
    public double getLength()
    {
        return length;
    }

    /**
     * @param value
     *        the length to set
     */
    public void setLength(double value)
    {
        if (length != value)
        {
            length = value;
            refresh();
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
     * @param value
     *        the position to set
     */
    public void setPosition(double value)
    {
        if (position != value)
        {
            position = value;
            refresh();
        }
    }

    /**
     * @deprecated use {@link #setPosition(double)} instead
     */
    @Deprecated
    public void setProgress(int value)
    {
        setPosition(value);
    }

    @Override
    public boolean notifyProgress(double position, double length)
    {
        if ((this.position != position) || (this.length != length))
        {
            this.length = length;
            this.position = position;

            refresh();
        }

        return true;
    }

}

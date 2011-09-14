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

import java.awt.Color;

/**
 * @author Stephane
 */
public class FailedAnnounceFrame extends AnnounceFrame
{
    private static final int DEFAULT_LIVETIME = 0;

    /**
     * @param message
     * @param liveTime
     */
    public FailedAnnounceFrame(String message, int liveTime)
    {
        super(message, liveTime);

        ThreadUtil.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                label.setOpaque(true);
                label.setBackground(new Color(255, 128, 128));
            }
        });
    }

    /**
     * @param message
     */
    public FailedAnnounceFrame(String message)
    {
        this(message, DEFAULT_LIVETIME);
    }
}

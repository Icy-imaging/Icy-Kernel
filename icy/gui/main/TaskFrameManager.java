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
package icy.gui.main;

import icy.gui.frame.progress.ProgressFrame;
import icy.gui.frame.progress.TaskFrame;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Manage the TaskFrame to display them on right of the window.
 * 
 * @author Fabrice de Chaumont & Stephane Dallongeville
 */
public class TaskFrameManager implements Runnable
{
    private static class FrameInformation
    {
        Point position;
        long msBeforeDisplay;
        long msBeforeClose;
        boolean canBeRemoved;

        public FrameInformation(Point pos, long msBeforeDisplay, long msBeforeClose)
        {
            super();

            position = pos;
            this.msBeforeDisplay = msBeforeDisplay;
            this.msBeforeClose = msBeforeClose;
            canBeRemoved = false;
        }

        public void setMsBeforeClose(long value)
        {
            msBeforeClose = value;

            if (value <= 0)
                canBeRemoved = true;
        }

        public boolean isVisible()
        {
            return (msBeforeDisplay <= 0);
        }
    }

    final Thread animThread;
    Map<TaskFrame, FrameInformation> taskFrameInfos;
    long lastAnimationMillisecondTime;

    /**
     * 
     */
    public TaskFrameManager()
    {
        super();

        taskFrameInfos = new HashMap<TaskFrame, FrameInformation>();
        animThread = new Thread(this, "TaskFrame manager");

        lastAnimationMillisecondTime = System.currentTimeMillis();
    }

    // we have to separate init as thread call the getMainInterface() method
    public void init()
    {
        animThread.start();
    }

    Dimension getDesktopSize()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

        if (mainFrame != null)
            // get bottom right border location
            return mainFrame.getDesktopSize();

        return null;
    }

    public void addTaskWindow(final TaskFrame tFrame, final long msBeforeDisplay, final long msAfterCloseRequest)
    {
        final Dimension desktopSize = getDesktopSize();

        if ((desktopSize != null) && !tFrame.canRemove())
        {
            // get bottom right border location
            final Point pos = new Point(desktopSize.width + 10, 0);
            final FrameInformation frameInformation = new FrameInformation(pos, msBeforeDisplay, msAfterCloseRequest);

            synchronized (taskFrameInfos)
            {
                taskFrameInfos.put(tFrame, frameInformation);
            }

            tFrame.addToMainDesktopPane();
            tFrame.setLocation(pos);
            tFrame.toFront();
        }
    }

    public void addTaskWindow(final TaskFrame tFrame)
    {
        // we use a different default value for progress frame
        if (tFrame instanceof ProgressFrame)
            addTaskWindow(tFrame, 0, 1000);
        else
            addTaskWindow(tFrame, 0, 0);
    }

    @Override
    public void run()
    {
        while (true)
        {
            long currentMillisecondTime = System.currentTimeMillis();
            long delayBetween2Animation = currentMillisecondTime - lastAnimationMillisecondTime;
            lastAnimationMillisecondTime = currentMillisecondTime;

            HashMap<TaskFrame, FrameInformation> frameInfos;

            synchronized (taskFrameInfos)
            {
                frameInfos = new HashMap<TaskFrame, FrameInformation>(taskFrameInfos);
            }

            // process frame which become visible and the one which will be closed
            for (Entry<TaskFrame, FrameInformation> finfo : frameInfos.entrySet())
            {
                final TaskFrame frame = finfo.getKey();
                final FrameInformation info = finfo.getValue();

                info.msBeforeDisplay -= delayBetween2Animation;

                if (info.isVisible())
                {
                    if (!frame.isVisible())
                        frame.setVisible(true);
                }

                if (frame.canRemove())
                    info.setMsBeforeClose(info.msBeforeClose - delayBetween2Animation);
            }

            // get bottom right border location
            final Dimension desktopSize = getDesktopSize();
            // not yet initialized
            if (desktopSize == null)
                return;

            final List<TaskFrame> toRemove = new ArrayList<TaskFrame>();

            for (Entry<TaskFrame, FrameInformation> finfo : frameInfos.entrySet())
            {
                final TaskFrame frame = finfo.getKey();
                final FrameInformation info = finfo.getValue();

                // frame need to be removed ?
                if ((info != null) && info.canBeRemoved)
                {
                    // get frame position
                    final Point location = frame.getLocation();

                    // frame already hidden ?
                    if ((!frame.isVisible()) || ((location.x > desktopSize.width)))
                    {
                        // remove it from list
                        toRemove.add(frame);
                        // and close it definitely
                        frame.internalClose();
                    }
                }
            }

            // remove frame which are complete from the list
            for (TaskFrame frame : toRemove)
                frameInfos.remove(frame);

            // update global list
            taskFrameInfos = new HashMap<TaskFrame, FrameInformation>(frameInfos);

            // calculate current Y position
            float currentY = desktopSize.height;
            for (Entry<TaskFrame, FrameInformation> finfo : frameInfos.entrySet())
            {
                final FrameInformation info = finfo.getValue();

                if ((info != null) && info.isVisible())
                    currentY -= finfo.getKey().getHeight();
            }

            // calculate all frames position
            for (Entry<TaskFrame, FrameInformation> finfo : frameInfos.entrySet())
            {
                final TaskFrame frame = finfo.getKey();
                final FrameInformation info = finfo.getValue();

                if ((info != null) && info.isVisible())
                {
                    int xTarget = desktopSize.width - frame.getWidth();

                    if (info.canBeRemoved)
                        xTarget = desktopSize.width + 20;

                    final Point positionTarget = new Point(xTarget, (int) currentY);
                    final Point positionCurrent = frame.getLocation();

                    float vectX = (positionTarget.x - positionCurrent.x) / 10f;
                    if (vectX != 0f)
                    {
                        // we want at least 1 or -1
                        if (Math.abs(vectX) < 1f)
                        {
                            if (vectX < 0)
                                vectX = -1f;
                            else
                                vectX = 1f;
                        }
                    }
                    float vectY = (positionTarget.y - positionCurrent.y) / 10f;
                    if (vectY != 0f)
                    {
                        // we want at least 1 or -1
                        if (Math.abs(vectY) < 1f)
                        {
                            if (vectY < 0)
                                vectY = -1f;
                            else
                                vectY = 1f;
                        }
                    }
                    final Point positionNew = new Point((int) (positionCurrent.x + vectX),
                            (int) (positionCurrent.y + vectY));

                    // set Y when starting the scroll.
                    if (positionCurrent.x > desktopSize.width)
                        positionNew.y = positionTarget.y;

                    // update position
                    info.position = positionNew;

                    // no enough space to display frame, close it
                    if (positionNew.y < 0)
                        frame.close();

                    currentY += frame.getHeight();
                }
            }

            // update frame position
            for (Entry<TaskFrame, FrameInformation> finfo : frameInfos.entrySet())
            {
                final TaskFrame frame = finfo.getKey();
                final FrameInformation info = finfo.getValue();

                if (info.position != null)
                {
                    // avoid repaint on JDesktopPane if position did not changed
                    if (!frame.getLocationInternal().equals(info.position))
                        frame.setLocationInternal(info.position);
                    if (!frame.getLocationExternal().equals(info.position))
                        frame.setLocationExternal(info.position);
                }
            }

            // sleep a bit
            ThreadUtil.sleep(20);
        }
    }
}

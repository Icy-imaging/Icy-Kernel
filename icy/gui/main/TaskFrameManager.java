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
import java.util.List;

/**
 * Manage the TaskFrame to display them on right of the window.
 * 
 * @author Fabrice de Chaumont & Stephane Dallongeville
 */
public class TaskFrameManager implements Runnable
{
    private static Dimension getDesktopSize()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

        if (mainFrame != null)
            // get bottom right border location
            return mainFrame.getDesktopSize();

        return null;
    }

    private static class TaskFrameInfo
    {
        final TaskFrame frame;
        Point position;
        long showDelay;
        long hideDelay;
        boolean visible;

        public TaskFrameInfo(TaskFrame frame, Point position, long showDelay, long hideDelay)
        {
            super();

            this.frame = frame;
            this.position = position;
            this.showDelay = showDelay;
            this.hideDelay = hideDelay;
            visible = false;
        }

        public boolean canHide()
        {
            return hideDelay < 0;
        }

        public boolean canShow()
        {
            return showDelay < 0;
        }
    }

    final Thread animThread;
    List<TaskFrameInfo> taskFrameInfos;
    long lastUpdateTime;

    /**
     * 
     */
    public TaskFrameManager()
    {
        super();

        taskFrameInfos = new ArrayList<TaskFrameInfo>();
        animThread = new Thread(this, "TaskFrame manager");
        lastUpdateTime = System.currentTimeMillis();
    }

    // we have to separate init as thread call the getMainInterface() method
    public void init()
    {
        animThread.start();
    }

    public void addTaskWindow(final TaskFrame tFrame, final long showDelay, final long hideDelay)
    {
        final Dimension desktopSize = getDesktopSize();

        if ((desktopSize != null) && !tFrame.canRemove())
        {
            // get bottom right border location
            final Point pos = new Point(desktopSize.width + 10, desktopSize.height);
            final TaskFrameInfo frameInfo = new TaskFrameInfo(tFrame, pos, showDelay, hideDelay);

            synchronized (taskFrameInfos)
            {
                taskFrameInfos.add(frameInfo);
            }
        }
    }

    public void addTaskWindow(final TaskFrame tFrame)
    {
        // we use a different default value for progress frame
        if (tFrame instanceof ProgressFrame)
            addTaskWindow(tFrame, 0L, 1000L);
        else
            addTaskWindow(tFrame, 0L, 0L);
    }

    @Override
    public void run()
    {
        while (true)
        {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;

            animateFrames(deltaTime);

            // sleep a bit
            ThreadUtil.sleep(20);
        }
    }

    void animateFrames(long delta)
    {
        // get bottom right border location
        final Dimension desktopSize = getDesktopSize();
        // not yet initialized
        if (desktopSize == null)
            return;

        List<TaskFrameInfo> list;

        // create temporary copy of the frame list
        synchronized (taskFrameInfos)
        {
            list = new ArrayList<TaskFrameInfo>(taskFrameInfos);
        }

        // process frames which will be closed
        for (int i = list.size() - 1; i >= 0; i--)
        {
            final TaskFrameInfo info = list.get(i);
            final TaskFrame frame = info.frame;

            info.showDelay -= delta;
            // close order
            if (frame.canRemove())
                info.hideDelay -= delta;

            // frame need to be removed ?
            if (info.canHide())
            {
                // frame hidden ?
                if (!info.visible || ((info.position.x >= desktopSize.width)))
                {
                    // remove it from list
                    list.remove(i);
                    // and close it definitely
                    frame.internalClose();
                }
            }
        }

        // calculate top Y position
        float currentY = desktopSize.height;
        int ind;
        for (ind = list.size() - 1; ind >= 0; ind--)
        {
            final TaskFrameInfo info = list.get(ind);

            if (info.canShow())
            {
                final int h = info.frame.getHeight();

                currentY -= h;
                // outside screen --> interrupt
                if (currentY < 0)
                {
                    currentY += h;
                    break;
                }
            }
        }

        // need to remove frame outside screen
        if (ind != -1)
        {
            for (int i = 0; i <= ind; i++)
            {
                final TaskFrameInfo info = list.get(i);
                // close the frame definitely
                info.frame.internalClose();
            }

            // remove frames from list
            list = list.subList(ind + 1, list.size());
        }

        // calculate and update all frames position
        for (TaskFrameInfo info : list)
        {
            final TaskFrame frame = info.frame;

            if (info.canShow())
            {
                int targetX;

                // find X target position
                if (info.canHide())
                    targetX = desktopSize.width + 20;
                else
                    targetX = desktopSize.width - frame.getWidth();

                final Point targetPos = new Point(targetX, (int) currentY);
                final Point curPos = info.position;

                float vectX = (targetPos.x - curPos.x) / 10f;
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
                float vectY = (targetPos.y - curPos.y) / 10f;
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

                // define new position
                final Point newPos = new Point((int) (curPos.x + vectX), (int) (curPos.y + vectY));

                // set Y when starting the scroll
                if (curPos.x > desktopSize.width)
                    newPos.y = targetPos.y;

                // update position
                info.position = newPos;

                // avoid repaint on JDesktopPane if position did not changed
                if (frame.isInternalized())
                {
                    if (!frame.getLocationInternal().equals(newPos))
                        frame.setLocationInternal(newPos);
                }
                else
                {
                    if (!frame.getLocationExternal().equals(newPos))
                        frame.setLocationExternal(newPos);
                }

                // frame need to be displayed now ?
                if (info.canShow() && !info.visible)
                {
                    // do set visible before adding to desktop pane so the frame does not take focus
                    frame.setVisible(true);
                    frame.addToDesktopPane();
                    frame.toFront();
                    info.visible = true;
                }

                currentY += frame.getHeight();
            }
        }

        // update global list
        taskFrameInfos = list;
    }
}

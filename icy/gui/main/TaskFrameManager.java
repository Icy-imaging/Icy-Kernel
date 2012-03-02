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
package icy.gui.main;

import icy.gui.frame.progress.ProgressFrame;
import icy.gui.frame.progress.TaskFrame;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Timer;

/**
 * Manage the TaskFrame to display them on right of the window.
 * 
 * @author Fabrice de Chaumont & Stephane Dallongeville
 */
public class TaskFrameManager implements ActionListener
{
    final Timer timer;
    final ArrayList<TaskFrame> taskFrames;
    final HashMap<TaskFrame, Point> framesPosition;
    final HashMap<TaskFrame, FrameInformation> framesInfo;
    long lastAnimationMillisecondTime;

    class FrameInformation
    {
        long msBeforeDisplay = 0;
        private long msBeforeClose = 0;
        boolean canBeRemoved = false;

        public FrameInformation(long msBeforeDisplay, long msBeforeClose)
        {
            this.msBeforeDisplay = msBeforeDisplay;
            this.msBeforeClose = msBeforeClose;
        }

        public long getMsBeforeClose()
        {
            return msBeforeClose;
        }

        public long getMsBeforeDisplay()
        {
            return msBeforeDisplay;
        }

        public void setMsBeforeClose(long msBeforeClose)
        {
            this.msBeforeClose = msBeforeClose;
            if (this.msBeforeClose <= 0)
            {
                canBeRemoved = true;
            }
        }

        public void setCanBeRemoved(boolean canBeRemoved)
        {
            this.canBeRemoved = canBeRemoved;
        }

        public boolean isCanBeRemoved()
        {
            return canBeRemoved;
        }

        public boolean displayOn()
        {
            if (msBeforeDisplay > 0)
                return false;
            return true;
        }
    }

    /**
     * 
     */
    public TaskFrameManager()
    {
        super();

        timer = new Timer(20, this);
        taskFrames = new ArrayList<TaskFrame>();
        framesPosition = new HashMap<TaskFrame, Point>();
        framesInfo = new HashMap<TaskFrame, FrameInformation>();
        lastAnimationMillisecondTime = System.currentTimeMillis();
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
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (taskFrames)
                {
                    final Dimension desktopSize = getDesktopSize();

                    if (desktopSize != null)
                    {
                        // get bottom right border location
                        FrameInformation frameInformation = new FrameInformation(msBeforeDisplay, msAfterCloseRequest);
                        framesInfo.put(tFrame, frameInformation);

                        taskFrames.add(tFrame);
                        tFrame.addToMainDesktopPane();
                        // tFrame.setVisible(true);
                        tFrame.setLocation(desktopSize.width + 10, 0);
                        tFrame.toFront();
                        lastAnimationMillisecondTime = System.currentTimeMillis();
                        timer.start();
                    }
                }
            }
        });
    }

    public void addTaskWindow(final TaskFrame tFrame, long msBeforeDisplay)
    {
        addTaskWindow(tFrame, msBeforeDisplay, 0);
    }

    public void addTaskWindow(final TaskFrame tFrame)
    {
        // we use a different default value for progress frame
        if (tFrame instanceof ProgressFrame)
            addTaskWindow(tFrame, 0, 1000);
        else
            addTaskWindow(tFrame, 0, 0);
    }

    private void animate()
    {
        synchronized (taskFrames)
        {
            long currentMillisecondTime = System.currentTimeMillis();
            long delayBetween2Animation = currentMillisecondTime - lastAnimationMillisecondTime;
            lastAnimationMillisecondTime = currentMillisecondTime;

            for (TaskFrame frame : framesInfo.keySet())
            {
                FrameInformation frameInformation = framesInfo.get(frame);
                frameInformation.msBeforeDisplay -= delayBetween2Animation;

                if (frameInformation.msBeforeDisplay <= 0)
                    frame.setVisible(true);

                if (frame.canRemove())
                    frameInformation.setMsBeforeClose(frameInformation.getMsBeforeClose() - delayBetween2Animation);
            }

            // build target position.
            framesPosition.clear();
            // get bottom right border location
            final Dimension desktopSize = getDesktopSize();

            final ArrayList<TaskFrame> frames = new ArrayList<TaskFrame>(taskFrames);

            for (TaskFrame frame : frames)
            {
                FrameInformation frameInformation = framesInfo.get(frame);

                // get frame position
                final Point location = frame.getLocation();

                // frame need to be removed ?
                if (frameInformation.canBeRemoved)
                {
                    if ((!frame.isVisible()) || ((location.x > desktopSize.width)))
                    {
                        // remove it from list
                        taskFrames.remove(frame);
                        framesInfo.remove(frame);
                        // and close it definitely
                        frame.internalClose();
                    }
                }
            }

            // calculate current Y position
            float currentY = desktopSize.height;
            for (TaskFrame frame : taskFrames)
            {
                FrameInformation frameInformation = framesInfo.get(frame);

                if (frameInformation != null && frameInformation.displayOn())
                    currentY -= frame.getHeight();
            }

            for (TaskFrame frame : taskFrames)
            {
                FrameInformation frameInformation = framesInfo.get(frame);

                if (frameInformation != null && frameInformation.displayOn())
                {
                    int xTarget = desktopSize.width - frame.getWidth();

                    if (frameInformation.canBeRemoved)
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

                    framesPosition.put(frame, positionNew);

                    // no enough space to display frame, close it
                    if (positionNew.y < 0)
                        frame.close();

                    currentY += frame.getHeight();
                }
            }

            for (TaskFrame frame : taskFrames)
            {
                final Point location = framesPosition.get(frame);

                if (location != null)
                {
                    // avoid repaint on JDesktopPane if position did not changed
                    if (!frame.getLocationInternal().equals(location))
                        frame.setLocationInternal(location);
                    if (!frame.getLocationExternal().equals(location))
                        frame.setLocationExternal(location);
                }
            }
        }

        if (taskFrames.size() == 0)
            timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        animate();
    }
}

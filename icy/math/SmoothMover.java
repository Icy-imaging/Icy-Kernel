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
package icy.math;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

/**
 * @author Stephane
 */
public class SmoothMover implements ActionListener
{
    public static interface SmoothMoverListener
    {
        public void moveStarted(SmoothMover source, double start, double end);

        public void moveModified(SmoothMover source, double start, double end);

        public void moveEnded(SmoothMover source, double value);

        public void valueChanged(SmoothMover source, double newValue, int pourcent);
    }

    public static class SmoothMoverAdapter implements SmoothMoverListener
    {
        @Override
        public void moveStarted(SmoothMover source, double start, double end)
        {
        }

        @Override
        public void moveModified(SmoothMover source, double start, double end)
        {
        }

        @Override
        public void moveEnded(SmoothMover source, double value)
        {
        }

        @Override
        public void valueChanged(SmoothMover source, double newValue, int pourcent)
        {
        }
    }

    public enum SmoothMoveType
    {
        NONE, LINEAR, LOG, EXP
    };

    /**
     * current value
     */
    private double currentValue;
    /**
     * smooth movement type
     */
    private SmoothMoveType type;
    /**
     * time to do move (in ms)
     */
    private int moveTime;

    /**
     * internals
     */
    private final Timer timer;
    private double destValue;
    private double[] stepValues;
    // private int stepIndex;
    private long startTime;
    private final ArrayList<SmoothMoverListener> listeners;

    public SmoothMover(double initValue, SmoothMoveType type)
    {
        super();

        currentValue = initValue;
        destValue = initValue;

        this.type = type;
        // 60 updates per second by default
        timer = new Timer(1000 / 60, this);
        // no initial delay
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        // default : 1 second to reach destination
        moveTime = 1000;
        // default
        stepValues = new double[0];

        listeners = new ArrayList<SmoothMoverListener>();
    }

    public SmoothMover(double initValue)
    {
        this(initValue, SmoothMoveType.LINEAR);
    }

    /**
     * Move to specified values v
     */
    public void moveTo(double value)
    {
        if (destValue != value)
        {
            destValue = value;
            // start movement
            start();
        }
    }

    public boolean isMoving()
    {
        return timer.isRunning();
    }

    private void start()
    {
        // number of step to reach final value
        final int size = Math.max(moveTime / timer.getDelay(), 1);

        // calculate interpolation
        switch (type)
        {
            case NONE:
                stepValues = new double[2];
                stepValues[0] = currentValue;
                stepValues[1] = destValue;
                break;

            case LINEAR:
                stepValues = Interpolator.doLinearInterpolation(currentValue, destValue, size);
                break;

            case LOG:
                stepValues = Interpolator.doLogInterpolation(currentValue, destValue, size);
                break;

            case EXP:
                stepValues = Interpolator.doExpInterpolation(currentValue, destValue, size);
                break;
        }

        // initialize index to 1 (ignore value 0 which is current value)
        // stepIndex = 1;

        if (!isMoving())
        {
            // notify and start
            moveStarted();
            timer.start();
        }
        else
        {
            // update current value
            // updateCurrentValue();
            // notify and restart
            moveModified();
            // timer.restart();
        }
    }

    public void stop()
    {
        if (isMoving())
        {
            // stop and notify
            timer.stop();
            moveEnded();
        }
    }

    /**
     * Shutdown the mover object (this actually stop internal timer and remove all listeners)
     */
    public void shutDown()
    {
        timer.stop();
        timer.removeActionListener(this);
        listeners.clear();
    }

    /**
     * @return the update delay (in ms)
     */
    public int getUpdateDelay()
    {
        return timer.getDelay();
    }

    /**
     * @param updateDelay
     *        the update delay (in ms) to set
     */
    public void setUpdateDelay(int updateDelay)
    {
        timer.setDelay(updateDelay);
    }

    /**
     * @return the smooth type
     */
    public SmoothMoveType getType()
    {
        return type;
    }

    /**
     * @param type
     *        the smooth type to set
     */
    public void setType(SmoothMoveType type)
    {
        this.type = type;
    }

    /**
     * @return the moveTime
     */
    public int getMoveTime()
    {
        return moveTime;
    }

    /**
     * @param moveTime
     *        the moveTime to set
     */
    public void setMoveTime(int moveTime)
    {
        // can't be < 1
        this.moveTime = Math.max(moveTime, 1);
    }

    /**
     * Immediately set the value
     */
    public void setValue(double value)
    {
        // stop current movement
        stop();
        // directly set value
        destValue = value;
        setCurrentValue(value, 100);
    }

    /**
     * @return the value
     */
    public double getValue()
    {
        return currentValue;
    }

    /**
     * @return the destValue
     */
    public double getDestValue()
    {
        return destValue;
    }

    public void addListener(SmoothMoverListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(SmoothMoverListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Move started event
     */
    private void moveStarted()
    {
        startTime = System.currentTimeMillis();

        for (SmoothMoverListener listener : listeners)
            listener.moveStarted(this, currentValue, destValue);
    }

    /**
     * Move modified event
     */
    private void moveModified()
    {
        startTime = System.currentTimeMillis();

        for (SmoothMoverListener listener : listeners)
            listener.moveModified(this, currentValue, destValue);
    }

    /**
     * Move ended event
     */
    private void moveEnded()
    {
        for (SmoothMoverListener listener : listeners)
            listener.moveEnded(this, currentValue);
    }

    /**
     * update current value from elapsed time
     */
    private void updateCurrentValue()
    {
        final int elapsedMsTime = (int) (System.currentTimeMillis() - startTime);

        // move completed ?
        if ((type == SmoothMoveType.NONE) || (elapsedMsTime >= moveTime))
        {
            setCurrentValue(destValue, 100);
            // stop
            stop();
        }
        else
        {
            final int len = stepValues.length;
            final int ind = Math.min((elapsedMsTime * len) / moveTime, len - 2);
            // set value
            setCurrentValue(stepValues[ind + 1], (elapsedMsTime * 100) / moveTime);
        }
    }

    private void setCurrentValue(double value, int pourcent)
    {
        if (currentValue != value)
        {
            currentValue = value;
            // notify value changed
            changed(value, pourcent);
        }
    }

    /**
     * Value changed event
     * 
     * @param oldValue
     * @param newValue
     * @param i
     */
    private void changed(double newValue, int pourcent)
    {
        for (SmoothMoverListener listener : listeners)
            listener.valueChanged(this, newValue, pourcent);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // process only if timer running
        if (isMoving())
            updateCurrentValue();
    }
}

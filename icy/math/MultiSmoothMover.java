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
package icy.math;

import icy.math.SmoothMover.SmoothMoveType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

/**
 * @author Stephane
 */
public class MultiSmoothMover implements ActionListener
{
    public static interface MultiSmoothMoverListener
    {
        public void moveStarted(MultiSmoothMover source, int index, double start, double end);

        public void moveModified(MultiSmoothMover source, int index, double start, double end);

        public void moveEnded(MultiSmoothMover source, int index, double value);

        public void valueChanged(MultiSmoothMover source, int index, double newValue, int pourcent);
    }

    public static class MultiSmoothMoverAdapter implements MultiSmoothMoverListener
    {
        @Override
        public void moveStarted(MultiSmoothMover source, int index, double start, double end)
        {
        }

        @Override
        public void moveModified(MultiSmoothMover source, int index, double start, double end)
        {
        }

        @Override
        public void moveEnded(MultiSmoothMover source, int index, double value)
        {
        }

        @Override
        public void valueChanged(MultiSmoothMover source, int index, double newValue, int pourcent)
        {
        }
    }

    /**
     * current value
     */
    protected double[] currentValues;
    /**
     * smooth movement type
     */
    protected SmoothMoveType type;
    /**
     * time to do move (in ms)
     */
    protected int moveTime;

    /**
     * internals
     */
    protected final Timer timer;
    protected boolean[] moving;
    protected double[] destValues;
    protected double[][] stepValues;
    // private int stepIndex;
    protected long[] startTime;
    protected final ArrayList<MultiSmoothMoverListener> listeners;

    public MultiSmoothMover(int size, SmoothMoveType type)
    {
        super();

        currentValues = new double[size];
        moving = new boolean[size];
        destValues = new double[size];
        startTime = new long[size];

        this.type = type;
        // 60 updates per second by default
        timer = new Timer(1000 / 60, this);
        // no initial delay
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        // timer always running here
        timer.start();
        // default : 1 second to reach destination
        moveTime = 1000;
        // default
        stepValues = new double[size][0];

        listeners = new ArrayList<MultiSmoothMoverListener>();
    }

    public MultiSmoothMover(int size)
    {
        this(size, SmoothMoveType.LINEAR);
    }

    /**
     * Move the value at specified index to 'value'
     */
    public void moveTo(int index, double value)
    {
        if (destValues[index] != value)
        {
            destValues[index] = value;
            // start movement
            start(index, System.currentTimeMillis());
        }
    }

    /**
     * Move all values
     */
    public void moveTo(double[] values)
    {
        final int maxInd = Math.min(values.length, destValues.length);

        // first we check we have at least one value which had changed
        boolean changed = false;
        for (int index = 0; index < maxInd; index++)
        {
            if (destValues[index] != values[index])
            {
                changed = true;
                break;
            }
        }

        // value changed ?
        if (changed)
        {
            // better synchronization for multiple changes
            final long time = System.currentTimeMillis();

            for (int index = 0; index < maxInd; index++)
            {
                destValues[index] = values[index];
                // start movement
                start(index, time);
            }
        }
    }

    public boolean isMoving(int index)
    {
        return moving[index];
    }

    public boolean isMoving()
    {
        for (boolean b : moving)
            if (b)
                return true;

        return false;
    }

    protected void start(int index, long time)
    {
        final double current = currentValues[index];
        final double dest = destValues[index];

        // number of step to reach final value
        final int size = Math.max(moveTime / timer.getDelay(), 1);

        // calculate interpolation
        switch (type)
        {
            case NONE:
                stepValues[index] = new double[2];
                stepValues[index][0] = current;
                stepValues[index][1] = dest;
                break;

            case LINEAR:
                stepValues[index] = Interpolator.doLinearInterpolation(current, dest, size);
                break;

            case LOG:
                stepValues[index] = Interpolator.doLogInterpolation(current, dest, size);
                break;

            case EXP:
                stepValues[index] = Interpolator.doExpInterpolation(current, dest, size);
                break;
        }

        // notify and start
        if (!isMoving(index))
        {
            moveStarted(index, time);
            moving[index] = true;
        }
        else
            moveModified(index, time);
    }

    /**
     * Stop specified index
     */
    public void stop(int index)
    {
        // stop and notify
        if (isMoving(index))
        {
            moving[index] = false;
            moveEnded(index);
        }
    }

    /**
     * Stop all
     */
    public void stopAll()
    {
        // stop all
        for (int index = 0; index < moving.length; index++)
            if (moving[index])
                moveEnded(index);
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
    public void setValue(int index, double value)
    {
        // stop current movement
        stop(index);
        // directly set value
        destValues[index] = value;
        setCurrentValue(index, value, 100);
    }

    /**
     * Immediately set all values
     */
    public void setValues(double[] values)
    {
        final int maxInd = Math.min(values.length, destValues.length);

        for (int index = 0; index < maxInd; index++)
        {
            final double value = values[index];
            // stop current movement
            stop(index);
            // directly set value
            destValues[index] = value;
            setCurrentValue(index, value, 100);
        }
    }

    /**
     * @return the value
     */
    public double getValue(int index)
    {
        return currentValues[index];
    }

    /**
     * @return the destValue
     */
    public double getDestValue(int index)
    {
        return destValues[index];
    }

    /**
     * update current value from elapsed time
     */
    protected void updateCurrentValue(int index, long time)
    {
        final int elapsedMsTime = (int) (time - startTime[index]);

        // move completed ?
        if ((type == SmoothMoveType.NONE) || (elapsedMsTime >= moveTime))
        {
            setCurrentValue(index, destValues[index], 100);
            // stop
            stop(index);
        }
        else
        {
            final int len = stepValues[index].length;
            final int ind = Math.min((elapsedMsTime * len) / moveTime, len - 2) + 1;

            // set value
            if ((ind >= 0) && (ind < stepValues[index].length))
                setCurrentValue(index, stepValues[index][ind], (elapsedMsTime * 100) / moveTime);
        }
    }

    protected void setCurrentValue(int index, double value, int pourcent)
    {
        if (currentValues[index] != value)
        {
            currentValues[index] = value;
            // notify value changed
            changed(index, value, pourcent);
        }
    }

    public void addListener(MultiSmoothMoverListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(MultiSmoothMoverListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Move started event
     */
    protected void moveStarted(int index, long time)
    {
        startTime[index] = time;

        for (MultiSmoothMoverListener listener : listeners)
            listener.moveStarted(this, index, currentValues[index], destValues[index]);
    }

    /**
     * Move modified event.
     */
    protected void moveModified(int index, long time)
    {
        startTime[index] = time;

        for (MultiSmoothMoverListener listener : listeners)
            listener.moveModified(this, index, currentValues[index], destValues[index]);
    }

    /**
     * Move ended event.
     */
    protected void moveEnded(int index)
    {
        for (MultiSmoothMoverListener listener : listeners)
            listener.moveEnded(this, index, currentValues[index]);
    }

    /**
     * Value changed event.
     */
    protected void changed(int index, double newValue, int pourcent)
    {
        for (MultiSmoothMoverListener listener : listeners)
            listener.valueChanged(this, index, newValue, pourcent);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // better synchronization for multiple changes
        final long time = System.currentTimeMillis();

        // process only moving values
        for (int index = 0; index < moving.length; index++)
            if (moving[index])
                updateCurrentValue(index, time);
    }

}

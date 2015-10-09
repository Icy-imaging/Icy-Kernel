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

/**
 * Uses this class to maintain and calculate a rate.
 * 
 * @author stephane
 */
public class RateMeter
{
    private double lastCnt;
    private double rate;
    private double rateCnt;
    private double lastRateCnt;

    public RateMeter()
    {
        super();

        reset();
    }

    /**
     * Reset counter
     */
    public void reset()
    {
        lastCnt = System.nanoTime();
        rate = 0;
        rateCnt = 0;
        lastRateCnt = 0;
    }

    /**
     * Update rate from delta<br>
     * Return current rate
     */
    public double updateFromDelta(double delta)
    {
        return updateFromTotal(rateCnt + delta);
    }

    /**
     * Update rate from total<br>
     * Return current rate
     */
    public double updateFromTotal(double total)
    {
        final double curCnt = System.nanoTime();
        final double difCnt = curCnt - lastCnt;

        rateCnt = total;

        if (difCnt > 1000000000d)
        {
            lastCnt = curCnt;
            rate = rateCnt - lastRateCnt;
            rate *= 1000000000d;
            rate /= difCnt;
            lastRateCnt = rateCnt;
        }

        return rate;
    }

    /**
     * @return rate
     */
    public double getRate()
    {
        return rate;
    }
}

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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stephane
 */
public class Interpolator
{
    private static double[][] pointsToXY(List<Point> points)
    {
        final int len = points.size();
        final double[][] xy = new double[2][len];
        final double[] x = xy[0];
        final double[] y = xy[1];

        for (int i = 0; i < len; i++)
        {
            final Point p = points.get(i);
            x[i] = p.x;
            y[i] = p.y;
        }

        return xy;
    }

    private static double[] prepareYInterpolation(double[] x, double[] y, double xinc)
    {
        if ((x.length == 0) || (y.length == 0))
            throw new IllegalArgumentException("x[] and y[] should not be empty.");
        if (x.length != y.length)
            throw new IllegalArgumentException("x[] and y[] should have the same length.");
        if (xinc == 0)
            throw new IllegalArgumentException("step must be > 0");

        return new double[(int) ((x[x.length - 1] - x[0]) / xinc) + 1];
    }

    /**
     * Return Y linear interpolated coordinates from specified points and given X increment
     */
    public static double[] doYLinearInterpolation(List<Point> points, double xinc)
    {
        final double[][] xy = pointsToXY(points);
        return doYLinearInterpolation(xy[0], xy[1], xinc);
    }

    /**
     * Return Y linear interpolated coordinates from specified points and given X increment
     */
    public static double[] doYLinearInterpolation(double[] x, double[] y, double xinc)
    {
        final double[] result = prepareYInterpolation(x, y, xinc);
        final int len = result.length;

        if (len == 1)
            result[0] = x[0];
        else
        {
            final int xlen = x.length - 1;
            int index = 0;
            int offset = 0;
            double xvalue = x[0];
            double yvalue = y[0];
            double yinc = 0;

            while (offset < len)
            {
                while ((index < xlen) && (xvalue >= x[index]))
                {
                    index++;
                    final double dx = x[index] - xvalue;

                    if (dx != 0)
                        yinc = (y[index] - yvalue) / dx;
                    else
                        yinc = 0;
                }

                result[offset++] = yvalue;
                yvalue += yinc;
                xvalue += xinc;
            }
        }

        return result;
    }

    /**
     * Return Y spline interpolated coordinates from specified points and given X increment
     */
    public static double[] doYSplineInterpolation(ArrayList<Point> points, double xstep)
    {
        final double[][] xy = pointsToXY(points);
        return doYSplineInterpolation(xy[0], xy[1], xstep);
    }

    /**
     * Return Y spline interpolated coordinates from specified points and given X increment.<br>
     * Not yet implemented !
     */
    public static double[] doYSplineInterpolation(double[] x, double[] y, double xstep)
    {
        final double[] result = prepareYInterpolation(x, y, xstep);
        final int len = result.length;

        if (len > 1)
        {

        }

        return result;
    }

    /**
     * Do linear interpolation from start to end with specified increment step
     */
    public static double[] doLinearInterpolation(double start, double end, double step)
    {
        int size;

        if (step == 0)
            size = 1;
        else
            size = (int) ((end - start) / step) + 1;

        // size should be at least 1
        if (size < 1)
            size = 1;

        final double[] result = new double[size];

        double value = start;
        for (int i = 0; i < size; i++)
        {
            result[i] = value;
            value += step;
        }

        return result;
    }

    /**
     * Do linear interpolation from start to end with specified size (step number)
     */
    public static double[] doLinearInterpolation(double start, double end, int size)
    {
        if (size < 1)
            return null;

        // special case
        if (size == 1)
        {
            final double[] result = new double[size];
            result[0] = end;
            return result;
        }

        return doLinearInterpolation(start, end, (end - start) / (size - 1));
    }

    /**
     * Do logarithmic interpolation from start to end with specified size (step number)
     */
    public static double[] doLogInterpolation(double start, double end, int size)
    {
        // get linear interpolation
        final double[] result = doLinearInterpolation(start, end, size);

        // define input and output scaler
        final Scaler scalerIn = new Scaler(start, end, 2, 20, true, true);
        final Scaler scalerOut = new Scaler(Math.log(2), Math.log(20), start, end, true, true);

        final int len = result.length;

        // log scaling
        for (int i = 0; i < len; i++)
            result[i] = scalerOut.scale(Math.log(scalerIn.scale(result[i])));

        return result;
    }

    /**
     * Do exponential interpolation from start to end with specified size (step number)
     */
    public static double[] doExpInterpolation(double start, double end, int size)
    {
        // get linear interpolation
        final double[] result = doLinearInterpolation(start, end, size);

        // define input and output scaler
        final Scaler scalerIn = new Scaler(start, end, 0, 2, false, true);
        final Scaler scalerOut = new Scaler(Math.exp(0), Math.exp(2), start, end, false, true);

        final int len = result.length;

        // exp scaling
        for (int i = 0; i < len; i++)
            result[i] = scalerOut.scale(Math.exp(scalerIn.scale(result[i])));

        return result;
    }

}

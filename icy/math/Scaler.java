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

import icy.common.CollapsibleEvent;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.file.xml.XMLPersistent;
import icy.type.TypeUtil;
import icy.type.collection.array.ArrayUtil;
import icy.util.XMLUtil;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

/**
 * @author stephane
 */
public class Scaler implements ChangeListener, XMLPersistent
{
    private enum ScalerRange
    {
        SR_ABSIN, SR_IN, SR_OUT
    };

    private static final String ID_ABSLEFTIN = "absleftin";
    private static final String ID_ABSRIGHTIN = "absrightin";
    private static final String ID_LEFTIN = "leftin";
    private static final String ID_RIGHTIN = "rightin";
    private static final String ID_LEFTOUT = "leftout";
    private static final String ID_RIGHTOUT = "rightout";
    private static final String ID_INTEGERDATA = "integerdata";
    private static final String ID_CANCROSS = "cancross";

    private double absLeftIn;
    private double absRightIn;

    private double leftIn;
    private double rightIn;

    private double leftOut;
    private double rightOut;

    private double scaler;
    private double unscaler;

    private boolean integerData;
    private boolean canCross;
    private boolean crossed;

    public double scaleLK[];

    private final EventListenerList listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    public static int indexOf(Scaler[] scalers, Scaler scaler)
    {
        for (int i = 0; i < scalers.length; i++)
            if (scalers[i].equals(scaler))
                return i;

        return -1;
    }

    public static boolean contains(Scaler[] scalers, Scaler scaler)
    {
        return (indexOf(scalers, scaler) != -1);
    }

    /**
     * 
     */
    public Scaler(double leftIn, double rightIn, double leftOut, double rightOut, boolean integerData)
    {
        this(leftIn, rightIn, leftIn, rightIn, leftOut, rightOut, integerData, false);
    }

    /**
     * 
     */
    public Scaler(double leftIn, double rightIn, double leftOut, double rightOut, boolean integerData, boolean canCross)
    {
        this(leftIn, rightIn, leftIn, rightIn, leftOut, rightOut, integerData, canCross);
    }

    /**
     * 
     */
    public Scaler(double absLeftIn, double absRightIn, double leftIn, double rightIn, double leftOut, double rightOut,
            boolean integerData, boolean canCross)
    {
        super();

        this.absLeftIn = absLeftIn;
        this.absRightIn = absRightIn;
        this.leftIn = leftIn;
        this.rightIn = rightIn;
        this.leftOut = leftOut;
        this.rightOut = rightOut;
        this.integerData = integerData;
        this.canCross = canCross;

        crossed = absLeftIn > absRightIn;

        if (crossed && !canCross)
            throw new IllegalArgumentException("Can't create scaler : left > right and canCross = false");

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // update scaler
        updateScaler(false);
    }

    /**
     * Refresh the scale lookup table
     */
    private void updateLookup()
    {
        scaleLK = null;

        if (integerData)
        {
            final boolean rangeOk;

            if (crossed)
                rangeOk = (absLeftIn <= 65535) && (absRightIn >= 0);
            else
                rangeOk = (absLeftIn >= 0) && (absRightIn <= 65535);

            // use lookup table only for integer scalar value in [0..65535] range
            if (rangeOk)
            {
                final int len;

                if (crossed)
                    len = (int) absRightIn;
                else
                    len = (int) absLeftIn;

                scaleLK = new double[len];

                // refresh lookup table data
                for (int i = 0; i < len; i++)
                    scaleLK[i] = scale(i);
            }
        }
    }

    /**
     * Refresh the scaler value
     */
    private void updateScaler(boolean notify)
    {
        final double deltaIn = rightIn - leftIn;
        final double deltaOut = rightOut - leftOut;

        // delta null
        if ((deltaIn == 0) || (deltaOut == 0))
            scaler = 1;
        else
        {
            scaler = deltaOut / deltaIn;
            unscaler = deltaIn / deltaOut;
        }

        // refresh lookup table
        updateLookup();

        // notify scaler changed
        if (notify)
            changed();
    }

    private void checkBounds()
    {
        double l = leftIn;
        double r = rightIn;

        if (crossed)
        {
            // check absolute range first
            if (l > absLeftIn)
            {
                l = absLeftIn;
                if (r > l)
                    r = l - Float.MIN_VALUE;
            }
            if (r < absRightIn)
            {
                r = absRightIn;
                if (l < r)
                    l = r + Float.MIN_VALUE;
            }
        }
        else
        {
            // check absolute range first
            if (l < absLeftIn)
            {
                l = absLeftIn;
                if (r < l)
                    r = l + Float.MIN_VALUE;
            }
            if (r > absRightIn)
            {
                r = absRightIn;
                if (l > r)
                    l = r - Float.MIN_VALUE;
            }
        }

        // set left and right for input value
        leftIn = l;
        rightIn = r;
    }

    /**
     * Sets the left and right value of specified range.
     * 
     * @param left
     *        the new left value to set
     * @param right
     *        the new right value to set
     * @param range
     *        range to modify
     * @param leftPrio
     *        priority to left border (to resolve conflict)
     */
    private void setLeftRight(double left, double right, ScalerRange range, boolean leftPrio)
    {
        double l = left;
        double r = right;

        if ((!canCross) && (l > r))
        {
            if (leftPrio)
                r = l + Float.MIN_VALUE;
            else
                l = r - Float.MIN_VALUE;
        }

        switch (range)
        {
            case SR_ABSIN:
                // nothing to do
                if ((absLeftIn == l) && (absRightIn == r))
                    return;

                // update crossed information
                crossed = l > r;
                // set absolute left and right for input value
                absLeftIn = l;
                absRightIn = r;
                // adjust current left and right for input value if they are out bounds
                checkBounds();
                break;

            case SR_IN:
                // nothing to do
                if ((leftIn == l) && (rightIn == r))
                    return;

                // update crossed information updated only on absolute
                // crossed = l > r;
                // set left and right for input value
                leftIn = l;
                rightIn = r;
                // adjust current left and right for input value if they are out bounds
                checkBounds();
                break;

            case SR_OUT:
                // nothing to do
                if ((leftOut == l) && (rightOut == r))
                    return;

                // set left and right for output value
                leftOut = l;
                rightOut = r;
                break;
        }

        // update scaler
        updateScaler(true);
    }

    /**
     * Scale the value
     * 
     * @param value
     *        value to scale
     * @return scaled output value
     */
    public double scale(double value)
    {
        if (crossed)
        {
            if (value >= leftIn)
                return leftOut;
            else if (value <= rightIn)
                return rightOut;
            else
                return ((value - leftIn) * scaler) + leftOut;
        }

        if (value <= leftIn)
            return leftOut;
        else if (value >= rightIn)
            return rightOut;
        else
            return ((value - leftIn) * scaler) + leftOut;
    }

    /**
     * Scale the value
     * 
     * @param value
     *        value to scale
     * @return scaled output value
     */
    public double unscale(double value)
    {
        if (crossed)
        {
            if (value >= leftOut)
                return leftIn;
            else if (value <= rightOut)
                return rightIn;
            else
                return ((value - leftOut) * unscaler) + leftIn;
        }

        if (value <= leftOut)
            return leftIn;
        else if (value >= rightOut)
            return rightIn;
        else
            return ((value - leftOut) * unscaler) + leftIn;
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of byte (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(Object src, int srcOffset, int[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        switch (ArrayUtil.getDataType(src))
        {
            case BYTE:
                scale((byte[]) src, srcOffset, dest, destOffset, len, signed);
                break;

            case SHORT:
                scale((short[]) src, srcOffset, dest, destOffset, len, signed);
                break;

            case INT:
                scale((int[]) src, srcOffset, dest, destOffset, len, signed);
                break;

            case LONG:
                scale((long[]) src, srcOffset, dest, destOffset, len, signed);
                break;

            case FLOAT:
                scale((float[]) src, srcOffset, dest, destOffset, len);
                break;

            case DOUBLE:
                scale((double[]) src, srcOffset, dest, destOffset, len);
                break;
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of byte (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(byte[] src, int srcOffset, int[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of short (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(short[] src, int srcOffset, int[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of int (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(int[] src, int srcOffset, int[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of long (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(long[] src, int srcOffset, int[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = (int) scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of float (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     */
    public void scale(float[] src, int srcOffset, int[] dest, int destOffset, int len)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        for (int i = 0; i < len; i++)
            dest[destOffset + i] = (int) scale(src[srcOffset + i]);
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of double (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of int (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     */
    public void scale(double[] src, int srcOffset, int[] dest, int destOffset, int len)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        for (int i = 0; i < len; i++)
            dest[destOffset + i] = (int) scale(src[srcOffset + i]);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of byte (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of double (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(byte[] src, int srcOffset, double[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of short (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of double (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(short[] src, int srcOffset, double[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of int (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of double (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(int[] src, int srcOffset, double[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of long (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of double (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(long[] src, int srcOffset, double[] dest, int destOffset, int len, boolean signed)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        if (signed)
        {
            // signed
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(src[srcOffset + i]);
        }
        else
        {
            // unsigned
            for (int i = 0; i < len; i++)
                dest[destOffset + i] = scale(TypeUtil.unsign(src[srcOffset + i]));
        }
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of float (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of double (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     */
    public void scale(float[] src, int srcOffset, double[] dest, int destOffset, int len)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        for (int i = 0; i < len; i++)
            dest[destOffset + i] = scale(src[srcOffset + i]);
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of double (unscaled values)
     * @param srcOffset
     *        offset for src buffer
     * @param dest
     *        result as array of double (scaled values)
     * @param destOffset
     *        offset for dest buffer
     * @param len
     *        length to compute
     */
    public void scale(double[] src, int srcOffset, double[] dest, int destOffset, int len)
    {
        if ((src == null) || (dest == null))
            throw new IllegalArgumentException("Parameters 'src' and 'dest' should not be null !");

        for (int i = 0; i < len; i++)
            dest[destOffset + i] = scale(src[srcOffset + i]);
    }

    /**
     * Scale array
     * 
     * @param data
     *        array of float value to scale
     * @param offset
     *        offset for buffer
     * @param len
     *        length to compute
     */
    public void scale(float[] data, int offset, int len)
    {
        if (data == null)
            throw new IllegalArgumentException("Parameters 'data' should not be null !");

        for (int i = 0; i < len; i++)
            data[offset + i] = (float) scale(data[i]);
    }

    /**
     * Scale array
     * 
     * @param data
     *        array of double value to scale
     * @param offset
     *        offset for buffer
     * @param len
     *        length to compute
     */
    public void scale(double[] data, int offset, int len)
    {
        if (data == null)
            throw new IllegalArgumentException("Parameters 'data' should not be null !");

        for (int i = 0; i < len; i++)
            data[offset + i] = scale(data[i]);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of byte (unscaled values)
     * @param dest
     *        result as array of int (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(Object src, int[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of byte (unscaled values)
     * @param dest
     *        result as array of int (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(byte[] src, int[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of short (unscaled values)
     * @param dest
     *        result as array of int (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(short[] src, int[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of int (unscaled values)
     * @param dest
     *        result as array of int (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(int[] src, int[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of float (unscaled values)
     * @param dest
     *        result as array of int (scaled values)
     */
    public void scale(float[] src, int[] dest)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src));
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of double (unscaled values)
     * @param dest
     *        result as array of int (scaled values)
     */
    public void scale(double[] src, int[] dest)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src));
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of byte (unscaled values)
     * @param dest
     *        result as array of double (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(byte[] src, double[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of short (unscaled values)
     * @param dest
     *        result as array of double (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(short[] src, double[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale each value in the "src" array and return result in "dest" array
     * 
     * @param src
     *        array of int (unscaled values)
     * @param dest
     *        result as array of double (scaled values)
     * @param signed
     *        signed/unsigned src data flag
     */
    public void scale(int[] src, double[] dest, boolean signed)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src), signed);
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of float (unscaled values)
     * @param dest
     *        result as array of double (scaled values)
     */
    public void scale(float[] src, double[] dest)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src));
    }

    /**
     * Scale array
     * 
     * @param src
     *        array of double (unscaled values)
     * @param dest
     *        result as array of double (scaled values)
     */
    public void scale(double[] src, double[] dest)
    {
        scale(src, 0, dest, 0, ArrayUtil.getTotalLength(src));
    }

    /**
     * Scale array
     * 
     * @param data
     *        array of float value to scale
     */
    public void scale(float[] data)
    {
        scale(data, 0, data.length);
    }

    /**
     * Scale array
     * 
     * @param data
     *        array of double value to scale
     */
    public void scale(double[] data)
    {
        scale(data, 0, data.length);
    }

    /**
     * Return the scaler value
     * 
     * @return the scaler value
     */
    public double getScaler()
    {
        return scaler;
    }

    /**
     * @return the integerData
     */
    public boolean isIntegerData()
    {
        return integerData;
    }

    /**
     * @return the crossed flag
     */
    public boolean isCrossed()
    {
        return crossed;
    }

    /**
     * Return true if scaler doesn't change value (input = output)
     */
    public boolean isNull()
    {
        return (leftIn == leftOut) && (rightIn == rightOut);
    }

    /**
     * @return the canCross
     */
    public boolean getCanCross()
    {
        return canCross;
    }

    /**
     * @param canCross
     *        the canCross to set
     */
    public void setCanCross(boolean canCross)
    {
        if (this.canCross != canCross)
        {
            // crossed and we can't anymore...
            if (!canCross && crossed)
            {
                final double ali = absLeftIn;
                final double ari = absRightIn;
                final double li = leftIn;
                final double ri = rightIn;

                // uncross
                setLeftRight(ari, ali, ScalerRange.SR_ABSIN, true);
                setLeftRight(ri, li, ScalerRange.SR_IN, true);
            }

            this.canCross = canCross;
        }
    }

    /**
     * @return the absLeftIn
     */
    public double getAbsLeftIn()
    {
        return absLeftIn;
    }

    /**
     * @param absLeftIn
     *        the absLeftIn to set
     */
    public void setAbsLeftIn(double absLeftIn)
    {
        setLeftRight(absLeftIn, absRightIn, ScalerRange.SR_ABSIN, true);
    }

    /**
     * @return the absRightIn
     */
    public double getAbsRightIn()
    {
        return absRightIn;
    }

    /**
     * @param absRightIn
     *        the absRightIn to set
     */
    public void setAbsRightIn(double absRightIn)
    {
        setLeftRight(absLeftIn, absRightIn, ScalerRange.SR_ABSIN, false);
    }

    /**
     * @return the leftIn
     */
    public double getLeftIn()
    {
        return leftIn;
    }

    /**
     * @param leftIn
     *        the leftIn to set
     */
    public void setLeftIn(double leftIn)
    {
        setLeftRight(leftIn, rightIn, ScalerRange.SR_IN, true);
    }

    /**
     * @return the rightIn
     */
    public double getRightIn()
    {
        return rightIn;
    }

    /**
     * @param rightIn
     *        the rightIn to set
     */
    public void setRightIn(double rightIn)
    {
        setLeftRight(leftIn, rightIn, ScalerRange.SR_IN, false);
    }

    /**
     * @return the leftOut
     */
    public double getLeftOut()
    {
        return leftOut;
    }

    /**
     * @param leftOut
     *        the leftOut to set
     */
    public void setLeftOut(double leftOut)
    {
        setLeftRight(leftOut, rightOut, ScalerRange.SR_OUT, true);
    }

    /**
     * @return the rightOut
     */
    public double getRightOut()
    {
        return rightOut;
    }

    /**
     * @param rightOut
     *        the rightOut to set
     */
    public void setRightOut(double rightOut)
    {
        setLeftRight(leftOut, rightOut, ScalerRange.SR_OUT, false);
    }

    /**
     * @param left
     *        the leftAbsIn to set
     * @param right
     *        the rightAbsIn to set
     */
    public void setAbsLeftRightIn(double left, double right)
    {
        setLeftRight(left, right, ScalerRange.SR_ABSIN, false);
    }

    /**
     * @param left
     *        the leftIn to set
     * @param right
     *        the rightIn to set
     */
    public void setLeftRightIn(double left, double right)
    {
        setLeftRight(left, right, ScalerRange.SR_IN, false);
    }

    /**
     * @param left
     *        the leftOut to set
     * @param right
     *        the rightOut to set
     */
    public void setLeftRightOut(double left, double right)
    {
        setLeftRight(left, right, ScalerRange.SR_OUT, false);
    }

    /**
     * fire event
     */
    public void fireEvent(ScalerEvent e)
    {
        for (ScalerListener listener : listeners.getListeners(ScalerListener.class))
            listener.scalerChanged(e);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(ScalerListener listener)
    {
        listeners.add(ScalerListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(ScalerListener listener)
    {
        listeners.remove(ScalerListener.class, listener);
    }

    @Override
    public void onChanged(CollapsibleEvent compare)
    {
        final ScalerEvent event = (ScalerEvent) compare;

        // notify listener we have changed
        fireEvent(event);
    }

    /**
     * process on change
     */
    private void changed()
    {
        // handle changed via updater object
        updater.changed(new ScalerEvent(this));
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    public void endUpdate()
    {
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            double l, r;

            setCanCross(XMLUtil.getElementBooleanValue(node, ID_CANCROSS, false));
            integerData = XMLUtil.getElementBooleanValue(node, ID_INTEGERDATA, false);

            l = XMLUtil.getElementDoubleValue(node, ID_ABSLEFTIN, 0d);
            r = XMLUtil.getElementDoubleValue(node, ID_ABSRIGHTIN, 0d);
            setLeftRight(l, r, ScalerRange.SR_ABSIN, true);
            l = XMLUtil.getElementDoubleValue(node, ID_LEFTIN, 0d);
            r = XMLUtil.getElementDoubleValue(node, ID_RIGHTIN, 0d);
            setLeftRight(l, r, ScalerRange.SR_IN, true);
            l = XMLUtil.getElementDoubleValue(node, ID_LEFTOUT, 0d);
            r = XMLUtil.getElementDoubleValue(node, ID_RIGHTOUT, 0d);
            setLeftRight(l, r, ScalerRange.SR_OUT, true);
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementBooleanValue(node, ID_CANCROSS, getCanCross());
        XMLUtil.setElementBooleanValue(node, ID_INTEGERDATA, isIntegerData());

        XMLUtil.setElementDoubleValue(node, ID_ABSLEFTIN, getAbsLeftIn());
        XMLUtil.setElementDoubleValue(node, ID_ABSRIGHTIN, getAbsRightIn());
        XMLUtil.setElementDoubleValue(node, ID_LEFTIN, getLeftIn());
        XMLUtil.setElementDoubleValue(node, ID_RIGHTIN, getRightIn());
        XMLUtil.setElementDoubleValue(node, ID_LEFTOUT, getLeftOut());
        XMLUtil.setElementDoubleValue(node, ID_RIGHTOUT, getRightOut());

        return true;
    }

}

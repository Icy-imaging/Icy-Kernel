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

import icy.util.StringUtil;

import java.util.concurrent.TimeUnit;

/**
 * Unit conversion utilities class.
 * 
 * @author Thomas Provoost & Stephane Dallongeville
 */
public class UnitUtil
{
    public static enum UnitPrefix
    {
        GIGA, MEGA, KILO, NONE, MILLI, MICRO, NANO, PICO;

        @Override
        public String toString()
        {
            switch (this)
            {
                case GIGA:
                    return "G";
                case KILO:
                    return "k";
                case MEGA:
                    return "M";
                case MILLI:
                    return "m";
                case MICRO:
                    return "µ";
                case NANO:
                    return "n";
                case PICO:
                    return "p";
                case NONE:
                    return "";
                default:
                    return "x";
            }
        }
    };

    /**
     * Return the specified value as "bytes" string :<br>
     * 1024 --> "1 KB"<br>
     * 1048576 --> "1 MB"<br>
     * ...<br>
     */
    public static String getBytesString(double value)
    {
        final double absValue = Math.abs(value);

        // GB
        if (absValue > 10737418240f)
            return Double.toString(MathUtil.round(value / 1073741824d, 1)) + " GB";
        // MB
        else if (absValue > 10485760f)
            return Double.toString(MathUtil.round(value / 1048576d, 1)) + " MB";
        // KB
        else if (absValue > 10240f)
            return Double.toString(MathUtil.round(value / 1024d, 1)) + " KB";

        // B
        return Double.toString(MathUtil.round(value, 1)) + " B";
    }

    /**
     * Get the best unit with the given value and {@link UnitPrefix}.<br>
     * By best unit we adapt the output unit so the value stay between 0.1 --> 100 range (for
     * dimension 1).<br>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or
     * {@link TimeUnit} methods.<br/>
     * <br/>
     * Example: <code>getBestUnit(0.01, UnitPrefix.MILLI, 1)</code> will return
     * <code>UnitPrefix.MICRO</code><br/>
     * 
     * @param value
     *        : value used to get the best unit.
     * @param currentUnit
     *        : current unit of the value.
     * @param dimension
     *        : current unit dimension.
     * @return Return the best unit
     * @see #getValueInUnit(double, UnitPrefix, UnitPrefix)
     */
    public static UnitPrefix getBestUnit(double value, UnitPrefix currentUnit, int dimension)
    {
        // special case
        if (value == 0d)
            return currentUnit;

        int typeInd = currentUnit.ordinal();
        double v = value;
        final int maxInd = UnitPrefix.values().length - 1;
        final double factor = Math.pow(1000d, dimension);
        final double midFactor = Math.pow(100d, dimension);

        while (((int) v == 0) && (typeInd < maxInd))
        {
            v *= factor;
            typeInd++;
        }
        while (((int) (v / midFactor) != 0) && (typeInd > 0))
        {
            v /= factor;
            typeInd--;
        }

        return UnitPrefix.values()[typeInd];
    }

    /**
     * Get the best unit with the given value and {@link UnitPrefix}. By best unit we adapt the
     * output unit so the value stay between 0.1 --> 100 range (for dimension 1).<br>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or
     * {@link TimeUnit} methods.<br/>
     * Example: <code>getBestUnit(0.01, UnitPrefix.MILLI, 1)</code> will return
     * <code>UnitPrefix.MICRO</code><br/>
     * 
     * @param value
     *        : value used to get the best unit.
     * @param currentUnit
     *        : current unit of the value.
     * @return Return the best unit
     * @see #getValueInUnit(double, UnitPrefix, UnitPrefix)
     */
    public static UnitPrefix getBestUnit(double value, UnitPrefix currentUnit)
    {
        return getBestUnit(value, currentUnit, 1);
    }

    /**
     * Return the value from a specific unit to another unit.<br/>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or
     * {@link TimeUnit} methods.<br/>
     * <b>Example:</b><br/>
     * <ul>
     * <li>value = 0.01</li>
     * <li>currentUnit = {@link UnitPrefix#MILLI}</li>
     * <li>wantedUnit = {@link UnitPrefix#MICRO}</li>
     * <li>returns: 10</li>
     * </ul>
     * 
     * @param value
     *        : Original value.
     * @param currentUnit
     *        : current unit
     * @param wantedUnit
     *        : wanted unit
     * @param dimension
     *        : unit dimension.
     * @return Return a double value in the <code>wantedUnit</code> unit.
     * @see #getBestUnit(double, UnitPrefix)
     */
    public static double getValueInUnit(double value, UnitPrefix currentUnit, UnitPrefix wantedUnit, int dimension)
    {
        int currentOrdinal = currentUnit.ordinal();
        int wantedOrdinal = wantedUnit.ordinal();
        double result = value;
        final double factor = Math.pow(1000d, dimension);

        while (currentOrdinal < wantedOrdinal)
        {
            result *= factor;
            currentOrdinal++;
        }
        while (currentOrdinal > wantedOrdinal)
        {
            result /= factor;
            currentOrdinal--;
        }

        return result;
    }

    /**
     * Return the value from a specific unit to another unit.<br/>
     * Be careful, this method is supposed to be used with unit in <b>decimal</b>
     * system. For sexagesimal system, please use {@link #getBestTimeUnit(double)} or
     * {@link TimeUnit} methods.<br/>
     * <b>Example:</b><br/>
     * <ul>
     * <li>value = 0.01</li>
     * <li>currentUnit = {@link UnitPrefix#MILLI}</li>
     * <li>wantedUnit = {@link UnitPrefix#MICRO}</li>
     * <li>returns: 10</li>
     * </ul>
     * 
     * @param value
     *        : Original value.
     * @param currentUnit
     *        : current unit
     * @param wantedUnit
     *        : wanted unit
     * @return Return a double value in the <code>wantedUnit</code> unit.
     * @see #getBestUnit(double, UnitPrefix)
     */
    public static double getValueInUnit(double value, UnitPrefix currentUnit, UnitPrefix wantedUnit)
    {
        return getValueInUnit(value, currentUnit, wantedUnit, 1);
    }

    /**
     * This method returns a string containing the value rounded to a specified
     * number of decimals and its best unit prefix. This method is supposed to
     * be used with meters only.
     * 
     * @param value
     *        : value to display
     * @param decimals
     *        : number of decimals to keep
     * @param currentUnit
     *        : current unit prefix (Ex: {@link UnitPrefix#MILLI})
     */
    public static String getBestUnitInMeters(double value, int decimals, UnitPrefix currentUnit)
    {
        UnitPrefix unitPxSize = getBestUnit(value, currentUnit);
        double distanceMeters = getValueInUnit(value, currentUnit, unitPxSize);

        return StringUtil.toString(distanceMeters, decimals) + unitPxSize + "m";
    }

    /**
     * Return the best unit to display the value. The best unit is chosen
     * according to the precision. <br/>
     * <b>Example:</b>
     * <ul>
     * <li>62001 ms -> {@link TimeUnit#MILLISECONDS}</li>
     * <li>62000 ms -> {@link TimeUnit#SECONDS}</li>
     * <li>60000 ms -> {@link TimeUnit#MINUTES}</li>
     * </ul>
     * 
     * @param valueInMs
     *        : value in milliseconds.
     * @return Return a {@link TimeUnit} enumeration value.
     */
    public static TimeUnit getBestTimeUnit(double valueInMs)
    {
        if (valueInMs % 1000 != 0)
            return TimeUnit.MILLISECONDS;
        if (valueInMs % 60000 != 0)
            return TimeUnit.SECONDS;
        if (valueInMs % 3600000 != 0)
            return TimeUnit.MINUTES;

        return TimeUnit.HOURS;
    }

    /**
     * @deprecated Use {@link #getBestTimeUnit(double)} instead.
     */
    @Deprecated
    public static TimeUnit getBestUnit(double valueInMs)
    {
        return getBestTimeUnit(valueInMs);
    }

    /**
     * Display the time with a comma and a given precision.
     * 
     * @param valueInMs
     *        : value in milliseconds
     * @param precision
     *        : number of decimals after comma
     * @return <b>Example:</b> "2.5 h", "1.543 min", "15 ms".
     */
    public static String displayTimeAsStringWithComma(double valueInMs, int precision)
    {
        String toReturn = "";

        if (valueInMs >= 360000d)
        {
            valueInMs /= 360000d;
            toReturn = StringUtil.toString(valueInMs, precision) + " h";
        }
        else if (valueInMs >= 60000d)
        {
            valueInMs /= 60000d;
            toReturn = StringUtil.toString(valueInMs, precision) + " min";
        }
        else if (valueInMs >= 1000d)
        {
            valueInMs /= 1000d;
            toReturn = StringUtil.toString(valueInMs, precision) + " sec";
        }
        else
        {
            toReturn = StringUtil.toString(valueInMs, precision) + " ms";
        }

        return toReturn;
    }

    /**
     * Display the time with all the units.
     * 
     * @param valueInMs
     *        : value in milliseconds
     * @param displayZero
     *        : Even if a unit is not relevant (equals to zero), it will be displayed.
     * @return <b>Example:</b> "2h 3min 40sec 350ms".
     */
    public static String displayTimeAsStringWithUnits(double valueInMs, boolean displayZero)
    {
        String toReturn = "";

        if (valueInMs >= 3600000d)
        {
            toReturn += (int) (valueInMs / 3600000) + "h ";
            valueInMs %= 3600000;
        }
        else if (displayZero)
        {
            toReturn += "00h ";
        }
        if (valueInMs >= 60000d)
        {
            toReturn += (int) (valueInMs / 60000) + "min ";
            valueInMs %= 60000;
        }
        else if (displayZero)
        {
            toReturn += "00min ";
        }
        if (valueInMs >= 1000d)
        {
            toReturn += (int) (valueInMs / 1000d) + "sec ";
            valueInMs %= 1000;
        }
        else if (displayZero)
        {
            toReturn += "00sec ";
        }

        if (valueInMs != 0)
            toReturn += StringUtil.toString(valueInMs, 2) + "ms";
        else if (displayZero)
            toReturn += "000ms";

        return toReturn;
    }
}

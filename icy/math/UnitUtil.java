package icy.math;

import icy.util.StringUtil;

public class UnitUtil {

	public static enum UnitPrefix {
		GIGA, MEGA, KILO, NONE, MILLI, MICRO, NANO, PICO
	};

	/**
	 * Return the specified value as "bytes" string :<br>
	 * 1024 --> "1 KB"<br>
	 * 1048576 --> "1 MB"<br>
	 * ...<br>
	 */
	public static String getBytesString(double value) {
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
	 * Get the best unit with the given value and {@link UnitPrefix}.
	 * 
	 * @param value
	 *            : value used to get the best unit.
	 * @return Return the best unit
	 */
	public static UnitPrefix getBestUnit(double value) {
		return getBestUnit(value, UnitPrefix.NONE);
	}

	/**
	 * Get the best unit with the given value and {@link UnitPrefix}. <br/>
	 * <b>Warning:</b> Be careful, this method is supposed to be used with unit
	 * in base 10. <b>Example:</b><br/>
	 * <ul>
	 * <li>value = 0.01</li>
	 * <li>currentUnit = {@link UnitPrefix#MILLI}</li>
	 * <li>returns: {@link UnitPrefix#MICRO}</li>
	 * </ul>
	 * 
	 * @param value
	 *            : value used to get the best unit.
	 * @param currentUnit
	 *            : current unit of the value.
	 * @return Return the best unit
	 * @see #getValueInUnit(double, UnitPrefix, UnitPrefix)
	 */
	public static UnitPrefix getBestUnit(double value, UnitPrefix currentUnit) {
		int type = currentUnit.ordinal();
		if ((int) value == 0) {
			do {
				if (type < UnitPrefix.values().length) {
					++type;
					value *= 1000d;
				} else
					break;
			} while ((int) value == 0);
		} else if ((int) (value / 1000d) != 0) {
			do {
				if (type < UnitPrefix.values().length) {
					--type;
					value /= 1000d;
				} else
					break;
			} while ((int) (value / 1000d) != 0);
		}
		return UnitPrefix.values()[type];
	}

	/**
	 * Return the value from a specific unit to another unit.<br/>
	 * <b>Warning:</b> Be careful, this method is supposed to be used with unit
	 * in base 10. <br/>
	 * <b>Example:</b><br/>
	 * <ul>
	 * <li>value = 0.01</li>
	 * <li>currentUnit = {@link UnitPrefix#MILLI}</li>
	 * <li>currentUnit = {@link UnitPrefix#MICRO}</li>
	 * <li>returns: {@link UnitPrefix#MICRO}</li>
	 * </ul>
	 * 
	 * @param value
	 *            : Original value.
	 * @param currentUnit
	 *            : current unit
	 * @param wantedUnit
	 *            : wanted unit
	 * @return Return a double value in the <code>wantedUnit</code> unit.
	 * @see #getBestUnit(double, UnitPrefix)
	 */
	public static double getValueInUnit(double value, UnitPrefix currentUnit, UnitPrefix wantedUnit) {
		int currentOrdinal = currentUnit.ordinal();
		int wantedOrdinal = wantedUnit.ordinal();
		double toReturn = value;
		if (currentOrdinal < wantedOrdinal) {
			for (; currentOrdinal < wantedOrdinal; ++currentOrdinal) {
				toReturn *= 1000d;
			}
		} else if (currentOrdinal > wantedOrdinal) {
			for (; currentOrdinal > wantedOrdinal; --currentOrdinal) {
				toReturn /= 1000d;
			}
		}
		return toReturn;
	}

	/**
	 * Get the prefix as a string.
	 * 
	 * @param unit
	 *            : UnitPrefix wanted
	 * @return <ul>
	 *         <li>G for GIGA, k for kilo, etc.</li>
	 *         <li>x if an error occurred</li>
	 *         </ul>
	 */
	private static String getUnitPrefixAsString(UnitPrefix unit) {
		switch (unit) {
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

	/**
	 * This method returns a string containing the value rounded to a specified number of
	 * decimals and its best unit prefix. This method is supposed to be used with meters only.
	 * 
	 * @param value : value to display
	 * @param decimals : number of decimals to keep 
	 * @param currentUnit : current unit prefix (Ex: {@link UnitPrefix#MILLI}
	 * @return
	 */
	public static String getBestUnitInMeters(double value,  int decimals, UnitPrefix currentUnit) {
		UnitPrefix unitPxSize = getBestUnit(value, currentUnit);
		double distanceMeters = getValueInUnit(value, currentUnit, unitPxSize);
		return StringUtil.toString(distanceMeters, decimals) + getUnitPrefixAsString(unitPxSize) + "m";
	}
}

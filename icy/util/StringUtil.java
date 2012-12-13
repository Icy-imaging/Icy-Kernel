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
package icy.util;

import icy.math.MathUtil;

/**
 * @author stephane
 */
public class StringUtil
{
    /**
     * Return defaultValue if value is empty
     */
    public static String getValue(String value, String defaultValue)
    {
        if (StringUtil.isEmpty(value))
            return defaultValue;

        return value;
    }

    /**
     * Return the index of previous digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getPreviousDigitCharIndex(CharSequence value, int from)
    {
        final int len = value.length();

        if (from >= len)
            return -1;

        int index = from;
        while (index >= 0)
        {
            if (Character.isDigit(value.charAt(index)))
                return index;
            index--;
        }

        return -1;

    }

    /**
     * Return the index of previous non digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getPreviousNonDigitCharIndex(CharSequence value, int from)
    {
        final int len = value.length();

        if (from >= len)
            return -1;

        int index = from;
        while (index >= 0)
        {
            if (!Character.isDigit(value.charAt(index)))
                return index;
            index--;
        }

        return -1;
    }

    /**
     * Return the index of next digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getNextDigitCharIndex(CharSequence value, int from)
    {
        final int len = value.length();

        if (from < 0)
            return -1;

        int index = from;
        while (index < len)
        {
            if (Character.isDigit(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Return the index of next non digit char from specified index in specified string<br>
     * return -1 if not found
     */
    public static int getNextNonDigitCharIndex(CharSequence value, int from)
    {
        final int len = value.length();

        if (from < 0)
            return -1;

        int index = from;
        while (index < len)
        {
            if (!Character.isDigit(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Return the index of next control char from specified <code>startIndex</code> in specified
     * string.<br>
     * return -1 if no control character found.
     */
    public static int getNextCtrlCharIndex(CharSequence value, int startIndex)
    {
        final int len = value.length();

        if (startIndex < 0)
            return -1;

        int index = startIndex;
        while (index < len)
        {
            if (Character.isISOControl(value.charAt(index)))
                return index;
            index++;
        }

        return -1;
    }

    /**
     * Limit the length of the specified string to maxlen.
     */
    public static String limit(String value, int maxlen, boolean tailLimit)
    {
        if (value == null)
            return null;

        final int len = value.length();

        if (len > maxlen)
        {
            // simple truncation
            if (tailLimit || (maxlen <= 8))
                return value.substring(0, maxlen - 2) + "..";

            // cut center
            final int cut = (maxlen - 3) / 2;
            return value.substring(0, cut) + "..." + value.substring(len - cut);
        }

        return value;
    }

    /**
     * Limit the length of the specified string to maxlen.
     */
    public static String limit(String value, int maxlen)
    {
        return limit(value, maxlen, false);
    }

    /**
     * Truncate the text to a specific size, according a keyword.<br>
     * The text will be truncated around the place where the keyword is found.<br>
     * If the string is found at the beginning, the text will be like this:<br/>
     * <b><center>Lorem ipsum dolor sit amet, consec...</center><b/>
     * 
     * @param fullText
     *        : text to be truncated.
     * @param keyword
     *        : string to be found in the text and truncated around.
     * @param maxSize
     *        : max size of the string
     */
    public static String trunc(String fullText, String keyword, int maxSize)
    {
        int idx = fullText.toLowerCase().indexOf(keyword.toLowerCase());

        // key not found
        if (idx == -1)
            return "";

        String toReturn = fullText;
        int fullTextSize = fullText.length();

        if (fullTextSize > maxSize)
        {
            int firstSpaceAfter;
            String textBeforeWord;
            int lastSpaceBefore;

            // extract the full word from the text
            firstSpaceAfter = fullText.indexOf(' ', idx);
            firstSpaceAfter = firstSpaceAfter == -1 ? fullTextSize : firstSpaceAfter;

            textBeforeWord = fullText.substring(0, idx);
            lastSpaceBefore = textBeforeWord.lastIndexOf(' ');
            lastSpaceBefore = lastSpaceBefore == -1 ? 0 : lastSpaceBefore;

            // determine if we are at the beginning, the end, or at the middle
            if (idx <= maxSize / 2)
            {
                toReturn = fullText.substring(0, maxSize);
                toReturn += "..";
            }
            else if ((fullTextSize - idx) <= maxSize / 2)
            {
                toReturn = fullText.substring(fullTextSize - maxSize, fullTextSize);
                toReturn = ".." + toReturn;
            }
            else
            {
                int beginIndex = idx - maxSize / 2;
                int endIndex = idx + maxSize / 2;
                if (endIndex > fullTextSize)
                    System.out.println(endIndex);
                // beginIndex = beginIndex < 0 ? 0 : beginIndex;
                // endIndex = endIndex > fullTextSize ? fullTextSize : endIndex;
                toReturn = ".." + fullText.substring(beginIndex, endIndex) + "..";
            }
        }

        return toReturn;
    }

    /**
     * Return true if the specified String are exactly the same
     */
    public static boolean equals(String s1, String s2)
    {
        if (isEmpty(s1))
            return isEmpty(s2);

        return s1.equals(s2);
    }

    /**
     * Return true if the specified String is empty.
     * 
     * @param trim
     *        trim the String before doing the empty test
     */
    public static boolean isEmpty(String value, boolean trim)
    {
        if (value != null)
        {
            if (trim)
                return value.trim().length() == 0;

            return value.length() == 0;
        }

        return true;
    }

    /**
     * Return true if the specified String is empty.
     * The String is trimed by default before doing the test
     */
    public static boolean isEmpty(String value)
    {
        return isEmpty(value, true);
    }

    /**
     * Try to parse a boolean from the specified String and return it.
     * Return 'def' is we can't parse any boolean from the string.
     */
    public static boolean parseBoolean(String s, boolean def)
    {
        if (s == null)
            return def;

        final String value = s.toLowerCase();

        if (value.equals(Boolean.toString(true)))
            return true;
        if (value.equals(Boolean.toString(false)))
            return false;

        return def;
    }

    /**
     * Try to parse a integer from the specified String and return it.
     * Return 'def' is we can't parse any integer from the string.
     */
    public static int parseInt(String s, int def)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException E)
        {
            return def;
        }
    }

    /**
     * Try to parse a long integer from the specified String and return it.
     * Return 'def' is we can't parse any integer from the string.
     */
    public static long parseLong(String s, long def)
    {
        try
        {
            return Long.parseLong(s);
        }
        catch (NumberFormatException E)
        {
            return def;
        }
    }

    /**
     * Try to parse a float from the specified String and return it.
     * Return 'def' is we can't parse any float from the string.
     */
    public static float parseFloat(String s, float def)
    {
        try
        {
            return Float.parseFloat(s);
        }
        catch (NumberFormatException E)
        {
            return def;
        }
    }

    /**
     * Try to parse a double from the specified String and return it.
     * Return 'def' is we can't parse any double from the string.
     */
    public static double parseDouble(String s, double def)
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException E)
        {
            return def;
        }
    }

    /**
     * Try to parse a array of byte from the specified String and return it.
     * Return 'def' is we can't parse any array of byte from the string.
     */
    public static byte[] parseBytes(String s, byte[] def)
    {
        if (s == null)
            return def;

        return s.getBytes();
    }

    /**
     * Returns a <tt>String</tt> object representing the specified
     * boolean. If the specified boolean is <code>true</code>, then
     * the string {@code "true"} will be returned, otherwise the
     * string {@code "false"} will be returned.
     */
    public static String toString(boolean value)
    {
        return Boolean.toString(value);
    }

    /**
     * Returns a <code>String</code> object representing the specified integer.
     */
    public static String toString(int value)
    {
        return Integer.toString(value);
    }

    /**
     * Returns a <code>String</code> object representing the specified integer.<br>
     * If the returned String is shorter than specified length<br>
     * then leading '0' are added to the string.
     */
    public static String toString(int value, int minSize)
    {
        String result = Integer.toString(value);

        while (result.length() < minSize)
            result = "0" + result;

        return result;
    }

    /**
     * Returns a <code>String</code> object representing the specified <code>long</code>.
     */
    public static String toString(long value)
    {
        return Long.toString(value);
    }

    /**
     * Returns a string representation of the <code>float</code> argument.
     */
    public static String toString(float value)
    {
        return Float.toString(value);
    }

    /**
     * Returns a string representation of the <code>double</code> argument.
     */
    public static String toString(double value)
    {
        final int i = (int) value;

        if (i == value)
            return toString(i);

        return Double.toString(value);
    }

    /**
     * Returns a string representation of the <code>double</code> argument
     * with specified number of decimal.
     */
    public static String toString(double value, int numDecimal)
    {
        return Double.toString(MathUtil.round(value, numDecimal));
    }

    /**
     * Returns a string representation of the <code>double</code> argument with specified size :<br>
     * <code>toString(1.23456, 5)</code> --> <code>"1.2345"</code><br>
     * <code>toString(123.4567, 4)</code> --> <code>"123.4"</code><br>
     * <code>toString(1234.567, 2)</code> --> <code>"1234"</code> as we never trunk integer part.<br>
     * <code>toString(1234.5, 10)</code> --> <code>"1234.5"</code> as we never trunk integer part.<br>
     */
    public static String toStringEx(double value, int size)
    {
        final int i = (int) value;

        if (i == value)
            return toString(i);

        return Double.toString(MathUtil.roundSignificant(value, size, true));
    }

    /**
     * Return a string representation of the byte array argument.
     */
    public static String toString(byte[] value)
    {
        return new String(value);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base 16.
     */
    public static String toHexaString(int value)
    {
        return Integer.toHexString(value);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base 16.<br>
     * Force the returned string to have the specified size :<br>
     * If the string is longer then only last past is kept.<br>
     * If the string is shorter then leading 0 are added to the string.
     */
    public static String toHexaString(int value, int size)
    {
        String result = Integer.toHexString(value);

        if (result.length() > size)
            return result.substring(result.length() - size);

        while (result.length() < size)
            result = "0" + result;
        return result;
    }

    /**
     * Remove <code>count</code> characters from the end of specified string.
     */
    public static String removeLast(String value, int count)
    {
        if (value == null)
            return null;

        final int l = value.length();

        if (l < 2)
            return "";

        return value.substring(0, l - count);
    }

    /**
     * Creates a flattened version of the provided String. The flattening operation splits the
     * string by inserting spaces between words starting with an upper case letter, and converts
     * upper case letters to lower case (with the exception of the first word). Note that
     * <b>consecutive upper case letters will remain grouped</b>, as they are considered to
     * represent an acronym.<br/>
     * <br/>
     * <u>NOTE:</u> This method is optimized for class names that follow the Java naming convention. <br/>
     * Examples:<br/>
     * MyGreatClass -> "My great class"<br/>
     * MyXYZClass -> "My XYZ class"
     * 
     * @param string
     *        the string to flatten
     * @return a flattened (i.e. pretty-printed) String based on the name of the string
     */
    public static String getFlattened(String string)
    {
        String[] words = string.split("(?=[A-Z])");

        String output = words[0];
        if (words.length > 1)
        {
            // words[0] is always empty here
            output = words[1];

            for (int i = 2; i < words.length; i++)
            {
                String word = words[i];
                if (word.length() == 1)
                {
                    // single letter
                    if (words[i - 1].length() == 1)
                    {
                        // append to the previous letter (acronym)
                        output += word;
                    }
                    else
                    {
                        // new isolated letter or acronym
                        output += " " + word;
                    }
                }
                else
                    output += " " + word.toLowerCase();
            }
        }

        return output;
    }

    /**
     * Replace all C line break sequence : <code>"\n", "\r", "\r\n"</code><br>
     * from the specified <code>text</code> by <code>str</code>.
     */
    public static String replaceCR(String text, String str)
    {
        return text.replaceAll("(\r\n|\n\r|\r|\n)", str);
    }

    /**
     * Remove all C line break sequence : <code>"\n", "\r", "\r\n"</code><br>
     * from the specified text.
     */
    public static String removeCR(String text)
    {
        return replaceCR(text, "");
    }

    /**
     * Convert the C line break sequence : <code>"\n", "\r", "\r\n"</code><br>
     * to HTML line break sequence.
     */
    public static String toHtmlCR(String text)
    {
        return replaceCR(text, "<br/>");
    }

    /**
     * Return true if the specified text contains HTML line break sequence.
     */
    public static boolean containHtmlCR(String text)
    {
        return (text.indexOf("<br>") != -1) || (text.indexOf("<BR>") != -1) || (text.indexOf("<br/>") != -1)
                || (text.indexOf("<BR/>") != -1);
    }

    /**
     * Bold (inserting HTML bold tag) the specified keyword in the text.
     */
    public static String htmlBoldSubstring(String text, String keyword, boolean ignoreCase)
    {
        if (!isEmpty(text) && !isEmpty(keyword))
        {
            final int keywordLen = keyword.length();
            final String key;

            if (ignoreCase)
                key = keyword.toLowerCase();
            else
                key = keyword;

            String result = text;
            int index;

            if (ignoreCase)
                index = result.toLowerCase().indexOf(key);
            else
                index = result.indexOf(key);

            while (index != -1)
            {
                result = result.substring(0, index) + "<b>" + result.substring(index, index + keywordLen) + "</b>"
                        + result.substring(index + keywordLen);

                if (ignoreCase)
                    index = result.toLowerCase().indexOf(key, index + keywordLen + 6);
                else
                    index = result.indexOf(key, index + keywordLen + 6);
            }

            return result;
        }

        return text;
    }

}

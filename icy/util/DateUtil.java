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
package icy.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date utilities class.
 * 
 * @author Stephane
 */
public class DateUtil
{
    /**
     * Return current date.
     */
    public static Date now()
    {
        return Calendar.getInstance().getTime();
    }

    /**
     * Return current date (String format).<br>
     * 
     * @param format
     *        define the wanted format.<br>
     *        Ex :<br>
     *        DateUtil.now("dd MMMMM yyyy");<br>
     *        DateUtil.now("yyyyMMdd");<br>
     *        DateUtil.now("MM/dd/yy");<br>
     *        DateUtil.now("yyyy.MM.dd G 'at' hh:mm:ss z");<br>
     *        DateUtil.now("H:mm:ss:SSS");<br>
     *        DateUtil.now("yyyy.MMMMM.dd GGG hh:mm aaa");<br>
     */
    public static String now(String format)
    {
        return new SimpleDateFormat(format).format(now());
    }

    /**
     * Return the specified date in String format.<br>
     * 
     * @param format
     *        define the wanted format.<br>
     *        Ex :<br>
     *        DateUtil.now("dd MMMMM yyyy");<br>
     *        DateUtil.now("yyyyMMdd");<br>
     *        DateUtil.now("MM/dd/yy");<br>
     *        DateUtil.now("yyyy.MM.dd G 'at' hh:mm:ss z");<br>
     *        DateUtil.now("H:mm:ss:SSS");<br>
     *        DateUtil.now("yyyy.MMMMM.dd GGG hh:mm aaa");<br>
     */
    public static String format(String format, Date date)
    {
        return new SimpleDateFormat(format).format(date);
    }
}

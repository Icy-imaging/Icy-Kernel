/**
 * 
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
     * @param dateFormat
     *        define the wanted format.<br>
     *        Ex :<br>
     *        DateUtil.now("dd MMMMM yyyy");<br>
     *        DateUtil.now("yyyyMMdd");<br>
     *        DateUtil.now("MM/dd/yy");<br>
     *        DateUtil.now("yyyy.MM.dd G 'at' hh:mm:ss z");<br>
     *        DateUtil.now("H:mm:ss:SSS");<br>
     *        DateUtil.now("yyyy.MMMMM.dd GGG hh:mm aaa");<br>
     */
    public static String now(String dateFormat)
    {
        return new SimpleDateFormat(dateFormat).format(now());
    }
}

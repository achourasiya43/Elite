package com.elite.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nilanchala Panigrahy on 8/24/16.
 */

public class CalendarUtils {

    private static String dateFormat = "dd-MM-yyyy hh:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

    public static boolean isSameMonth(Calendar c1, Calendar c2) {
        if (c1 == null || c2 == null)
            return false;
        return (c1.get(Calendar.ERA) == c2.get(Calendar.ERA)
                && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH));
    }

    /**
     * <p>Checks if a calendar is today.</p>
     *
     * @param calendar the calendar, not altered, not null.
     * @return true if the calendar is today.
     * @throws IllegalArgumentException if the calendar is <code>null</code>
     */
    public static boolean isToday(Calendar calendar) {
        return isSameDay(calendar, Calendar.getInstance());
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null)
            throw new IllegalArgumentException("The dates must not be null");
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static int getTotalWeeks(Calendar calendar) {
        if (null == calendar) return 0;
        int maxWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
        return maxWeeks;

    }

    public static int getTotalWeeks(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getTotalWeeks(cal);
    }

    public static boolean isPastDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (date.before(calendar.getTime())) ? true : false;
    }

    public static boolean isPastTime(Calendar calendar) {
        Calendar c1 = Calendar.getInstance();
        return (calendar.before(c1)) ? true : false;
    }

   /* public static boolean isPastTime(Calendar c1){
        int c1Year = c1.get(Calendar.YEAR);
        int c1Month = c1.get(Calendar.MONTH);
        int c1Day = c1.get(Calendar.DAY_OF_MONTH);

        Calendar c2 = Calendar.getInstance().getTimeInMillis();

        if(c1Year<c2Year){
            return true;
        }else if (c1Year>c2Year){
            return false;
        }else{
            if(c1Month>c2Month){
                return false;
            }else if(c1Month<c2Month){
                return true;
            }else{
                return c1Day<c2Day;
            }
        }
    }*/

    public static String getStringDate(Date date) {
        // the string representation of date (month/day/year)
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public static String ConvertMilliSecondsToFormattedDate(String milliSeconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String ConvertMilliSecondsToFormattedDate(long milliSeconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return simpleDateFormat.format(calendar.getTime());
    }

}

package com.morecruit.ext.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期Date相关工具类
 * <p>
 * Created by zhaiyifan on 2015/8/3.
 */
public class DateUtils {
    /**
     * Default date pattern.
     */
    public final static String DEFAULT_PATTERN = "yyyy-MM-dd_HH-mm-ss.SSS";

    private final static ThreadLocal<SimpleDateFormat> sDefaultDateFormat = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_PATTERN);
        }
    };

    private DateUtils() {
        // static usage.
    }

    /**
     * Get current date with default pattern.
     *
     * @return Current date with default pattern.
     * @see #DEFAULT_PATTERN
     */
    public static String getDate() {
        return getDate(System.currentTimeMillis());
    }

    /**
     * Get date with default pattern of corresponding time.
     *
     * @param timeInMillis Time in milliseconds.
     * @return Date with default pattern of corresponding time.
     */
    public static String getDate(long timeInMillis) {
        return sDefaultDateFormat.get().format(new Date(timeInMillis));
    }

    /**
     * Get date with default pattern of corresponding time.
     *
     * @param pattern Date format pattern.
     * @return Date with default pattern of corresponding time.
     * @see #DEFAULT_PATTERN
     */
    public static String getDate(String pattern) {
        return getDate(pattern, System.currentTimeMillis());
    }

    /**
     * Get date with default pattern of corresponding time.
     *
     * @param pattern      Date format pattern.
     * @param timeInMillis Time in milliseconds.
     * @return Date with default pattern of corresponding time.
     */
    public static String getDate(String pattern, long timeInMillis) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date(timeInMillis));
    }

    /**
     * Get the start time in milliseconds of a day.
     *
     * @param time Time within one day.
     * @return The start time of a day.
     */
    public static long getStartOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String getDateDesc(Date date) {
        Date now = new Date();
        long time = now.getTime() - date.getTime();
        String str = "很久了";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String mmDate = format.format(date);
        if (time / 1000 / 60 <= 3) {
            str = "刚刚";
        } else if (time / 1000 / 60 > 3 && time / 1000 / 60 < 60) {
            str = time / 1000 / 60 + "分钟前";
        } else if (time / 1000 / 60 / 60 >= 1 && time / 1000 / 60 / 60 < 24) {
            str = time / 1000 / 60 / 60 + "小时前";
        } else if (time / 1000 / 60 / 60 / 24 >= 1
                && time / 1000 / 60 / 60 / 24 < 2) {
            str = "1天前";
        } else if (time / 1000 / 60 / 60 / 24 >= 2
                && time / 1000 / 60 / 60 / 24 < 3) {
            str = "2天前";
        } else if (time / 1000 / 60 / 60 / 24 >= 3) {
            str = mmDate;
        }
        return str;
    }
}

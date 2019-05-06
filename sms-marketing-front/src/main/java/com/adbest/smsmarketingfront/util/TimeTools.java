package com.adbest.smsmarketingfront.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 */
public class TimeTools {
    
    private static Calendar instance = Calendar.getInstance();
    private static SimpleDateFormat simpleDateFormat;
    
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    public static long nowMillis() {
        return System.currentTimeMillis();
    }
    
    public static long thisMonthStartMillis() {
        return monthStart(System.currentTimeMillis());
    }
    
    public static Timestamp thisMonthStart() {
        return monthStart(now());
    }
    
    public static long monthStart(long time) {
        instance.setTimeInMillis(dayStart(time));
        instance.set(Calendar.DATE, 1);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp monthStart(Timestamp time) {
        instance.setTimeInMillis(dayStart(time.getTime()));
        instance.set(Calendar.DATE, 1);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long monthEnd(long time) {
        instance.setTimeInMillis(dayEnd(time));
        instance.set(Calendar.DATE, instance.getActualMaximum(Calendar.DATE));
        return instance.getTimeInMillis();
    }
    
    public static Timestamp monthEnd(Timestamp time) {
        instance.setTimeInMillis(dayEnd(time.getTime()));
        instance.set(Calendar.DATE, instance.getActualMaximum(Calendar.DATE));
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long todayStartMillis() {
        return dayStart(System.currentTimeMillis());
    }
    
    public static Timestamp todayStart() {
        return dayStart(now());
    }
    
    public static long dayStart(long time) {
        instance.setTimeInMillis(time);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp dayStart(Timestamp time) {
        instance.setTimeInMillis(time.getTime());
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long dayEnd(long time) {
        instance.setTimeInMillis(time);
        instance.set(Calendar.HOUR_OF_DAY, 23);
        instance.set(Calendar.MINUTE, 59);
        instance.set(Calendar.SECOND, 59);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp dayEnd(Timestamp time) {
        instance.setTimeInMillis(time.getTime());
        instance.set(Calendar.HOUR_OF_DAY, 23);
        instance.set(Calendar.MINUTE, 59);
        instance.set(Calendar.SECOND, 59);
        instance.set(Calendar.MILLISECOND, 0);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long addMonth(long time, int months) {
        instance.setTimeInMillis(time);
        instance.add(Calendar.MONTH, months);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp addMonth(Timestamp time, int months) {
        instance.setTimeInMillis(time.getTime());
        instance.add(Calendar.MONTH, months);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long addDay(long time, int days) {
        instance.setTimeInMillis(time);
        instance.add(Calendar.DATE, days);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp addDay(Timestamp time, int days) {
        instance.setTimeInMillis(time.getTime());
        instance.add(Calendar.DATE, days);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long addHours(long time, int hours) {
        instance.setTimeInMillis(time);
        instance.add(Calendar.HOUR_OF_DAY, hours);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp addHours(Timestamp time, int hours) {
        instance.setTimeInMillis(time.getTime());
        instance.add(Calendar.HOUR_OF_DAY, hours);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long addMinutes(long time, int minutes) {
        instance.setTimeInMillis(time);
        instance.add(Calendar.MINUTE, minutes);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp addMinutes(Timestamp time, int minutes) {
        instance.setTimeInMillis(time.getTime());
        instance.add(Calendar.MINUTE, minutes);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static long addSeconds(long time, int seconds) {
        instance.setTimeInMillis(time);
        instance.add(Calendar.SECOND, seconds);
        return instance.getTimeInMillis();
    }
    
    public static Timestamp addSeconds(Timestamp time, int seconds) {
        instance.setTimeInMillis(time.getTime());
        instance.add(Calendar.SECOND, seconds);
        return new Timestamp(instance.getTimeInMillis());
    }
    
    public static String formatDateStr(long time, String pattern) {
        simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date(time));
    }
    
    public static String formatDateStr(Timestamp time, String pattern) {
        if (time == null) {
            return "";
        }
        simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date(time.getTime()));
    }
}

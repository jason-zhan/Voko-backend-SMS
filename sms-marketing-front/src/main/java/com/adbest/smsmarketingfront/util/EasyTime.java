package com.adbest.smsmarketingfront.util;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple and easy-to-use time tools class.
 * It's not thread-safe. So don't share it in multi threads.
 * Depend on {@link Calendar}
 */
public class EasyTime {
    
    private Calendar instance;
    
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    public static long nowMillis() {
        return System.currentTimeMillis();
    }
    
    /**
     * Init use current time.
     * @return
     */
    public static EasyTime init() {
        return new EasyTime(now());
    }
    
    /**
     * Init use specific time.
     * @param time
     * @param <T>
     * @return
     */
    public static <T extends Date> EasyTime init(@NotNull T time) {
        return new EasyTime(time);
    }
    
    /**
     * return format time string use specific time and pattern.
     * Depend on {@link SimpleDateFormat}
     * @param time
     * @param pattern
     * @param <T>
     * @return
     */
    public static <T extends Date> String format(@NotNull T time, @NotEmpty String pattern){
        return new SimpleDateFormat(pattern).format(time);
    }
    
    public String format(@NotEmpty String pattern){
        return new SimpleDateFormat(pattern).format(instance.getTime());
    }
    
    public <T extends Date> EasyTime(@NotNull T time) {
        instance = new Calendar.Builder().setInstant(time).build();
    }
    
    /**
     * get the {@link Timestamp} of this time.
     * @return
     */
    public Timestamp stamp() {
        return new Timestamp(instance.getTimeInMillis());
    }
    
    /**
     * get the milliseconds of this time.
     * @return
     */
    public long millis() {
        return instance.getTimeInMillis();
    }
    
    public EasyTime dayStart() {
        dayStartImpl();
        return this;
    }
    
    private void dayStartImpl() {
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
    }
    
    public EasyTime dayEnd() {
        dayEndImpl();
        return this;
    }
    
    private void dayEndImpl() {
        instance.set(Calendar.HOUR_OF_DAY, 23);
        instance.set(Calendar.MINUTE, 59);
        instance.set(Calendar.SECOND, 59);
        instance.set(Calendar.MILLISECOND, 0);
    }
    
    public EasyTime monthStart() {
        instance.set(Calendar.DATE, 1);
        dayStartImpl();
        return this;
    }
    
    public EasyTime monthEnd() {
        instance.set(Calendar.DATE, instance.getActualMaximum(Calendar.DATE));
        dayEndImpl();
        return this;
    }
    
    /**
     * add some milliseconds
     * @param millis (+/-)
     * @return
     */
    public EasyTime addMillis(int millis) {
        instance.add(Calendar.MILLISECOND, millis);
        return this;
    }
    
    /**
     * add some seconds
     * @param seconds (+/-)
     * @return
     */
    public EasyTime addSeconds(int seconds) {
        instance.add(Calendar.SECOND, seconds);
        return this;
    }
    
    /**
     * add some minutes
     * @param minutes (+/-)
     * @return
     */
    public EasyTime addMinutes(int minutes) {
        instance.add(Calendar.MINUTE, minutes);
        return this;
    }
    
    /**
     * add some hours
     * @param hours (+/-)
     * @return
     */
    public EasyTime addHours(int hours) {
        instance.add(Calendar.HOUR_OF_DAY, hours);
        return this;
    }
    
    /**
     * add some days
     * @param days (+/-)
     * @return
     */
    public EasyTime addDays(int days) {
        instance.add(Calendar.DATE, days);
        return this;
    }
    
    /**
     * add some months
     * @param months (+/-)
     * @return
     */
    public EasyTime addMonths(int months) {
        instance.add(Calendar.MONTH, months);
        return this;
    }
    
}

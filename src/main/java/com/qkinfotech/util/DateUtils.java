package com.qkinfotech.util;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 时间日期工具类
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM" };

    /**
     * 获取当前Date型日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前Date型日期 YYYY_MM_DD
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取当前Date型日期 YYYY_MM_DD_HH_MM_SS
     */
    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前Date型日期 YYYYMMDDHHMMSS
     */
    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * 获取当前Date型日期 format
     */
    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * 获取Date型日期 YYYY_MM_DD
     */
    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * 将Date型日期转换为format型字符串
     */
    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 获取Date型日期 format
     */
    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 将 LocalDateTime 对象转换为 Date 对象。
     *
     * @param temporalAccessor LocalDateTime 实例
     * @return 返回对应的 Date 实例
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        // 将 LocalDateTime 转换为系统默认时区的 ZonedDateTime
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        // 使用 ZonedDateTime 的瞬时时间创建 Date 对象
        return Date.from(zdt.toInstant());
    }

    /**
     * 将 LocalDate 对象转换为 Date 对象。
     *
     * @param temporalAccessor LocalDate 实例
     * @return 返回对应的 Date 实例
     */
    public static Date toDate(LocalDate temporalAccessor) {
        // 将 LocalDate 转换为 LocalDateTime，时间为 00:00:00
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        // 将 LocalDateTime 转换为系统默认时区的 ZonedDateTime
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        // 使用 ZonedDateTime 的瞬时时间创建 Date 对象
        return Date.from(zdt.toInstant());
    }

    /**
     * 返回两个时间之间的所有天数
     * @param startDateStr
     * @param endDateStr
     * @return
     */
    public static List<String> getAllDatesBetween(String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 将字符串日期转换为LocalDateTime对象
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

        // 转换为LocalDate以忽略时间部分
        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        // 如果起始日期等于结束日期，则直接返回包含该日期的列表
        if (startLocalDate.equals(endLocalDate)) {
            return List.of(startLocalDate.toString());
        }

        List<String> dates = new ArrayList<>();

        // 使用循环遍历每一天，直到结束日期的前一天
        for (LocalDate date = startLocalDate; !date.isAfter(endLocalDate); date = date.plusDays(1)) {
            dates.add(date.toString());
        }

        return dates;
    }


    public static void main(String[] args) {
        String startDateStr = "2023-01-01 00:00";
        String endDateStr = "2023-01-05 00:00";

        List<String> dates = getAllDatesBetween(startDateStr, endDateStr);

        for (String date : dates) {
            System.out.println(date);
        }
    }

}



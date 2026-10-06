package com.awe.foundation.common.convert;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 本地日期时间转换器
 *
 * @author Awe
 * @date 2023/4/7 16:03
 */
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

    public static final String MINUTE = "yyyy-MM-dd HH:mm";

    /**
     * 将字符串转换为日期时间
     *
     * @param s 日期时间字符串
     * @return 日期时间对象
     */
    @Override
    public LocalDateTime convert(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * LocalDateTime 转为 String
     */
    public static String timeToString(LocalDateTime time, String pattern) {
        if (java.util.Objects.isNull(time)) {
            return "";
        }
        return time.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
    }

}

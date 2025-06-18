package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    /**
     * 纯数字
     */
    public static final Pattern DIGIT = Pattern.compile("\\d+");

    /**
     * 默认实例
     */
    public static final StringToLocalDateTimeConverter INSTANCE = new StringToLocalDateTimeConverter();

    /**
     * 默认时间格式
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 默认时间格式
     */
    public static final DateTimeFormatter SIMPLE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public LocalDateTime apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        if (DIGIT.matcher(source).matches()) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(source)), ZoneId.systemDefault());
        }
        return apply(source, LocalDateTime::parse);
    }

    public static LocalDateTime apply(String source, Function<String, LocalDateTime> finallyResolve) {
        if (source.length() > 10 && source.charAt(10) != ' ') {
            if (source.endsWith("Z")) {
                return LocalDateTime.ofInstant(Instant.parse(source), ZoneOffset.UTC);
            }
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (source.length() >= 19 && source.charAt(10) == ' ') {
            if (source.lastIndexOf('.') > -1) {
                return LocalDateTime.parse(source, SIMPLE_DATE_TIME_FORMATTER);
            }
            return LocalDateTime.parse(source, DEFAULT_DATE_TIME_FORMATTER);
        }
        return finallyResolve.apply(source);
    }
}

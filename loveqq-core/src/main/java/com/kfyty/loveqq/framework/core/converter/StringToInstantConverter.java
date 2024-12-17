package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.time.Instant;

import static com.kfyty.loveqq.framework.core.converter.StringToLocalDateTimeConverter.DEFAULT_DATE_TIME_FORMATTER;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToInstantConverter implements Converter<String, Instant> {

    @Override
    public Instant apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        if (source.matches("\\d+")) {
            return Instant.ofEpochMilli(Long.parseLong(source));
        }
        return DEFAULT_DATE_TIME_FORMATTER.parse(source, Instant::from);
    }
}

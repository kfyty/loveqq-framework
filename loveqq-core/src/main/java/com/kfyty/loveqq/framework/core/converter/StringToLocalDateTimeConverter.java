package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime apply(String source) {
        return CommonUtil.empty(source) ? null : LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

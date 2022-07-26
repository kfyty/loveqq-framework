package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

import java.time.LocalDateTime;

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
        return CommonUtil.empty(source) ? null : LocalDateTime.parse(source);
    }
}

package com.kfyty.core.converter;

import com.kfyty.core.utils.CommonUtil;

import java.sql.Timestamp;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToTimestampConverter implements Converter<String, Timestamp> {

    @Override
    public Timestamp apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        return source.contains("-") ? Timestamp.valueOf(source) : new Timestamp(Long.parseLong(source));
    }
}

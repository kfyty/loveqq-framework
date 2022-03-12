package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

import java.time.Duration;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToDurationConverter implements Converter<String, Duration> {

    @Override
    public Duration apply(String source) {
        return CommonUtil.empty(source) ? null : Duration.parse(source);
    }
}

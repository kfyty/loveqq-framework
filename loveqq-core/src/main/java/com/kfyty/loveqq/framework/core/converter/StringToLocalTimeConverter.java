package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.time.LocalTime;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToLocalTimeConverter implements Converter<String, LocalTime> {

    @Override
    public LocalTime apply(String source) {
        return CommonUtil.empty(source) ? null : LocalTime.parse(source);
    }
}

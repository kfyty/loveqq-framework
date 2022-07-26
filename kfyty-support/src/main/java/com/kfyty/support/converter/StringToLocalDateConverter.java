package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

import java.time.LocalDate;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate apply(String source) {
        return CommonUtil.empty(source) ? null : LocalDate.parse(source);
    }
}

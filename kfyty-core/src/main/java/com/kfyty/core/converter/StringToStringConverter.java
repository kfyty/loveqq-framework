package com.kfyty.core.converter;

import com.kfyty.core.utils.CommonUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToStringConverter implements Converter<String, String> {

    @Override
    public String apply(String source) {
        return CommonUtil.empty(source) ? null : source;
    }
}

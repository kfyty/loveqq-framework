package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

    @Override
    public Boolean apply(String source) {
        return CommonUtil.empty(source) ? null : Boolean.parseBoolean(source);
    }
}

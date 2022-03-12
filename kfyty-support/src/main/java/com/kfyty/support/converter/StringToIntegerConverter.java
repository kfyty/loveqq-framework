package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToIntegerConverter implements Converter<String, Integer> {

    @Override
    public Integer apply(String source) {
        return CommonUtil.empty(source) ? null : Integer.parseInt(source);
    }
}

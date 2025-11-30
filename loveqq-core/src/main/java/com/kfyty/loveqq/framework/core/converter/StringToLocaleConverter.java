package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.Locale;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToLocaleConverter implements Converter<String, Locale> {

    @Override
    public Locale apply(String source) {
        return CommonUtil.empty(source) ? null : new Locale(source);
    }
}

package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToAtomicLongConverter implements Converter<String, AtomicLong> {

    @Override
    public AtomicLong apply(String source) {
        return CommonUtil.empty(source) ? null : new AtomicLong(Long.parseLong(source));
    }
}

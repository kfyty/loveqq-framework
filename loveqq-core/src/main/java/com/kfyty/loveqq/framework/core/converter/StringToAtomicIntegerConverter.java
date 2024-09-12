package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToAtomicIntegerConverter implements Converter<String, AtomicInteger> {

    @Override
    public AtomicInteger apply(String source) {
        return CommonUtil.empty(source) ? null : new AtomicInteger(Integer.parseInt(source));
    }
}

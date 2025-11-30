package com.kfyty.loveqq.framework.core.converter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToObjectConverter implements Converter<String, Object> {

    @Override
    public Object apply(String source) {
        return source == null || source.isEmpty() ? null : source;
    }
}

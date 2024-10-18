package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.Collections;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToCharacterConverter implements Converter<String, Character> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(char.class);
    }

    @Override
    public Character apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        return source.charAt(0);
    }
}

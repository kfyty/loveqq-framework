package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToInstantConverter implements Converter<String, Instant> {

    @Override
    public Instant apply(String source) {
        return CommonUtil.empty(source) ? null : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(source, Instant::from);
    }
}

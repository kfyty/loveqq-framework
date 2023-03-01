package com.kfyty.core.converter;

import com.kfyty.core.utils.CommonUtil;
import lombok.SneakyThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToDateConverter implements Converter<String, Date> {

    @Override
    @SneakyThrows(ParseException.class)
    public Date apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        try {
            return !source.contains("-") ? new Date(Long.parseLong(source)) : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(source);
        } catch (ParseException e) {
            return new SimpleDateFormat("yyyy-MM-dd").parse(source);
        }
    }
}

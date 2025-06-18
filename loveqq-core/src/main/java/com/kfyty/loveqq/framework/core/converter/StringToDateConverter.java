package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
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
        if (StringToLocalDateTimeConverter.DIGIT.matcher(source).matches()) {
            return new Date(Long.parseLong(source));
        }
        if (source.length() == 10) {
            return new SimpleDateFormat("yyyy-MM-dd").parse(source);
        }
        if (source.charAt(2) == ':') {
            if (source.lastIndexOf('.') > -1) {
                new SimpleDateFormat("HH:mm:ss.SSS").parse(source);
            }
            return new SimpleDateFormat("HH:mm:ss").parse(source);
        }
        if (source.charAt(10) == ' ') {
            if (source.lastIndexOf('.') > -1) {
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(source);
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(source);
        }
        if (source.lastIndexOf('.') > -1) {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(source);
        }
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(source);
    }
}

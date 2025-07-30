package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;

import java.net.URL;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToURLConverter implements Converter<String, URL> {

    @Override
    public URL apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        URL resource = this.getClass().getResource(source);
        return resource != null ? resource : IOUtil.newURL(source);
    }
}

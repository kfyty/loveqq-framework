package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import feign.Response;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class LoveqqDecoder extends feign.jackson.JacksonDecoder {

    public LoveqqDecoder() {
        super();
    }

    public LoveqqDecoder(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.body() != null) {
            if (type == byte[].class) {
                return IOUtil.read(response.body().asInputStream());
            }
            if (type instanceof Class<?> clazz) {
                if (InputStream.class.isAssignableFrom(clazz)) {
                    return response.body().asInputStream();
                }
                if (ReflectUtil.isBaseDataType(clazz)) {
                    String body = new String(IOUtil.read(response.body().asInputStream()), response.charset());
                    return ConverterUtil.convert(body, clazz);
                }
            }
        }
        return super.decode(response, type);
    }
}

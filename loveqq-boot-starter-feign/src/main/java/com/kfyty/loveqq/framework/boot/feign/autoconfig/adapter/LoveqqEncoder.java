package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.form.ManyMultipartFileWriter;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.form.MultipartFileWriter;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import feign.RequestTemplate;
import feign.codec.Encoder;
import feign.form.ContentProcessor;
import feign.form.ContentType;
import feign.form.FormEncoder;
import feign.form.MultipartFormContentProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class LoveqqEncoder extends FormEncoder {

    public LoveqqEncoder() {
        super();
    }

    public LoveqqEncoder(Encoder delegate) {
        super(delegate);
        this.loadContentProcessor();
    }

    protected void loadContentProcessor() {
        ContentProcessor processor = this.getContentProcessor(ContentType.MULTIPART);
        if (processor instanceof MultipartFormContentProcessor) {
            MultipartFormContentProcessor formContentProcessor = (MultipartFormContentProcessor) processor;
            formContentProcessor.addFirstWriter(new ManyMultipartFileWriter());
            formContentProcessor.addFirstWriter(new MultipartFileWriter());
        } else {
            log.warn("MultipartFileWriter not support.");
        }
    }

    @Override
    @SneakyThrows(IOException.class)
    public void encode(Object object, Type bodyType, RequestTemplate template) {
        if (bodyType instanceof Class<?>) {
            Class<?> type = (Class<?>) bodyType;
            if (CharSequence.class.isAssignableFrom(type)) {
                template.body(object.toString());
                template.header("Content-Type", "application/json; charset=utf-8");
                return;
            }
            if (type == byte[].class) {
                template.body((byte[]) object, StandardCharsets.UTF_8);
                template.header("Content-Type", "application/octet-stream");
                return;
            }
            if (MultipartFile.class.isAssignableFrom(type)) {
                template.body(IOUtil.read(((MultipartFile) object).getInputStream()), StandardCharsets.UTF_8);
                template.header("Content-Type", "application/octet-stream");
                return;
            }
        }
        super.encode(object, bodyType, template);
    }
}

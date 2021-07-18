package com.kfyty.database.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 功能描述: 模板引擎工具
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 11:25
 * @since JDK 1.8
 */
@Slf4j
public abstract class TemplateEngineUtil {

    public static Template loadFreemarkerTemplate(String templatePath, String template) {
        try {
            Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configuration.setDefaultEncoding("UTF-8");
            configuration.setOutputEncoding("UTF-8");
            configuration.setClassicCompatible(true);
            configuration.setClassForTemplateLoading(CodeGeneratorTemplateEngineUtil.class, templatePath);
            return configuration.getTemplate(template);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

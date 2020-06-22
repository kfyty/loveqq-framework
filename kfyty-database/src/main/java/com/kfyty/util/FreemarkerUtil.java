package com.kfyty.util;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.Properties;

/**
 * 功能描述: freemarker 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 11:25
 * @since JDK 1.8
 */
public abstract class FreemarkerUtil {
    private static Properties CONFIG = null;
    private static String CONFIG_PATH = "/code-generate.properties";
    private static String TEMPLATE_BASE_PATH = "/template";

    public static Properties loadGenerateProperties() throws IOException {
        if(CONFIG != null) {
            return CONFIG;
        }
        CONFIG = new Properties();
        CONFIG.load(FreemarkerUtil.class.getResourceAsStream(CONFIG_PATH));
        return CONFIG;
    }

    public static Template loadTemplate(String prefix, String template) throws IOException {
        String templatePath = TEMPLATE_BASE_PATH;
        if(!CommonUtil.empty(prefix)) {
            templatePath += "/" + prefix;
        }
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setOutputEncoding("UTF-8");
        configuration.setClassForTemplateLoading(FreemarkerUtil.class, templatePath);
        return configuration.getTemplate(template);
    }
}

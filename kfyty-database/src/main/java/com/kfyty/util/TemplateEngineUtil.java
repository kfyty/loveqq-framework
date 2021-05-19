package com.kfyty.util;

import com.kfyty.generate.template.freemarker.FreemarkerTemplate;
import com.kfyty.generate.template.jsp.JspTemplate;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 功能描述: 模板引擎工具
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 11:25
 * @since JDK 1.8
 */
@Slf4j
public abstract class TemplateEngineUtil {
    private static Properties CONFIG = null;
    private final static String CONFIG_PATH = "/code-generate.properties";
    private final static String TEMPLATE_BASE_PATH = "/template";

    public static Properties loadGenerateProperties() throws IOException {
        if(CONFIG != null) {
            return CONFIG;
        }
        CONFIG = new Properties();
        CONFIG.load(TemplateEngineUtil.class.getResourceAsStream(CONFIG_PATH));
        return CONFIG;
    }

    public static Template loadFreemarkerTemplate(String prefix, String template) throws IOException {
        String templatePath = getTemplatePath(prefix);
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setOutputEncoding("UTF-8");
        configuration.setClassicCompatible(true);
        configuration.setClassForTemplateLoading(TemplateEngineUtil.class, templatePath);
        return configuration.getTemplate(template);
    }

    public static List<FreemarkerTemplate> loadFreemarkerTemplates(String prefix) throws Exception {
        String templateNames = getTemplateNames(prefix);
        if(CommonUtil.empty(templateNames)) {
            return Collections.emptyList();
        }
        return Arrays.stream(templateNames.split(",")).map(e -> new FreemarkerTemplate(prefix, e)).collect(Collectors.toList());
    }

    public static List<JspTemplate> loadJspTemplates(String prefix) throws Exception {
        String templateNames = getTemplateNames(prefix);
        if(CommonUtil.empty(templateNames)) {
            return Collections.emptyList();
        }
        String templatePath = getTemplatePath(prefix);
        List<File> jspFiles = Arrays.stream(templateNames.split(",")).flatMap(e -> new JstlTemplateEngineConfig(templatePath + "/" + e).getJspFiles().stream()).collect(Collectors.toList());
        List<JspTemplate> jspTemplates = new ArrayList<>(jspFiles.size());
        List<String> classes = new JstlTemplateEngine(new JstlTemplateEngineConfig(getTemplatePath(prefix), jspFiles)).compile();
        for (int i = 0; i < classes.size(); i++) {
            jspTemplates.add(new JspTemplate(prefix, jspFiles.get(i), classes.get(i)));
        }
        return jspTemplates;
    }

    public static String getTemplatePath(String prefix) {
        String templatePath = TEMPLATE_BASE_PATH;
        if(!CommonUtil.empty(prefix)) {
            templatePath += "/" + prefix;
        }
        return templatePath.replace(".", "/");
    }

    public static String getTemplateNames(String prefix) throws IOException {
        log.debug(": load template for prefix: '" + prefix + "' !");
        String key = CommonUtil.empty(prefix) ? "template" : prefix + ".template";
        return loadGenerateProperties().getProperty(key);
    }
}

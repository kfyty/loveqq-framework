package com.kfyty.database.util;

import com.kfyty.database.generator.template.freemarker.FreemarkerTemplate;
import com.kfyty.database.generator.template.jsp.JspTemplate;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 功能描述: 代码生成器模板引擎工具
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 11:25
 * @since JDK 1.8
 */
@Slf4j
public abstract class CodeGeneratorTemplateEngineUtil {
    /**
     * 代码生成器配置文件路径
     */
    private final static String CONFIG_PATH = "/code-generator.properties";

    /**
     * 代码生成器模板基础路径
     */
    private final static String TEMPLATE_BASE_PATH = "/template";

    /**
     * 代码生成器配置属性，支持 ${} 占位符
     */
    private static Properties CONFIG = null;

    public static Properties loadGeneratorProperties() throws IOException {
        return CONFIG != null ? CONFIG : (CONFIG = PropertiesUtil.load(CONFIG_PATH, CodeGeneratorTemplateEngineUtil.class));
    }

    public static List<? extends FreemarkerTemplate> loadFreemarkerTemplates(FreemarkerTemplate template, String prefix) throws Exception {
        String templateNames = getTemplateNames(prefix);
        if(CommonUtil.empty(templateNames)) {
            return Collections.emptyList();
        }
        return Arrays.stream(templateNames.split(",")).map(e -> template.create(prefix, e)).collect(Collectors.toList());
    }

    public static List<? extends JspTemplate> loadJspTemplates(JspTemplate template, String prefix) throws Exception {
        String templateNames = getTemplateNames(prefix);
        if(CommonUtil.empty(templateNames)) {
            return Collections.emptyList();
        }
        String templatePath = getTemplatePath(prefix);
        List<File> jspFiles = Arrays.stream(templateNames.split(",")).flatMap(e -> new JstlTemplateEngineConfig(templatePath + "/" + e).getJspFiles().stream()).collect(Collectors.toList());
        List<JspTemplate> jspTemplates = new ArrayList<>(jspFiles.size());
        List<String> classes = new JstlTemplateEngine(new JstlTemplateEngineConfig(getTemplatePath(prefix), jspFiles)).compile();
        for (int i = 0; i < classes.size(); i++) {
            jspTemplates.add(template.create(prefix, jspFiles.get(i), classes.get(i)));
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
        log.debug("load template for prefix: '" + prefix + "' !");
        String key = CommonUtil.empty(prefix) ? "template" : prefix + ".template";
        return loadGeneratorProperties().getProperty(key);
    }
}

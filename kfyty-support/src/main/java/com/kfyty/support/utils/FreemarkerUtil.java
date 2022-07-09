package com.kfyty.support.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: freemarker 模板工具，使用需自行导入依赖
 *
 * @author kfyty725
 * @date 2022/7/3 17:18
 * @email kfyty725@hotmail.com
 */
public abstract class FreemarkerUtil {
    public static final String FREEMARKER_SUFFIX = ".ftl";

    private static final Map<String, Configuration> CONFIG_CACHE = new ConcurrentHashMap<>(4);

    public static Configuration getConfiguration(String basePath) {
        return CONFIG_CACHE.computeIfAbsent(basePath, path -> {
            Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configuration.setDefaultEncoding("UTF-8");
            configuration.setOutputEncoding("UTF-8");
            configuration.setClassicCompatible(true);
            configuration.setClassForTemplateLoading(FreemarkerUtil.class, path);
            return configuration;
        });
    }

    public static Template getTemplate(String basePath, String path) {
        try {
            return getConfiguration(basePath).getTemplate(path);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Template buildTemplate(String basePath, String name, String template) {
        return buildTemplate(name, new StringReader(template), getConfiguration(basePath));
    }

    public static Template buildTemplate(String name, Reader reader, Configuration configuration) {
        try {
            return new Template(name, reader, configuration);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static String renderTemplate(String basePath, String path, Object params) {
        return renderTemplate(getTemplate(basePath, path), params);
    }

    public static String renderTemplate(Template template, Object params) {
        try {
            StringWriter render = new StringWriter();
            template.process(params, render);
            return render.toString();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}

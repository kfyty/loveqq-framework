package com.kfyty;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.parser.ClassAnnotationParser;
import com.kfyty.util.PackageUtil;

import java.util.Set;

/**
 * 功能描述: 启动类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:46
 * @since JDK 1.8
 */
public class KfytyApplication {
    private static ApplicationConfigurable applicationConfigurable;

    public static <T> T getResources(Class<T> clazz) {
        return (T) applicationConfigurable.getBeanResources().get(clazz);
    }

    public static void run(Class<?> clazz) throws Exception {
        run(clazz, clazz.getPackage().getName());
    }

    public static void run(Class<?> clazz, String basePackage) throws Exception {
        run(clazz, basePackage, false);
    }

    public static void run(Class<?> clazz, String basePackage, boolean ignoredBootAnnotation) throws Exception {
        Set<Class<?>> classSet = PackageUtil.parseBasePackage(basePackage);
        ClassAnnotationParser classAnnotationParser = new ClassAnnotationParser();
        applicationConfigurable = classAnnotationParser.initClassAnnotationParser();
        classAnnotationParser.parseClassAnnotation(clazz, classSet, ignoredBootAnnotation);
    }
}

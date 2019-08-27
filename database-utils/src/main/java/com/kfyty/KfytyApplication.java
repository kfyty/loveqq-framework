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
        new KfytyApplication().run(clazz, PackageUtil.parseBasePackage(clazz.getPackage().getName()));
    }

    public void run(Class<?> clazz, Set<Class<?>> classSet) throws Exception {
        applicationConfigurable = new ClassAnnotationParser().parseClassAnnotation(clazz, classSet);
    }
}

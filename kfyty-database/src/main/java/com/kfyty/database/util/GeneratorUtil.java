package com.kfyty.database.util;

import com.kfyty.database.generator.GenerateSources;
import com.kfyty.database.generator.config.GeneratorConfigurationSupport;
import com.kfyty.support.utils.ReflectUtil;

/**
 * 功能描述: 生成资源工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:26
 * @since JDK 1.8
 */
public abstract class GeneratorUtil {

    public static void generateSources(GeneratorConfigurationSupport generatorConfigurationSupport) throws Exception {
        new GenerateSources(generatorConfigurationSupport).doGenerate();
    }

    public static void generateSources(Class<? extends GeneratorConfigurationSupport> configurationSupportClass) throws Exception {
        generateSources(ReflectUtil.newInstance(configurationSupportClass));
    }
}

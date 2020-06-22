package com.kfyty.util;

import com.kfyty.generate.GenerateSources;
import com.kfyty.generate.configuration.GenerateConfiguration;

/**
 * 功能描述: 生成资源工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:26
 * @since JDK 1.8
 */
public class GenerateUtil {

    public static void generateSources(GenerateConfiguration generateConfiguration) throws Exception {
        new GenerateSources(generateConfiguration).generate();
    }

    public static void generateSources(Class<? extends GenerateConfiguration> generateConfigurationClass) throws Exception {
        generateSources(generateConfigurationClass.newInstance());
    }
}

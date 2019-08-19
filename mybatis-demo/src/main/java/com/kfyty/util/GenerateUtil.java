package com.kfyty.util;

import com.kfyty.generate.pojo.GeneratePojo;
import com.kfyty.generate.pojo.configuration.GeneratePojoConfiguration;

/**
 * 功能描述: 生成资源工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:26
 * @since JDK 1.8
 */
public class GenerateUtil {

    public static void generatePojo(GeneratePojoConfiguration generatePojoConfiguration) throws Exception {
        new GeneratePojo(generatePojoConfiguration).generate();
    }

    public static void generatePojo(Class<? extends GeneratePojoConfiguration> generatePojoConfigurationClass) throws Exception {
        generatePojo(generatePojoConfigurationClass.newInstance());
    }
}

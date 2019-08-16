package com.kfyty.util;

import com.kfyty.generate.pojo.GeneratePojo;
import com.kfyty.generate.pojo.GenerateTemplate;
import com.kfyty.generate.pojo.configuration.GeneratePojoConfiguration;

import java.io.IOException;
import java.util.List;

/**
 * 功能描述: 生成资源工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:26
 * @since JDK 1.8
 */
public class GenerateUtil {

    public static void generatePojo(GeneratePojoConfiguration generatePojoConfiguration) throws NoSuchMethodException, IOException {
        new GeneratePojo(generatePojoConfiguration).generate();
    }

    public static void generatePojo(List<GeneratePojoConfiguration> generatePojoConfigurations) throws NoSuchMethodException, IOException {
        new GeneratePojo(generatePojoConfigurations).generate();
    }

    public static void generatePojo(GeneratePojoConfiguration generatePojoConfiguration, GenerateTemplate generateTemplate) throws NoSuchMethodException, IOException {
        new GeneratePojo(generatePojoConfiguration, generateTemplate).generate();
    }

    public static void generatePojo(List<GeneratePojoConfiguration> generatePojoConfigurations, GenerateTemplate generateTemplate) throws NoSuchMethodException, IOException {
        new GeneratePojo(generatePojoConfigurations, generateTemplate).generate();
    }
}

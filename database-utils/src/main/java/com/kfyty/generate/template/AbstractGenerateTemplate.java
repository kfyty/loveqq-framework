package com.kfyty.generate.template;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.util.CommonUtil;

import java.io.IOException;

/**
 * 功能描述: 生成模板接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 16:07:16
 * @since JDK 1.8
 */
public interface AbstractGenerateTemplate {
    default String convert2JavaType(String dataBaseType) {
        return CommonUtil.convert2JavaType(dataBaseType);
    }

    default String fileSuffix() {
        return "";
    }

    default String fileTypeSuffix() {
        return ".java";
    }

    default boolean sameFile() {
        return false;
    }

    default void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {

    }
}

package com.kfyty.database.generator.template;

import com.kfyty.database.generator.config.GeneratorConfiguration;
import com.kfyty.core.io.SimpleBufferedWriter;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.core.utils.JdbcTypeUtil;

import java.io.IOException;
import java.util.Optional;

/**
 * 功能描述: 生成模板接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 16:07:16
 * @since JDK 1.8
 */
public interface GeneratorTemplate {
    /**
     * 将数据库数据类型转换为 java 类型
     *
     * @param databaseType 数据库数据类型
     * @return java 类型
     */
    default String convert2JavaType(String databaseType) {
        return Optional.ofNullable(JdbcTypeUtil.convert2JavaType(databaseType))
                .orElseThrow(() -> new IllegalArgumentException("no java data type matched for database type: [" +
                        databaseType + "], please override convert2JavaType method !"));
    }

    /**
     * 类名后缀 eg: Service、ServiceImpl
     * 该后缀可能会参与计算最终包名与最终保存路径
     *
     * @return 类名后缀
     */
    default String classSuffix() {
        return "";
    }

    /**
     * 文件后缀
     *
     * @return 默认 .java
     */
    default String fileTypeSuffix() {
        return ".java";
    }

    /**
     * 该模板应用的包名
     * 若返回无效值，则依据 basePackage + classSuffix() 进行计算
     *
     * @return 包名 eg: com.kfyty
     */
    default String packageName() {
        return null;
    }

    /**
     * 该模板保存的路径
     * 若返回无效值，则依据 GeneratorConfiguration.filePath + packageName() 进行计算
     *
     * @return 磁盘路径
     */
    default String filePath() {
        return null;
    }

    /**
     * 是否输出到同一个文件
     *
     * @return 默认 false
     */
    default boolean sameFile() {
        return false;
    }

    /**
     * 渲染模板
     *
     * @param tableInfo     数据表结构
     * @param configuration 生成器配置
     * @param out           输出流
     * @throws IOException IOException
     */
    default void doGenerate(AbstractTableStructInfo tableInfo, GeneratorConfiguration configuration, SimpleBufferedWriter out) throws IOException {
        this.doGenerate(tableInfo, configuration.getBasePackage(), out);
    }

    /**
     * 渲染模板
     *
     * @param tableInfo   数据表结构
     * @param basePackage 基础包名
     * @param out         输出流
     * @throws IOException IOException
     */
    default void doGenerate(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {

    }
}

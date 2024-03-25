package com.kfyty.database.generator.config;

import com.kfyty.database.generator.mapper.DatabaseMapper;
import com.kfyty.database.generator.template.GeneratorTemplate;

import javax.sql.DataSource;

/**
 * 功能描述: 生成代码配置接口，注解优先于返回值
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:33:20
 * @since JDK 1.8
 */
public interface GeneratorConfigurationSupport {

    DataSource getDataSource();

    default Class<? extends DatabaseMapper> databaseMapping() {
        return null;
    }

    default String databaseName() {
        return null;
    }

    default GeneratorTemplate[] getTemplates() {
        return new GeneratorTemplate[0];
    }

    default String[] table() {
        return null;
    }

    default String tablePrefix() {
        return "";
    }

    default boolean isRemoveTablePrefix() {
        return false;
    }

    default String basePackage() {
        return null;
    }

    default String filePath() {
        return null;
    }
}

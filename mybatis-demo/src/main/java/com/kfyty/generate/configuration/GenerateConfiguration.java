package com.kfyty.generate.configuration;

import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.template.AbstractGenerateTemplate;

import javax.sql.DataSource;

/**
 * 功能描述: 生成 pojo 配置接口，注解优先于返回值
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:33:20
 * @since JDK 1.8
 */
public interface GenerateConfiguration {

    DataSource getDataSource();

    default Class<? extends AbstractDataBaseMapper> dataBaseMapping() {
        return null;
    }

    default String dataBaseName() {
        return null;
    }

    default AbstractGenerateTemplate getGenerateTemplate() {
        return null;
    }

    default String[] table() {
        return null;
    }

    default String packageName() {
        return null;
    }

    default String filePath() {
        return null;
    }

    default Boolean sameFile() {
        return false;
    }
}

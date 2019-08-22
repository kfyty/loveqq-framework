package com.kfyty.generate.configuration;

import com.kfyty.generate.database.DataBaseMapper;
import com.kfyty.generate.template.GenerateTemplate;

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

    default Class<? extends DataBaseMapper> dataBaseMapping() {
        return null;
    }

    default String dataBaseName() {
        return null;
    }

    default GenerateTemplate getGenerateTemplate() {
        return new GenerateTemplate() {};
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

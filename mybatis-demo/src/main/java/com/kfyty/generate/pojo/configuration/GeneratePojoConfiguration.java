package com.kfyty.generate.pojo.configuration;

import com.kfyty.generate.pojo.GenerateTemplate;
import com.kfyty.generate.pojo.database.DataBaseMapper;

import javax.sql.DataSource;

/**
 * 功能描述: 生成 pojo 配置接口，注解优先于返回值
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:33:20
 * @since JDK 1.8
 */
public interface GeneratePojoConfiguration {

    DataSource getDataSource();

    Class<? extends DataBaseMapper> dataBaseMapping();

    String dataBaseName();

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

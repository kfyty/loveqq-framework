package com.kfyty.generate.pojo.configuration;

import javax.sql.DataSource;

/**
 * 功能描述: 生成 pojo 配置接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:33:20
 * @since JDK 1.8
 */
public interface GeneratePojoConfiguration {

    DataSource getDataSource();

    void dataBaseType();

    void dataBaseName();

    void table();

    void packageName();

    void fileSuffix();

    void filePath();
}

package com.kfyty.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 功能描述: 数据源工具
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/6/27 16:52
 * @since JDK 1.8
 */
@Slf4j
public class DataSourceUtil {
    public static DataSource getDataSource(String propertiesPath) {
        try {
            Properties properties = new Properties();
            properties.load(DataSourceUtil.class.getResourceAsStream(propertiesPath));
            return DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            log.error("load properties failed:", e);
        }
        return null;
    }
}

package com.kfyty.boot.data.jdbc.autoconfig;

import com.alibaba.druid.pool.DruidDataSource;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * 描述: 数据源自动配置
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = DataSourceProperties.class)
@ConditionalOnBean(DataSourceProperties.class)
public class DataSourceAutoConfiguration {
    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "k.datasource", value = "type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
    @ConfigurationProperties("k.datasource.druid")
    public DataSource druidDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUsername(this.dataSourceProperties.getUsername());
        druidDataSource.setPassword(this.dataSourceProperties.getPassword());
        druidDataSource.setUrl(this.dataSourceProperties.getUrl());
        druidDataSource.setDriverClassName(this.dataSourceProperties.getDriverClassName());
        return druidDataSource;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "k.datasource", value = "type", havingValue = "com.zaxxer.hikari.HikariDataSource")
    @ConfigurationProperties("k.datasource.hikari")
    public DataSource hikariDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setUsername(this.dataSourceProperties.getUsername());
        hikariDataSource.setPassword(this.dataSourceProperties.getPassword());
        hikariDataSource.setJdbcUrl(this.dataSourceProperties.getUrl());
        hikariDataSource.setDriverClassName(this.dataSourceProperties.getDriverClassName());
        return hikariDataSource;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "k.datasource", value = "type", havingValue = "org.apache.tomcat.jdbc.pool.DataSource")
    @ConfigurationProperties("k.datasource.tomcat")
    public DataSource tomcatJdbcPoolDataSource() {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setUsername(this.dataSourceProperties.getUsername());
        dataSource.setPassword(this.dataSourceProperties.getPassword());
        dataSource.setUrl(this.dataSourceProperties.getUrl());
        dataSource.setDriverClassName(this.dataSourceProperties.getDriverClassName());
        return dataSource;
    }
}

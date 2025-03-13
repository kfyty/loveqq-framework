package com.kfyty.loveqq.framework.boot.data.jdbc.autoconfig;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.kfyty.loveqq.framework.boot.data.jdbc.DataSourceAspect;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletRegistrationBean;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;

import javax.sql.DataSource;
import java.util.List;

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

    @Bean
    public DataSourceAspect defaultDataSourceAspect() {
        return new DataSourceAspect();
    }

    @Component
    @ConditionalOnProperty(prefix = "k.datasource", value = "type", havingValue = "com.zaxxer.hikari.HikariDataSource")
    public static class HikariDataSourceAutoConfig {
        @Autowired
        private DataSourceProperties dataSourceProperties;

        @ConditionalOnMissingBean
        @ConfigurationProperties("k.datasource.hikari")
        @Bean(destroyMethod = "close", resolveNested = false, independent = true)
        public DataSource hikariDataSource() {
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setUsername(this.dataSourceProperties.getUsername());
            hikariDataSource.setPassword(this.dataSourceProperties.getPassword());
            hikariDataSource.setJdbcUrl(this.dataSourceProperties.getUrl());
            hikariDataSource.setDriverClassName(this.dataSourceProperties.getDriverClassName());
            return hikariDataSource;
        }
    }

    @Component
    @Import(config = DruidDataSourceAutoConfig.DruidServletAutoConfig.class)
    @ConditionalOnProperty(prefix = "k.datasource", value = "type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
    public static class DruidDataSourceAutoConfig {
        @Autowired
        private DataSourceProperties dataSourceProperties;

        @ConditionalOnMissingBean
        @ConfigurationProperties("k.datasource.druid")
        @Bean(destroyMethod = "close", resolveNested = false, independent = true)
        public DataSource druidDataSource(@Autowired(required = false) List<Filter> filters) {
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setUsername(this.dataSourceProperties.getUsername());
            druidDataSource.setPassword(this.dataSourceProperties.getPassword());
            druidDataSource.setUrl(this.dataSourceProperties.getUrl());
            druidDataSource.setDriverClassName(this.dataSourceProperties.getDriverClassName());
            druidDataSource.setProxyFilters(filters);
            return druidDataSource;
        }

        @Component
        @ConditionalOnClass({"jakarta.servlet.ServletContext", "com.alibaba.druid.pool.DruidDataSource"})
        static class DruidServletAutoConfig {

            @ConditionalOnBean(ServletContext.class)
            @Bean(resolveNested = false, independent = true)
            @ConditionalOnProperty(prefix = "k.datasource.druid.statViewServlet", value = "enabled", havingValue = "true")
            public ServletRegistrationBean statViewServletBean(@Value("${k.datasource.druid.statViewServlet.allow:}") String allow,
                                                               @Value("${k.datasource.druid.statViewServlet.deny:}") String deny,
                                                               @Value("${k.datasource.druid.statViewServlet.remoteAddress:}") String remoteAddress,
                                                               @Value("${k.datasource.druid.statViewServlet.urlPattern}") String urlPattern,
                                                               @Value("${k.datasource.druid.statViewServlet.loginUsername}") String loginUsername,
                                                               @Value("${k.datasource.druid.statViewServlet.loginPassword}") String loginPassword) {
                return new ServletRegistrationBean()
                        .setServlet(new StatViewServlet())
                        .setUrlPatterns(CommonUtil.split(urlPattern, ","))
                        .addInitParam("allow", allow)
                        .addInitParam("deny", deny)
                        .addInitParam("remoteAddress", remoteAddress)
                        .addInitParam("loginUsername", loginUsername)
                        .addInitParam("loginPassword", loginPassword);
            }
        }
    }

    @Component
    @ConditionalOnProperty(prefix = "k.datasource", value = "type", havingValue = "org.apache.tomcat.jdbc.pool.DataSource")
    public static class TomcatDataSourceAutoConfig {
        @Autowired
        private DataSourceProperties dataSourceProperties;

        @ConditionalOnMissingBean
        @ConfigurationProperties("k.datasource.tomcat")
        @Bean(destroyMethod = "close", resolveNested = false, independent = true)
        public DataSource tomcatJdbcPoolDataSource() {
            org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
            dataSource.setUsername(this.dataSourceProperties.getUsername());
            dataSource.setPassword(this.dataSourceProperties.getPassword());
            dataSource.setUrl(this.dataSourceProperties.getUrl());
            dataSource.setDriverClassName(this.dataSourceProperties.getDriverClassName());
            return dataSource;
        }
    }
}

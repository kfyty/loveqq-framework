package com.kfyty.boot.pagehelper;

import com.github.pagehelper.PageInterceptor;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.utils.BeanUtil;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Properties;

/**
 * 描述: PageHelperAutoConfiguration
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
public class PageHelperAutoConfiguration implements InitializingBean {
    @Autowired
    private PageHelperProperties pageHelperProperties;

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactories;

    @Override
    public void afterPropertiesSet() {
        Properties properties = new Properties();
        properties.putAll(BeanUtil.copyProperties(this.pageHelperProperties));

        PageInterceptor interceptor = new PageInterceptor();
        interceptor.setProperties(properties);
        for (SqlSessionFactory sqlSessionFactory : this.sqlSessionFactories) {
            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            if (!containsInterceptor(configuration, interceptor)) {
                configuration.addInterceptor(interceptor);
            }
        }
    }

    private boolean containsInterceptor(org.apache.ibatis.session.Configuration configuration, Interceptor interceptor) {
        try {
            // getInterceptors since 3.2.2
            return configuration.getInterceptors().stream().anyMatch(config -> interceptor.getClass().isAssignableFrom(config.getClass()));
        } catch (Exception e) {
            return false;
        }
    }
}

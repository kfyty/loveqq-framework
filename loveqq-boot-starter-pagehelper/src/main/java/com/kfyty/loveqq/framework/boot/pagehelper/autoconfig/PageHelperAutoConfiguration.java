package com.kfyty.loveqq.framework.boot.pagehelper.autoconfig;

import com.github.pagehelper.PageInterceptor;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;

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
        properties.putAll(BeanUtil.copyProperties(this.pageHelperProperties, (f, v) -> v != null).entrySet().stream().collect(toMap(Map.Entry::getKey, v -> v.getValue().toString())));

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

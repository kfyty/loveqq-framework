package com.kfyty.loveqq.framework.boot.data.jdbc.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.Condition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.ConditionContext;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import lombok.Data;

import java.util.Map;

/**
 * 描述: 数据源自动配置类
 *
 * @author kfyty725
 * @date 2022/6/1 10:58
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.datasource")
@Conditional(DataSourceProperties.DataSourcePropertiesCondition.class)
public class DataSourceProperties {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * jdbc 连接 url
     */
    private String url;

    /**
     * 驱动类全限定名
     */
    private String driverClassName;

    static class DataSourcePropertiesCondition implements Condition {

        @Override
        public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
            PropertyContext propertyContext = context.getBeanFactory().getBean(PropertyContext.class);
            Map<String, String> properties = propertyContext.searchMapProperties("k.datasource");
            return properties != null && !properties.isEmpty();
        }
    }
}

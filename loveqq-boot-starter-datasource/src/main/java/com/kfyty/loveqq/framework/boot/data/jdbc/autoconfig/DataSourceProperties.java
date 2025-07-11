package com.kfyty.loveqq.framework.boot.data.jdbc.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;

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
@ConditionalOnProperty(value = "k.datasource", matchIfNotEmpty = true)
public class DataSourceProperties {
    /**
     * 数据源类型，即 class 的全限定名称
     */
    private String type;

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
}

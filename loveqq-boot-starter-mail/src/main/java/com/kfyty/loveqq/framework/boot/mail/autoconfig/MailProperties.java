package com.kfyty.loveqq.framework.boot.mail.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;

import java.util.Properties;

/**
 * 描述: mail 属性配置
 *
 * @author kfyty725
 * @date 2024/7/4 16:29
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.mail")
@ConditionalOnProperty(value = "k.mail.host", matchIfNonNull = true)
public class MailProperties {
    private String host;
    private Integer port;
    private String protocol;
    private String username;
    private String password;
    private Properties properties;
}

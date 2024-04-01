package com.kfyty.web.mvc.core.autoconfig;

import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * 描述: web server 配置属性
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.server")
public class WebServerProperties {
    private int port = 8080;
}

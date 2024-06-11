package com.kfyty.loveqq.framework.boot.feign.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * 描述: feign 配置属性
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.feign")
public class FeignProperties {
    /**
     * ribbon 轮训规则
     */
    private String rule;
}

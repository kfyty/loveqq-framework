package com.kfyty.loveqq.framework.boot.feign.autoconfig;

import com.kfyty.loveqq.framework.boot.feign.autoconfig.rule.DefaultRoundRobinRule;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.netflix.loadbalancer.IRule;
import feign.Request;
import lombok.Data;

import java.util.Map;

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
    private IRule rule = new DefaultRoundRobinRule();

    /**
     * 请求选项配置
     * key: feign 接口类名转换的 bean name
     * value: 请求选项配置
     *
     * @see com.kfyty.loveqq.framework.core.utils.BeanUtil#getBeanName(Class)
     */
    private Map<String, Request.Options> config;

    /**
     * ribbon 配置，该配置使用默认的命名空间
     * key: 服务客户端名称
     * value: 针对客户端的配置，{@link com.netflix.client.config.CommonClientConfigKey}
     */
    private Map<String, Map<String, String>> ribbon;
}

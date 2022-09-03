package com.kfyty.boot.xxl.job.autoconfig;

import com.kfyty.boot.xxl.job.XxlJobBootExecutor;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnProperty;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/9/3 18:03
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnProperty(value = "k.xxl.adminAddresses", matchIfNonNull = true)
public class XxlJobAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("k.xxl")
    public XxlJobBootExecutor xxlJobBootExecutor() {
        return new XxlJobBootExecutor();
    }
}

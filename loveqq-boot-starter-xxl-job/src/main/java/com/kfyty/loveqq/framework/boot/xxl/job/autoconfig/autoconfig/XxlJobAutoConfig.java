package com.kfyty.loveqq.framework.boot.xxl.job.autoconfig.autoconfig;

import com.kfyty.loveqq.framework.boot.xxl.job.autoconfig.XxlJobBootExecutor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/9/3 18:03
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnProperty(value = "k.xxl.job.adminAddresses", matchIfNonNull = true)
public class XxlJobAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("k.xxl.job")
    public XxlJobBootExecutor xxlJobBootExecutor() {
        return new XxlJobBootExecutor();
    }
}

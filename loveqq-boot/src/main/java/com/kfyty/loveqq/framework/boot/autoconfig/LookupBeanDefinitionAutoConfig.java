package com.kfyty.loveqq.framework.boot.autoconfig;

import com.kfyty.loveqq.framework.boot.autoconfig.support.LookupBeanDefinitionImport;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/22 10:11
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnMissingBean(LookupBeanDefinitionImport.class)
public class LookupBeanDefinitionAutoConfig {

    @Bean
    public LookupBeanDefinitionImport lookupBeanDefinitionImport() {
        return new LookupBeanDefinitionImport();
    }
}

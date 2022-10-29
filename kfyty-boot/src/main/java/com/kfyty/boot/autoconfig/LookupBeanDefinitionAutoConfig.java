package com.kfyty.boot.autoconfig;

import com.kfyty.boot.autoconfig.support.LookupBeanDefinitionImport;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;

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

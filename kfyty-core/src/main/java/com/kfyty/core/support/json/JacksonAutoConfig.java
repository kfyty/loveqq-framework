package com.kfyty.core.support.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.utils.JsonUtil;

/**
 * 描述: jackson 自动配置
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Configuration
public class JacksonAutoConfig implements ContextAfterRefreshed {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return JsonUtil.configure();
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        JsonUtil.configure(objectMapper);
    }
}

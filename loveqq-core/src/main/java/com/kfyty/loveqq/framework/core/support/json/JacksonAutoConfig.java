package com.kfyty.loveqq.framework.core.support.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;

/**
 * 描述: jackson 自动配置
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnWebApplication
public class JacksonAutoConfig implements ContextAfterRefreshed {

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public ObjectMapper objectMapper() {
        return JsonUtil.configure();
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        JsonUtil.configure(objectMapper);
    }
}

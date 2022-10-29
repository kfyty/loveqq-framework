package com.kfyty.boot.validator.autoconfig;

import com.kfyty.boot.validator.processor.MethodValidationBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * 描述: 校验器配置
 *
 * @author kfyty725
 * @date 2021/9/25 15:23
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidatorFactory validatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public Validator validator() {
        return this.validatorFactory().getValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodValidationBeanPostProcessor methodValidationBeanPostProcessor() {
        return new MethodValidationBeanPostProcessor();
    }
}

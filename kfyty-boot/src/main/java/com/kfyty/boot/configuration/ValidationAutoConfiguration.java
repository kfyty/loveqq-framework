package com.kfyty.boot.configuration;

import com.kfyty.boot.processor.MethodValidationBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
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
    public ValidatorFactory validatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }

    @Bean
    public Validator validator() {
        return this.validatorFactory().getValidator();
    }

    @Bean
    public MethodValidationBeanPostProcessor methodValidationBeanPostProcessor() {
        return new MethodValidationBeanPostProcessor();
    }
}

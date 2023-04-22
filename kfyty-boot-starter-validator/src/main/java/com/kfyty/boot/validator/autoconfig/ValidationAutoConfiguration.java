package com.kfyty.boot.validator.autoconfig;

import com.kfyty.boot.validator.context.IOCContext;
import com.kfyty.boot.validator.context.ValidatorContext;
import com.kfyty.boot.validator.processor.MethodValidationBeanPostProcessor;
import com.kfyty.boot.validator.support.IOC;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.event.ContextRefreshedEvent;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;

/**
 * 描述: 校验器配置
 *
 * @author kfyty725
 * @date 2021/9/25 15:23
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ValidationAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Bean
    @ConditionalOnMissingBean
    public jakarta.validation.Configuration<?> validatorConfiguration() {
        return Validation.byDefaultProvider().configure();
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidatorFactory validatorFactory() {
        jakarta.validation.Configuration<?> configure = this.validatorConfiguration();
        configure.addProperty(BaseHibernateValidatorConfiguration.FAIL_FAST, Boolean.TRUE.toString());
        return configure.buildValidatorFactory();
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ValidatorContext.setValidator(event.getSource().getBean(Validator.class));
        IOCContext.setIOC(new IOC() {

            @Override
            public <T> T getBean(Class<T> clazz) {
                return event.getSource().getBean(clazz);
            }
        });
    }
}

package com.kfyty.boot.validator.autoconfig;

import com.kfyty.boot.validator.context.IOCContext;
import com.kfyty.boot.validator.context.ValidatorContext;
import com.kfyty.boot.validator.processor.MethodValidationBeanPostProcessor;
import com.kfyty.boot.validator.proxy.ValidatorAccess;
import com.kfyty.boot.validator.proxy.ValidatorProxy;
import com.kfyty.boot.validator.support.IOC;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.event.ContextRefreshedEvent;
import com.kfyty.core.utils.ClassLoaderUtil;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;

import java.lang.reflect.Proxy;

/**
 * 描述: 校验器配置
 *
 * @author kfyty725
 * @date 2021/9/25 15:23
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ValidationAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Value("${k.validator.proxy:true}")
    private boolean isProxyValidator;

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
        Validator validator = this.validatorFactory().getValidator();
        return this.isProxyValidator ? this.createValidatorProxy(validator) : validator;
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

    /**
     * 为了方便无感使用，为 {@link Validator} 创建代理
     */
    protected Validator createValidatorProxy(Validator validator) {
        return (Validator) Proxy.newProxyInstance(ClassLoaderUtil.classLoader(this.getClass()), new Class[]{Validator.class}, new ValidatorProxy(new ValidatorAccess(validator)));
    }
}

package com.kfyty.loveqq.framework.boot.validator.autoconfig;

import com.kfyty.loveqq.framework.boot.validator.context.IOCContext;
import com.kfyty.loveqq.framework.boot.validator.context.ValidatorContext;
import com.kfyty.loveqq.framework.boot.validator.proxy.ValidatorAccess;
import com.kfyty.loveqq.framework.boot.validator.proxy.ValidatorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ValidatorContext.setValidator(event.getSource().getBean(Validator.class));
        IOCContext.setIOC(clazz -> event.getSource().getBean(clazz));
    }

    /**
     * 为了方便无感使用，为 {@link Validator} 创建代理
     */
    protected Validator createValidatorProxy(Validator validator) {
        return (Validator) Proxy.newProxyInstance(ClassLoaderUtil.classLoader(this.getClass()), new Class[]{Validator.class}, new ValidatorProxy(new ValidatorAccess(validator)));
    }
}

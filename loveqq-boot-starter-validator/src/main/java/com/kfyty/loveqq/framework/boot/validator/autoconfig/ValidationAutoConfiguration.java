package com.kfyty.loveqq.framework.boot.validator.autoconfig;

import com.kfyty.loveqq.framework.boot.validator.context.IOCContext;
import com.kfyty.loveqq.framework.boot.validator.context.ValidatorContext;
import com.kfyty.loveqq.framework.boot.validator.proxy.ValidatorAccess;
import com.kfyty.loveqq.framework.boot.validator.proxy.ValidatorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.i18n.I18nResourceBundle;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.ResourceBundle;

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

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public jakarta.validation.Configuration<?> validatorConfiguration() {
        return Validation.byDefaultProvider().configure();
    }

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public ValidatorFactory validatorFactory(jakarta.validation.Configuration<?> configure, @Autowired(required = false) I18nResourceBundle i18nResourceBundle) {
        // 配置国际化
        if (i18nResourceBundle != null) {
            configure.messageInterpolator(new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(i18nResourceBundle)));
        }

        // 配置快速失败
        configure.addProperty(BaseHibernateValidatorConfiguration.FAIL_FAST, Boolean.TRUE.toString());

        return configure.buildValidatorFactory();
    }

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public Validator validator(ValidatorFactory validatorFactory) {
        Validator validator = validatorFactory.getValidator();
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
        if (ConstantConfig.LOAD_TRANSFORMER && ClassLoaderUtil.isIndexedClassLoader()) {
            return validator;
        }
        return (Validator) Proxy.newProxyInstance(ClassLoaderUtil.classLoader(this.getClass()), new Class[]{Validator.class}, new ValidatorProxy(new ValidatorAccess(validator)));
    }

    @RequiredArgsConstructor
    protected static class MessageSourceResourceBundleLocator implements ResourceBundleLocator {
        /**
         * 国际化资源
         */
        protected final I18nResourceBundle i18nResourceBundle;

        @Override
        public ResourceBundle getResourceBundle(Locale locale) {
            ResourceBundle[] resourceBundle = this.i18nResourceBundle.getResourceBundle(locale);
            return resourceBundle.length < 1 ? null : resourceBundle[0];
        }
    }
}

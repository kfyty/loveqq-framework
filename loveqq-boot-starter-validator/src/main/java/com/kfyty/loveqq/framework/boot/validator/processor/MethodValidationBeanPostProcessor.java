package com.kfyty.loveqq.framework.boot.validator.processor;

import com.kfyty.loveqq.framework.boot.validator.proxy.MethodValidationInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

/**
 * 描述: 方法参数校验
 *
 * @author kfyty725
 * @date 2021/9/25 15:37
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnMissingBean
public class MethodValidationBeanPostProcessor extends AbstractProxyCreatorProcessor {
    @Autowired
    private Lazy<Validator> validator;

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return AnnotationUtil.hasAnnotation(beanType, Valid.class);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new MethodValidationInterceptorProxy(this.validator);
    }
}

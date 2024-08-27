package com.kfyty.loveqq.framework.boot.validator.proxy;

import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import jakarta.validation.Validator;
import lombok.Getter;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.validationcontext.BaseBeanValidationContext;
import org.hibernate.validator.internal.engine.validationcontext.ValidationContextBuilder;
import org.hibernate.validator.internal.engine.validationcontext.ValidatorScopedContext;
import org.hibernate.validator.internal.engine.valuecontext.BeanValueContext;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;

import java.lang.reflect.Method;

/**
 * 描述: 对 {@link org.hibernate.validator.internal.engine.ValidatorImpl} 私有属性/方法提供访问
 *
 * @author kfyty725
 * @date 2023/4/20 16:44
 * @email kfyty725@hotmail.com
 */
@Getter
public class ValidatorAccess {
    private final Validator validator;

    private final BeanMetaDataManager beanMetaDataManager;

    private final ValidatorScopedContext validatorScopedContext;

    private final Method getValidationContextBuilderMethod;

    private final Method getValueContextForPropertyValidationMethod;

    private final Method getValueContextForValueValidationMethod;

    public ValidatorAccess(Validator validator) {
        this.validator = validator;
        this.beanMetaDataManager = (BeanMetaDataManager) ReflectUtil.getFieldValue(this.validator, "beanMetaDataManager");
        this.validatorScopedContext = (ValidatorScopedContext) ReflectUtil.getFieldValue(this.validator, "validatorScopedContext");
        this.getValidationContextBuilderMethod = ReflectUtil.getMethod(this.validator.getClass(), "getValidationContextBuilder", true);
        this.getValueContextForPropertyValidationMethod = ReflectUtil.getMethod(this.validator.getClass(), "getValueContextForPropertyValidation", true, BaseBeanValidationContext.class, PathImpl.class);
        this.getValueContextForValueValidationMethod = ReflectUtil.getMethod(this.validator.getClass(), "getValueContextForValueValidation", true, Class.class, PathImpl.class);
    }

    /**
     * @see org.hibernate.validator.internal.engine.ValidatorImpl#getValidationContextBuilder
     */
    public ValidationContextBuilder getValidationContextBuilder() {
        return (ValidationContextBuilder) ReflectUtil.invokeMethod(this.validator, this.getValidationContextBuilderMethod);
    }

    /**
     * @see org.hibernate.validator.internal.engine.ValidatorImpl#getValueContextForPropertyValidation
     */
    public BeanValueContext<?, ?> getValueContextForPropertyValidation(BaseBeanValidationContext<?> validationContext, PathImpl propertyPath) {
        return (BeanValueContext<?, ?>) ReflectUtil.invokeMethod(this.validator, this.getValueContextForPropertyValidationMethod, validationContext, propertyPath);
    }

    /**
     * @see org.hibernate.validator.internal.engine.ValidatorImpl#getValueContextForValueValidation
     */
    @SuppressWarnings("unchecked")
    public BeanValueContext<?, Object> getValueContextForValueValidation(Class<?> rootBeanClass, PathImpl propertyPath) {
        return (BeanValueContext<?, Object>) ReflectUtil.invokeMethod(this.validator, this.getValueContextForValueValidationMethod, rootBeanClass, propertyPath);
    }
}

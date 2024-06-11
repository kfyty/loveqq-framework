package com.kfyty.loveqq.framework.boot.validator.proxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.validationcontext.BaseBeanValidationContext;
import org.hibernate.validator.internal.engine.valuecontext.BeanValueContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContexts;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 描述: 对 {@link jakarta.validation.Validator} 方法拦截操作枚举
 *
 * @author kfyty725
 * @date 2023/4/20 16:45
 * @email kfyty725@hotmail.com
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ValidatorProxyEnum implements ValidatorProxyOp {
    VALIDATE("validate") {
        @Override
        @SuppressWarnings("unchecked")
        public Object doOp(ValidatorAccess validatorAccess, Method method, Object[] args) throws Throwable {
            Class<Object> rootBeanClass = (Class<Object>) args[0].getClass();
            BeanMetaData<Object> rootBeanMetaData = validatorAccess.getBeanMetaDataManager().getBeanMetaData(rootBeanClass);
            if (!rootBeanMetaData.hasConstraints()) {
                return Collections.emptySet();
            }
            BaseBeanValidationContext<?> validationContext = validatorAccess.getValidationContextBuilder().forValidate(rootBeanClass, rootBeanMetaData, args[0]);
            BeanValueContext<?, Object> valueContext = ValueContexts.getLocalExecutionContextForBean(validatorAccess.getValidatorScopedContext().getParameterNameProvider(), args[0], validationContext.getRootBeanMetaData(), PathImpl.createRootPath());
            return this.doOp(validatorAccess, method, args, valueContext);
        }
    },

    VALIDATE_PROPERTY("validateProperty") {
        @Override
        @SuppressWarnings("unchecked")
        public Object doOp(ValidatorAccess validatorAccess, Method method, Object[] args) throws Throwable {
            Class<Object> rootBeanClass = (Class<Object>) args[0].getClass();
            BeanMetaData<Object> rootBeanMetaData = validatorAccess.getBeanMetaDataManager().getBeanMetaData(rootBeanClass);
            if (!rootBeanMetaData.hasConstraints()) {
                return Collections.emptySet();
            }
            PathImpl propertyPath = PathImpl.createPathFromString((String) args[1]);
            BaseBeanValidationContext<Object> validationContext = validatorAccess.getValidationContextBuilder().forValidateProperty(rootBeanClass, rootBeanMetaData, args[0], propertyPath);
            BeanValueContext<?, ?> valueContext = validatorAccess.getValueContextForPropertyValidation(validationContext, propertyPath);
            return this.doOp(validatorAccess, method, args, valueContext);
        }
    },

    VALIDATE_VALUE("validateValue") {
        @Override
        @SuppressWarnings("unchecked")
        public Object doOp(ValidatorAccess validatorAccess, Method method, Object[] args) throws Throwable {
            BeanMetaData<Object> rootBeanMetaData = validatorAccess.getBeanMetaDataManager().getBeanMetaData((Class<Object>) args[0]);
            if (!rootBeanMetaData.hasConstraints()) {
                return Collections.emptySet();
            }
            PathImpl propertyPath = PathImpl.createPathFromString((String) args[1]);
            BaseBeanValidationContext<?> validationContext = validatorAccess.getValidationContextBuilder().forValidateValue((Class<Object>) args[0], rootBeanMetaData, propertyPath);
            BeanValueContext<?, Object> valueContext = validatorAccess.getValueContextForValueValidation(validationContext.getRootBeanClass(), propertyPath);
            valueContext.setCurrentValidatedValue(args[2]);
            return this.doOp(validatorAccess, method, args, valueContext);
        }
    },

    NO_MATCH("NO_MATCH") {
        @Override
        public Object doOp(ValidatorAccess validatorAccess, Method method, Object[] args) throws Throwable {
            return method.invoke(validatorAccess.getValidator(), args);
        }
    };

    private final String name;

    public static ValidatorProxyEnum forName(String name) {
        for (ValidatorProxyEnum value : ValidatorProxyEnum.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return NO_MATCH;
    }
}
